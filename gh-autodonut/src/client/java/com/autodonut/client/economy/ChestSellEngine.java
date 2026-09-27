package com.autodonut.client.economy;

import com.autodonut.config.AutoDonutConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Works on any currently-open chest-like container. For every distinct
 * sellable item found, it: picks the stack up (real click), places it into a
 * dedicated staging hotbar slot (real click), selects that hotbar slot (the
 * same packet your client sends when you press a number key), then types the
 * configured /ah sell command - one item type at a time, with a short pause
 * between each so it never fires faster than a human plausibly could.
 */
public final class ChestSellEngine {

    public enum Tier { AT_MARKET, BELOW_MARKET, ABOVE_MARKET }

    private static final class Job {
        final String itemId;
        final String displayName;
        final Deque<Integer> chestSlots;

        Job(String itemId, String displayName, Deque<Integer> chestSlots) {
            this.itemId = itemId;
            this.displayName = displayName;
            this.chestSlots = chestSlots;
        }
    }

    private static boolean active = false;
    private static AbstractContainerMenu menu;
    private static Tier tier;
    private static final Deque<Job> jobs = new ArrayDeque<>();
    private static Job currentJob;
    private static int hotbarMenuIndex;
    private static int hotbarSelected;
    private static int step = 0;
    private static int cooldown = 0;
    private static final List<String> soldSummary = new ArrayList<>();
    private static final List<String> skippedSummary = new ArrayList<>();

    private ChestSellEngine() {}

    public static boolean isActive() {
        return active;
    }

