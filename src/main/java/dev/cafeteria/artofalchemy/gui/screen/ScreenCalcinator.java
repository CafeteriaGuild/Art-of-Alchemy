package dev.cafeteria.artofalchemy.gui.screen;

import dev.cafeteria.artofalchemy.gui.handler.HandlerCalcinator;
import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;

@Environment(EnvType.CLIENT)
public class ScreenCalcinator extends CottonInventoryScreen<HandlerCalcinator> {

	public ScreenCalcinator(final HandlerCalcinator container, final PlayerEntity player) {
		super(container, player);
	}

}
