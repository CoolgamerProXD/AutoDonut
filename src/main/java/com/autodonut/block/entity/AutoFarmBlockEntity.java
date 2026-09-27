package com.autodonut.block.entity;

import com.autodonut.config.AutoDonutConfig;
import com.autodonut.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.List;

/**
 * Placed in the middle of a crop field (same Y level as the crops).
 * Every second it scans a square radius around itself, harvests any fully
 * grown crop (wheat/carrots/potatoes/beetroot) and instantly replants it,
 * storing the produce in its own 9-slot buffer (hopper-extractable).
 *
 * Sneak + right click cycles the scan radius: 2 -> 4 -> 6 -> 8 -> 2 ...
 */
public class AutoFarmBlockEntity extends AutomationBlockEntity {

	public static final int SIZE = 9;
	private static final int[] RADII = {2, 4, 6, 8};

	private int radius = 4;
	private int tickCounter = 0;

	public AutoFarmBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.AUTO_FARM, pos, state, SIZE);
	}

	public int getRadius() {
		return radius;
	}

	public void cycleRadius() {
		int idx = 0;
		for (int i = 0; i < RADII.length; i++) {
			if (RADII[i] == radius) { idx = i; break; }
		}
		radius = RADII[(idx + 1) % RADII.length];
		setChanged();
	}

	public static void serverTick(ServerLevel level, BlockPos pos, BlockState state, AutoFarmBlockEntity entity) {
		AutoDonutConfig cfg = AutoDonutConfig.get();
		if (!cfg.automateEnabled || !cfg.autoFarmEnabled) return;

		entity.tickCounter++;
		if (entity.tickCounter < 20) return;
		entity.tickCounter = 0;

		int r = entity.radius;
		boolean changed = false;
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

		for (int dx = -r; dx <= r; dx++) {
			for (int dz = -r; dz <= r; dz++) {
				if (dx == 0 && dz == 0) continue;
				cursor.set(pos.getX() + dx, pos.getY(), pos.getZ() + dz);
				BlockState cropState = level.getBlockState(cursor);
				if (!(cropState.getBlock() instanceof CropBlock crop)) continue;
				if (!crop.isMaxAge(cropState)) continue;

				List<ItemStack> drops = Block.getDrops(cropState, level, cursor, null);
				level.setBlockAndUpdate(cursor, crop.getStateForAge(0));

				for (ItemStack drop : drops) {
					ItemStack leftover = entity.insert(drop, 0, SIZE);
					if (!leftover.isEmpty()) {
						Block.popResource(level, cursor, leftover);
					}
				}
				changed = true;
			}
		}

		if (changed) entity.setChanged();
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.radius = input.getIntOr("Radius", 4);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("Radius", radius);
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return false;
	}
}
