package com.cumulusmc.artofalchemy.integration;

import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import com.cumulusmc.artofalchemy.AoAConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class AoAxModMenu implements ModMenuApi {

	@SuppressWarnings("deprecation")
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return (screen) -> AutoConfig.getConfigScreen(AoAConfig.class, screen).get();
	}
}
