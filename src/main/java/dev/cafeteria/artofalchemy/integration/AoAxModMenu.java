package dev.cafeteria.artofalchemy.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import dev.cafeteria.artofalchemy.AoAConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;

@Environment(EnvType.CLIENT)
public class AoAxModMenu implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return new ConfigScreenFactory<Screen>() {
			@Override
			public Screen create(final Screen screen) {
				return AutoConfig.getConfigScreen(AoAConfig.class, screen).get();
			}
		};
	}
}
