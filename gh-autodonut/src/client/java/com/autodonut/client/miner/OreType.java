package com.autodonut.client.miner;

import com.autodonut.config.AutoDonutConfig;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

/**
 * The ores the client Auto Miner knows about. Each entry is a logical ore
 * (e.g. "Iron") that can cover multiple real blocks (regular + deepslate
 * variants, nether variants, etc). Whether an ore is currently something the
 * miner will seek out/break is controlled by {@link AutoDonutConfig#minerDisabledOres}.
 */
public enum OreType {
	COAL("coal", "Coal", Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE),
	IRON("iron", "Iron", Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE),
	COPPER("copper", "Copper", Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE),
	GOLD("gold", "Gold", Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.NETHER_GOLD_ORE),
	REDSTONE("redstone", "Redstone", Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE),
	LAPIS("lapis", "Lapis Lazuli", Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE),
	DIAMOND("diamond", "Diamond", Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE),
	EMERALD("emerald", "Emerald", Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE),
	QUARTZ("quartz", "Nether Quartz", Blocks.NETHER_QUARTZ_ORE),
	ANCIENT_DEBRIS("ancient_debris", "Ancient Debris", Blocks.ANCIENT_DEBRIS);

	public final String key;
	public final String displayName;
	public final List<Block> blocks;

	OreType(String key, String displayName, Block... blocks) {
		this.key = key;
		this.displayName = displayName;
		this.blocks = List.of(blocks);
	}

	public boolean matches(Block block) {
		return blocks.contains(block);
	}

	public boolean isEnabled(AutoDonutConfig cfg) {
		return !cfg.minerDisabledOres.contains(key);
	}

	public static OreType fromBlock(Block block) {
		for (OreType type : values()) {
			if (type.matches(block)) return type;
		}
		return null;
	}

	public static OreType byKey(String key) {
		for (OreType type : values()) {
			if (type.key.equalsIgnoreCase(key)) return type;
		}
		return null;
	}
}
