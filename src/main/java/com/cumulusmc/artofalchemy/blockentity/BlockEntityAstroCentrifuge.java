package com.cumulusmc.artofalchemy.blockentity;

import com.cumulusmc.artofalchemy.essentia.AoAEssentia;
import com.cumulusmc.artofalchemy.essentia.EssentiaContainer;

public class BlockEntityAstroCentrifuge extends AbstractBlockEntityCentrifuge {
	public BlockEntityAstroCentrifuge() {
		super(AoABlockEntities.ASTRO_CENTRIFUGE);
		outputs = new EssentiaContainer[]{
				outputOf(AoAEssentia.MERCURY, AoAEssentia.VENUS, AoAEssentia.TELLUS, AoAEssentia.MARS),
				outputOf(AoAEssentia.JUPITER, AoAEssentia.SATURN, AoAEssentia.URANUS, AoAEssentia.NEPTUNE),
				outputOf(AoAEssentia.APOLLO, AoAEssentia.DIANA, AoAEssentia.CERES, AoAEssentia.PLUTO),
				outputOf(AoAEssentia.VOID)
		};
	}
}
