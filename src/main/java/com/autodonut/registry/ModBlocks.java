package com.autodonut.registry;

import com.autodonut.AutoDonut;
import com.autodonut.block.AutoFarmBlock;
import com.autodonut.block.AutoMinerBlock;
import com.autodonut.block.AutoSmelterBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class ModBlocks {

	public static final Block AUTO_SMELTER = register("auto_smelter",
			key -> new AutoSmelterBlock(BlockBehaviour.Properties.of()
					.setId(key)
					.mapColor(MapColor.COLOR_ORANGE)
					.sound(SoundType.METAL)
					.strength(3.5f)
					.requiresCorrectToolForDrops()));

	public static final Block AUTO_FARM = register("auto_farm",
			key -> new AutoFarmBlock(BlockBehaviour.Properties.of()
					.setId(key)
					.mapColor(MapColor.COLOR_GREEN)
					.sound(SoundType.WOOD)
					.strength(2.5f)));

	public static final Block AUTO_MINER = register("auto_miner",
			key -> new AutoMinerBlock(BlockBehaviour.Properties.of()
					.setId(key)
					.mapColor(MapColor.COLOR_GRAY)
					.sound(SoundType.NETHERITE_BLOCK)
					.strength(5.0f)
					.requiresCorrectToolForDrops()));

	private static Block register(String path, java.util.function.Function<ResourceKey<Block>, Block> factory) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(AutoDonut.MOD_ID, path));
		Block block = factory.apply(key);
		return Registry.register(BuiltInRegistries.BLOCK, key, block);
	}

	public static void register() {
		// Triggers static initializers above.
	}
}
