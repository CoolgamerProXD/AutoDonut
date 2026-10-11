package com.autodonut.client.economy;

import com.autodonut.config.AutoDonutConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Watches scan results for anything on the user's watchlist and alerts in chat (with a per-item cooldown). */
public final class Watchlist {

    private static final long ALERT_COOLDOWN_MS = 60_000L;
    private static final Map<String, Long> lastAlertMs = new HashMap<>();

    private Watchlist() {}

    public static void checkAndAlert(List<AhListing> listings) {
        AutoDonutConfig cfg = AutoDonutConfig.get();
        if (cfg.economyWatchlist == null || cfg.economyWatchlist.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        long now = System.currentTimeMillis();
        for (AhListing listing : listings) {
            Double maxPrice = cfg.economyWatchlist.get(listing.itemId);
            if (maxPrice == null || listing.pricePerUnit > maxPrice) continue;
            long last = lastAlertMs.getOrDefault(listing.itemId, 0L);
            if (now - last < ALERT_COOLDOWN_MS) continue;
            lastAlertMs.put(listing.itemId, now);
            mc.player.sendSystemMessage(Component.literal(
                    "[AutoDonut Watchlist] " + listing.displayName + " x" + listing.count
                            + " listed at " + formatPrice(listing.totalPrice) + " (your target: "
                            + formatPrice(maxPrice) + "/unit or less) - check the Economy panel!"));
        }
    }

    private static String formatPrice(double price) {
        if (price == Math.floor(price)) return String.valueOf((long) price);
        return String.valueOf(price);
    }
}
