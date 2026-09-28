package com.autodonut.client.economy;

import com.autodonut.config.AutoDonutConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Keybind-driven: sells whatever you're currently holding in your main hand,
 * right now, with no GUI needed at all - either via the AH tiered price
 * (like Chest Sell) or the flat sell-to-game command, both exactly the
 * commands you configured.
 */
public final class QuickSellEngine {

    private static String pendingItemId;
    private static String pendingDisplayName;
    private static long pendingSentAtMs;
    private static final long PENDING_TIMEOUT_MS = 4000L;

    private QuickSellEngine() {}

    public static void quickSell() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        ItemStack held = mc.player.getMainHandItem();
        if (held == null || held.isEmpty()) {
            mc.player.sendSystemMessage(Component.literal("[AutoDonut Quick Sell] You're not holding anything."));
            return;
        }

        AutoDonutConfig cfg = AutoDonutConfig.get();
        String itemId = BuiltInRegistries.ITEM.getKey(held.getItem()).toString();
        String displayName = held.getHoverName().getString();

        if (cfg.quickSellUseFlatSell) {
            pendingItemId = itemId;
            pendingDisplayName = displayName;
            pendingSentAtMs = System.currentTimeMillis();
            mc.player.connection.sendCommand(cfg.flatSellCommandTemplate);
            mc.player.sendSystemMessage(Component.literal("[AutoDonut Quick Sell] Sent flat sell command for " + displayName + " x" + held.getCount() + "."));
            return;
        }

        Double marketValue = MarketDatabase.getMarketValue(itemId, cfg.economyMinListingsForPrice);
        if (marketValue == null) {
            mc.player.sendSystemMessage(Component.literal(
                    "[AutoDonut Quick Sell] No market data yet for " + displayName + " - browse /ah a bit first, or switch Quick Sell to flat-sell mode in settings."));
            return;
        }

        double multiplier = switch (cfg.quickSellTier) {
            case "BELOW_MARKET" -> 1.0 - (cfg.economySellBelowMarketPercent / 100.0);
            case "ABOVE_MARKET" -> 1.0 + (cfg.economySellAboveMarketPercent / 100.0);
            default -> 1.0;
        };
        double price = marketValue * held.getCount() * multiplier;
        String formatted = formatPrice(price, cfg.economyPriceDecimalPlaces);
        String command = cfg.ahSellCommandTemplate.replace("{price}", formatted);
        mc.player.connection.sendCommand(command);
        mc.player.sendSystemMessage(Component.literal(
                "[AutoDonut Quick Sell] Listed " + displayName + " x" + held.getCount() + " for " + formatted + "."));
        ProfitLedger.recordSell(itemId, displayName, price);
    }

    /** Call this from the global chat/game message hook to learn what the flat /sell command actually paid. */
    public static void onGameMessage(Component message) {
        if (pendingItemId == null) return;
        if (System.currentTimeMillis() - pendingSentAtMs > PENDING_TIMEOUT_MS) {
            pendingItemId = null;
            return;
        }
        AutoDonutConfig cfg = AutoDonutConfig.get();
        Pattern pattern;
        try {
            pattern = Pattern.compile(cfg.flatSellConfirmPattern);
        } catch (RuntimeException e) {
            return;
        }
        Matcher m = pattern.matcher(message.getString());
        if (!m.find() || m.groupCount() < 1) return;
        try {
            double price = Double.parseDouble(m.group(1).replace(",", ""));
            ProfitLedger.recordSell(pendingItemId, pendingDisplayName, price);
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.sendSystemMessage(Component.literal(
                        "[AutoDonut Quick Sell] Learned flat-sell value: " + pendingDisplayName + " sold for " + price + "."));
            }
        } catch (NumberFormatException ignored) {
            // not a number after all - ignore
        } finally {
            pendingItemId = null;
        }
    }

    private static String formatPrice(double price, int decimals) {
        BigDecimal bd = BigDecimal.valueOf(price).setScale(Math.max(0, decimals), RoundingMode.HALF_UP);
        return bd.toPlainString();
    }
}
