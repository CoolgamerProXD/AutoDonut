package com.autodonut.command;

import com.autodonut.ai.AiClient;
import com.autodonut.config.AutoDonutConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;

public class AutoDonutCommand {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("autodonut")
				.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))

				.then(Commands.literal("status").executes(AutoDonutCommand::status))

				.then(Commands.literal("automate")
						.then(Commands.argument("on", bool()).executes(ctx -> {
							AutoDonutConfig cfg = AutoDonutConfig.get();
							cfg.automateEnabled = getBool(ctx, "on");
							AutoDonutConfig.save();
							ctx.getSource().sendSuccess(() -> Component.literal(
									"[AutoDonut] Automation " + (cfg.automateEnabled ? "ENABLED" : "DISABLED") + "."), true);
							return 1;
						})))

				.then(Commands.literal("machine")
						.then(Commands.literal("smelter").then(Commands.argument("on", bool()).executes(ctx -> {
							AutoDonutConfig.get().autoSmelterEnabled = getBool(ctx, "on");
							AutoDonutConfig.save();
							return ok(ctx, "Auto Smelter " + onOff(AutoDonutConfig.get().autoSmelterEnabled));
						})))
						.then(Commands.literal("farm").then(Commands.argument("on", bool()).executes(ctx -> {
							AutoDonutConfig.get().autoFarmEnabled = getBool(ctx, "on");
							AutoDonutConfig.save();
							return ok(ctx, "Auto Farm " + onOff(AutoDonutConfig.get().autoFarmEnabled));
						})))
						.then(Commands.literal("miner").then(Commands.argument("on", bool()).executes(ctx -> {
							AutoDonutConfig.get().autoMinerEnabled = getBool(ctx, "on");
							AutoDonutConfig.save();
							return ok(ctx, "Auto Miner " + onOff(AutoDonutConfig.get().autoMinerEnabled));
						}))))

				.then(Commands.literal("kit").executes(ctx -> {
					var player = ctx.getSource().getPlayer();
					if (player == null) {
						ctx.getSource().sendFailure(Component.literal("Only a player can request the kit."));
						return 0;
					}
					player.addItem(new net.minecraft.world.item.ItemStack(com.autodonut.registry.ModItems.AUTO_SMELTER_ITEM));
					player.addItem(new net.minecraft.world.item.ItemStack(com.autodonut.registry.ModItems.AUTO_FARM_ITEM));
					player.addItem(new net.minecraft.world.item.ItemStack(com.autodonut.registry.ModItems.AUTO_MINER_ITEM));
					ctx.getSource().sendSuccess(() -> Component.literal("[AutoDonut] Gave you one of each automation block."), true);
					return 1;
				}))

				.then(Commands.literal("ai")
						.then(Commands.literal("setkey")
								.then(Commands.argument("key", StringArgumentType.greedyString()).executes(ctx -> {
									String key = StringArgumentType.getString(ctx, "key");
									AutoDonutConfig.get().aiApiKey = key;
									AutoDonutConfig.save();
									ctx.getSource().sendSuccess(() -> Component.literal(
											"[AutoDonut AI] API key saved (" + key.length() + " chars). It is stored locally in config/autodonut.json and only sent to " + AutoDonutConfig.get().aiBaseUrl + "."), false);
									return 1;
								})))
						.then(Commands.literal("model")
								.then(Commands.argument("name", StringArgumentType.greedyString()).executes(ctx -> {
									AutoDonutConfig.get().aiModel = StringArgumentType.getString(ctx, "name");
									AutoDonutConfig.save();
									return ok(ctx, "AI model set to " + AutoDonutConfig.get().aiModel);
								})))
						.then(Commands.literal("baseurl")
								.then(Commands.argument("url", StringArgumentType.greedyString()).executes(ctx -> {
									AutoDonutConfig.get().aiBaseUrl = StringArgumentType.getString(ctx, "url");
									AutoDonutConfig.save();
									return ok(ctx, "AI endpoint set to " + AutoDonutConfig.get().aiBaseUrl);
								})))
						.then(Commands.literal("ask")
								.then(Commands.argument("question", StringArgumentType.greedyString()).executes(ctx -> {
									String question = StringArgumentType.getString(ctx, "question");
									var source = ctx.getSource();
									source.sendSystemMessage(Component.literal("[AutoDonut AI] Thinking..."));
									AiClient.ask(
											"You are AutoDonut's in-game assistant for a private, self-hosted Minecraft "
													+ "survival server with DonutSMP-like mechanics (mining, farming, smelting, "
													+ "an economy/shop). Give short, practical, in-character tips. Keep replies under 4 sentences.",
											question
									).thenAccept(answer -> source.sendSystemMessage(Component.literal("[AutoDonut AI] " + answer)));
									return 1;
								})))
						.then(Commands.literal("auto")
								.then(Commands.argument("on", bool()).executes(ctx -> {
									AutoDonutConfig.get().aiAutoControllerEnabled = getBool(ctx, "on");
									AutoDonutConfig.save();
									return ok(ctx, "AI automation controller " + onOff(AutoDonutConfig.get().aiAutoControllerEnabled)
											+ " (advisory only for now - it will suggest priorities in chat).");
								}))))
		);
	}

	private static int status(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
		AutoDonutConfig cfg = AutoDonutConfig.get();
		ctx.getSource().sendSuccess(() -> Component.literal(String.format(
				"[AutoDonut] automate=%s | smelter=%s farm=%s miner=%s | AI key set=%s auto=%s",
				onOff(cfg.automateEnabled), onOff(cfg.autoSmelterEnabled), onOff(cfg.autoFarmEnabled),
				onOff(cfg.autoMinerEnabled), (cfg.aiApiKey != null && !cfg.aiApiKey.isBlank()),
				onOff(cfg.aiAutoControllerEnabled))), false);
		return 1;
	}

	private static int ok(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx, String msg) {
		ctx.getSource().sendSuccess(() -> Component.literal("[AutoDonut] " + msg), true);
		return 1;
	}

	private static String onOff(boolean b) {
		return b ? "ON" : "OFF";
	}
}
