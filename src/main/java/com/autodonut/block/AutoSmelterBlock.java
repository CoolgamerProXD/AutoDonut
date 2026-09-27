package com.autodonut.block;

import com.autodonut.block.entity.AutoSmelterBlockEntity;
import com.autodonut.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class AutoSmelterBlock extends BaseEntityBlock {

	public static final com.mojang.serialization.MapCodec<AutoSmelterBlock> CODEC = simpleCodec(AutoSmelterBlock::new);

	@Override
	protected com.mojang.serialization.MapCodec<AutoSmelterBlock> codec() {
		return CODEC;
	}

	public AutoSmelterBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new AutoSmelterBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide()) return null;
		return createTickerHelper(type, ModBlockEntities.AUTO_SMELTER,
				(lvl, pos, st, be) -> AutoSmelterBlockEntity.serverTick((ServerLevel) lvl, pos, st, be));
	}

	@Override
	protected InteractionResult useItemOn(ItemStack heldItem, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (level.isClientSide()) return InteractionResult.SUCCESS;
		if (!(level.getBlockEntity(pos) instanceof AutoSmelterBlockEntity be)) return InteractionResult.PASS;
		if (heldItem.isEmpty()) return InteractionResult.PASS;
		ItemStack leftover = be.insert(heldItem.copy(), 0, AutoSmelterBlockEntity.INPUT_SLOTS);
		if (leftover.getCount() != heldItem.getCount()) {
			heldItem.setCount(leftover.getCount());
			return InteractionResult.CONSUME;
		}
		return InteractionResult.PASS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (level.isClientSide()) return InteractionResult.SUCCESS;
		if (!(level.getBlockEntity(pos) instanceof AutoSmelterBlockEntity be)) return InteractionResult.PASS;
		ItemStack out = be.extractFirst(AutoSmelterBlockEntity.INPUT_SLOTS, AutoSmelterBlockEntity.SIZE);
		if (!out.isEmpty()) {
			if (!player.addItem(out)) {
				popIntoWorld(level, pos, out);
			}
			return InteractionResult.CONSUME;
		}
		return InteractionResult.PASS;
	}

	private static void popIntoWorld(Level level, BlockPos pos, ItemStack stack) {
		net.minecraft.world.level.block.Block.popResource(level, pos, stack);
	}
}
