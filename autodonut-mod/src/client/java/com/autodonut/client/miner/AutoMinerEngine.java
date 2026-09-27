package com.autodonut.client.miner;

import com.autodonut.config.AutoDonutConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Everything here only ever does things a normal player could do by hand:
 * hold left-click on a block (real block-breaking timing, via
 * MultiPlayerGameMode.startDestroyBlock/continueDestroyBlock - the exact same
 * calls the vanilla client makes when you hold your mouse button down), walk
 * forward (normal WASD-style input), press a hotbar number to select a torch
 * or your tool, and right-click a block to place a torch or open a chest.
 * There is no packet forging, no reach/speed hacking, and nothing here
 * touches the server - it only needs the server to be a normal vanilla (or
 * modded) world, exactly like any other player connecting.
 */
public final class AutoMinerEngine {

	/** Set/cleared by SafetyEngine's threat-pause. Doesn't touch the persisted config toggle. */
	public static volatile boolean externallyPaused = false;

	private static Direction lockedDirection = Direction.NORTH;
	private static BlockPos breakingPos = null;
	private static OreType breakingOreType = null;
	private static int scanCooldown = 0;
	private static BlockPos steerTargetPos = null;
	private static int warningCooldown = 0;

	// --- Vein clearing ---
	private static final Deque<BlockPos> veinQueue = new ArrayDeque<>();

	// --- Session stats ---
	private static int blocksMined = 0;
	private static final Map<String, Integer> oresMined = new LinkedHashMap<>();

	// --- Auto-torch ---
	private static BlockPos lastTorchPos = null;

	// --- Auto-return-to-dump ---
	private enum ReturnState { NONE, WALKING_BACK, OPENING_CHEST, DEPOSITING, WALKING_FORWARD }
	private static ReturnState returnState = ReturnState.NONE;
	private static final Deque<Direction> walkPath = new ArrayDeque<>();
	private static Deque<Direction> replaySteps = null;
	private static BlockPos lastFeetPos = null;
	private static BlockPos returnStepAnchor = null;
	private static BlockPos homePos = null;
	private static int returnCooldown = 0;

	private AutoMinerEngine() {}

	public static void tick() {
		Minecraft mc = Minecraft.getInstance();
		AutoDonutConfig cfg = AutoDonutConfig.get();

		if (!cfg.clientMinerEnabled || externallyPaused || mc.player == null || mc.level == null || mc.gui.screen() != null) {
			// Still let an in-progress return-and-deposit finish even if a GUI (the chest) is open.
			if (!(cfg.clientMinerEnabled && returnState != ReturnState.NONE && mc.player != null && mc.level != null)) {
				stopBreaking(mc);
				setWalkInput(mc.player, false);
				return;
			}
		}

		LocalPlayer player = mc.player;
		if (warningCooldown > 0) warningCooldown--;

		if (cfg.minerAutoReturnEnabled) {
			trackWalkPath(player);
			if (homePos == null) homePos = player.blockPosition().immutable();

			if (returnState != ReturnState.NONE) {
				tickReturn(mc, player, cfg);
				return;
			}
			if (returnCooldown <= 0 && isInventoryFull(player)) {
				beginReturn(player);
				return;
			}
			if (returnCooldown > 0) returnCooldown--;
		}

		if (mc.gui.screen() != null) return; // (return path above handles the chest GUI case; normal mining never runs with a screen open)

		if (cfg.minerToolDurabilityGuardEnabled && toolAboutToBreak(player)) {
			stopBreaking(mc);
			setWalkInput(player, false);
			warn(player, "your pickaxe is almost broken - swap it manually to keep mining.");
			return;
		}

		if (scanCooldown-- <= 0) {
			scanCooldown = 10; // re-scan for ore roughly twice a second
			steerTargetPos = cfg.minerOreSeekingEnabled ? findNearestOre(mc, player, cfg) : null;
		}

		if (cfg.minerWalkEnabled) {
			tickWalkAndMine(mc, player, cfg);
		} else {
			setWalkInput(player, false);
			tickMineOnly(mc, player, cfg);
		}
	}

