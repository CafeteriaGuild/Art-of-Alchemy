package dev.cafeteria.artofalchemy.block;

import dev.cafeteria.artofalchemy.blockentity.AoABlockEntities;
import dev.cafeteria.artofalchemy.blockentity.BlockEntityCalcinatorPlus;

import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class BlockCalcinatorPlus extends BlockCalcinator {

	public static final Settings SETTINGS = Settings
			.of(Material.METAL).sounds(BlockSoundGroup.METAL)
			.strength(5.0f, 6.0f)
			.luminance((state) -> state.get(LIT) ? 15 : 0)
			.nonOpaque();

	public static Identifier getId() {
		return Registry.BLOCK.getId(AoABlocks.CALCINATOR_PLUS);
	}

	public BlockCalcinatorPlus() {
		super(SETTINGS);
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new BlockEntityCalcinatorPlus(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return checkType(type, AoABlockEntities.CALCINATOR_PLUS, (world2, pos, state2, entity) -> ((BlockEntityCalcinatorPlus) entity).tick(world2, pos, state2, (BlockEntityCalcinatorPlus) entity));
	}

}
