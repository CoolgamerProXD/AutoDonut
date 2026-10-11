package com.autodonut.client.esp;

import com.autodonut.config.AutoDonutConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Block ESP - finds blocks you care about near you and remembers where they are.
 *
 * <p><b>Credit:</b> this feature exists because of
 * <a href="https://www.curseforge.com/minecraft/mc-mods/block-esp">BaseFinder
 * (Block ESP)</a> by ShadowsIsNothing. That mod is All Rights Reserved, so
 * none of its code is used here - this is an independent implementation
 * written against the public Minecraft and Fabric APIs. See CREDITS.md.
 *
 * <p>This class deliberately contains <b>no rendering code at all</b>. It just
 * maintains a list of hits. {@link BlockEspRenderer} draws them, and
 * {@code /autodonutesp list} prints them. If the rendering API shifts in a
 * future Minecraft version, the detection half keeps working and only the
 * renderer needs repair.
 *
 * <p>Scanning is spread over time and bounded: at most
 * {@link AutoDonutConfig#espMaxTargets} hits are kept, and a scan only happens
 * every {@link AutoDonutConfig#espScanIntervalTicks} ticks, so a large radius
 * costs throughput rather than frame time.
 */
public final class BlockEspEngine {

	private BlockEspEngine() {}

	/** A detected block: where it is, and what colour to draw it. */
	public record Hit(BlockPos pos, int rgb, String blockId) {}

	/** Rendering reads this; scanning replaces it wholesale. Never mutated in place. */
	private static volatile List<Hit> hits = List.of();

	private static int tickCounter = 0;
	private static long lastScanDurationNanos = 0L;
	private static BlockPos lastScanOrigin = null;

	/** Set by other AutoDonut engines when they need the world left alone. Harmless here - ESP is read-only. */
	public static volatile boolean externallyPaused = false;

	public static List<Hit> hits() {
		return hits;
	}

	public static void clear() {
		hits = List.of();
		lastScanOrigin = null;
	}

	public static void tick(Minecraft mc, AutoDonutConfig cfg) {
		if (!cfg.espEnabled) {
			if (!hits.isEmpty()) clear();
			return;
		}
		if (mc.level == null || mc.player == null) {
			if (!hits.isEmpty()) clear();
			return;
		}
		int interval = Math.max(1, cfg.espScanIntervalTicks);
		if (++tickCounter < interval) return;
		tickCounter = 0;
		scan(mc, mc.player, cfg);
	}

	private static void scan(Minecraft mc, LocalPlayer player, AutoDonutConfig cfg) {
		Map<Block, Integer> wanted = resolveTargets(cfg);
		if (wanted.isEmpty()) {
			if (!hits.isEmpty()) clear();
			return;
		}

		long started = System.nanoTime();

		int radius = Math.max(1, Math.min(cfg.espRadius, 128));
		int vertical = Math.max(1, Math.min(cfg.espVerticalRadius, 128));
		int max = Math.max(1, cfg.espMaxTargets);

		BlockPos origin = player.blockPosition();
		List<Hit> found = new ArrayList<>(Math.min(max, 256));
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

		int minY = mc.level.getMinY();
		int maxY = minY + mc.level.getHeight() - 1;
		int yLow = Math.max(minY, origin.getY() - vertical);
		int yHigh = Math.min(maxY, origin.getY() + vertical);

		// Iterate y innermost: scanning a column at a time keeps us inside one
		// chunk section for longer, which is much friendlier to the chunk cache.
		outer:
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dz = -radius; dz <= radius; dz++) {
				int x = origin.getX() + dx;
				int z = origin.getZ() + dz;
				if (!mc.level.isLoaded(cursor.set(x, origin.getY(), z))) continue;

				for (int y = yLow; y <= yHigh; y++) {
					cursor.set(x, y, z);
					BlockState state = mc.level.getBlockState(cursor);
					if (state.isAir()) continue;
					Integer rgb = wanted.get(state.getBlock());
					if (rgb == null) continue;

					found.add(new Hit(cursor.immutable(), rgb, idOf(state.getBlock())));
					if (found.size() >= max) break outer;
				}
			}
		}

		// Nearest first, so a truncated list is still the useful half of the list.
		found.sort((a, b) -> Double.compare(
				a.pos().distSqr(origin), b.pos().distSqr(origin)));

		hits = Collections.unmodifiableList(found);
		lastScanOrigin = origin;
		lastScanDurationNanos = System.nanoTime() - started;
	}

	/**
	 * Turns the configured {@code "minecraft:chest" -> "#RRGGBB"} map into real
	 * blocks. Unknown ids are skipped rather than fatal, so a config written for
	 * a modded setup still works on vanilla.
	 */
	public static Map<Block, Integer> resolveTargets(AutoDonutConfig cfg) {
		Map<Block, Integer> out = new LinkedHashMap<>();
		if (cfg.espTracked == null) return out;
		for (Map.Entry<String, String> entry : cfg.espTracked.entrySet()) {
			Block block = lookupBlock(entry.getKey());
			if (block == null) continue;
			out.put(block, parseColor(entry.getValue()));
		}
		return out;
	}

	/** Resolves "chest" or "minecraft:chest" to a block, or null if there's no such block. */
	public static Block lookupBlock(String rawId) {
		if (rawId == null || rawId.isBlank()) return null;
		String id = rawId.trim().toLowerCase(Locale.ROOT);
		if (!id.contains(":")) id = "minecraft:" + id;
		int colon = id.indexOf(':');
		Identifier identifier = Identifier.fromNamespaceAndPath(
				id.substring(0, colon), id.substring(colon + 1));
		Block block = BuiltInRegistries.BLOCK.get(identifier);
		// A miss returns AIR from the registry, which is never a sensible target.
		if (block == null || block == net.minecraft.world.level.block.Blocks.AIR) return null;
		return block;
	}

	public static String idOf(Block block) {
		return BuiltInRegistries.BLOCK.getKey(block).toString();
	}

	/** "#RRGGBB" / "RRGGBB" / "0xRRGGBB" -> packed rgb. Falls back to white. */
	public static int parseColor(String raw) {
		if (raw == null) return 0xFFFFFF;
		String s = raw.trim();
		if (s.startsWith("#")) s = s.substring(1);
		else if (s.startsWith("0x") || s.startsWith("0X")) s = s.substring(2);
		try {
			return (int) (Long.parseLong(s, 16) & 0xFFFFFF);
		} catch (NumberFormatException e) {
			return 0xFFFFFF;
		}
	}

	public static String colorToHex(int rgb) {
		return String.format("#%06X", rgb & 0xFFFFFF);
	}

	// ---------------------------------------------------------------
	// Chat reporting - works whether or not the renderer is functioning
	// ---------------------------------------------------------------

	/** Prints the nearest hits with coordinates. Useful on its own, and the fallback if rendering breaks. */
	public static void report(LocalPlayer player, int limit) {
		List<Hit> snapshot = hits;
		if (snapshot.isEmpty()) {
			player.sendSystemMessage(Component.literal(
					"[AutoDonut ESP] Nothing found. Check /autodonutesp status for what's being tracked."));
			return;
		}
		BlockPos from = lastScanOrigin != null ? lastScanOrigin : player.blockPosition();
		int shown = Math.min(limit, snapshot.size());
		player.sendSystemMessage(Component.literal(String.format(
				"[AutoDonut ESP] %d found, nearest %d:", snapshot.size(), shown)));
		for (int i = 0; i < shown; i++) {
			Hit hit = snapshot.get(i);
			BlockPos p = hit.pos();
			double dist = Math.sqrt(p.distSqr(from));
			player.sendSystemMessage(Component.literal(String.format(
					"  %-28s %6d %4d %6d   %5.1fm %s",
					shortId(hit.blockId()), p.getX(), p.getY(), p.getZ(), dist, compass(from, p))));
		}
	}

	private static String shortId(String id) {
		int colon = id.indexOf(':');
		return colon >= 0 ? id.substring(colon + 1) : id;
	}

	/** Rough cardinal direction from one position to another, for the chat listing. */
	private static String compass(BlockPos from, BlockPos to) {
		int dx = to.getX() - from.getX();
		int dz = to.getZ() - from.getZ();
		if (dx == 0 && dz == 0) return "here";
		StringBuilder sb = new StringBuilder();
		if (Math.abs(dz) > Math.abs(dx) / 2) sb.append(dz < 0 ? "N" : "S");
		if (Math.abs(dx) > Math.abs(dz) / 2) sb.append(dx > 0 ? "E" : "W");
		return sb.isEmpty() ? "here" : sb.toString();
	}

	public static String statusLine(AutoDonutConfig cfg) {
		return String.format(
				"[AutoDonut ESP] %s | radius %d (vert %d) | %d tracked block types | %d visible | scan %.1fms every %dt",
				cfg.espEnabled ? "ON" : "off",
				cfg.espRadius, cfg.espVerticalRadius,
				cfg.espTracked == null ? 0 : cfg.espTracked.size(),
				hits.size(),
				lastScanDurationNanos / 1_000_000.0,
				Math.max(1, cfg.espScanIntervalTicks));
	}
}
