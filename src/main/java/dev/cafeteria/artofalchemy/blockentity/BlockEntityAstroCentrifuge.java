package dev.cafeteria.artofalchemy.blockentity;

import dev.cafeteria.artofalchemy.essentia.AoAEssentia;
import dev.cafeteria.artofalchemy.essentia.EssentiaContainer;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class BlockEntityAstroCentrifuge extends AbstractBlockEntityCentrifuge {
	public BlockEntityAstroCentrifuge(final BlockPos pos, final BlockState state) {
		super(AoABlockEntities.ASTRO_CENTRIFUGE, pos, state);
		this.outputs = new EssentiaContainer[] {
			AbstractBlockEntityCentrifuge
				.outputOf(AoAEssentia.MERCURY, AoAEssentia.VENUS, AoAEssentia.TELLUS, AoAEssentia.MARS),
			AbstractBlockEntityCentrifuge
				.outputOf(AoAEssentia.JUPITER, AoAEssentia.SATURN, AoAEssentia.URANUS, AoAEssentia.NEPTUNE),
			AbstractBlockEntityCentrifuge
				.outputOf(AoAEssentia.APOLLO, AoAEssentia.DIANA, AoAEssentia.CERES, AoAEssentia.PLUTO),
			AbstractBlockEntityCentrifuge.outputOf(AoAEssentia.VOID)
		};
	}
}
