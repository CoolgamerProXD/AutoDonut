package com.autodonut.client.economy;

import com.autodonut.config.AutoDonutConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;

import java.util.List;

/**
 * Buying a deal is deliberately simple and instant - no travel, no
 * navigation, nothing that looks different from you personally clicking the
 * listing: re-check the exact same still-open auction house GUI to make sure
 * that listing (same item, same price) is still really there, then click it
 * once, exactly like DonutSMP-style click-to-buy menus.
 */
public final class BuyFlowEngine {

    private static final double PRICE_EPSILON = 0.01;

    private BuyFlowEngine() {}

    public static void attemptBuy(AbstractContainerMenu menu, AhListing target) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameMode == null || menu == null || target == null) return;

        List<AhListing> fresh = AhScanner.scan(menu);

        AhListing match = null;
        for (AhListing l : fresh) {
            if (l.slotIndex == target.slotIndex && l.itemId.equals(target.itemId)
                    && Math.abs(l.totalPrice - target.totalPrice) < PRICE_EPSILON) {
                match = l;
                break;
            }
        }
        if (match == null) {
            // Slot contents may have shifted (e.g. someone else bought the one above it) - fall back to same item+price anywhere.
            for (AhListing l : fresh) {
                if (l.itemId.equals(target.itemId) && Math.abs(l.totalPrice - target.totalPrice) < PRICE_EPSILON) {
                    match = l;
                    break;
                }
            }
        }

        if (match == null) {
            mc.player.sendSystemMessage(Component.literal(
                    "[AutoDonut Economy] That deal isn't there anymore (sold or price changed) - not buying."));
            return;
        }

        mc.gameMode.handleContainerInput(menu.containerId, match.slotIndex, 0, ContainerInput.PICKUP, mc.player);
        mc.player.sendSystemMessage(Component.literal(
                "[AutoDonut Economy] Clicked to buy " + match.displayName + " x" + match.count
                        + " for " + formatPrice(match.totalPrice) + " - check your inventory/chat to confirm the server accepted it."));

        ProfitLedger.recordBuy(match.itemId, match.displayName, match.totalPrice);

        AutoDonutConfig cfg = AutoDonutConfig.get();
        Integer wanted = cfg.economyWantList.get(match.itemId);
        if (wanted != null) {
            int remaining = wanted - match.count;
            if (remaining <= 0) {
                cfg.economyWantList.remove(match.itemId);
                mc.player.sendSystemMessage(Component.literal("[AutoDonut Economy] Shopping list: " + match.displayName + " complete!"));
            } else {
                cfg.economyWantList.put(match.itemId, remaining);
                mc.player.sendSystemMessage(Component.literal("[AutoDonut Economy] Shopping list: " + remaining + " more " + match.displayName + " still wanted."));
            }
            AutoDonutConfig.save();
        }
    }

    private static String formatPrice(double price) {
        if (price == Math.floor(price)) return String.valueOf((long) price);
        return String.valueOf(price);
    }
}
