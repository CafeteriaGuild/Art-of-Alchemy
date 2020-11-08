package com.cumulusmc.artofalchemy;

import com.cumulusmc.artofalchemy.gui.screen.AoAScreens;
import com.cumulusmc.artofalchemy.network.AoAClientNetworking;
import com.cumulusmc.artofalchemy.render.AoARenderers;
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
