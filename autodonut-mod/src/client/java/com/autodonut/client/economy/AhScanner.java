package com.autodonut.client.economy;

import com.autodonut.config.AutoDonutConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads whatever is currently shown in an open GUI's non-player slots and
 * looks for a price in each item's tooltip/lore text. This is exactly what
 * you'd see just by hovering the items yourself - AutoDonut just does it for
 * every visible slot at once and remembers what it found.
 */
public final class AhScanner {

    private static List<AhListing> lastScan = new ArrayList<>();
    private static long lastScanTimeMs = 0;

    private AhScanner() {}

    public static List<AhListing> lastScan() {
        return lastScan;
    }

    public static long lastScanTimeMs() {
        return lastScanTimeMs;
    }

    /**
     * Scans every non-player-inventory slot of the given menu. Any listing
     * with a price AutoDonut could parse also gets folded into the market
     * value database (this is how the "market value" is learned over time -
     * purely from real listings you've actually looked at).
     */
    public static List<AhListing> scan(AbstractContainerMenu menu) {
        Minecraft mc = Minecraft.getInstance();
        List<AhListing> found = new ArrayList<>();
        if (mc.player == null || mc.level == null || menu == null) return found;

        AutoDonutConfig cfg = AutoDonutConfig.get();
        Pattern pricePattern;
        try {
            pricePattern = Pattern.compile(cfg.economyPriceLorePattern);
        } catch (RuntimeException e) {
            pricePattern = Pattern.compile("\\$([0-9][0-9,]*(?:\\.[0-9]+)?)");
        }

        int containerSlotCount = Math.max(0, menu.slots.size() - 36);
        Item.TooltipContext ctx = Item.TooltipContext.of(mc.level);

        for (int i = 0; i < containerSlotCount; i++) {
            Slot slot = menu.slots.get(i);
            ItemStack stack = slot.getItem();
            if (stack == null || stack.isEmpty()) continue;

            List<Component> lines;
            try {
                lines = stack.getTooltipLines(ctx, mc.player, TooltipFlag.NORMAL);
            } catch (RuntimeException e) {
                continue;
            }

            Double price = null;
            for (Component line : lines) {
                Matcher m = pricePattern.matcher(line.getString());
                if (m.find() && m.groupCount() >= 1) {
                    try {
                        price = Double.parseDouble(m.group(1).replace(",", ""));
                        break;
                    } catch (NumberFormatException ignored) {
                        // keep looking at other lines
                    }
                }
            }
            if (price == null || price <= 0) continue;

            String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            String displayName = stack.getHoverName().getString();
            AhListing listing = new AhListing(i, itemId, displayName, price, stack.getCount());
            found.add(listing);
            MarketDatabase.recordPrice(itemId, listing.pricePerUnit);
        }

        lastScan = found;
        lastScanTimeMs = System.currentTimeMillis();
        Watchlist.checkAndAlert(found);
        return found;
    }

    /** Dumps every non-empty slot's tooltip lines to chat, for tuning economyPriceLorePattern. */
    public static void debugDumpLore(AbstractContainerMenu menu) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || menu == null) return;
        int containerSlotCount = Math.max(0, menu.slots.size() - 36);
        Item.TooltipContext ctx = Item.TooltipContext.of(mc.level);
        mc.player.sendSystemMessage(Component.literal("[AutoDonut] --- lore dump of " + containerSlotCount + " slot(s) ---"));
        for (int i = 0; i < containerSlotCount; i++) {
            ItemStack stack = menu.slots.get(i).getItem();
            if (stack == null || stack.isEmpty()) continue;
            List<Component> lines;
            try {
                lines = stack.getTooltipLines(ctx, mc.player, TooltipFlag.NORMAL);
            } catch (RuntimeException e) {
                continue;
            }
            mc.player.sendSystemMessage(Component.literal("[slot " + i + "] " + stack.getItem() + " x" + stack.getCount() + ":"));
            for (Component line : lines) {
                mc.player.sendSystemMessage(Component.literal("    " + line.getString()));
            }
        }
        mc.player.sendSystemMessage(Component.literal("[AutoDonut] --- end lore dump ---"));
    }
}
