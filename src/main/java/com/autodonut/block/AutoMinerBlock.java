package com.autodonut.block;

import com.autodonut.block.entity.AutoMinerBlockEntity;
import com.autodonut.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class AutoMinerBlock extends BaseEntityBlock {

	public static final com.mojang.serialization.MapCodec<AutoMinerBlock> CODEC = simpleCodec(AutoMinerBlock::new);

	@Override
	protected com.mojang.serialization.MapCodec<AutoMinerBlock> codec() {
		return CODEC;
	}

	public AutoMinerBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new AutoMinerBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide()) return null;
		return createTickerHelper(type, ModBlockEntities.AUTO_MINER,
				(lvl, pos, st, be) -> AutoMinerBlockEntity.serverTick((ServerLevel) lvl, pos, st, be));
	}

	@Override
	protected InteractionResult useItemOn(ItemStack heldItem, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (level.isClientSide()) return InteractionResult.SUCCESS;
		if (!(level.getBlockEntity(pos) instanceof AutoMinerBlockEntity be)) return InteractionResult.PASS;
		if (heldItem.isEmpty()) return InteractionResult.PASS;
		ItemStack leftover = be.insert(heldItem.copy(), AutoMinerBlockEntity.FUEL_SLOT, AutoMinerBlockEntity.FUEL_SLOT + 1);
		if (leftover.getCount() != heldItem.getCount() && be.canPlaceItem(AutoMinerBlockEntity.FUEL_SLOT, heldItem)) {
			heldItem.setCount(leftover.getCount());
			return InteractionResult.CONSUME;
		}
		return InteractionResult.PASS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (level.isClientSide()) return InteractionResult.SUCCESS;
		if (!(level.getBlockEntity(pos) instanceof AutoMinerBlockEntity be)) return InteractionResult.PASS;

		if (player.isShiftKeyDown()) {
			be.cycleWidth();
			int span = be.getWidth() * 2 + 1;
			player.sendOverlayMessage(Component.literal("AutoDonut Miner dig width: " + span + "x" + span));
			return InteractionResult.CONSUME;
		}

		ItemStack out = be.extractFirst(AutoMinerBlockEntity.STORAGE_START, AutoMinerBlockEntity.SIZE);
		if (!out.isEmpty()) {
			if (!player.addItem(out)) {
				Block.popResource(level, pos, out);
			}
			return InteractionResult.CONSUME;
		}
		return InteractionResult.PASS;
	}
}
