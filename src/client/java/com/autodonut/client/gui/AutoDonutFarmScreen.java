package com.autodonut.client.gui;

import com.autodonut.client.automation.ClientFarmEngine;
import com.autodonut.config.AutoDonutConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Settings for the 100% client-side Auto Farm, which harvests and replants
 * ordinary vanilla crops around you rather than adding a block of its own.
 */
public class AutoDonutFarmScreen extends Screen {

	private final Screen parent;

	public AutoDonutFarmScreen(Screen parent) {
		super(Component.literal("AutoDonut - Auto Farm (client-side)"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		AutoDonutConfig cfg = AutoDonutConfig.get();
		int cx = this.width / 2;
		int left = cx - 150;
		int y = 18;

		this.addRenderableWidget(new StringWidget(left, y, 300, 16, this.title, this.font));
		y += 22;

		MultiLineTextWidget info = new MultiLineTextWidget(left, y, Component.literal(
				"Harvests fully-grown vanilla crops within your normal reach and replants them. "
						+ "Stand in your field (or walk around it) and it keeps the field cleared. "
						+ "Nothing is installed on the server."), this.font);
		info.setMaxWidth(300);
		this.addRenderableWidget(info);
		y += 40;

		this.addRenderableWidget(Checkbox.builder(Component.literal("Enable Auto Farm"), this.font)
				.pos(left, y).selected(cfg.clientFarmEnabled)
				.onValueChange((cb, v) -> { cfg.clientFarmEnabled = v; AutoDonutConfig.save(); })
				.build());
		y += 24;

		this.addRenderableWidget(Checkbox.builder(Component.literal("Replant after harvesting"), this.font)
				.pos(left, y).selected(cfg.farmReplant)
				.onValueChange((cb, v) -> { cfg.farmReplant = v; AutoDonutConfig.save(); })
				.build());
		y += 22;

		this.addRenderableWidget(Checkbox.builder(Component.literal("Also harvest nether wart"), this.font)
				.pos(left, y).selected(cfg.farmIncludeNetherWart)
				.onValueChange((cb, v) -> { cfg.farmIncludeNetherWart = v; AutoDonutConfig.save(); })
				.build());
		y += 26;

		StringWidget radiusLabel = new StringWidget(left, y + 4, 200, 12,
				Component.literal("Harvest radius: " + cfg.farmRadius + " blocks"), this.font);
		this.addRenderableWidget(radiusLabel);
		this.addRenderableWidget(Button.builder(Component.literal("-"), b -> {
			cfg.farmRadius = Math.max(1, cfg.farmRadius - 1);
			AutoDonutConfig.save();
			radiusLabel.setMessage(Component.literal("Harvest radius: " + cfg.farmRadius + " blocks"));
		}).bounds(left + 205, y, 20, 20).build());
		this.addRenderableWidget(Button.builder(Component.literal("+"), b -> {
			cfg.farmRadius = Math.min(6, cfg.farmRadius + 1);
			AutoDonutConfig.save();
			radiusLabel.setMessage(Component.literal("Harvest radius: " + cfg.farmRadius + " blocks"));
		}).bounds(left + 230, y, 20, 20).build());
		y += 26;

		StringWidget speedLabel = new StringWidget(left, y + 4, 200, 12,
				Component.literal("Delay between actions: " + cfg.farmActionIntervalTicks + " ticks"), this.font);
		this.addRenderableWidget(speedLabel);
		this.addRenderableWidget(Button.builder(Component.literal("-"), b -> {
			cfg.farmActionIntervalTicks = Math.max(1, cfg.farmActionIntervalTicks - 1);
			AutoDonutConfig.save();
			speedLabel.setMessage(Component.literal("Delay between actions: " + cfg.farmActionIntervalTicks + " ticks"));
		}).bounds(left + 205, y, 20, 20).build());
		this.addRenderableWidget(Button.builder(Component.literal("+"), b -> {
			cfg.farmActionIntervalTicks = Math.min(20, cfg.farmActionIntervalTicks + 1);
			AutoDonutConfig.save();
			speedLabel.setMessage(Component.literal("Delay between actions: " + cfg.farmActionIntervalTicks + " ticks"));
		}).bounds(left + 230, y, 20, 20).build());
		y += 28;

		boolean hasSeeds = ClientFarmEngine.playerHasAnySeed(Minecraft.getInstance().player);
		this.addRenderableWidget(new StringWidget(left, y, 300, 12, Component.literal(
				hasSeeds
						? "Seeds detected in your inventory - replanting will work."
						: "No seeds in your inventory - it will harvest but not replant."), this.font));
		y += 20;

		MultiLineTextWidget note = new MultiLineTextWidget(left, y, Component.literal(
				"Supports wheat, carrots, potatoes, beetroot and nether wart. Keep the matching seed in "
						+ "your hotbar for replanting. Radius is always clamped to your real reach - "
						+ "AutoDonut never extends how far you can interact."), this.font);
		note.setMaxWidth(300);
		this.addRenderableWidget(note);
		y += 44;

		this.addRenderableWidget(Button.builder(Component.literal("Session stats"), b ->
				ClientFarmEngine.announceStats()).bounds(left, y, 145, 20).build());
		this.addRenderableWidget(Button.builder(Component.literal("Reset stats"), b ->
				ClientFarmEngine.resetStats()).bounds(left + 155, y, 145, 20).build());
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
