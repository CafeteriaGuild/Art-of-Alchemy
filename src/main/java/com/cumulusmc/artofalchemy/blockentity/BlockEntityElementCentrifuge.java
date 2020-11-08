package com.cumulusmc.artofalchemy.blockentity;

import com.cumulusmc.artofalchemy.essentia.AoAEssentia;
import com.cumulusmc.artofalchemy.essentia.EssentiaContainer;

public class BlockEntityElementCentrifuge extends AbstractBlockEntityCentrifuge {
	public BlockEntityElementCentrifuge() {
		super(AoABlockEntities.ELEMENT_CENTRIFUGE);
		outputs = new EssentiaContainer[]{
				outputOf(AoAEssentia.MARS, AoAEssentia.JUPITER, AoAEssentia.APOLLO),
				outputOf(AoAEssentia.VENUS, AoAEssentia.URANUS, AoAEssentia.PLUTO),
				outputOf(AoAEssentia.MERCURY, AoAEssentia.NEPTUNE, AoAEssentia.DIANA),
				outputOf(AoAEssentia.TELLUS, AoAEssentia.SATURN, AoAEssentia.CERES)
		};
	}
}
