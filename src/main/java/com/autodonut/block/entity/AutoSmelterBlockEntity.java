package com.autodonut.block.entity;

import com.autodonut.config.AutoDonutConfig;
import com.autodonut.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/**
 * A no-fuel-required, instant smelter. Feed raw items into slots 0-4 (via
 * hopper or right click); smeltable results appear in slots 5-9 (also
 * hopper-extractable). This is the "automate smelting" QoL machine - it
 * still costs the same items/time-equivalent as vanilla smelting in
 * resources, it just removes the manual furnace-babysitting.
 */
public class AutoSmelterBlockEntity extends AutomationBlockEntity {

	public static final int INPUT_SLOTS = 5;
	public static final int OUTPUT_SLOTS = 5;
	public static final int SIZE = INPUT_SLOTS + OUTPUT_SLOTS;

	private static final RecipeManager.CachedCheck<SingleRecipeInput, SmeltingRecipe> QUICK_CHECK =
			RecipeManager.createCheck(RecipeType.SMELTING);

	public AutoSmelterBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.AUTO_SMELTER, pos, state, SIZE);
	}

	public static void serverTick(ServerLevel level, BlockPos pos, BlockState state, AutoSmelterBlockEntity entity) {
		AutoDonutConfig cfg = AutoDonutConfig.get();
		if (!cfg.automateEnabled || !cfg.autoSmelterEnabled) return;

		boolean changed = false;
		for (int in = 0; in < INPUT_SLOTS; in++) {
			ItemStack input = entity.items.get(in);
			if (input.isEmpty()) continue;

			SingleRecipeInput recipeInput = new SingleRecipeInput(input);
			Optional<RecipeHolder<SmeltingRecipe>> match = QUICK_CHECK.getRecipeFor(recipeInput, level);
			if (match.isEmpty()) continue;

			ItemStack result = match.get().value().assemble(recipeInput);
			if (result.isEmpty()) continue;

			if (entity.tryInsertOutput(result)) {
				input.shrink(1);
				changed = true;
			}
		}

		if (changed) entity.setChanged();
	}

	private boolean tryInsertOutput(ItemStack sample) {
		for (int out = INPUT_SLOTS; out < SIZE; out++) {
			ItemStack slot = items.get(out);
			if (slot.isEmpty()) {
				items.set(out, sample.copy());
				return true;
			}
			if (ItemStack.isSameItemSameComponents(slot, sample) && slot.getCount() + sample.getCount() <= slot.getMaxStackSize()) {
				slot.grow(sample.getCount());
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return slot < INPUT_SLOTS;
	}
}
