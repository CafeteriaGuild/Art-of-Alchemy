package dev.cafeteria.artofalchemy;

import dev.cafeteria.artofalchemy.gui.screen.AoAScreens;
import dev.cafeteria.artofalchemy.network.AoAClientNetworking;
import dev.cafeteria.artofalchemy.render.AoARenderers;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ArtOfAlchemyClient implements ClientModInitializer {

	@Environment(EnvType.CLIENT)
	@Override
	public void onInitializeClient() {
		AoAScreens.registerScreens();
		AoARenderers.registerRenderers();
		AoAClientNetworking.initializeClientNetworking();
	}

}
