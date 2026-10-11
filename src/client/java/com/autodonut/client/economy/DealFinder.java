package com.autodonut.client.economy;

import com.autodonut.config.AutoDonutConfig;

import java.util.ArrayList;
import java.util.List;

/** Compares fresh listings against the local market-value database to find underpriced deals. */
public final class DealFinder {

    private DealFinder() {}

    public static final class Deal {
        public final AhListing listing;
        public final double marketValuePerUnit;
        public final double discountPercent;

        Deal(AhListing listing, double marketValuePerUnit, double discountPercent) {
            this.listing = listing;
            this.marketValuePerUnit = marketValuePerUnit;
            this.discountPercent = discountPercent;
        }
    }

    public static List<Deal> findDeals(List<AhListing> listings, AutoDonutConfig cfg) {
        List<Deal> deals = new ArrayList<>();
        for (AhListing listing : listings) {
            Double marketValue = MarketDatabase.getMarketValue(listing.itemId, cfg.economyMinListingsForPrice);
            if (marketValue == null || marketValue <= 0) continue;
            double discount = (marketValue - listing.pricePerUnit) / marketValue * 100.0;
            if (discount >= cfg.economyDealThresholdPercent) {
                deals.add(new Deal(listing, marketValue, discount));
            }
        }
        deals.sort((a, b) -> Double.compare(b.discountPercent, a.discountPercent));
        return deals;
    }
}
