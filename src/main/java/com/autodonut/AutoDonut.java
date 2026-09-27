package com.autodonut;

import com.autodonut.command.AutoDonutCommand;
import com.autodonut.config.AutoDonutConfig;
import com.autodonut.registry.ModBlockEntities;
import com.autodonut.registry.ModBlocks;
import com.autodonut.registry.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoDonut implements ModInitializer {
	public static final String MOD_ID = "autodonut";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("[AutoDonut] Loading automation core...");

		AutoDonutConfig.load();

		ModBlocks.register();
		ModItems.register();
		ModBlockEntities.register();

		CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) ->
				AutoDonutCommand.register(dispatcher));

		LOGGER.info("[AutoDonut] Ready. Automation is {}.",
				AutoDonutConfig.get().automateEnabled ? "ENABLED" : "disabled");
	}
}
