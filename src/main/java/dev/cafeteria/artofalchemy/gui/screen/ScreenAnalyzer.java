package dev.cafeteria.artofalchemy.gui.screen;

import dev.cafeteria.artofalchemy.gui.handler.HandlerAnalyzer;

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;

@Environment(EnvType.CLIENT)
public class ScreenAnalyzer extends CottonInventoryScreen<HandlerAnalyzer> {

	public ScreenAnalyzer(HandlerAnalyzer container, PlayerEntity player) {
		super(container, player);
	}

}
