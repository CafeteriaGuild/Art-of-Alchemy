package dev.cafeteria.artofalchemy.gui.screen;

import dev.cafeteria.artofalchemy.essentia.EssentiaContainer;
import dev.cafeteria.artofalchemy.essentia.EssentiaStack;
import dev.cafeteria.artofalchemy.gui.handler.HandlerSynthesizer;

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class ScreenSynthesizer extends CottonInventoryScreen<HandlerSynthesizer>
implements EssentiaScreen {

	public ScreenSynthesizer(HandlerSynthesizer container, PlayerEntity player) {
		super(container, player);
	}

	@Override
	public void updateEssentia(int essentiaId, EssentiaContainer essentia, BlockPos pos) {
		handler.updateEssentia(essentiaId, essentia, pos);
	}

	@Override
	public void updateEssentia(int essentiaId, EssentiaContainer essentia, EssentiaStack required, BlockPos pos) {
		handler.updateEssentia(essentiaId, essentia, required, pos);
	}

}
