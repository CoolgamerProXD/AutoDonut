package com.autodonut.client.economy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A small persistent "what does this actually sell for" database, built
 * purely by passively watching real /ah listings whenever you have that GUI
 * open. Nothing here ever contacts a server other than the one you're
 * already connected to, and nothing here is sent anywhere - it's just a
 * local price-history file (config/autodonut_market.json).
 */
public final class MarketDatabase {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int MAX_SAMPLES_PER_ITEM = 200;

    private static Path dbPath;
    private static Map<String, List<Double>> prices;

    private MarketDatabase() {}

    private static synchronized void ensureLoaded() {
        if (prices != null) return;
        try {
            dbPath = FabricLoader.getInstance().getConfigDir().resolve("autodonut_market.json");
            if (Files.exists(dbPath)) {
                try (Reader reader = Files.newBufferedReader(dbPath, StandardCharsets.UTF_8)) {
                    Type type = new TypeToken<Map<String, List<Double>>>() {}.getType();
                    prices = GSON.fromJson(reader, type);
                }
            }
        } catch (IOException | RuntimeException ignored) {
            // Corrupt/missing file - just start fresh.
        }
        if (prices == null) prices = new HashMap<>();
    }

    /** Records one observed per-unit listing price for an item id (e.g. "minecraft:diamond"). */
    public static synchronized void recordPrice(String itemId, double perUnitPrice) {
        if (itemId == null || perUnitPrice <= 0 || !Double.isFinite(perUnitPrice)) return;
        ensureLoaded();
        List<Double> list = prices.computeIfAbsent(itemId, k -> new ArrayList<>());
        list.add(perUnitPrice);
        while (list.size() > MAX_SAMPLES_PER_ITEM) list.remove(0);
        save();
    }

    /** Median observed per-unit price, or null if we haven't seen at least minSamples listings yet. */
    public static synchronized Double getMarketValue(String itemId, int minSamples) {
        ensureLoaded();
        List<Double> list = prices.get(itemId);
        if (list == null || list.size() < Math.max(1, minSamples)) return null;
        List<Double> sorted = new ArrayList<>(list);
        Collections.sort(sorted);
        int n = sorted.size();
        return (n % 2 == 1) ? sorted.get(n / 2) : (sorted.get(n / 2 - 1) + sorted.get(n / 2)) / 2.0;
    }

    public static synchronized int getSampleCount(String itemId) {
        ensureLoaded();
        List<Double> list = prices.get(itemId);
        return list == null ? 0 : list.size();
    }

    public static synchronized void clear() {
        ensureLoaded();
        prices.clear();
        save();
    }

    /**
     * Rough trend as a % change between the older half and newer half of the
     * samples we've seen (positive = rising, negative = falling). Returns
     * null if there isn't enough history yet to say anything meaningful.
     */
    public static synchronized Double getTrendPercent(String itemId) {
        ensureLoaded();
        List<Double> list = prices.get(itemId);
        if (list == null || list.size() < 4) return null;
        int mid = list.size() / 2;
        double oldAvg = average(list.subList(0, mid));
        double newAvg = average(list.subList(mid, list.size()));
        if (oldAvg <= 0) return null;
        return (newAvg - oldAvg) / oldAvg * 100.0;
    }

    private static double average(List<Double> values) {
        double sum = 0;
        for (double v : values) sum += v;
        return values.isEmpty() ? 0 : sum / values.size();
    }

    /** Writes the whole price database out to an arbitrary file, e.g. to share with a friend. */
    public static synchronized boolean exportTo(Path target) {
        ensureLoaded();
        try {
            Files.createDirectories(target.toAbsolutePath().getParent());
            try (Writer writer = Files.newBufferedWriter(target, StandardCharsets.UTF_8)) {
                GSON.toJson(prices, writer);
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /** Merges another AutoDonut market file (e.g. a friend's) into this one. */
    public static synchronized boolean importFrom(Path source) {
        ensureLoaded();
        try {
            if (!Files.exists(source)) return false;
            Map<String, List<Double>> incoming;
            try (Reader reader = Files.newBufferedReader(source, StandardCharsets.UTF_8)) {
                Type type = new TypeToken<Map<String, List<Double>>>() {}.getType();
                incoming = GSON.fromJson(reader, type);
            }
            if (incoming == null) return false;
            for (Map.Entry<String, List<Double>> entry : incoming.entrySet()) {
                List<Double> mine = prices.computeIfAbsent(entry.getKey(), k -> new ArrayList<>());
                mine.addAll(entry.getValue());
                while (mine.size() > MAX_SAMPLES_PER_ITEM) mine.remove(0);
            }
            save();
            return true;
        } catch (IOException | RuntimeException e) {
            return false;
        }
    }

    private static void save() {
        try {
            if (dbPath == null) dbPath = FabricLoader.getInstance().getConfigDir().resolve("autodonut_market.json");
            Files.createDirectories(dbPath.getParent());
            try (Writer writer = Files.newBufferedWriter(dbPath, StandardCharsets.UTF_8)) {
                GSON.toJson(prices, writer);
            }
        } catch (IOException ignored) {
            // Non-fatal: price history just won't persist this session.
        }
    }
}