	// ---------------------------------------------------------------
	// Normal mining
	// ---------------------------------------------------------------

	private static BlockPos currentPriorityTarget(Minecraft mc, LocalPlayer player, AutoDonutConfig cfg, double reach) {
		if (cfg.minerVeinClearEnabled) {
			while (!veinQueue.isEmpty()) {
				BlockPos candidate = veinQueue.peekFirst();
				if (!mc.level.isLoaded(candidate) || mc.level.getBlockState(candidate).isAir()) {
					veinQueue.pollFirst();
					continue;
				}
				if (player.position().distanceTo(Vec3.atCenterOf(candidate)) <= reach + 1.0) {
					return candidate;
				}
				break; // nearest queued vein block isn't in range yet - let normal steering/tunneling continue
			}
		}
		return steerTargetPos;
	}

	private static void tickMineOnly(Minecraft mc, LocalPlayer player, AutoDonutConfig cfg) {
		double reach = player.blockInteractionRange();
		BlockPos priority = currentPriorityTarget(mc, player, cfg, reach);
		BlockPos target = null;

		if (priority != null && player.position().distanceTo(Vec3.atCenterOf(priority)) <= reach + 1.0) {
			target = priority;
		} else if (mc.hitResult instanceof BlockHitResult bhr && bhr.getType() == HitResult.Type.BLOCK) {
			BlockPos lookPos = bhr.getBlockPos();
			if (player.position().distanceTo(Vec3.atCenterOf(lookPos)) <= reach) {
				BlockState state = mc.level.getBlockState(lookPos);
				if (!state.isAir() && isBreakable(mc, lookPos, state)) target = lookPos;
			}
		}
		mineTowards(mc, player, target);
	}

	private static void tickWalkAndMine(Minecraft mc, LocalPlayer player, AutoDonutConfig cfg) {
		double reach = player.blockInteractionRange();
		BlockPos priority = currentPriorityTarget(mc, player, cfg, reach);

		// An ore (or queued vein block) right next to us takes priority over tunneling - grab it first.
		if (priority != null && player.position().distanceTo(Vec3.atCenterOf(priority)) <= reach + 0.5) {
			setWalkInput(player, false);
			mineTowards(mc, player, priority);
			return;
		}

		Direction dir = lockedDirection;
		if (steerTargetPos != null) {
			BlockPos p = player.blockPosition();
			int dx = steerTargetPos.getX() - p.getX();
			int dz = steerTargetPos.getZ() - p.getZ();
			if (Math.abs(dx) + Math.abs(dz) > 0) {
				dir = Math.abs(dx) >= Math.abs(dz) ? (dx > 0 ? Direction.EAST : Direction.WEST)
						: (dz > 0 ? Direction.SOUTH : Direction.NORTH);
			}
		}
		lockedDirection = dir;

		BlockPos feet = player.blockPosition();
		BlockPos aheadFeet = feet.relative(dir);
		BlockPos aheadHead = aheadFeet.above();

		if (isFluid(mc, aheadFeet) || isFluid(mc, aheadHead)) {
			setWalkInput(player, false);
			stopBreaking(mc);
			warn(player, "Auto Miner: water/lava ahead, stopping so it doesn't flood the tunnel. Steer around it manually.");
			return;
		}

		BlockState feetState = mc.level.getBlockState(aheadFeet);
		BlockState headState = mc.level.getBlockState(aheadHead);

		if (!isPassable(mc, aheadFeet, feetState)) {
			setWalkInput(player, false);
			if (!isBreakable(mc, aheadFeet, feetState)) {
				warn(player, "Auto Miner: hit an unbreakable block, stopping.");
				return;
			}
			mineTowards(mc, player, aheadFeet);
			return;
		}
		if (!isPassable(mc, aheadHead, headState)) {
			setWalkInput(player, false);
			if (!isBreakable(mc, aheadHead, headState)) {
				warn(player, "Auto Miner: hit an unbreakable block, stopping.");
				return;
			}
			mineTowards(mc, player, aheadHead);
			return;
		}

		BlockPos sideOre = findAdjacentOre(mc, aheadFeet, aheadHead, dir, cfg);
		if (sideOre != null) {
			setWalkInput(player, false);
			mineTowards(mc, player, sideOre);
			return;
		}

		BlockPos below = aheadFeet.below();
		if (mc.level.getBlockState(below).isAir()) {
			setWalkInput(player, false);
			stopBreaking(mc);
			warn(player, "Auto Miner: drop ahead, stopping so you don't fall. Move manually to continue.");
			return;
		}

		stopBreaking(mc);
		maybePlaceTorch(mc, player, cfg, dir, feet);
		player.setYRot(dir.toYRot());
		setWalkInput(player, true);
	}

