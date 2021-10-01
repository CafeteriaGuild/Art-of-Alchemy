package dev.cafeteria.artofalchemy.block;

import dev.cafeteria.artofalchemy.blockentity.AoABlockEntities;
import dev.cafeteria.artofalchemy.blockentity.BlockEntityTank;
import dev.cafeteria.artofalchemy.essentia.EssentiaContainer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class BlockTank extends Block implements BlockEntityProvider {

	public static final BooleanProperty CONNECTED_TOP = BooleanProperty.of("connected_top");
	public static final BooleanProperty CONNECTED_BOTTOM = BooleanProperty.of("connected_bottom");
	public static final Settings SETTINGS = Settings.of(Material.GLASS).nonOpaque().strength(0.5f)
		.sounds(BlockSoundGroup.GLASS);

	public BlockTank() {
		super(BlockTank.SETTINGS);
		this.setDefaultState(
			this.getDefaultState().with(BlockTank.CONNECTED_TOP, false).with(BlockTank.CONNECTED_BOTTOM, false)
		);
	}

	@Override
	protected void appendProperties(final StateManager.Builder<Block, BlockState> builder) {
		builder.add(BlockTank.CONNECTED_TOP).add(BlockTank.CONNECTED_BOTTOM);
	}

	@Override
	public BlockEntity createBlockEntity(final BlockPos pos, final BlockState state) {
		return new BlockEntityTank(pos, state);
	}

	@Override
	public int getComparatorOutput(final BlockState state, final World world, final BlockPos pos) {
		final BlockEntity be = world.getBlockEntity(pos);
		if (be instanceof BlockEntityTank) {
			final EssentiaContainer container = ((BlockEntityTank) be).getContainer();
			final double fillLevel = (double) container.getCount() / container.getCapacity();
			if (fillLevel == 0.0) {
				return 0;
			} else {
				return 1 + (int) (fillLevel * 14);
			}
		} else {
			return 0;
		}
	}

	@Override
	public BlockState getPlacementState(final ItemPlacementContext ctx) {
		BlockState state = super.getPlacementState(ctx);
		if (ctx.getWorld().getBlockState(ctx.getBlockPos().offset(Direction.DOWN)).getBlock() == this) {
			state = state.with(BlockTank.CONNECTED_TOP, true);
		}
		if (ctx.getWorld().getBlockState(ctx.getBlockPos().offset(Direction.UP)).getBlock() == this) {
			state = state.with(BlockTank.CONNECTED_BOTTOM, true);
		}
		return state;
	}

	@Override
	public BlockState getStateForNeighborUpdate(
		final BlockState state, final Direction direction, final BlockState newState, final WorldAccess world,
		final BlockPos pos, final BlockPos posFrom
	) {
		if (posFrom.equals(pos.offset(Direction.DOWN))) {
			if (newState.getBlock() == this) {
				return state.with(BlockTank.CONNECTED_TOP, true);
			} else {
				return state.with(BlockTank.CONNECTED_TOP, false);
			}
		}
		if (posFrom.equals(pos.offset(Direction.UP))) {
			if (newState.getBlock() == this) {
				return state.with(BlockTank.CONNECTED_BOTTOM, true);
			} else {
				return state.with(BlockTank.CONNECTED_BOTTOM, false);
			}
		}
		return state;
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
		final World world, final BlockState state, final BlockEntityType<T> type
	) {
		return type == AoABlockEntities.TANK ? new BlockEntityTicker<T>() {
			@Override
			public void tick(final World world2, final BlockPos pos, final BlockState state2, final T entity) {
				((BlockEntityTank) entity).tick(world2, pos, state2, (BlockEntityTank) entity);
			}
		} : null;
	}

	@Override
	public boolean hasComparatorOutput(final BlockState state) {
		return true;
	}
}
