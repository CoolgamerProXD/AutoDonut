package com.autodonut.client;

import com.autodonut.client.automation.ClientFarmEngine;
import com.autodonut.client.automation.ClientSmelterEngine;
import com.autodonut.client.economy.AhScanner;
import com.autodonut.client.economy.ChestSellEngine;
import com.autodonut.client.economy.EconomyGuiHooks;
import com.autodonut.client.economy.MarketDatabase;
import com.autodonut.client.economy.ProfitLedger;
import com.autodonut.client.economy.QuickSellEngine;
import com.autodonut.client.gui.AutoDonutConfigScreen;
import com.autodonut.client.miner.AutoMinerEngine;
import com.autodonut.client.miner.OreType;
import com.autodonut.client.safety.SafetyEngine;
import com.autodonut.config.AutoDonutConfig;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.regex.Pattern;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;

/**
 * Client-only entrypoint. Everything here only affects what YOU see locally
 * in chat - it never touches what you send, and never needs the server to
 * have anything special installed for it to work.
 */
public class AutoDonutClient implements ClientModInitializer {

	private static KeyMapping openSettingsKey;
	private static KeyMapping quickSellKey;

	@Override
	public void onInitializeClient() {
		openSettingsKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.autodonut.open_settings", InputConstants.Type.KEYSYM,
				InputConstants.KEY_K,
				net.minecraft.client.KeyMapping.Category.register(
						net.minecraft.resources.Identifier.fromNamespaceAndPath("autodonut", "general"))));

		quickSellKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.autodonut.quick_sell", InputConstants.Type.KEYSYM,
				InputConstants.UNKNOWN.getValue(),
				net.minecraft.client.KeyMapping.Category.register(
						net.minecraft.resources.Identifier.fromNamespaceAndPath("autodonut", "general"))));

		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			while (openSettingsKey.consumeClick()) {
				if (mc.gui.screen() == null) {
					AutoDonutConfigScreen.open();
				}
			}
			while (quickSellKey.consumeClick()) {
				QuickSellEngine.quickSell();
			}
			SafetyEngine.tick();
			AutoMinerEngine.tick();
			ClientSmelterEngine.tick();
			ClientFarmEngine.tick();
			ChestSellEngine.tick();
		});

		EconomyGuiHooks.register();
		// Player chat messages (real messages typed by real players).
		ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, type, receptionTimestamp) -> {
			AutoDonutConfig cfg = AutoDonutConfig.get();
			if (cfg.chatHidePlayerMessages) return false;
			if (cfg.chatHideScamKeywordMessages && containsScamKeyword(message, cfg)) return false;
			return true;
		});

		// Mention highlighting can't rewrite signed chat text, so instead we
		// echo an extra, clearly-marked line right after any message that
		// contains our own name.
		ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
			AutoDonutConfig cfg = AutoDonutConfig.get();
			if (!cfg.chatHighlightMentions) return;
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null) return;
			String myName = mc.player.getName().getString();
			if (myName.isBlank()) return;
			if (message.getString().toLowerCase().contains(myName.toLowerCase())) {
				mc.player.sendSystemMessage(Component.literal(">> you were mentioned above <<")
						.withStyle(net.minecraft.ChatFormatting.YELLOW, net.minecraft.ChatFormatting.BOLD));
			}
		});

		// System/game messages (server broadcasts, plugin messages, etc.) -
		// filtered for scam keywords and (optionally) join/leave spam, and
		// also fed to the Quick-Sell learner so it can pick up flat-sell
		// confirmation messages.
		ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
			AutoDonutConfig cfg = AutoDonutConfig.get();
			if (overlay) return true; // don't touch the action bar
			QuickSellEngine.onGameMessage(message);
			if (cfg.chatHideScamKeywordMessages && containsScamKeyword(message, cfg)) return false;
			if (cfg.chatHideJoinLeave && isJoinLeaveMessage(message.getString())) return false;
			return true;
		});

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> registerCommands(dispatcher));
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> registerMinerCommands(dispatcher));
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> registerEconomyCommands(dispatcher));

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> registerSmelterCommands(dispatcher));
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> registerFarmCommands(dispatcher));

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> registerAiCommands(dispatcher));

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
				dispatcher.register(ClientCommands.literal("autodonutmenu").executes(ctx -> {
					AutoDonutConfigScreen.open();
					return 1;
				})));
	}

	private static boolean isJoinLeaveMessage(String text) {
		return text.endsWith(" joined the game") || text.endsWith(" left the game");
	}

	private static boolean containsScamKeyword(Component message, AutoDonutConfig cfg) {
		List<String> keywords = cfg.chatScamKeywords;
		if (keywords == null || keywords.isEmpty()) return false;
		String text = message.getString().toLowerCase();
		for (String keyword : keywords) {
			if (keyword == null || keyword.isBlank()) continue;
			try {
				Pattern pattern = cfg.chatKeywordsAreRegex
						? Pattern.compile(keyword, Pattern.CASE_INSENSITIVE)
						: Pattern.compile("\\b" + Pattern.quote(keyword.toLowerCase()) + "\\b");
				if (pattern.matcher(text).find()) return true;
			} catch (java.util.regex.PatternSyntaxException ignored) {
				// Bad regex typed by the user - skip it rather than crash the chat pipeline.
			}
		}
		return false;
	}

	/**
	 * The optional AI advisor, as a client command.
	 *
	 * <p>This used to be a server command. It is client-side now like everything
	 * else: the request goes straight from your machine to whichever endpoint you
	 * configured, and the answer is printed only to you. Your API key never leaves
	 * {@code config/autodonut.json}.
	 */
	private static void registerAiCommands(com.mojang.brigadier.CommandDispatcher<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> dispatcher) {
		dispatcher.register(ClientCommands.literal("autodonut")

				.then(ClientCommands.literal("ai")

						.then(ClientCommands.literal("setkey")
								.then(ClientCommands.argument("key", com.mojang.brigadier.arguments.StringArgumentType.greedyString()).executes(ctx -> {
									String key = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "key");
									AutoDonutConfig cfg = AutoDonutConfig.get();
									cfg.aiApiKey = key;
									AutoDonutConfig.save();
									ctx.getSource().sendFeedback(Component.literal(
											"[AutoDonut AI] Key saved (" + key.length() + " chars). Stored locally in "
													+ "config/autodonut.json and only ever sent to " + cfg.aiBaseUrl + "."));
									return 1;
								})))

						.then(ClientCommands.literal("forgetkey").executes(ctx -> {
							AutoDonutConfig.get().aiApiKey = "";
							AutoDonutConfig.save();
							ctx.getSource().sendFeedback(Component.literal("[AutoDonut AI] Key deleted from config."));
							return 1;
						}))

						.then(ClientCommands.literal("model")
								.then(ClientCommands.argument("name", com.mojang.brigadier.arguments.StringArgumentType.string()).executes(ctx -> {
									AutoDonutConfig.get().aiModel = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "name");
									AutoDonutConfig.save();
									ctx.getSource().sendFeedback(Component.literal("[AutoDonut AI] Model set to " + AutoDonutConfig.get().aiModel));
									return 1;
								})))

						.then(ClientCommands.literal("baseurl")
								.then(ClientCommands.argument("url", com.mojang.brigadier.arguments.StringArgumentType.string()).executes(ctx -> {
									AutoDonutConfig.get().aiBaseUrl = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "url");
									AutoDonutConfig.save();
									ctx.getSource().sendFeedback(Component.literal("[AutoDonut AI] Endpoint set to " + AutoDonutConfig.get().aiBaseUrl));
									return 1;
								})))

						.then(ClientCommands.literal("ask")
								.then(ClientCommands.argument("question", com.mojang.brigadier.arguments.StringArgumentType.greedyString()).executes(ctx -> {
									AutoDonutConfig cfg = AutoDonutConfig.get();
									if (cfg.aiApiKey == null || cfg.aiApiKey.isBlank()) {
										ctx.getSource().sendError(Component.literal(
												"[AutoDonut AI] No API key set. Use /autodonut ai setkey <key> first."));
										return 0;
									}
									String question = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "question");
									Minecraft mc = Minecraft.getInstance();
									ctx.getSource().sendFeedback(Component.literal("[AutoDonut AI] Thinking..."));
									com.autodonut.ai.AiClient.ask(
											"You are AutoDonut's in-game assistant for a private, self-hosted Minecraft "
													+ "survival server with mining, farming, smelting and a player economy. "
													+ "Give short, practical, in-character tips. Keep replies under 4 sentences.",
											question
									).thenAccept(answer -> mc.execute(() -> {
										if (mc.player != null) {
											mc.player.sendSystemMessage(Component.literal("[AutoDonut AI] " + answer));
										}
									}));
									return 1;
								}))))

				.then(ClientCommands.literal("status").executes(ctx -> {
					AutoDonutConfig cfg = AutoDonutConfig.get();
					ctx.getSource().sendFeedback(Component.literal(String.format(
							"[AutoDonut] client-side status%n"
									+ "  Auto Miner:   %s%n"
									+ "  Auto Smelter: %s%n"
									+ "  Auto Farm:    %s%n"
									+ "  Safety:       auto-eat %s, threat-pause %s, anti-AFK %s%n"
									+ "  AI advisor:   %s",
							onOff(cfg.clientMinerEnabled), onOff(cfg.clientSmelterEnabled), onOff(cfg.clientFarmEnabled),
							onOff(cfg.safetyAutoEatEnabled), onOff(cfg.safetyThreatPauseEnabled), onOff(cfg.safetyAntiAfkEnabled),
							(cfg.aiApiKey != null && !cfg.aiApiKey.isBlank()) ? "key set" : "no key")));
					return 1;
				}))

				.executes(ctx -> {
					ctx.getSource().sendFeedback(Component.literal(
							"[AutoDonut] /autodonut status | /autodonut ai setkey|forgetkey|model|baseurl|ask"
									+ " - or press K for the settings screen."));
					return 1;
				}));
	}

	private static void registerCommands(com.mojang.brigadier.CommandDispatcher<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> dispatcher) {
		dispatcher.register(ClientCommands.literal("autodonutchat")

				.then(ClientCommands.literal("hideplayers")
						.then(ClientCommands.argument("on", bool()).executes(ctx -> {
							AutoDonutConfig.get().chatHidePlayerMessages = getBool(ctx, "on");
							AutoDonutConfig.save();
							ctx.getSource().sendFeedback(Component.literal("[AutoDonut Chat] Hide player chat: "
									+ onOff(AutoDonutConfig.get().chatHidePlayerMessages)));
							return 1;
						})))

				.then(ClientCommands.literal("hidescam")
						.then(ClientCommands.argument("on", bool()).executes(ctx -> {
							AutoDonutConfig.get().chatHideScamKeywordMessages = getBool(ctx, "on");
							AutoDonutConfig.save();
							ctx.getSource().sendFeedback(Component.literal("[AutoDonut Chat] Hide scam-keyword chat: "
									+ onOff(AutoDonutConfig.get().chatHideScamKeywordMessages)));
							return 1;
						})))

				.then(ClientCommands.literal("keyword")
						.then(ClientCommands.literal("add")
								.then(ClientCommands.argument("word", com.mojang.brigadier.arguments.StringArgumentType.word()).executes(ctx -> {
									String word = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "word").toLowerCase();
									AutoDonutConfig cfg = AutoDonutConfig.get();
									if (!cfg.chatScamKeywords.contains(word)) cfg.chatScamKeywords.add(word);
									AutoDonutConfig.save();
									ctx.getSource().sendFeedback(Component.literal("[AutoDonut Chat] Scam keywords: " + cfg.chatScamKeywords));
									return 1;
								})))
						.then(ClientCommands.literal("remove")
								.then(ClientCommands.argument("word", com.mojang.brigadier.arguments.StringArgumentType.word()).executes(ctx -> {
									String word = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "word").toLowerCase();
									AutoDonutConfig cfg = AutoDonutConfig.get();
									cfg.chatScamKeywords.remove(word);
									AutoDonutConfig.save();
									ctx.getSource().sendFeedback(Component.literal("[AutoDonut Chat] Scam keywords: " + cfg.chatScamKeywords));
									return 1;
								})))
						.executes(ctx -> {
							ctx.getSource().sendFeedback(Component.literal("[AutoDonut Chat] Scam keywords: " + AutoDonutConfig.get().chatScamKeywords));
							return 1;
						}))

				.executes(ctx -> {
					AutoDonutConfig cfg = AutoDonutConfig.get();
					ctx.getSource().sendFeedback(Component.literal(String.format(
							"[AutoDonut Chat] hidePlayers=%s hideScamKeywords=%s keywords=%s",
							onOff(cfg.chatHidePlayerMessages), onOff(cfg.chatHideScamKeywordMessages), cfg.chatScamKeywords)));
					return 1;
				})
		);
	}

	private static void registerSmelterCommands(com.mojang.brigadier.CommandDispatcher<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> dispatcher) {
		dispatcher.register(ClientCommands.literal("autodonutsmelt")

				.then(ClientCommands.argument("on", bool()).executes(ctx -> {
					AutoDonutConfig.get().clientSmelterEnabled = getBool(ctx, "on");
					AutoDonutConfig.save();
					ctx.getSource().sendFeedback(Component.literal("[AutoDonut Smelter] Enabled: "
							+ onOff(AutoDonutConfig.get().clientSmelterEnabled)));
					return 1;
				}))

				.then(ClientCommands.literal("interval")
						.then(ClientCommands.argument("seconds", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 60)).executes(ctx -> {
							AutoDonutConfig.get().smelterIntervalSeconds =
									com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, "seconds");
							AutoDonutConfig.save();
							ctx.getSource().sendFeedback(Component.literal("[AutoDonut Smelter] Checking every "
									+ AutoDonutConfig.get().smelterIntervalSeconds + "s."));
							return 1;
						})))

				.then(ClientCommands.literal("radius")
						.then(ClientCommands.argument("blocks", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 6)).executes(ctx -> {
							AutoDonutConfig.get().smelterSearchRadius =
									com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, "blocks");
							AutoDonutConfig.save();
							ctx.getSource().sendFeedback(Component.literal("[AutoDonut Smelter] Search radius: "
									+ AutoDonutConfig.get().smelterSearchRadius + " blocks (still clamped to your real reach)."));
							return 1;
						})))

				.then(ClientCommands.literal("fuel")
						.then(ClientCommands.literal("add")
								.then(ClientCommands.argument("item", com.mojang.brigadier.arguments.StringArgumentType.string()).executes(ctx -> {
									String item = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "item").trim();
									AutoDonutConfig cfg = AutoDonutConfig.get();
									if (!item.isEmpty() && !cfg.smelterFuelItems.contains(item)) cfg.smelterFuelItems.add(item);
									AutoDonutConfig.save();
									ctx.getSource().sendFeedback(Component.literal("[AutoDonut Smelter] Fuel: " + cfg.smelterFuelItems));
									return 1;
								})))
						.then(ClientCommands.literal("remove")
								.then(ClientCommands.argument("item", com.mojang.brigadier.arguments.StringArgumentType.string()).executes(ctx -> {
									AutoDonutConfig cfg = AutoDonutConfig.get();
									cfg.smelterFuelItems.remove(com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "item").trim());
									AutoDonutConfig.save();
									ctx.getSource().sendFeedback(Component.literal("[AutoDonut Smelter] Fuel: " + cfg.smelterFuelItems));
									return 1;
								})))
						.executes(ctx -> {
							ctx.getSource().sendFeedback(Component.literal("[AutoDonut Smelter] Fuel: "
									+ AutoDonutConfig.get().smelterFuelItems));
							return 1;
						}))

				.then(ClientCommands.literal("input")
						.then(ClientCommands.literal("add")
								.then(ClientCommands.argument("item", com.mojang.brigadier.arguments.StringArgumentType.string()).executes(ctx -> {
									String item = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "item").trim();
									AutoDonutConfig cfg = AutoDonutConfig.get();
									if (!item.isEmpty() && !cfg.smelterInputItems.contains(item)) cfg.smelterInputItems.add(item);
									AutoDonutConfig.save();
									ctx.getSource().sendFeedback(Component.literal("[AutoDonut Smelter] Inputs: " + cfg.smelterInputItems));
									return 1;
								})))
						.then(ClientCommands.literal("remove")
								.then(ClientCommands.argument("item", com.mojang.brigadier.arguments.StringArgumentType.string()).executes(ctx -> {
									AutoDonutConfig cfg = AutoDonutConfig.get();
									cfg.smelterInputItems.remove(com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "item").trim());
									AutoDonutConfig.save();
									ctx.getSource().sendFeedback(Component.literal("[AutoDonut Smelter] Inputs: " + cfg.smelterInputItems));
									return 1;
								})))
						.executes(ctx -> {
							ctx.getSource().sendFeedback(Component.literal("[AutoDonut Smelter] Inputs: "
									+ AutoDonutConfig.get().smelterInputItems));
							return 1;
						}))

				.then(ClientCommands.literal("stats")
						.then(ClientCommands.literal("reset").executes(ctx -> {
							ClientSmelterEngine.resetStats();
							ctx.getSource().sendFeedback(Component.literal("[AutoDonut Smelter] Session stats reset."));
							return 1;
						}))
						.executes(ctx -> {
							ClientSmelterEngine.announceStats();
							return 1;
						}))

				.executes(ctx -> {
					AutoDonutConfig cfg = AutoDonutConfig.get();
					ctx.getSource().sendFeedback(Component.literal(String.format(
							"[AutoDonut Smelter] enabled=%s interval=%ds radius=%d output=%s fuel=%s input=%s",
							onOff(cfg.clientSmelterEnabled), cfg.smelterIntervalSeconds, cfg.smelterSearchRadius,
							onOff(cfg.smelterTakeOutput), onOff(cfg.smelterInsertFuel), onOff(cfg.smelterInsertInput))));
					return 1;
				})
		);
	}

	private static void registerFarmCommands(com.mojang.brigadier.CommandDispatcher<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> dispatcher) {
		dispatcher.register(ClientCommands.literal("autodonutfarm")

				.then(ClientCommands.argument("on", bool()).executes(ctx -> {
					AutoDonutConfig.get().clientFarmEnabled = getBool(ctx, "on");
					AutoDonutConfig.save();
					ctx.getSource().sendFeedback(Component.literal("[AutoDonut Farm] Enabled: "
							+ onOff(AutoDonutConfig.get().clientFarmEnabled)));
					return 1;
				}))

				.then(ClientCommands.literal("radius")
						.then(ClientCommands.argument("blocks", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 6)).executes(ctx -> {
							AutoDonutConfig.get().farmRadius =
									com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, "blocks");
							AutoDonutConfig.save();
							ctx.getSource().sendFeedback(Component.literal("[AutoDonut Farm] Harvest radius: "
									+ AutoDonutConfig.get().farmRadius + " blocks (still clamped to your real reach)."));
							return 1;
						})))

				.then(ClientCommands.literal("replant")
						.then(ClientCommands.argument("on", bool()).executes(ctx -> {
							AutoDonutConfig.get().farmReplant = getBool(ctx, "on");
							AutoDonutConfig.save();
							ctx.getSource().sendFeedback(Component.literal("[AutoDonut Farm] Replant: "
									+ onOff(AutoDonutConfig.get().farmReplant)));
							return 1;
						})))

				.then(ClientCommands.literal("netherwart")
						.then(ClientCommands.argument("on", bool()).executes(ctx -> {
							AutoDonutConfig.get().farmIncludeNetherWart = getBool(ctx, "on");
							AutoDonutConfig.save();
							ctx.getSource().sendFeedback(Component.literal("[AutoDonut Farm] Harvest nether wart: "
									+ onOff(AutoDonutConfig.get().farmIncludeNetherWart)));
							return 1;
						})))

				.then(ClientCommands.literal("speed")
						.then(ClientCommands.argument("ticks", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 20)).executes(ctx -> {
							AutoDonutConfig.get().farmActionIntervalTicks =
									com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, "ticks");
							AutoDonutConfig.save();
							ctx.getSource().sendFeedback(Component.literal("[AutoDonut Farm] Delay between actions: "
									+ AutoDonutConfig.get().farmActionIntervalTicks + " ticks."));
							return 1;
						})))

				.then(ClientCommands.literal("stats")
						.then(ClientCommands.literal("reset").executes(ctx -> {
							ClientFarmEngine.resetStats();
							ctx.getSource().sendFeedback(Component.literal("[AutoDonut Farm] Session stats reset."));
							return 1;
						}))
						.executes(ctx -> {
							ClientFarmEngine.announceStats();
							return 1;
						}))

				.executes(ctx -> {
					AutoDonutConfig cfg = AutoDonutConfig.get();
					ctx.getSource().sendFeedback(Component.literal(String.format(
							"[AutoDonut Farm] enabled=%s radius=%d replant=%s netherWart=%s delay=%dt",
							onOff(cfg.clientFarmEnabled), cfg.farmRadius, onOff(cfg.farmReplant),
							onOff(cfg.farmIncludeNetherWart), cfg.farmActionIntervalTicks)));
					return 1;
				})
		);
	}

	private static void registerMinerCommands(com.mojang.brigadier.CommandDispatcher<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> dispatcher) {
		dispatcher.register(ClientCommands.literal("autodonutmine")

				.then(ClientCommands.argument("on", bool()).executes(ctx -> {
					AutoDonutConfig.get().clientMinerEnabled = getBool(ctx, "on");
					AutoDonutConfig.save();
					ctx.getSource().sendFeedback(Component.literal("[AutoDonut Miner] Enabled: "
							+ onOff(AutoDonutConfig.get().clientMinerEnabled)));
					return 1;
				}))

				.then(ClientCommands.literal("mode")
						.then(ClientCommands.literal("mineonly").executes(ctx -> {
							AutoDonutConfig.get().minerWalkEnabled = false;
							AutoDonutConfig.save();
							ctx.getSource().sendFeedback(Component.literal("[AutoDonut Miner] Mode: Only Mine (no movement)"));
							return 1;
						}))
						.then(ClientCommands.literal("walkmine").executes(ctx -> {
							AutoDonutConfig.get().minerWalkEnabled = true;
							AutoDonutConfig.save();
							ctx.getSource().sendFeedback(Component.literal(
									"[AutoDonut Miner] Mode: Walk + Mine (will move your character - don't touch WASD/mouse)"));
							return 1;
						})))

				.then(ClientCommands.literal("radar")
						.then(ClientCommands.argument("on", bool()).executes(ctx -> {
							AutoDonutConfig.get().minerOreSeekingEnabled = getBool(ctx, "on");
							AutoDonutConfig.save();
							ctx.getSource().sendFeedback(Component.literal("[AutoDonut Miner] Ore radar: "
									+ onOff(AutoDonutConfig.get().minerOreSeekingEnabled)));
							return 1;
						})))

				.then(ClientCommands.literal("radius")
						.then(ClientCommands.argument("blocks", com.mojang.brigadier.arguments.IntegerArgumentType.integer(2, 32)).executes(ctx -> {
							AutoDonutConfig.get().minerOreRadarRadius = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, "blocks");
							AutoDonutConfig.save();
							ctx.getSource().sendFeedback(Component.literal("[AutoDonut Miner] Radar radius: "
									+ AutoDonutConfig.get().minerOreRadarRadius + " blocks"));
							return 1;
						})))

				.then(ClientCommands.literal("stats")
						.then(ClientCommands.literal("reset").executes(ctx -> {
							AutoMinerEngine.resetStats();
							ctx.getSource().sendFeedback(Component.literal("[AutoDonut Miner] Session stats reset."));
							return 1;
						}))
						.executes(ctx -> {
							AutoMinerEngine.announceStats();
							return 1;
						}))

				.then(ClientCommands.literal("ore")
						.then(ClientCommands.literal("list").executes(ctx -> {
							AutoDonutConfig cfg = AutoDonutConfig.get();
							StringBuilder sb = new StringBuilder("[AutoDonut Miner] Ores: ");
							for (OreType ore : OreType.values()) {
								sb.append(ore.key).append("=").append(ore.isEnabled(cfg) ? "ON" : "OFF").append(" ");
							}
							ctx.getSource().sendFeedback(Component.literal(sb.toString().trim()));
							return 1;
						}))
						.then(ClientCommands.argument("ore", com.mojang.brigadier.arguments.StringArgumentType.word())
								.then(ClientCommands.argument("on", bool()).executes(ctx -> {
									String key = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "ore");
									OreType ore = OreType.byKey(key);
									if (ore == null) {
										ctx.getSource().sendFeedback(Component.literal("[AutoDonut Miner] Unknown ore: " + key));
										return 0;
									}
									AutoDonutConfig cfg = AutoDonutConfig.get();
									boolean on = getBool(ctx, "on");
									if (on) cfg.minerDisabledOres.remove(ore.key);
									else if (!cfg.minerDisabledOres.contains(ore.key)) cfg.minerDisabledOres.add(ore.key);
									AutoDonutConfig.save();
									ctx.getSource().sendFeedback(Component.literal(
											"[AutoDonut Miner] " + ore.displayName + ": " + onOff(on)));
									return 1;
								}))))

				.executes(ctx -> {
					AutoDonutConfig cfg = AutoDonutConfig.get();
					ctx.getSource().sendFeedback(Component.literal(String.format(
							"[AutoDonut Miner] enabled=%s mode=%s radar=%s radius=%d (open the GUI with the keybind or /autodonutmenu for the full ore list)",
							onOff(cfg.clientMinerEnabled),
							cfg.minerWalkEnabled ? "Walk+Mine" : "MineOnly",
							onOff(cfg.minerOreSeekingEnabled), cfg.minerOreRadarRadius)));
					return 1;
				})
		);
	}

	private static void registerEconomyCommands(com.mojang.brigadier.CommandDispatcher<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> dispatcher) {
		dispatcher.register(ClientCommands.literal("autodonuteconomy")

				.then(ClientCommands.argument("on", bool()).executes(ctx -> {
					AutoDonutConfig.get().economyEnabled = getBool(ctx, "on");
					AutoDonutConfig.save();
					ctx.getSource().sendFeedback(Component.literal("[AutoDonut Economy] Enabled: "
							+ onOff(AutoDonutConfig.get().economyEnabled)));
					return 1;
				}))

				.then(ClientCommands.literal("dumplore").executes(ctx -> {
					Minecraft mc = Minecraft.getInstance();
					if (mc.player == null || mc.player.containerMenu == null
							|| mc.player.containerMenu instanceof net.minecraft.world.inventory.InventoryMenu) {
						ctx.getSource().sendFeedback(Component.literal(
								"[AutoDonut Economy] Open a chest/AH GUI first, then run this while it's open."));
						return 0;
					}
					AhScanner.debugDumpLore(mc.player.containerMenu);
					return 1;
				}))

				.then(ClientCommands.literal("clearmarket").executes(ctx -> {
					MarketDatabase.clear();
					ctx.getSource().sendFeedback(Component.literal("[AutoDonut Economy] Market price history cleared."));
					return 1;
				}))

				.then(ClientCommands.literal("ledger")
						.then(ClientCommands.literal("clear").executes(ctx -> {
							ProfitLedger.clear();
							ctx.getSource().sendFeedback(Component.literal("[AutoDonut Economy] Ledger cleared."));
							return 1;
						}))
						.executes(ctx -> {
							ProfitLedger.announceSummary();
							return 1;
						}))

				.then(ClientCommands.literal("watch")
						.then(ClientCommands.literal("add")
								.then(ClientCommands.argument("item", com.mojang.brigadier.arguments.StringArgumentType.word())
										.then(ClientCommands.argument("maxPrice", com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg(0))
												.executes(ctx -> {
													String item = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "item");
													double maxPrice = com.mojang.brigadier.arguments.DoubleArgumentType.getDouble(ctx, "maxPrice");
													AutoDonutConfig.get().economyWatchlist.put(item, maxPrice);
													AutoDonutConfig.save();
													ctx.getSource().sendFeedback(Component.literal(
															"[AutoDonut Economy] Watching " + item + " for <= " + maxPrice + "/unit."));
													return 1;
												}))))
						.then(ClientCommands.literal("remove")
								.then(ClientCommands.argument("item", com.mojang.brigadier.arguments.StringArgumentType.word()).executes(ctx -> {
									String item = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "item");
									AutoDonutConfig.get().economyWatchlist.remove(item);
									AutoDonutConfig.save();
									ctx.getSource().sendFeedback(Component.literal("[AutoDonut Economy] No longer watching " + item + "."));
									return 1;
								})))
						.executes(ctx -> {
							ctx.getSource().sendFeedback(Component.literal(
									"[AutoDonut Economy] Watchlist: " + AutoDonutConfig.get().economyWatchlist));
							return 1;
						}))

				.then(ClientCommands.literal("want")
						.then(ClientCommands.literal("add")
								.then(ClientCommands.argument("item", com.mojang.brigadier.arguments.StringArgumentType.word())
										.then(ClientCommands.argument("qty", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1))
												.executes(ctx -> {
													String item = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "item");
													int qty = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, "qty");
													AutoDonutConfig.get().economyWantList.merge(item, qty, Integer::sum);
													AutoDonutConfig.save();
													ctx.getSource().sendFeedback(Component.literal(
															"[AutoDonut Economy] Shopping list: " + item + " x"
																	+ AutoDonutConfig.get().economyWantList.get(item)));
													return 1;
												}))))
						.then(ClientCommands.literal("remove")
								.then(ClientCommands.argument("item", com.mojang.brigadier.arguments.StringArgumentType.word()).executes(ctx -> {
									String item = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "item");
									AutoDonutConfig.get().economyWantList.remove(item);
									AutoDonutConfig.save();
									ctx.getSource().sendFeedback(Component.literal("[AutoDonut Economy] Removed " + item + " from shopping list."));
									return 1;
								})))
						.executes(ctx -> {
							ctx.getSource().sendFeedback(Component.literal(
									"[AutoDonut Economy] Shopping list: " + AutoDonutConfig.get().economyWantList));
							return 1;
						}))

				.then(ClientCommands.literal("sharedb")
						.then(ClientCommands.literal("export")
								.then(ClientCommands.argument("filename", com.mojang.brigadier.arguments.StringArgumentType.word()).executes(ctx -> {
									String filename = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "filename");
									java.nio.file.Path target = net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().resolve(filename + ".json");
									boolean ok = MarketDatabase.exportTo(target);
									ctx.getSource().sendFeedback(Component.literal(ok
											? "[AutoDonut Economy] Exported market prices to config/" + filename + ".json"
											: "[AutoDonut Economy] Export failed - check the log."));
									return ok ? 1 : 0;
								})))
						.then(ClientCommands.literal("import")
								.then(ClientCommands.argument("filename", com.mojang.brigadier.arguments.StringArgumentType.word()).executes(ctx -> {
									String filename = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "filename");
									java.nio.file.Path source = net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().resolve(filename + ".json");
									boolean ok = MarketDatabase.importFrom(source);
									ctx.getSource().sendFeedback(Component.literal(ok
											? "[AutoDonut Economy] Imported market prices from config/" + filename + ".json"
											: "[AutoDonut Economy] Import failed - check the file exists and is valid."));
									return ok ? 1 : 0;
								}))))

				.then(ClientCommands.literal("dns")
						.then(ClientCommands.literal("add")
								.then(ClientCommands.argument("item", com.mojang.brigadier.arguments.StringArgumentType.word()).executes(ctx -> {
									String item = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "item");
									AutoDonutConfig cfg = AutoDonutConfig.get();
									if (!cfg.economyDoNotSellList.contains(item)) cfg.economyDoNotSellList.add(item);
									AutoDonutConfig.save();
									ctx.getSource().sendFeedback(Component.literal("[AutoDonut Economy] Do Not Sell: " + cfg.economyDoNotSellList));
									return 1;
								})))
						.then(ClientCommands.literal("remove")
								.then(ClientCommands.argument("item", com.mojang.brigadier.arguments.StringArgumentType.word()).executes(ctx -> {
									String item = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "item");
									AutoDonutConfig cfg = AutoDonutConfig.get();
									cfg.economyDoNotSellList.remove(item);
									AutoDonutConfig.save();
									ctx.getSource().sendFeedback(Component.literal("[AutoDonut Economy] Do Not Sell: " + cfg.economyDoNotSellList));
									return 1;
								})))
						.executes(ctx -> {
							ctx.getSource().sendFeedback(Component.literal(
									"[AutoDonut Economy] Do Not Sell: " + AutoDonutConfig.get().economyDoNotSellList));
							return 1;
						}))

				.executes(ctx -> {
					AutoDonutConfig cfg = AutoDonutConfig.get();
					ctx.getSource().sendFeedback(Component.literal(String.format(
							"[AutoDonut Economy] enabled=%s ahKeyword=\"%s\" sellCmd=\"%s\" searchCmd=\"%s\" "
									+ "(open the GUI with the keybind or /autodonutmenu for the full settings screen)",
							onOff(cfg.economyEnabled), cfg.ahScreenTitleKeyword, cfg.ahSellCommandTemplate, cfg.ahSearchCommandTemplate)));
					return 1;
				})
		);
	}

	private static String onOff(boolean b) {
		return b ? "ON" : "OFF";
	}
}
