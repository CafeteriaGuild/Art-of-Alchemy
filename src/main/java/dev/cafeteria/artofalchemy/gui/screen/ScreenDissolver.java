package dev.cafeteria.artofalchemy.gui.screen;

import dev.cafeteria.artofalchemy.essentia.EssentiaContainer;
import dev.cafeteria.artofalchemy.gui.handler.HandlerDissolver;
import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class ScreenDissolver extends CottonInventoryScreen<HandlerDissolver> implements EssentiaScreen {

	public ScreenDissolver(final HandlerDissolver container, final PlayerEntity player) {
		super(container, player);
	}

	@Override
	public void updateEssentia(final int essentiaId, final EssentiaContainer essentia, final BlockPos pos) {
		this.handler.updateEssentia(essentiaId, essentia, pos);
	}

}
