package com.autodonut.client.automation;

import com.autodonut.client.miner.AutoMinerEngine;
import com.autodonut.config.AutoDonutConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 100% client-side Auto Farm.
 *
 * <p>This does NOT add a block and needs nothing installed on the server. It
 * harvests ordinary vanilla crops growing around you and replants them, using
 * exactly the actions you'd perform by hand: start/continue breaking a block
 * you can actually reach, then right-click a seed onto the farmland.
 *
 * <p>There is no reach extension - a crop has to be inside your real
 * {@code blockInteractionRange()} before it will be touched. Stand in the
 * middle of your field (or walk around it) and it keeps the field harvested.
 */
public final class ClientFarmEngine {

	/** Crop block -> the seed item used to replant it. */
	private static final Map<Block, Item> REPLANT = new LinkedHashMap<>();

	static {
		REPLANT.put(Blocks.WHEAT, Items.WHEAT_SEEDS);
		REPLANT.put(Blocks.CARROTS, Items.CARROT);
		REPLANT.put(Blocks.POTATOES, Items.POTATO);
		REPLANT.put(Blocks.BEETROOTS, Items.BEETROOT_SEEDS);
		REPLANT.put(Blocks.NETHER_WART, Items.NETHER_WART);
	}

	public static volatile boolean externallyPaused = false;

	private static BlockPos breakingPos = null;
	private static Block breakingCrop = null;
	private static BlockPos replantPos = null;
	private static Block replantCrop = null;
	private static int actionDelay = 0;
	private static int warningCooldown = 0;

	// --- Session stats ---
	private static int harvested = 0;
	private static int replanted = 0;
	private static final Map<String, Integer> harvestedByType = new LinkedHashMap<>();

	private ClientFarmEngine() {}

	public static void tick() {
		Minecraft mc = Minecraft.getInstance();
		AutoDonutConfig cfg = AutoDonutConfig.get();
		LocalPlayer player = mc.player;

		if (!cfg.clientFarmEnabled || externallyPaused || player == null || mc.level == null || mc.gameMode == null) {
			stopBreaking(mc);
			return;
		}
		// Don't fight an open GUI, and don't fight the Auto Miner for control of block-breaking.
		if (mc.gui.screen() != null || (cfg.clientMinerEnabled && !AutoMinerEngine.externallyPaused)) {
			stopBreaking(mc);
			return;
		}
		if (warningCooldown > 0) warningCooldown--;
		if (actionDelay-- > 0) return;

		// A crop we just broke is waiting to be replanted.
		if (replantPos != null) {
			tryReplant(mc, player, cfg);
			return;
		}

		// Continue an in-progress break.
		if (breakingPos != null) {
			BlockState state = mc.level.getBlockState(breakingPos);
			if (!isHarvestable(state, cfg)) {
				// It finished breaking (or something else changed it) - queue the replant.
				if (state.isAir() && breakingCrop != null) {
					harvested++;
					harvestedByType.merge(displayName(breakingCrop), 1, Integer::sum);
					if (cfg.farmReplant && REPLANT.containsKey(breakingCrop)) {
						replantPos = breakingPos;
						replantCrop = breakingCrop;
					}
				}
				breakingPos = null;
				breakingCrop = null;
				actionDelay = Math.max(1, cfg.farmActionIntervalTicks);
				return;
			}
			Direction face = faceTowards(player, breakingPos);
			if (!mc.gameMode.continueDestroyBlock(breakingPos, face)) {
				// continueDestroyBlock returning false means the break completed.
				if (breakingCrop != null) {
					harvested++;
					harvestedByType.merge(displayName(breakingCrop), 1, Integer::sum);
					if (cfg.farmReplant && REPLANT.containsKey(breakingCrop)) {
						replantPos = breakingPos;
						replantCrop = breakingCrop;
					}
				}
				breakingPos = null;
				breakingCrop = null;
				actionDelay = Math.max(1, cfg.farmActionIntervalTicks);
			}
			return;
		}

		// Look for the next ripe crop in range.
		BlockPos target = findRipeCrop(mc, player, cfg);
		if (target == null) {
			actionDelay = 10; // nothing ready - idle cheaply
			return;
		}
		BlockState state = mc.level.getBlockState(target);
		breakingPos = target;
		breakingCrop = state.getBlock();
		mc.gameMode.startDestroyBlock(target, faceTowards(player, target));
	}

	// ---------------------------------------------------------------
	// Replanting
	// ---------------------------------------------------------------

	private static void tryReplant(Minecraft mc, LocalPlayer player, AutoDonutConfig cfg) {
		BlockPos pos = replantPos;
		Block crop = replantCrop;
		replantPos = null;
		replantCrop = null;
		actionDelay = Math.max(1, cfg.farmActionIntervalTicks);

		if (pos == null || crop == null) return;
		if (!mc.level.getBlockState(pos).isAir()) return; // something already there

		Item seed = REPLANT.get(crop);
		if (seed == null) return;

		int seedSlot = findInHotbar(player, seed);
		if (seedSlot < 0) {
			warn(player, "out of " + new ItemStack(seed).getHoverName().getString() + " - harvesting without replanting.");
			return;
		}

		// Right-click the seed onto the top face of the soil below the crop.
		BlockPos soil = pos.below();
		if (!mc.level.isLoaded(soil)) return;

		int previousSlot = player.getInventory().getSelectedSlot();
		selectHotbarSlot(player, seedSlot);

		Vec3 hitLoc = Vec3.atCenterOf(soil).add(0.0, 0.5, 0.0);
		BlockHitResult hit = new BlockHitResult(hitLoc, Direction.UP, soil, false);
		mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);
		replanted++;

