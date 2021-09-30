package dev.cafeteria.artofalchemy.integration;

import dev.cafeteria.artofalchemy.AoAConfig;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class AoAxModMenu implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return (screen) -> AutoConfig.getConfigScreen(AoAConfig.class, screen).get();
	}
}
