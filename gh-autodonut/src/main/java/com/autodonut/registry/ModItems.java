package com.autodonut.registry;

import com.autodonut.AutoDonut;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public class ModItems {

	public static final Item AUTO_SMELTER_ITEM = registerBlockItem("auto_smelter", ModBlocks.AUTO_SMELTER);
	public static final Item AUTO_FARM_ITEM = registerBlockItem("auto_farm", ModBlocks.AUTO_FARM);
	public static final Item AUTO_MINER_ITEM = registerBlockItem("auto_miner", ModBlocks.AUTO_MINER);

	private static Item registerBlockItem(String path, net.minecraft.world.level.block.Block block) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(AutoDonut.MOD_ID, path));
		BlockItem item = new BlockItem(block, new Item.Properties().setId(key));
		return Registry.register(BuiltInRegistries.ITEM, key, item);
	}

	public static void register() {
		// Triggers static initializers above.
	}
}
