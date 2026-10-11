package com.autodonut.client.gui;

import com.autodonut.client.economy.MarketDatabase;
import com.autodonut.config.AutoDonutConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Settings for the Economy tab + Chest Sell. Every command/pattern here is a
 * best-guess default for a typical /ah plugin - if it doesn't match your
 * server, this is where you fix it (use /autodonuteconomy dumplore while an
 * item's tooltip is visible in a container GUI to see the exact text AutoDonut
 * is working with).
 */
public class AutoDonutEconomyScreen extends Screen {

    private final Screen parent;
    private EditBox ahKeywordBox;
    private EditBox sellCommandBox;
    private EditBox searchCommandBox;
    private EditBox priceRegexBox;
    private EditBox dealThresholdBox;
    private EditBox belowMarketBox;
    private EditBox aboveMarketBox;
    private EditBox minListingsBox;
    private EditBox hotbarSlotBox;
    private EditBox doNotSellBox;
    private Button quickSellTierButton;

    public AutoDonutEconomyScreen(Screen parent) {
        super(Component.literal("AutoDonut - Economy & Chest Sell"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        AutoDonutConfig cfg = AutoDonutConfig.get();
        int cx = this.width / 2;
        int left = cx - 150;
        int labelWidth = 150;
        int boxWidth = 150;
        int y = 16;

        this.addRenderableWidget(new StringWidget(left, y, 300, 16, this.title, this.font));
        y += 20;

        this.addRenderableWidget(Checkbox.builder(Component.literal("Enable Economy tab + Chest Sell"), this.font)
                .pos(left, y).selected(cfg.economyEnabled)
                .onValueChange((cb, v) -> { cfg.economyEnabled = v; AutoDonutConfig.save(); })
                .build());
        y += 24;

        this.addRenderableWidget(new StringWidget(left, y + 4, labelWidth, 12,
                Component.literal("Auction house GUI title contains:"), this.font));
        ahKeywordBox = new EditBox(this.font, left + labelWidth, y, boxWidth, 16, Component.literal("ah keyword"));
        ahKeywordBox.setMaxLength(64);
        ahKeywordBox.setValue(nullToEmpty(cfg.ahScreenTitleKeyword));
        ahKeywordBox.setResponder(v -> { cfg.ahScreenTitleKeyword = v; AutoDonutConfig.save(); });
        this.addRenderableWidget(ahKeywordBox);
        y += 20;

        this.addRenderableWidget(new StringWidget(left, y + 4, labelWidth, 12,
                Component.literal("Sell command ({price}):"), this.font));
        sellCommandBox = new EditBox(this.font, left + labelWidth, y, boxWidth, 16, Component.literal("sell cmd"));
        sellCommandBox.setMaxLength(128);
        sellCommandBox.setValue(nullToEmpty(cfg.ahSellCommandTemplate));
        sellCommandBox.setResponder(v -> { cfg.ahSellCommandTemplate = v; AutoDonutConfig.save(); });
        this.addRenderableWidget(sellCommandBox);
        y += 20;

        this.addRenderableWidget(new StringWidget(left, y + 4, labelWidth, 12,
                Component.literal("Search command ({item}):"), this.font));
        searchCommandBox = new EditBox(this.font, left + labelWidth, y, boxWidth, 16, Component.literal("search cmd"));
        searchCommandBox.setMaxLength(128);
        searchCommandBox.setValue(nullToEmpty(cfg.ahSearchCommandTemplate));
        searchCommandBox.setResponder(v -> { cfg.ahSearchCommandTemplate = v; AutoDonutConfig.save(); });
        this.addRenderableWidget(searchCommandBox);
        y += 20;

        this.addRenderableWidget(new StringWidget(left, y + 4, labelWidth, 12,
                Component.literal("Price regex (1 capture group):"), this.font));
        priceRegexBox = new EditBox(this.font, left + labelWidth, y, boxWidth, 16, Component.literal("price regex"));
        priceRegexBox.setMaxLength(128);
        priceRegexBox.setValue(nullToEmpty(cfg.economyPriceLorePattern));
        priceRegexBox.setResponder(v -> { cfg.economyPriceLorePattern = v; AutoDonutConfig.save(); });
        this.addRenderableWidget(priceRegexBox);
        y += 24;

        this.addRenderableWidget(new StringWidget(left, y + 4, labelWidth, 12,
                Component.literal("Deal threshold % below market:"), this.font));
        dealThresholdBox = numberBox(left + labelWidth, y, String.valueOf(cfg.economyDealThresholdPercent),
                v -> { cfg.economyDealThresholdPercent = v; AutoDonutConfig.save(); });
        this.addRenderableWidget(dealThresholdBox);
        y += 20;

        this.addRenderableWidget(new StringWidget(left, y + 4, labelWidth, 12,
                Component.literal("Below Market sell discount %:"), this.font));
        belowMarketBox = numberBox(left + labelWidth, y, String.valueOf(cfg.economySellBelowMarketPercent),
                v -> { cfg.economySellBelowMarketPercent = v; AutoDonutConfig.save(); });
        this.addRenderableWidget(belowMarketBox);
        y += 20;

        this.addRenderableWidget(new StringWidget(left, y + 4, labelWidth, 12,
                Component.literal("Above Market sell premium %:"), this.font));
        aboveMarketBox = numberBox(left + labelWidth, y, String.valueOf(cfg.economySellAboveMarketPercent),
                v -> { cfg.economySellAboveMarketPercent = v; AutoDonutConfig.save(); });
        this.addRenderableWidget(aboveMarketBox);
        y += 20;

        this.addRenderableWidget(new StringWidget(left, y + 4, labelWidth, 12,
                Component.literal("Min listings before trusting a price:"), this.font));
        minListingsBox = new EditBox(this.font, left + labelWidth, y, boxWidth, 16, Component.literal("min listings"));
        minListingsBox.setValue(String.valueOf(cfg.economyMinListingsForPrice));
        minListingsBox.setResponder(v -> {
            try { cfg.economyMinListingsForPrice = Math.max(1, Integer.parseInt(v.trim())); AutoDonutConfig.save(); }
            catch (NumberFormatException ignored) {}
        });
        this.addRenderableWidget(minListingsBox);
        y += 20;

        this.addRenderableWidget(new StringWidget(left, y + 4, labelWidth, 12,
                Component.literal("Chest Sell staging hotbar slot (1-9):"), this.font));
        hotbarSlotBox = new EditBox(this.font, left + labelWidth, y, boxWidth, 16, Component.literal("hotbar slot"));
        hotbarSlotBox.setValue(String.valueOf(cfg.economyChestSellHotbarSlot + 1));
        hotbarSlotBox.setResponder(v -> {
            try {
                int slot = Integer.parseInt(v.trim());
                cfg.economyChestSellHotbarSlot = Math.max(0, Math.min(8, slot - 1));
                AutoDonutConfig.save();
            } catch (NumberFormatException ignored) {}
        });
        this.addRenderableWidget(hotbarSlotBox);
        y += 24;

        this.addRenderableWidget(new StringWidget(left, y, 300, 12,
                Component.literal("Do Not Sell list (comma-separated item ids, e.g. minecraft:diamond):"), this.font));
        y += 14;
        doNotSellBox = new EditBox(this.font, left, y, 300, 16, Component.literal("do not sell"));
        doNotSellBox.setMaxLength(500);
        doNotSellBox.setValue(String.join(", ", cfg.economyDoNotSellList));
        doNotSellBox.setResponder(v -> {
            cfg.economyDoNotSellList.clear();
            for (String part : v.split(",")) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) cfg.economyDoNotSellList.add(trimmed);
            }
            AutoDonutConfig.save();
        });
        this.addRenderableWidget(doNotSellBox);
        y += 24;

        this.addRenderableWidget(new StringWidget(left, y + 4, labelWidth, 12,
                Component.literal("Quick-Sell keybind tier:"), this.font));
        quickSellTierButton = Button.builder(quickSellTierText(cfg), b -> {
            cfg.quickSellTier = switch (cfg.quickSellTier) {
                case "AT_MARKET" -> "BELOW_MARKET";
                case "BELOW_MARKET" -> "ABOVE_MARKET";
                default -> "AT_MARKET";
            };
            AutoDonutConfig.save();
            quickSellTierButton.setMessage(quickSellTierText(cfg));
        }).bounds(left + labelWidth, y, boxWidth, 16).build();
        this.addRenderableWidget(quickSellTierButton);
        y += 20;

        this.addRenderableWidget(Checkbox.builder(Component.literal("Quick-Sell keybind uses flat /sell instead of AH"), this.font)
                .pos(left, y).selected(cfg.quickSellUseFlatSell)
                .onValueChange((cb, v) -> { cfg.quickSellUseFlatSell = v; AutoDonutConfig.save(); })
                .build());
        y += 22;

        this.addRenderableWidget(new StringWidget(left, y + 4, labelWidth, 12,
                Component.literal("Flat sell command:"), this.font));
        EditBox flatSellBox = new EditBox(this.font, left + labelWidth, y, boxWidth, 16, Component.literal("flat sell cmd"));
        flatSellBox.setMaxLength(128);
        flatSellBox.setValue(nullToEmpty(cfg.flatSellCommandTemplate));
        flatSellBox.setResponder(v -> { cfg.flatSellCommandTemplate = v; AutoDonutConfig.save(); });
        this.addRenderableWidget(flatSellBox);
        y += 24;

        MultiLineTextWidget note = new MultiLineTextWidget(left, y, Component.literal(
                "None of the command/regex defaults above are guaranteed to match your server's real /ah plugin - " +
                "they're sensible guesses. Use /autodonuteconomy dumplore while hovering an /ah listing to see the " +
                "exact tooltip text and fix the regex/commands above if deals aren't showing up. Watchlist, shopping " +
                "list, ledger and price-sharing export/import all live under /autodonuteconomy - see the README."), this.font);
        note.setMaxWidth(300);
        this.addRenderableWidget(note);
        y += 58;

        this.addRenderableWidget(Button.builder(Component.literal("Show profit ledger"), b ->
                com.autodonut.client.economy.ProfitLedger.announceSummary())
                .bounds(left, y, 145, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Clear market history"), b -> {
            MarketDatabase.clear();
        }).bounds(left + 155, y, 145, 20).build());
        y += 26;

        this.addRenderableWidget(Button.builder(Component.literal("Back"), b -> onClose())
                .bounds(cx - 50, y, 100, 20).build());
    }

    private static Component quickSellTierText(AutoDonutConfig cfg) {
        return Component.literal("Tier: " + cfg.quickSellTier.replace('_', ' '));
    }

    private EditBox numberBox(int x, int y, String initial, java.util.function.DoubleConsumer onChange) {
        EditBox box = new EditBox(this.font, x, y, 150, 16, Component.literal("number"));
        box.setValue(initial);
        box.setResponder(v -> {
            try { onChange.accept(Double.parseDouble(v.trim())); }
            catch (NumberFormatException ignored) {}
        });
        return box;
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    @Override
    public void onClose() {
        AutoDonutConfig.save();
        this.minecraft.gui.setScreen(parent);
    }
}
