package com.autodonut.registry;

import com.autodonut.AutoDonut;
import com.autodonut.block.entity.AutoFarmBlockEntity;
import com.autodonut.block.entity.AutoMinerBlockEntity;
import com.autodonut.block.entity.AutoSmelterBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {

	public static final BlockEntityType<AutoSmelterBlockEntity> AUTO_SMELTER =
			register("auto_smelter", FabricBlockEntityTypeBuilder.create(AutoSmelterBlockEntity::new, ModBlocks.AUTO_SMELTER).build());

	public static final BlockEntityType<AutoFarmBlockEntity> AUTO_FARM =
			register("auto_farm", FabricBlockEntityTypeBuilder.create(AutoFarmBlockEntity::new, ModBlocks.AUTO_FARM).build());

	public static final BlockEntityType<AutoMinerBlockEntity> AUTO_MINER =
			register("auto_miner", FabricBlockEntityTypeBuilder.create(AutoMinerBlockEntity::new, ModBlocks.AUTO_MINER).build());

	private static <T extends BlockEntityType<?>> T register(String path, T type) {
		ResourceKey<BlockEntityType<?>> key =
				ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath(AutoDonut.MOD_ID, path));
		return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, key, type);
	}

	public static void register() {
		// Triggers static initializers above.
	}
}
