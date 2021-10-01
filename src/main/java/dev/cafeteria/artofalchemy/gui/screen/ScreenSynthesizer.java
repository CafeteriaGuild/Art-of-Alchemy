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
public class ScreenSynthesizer extends CottonInventoryScreen<HandlerSynthesizer> implements EssentiaScreen {

	public ScreenSynthesizer(final HandlerSynthesizer container, final PlayerEntity player) {
		super(container, player);
	}

	@Override
	public void updateEssentia(final int essentiaId, final EssentiaContainer essentia, final BlockPos pos) {
		this.handler.updateEssentia(essentiaId, essentia, pos);
	}

	@Override
	public void updateEssentia(
		final int essentiaId, final EssentiaContainer essentia, final EssentiaStack required, final BlockPos pos
	) {
		this.handler.updateEssentia(essentiaId, essentia, required, pos);
	}

}
