package dev.cafeteria.artofalchemy.block;

import dev.cafeteria.artofalchemy.blockentity.AoABlockEntities;
import dev.cafeteria.artofalchemy.blockentity.BlockEntityAstroCentrifuge;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockAstroCentrifuge extends AbstractBlockCentrifuge {
	@Override
	public BlockEntity createBlockEntity(final BlockPos pos, final BlockState state) {
		return new BlockEntityAstroCentrifuge(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
		final World world, final BlockState state, final BlockEntityType<T> type
	) {
		return type == AoABlockEntities.ASTRO_CENTRIFUGE
			? (world2, pos, state2, entity) -> ((BlockEntityAstroCentrifuge) entity)
				.tick(world2, pos, state2, (BlockEntityAstroCentrifuge) entity)
			: null;
	}
}