	private static BlockPos findAdjacentOre(Minecraft mc, BlockPos aheadFeet, BlockPos aheadHead, Direction dir, AutoDonutConfig cfg) {
		Direction left = dir.getCounterClockWise();
		Direction right = dir.getClockWise();
		BlockPos[] candidates = new BlockPos[] {
				aheadFeet.below(), aheadHead.above(),
				aheadFeet.relative(left), aheadFeet.relative(right),
				aheadHead.relative(left), aheadHead.relative(right)
		};
		for (BlockPos pos : candidates) {
			if (!mc.level.isLoaded(pos)) continue;
			BlockState state = mc.level.getBlockState(pos);
			OreType ore = OreType.fromBlock(state.getBlock());
			if (ore != null && ore.isEnabled(cfg)) return pos;
		}
		return null;
	}

	private static BlockPos findNearestOre(Minecraft mc, LocalPlayer player, AutoDonutConfig cfg) {
		int radius = Math.max(1, Math.min(cfg.minerOreRadarRadius, 32));
		int vRadius = Math.min(radius, 6);
		BlockPos center = player.blockPosition();
		BlockPos best = null;
		double bestDist = Double.MAX_VALUE;

		for (int dx = -radius; dx <= radius; dx++) {
			for (int dz = -radius; dz <= radius; dz++) {
				for (int dy = -vRadius; dy <= vRadius; dy++) {
					BlockPos pos = center.offset(dx, dy, dz);
					if (!mc.level.isLoaded(pos)) continue;
					BlockState state = mc.level.getBlockState(pos);
					if (state.isAir()) continue;
					OreType ore = OreType.fromBlock(state.getBlock());
					if (ore == null || !ore.isEnabled(cfg)) continue;
					double distSq = pos.distSqr(center);
					if (distSq < bestDist) {
						bestDist = distSq;
						best = pos.immutable();
					}
				}
			}
		}
		return best;
	}

	private static void mineTowards(Minecraft mc, LocalPlayer player, BlockPos pos) {
		if (pos == null) {
			stopBreaking(mc);
			return;
		}
		BlockState state = mc.level.getBlockState(pos);
		if (state.isAir()) {
			stopBreaking(mc);
			return;
		}
		Direction face = faceTowards(player, pos);
		if (breakingPos == null || !breakingPos.equals(pos)) {
			mc.gameMode.startDestroyBlock(pos, face);
			breakingPos = pos;
			breakingOreType = OreType.fromBlock(state.getBlock());
		} else {
			boolean stillDestroying = mc.gameMode.continueDestroyBlock(pos, face);
			if (!stillDestroying) {
				onBlockPossiblyMined(mc, pos, breakingOreType);
				breakingPos = null;
				breakingOreType = null;
			}
		}
	}

