package com.autodonut.client.gui;

import com.autodonut.client.automation.ClientSmelterEngine;
import com.autodonut.config.AutoDonutConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Settings for the 100% client-side Auto Smelter, which tends ordinary
 * vanilla furnaces rather than adding a block of its own.
 */
public class AutoDonutSmelterScreen extends Screen {

	private final Screen parent;

	public AutoDonutSmelterScreen(Screen parent) {
		super(Component.literal("AutoDonut - Auto Smelter (client-side)"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		AutoDonutConfig cfg = AutoDonutConfig.get();
		int cx = this.width / 2;
		int left = cx - 150;
		int y = 14;

		this.addRenderableWidget(new StringWidget(left, y, 300, 16, this.title, this.font));
		y += 20;

		MultiLineTextWidget info = new MultiLineTextWidget(left, y, Component.literal(
				"Tends a normal vanilla Furnace / Blast Furnace / Smoker that is already in your world. "
						+ "Stand next to it: every few seconds AutoDonut opens it, takes the finished output, "
						+ "tops up fuel and input from your inventory, and closes it again. "
						+ "Nothing is installed on the server."), this.font);
		info.setMaxWidth(300);
		this.addRenderableWidget(info);
		y += 46;

		this.addRenderableWidget(Checkbox.builder(Component.literal("Enable Auto Smelter"), this.font)
				.pos(left, y).selected(cfg.clientSmelterEnabled)
				.onValueChange((cb, v) -> { cfg.clientSmelterEnabled = v; AutoDonutConfig.save(); })
				.build());
		y += 24;

		this.addRenderableWidget(Checkbox.builder(Component.literal("Take finished output"), this.font)
				.pos(left, y).selected(cfg.smelterTakeOutput)
				.onValueChange((cb, v) -> { cfg.smelterTakeOutput = v; AutoDonutConfig.save(); })
				.build());
		y += 22;

		this.addRenderableWidget(Checkbox.builder(Component.literal("Top up fuel"), this.font)
				.pos(left, y).selected(cfg.smelterInsertFuel)
				.onValueChange((cb, v) -> { cfg.smelterInsertFuel = v; AutoDonutConfig.save(); })
				.build());
		y += 22;

		this.addRenderableWidget(Checkbox.builder(Component.literal("Feed in new items to smelt"), this.font)
				.pos(left, y).selected(cfg.smelterInsertInput)
				.onValueChange((cb, v) -> { cfg.smelterInsertInput = v; AutoDonutConfig.save(); })
				.build());
		y += 26;

		StringWidget intervalLabel = new StringWidget(left, y + 4, 200, 12,
				Component.literal("Check every: " + cfg.smelterIntervalSeconds + "s"), this.font);
		this.addRenderableWidget(intervalLabel);
		this.addRenderableWidget(Button.builder(Component.literal("-"), b -> {
			cfg.smelterIntervalSeconds = Math.max(1, cfg.smelterIntervalSeconds - 1);
			AutoDonutConfig.save();
			intervalLabel.setMessage(Component.literal("Check every: " + cfg.smelterIntervalSeconds + "s"));
		}).bounds(left + 205, y, 20, 20).build());
		this.addRenderableWidget(Button.builder(Component.literal("+"), b -> {
			cfg.smelterIntervalSeconds = Math.min(60, cfg.smelterIntervalSeconds + 1);
			AutoDonutConfig.save();
			intervalLabel.setMessage(Component.literal("Check every: " + cfg.smelterIntervalSeconds + "s"));
		}).bounds(left + 230, y, 20, 20).build());
		y += 26;

		StringWidget radiusLabel = new StringWidget(left, y + 4, 200, 12,
				Component.literal("Search radius: " + cfg.smelterSearchRadius + " blocks"), this.font);
		this.addRenderableWidget(radiusLabel);
		this.addRenderableWidget(Button.builder(Component.literal("-"), b -> {
			cfg.smelterSearchRadius = Math.max(1, cfg.smelterSearchRadius - 1);
			AutoDonutConfig.save();
			radiusLabel.setMessage(Component.literal("Search radius: " + cfg.smelterSearchRadius + " blocks"));
		}).bounds(left + 205, y, 20, 20).build());
		this.addRenderableWidget(Button.builder(Component.literal("+"), b -> {
			cfg.smelterSearchRadius = Math.min(6, cfg.smelterSearchRadius + 1);
			AutoDonutConfig.save();
			radiusLabel.setMessage(Component.literal("Search radius: " + cfg.smelterSearchRadius + " blocks"));
		}).bounds(left + 230, y, 20, 20).build());
		y += 28;

		this.addRenderableWidget(new StringWidget(left, y, 300, 12,
				Component.literal("Fuel items (comma-separated):"), this.font));
		y += 14;
		EditBox fuelBox = new EditBox(this.font, left, y, 300, 16, Component.literal("fuel items"));
		fuelBox.setMaxLength(500);
		fuelBox.setValue(String.join(", ", cfg.smelterFuelItems));
		fuelBox.setResponder(v -> {
			cfg.smelterFuelItems.clear();
			for (String part : v.split(",")) {
				String trimmed = part.trim();
				if (!trimmed.isEmpty()) cfg.smelterFuelItems.add(trimmed);
			}
			AutoDonutConfig.save();
		});
		this.addRenderableWidget(fuelBox);
		y += 22;

		this.addRenderableWidget(new StringWidget(left, y, 300, 12,
				Component.literal("Allowed input items (comma-separated):"), this.font));
		y += 14;
		EditBox inputBox = new EditBox(this.font, left, y, 300, 16, Component.literal("input items"));
		inputBox.setMaxLength(1000);
		inputBox.setValue(String.join(", ", cfg.smelterInputItems));
		inputBox.setResponder(v -> {
			cfg.smelterInputItems.clear();
			for (String part : v.split(",")) {
				String trimmed = part.trim();
				if (!trimmed.isEmpty()) cfg.smelterInputItems.add(trimmed);
			}
			AutoDonutConfig.save();
		});
		this.addRenderableWidget(inputBox);
		y += 24;

		MultiLineTextWidget note = new MultiLineTextWidget(left, y, Component.literal(
				"The input list is an allow-list on purpose, so nothing valuable can be fed into a furnace "
						+ "by accident. Bare names like \"coal\" work as well as \"minecraft:coal\"."), this.font);
		note.setMaxWidth(300);
		this.addRenderableWidget(note);
		y += 34;

		this.addRenderableWidget(Button.builder(Component.literal("Session stats"), b ->
				ClientSmelterEngine.announceStats()).bounds(left, y, 145, 20).build());
		this.addRenderableWidget(Button.builder(Component.literal("Reset stats"), b ->
				ClientSmelterEngine.resetStats()).bounds(left + 155, y, 145, 20).build());
		y += 26;

		this.addRenderableWidget(Button.builder(Component.literal("Back"), b -> onClose())
				.bounds(cx - 50, y, 100, 20).build());
	}

	@Override
	public void onClose() {
		AutoDonutConfig.save();
		this.minecraft.gui.setScreen(parent);
	}
}