    public static void start(AbstractContainerMenu targetMenu, Tier t) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || targetMenu == null) return;

        if (active) {
            player.sendSystemMessage(Component.literal("[AutoDonut Chest Sell] Already running - please wait for it to finish."));
            return;
        }

        AutoDonutConfig cfg = AutoDonutConfig.get();
        int hotbarSlot = Math.max(0, Math.min(8, cfg.economyChestSellHotbarSlot));
        int[] range = sourceSlotRange(targetMenu);
        int sourceStart = range[0];
        int sourceEnd = range[1];
        if (sourceEnd <= sourceStart) {
            player.sendSystemMessage(Component.literal("[AutoDonut Chest Sell] Nothing to sell here."));
            return;
        }
        int hotbarMenuIdx = hotbarMenuIndex(targetMenu, hotbarSlot);

        if (!targetMenu.getCarried().isEmpty()) {
            player.sendSystemMessage(Component.literal("[AutoDonut Chest Sell] Your cursor is holding an item - place it down first."));
            return;
        }
        if (!targetMenu.getSlot(hotbarMenuIdx).getItem().isEmpty()) {
            player.sendSystemMessage(Component.literal(
                    "[AutoDonut Chest Sell] Hotbar slot " + (hotbarSlot + 1)
                            + " needs to be empty first (AutoDonut stages items there one at a time). "
                            + "Clear it, or change the staging slot in AutoDonut > Economy settings."));
            return;
        }

        Map<String, Deque<Integer>> byItem = new LinkedHashMap<>();
        Map<String, String> names = new HashMap<>();
        for (int i = sourceStart; i < sourceEnd; i++) {
            if (i == hotbarMenuIdx) continue; // don't treat the staging slot itself as something to sell
            Slot slot = targetMenu.slots.get(i);
            ItemStack stack = slot.getItem();
            if (stack == null || stack.isEmpty()) continue;
            String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            if (cfg.economyDoNotSellList.contains(id)) continue;
            byItem.computeIfAbsent(id, k -> new ArrayDeque<>()).add(i);
            names.put(id, stack.getHoverName().getString());
        }
        if (byItem.isEmpty()) {
            player.sendSystemMessage(Component.literal(
                    "[AutoDonut Chest Sell] Nothing sellable found (chest is empty, or everything in it is on your Do Not Sell list)."));
            return;
        }

        jobs.clear();
        for (Map.Entry<String, Deque<Integer>> e : byItem.entrySet()) {
            jobs.add(new Job(e.getKey(), names.get(e.getKey()), e.getValue()));
        }
        soldSummary.clear();
        skippedSummary.clear();
        menu = targetMenu;
        tier = t;
        hotbarMenuIndex = hotbarMenuIdx;
        hotbarSelected = hotbarSlot;
        currentJob = null;
        step = 0;
        cooldown = 0;
        active = true;

        player.sendSystemMessage(Component.literal("[AutoDonut Chest Sell] Starting (" + tierName(t) + ") - "
                + jobs.size() + " item type(s). Don't close the chest or touch your inventory until it's done."));
    }

    public static void tick() {
        if (!active) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null || player.containerMenu != menu || !(mc.gui.screen() instanceof AbstractContainerScreen<?>)) {
            abort("the chest was closed");
            return;
        }

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        if (currentJob == null) {
            if (jobs.isEmpty()) {
                finish();
                return;
            }
            currentJob = jobs.poll();
            step = 0;
        }

        AutoDonutConfig cfg = AutoDonutConfig.get();

        switch (step) {
            case 0 -> {
                while (!currentJob.chestSlots.isEmpty() && menu.getSlot(currentJob.chestSlots.peek()).getItem().isEmpty()) {
                    currentJob.chestSlots.poll();
                }
                if (currentJob.chestSlots.isEmpty()) {
                    currentJob = null;
                    return;
                }
                int chestSlot = currentJob.chestSlots.peek();
                mc.gameMode.handleContainerInput(menu.containerId, chestSlot, 0, ContainerInput.PICKUP, player);
                step = 1;
                cooldown = 2;
            }
            case 1 -> {
                if (menu.getCarried().isEmpty()) {
                    step = 0; // slot turned out empty (stack size 0?) - retry
                    return;
                }
                mc.gameMode.handleContainerInput(menu.containerId, hotbarMenuIndex, 0, ContainerInput.PICKUP, player);
                step = 2;
                cooldown = 2;
            }
            case 2 -> {
                player.getInventory().setSelectedSlot(hotbarSelected);
                player.connection.send(new ServerboundSetCarriedItemPacket(hotbarSelected));
                step = 3;
                cooldown = 3;
            }
            case 3 -> {
                ItemStack held = menu.getSlot(hotbarMenuIndex).getItem();
                if (held.isEmpty()) {
                    currentJob.chestSlots.poll();
                    step = 0;
                    return;
                }
                Double marketValue = MarketDatabase.getMarketValue(currentJob.itemId, cfg.economyMinListingsForPrice);
                if (marketValue == null) {
                    // Put it back rather than leave it stuck in the staging slot.
                    mc.gameMode.handleContainerInput(menu.containerId, hotbarMenuIndex, 0, ContainerInput.PICKUP, player);
                    Integer chestSlot = currentJob.chestSlots.peek();
                    if (chestSlot != null) {
                        mc.gameMode.handleContainerInput(menu.containerId, chestSlot, 0, ContainerInput.PICKUP, player);
                    }
                    skippedSummary.add(currentJob.displayName + " (no market data yet - open /ah and browse it a bit first)");
                    currentJob.chestSlots.clear();
                    currentJob = null;
                    step = 0;
                    cooldown = 2;
                    return;
                }

                double multiplier = switch (tier) {
                    case AT_MARKET -> 1.0;
                    case BELOW_MARKET -> 1.0 - (cfg.economySellBelowMarketPercent / 100.0);
                    case ABOVE_MARKET -> 1.0 + (cfg.economySellAboveMarketPercent / 100.0);
                };
                double price = marketValue * held.getCount() * multiplier;
                String formatted = formatPrice(price, cfg.economyPriceDecimalPlaces);
                String command = cfg.ahSellCommandTemplate.replace("{price}", formatted);
                player.connection.sendCommand(command);
                soldSummary.add(currentJob.displayName + " x" + held.getCount() + " for " + formatted);
                ProfitLedger.recordSell(currentJob.itemId, currentJob.displayName, price);

                currentJob.chestSlots.poll();
                if (currentJob.chestSlots.isEmpty()) currentJob = null;
                step = 0;
                cooldown = 10;
            }
            default -> {
                currentJob = null;
                step = 0;
            }
        }
    }

    private static void finish() {
        Minecraft mc = Minecraft.getInstance();
        active = false;
        menu = null;
        StringBuilder sb = new StringBuilder("[AutoDonut Chest Sell] Done. ");
        if (!soldSummary.isEmpty()) sb.append("Sold: ").append(String.join(", ", soldSummary)).append(". ");
        if (!skippedSummary.isEmpty()) sb.append("Skipped: ").append(String.join("; ", skippedSummary)).append(".");
        if (soldSummary.isEmpty() && skippedSummary.isEmpty()) sb.append("Nothing was sold.");
        if (mc.player != null) mc.player.sendSystemMessage(Component.literal(sb.toString()));
    }

    private static void abort(String reason) {
        Minecraft mc = Minecraft.getInstance();
        active = false;
        menu = null;
        jobs.clear();
        currentJob = null;
        if (mc.player != null) {
            mc.player.sendSystemMessage(Component.literal("[AutoDonut Chest Sell] Stopped because " + reason + "."));
        }
    }

    /** [startInclusive, endExclusive) of menu slot indices that are safe/sensible to offer for sale. */
    private static int[] sourceSlotRange(AbstractContainerMenu menu) {
        if (menu instanceof InventoryMenu) {
            // Player's own inventory: main inv (9-35) + hotbar (36-44). Skips crafting/result/armor/offhand.
            return new int[] { 9, 45 };
        }
        int containerCount = Math.max(0, menu.slots.size() - 36);
        return new int[] { 0, containerCount };
    }

    private static int hotbarMenuIndex(AbstractContainerMenu menu, int hotbarSlot) {
        if (menu instanceof InventoryMenu) {
            return 36 + hotbarSlot;
        }
        int containerCount = Math.max(0, menu.slots.size() - 36);
        return containerCount + 27 + hotbarSlot;
    }

    private static String formatPrice(double price, int decimals) {
        BigDecimal bd = BigDecimal.valueOf(price).setScale(Math.max(0, decimals), RoundingMode.HALF_UP);
        return bd.toPlainString();
    }

    private static String tierName(Tier t) {
        return switch (t) {
            case AT_MARKET -> "At Market";
            case BELOW_MARKET -> "Below Market";
            case ABOVE_MARKET -> "Above Market";
        };
    }
}
