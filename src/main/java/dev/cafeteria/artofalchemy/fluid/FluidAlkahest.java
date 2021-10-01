package dev.cafeteria.artofalchemy.fluid;

import dev.cafeteria.artofalchemy.block.AoABlocks;
import dev.cafeteria.artofalchemy.item.AoAItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

abstract class FluidAlkahest extends FlowableFluid {

	public static class Flowing extends FluidAlkahest {

		@Override
		protected void appendProperties(final StateManager.Builder<Fluid, FluidState> builder) {
			super.appendProperties(builder);
			builder.add(FlowableFluid.LEVEL);
		}

		// @Override
		// protected boolean canBeReplacedWith(FluidState state, BlockView world,
		// BlockPos pos, Fluid fluid,
		// Direction direction) {
		// if (AoAFluids.ESSENTIA_FLUIDS.containsValue(fluid) ||
		// AoAFluids.ESSENTIA_FLUIDS_FLOWING.containsValue(fluid) ||
		// AoAFluids.ALKAHEST == fluid ||
		// AoAFluids.ALKAHEST_FLOWING == fluid) {
		// return true;
		// } else {
		// return super.canBeReplacedWith(state, world, pos, fluid, direction);
		// }
		// }

		@Override
		public int getLevel(final FluidState state) {
			return state.get(FlowableFluid.LEVEL);
		}

		@Override
		public boolean isStill(final FluidState state) {
			return false;
		}

	}

	public static class Still extends FluidAlkahest {

		@Override
		public int getLevel(final FluidState state) {
			return 8;
		}

		@Override
		public boolean isStill(final FluidState state) {
			return true;
		}

	}

	@Override
	protected void beforeBreakingBlock(final WorldAccess world, final BlockPos pos, final BlockState state) {
		Block.dropStacks(state, world, pos, world.getBlockEntity(pos));
	}

	@Override
	protected boolean canBeReplacedWith(
		final FluidState state, final BlockView world, final BlockPos pos, final Fluid fluid, final Direction direction
	) {
		return false;
	}

	@Override
	protected float getBlastResistance() {
		return 100.0F;
	}

	@Override
	public Item getBucketItem() {
		return AoAItems.ALKAHEST_BUCKET;
	}

	@Override
	public Fluid getFlowing() {
		return AoAFluids.ALKAHEST_FLOWING;
	}

	@Override
	protected int getFlowSpeed(final WorldView world) {
		// Seems to be distance-related; water is 4, and so too will be Alkahest
		return 4;
	}

	@Override
	protected int getLevelDecreasePerBlock(final WorldView world) {
		return 1;
	}

	@Override
	public Fluid getStill() {
		return AoAFluids.ALKAHEST;
	}

	@Override
	public int getTickRate(final WorldView world) {
		return 5;
	}

	@Override
	protected boolean isInfinite() {
		return false;
	}

	@Override
	public boolean matchesType(final Fluid fluid) {
		return fluid == this.getStill() || fluid == this.getFlowing();
	}

	@Override
	protected BlockState toBlockState(final FluidState state) {
		return AoABlocks.ALKAHEST.getDefaultState().with(Properties.LEVEL_15, FlowableFluid.getBlockStateLevel(state));
	}

}
