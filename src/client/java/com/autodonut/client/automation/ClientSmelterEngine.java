package com.autodonut.client.automation;

import com.autodonut.config.AutoDonutConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 100% client-side Auto Smelter.
 *
 * <p>This does NOT add a block and needs nothing installed on the server. It
 * tends an ordinary vanilla Furnace / Blast Furnace / Smoker that already
 * exists in your world: every few seconds it right-clicks the furnace open,
 * pulls the finished output into your inventory, tops up the fuel and input
 * slots from your inventory, and closes it again.
 *
 * <p>Every single action is one you could do by hand - opening a container,
 * clicking a slot, shift-clicking a stack, closing the container. There is no
 * packet forging and no reach extension: the furnace has to be within your
 * real {@code blockInteractionRange()}.
 *
 * <p>What it is allowed to put IN is an explicit allow-list
 * ({@link AutoDonutConfig#smelterInputItems}) so it can never shove something
 * valuable into a furnace by accident.
 */
public final class ClientSmelterEngine {

	/** Furnace-family menus are 3 container slots + the usual 36 player slots. */
	private static final int FURNACE_CONTAINER_SLOTS = 3;
	private static final int SLOT_INPUT = 0;
	private static final int SLOT_FUEL = 1;
	private static final int SLOT_OUTPUT = 2;

	private enum State { IDLE, OPENING, SERVICING }

	public static volatile boolean externallyPaused = false;

	private static State state = State.IDLE;
	private static BlockPos targetFurnace = null;
	private static int cooldownTicks = 0;
	private static int stepDelay = 0;
	private static int warningCooldown = 0;

	// --- Session stats ---
	private static int serviceRuns = 0;
	private static int outputsCollected = 0;
	private static int fuelInserted = 0;
	private static int inputsInserted = 0;

	private ClientSmelterEngine() {}

	public static void tick() {
		Minecraft mc = Minecraft.getInstance();
		AutoDonutConfig cfg = AutoDonutConfig.get();
		LocalPlayer player = mc.player;

		if (!cfg.clientSmelterEnabled || externallyPaused || player == null || mc.level == null || mc.gameMode == null) {
			reset();
			return;
		}
		if (warningCooldown > 0) warningCooldown--;

		switch (state) {
			case IDLE -> tickIdle(mc, player, cfg);
			case OPENING -> tickOpening(mc, player);
			case SERVICING -> tickServicing(mc, player, cfg);
		}
	}

	// ---------------------------------------------------------------
	// States
	// ---------------------------------------------------------------

	private static void tickIdle(Minecraft mc, LocalPlayer player, AutoDonutConfig cfg) {
		if (cooldownTicks > 0) {
			cooldownTicks--;
			return;
		}
		// Never fight a GUI the player opened themselves.
		if (mc.gui.screen() != null) return;

		targetFurnace = findFurnace(mc, player, cfg);
		if (targetFurnace == null) {
			// Nothing in range - check again shortly rather than every tick.
			cooldownTicks = 20;
			return;
		}

		BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(targetFurnace), Direction.UP, targetFurnace, false);
		mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);
		state = State.OPENING;
		stepDelay = 10; // give the server a moment to send the container
	}

	private static void tickOpening(Minecraft mc, LocalPlayer player) {
		if (stepDelay-- > 0) return;

		if (isFurnaceScreenOpen(mc)) {
			state = State.SERVICING;
			stepDelay = 2;
			return;
		}
		// The furnace didn't open (locked, out of range, someone else using it, or we
		// right-clicked something else). Back off and retry later.
		if (mc.gui.screen() != null && player != null) player.closeContainer();
		finishRun(AutoDonutConfig.get());
	}

	private static void tickServicing(Minecraft mc, LocalPlayer player, AutoDonutConfig cfg) {
		if (stepDelay-- > 0) return;

		if (!isFurnaceScreenOpen(mc)) {
			// Player closed it, or the server did. Don't fight them.
			finishRun(cfg);
			return;
		}

		AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) mc.gui.screen();
		AbstractContainerMenu menu = screen.getMenu();

		// Never act while something is on the cursor - that's how items get lost.
		if (!menu.getCarried().isEmpty()) {
			stepDelay = 2;
			return;
		}

		// One action per visit, so the server always sees a sane, human-paced click rate.
		if (cfg.smelterTakeOutput && !menu.getSlot(SLOT_OUTPUT).getItem().isEmpty()) {
			mc.gameMode.handleContainerInput(menu.containerId, SLOT_OUTPUT, 0, ContainerInput.QUICK_MOVE, player);
			outputsCollected++;
			stepDelay = 3;
			return;
		}

		if (cfg.smelterInsertFuel && menu.getSlot(SLOT_FUEL).getItem().isEmpty()) {
			int source = findInPlayerSection(menu, stack -> isListed(stack, cfg.smelterFuelItems));
			if (source >= 0) {
				moveStack(mc, player, menu, source, SLOT_FUEL);
				fuelInserted++;
				stepDelay = 3;
				return;
			}
		}

		if (cfg.smelterInsertInput && menu.getSlot(SLOT_INPUT).getItem().isEmpty()) {
			int source = findInPlayerSection(menu, stack -> isListed(stack, cfg.smelterInputItems));
			if (source >= 0) {
				moveStack(mc, player, menu, source, SLOT_INPUT);
				inputsInserted++;
				stepDelay = 3;
				return;
			}
		}

		// Nothing left to do this run.
		player.closeContainer();
		serviceRuns++;
		finishRun(cfg);
	}

	private static void finishRun(AutoDonutConfig cfg) {
		state = State.IDLE;
		targetFurnace = null;
		cooldownTicks = Math.max(20, cfg.smelterIntervalSeconds * 20);
	}

	private static void reset() {
		state = State.IDLE;
		targetFurnace = null;
		cooldownTicks = 0;
		stepDelay = 0;
	}

	// ---------------------------------------------------------------
	// Helpers
	// ---------------------------------------------------------------

	/**
	 * Picks a stack up out of one slot and puts it down in another - literally two
	 * clicks, the same pair a player makes by hand.
	 */
	private static void moveStack(Minecraft mc, LocalPlayer player, AbstractContainerMenu menu, int from, int to) {
		mc.gameMode.handleContainerInput(menu.containerId, from, 0, ContainerInput.PICKUP, player);
		if (menu.getCarried().isEmpty()) return; // pickup didn't take - bail rather than click blindly
		mc.gameMode.handleContainerInput(menu.containerId, to, 0, ContainerInput.PICKUP, player);
		if (!menu.getCarried().isEmpty()) {
			// Destination refused it (wrong item for that slot); put it straight back.
			mc.gameMode.handleContainerInput(menu.containerId, from, 0, ContainerInput.PICKUP, player);
		}
	}

	private static boolean isFurnaceScreenOpen(Minecraft mc) {
		if (!(mc.gui.screen() instanceof AbstractContainerScreen<?> screen)) return false;
		AbstractContainerMenu menu = screen.getMenu();
		return menu.slots.size() == FURNACE_CONTAINER_SLOTS + 36;
	}

	private static int findInPlayerSection(AbstractContainerMenu menu, java.util.function.Predicate<ItemStack> predicate) {
		for (int i = FURNACE_CONTAINER_SLOTS; i < menu.slots.size(); i++) {
			ItemStack stack = menu.getSlot(i).getItem();
			if (!stack.isEmpty() && predicate.test(stack)) return i;
		}
		return -1;
	}

	private static boolean isListed(ItemStack stack, java.util.List<String> ids) {
		if (stack.isEmpty() || ids == null || ids.isEmpty()) return false;
		String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
		for (String candidate : ids) {
			if (candidate == null) continue;
			String trimmed = candidate.trim();
			if (trimmed.isEmpty()) continue;
			// Accept both "minecraft:coal" and bare "coal" so the settings box is forgiving.
			if (trimmed.equalsIgnoreCase(id) || trimmed.equalsIgnoreCase(id.substring(id.indexOf(':') + 1))) {
				return true;
			}
		}
		return false;
	}

	private static boolean isFurnaceBlock(BlockState blockState) {
		net.minecraft.world.level.block.Block block = blockState.getBlock();
		return block == Blocks.FURNACE || block == Blocks.BLAST_FURNACE || block == Blocks.SMOKER;
	}

	/** Nearest furnace-family block within both the configured radius and your real reach. */
	private static BlockPos findFurnace(Minecraft mc, LocalPlayer player, AutoDonutConfig cfg) {
		double reach = player.blockInteractionRange();
		int radius = Math.max(1, Math.min(cfg.smelterSearchRadius, (int) Math.ceil(reach)));
		BlockPos origin = player.blockPosition();
		Vec3 eye = player.getEyePosition();

		BlockPos best = null;
		double bestDist = Double.MAX_VALUE;

		for (int dx = -radius; dx <= radius; dx++) {
			for (int dy = -radius; dy <= radius; dy++) {
				for (int dz = -radius; dz <= radius; dz++) {
					BlockPos pos = origin.offset(dx, dy, dz);
					if (!mc.level.isLoaded(pos)) continue;
					if (!isFurnaceBlock(mc.level.getBlockState(pos))) continue;
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

	static void warn(LocalPlayer player, String message) {
		if (warningCooldown > 0) return;
		warningCooldown = 100;
		player.sendSystemMessage(Component.literal("[AutoDonut] Auto Smelter: " + message));
	}

	// ---------------------------------------------------------------
	// Stats
	// ---------------------------------------------------------------

	public static void announceStats() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		mc.player.sendSystemMessage(Component.literal(String.format(
				"[AutoDonut Smelter] Session: %d service run(s), %d output stack(s) collected, %d fuel and %d input inserted.",
				serviceRuns, outputsCollected, fuelInserted, inputsInserted)));
	}

	public static void resetStats() {
		serviceRuns = 0;
		outputsCollected = 0;
		fuelInserted = 0;
		inputsInserted = 0;
	}
}
