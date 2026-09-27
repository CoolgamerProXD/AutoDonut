package com.autodonut.client.gui;

import com.autodonut.config.AutoDonutConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Toggles for the server-side automation blocks (these DO need the mod on the server too). */
public class AutoDonutAutomationScreen extends Screen {

	private final Screen parent;

	public AutoDonutAutomationScreen(Screen parent) {
		super(Component.literal("AutoDonut - AI Advisor & legacy server blocks"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		AutoDonutConfig cfg = AutoDonutConfig.get();
		int cx = this.width / 2;
		int top = this.height / 2 - 110;

		this.addRenderableWidget(new StringWidget(cx - 100, top - 20, 200, 20, this.title, this.font));

		MultiLineTextWidget warn = new MultiLineTextWidget(cx - 140, top, Component.literal(
				"LEGACY. These toggles only do something if the AutoDonut mod is also installed " +
				"on the SERVER, because they add real blocks. You do not need them: every feature " +
				"now has a 100% client-side version on the main settings screen. Leave these off " +
				"unless you run a modded server on purpose."), this.font);
		warn.setMaxWidth(280);
		this.addRenderableWidget(warn);

		int y = top + 45;
		this.addRenderableWidget(Checkbox.builder(Component.literal("Automation master switch"), this.font)
				.pos(cx - 130, y).selected(cfg.automateEnabled)
				.onValueChange((cb, v) -> { cfg.automateEnabled = v; AutoDonutConfig.save(); })
				.build());

		y += 22;
		this.addRenderableWidget(Checkbox.builder(Component.literal("Auto Smelter block"), this.font)
				.pos(cx - 130, y).selected(cfg.autoSmelterEnabled)
				.onValueChange((cb, v) -> { cfg.autoSmelterEnabled = v; AutoDonutConfig.save(); })
				.build());

		y += 22;
		this.addRenderableWidget(Checkbox.builder(Component.literal("Auto Farm block"), this.font)
				.pos(cx - 130, y).selected(cfg.autoFarmEnabled)
				.onValueChange((cb, v) -> { cfg.autoFarmEnabled = v; AutoDonutConfig.save(); })
				.build());

		y += 22;
		this.addRenderableWidget(Checkbox.builder(Component.literal("Auto Miner/Quarry block"), this.font)
				.pos(cx - 130, y).selected(cfg.autoMinerEnabled)
				.onValueChange((cb, v) -> { cfg.autoMinerEnabled = v; AutoDonutConfig.save(); })
				.build());

		y += 22;
		this.addRenderableWidget(Checkbox.builder(Component.literal("AI auto-controller"), this.font)
				.pos(cx - 130, y).selected(cfg.aiAutoControllerEnabled)
				.onValueChange((cb, v) -> { cfg.aiAutoControllerEnabled = v; AutoDonutConfig.save(); })
				.build());

		y += 26;
		this.addRenderableWidget(new StringWidget(cx - 130, y, 280, 20,
				Component.literal("AI API key: set with /autodonut ai setkey <key>"), this.font));

		this.addRenderableWidget(Button.builder(Component.literal("Back"), b -> onClose())
				.bounds(cx - 50, y + 30, 100, 20).build());
	}

	@Override
	public void onClose() {
		AutoDonutConfig.save();
		this.minecraft.gui.setScreen(parent);
	}
}
