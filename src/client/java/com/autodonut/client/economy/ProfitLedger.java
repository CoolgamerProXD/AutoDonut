package com.autodonut.client.economy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** A simple local log of everything AutoDonut bought/sold for you, with running totals. */
public final class ProfitLedger {

    public static final class Entry {
        public String type; // "buy" or "sell"
        public String itemId;
        public String displayName;
        public double amount;
        public long timestampMs;
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path ledgerPath;
    private static List<Entry> entries;

    private ProfitLedger() {}

    private static synchronized void ensureLoaded() {
        if (entries != null) return;
        try {
            ledgerPath = FabricLoader.getInstance().getConfigDir().resolve("autodonut_ledger.json");
            if (Files.exists(ledgerPath)) {
                try (Reader reader = Files.newBufferedReader(ledgerPath, StandardCharsets.UTF_8)) {
                    Entry[] loaded = GSON.fromJson(reader, Entry[].class);
                    if (loaded != null) entries = new ArrayList<>(List.of(loaded));
                }
            }
        } catch (IOException | RuntimeException ignored) {
            // start fresh
        }
        if (entries == null) entries = new ArrayList<>();
    }

    public static synchronized void recordBuy(String itemId, String displayName, double amount) {
        record("buy", itemId, displayName, amount);
    }

    public static synchronized void recordSell(String itemId, String displayName, double amount) {
        record("sell", itemId, displayName, amount);
    }

    private static void record(String type, String itemId, String displayName, double amount) {
        ensureLoaded();
        Entry e = new Entry();
        e.type = type;
        e.itemId = itemId;
        e.displayName = displayName;
        e.amount = amount;
        e.timestampMs = Instant.now().toEpochMilli();
        entries.add(e);
        save();
    }

    public static synchronized double totalSpent() {
        ensureLoaded();
        double sum = 0;
        for (Entry e : entries) if ("buy".equals(e.type)) sum += e.amount;
        return sum;
    }

    public static synchronized double totalEarned() {
        ensureLoaded();
        double sum = 0;
        for (Entry e : entries) if ("sell".equals(e.type)) sum += e.amount;
        return sum;
    }

    public static synchronized double netProfit() {
        return totalEarned() - totalSpent();
    }

    public static synchronized int entryCount() {
        ensureLoaded();
        return entries.size();
    }

    public static void announceSummary() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.player.sendSystemMessage(Component.literal(String.format(
                "[AutoDonut Economy] Ledger: earned $%.2f, spent $%.2f, net $%.2f over %d transaction(s).",
                totalEarned(), totalSpent(), netProfit(), entryCount())));
    }

    public static synchronized void clear() {
        ensureLoaded();
        entries.clear();
        save();
    }

    private static void save() {
        try {
            if (ledgerPath == null) ledgerPath = FabricLoader.getInstance().getConfigDir().resolve("autodonut_ledger.json");
            Files.createDirectories(ledgerPath.getParent());
            try (Writer writer = Files.newBufferedWriter(ledgerPath, StandardCharsets.UTF_8)) {
                GSON.toJson(entries, writer);
            }
        } catch (IOException ignored) {
            // Non-fatal.
        }
    }
}
