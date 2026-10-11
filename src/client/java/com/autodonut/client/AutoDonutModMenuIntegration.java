package com.autodonut.client;

import com.autodonut.client.gui.AutoDonutConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Only ever loaded/used if Mod Menu is actually installed - if it's not, this
 * class is never touched by anything, so it has zero effect either way.
 */
public class AutoDonutModMenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return AutoDonutConfigScreen::new;
	}
}
