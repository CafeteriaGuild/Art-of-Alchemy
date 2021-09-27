package dev.cafeteria.artofalchemy.blockentity;

import dev.cafeteria.artofalchemy.essentia.AoAEssentia;
import dev.cafeteria.artofalchemy.essentia.EssentiaContainer;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class BlockEntityAstroCentrifuge extends AbstractBlockEntityCentrifuge {
	public BlockEntityAstroCentrifuge(BlockPos pos, BlockState state) {
		super(AoABlockEntities.ASTRO_CENTRIFUGE, pos, state);
		this.outputs = new EssentiaContainer[]{
				outputOf(AoAEssentia.MERCURY, AoAEssentia.VENUS, AoAEssentia.TELLUS, AoAEssentia.MARS),
				outputOf(AoAEssentia.JUPITER, AoAEssentia.SATURN, AoAEssentia.URANUS, AoAEssentia.NEPTUNE),
				outputOf(AoAEssentia.APOLLO, AoAEssentia.DIANA, AoAEssentia.CERES, AoAEssentia.PLUTO),
				outputOf(AoAEssentia.VOID)
		};
	}
}
