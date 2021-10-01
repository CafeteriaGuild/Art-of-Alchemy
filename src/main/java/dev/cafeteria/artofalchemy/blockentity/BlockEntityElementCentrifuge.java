package dev.cafeteria.artofalchemy.blockentity;

import dev.cafeteria.artofalchemy.essentia.AoAEssentia;
import dev.cafeteria.artofalchemy.essentia.EssentiaContainer;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class BlockEntityElementCentrifuge extends AbstractBlockEntityCentrifuge {
	public BlockEntityElementCentrifuge(final BlockPos pos, final BlockState state) {
		super(AoABlockEntities.ELEMENT_CENTRIFUGE, pos, state);
		this.outputs = new EssentiaContainer[] {
			AbstractBlockEntityCentrifuge.outputOf(AoAEssentia.MARS, AoAEssentia.JUPITER, AoAEssentia.APOLLO),
			AbstractBlockEntityCentrifuge.outputOf(AoAEssentia.VENUS, AoAEssentia.URANUS, AoAEssentia.PLUTO),
			AbstractBlockEntityCentrifuge.outputOf(AoAEssentia.MERCURY, AoAEssentia.NEPTUNE, AoAEssentia.DIANA),
			AbstractBlockEntityCentrifuge.outputOf(AoAEssentia.TELLUS, AoAEssentia.SATURN, AoAEssentia.CERES)
		};
	}
}
