package com.cumulusmc.artofalchemy.blockentity;

import com.cumulusmc.artofalchemy.essentia.AoAEssentia;
import com.cumulusmc.artofalchemy.essentia.EssentiaContainer;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class BlockEntityElementCentrifuge extends AbstractBlockEntityCentrifuge {
	public BlockEntityElementCentrifuge(BlockPos pos, BlockState state) {
		super(AoABlockEntities.ELEMENT_CENTRIFUGE, pos, state);
		outputs = new EssentiaContainer[]{
				outputOf(AoAEssentia.MARS, AoAEssentia.JUPITER, AoAEssentia.APOLLO),
				outputOf(AoAEssentia.VENUS, AoAEssentia.URANUS, AoAEssentia.PLUTO),
				outputOf(AoAEssentia.MERCURY, AoAEssentia.NEPTUNE, AoAEssentia.DIANA),
				outputOf(AoAEssentia.TELLUS, AoAEssentia.SATURN, AoAEssentia.CERES)
		};
	}
}