		if (previousSlot != seedSlot) selectHotbarSlot(player, previousSlot);
	}

	// ---------------------------------------------------------------
	// Scanning
	// ---------------------------------------------------------------

	/** Is this block a fully-grown crop AutoDonut is willing to harvest? */
	private static boolean isHarvestable(BlockState state, AutoDonutConfig cfg) {
		Block block = state.getBlock();
		if (block instanceof CropBlock crop) {
			return crop.isMaxAge(state);
		}
		if (cfg.farmIncludeNetherWart && block instanceof NetherWartBlock) {
			// Read the age off the generic vanilla property rather than
			// NetherWartBlock.AGE, and guard with hasProperty first, so a
			// version bump that moves the constant can't crash the engine.
			if (!state.hasProperty(BlockStateProperties.AGE_3)) return false;
			return state.getValue(BlockStateProperties.AGE_3) >= 3;
		}
		return false;
	}

	private static BlockPos findRipeCrop(Minecraft mc, LocalPlayer player, AutoDonutConfig cfg) {
		double reach = player.blockInteractionRange();
		int radius = Math.max(1, Math.min(cfg.farmRadius, (int) Math.ceil(reach)));
		BlockPos origin = player.blockPosition();
		Vec3 eye = player.getEyePosition();

		BlockPos best = null;
		double bestDist = Double.MAX_VALUE;

		// Crops sit at, just below, or just above foot level depending on terrain.
		for (int dy = -2; dy <= 2; dy++) {
			for (int dx = -radius; dx <= radius; dx++) {
				for (int dz = -radius; dz <= radius; dz++) {
					BlockPos pos = origin.offset(dx, dy, dz);
					if (!mc.level.isLoaded(pos)) continue;
					if (!isHarvestable(mc.level.getBlockState(pos), cfg)) continue;
					double dist = eye.distanceTo(Vec3.atCenterOf(pos));
					if (dist > reach) continue; // must be genuinely reachable
					if (dist < bestDist) {
						bestDist = dist;
						best = pos.immutable();
					}
				}
			}
		}
		return best;
	}

	// ---------------------------------------------------------------
	// Helpers
	// ---------------------------------------------------------------

	private static void stopBreaking(Minecraft mc) {
		if (breakingPos != null) {
			if (mc.gameMode != null) mc.gameMode.stopDestroyBlock();
			breakingPos = null;
			breakingCrop = null;
		}
	}

	private static int findInHotbar(LocalPlayer player, Item item) {
		for (int i = 0; i < 9; i++) {
			ItemStack stack = player.getInventory().getItem(i);
			if (!stack.isEmpty() && stack.getItem() == item) return i;
		}
		return -1;
	}

	private static void selectHotbarSlot(LocalPlayer player, int slot) {
		player.getInventory().setSelectedSlot(slot);
		player.connection.send(new ServerboundSetCarriedItemPacket(slot));
	}

	private static Direction faceTowards(LocalPlayer player, BlockPos pos) {
		Vec3 eye = player.getEyePosition();
		Vec3 center = Vec3.atCenterOf(pos);
		Vec3 diff = eye.subtract(center);
		return Direction.getApproximateNearest(diff.x, diff.y, diff.z);
	}

	private static String displayName(Block block) {
		Item seed = REPLANT.get(block);
		if (seed != null) return new ItemStack(seed).getHoverName().getString();
		return net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).getPath();
	}

	private static void warn(LocalPlayer player, String message) {
		if (warningCooldown > 0) return;
		warningCooldown = 100; // ~5 seconds between repeats
		player.sendSystemMessage(Component.literal("[AutoDonut] Auto Farm: " + message));
	}

	/** Does the player have at least one seed for any supported crop? Used by the settings GUI. */
	public static boolean playerHasAnySeed(LocalPlayer player) {
		if (player == null) return false;
		for (int i = 0; i < 36; i++) {
			ItemStack stack = player.getInventory().getItem(i);
			if (stack.isEmpty()) continue;
			if (REPLANT.containsValue(stack.getItem())) return true;
		}
		return false;
	}

	// ---------------------------------------------------------------
	// Stats
	// ---------------------------------------------------------------

	public static void announceStats() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		StringBuilder sb = new StringBuilder("[AutoDonut Farm] Session: " + harvested + " crop(s) harvested, "
				+ replanted + " replanted");
		if (!harvestedByType.isEmpty()) {
			sb.append(" (");
			boolean first = true;
			for (Map.Entry<String, Integer> e : harvestedByType.entrySet()) {
				if (!first) sb.append(", ");
				sb.append(e.getValue()).append("x ").append(e.getKey());
				first = false;
			}
			sb.append(")");
		}
		sb.append(".");
		mc.player.sendSystemMessage(Component.literal(sb.toString()));
	}

	public static void resetStats() {
		harvested = 0;
		replanted = 0;
		harvestedByType.clear();
	}
}
