package dev.cafeteria.artofalchemy.block;

import dev.cafeteria.artofalchemy.blockentity.AoABlockEntities;
import dev.cafeteria.artofalchemy.blockentity.BlockEntitySynthesizerPlus;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class BlockSynthesizerPlus extends BlockSynthesizer {

	public static final Settings SETTINGS = Settings.of(Material.METAL).strength(5.0f, 6.0f)
		.luminance(state -> state.get(BlockSynthesizer.LIT) ? 15 : 0).nonOpaque();

	public static Identifier getId() {
		return Registry.BLOCK.getId(AoABlocks.SYNTHESIZER_PLUS);
	}

	public BlockSynthesizerPlus() {
		super(BlockSynthesizerPlus.SETTINGS);
	}

	@Override
	public BlockEntity createBlockEntity(final BlockPos pos, final BlockState state) {
		return new BlockEntitySynthesizerPlus(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
		final World world, final BlockState state, final BlockEntityType<T> type
	) {
		return BlockWithEntity.checkType(
			type,
			AoABlockEntities.SYNTHESIZER_PLUS,
			(world2, pos, state2, entity) -> ((BlockEntitySynthesizerPlus) entity)
				.tick(world2, pos, state2, (BlockEntitySynthesizerPlus) entity)
		);
	}

}