	private static void onBlockPossiblyMined(Minecraft mc, BlockPos pos, OreType ore) {
		if (!mc.level.getBlockState(pos).isAir()) return; // interrupted, not actually broken
		blocksMined++;
		if (ore != null) {
			oresMined.merge(ore.displayName, 1, Integer::sum);
			AutoDonutConfig cfg = AutoDonutConfig.get();
			if (cfg.minerVeinClearEnabled) {
				for (Direction d : Direction.values()) {
					BlockPos n = pos.relative(d);
					if (!mc.level.isLoaded(n)) continue;
					if (OreType.fromBlock(mc.level.getBlockState(n).getBlock()) == ore) {
						veinQueue.addLast(n.immutable());
					}
				}
			}
		}
	}

	// ---------------------------------------------------------------
	// Auto-torch (only ever runs if minerAutoTorchEnabled is on - and that
	// checkbox is only offered in the GUI when Walk+Mine is selected AND you
	// are actually carrying torches)
	// ---------------------------------------------------------------

	private static void maybePlaceTorch(Minecraft mc, LocalPlayer player, AutoDonutConfig cfg, Direction dir, BlockPos feet) {
		if (!cfg.minerAutoTorchEnabled || !cfg.minerWalkEnabled) return;
		int spacing = Math.max(2, cfg.minerTorchSpacing);
		if (lastTorchPos != null && lastTorchPos.distManhattan(feet) < spacing) return;

		int torchSlot = findItemInHotbar(player, AutoMinerEngine::isTorch);
		if (torchSlot < 0) return; // ran out - silently skip, don't nag every tick

		BlockPos wallPos = feet.relative(dir.getOpposite());
		BlockState wallState = mc.level.getBlockState(wallPos);
		if (wallState.isAir() || wallState.getCollisionShape(mc.level, wallPos).isEmpty()) return; // nothing solid to stick the torch to

		int previousSlot = player.getInventory().getSelectedSlot();
		selectHotbarSlot(player, torchSlot);

		Vec3 hitLoc = Vec3.atCenterOf(wallPos).add(dir.getStepX() * 0.5, dir.getStepY() * 0.5, dir.getStepZ() * 0.5);
		BlockHitResult hit = new BlockHitResult(hitLoc, dir, wallPos, false);
		mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);

