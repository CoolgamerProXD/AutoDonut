package com.autodonut.client.gui;

import com.autodonut.config.AutoDonutConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Settings for the optional AI advisor.
 *
 * <p>This is entirely opt-in. With no API key set, AutoDonut never makes a
 * network request. When you do use it, your question goes to whichever
 * OpenAI-compatible endpoint you configured and nowhere else - the key lives
 * only in {@code config/autodonut.json} on your own disk.
 */
public class AutoDonutAiScreen extends Screen {

	private final Screen parent;

	public AutoDonutAiScreen(Screen parent) {
		super(Component.literal("AutoDonut - AI Advisor"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		AutoDonutConfig cfg = AutoDonutConfig.get();
		int cx = this.width / 2;
		int top = this.height / 2 - 100;

		this.addRenderableWidget(new StringWidget(cx - 100, top - 22, 200, 20, this.title, this.font));

		MultiLineTextWidget blurb = new MultiLineTextWidget(cx - 150, top, Component.literal(
				"Optional. Ask for build/economy advice in chat with /autodonut ai ask <question>. "
						+ "Nothing is sent anywhere until you set a key, and then only to the endpoint "
						+ "below. Your key is stored locally in config/autodonut.json."), this.font);
		blurb.setMaxWidth(300);
		this.addRenderableWidget(blurb);

		int y = top + 52;
		int labelWidth = 70;
		int boxWidth = 210;

		this.addRenderableWidget(new StringWidget(cx - 150, y, labelWidth, 16,
				Component.literal("Endpoint"), this.font));
		EditBox baseUrl = new EditBox(this.font, cx - 150 + labelWidth, y, boxWidth, 16,
				Component.literal("ai base url"));
		baseUrl.setMaxLength(512);
		baseUrl.setValue(cfg.aiBaseUrl == null ? "" : cfg.aiBaseUrl);
		baseUrl.setResponder(v -> { cfg.aiBaseUrl = v; AutoDonutConfig.save(); });
		this.addRenderableWidget(baseUrl);

		y += 24;
		this.addRenderableWidget(new StringWidget(cx - 150, y, labelWidth, 16,
				Component.literal("Model"), this.font));
		EditBox model = new EditBox(this.font, cx - 150 + labelWidth, y, boxWidth, 16,
				Component.literal("ai model"));
		model.setMaxLength(128);
		model.setValue(cfg.aiModel == null ? "" : cfg.aiModel);
		model.setResponder(v -> { cfg.aiModel = v; AutoDonutConfig.save(); });
		this.addRenderableWidget(model);

		y += 30;
		boolean hasKey = cfg.aiApiKey != null && !cfg.aiApiKey.isBlank();
		this.addRenderableWidget(new StringWidget(cx - 150, y, 300, 16, Component.literal(
				hasKey ? "API key: set (" + cfg.aiApiKey.length() + " chars)" : "API key: not set"),
				this.font));

		y += 20;
		this.addRenderableWidget(new StringWidget(cx - 150, y, 300, 16, Component.literal(
				"Set it with /autodonut ai setkey <key>"), this.font));

		y += 26;
		this.addRenderableWidget(Button.builder(Component.literal("Forget my API key"), b -> {
			cfg.aiApiKey = "";
			AutoDonutConfig.save();
			this.rebuild();
		}).bounds(cx - 150, y, 145, 20).build());

		this.addRenderableWidget(Button.builder(Component.literal("Back"), b -> onClose())
				.bounds(cx + 5, y, 145, 20).build());
	}

	private void rebuild() {
		this.minecraft.gui.setScreen(new AutoDonutAiScreen(this.parent));
	}

	@Override
	public void onClose() {
		AutoDonutConfig.save();
		this.minecraft.gui.setScreen(parent);
	}
}
