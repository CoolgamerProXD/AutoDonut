package com.autodonut.client.economy;

import com.autodonut.config.AutoDonutConfig;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds a small AutoDonut panel on top of whatever container GUI you already
 * have open - it never replaces the screen or closes your container, so
 * there's nothing to navigate to and nothing that could interrupt a trade.
 */
public final class EconomyGuiHooks {

    private static final int MAX_DEAL_ROWS = 6;

    private EconomyGuiHooks() {}

    public static void register() {
        ScreenEvents.AFTER_INIT.register(EconomyGuiHooks::onScreenInit);
    }

    private static void onScreenInit(Minecraft client, Screen screen, int scaledWidth, int scaledHeight) {
        AutoDonutConfig cfg = AutoDonutConfig.get();
        if (!cfg.economyEnabled) return;
        if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) return;
        AbstractContainerMenu menu = containerScreen.getMenu();
        if (menu instanceof InventoryMenu) {
            // Your own inventory screen (E) - offer to sell straight out of your own inventory too.
            setupChestSellPanel(screen, menu);
            return;
        }

        String title = screen.getTitle().getString();
        boolean isAhScreen = cfg.ahScreenTitleKeyword != null && !cfg.ahScreenTitleKeyword.isBlank()
                && title.toLowerCase().contains(cfg.ahScreenTitleKeyword.toLowerCase());

        if (isAhScreen) {
            setupAhPanel(screen, menu);
        } else {
            setupChestSellPanel(screen, menu);
        }
    }

    private static void setupAhPanel(Screen screen, AbstractContainerMenu menu) {
        List<AbstractWidget> widgets = Screens.getWidgets(screen);
        int x = screen.width - 175;
        int y = 6;

        AhScanner.scan(menu);

        EditBox searchBox = new EditBox(screen.getFont(), x, y, 115, 16, Component.literal("search"));
        searchBox.setMaxLength(64);
        searchBox.setValue("");
        widgets.add(searchBox);

        widgets.add(Button.builder(Component.literal("Go"), b -> {
            AutoDonutConfig cfg = AutoDonutConfig.get();
            String item = searchBox.getValue().trim();
            if (!item.isEmpty() && cfg.ahSearchCommandTemplate != null) {
                Minecraft.getInstance().player.connection.sendCommand(cfg.ahSearchCommandTemplate.replace("{item}", item));
            }
        }).bounds(x + 120, y, 30, 16).build());

        List<AbstractWidget> dealRows = new ArrayList<>();
        Button rescanButton = Button.builder(Component.literal("Rescan Deals"), b ->
                refreshDeals(screen, menu, dealRows))
                .bounds(x, y + 20, 150, 16).build();
        widgets.add(rescanButton);

        refreshDeals(screen, menu, dealRows);
    }

    private static void refreshDeals(Screen screen, AbstractContainerMenu menu, List<AbstractWidget> dealRows) {
        List<AbstractWidget> widgets = Screens.getWidgets(screen);
        widgets.removeAll(dealRows);
        dealRows.clear();

        AutoDonutConfig cfg = AutoDonutConfig.get();
        List<AhListing> listings = AhScanner.scan(menu);
        List<DealFinder.Deal> deals = DealFinder.findDeals(listings, cfg);

        int x = screen.width - 175;
        int y = 42;
        int shown = 0;
        for (DealFinder.Deal deal : deals) {
            if (shown >= MAX_DEAL_ROWS) break;
            Double trend = MarketDatabase.getTrendPercent(deal.listing.itemId);
            String trendArrow = trend == null ? "" : (trend > 1 ? " \u2191" : (trend < -1 ? " \u2193" : ""));
            Integer wanted = cfg.economyWantList.get(deal.listing.itemId);
            String wantTag = wanted == null ? "" : " [want " + wanted + "]";
            String label = trim(deal.listing.displayName, 10) + " $" + trim(deal.listing.totalPrice)
                    + " (-" + Math.round(deal.discountPercent) + "%)" + trendArrow + wantTag;
            Button row = Button.builder(Component.literal(label), b -> {
                BuyFlowEngine.attemptBuy(menu, deal.listing);
                refreshDeals(screen, menu, dealRows);
            }).bounds(x, y, 150, 16).build();
            widgets.add(row);
            dealRows.add(row);
            y += 18;
            shown++;
        }
        if (shown == 0) {
            Button none = Button.builder(Component.literal("No deals found right now"), b -> {})
                    .bounds(x, y, 150, 16).build();
            none.active = false;
            widgets.add(none);
            dealRows.add(none);
        }
    }

    private static void setupChestSellPanel(Screen screen, AbstractContainerMenu menu) {
        if (!(menu instanceof InventoryMenu)) {
            int containerSlots = menu.slots.size() - 36;
            if (containerSlots <= 0) return;
        }

        List<AbstractWidget> widgets = Screens.getWidgets(screen);
        int x = screen.width - 145;
        widgets.add(Button.builder(Component.literal("Sell: At Market"), b ->
                ChestSellEngine.start(menu, ChestSellEngine.Tier.AT_MARKET)).bounds(x, 6, 135, 16).build());
        widgets.add(Button.builder(Component.literal("Sell: Below Market"), b ->
                ChestSellEngine.start(menu, ChestSellEngine.Tier.BELOW_MARKET)).bounds(x, 24, 135, 16).build());
        widgets.add(Button.builder(Component.literal("Sell: Above Market"), b ->
                ChestSellEngine.start(menu, ChestSellEngine.Tier.ABOVE_MARKET)).bounds(x, 42, 135, 16).build());
    }

    private static String trim(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + ".";
    }

    private static String trim(double price) {
        if (price == Math.floor(price)) return String.valueOf((long) price);
        return String.valueOf(price);
    }
}
