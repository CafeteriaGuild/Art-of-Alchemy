package dev.cafeteria.artofalchemy.blockentity;

import dev.cafeteria.artofalchemy.essentia.AoAEssentia;
import dev.cafeteria.artofalchemy.essentia.EssentiaContainer;

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
