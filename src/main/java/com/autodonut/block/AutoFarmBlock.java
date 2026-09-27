package com.autodonut.block;

import com.autodonut.block.entity.AutoFarmBlockEntity;
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

public class AutoFarmBlock extends BaseEntityBlock {

	public static final com.mojang.serialization.MapCodec<AutoFarmBlock> CODEC = simpleCodec(AutoFarmBlock::new);

	@Override
	protected com.mojang.serialization.MapCodec<AutoFarmBlock> codec() {
		return CODEC;
	}

	public AutoFarmBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new AutoFarmBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide()) return null;
		return createTickerHelper(type, ModBlockEntities.AUTO_FARM,
				(lvl, pos, st, be) -> AutoFarmBlockEntity.serverTick((ServerLevel) lvl, pos, st, be));
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (level.isClientSide()) return InteractionResult.SUCCESS;
		if (!(level.getBlockEntity(pos) instanceof AutoFarmBlockEntity be)) return InteractionResult.PASS;

		if (player.isShiftKeyDown()) {
			be.cycleRadius();
			player.displayClientMessage(Component.literal("AutoDonut Farm radius: " + be.getRadius()), true);
			return InteractionResult.CONSUME;
		}

		ItemStack out = be.extractFirst(0, AutoFarmBlockEntity.SIZE);
		if (!out.isEmpty()) {
			if (!player.addItem(out)) {
				Block.popResource(level, pos, out);
			}
			return InteractionResult.CONSUME;
		}
		return InteractionResult.PASS;
	}

	@Override
	protected InteractionResult useItemOn(ItemStack heldItem, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		return InteractionResult.PASS;
	}
}
