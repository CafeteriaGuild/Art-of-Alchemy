package com.cumulusmc.artofalchemy.gui.screen;

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import com.cumulusmc.artofalchemy.gui.handler.HandlerCalcinator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;

@Environment(EnvType.CLIENT)
public class ScreenCalcinator extends CottonInventoryScreen<HandlerCalcinator> {

	public ScreenCalcinator(HandlerCalcinator container, PlayerEntity player) {
		super(container, player);
	}

}