		if (previousSlot != torchSlot) selectHotbarSlot(player, previousSlot);
		lastTorchPos = feet.immutable();
	}

	private static boolean isTorch(ItemStack stack) {
		if (stack.isEmpty()) return false;
		String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
		return path.equals("torch") || path.equals("soul_torch") || path.equals("redstone_torch");
	}

	/** Does the player currently have at least one torch anywhere in their main inventory/hotbar? Used by the settings GUI. */
	public static boolean playerHasTorches(LocalPlayer player) {
		if (player == null) return false;
		for (int i = 0; i < 36; i++) {
			if (isTorch(player.getInventory().getItem(i))) return true;
		}
		return false;
	}

	private static int findItemInHotbar(LocalPlayer player, java.util.function.Predicate<ItemStack> predicate) {
		for (int i = 0; i < 9; i++) {
			if (predicate.test(player.getInventory().getItem(i))) return i;
		}
		return -1;
	}

	private static void selectHotbarSlot(LocalPlayer player, int slot) {
		player.getInventory().setSelectedSlot(slot);
		player.connection.send(new ServerboundSetCarriedItemPacket(slot));
	}

	// ---------------------------------------------------------------
	// Tool durability guard
	// ---------------------------------------------------------------

	private static boolean toolAboutToBreak(LocalPlayer player) {
		ItemStack held = player.getMainHandItem();
		if (held.isEmpty() || !held.isDamageableItem()) return false;
		int remaining = held.getMaxDamage() - held.getDamageValue();
		AutoDonutConfig cfg = AutoDonutConfig.get();
		return remaining <= Math.max(1, cfg.minerToolDurabilityGuardThreshold);
	}

	// ---------------------------------------------------------------
	// Auto-return-to-dump (opt-in, off by default). Not generic navigation -
	// it only ever retraces the exact tunnel it just dug, then walks the
	// exact same steps forward again to resume where it left off.
	// ---------------------------------------------------------------

	private static void trackWalkPath(LocalPlayer player) {
		BlockPos feet = player.blockPosition();
		if (lastFeetPos == null) {
			lastFeetPos = feet.immutable();
			return;
		}
		if (feet.equals(lastFeetPos)) return;
		int dx = feet.getX() - lastFeetPos.getX();
		int dz = feet.getZ() - lastFeetPos.getZ();
		if (Math.abs(dx) + Math.abs(dz) == 1) {
			Direction moveDir = dx != 0 ? (dx > 0 ? Direction.EAST : Direction.WEST) : (dz > 0 ? Direction.SOUTH : Direction.NORTH);
			walkPath.addLast(moveDir);
			if (walkPath.size() > 4000) walkPath.removeFirst();
		}
		lastFeetPos = feet.immutable();
	}

	private static boolean isInventoryFull(LocalPlayer player) {
		for (int i = 0; i < 36; i++) {
			if (player.getInventory().getItem(i).isEmpty()) return false;
		}
		return true;
	}

	private static void beginReturn(LocalPlayer player) {
		if (walkPath.isEmpty()) return; // haven't moved yet, nothing to retrace
		replaySteps = new ArrayDeque<>(walkPath);
		returnStepAnchor = null;
		returnState = ReturnState.WALKING_BACK;
		stopBreaking(Minecraft.getInstance());
		player.sendSystemMessage(Component.literal("[AutoDonut Miner] Inventory full - heading back to deposit, then resuming automatically."));
	}

	private static void tickReturn(Minecraft mc, LocalPlayer player, AutoDonutConfig cfg) {
		switch (returnState) {
			case WALKING_BACK -> {
				if (replaySteps.isEmpty()) {
					returnState = ReturnState.OPENING_CHEST;
					setWalkInput(player, false);
					returnCooldown = 4;
					return;
				}
				Direction backDir = replaySteps.peekLast().getOpposite();
				player.setYRot(backDir.toYRot());
				setWalkInput(player, true);
				BlockPos feet = player.blockPosition();
				if (returnStepAnchor == null) returnStepAnchor = feet.immutable();
				else if (!feet.equals(returnStepAnchor)) {
					replaySteps.removeLast();
					returnStepAnchor = feet.immutable();
				}
			}
			case OPENING_CHEST -> {
				if (returnCooldown-- > 0) return;
				BlockPos chestPos = findAdjacentContainer(mc, homePos);
				if (chestPos == null) {
					player.sendSystemMessage(Component.literal(
							"[AutoDonut Miner] No chest found at your starting point - resuming with a full inventory. " +
									"Place a chest right where you start mining next time for auto-deposit."));
					beginForwardReplay(player);
					return;
				}
				BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(chestPos), Direction.UP, chestPos, false);
				mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);
				returnState = ReturnState.DEPOSITING;
				returnCooldown = 10;
			}
			case DEPOSITING -> {
				if (returnCooldown-- > 0) return;
				if (!(mc.gui.screen() instanceof AbstractContainerScreen<?> cs)) {
					beginForwardReplay(player);
					return;
				}
				AbstractContainerMenu menu = cs.getMenu();
				int playerStart = Math.max(0, menu.slots.size() - 36);
				boolean movedAny = false;
				for (int i = playerStart; i < menu.slots.size(); i++) {
					ItemStack stack = menu.getSlot(i).getItem();
					if (stack.isEmpty() || isPickaxeLike(stack) || isTorch(stack)) continue;
					mc.gameMode.handleContainerInput(menu.containerId, i, 0, ContainerInput.QUICK_MOVE, player);
					movedAny = true;
					break; // one item per tick, gives the server time to keep up
				}
				if (!movedAny) {
					player.closeContainer();
					player.sendSystemMessage(Component.literal("[AutoDonut Miner] Deposited - resuming mining."));
					beginForwardReplay(player);
				}
			}
			case WALKING_FORWARD -> {
				if (replaySteps.isEmpty()) {
					returnState = ReturnState.NONE;
					setWalkInput(player, false);
					returnCooldown = 20;
					return;
				}
				Direction stepDir = replaySteps.peekFirst();
				player.setYRot(stepDir.toYRot());
				setWalkInput(player, true);
				BlockPos feet = player.blockPosition();
				if (returnStepAnchor == null) returnStepAnchor = feet.immutable();
				else if (!feet.equals(returnStepAnchor)) {
					replaySteps.removeFirst();
					returnStepAnchor = feet.immutable();
				}
			}
			case NONE -> { /* not reached */ }
		}
	}

	private static void beginForwardReplay(LocalPlayer player) {
		replaySteps = new ArrayDeque<>(walkPath);
		returnStepAnchor = null;
		returnState = ReturnState.WALKING_FORWARD;
	}

	private static BlockPos findAdjacentContainer(Minecraft mc, BlockPos center) {
		if (center == null) return null;
		BlockPos[] candidates = new BlockPos[] {
				center, center.above(), center.below(),
				center.north(), center.south(), center.east(), center.west()
		};
		for (BlockPos pos : candidates) {
			if (!mc.level.isLoaded(pos)) continue;
			if (mc.level.getBlockEntity(pos) instanceof net.minecraft.world.Container) return pos.immutable();
		}
		return null;
	}

	private static boolean isPickaxeLike(ItemStack stack) {
		if (stack.isEmpty()) return false;
		return BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().contains("pickaxe");
	}

	// ---------------------------------------------------------------
	// Shared helpers
	// ---------------------------------------------------------------

	private static void stopBreaking(Minecraft mc) {
		if (breakingPos != null) {
			if (mc.gameMode != null) mc.gameMode.stopDestroyBlock();
			breakingPos = null;
			breakingOreType = null;
		}
	}

	private static void setWalkInput(LocalPlayer player, boolean forward) {
		if (player == null) return;
		if (!forward && !player.input.keyPresses.forward()) return;
		player.input.keyPresses = new Input(forward, false, false, false, false, false, false);
	}

	private static Direction faceTowards(LocalPlayer player, BlockPos pos) {
		Vec3 eye = player.getEyePosition();
		Vec3 center = Vec3.atCenterOf(pos);
		Vec3 diff = eye.subtract(center);
		return Direction.getApproximateNearest(diff.x, diff.y, diff.z);
	}

	private static boolean isFluid(Minecraft mc, BlockPos pos) {
		FluidState fluid = mc.level.getFluidState(pos);
		return !fluid.isEmpty();
	}

	private static boolean isPassable(Minecraft mc, BlockPos pos, BlockState state) {
		if (state.isAir()) return true;
		if (!isFluid(mc, pos) && state.getCollisionShape(mc.level, pos).isEmpty()) return true;
		return false;
	}

	private static boolean isBreakable(Minecraft mc, BlockPos pos, BlockState state) {
		if (state.isAir()) return true;
		return state.getDestroySpeed(mc.level, pos) >= 0f;
	}

	private static void warn(LocalPlayer player, String message) {
		if (warningCooldown > 0) return;
		warningCooldown = 100; // ~5 seconds between repeats
		player.sendSystemMessage(Component.literal("[AutoDonut] Auto Miner: " + message));
	}

	// ---------------------------------------------------------------
	// Session stats
	// ---------------------------------------------------------------

	public static void announceStats() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		StringBuilder sb = new StringBuilder("[AutoDonut Miner] Session: " + blocksMined + " block(s) mined");
		if (!oresMined.isEmpty()) {
			sb.append(" (");
			boolean first = true;
			for (Map.Entry<String, Integer> e : oresMined.entrySet()) {
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
		blocksMined = 0;
		oresMined.clear();
	}
}
