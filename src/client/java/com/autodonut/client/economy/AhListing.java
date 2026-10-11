package com.autodonut.client.economy;

/** One priced listing seen in an open auction-house GUI at scan time. */
public final class AhListing {
    public final int slotIndex;
    public final String itemId;
    public final String displayName;
    public final double totalPrice;
    public final int count;
    public final double pricePerUnit;

    public AhListing(int slotIndex, String itemId, String displayName, double totalPrice, int count) {
        this.slotIndex = slotIndex;
        this.itemId = itemId;
        this.displayName = displayName;
        this.totalPrice = totalPrice;
        this.count = Math.max(1, count);
        this.pricePerUnit = totalPrice / this.count;
    }
}
