package com.autodonut.client.gui;

import com.autodonut.config.AutoDonutConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Top-level AutoDonut settings hub. Reachable three ways:
 * - the AutoDonut entry in Mod Menu's mod list (if Mod Menu is installed)
 * - the default keybind (see AutoDonutClient), rebindable in Controls
 * - the "/autodonut config" client command
 */
public class AutoDonutConfigScreen extends Screen {

	private final Screen parent;

	public AutoDonutConfigScreen(Screen parent) {
		super(Component.literal("AutoDonut Settings"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int cx = this.width / 2;
		int top = this.height / 2 - 70;

		this.addRenderableWidget(new StringWidget(cx - 100, top - 40, 200, 20, this.title, this.font));
		this.addRenderableWidget(new StringWidget(cx - 140, top - 22, 280, 20,
				Component.literal("Every feature below is 100% client-side."), this.font));

		this.addRenderableWidget(Button.builder(Component.literal("Auto Miner (client-side)"), b ->
				this.minecraft.gui.setScreen(new AutoDonutMinerScreen(this)))
				.bounds(cx - 130, top, 260, 20).build());

		this.addRenderableWidget(Button.builder(Component.literal("Auto Smelter (client-side)"), b ->
				this.minecraft.gui.setScreen(new AutoDonutSmelterScreen(this)))
				.bounds(cx - 130, top + 25, 260, 20).build());

		this.addRenderableWidget(Button.builder(Component.literal("Auto Farm (client-side)"), b ->
				this.minecraft.gui.setScreen(new AutoDonutFarmScreen(this)))
				.bounds(cx - 130, top + 50, 260, 20).build());

		this.addRenderableWidget(Button.builder(Component.literal("Economy & Chest Sell (client-side)"), b ->
				this.minecraft.gui.setScreen(new AutoDonutEconomyScreen(this)))
				.bounds(cx - 130, top + 75, 260, 20).build());

		this.addRenderableWidget(Button.builder(Component.literal("Chat Modifiers (client-side)"), b ->
				this.minecraft.gui.setScreen(new AutoDonutChatScreen(this)))
				.bounds(cx - 130, top + 100, 260, 20).build());

		this.addRenderableWidget(Button.builder(Component.literal("Safety (auto-eat / threat pause / anti-AFK)"), b ->
				this.minecraft.gui.setScreen(new AutoDonutSafetyScreen(this)))
				.bounds(cx - 130, top + 125, 260, 20).build());

		this.addRenderableWidget(Button.builder(Component.literal("AI Advisor & legacy server blocks"), b ->
				this.minecraft.gui.setScreen(new AutoDonutAutomationScreen(this)))
				.bounds(cx - 130, top + 150, 260, 20).build());

		MultiLineTextWidget note = new MultiLineTextWidget(cx - 140, top + 178, Component.literal(
				"Note: Resource/texture packs (e.g. ore highlighters) only change what things look like. " +
				"AutoDonut reads real block data, so it works fine alongside any pack you have loaded."), this.font);
		note.setMaxWidth(280);
		this.addRenderableWidget(note);

		this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
				.bounds(cx - 50, top + 228, 100, 20).build());
	}

	@Override
	public void onClose() {
		AutoDonutConfig.save();
		this.minecraft.gui.setScreen(parent);
	}

	public static void open() {
		Minecraft mc = Minecraft.getInstance();
		mc.gui.setScreen(new AutoDonutConfigScreen(mc.gui.screen()));
	}
}
