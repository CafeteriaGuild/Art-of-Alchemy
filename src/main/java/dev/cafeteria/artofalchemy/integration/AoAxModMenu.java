package dev.cafeteria.artofalchemy.integration;

import dev.cafeteria.artofalchemy.AoAConfig;

import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
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
