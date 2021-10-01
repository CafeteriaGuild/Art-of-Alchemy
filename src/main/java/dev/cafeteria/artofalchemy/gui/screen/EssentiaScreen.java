package dev.cafeteria.artofalchemy.gui.screen;

import dev.cafeteria.artofalchemy.essentia.EssentiaContainer;
import dev.cafeteria.artofalchemy.essentia.EssentiaStack;
import net.minecraft.util.math.BlockPos;

public interface EssentiaScreen {

	void updateEssentia(int essentiaId, EssentiaContainer container, BlockPos pos);

	default void updateEssentia(
		final int essentiaId, final EssentiaContainer container, final EssentiaStack required, final BlockPos pos
	) {
		this.updateEssentia(essentiaId, container, pos);
	}

}
