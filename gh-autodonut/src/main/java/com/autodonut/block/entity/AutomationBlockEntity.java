package com.autodonut.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Shared base for AutoDonut's automation machines.
 * Provides a simple internal inventory that hoppers (and players, via
 * right-click) can pull items out of - just like a vanilla furnace/chest,
 * so these machines slot into normal redstone/item-transport setups.
 */
public abstract class AutomationBlockEntity extends BlockEntity implements WorldlyContainer {

	protected NonNullList<ItemStack> items;

	protected AutomationBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int size) {
		super(type, pos, state);
		this.items = NonNullList.withSize(size, ItemStack.EMPTY);
	}

	@Override
	public int getContainerSize() {
		return items.size();
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack stack : items) {
			if (!stack.isEmpty()) return false;
		}
		return true;
	}

	@Override
	public ItemStack getItem(int slot) {
		return items.get(slot);
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		ItemStack result = ContainerHelper.removeItem(items, slot, amount);
		if (!result.isEmpty()) setChanged();
		return result;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return ContainerHelper.takeItem(items, slot);
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		items.set(slot, stack);
		if (stack.getCount() > getMaxStackSize()) {
			stack.setCount(getMaxStackSize());
		}
		setChanged();
	}

	@Override
	public boolean stillValid(Player player) {
		if (level == null || level.getBlockEntity(worldPosition) != this) return false;
		return player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
	}

	@Override
	public void clearContent() {
		items.clear();
	}

	@Override
	public int[] getSlotsForFace(Direction direction) {
		int[] slots = new int[items.size()];
		for (int i = 0; i < slots.length; i++) slots[i] = i;
		return slots;
	}

	@Override
	public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) {
		return canPlaceItem(slot, stack);
	}

	@Override
	public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
		return true;
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(input, this.items);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		ContainerHelper.saveAllItems(output, this.items);
	}

	/** Removes and returns the first non-empty stack found in the given slot range. */
	public ItemStack extractFirst(int fromSlot, int toSlotExclusive) {
		for (int i = fromSlot; i < toSlotExclusive; i++) {
			ItemStack stack = items.get(i);
			if (!stack.isEmpty()) {
				ItemStack result = stack.copy();
				items.set(i, ItemStack.EMPTY);
				setChanged();
				return result;
			}
		}
		return ItemStack.EMPTY;
	}

	/** Tries to push one whole/partial stack into this container's buffer. Returns leftover. */
	public ItemStack insert(ItemStack stack, int fromSlot, int toSlotExclusive) {
		ItemStack remaining = stack.copy();
		for (int i = fromSlot; i < toSlotExclusive && !remaining.isEmpty(); i++) {
			ItemStack slotStack = items.get(i);
			if (slotStack.isEmpty()) {
				int move = Math.min(remaining.getCount(), remaining.getMaxStackSize());
				ItemStack placed = remaining.copy();
				placed.setCount(move);
				items.set(i, placed);
				remaining.shrink(move);
			} else if (ItemStack.isSameItemSameComponents(slotStack, remaining)) {
				int space = slotStack.getMaxStackSize() - slotStack.getCount();
				int move = Math.min(space, remaining.getCount());
				if (move > 0) {
					slotStack.grow(move);
					remaining.shrink(move);
				}
			}
		}
		if (remaining.getCount() != stack.getCount()) setChanged();
		return remaining;
	}
}
