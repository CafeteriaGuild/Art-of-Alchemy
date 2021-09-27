package dev.cafeteria.artofalchemy.gui.screen;

import dev.cafeteria.artofalchemy.essentia.EssentiaContainer;
import dev.cafeteria.artofalchemy.gui.handler.HandlerDistiller;

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class ScreenDistiller extends CottonInventoryScreen<HandlerDistiller>
implements EssentiaScreen {

	public ScreenDistiller(HandlerDistiller container, PlayerEntity player) {
		super(container, player);
	}

	@Override
	public void updateEssentia(int essentiaId, EssentiaContainer essentia, BlockPos pos) {
		handler.updateEssentia(essentiaId, essentia, pos);
	}

}
