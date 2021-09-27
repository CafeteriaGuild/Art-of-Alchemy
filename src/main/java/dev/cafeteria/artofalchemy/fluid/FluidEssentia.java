package dev.cafeteria.artofalchemy.fluid;

import dev.cafeteria.artofalchemy.block.AoABlocks;
import dev.cafeteria.artofalchemy.essentia.Essentia;
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

abstract class FluidEssentia extends FlowableFluid {

	protected final Essentia essentia;

	public FluidEssentia(Essentia essentia) {
		this.essentia = essentia;
	}

	public Essentia getEssentiaType() {
		return essentia;
	}

	@Override
	public Fluid getStill() {
		return AoAFluids.ESSENTIA_FLUIDS.get(essentia);
	}

	@Override
	public Fluid getFlowing() {
		return AoAFluids.ESSENTIA_FLUIDS_FLOWING.get(essentia);
	}

	@Override
	public boolean matchesType(Fluid fluid) {
		return fluid == getStill() || fluid == getFlowing();
	}

	@Override
	protected boolean isInfinite() {
		return false;
	}

	@Override
	protected void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state) {
		Block.dropStacks(state, world, pos, world.getBlockEntity(pos));
	}

	@Override
	protected int getFlowSpeed(WorldView world) {
		// Seems to be distance-related; water is 4, and so too will essentia
		return 4;
	}

	@Override
	protected int getLevelDecreasePerBlock(WorldView world) {
		return 1;
	}

	@Override
	public Item getBucketItem() {
		return AoAItems.ESSENTIA_BUCKETS.get(essentia);
	}

	@Override
	protected boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos, Fluid fluid,
			Direction direction) {
		return false;
	}

	@Override
	public int getTickRate(WorldView world) {
		return 5;
	}

	@Override
	protected float getBlastResistance() {
		return 100.0F;
	}

	@Override
	protected BlockState toBlockState(FluidState state) {
		return AoABlocks.ESSENTIA.get(essentia).getDefaultState().with(Properties.LEVEL_15, getBlockStateLevel(state));
	}

	public static class Flowing extends FluidEssentia {

		public Flowing(Essentia essentia) {
			super(essentia);
		}

		//		@Override
		//		protected boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos, Fluid fluid,
		//				Direction direction) {
		//			if (AoAFluids.ESSENTIA_FLUIDS.containsValue(fluid) ||
		//				AoAFluids.ESSENTIA_FLUIDS_FLOWING.containsValue(fluid) ||
		//				AoAFluids.ALKAHEST == fluid ||
		//				AoAFluids.ALKAHEST_FLOWING == fluid) {
		//				return true;
		//			} else {
		//				return super.canBeReplacedWith(state, world, pos, fluid, direction);
		//			}
		//		}

		@Override
		protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
			super.appendProperties(builder);
			builder.add(LEVEL);
		}

		@Override
		public int getLevel(FluidState state) {
			return state.get(LEVEL);
		}

		@Override
		public boolean isStill(FluidState state) {
			return false;
		}

	}

	public static class Still extends FluidEssentia {

		public Still(Essentia essentia) {
			super(essentia);
		}

		@Override
		public int getLevel(FluidState state) {
			return 8;
		}

		@Override
		public boolean isStill(FluidState state) {
			return true;
		}

	}

}
