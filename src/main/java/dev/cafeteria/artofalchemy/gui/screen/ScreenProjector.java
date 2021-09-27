package dev.cafeteria.artofalchemy.gui.screen;

import dev.cafeteria.artofalchemy.gui.handler.HandlerProjector;

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;

@Environment(EnvType.CLIENT)
public class ScreenProjector extends CottonInventoryScreen<HandlerProjector> {

	public ScreenProjector(HandlerProjector container, PlayerEntity player) {
		super(container, player);
	}

}
