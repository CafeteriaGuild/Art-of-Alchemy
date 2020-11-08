package com.cumulusmc.artofalchemy.gui.screen;

import com.cumulusmc.artofalchemy.essentia.EssentiaContainer;
import com.cumulusmc.artofalchemy.essentia.EssentiaStack;
import net.minecraft.util.math.BlockPos;

public interface EssentiaScreen {

	void updateEssentia(int essentiaId, EssentiaContainer container, BlockPos pos);

	default void updateEssentia(int essentiaId, EssentiaContainer container, EssentiaStack required, BlockPos pos) {
		updateEssentia(essentiaId, container, pos);
	}

}
