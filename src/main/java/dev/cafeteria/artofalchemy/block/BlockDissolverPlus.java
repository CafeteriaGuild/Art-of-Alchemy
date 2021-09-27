package dev.cafeteria.artofalchemy.block;

import dev.cafeteria.artofalchemy.blockentity.AoABlockEntities;
import dev.cafeteria.artofalchemy.blockentity.BlockEntityDissolverPlus;

import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class BlockDissolverPlus extends BlockDissolver {

	public static final Settings SETTINGS = Settings
			.of(Material.METAL)
			.strength(5.0f, 6.0f)
			.luminance((state) -> state.get(LIT) ? 15 : 0)
			.nonOpaque();

	public static Identifier getId() {
		return Registry.BLOCK.getId(AoABlocks.DISSOLVER_PLUS);
	}

	public BlockDissolverPlus() {
		super(SETTINGS);
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new BlockEntityDissolverPlus(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return checkType(type, AoABlockEntities.DISSOLVER_PLUS, (world2, pos, state2, entity) -> ((BlockEntityDissolverPlus) entity).tick(world2, pos, state2, (BlockEntityDissolverPlus) entity));
	}

}
