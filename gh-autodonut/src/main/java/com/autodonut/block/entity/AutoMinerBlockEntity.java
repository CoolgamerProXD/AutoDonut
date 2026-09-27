package com.autodonut.block.entity;

import com.autodonut.config.AutoDonutConfig;
import com.autodonut.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.List;

/**
 * A fuel-powered quarry. Place it and it digs a square shaft straight down
 * beneath itself, layer by layer, storing everything it mines (minus
 * unbreakable/liquid blocks, which are skipped) in its internal buffer.
 *
 * Slot 0 = fuel (coal / charcoal / coal block). Slots 1-27 = storage.
 * Sneak + right click cycles the dig width: 1 -> 3 -> 5 -> 1 ...
 */
public class AutoMinerBlockEntity extends AutomationBlockEntity {

	public static final int FUEL_SLOT = 0;
	public static final int STORAGE_START = 1;
	public static final int SIZE = 28; // 1 fuel + 27 storage

	private static final int[] WIDTHS = {1, 3, 5};

	private int width = 3;
	private int minesRemaining = 0; // "fuel charge" left, in block-mines
	private int depth = 1; // how many blocks below the miner we've dug down to
	private int layerIndex = 0; // index within the current layer's scan pattern
	private int tickCounter = 0;

	public AutoMinerBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.AUTO_MINER, pos, state, SIZE);
	}

	public void cycleWidth() {
		int idx = 0;
		for (int i = 0; i < WIDTHS.length; i++) if (WIDTHS[i] == width) { idx = i; break; }
		width = WIDTHS[(idx + 1) % WIDTHS.length];
		setChanged();
	}

	public int getWidth() {
		return width;
	}

	public static void serverTick(ServerLevel level, BlockPos pos, BlockState state, AutoMinerBlockEntity entity) {
		AutoDonutConfig cfg = AutoDonutConfig.get();
		if (!cfg.automateEnabled || !cfg.autoMinerEnabled) return;

		entity.tickCounter++;
		if (entity.tickCounter < 4) return; // one mining action every 4 ticks (5/sec) when fueled
		entity.tickCounter = 0;

		if (entity.minesRemaining <= 0) {
			entity.tryConsumeFuel();
			if (entity.minesRemaining <= 0) return; // out of fuel, idle
		}

		if (level.getMinY() >= pos.getY() - entity.depth) return; // hit bottom of world

		int w = entity.width;
		int span = w * 2 + 1;
		int cellsPerLayer = span * span;

		// Try to find the next minable block in the current layer; skip already-air ones.
		int attempts = 0;
		while (attempts < cellsPerLayer) {
			int idx = entity.layerIndex;
			int lx = (idx % span) - w;
			int lz = (idx / span) - w;
			BlockPos target = pos.offset(lx, -entity.depth, lz);

			entity.layerIndex++;
			attempts++;
			boolean layerDone = entity.layerIndex >= cellsPerLayer;
			if (layerDone) entity.layerIndex = 0;

			BlockState targetState = level.getBlockState(target);
			if (entity.isMinable(targetState)) {
				List<ItemStack> drops = Block.getDrops(targetState, level, target, null);
				boolean fits = true;
				for (ItemStack drop : drops) {
					if (!entity.wouldFit(drop)) { fits = false; break; }
				}
				if (!fits) return; // storage full, wait for a hopper to empty it

				level.removeBlock(target, false);
				for (ItemStack drop : drops) {
					entity.insert(drop, STORAGE_START, SIZE);
				}
				entity.minesRemaining--;
				entity.setChanged();
				if (layerDone) entity.depth++;
				return;
			}

			if (layerDone) {
				entity.depth++;
				return;
			}
		}
	}

	private boolean isMinable(BlockState state) {
		if (state.isAir()) return false;
		if (!state.getFluidState().isEmpty()) return false;
		if (state.getDestroySpeed(null, null) < 0) return false; // unbreakable (bedrock etc)
		return true;
	}

	private boolean wouldFit(ItemStack stack) {
		for (int i = STORAGE_START; i < SIZE; i++) {
			ItemStack slot = items.get(i);
			if (slot.isEmpty()) return true;
			if (ItemStack.isSameItemSameComponents(slot, stack) && slot.getCount() < slot.getMaxStackSize()) return true;
		}
		return false;
	}

	private void tryConsumeFuel() {
		ItemStack fuel = items.get(FUEL_SLOT);
		if (fuel.isEmpty()) return;
		Item item = fuel.getItem();
		int mines = 0;
		if (item == Items.COAL || item == Items.CHARCOAL) mines = 16;
		else if (item == Items.COAL_BLOCK) mines = 160;
		if (mines > 0) {
			fuel.shrink(1);
			minesRemaining += mines;
			setChanged();
		}
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		if (slot == FUEL_SLOT) {
			Item item = stack.getItem();
			return item == Items.COAL || item == Items.CHARCOAL || item == Items.COAL_BLOCK;
		}
		return false;
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.width = input.getIntOr("Width", 3);
		this.minesRemaining = input.getIntOr("MinesRemaining", 0);
		this.depth = input.getIntOr("Depth", 1);
		this.layerIndex = input.getIntOr("LayerIndex", 0);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("Width", width);
		output.putInt("MinesRemaining", minesRemaining);
		output.putInt("Depth", depth);
		output.putInt("LayerIndex", layerIndex);
	}
}
