package dev.cafeteria.artofalchemy.block;

import java.util.function.Predicate;

import dev.cafeteria.artofalchemy.blockentity.AoABlockEntities;
import dev.cafeteria.artofalchemy.blockentity.BlockEntityDissolver;
import dev.cafeteria.artofalchemy.fluid.AoAFluids;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class BlockDissolver extends BlockWithEntity {

	public static final BooleanProperty FILLED = BooleanProperty.of("filled");
	public static final BooleanProperty LIT = Properties.LIT;
	public static final Settings SETTINGS = Settings.of(Material.STONE).strength(5.0f, 6.0f)
		.luminance(state -> state.get(BlockDissolver.LIT) ? 15 : 0).nonOpaque();

	public static Identifier getId() {
		return Registry.BLOCK.getId(AoABlocks.DISSOLVER);
	}

	public BlockDissolver() {
		this(BlockDissolver.SETTINGS);
	}

	protected BlockDissolver(final Settings settings) {
		super(settings);
		this.setDefaultState(this.getDefaultState().with(BlockDissolver.FILLED, false).with(BlockDissolver.LIT, false));
	}

	@Override
	protected void appendProperties(final Builder<Block, BlockState> builder) {
		builder.add(BlockDissolver.FILLED).add(BlockDissolver.LIT);
	}

	@Override
	public BlockEntity createBlockEntity(final BlockPos pos, final BlockState state) {
		return new BlockEntityDissolver(pos, state);
	}

	@Override
	public int getComparatorOutput(final BlockState state, final World world, final BlockPos pos) {
		final BlockEntity be = world.getBlockEntity(pos);
		if (be instanceof BlockEntityDissolver) {
			final int capacity = (int) ((BlockEntityDissolver) be).getAlkahestCapacity();
			final int filled = (int) ((BlockEntityDissolver) be).getAlkahest();
			final double fillLevel = (double) filled / capacity;
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
		return super.getPlacementState(ctx);
	}

	@Override
	public BlockRenderType getRenderType(final BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
		final World world, final BlockState state, final BlockEntityType<T> type
	) {
		return BlockWithEntity.checkType(
			type,
			AoABlockEntities.DISSOLVER,
			(world2, pos, state2, entity) -> ((BlockEntityDissolver) entity)
				.tick(world2, pos, state2, (BlockEntityDissolver) entity)
		);
	}

	@Override
	public boolean hasComparatorOutput(final BlockState state) {
		return true;
	}

	@Override
	public void onStateReplaced(
		final BlockState state, final World world, final BlockPos pos, final BlockState newState, final boolean moved
	) {
		if (state.getBlock() != newState.getBlock()) {
			final BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity instanceof BlockEntityDissolver) {
				ItemScatterer.spawn(world, pos, (Inventory) blockEntity);
			}

			super.onStateReplaced(state, world, pos, newState, moved);
		}
	}

	@SuppressWarnings("deprecation") // Experimental API
	@Override
	public ActionResult onUse(
		final BlockState state, final World world, final BlockPos pos, final PlayerEntity player, final Hand hand,
		final BlockHitResult hit
	) {
		final BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof final BlockEntityDissolver dissolver) {
			final Storage<FluidVariant> tankItem = ContainerItemContext.ofPlayerHand(player, hand).find(FluidStorage.ITEM);
			if (tankItem != null) {
				final Transaction trans = Transaction.openOuter();
				StorageUtil.move(
					tankItem,
					dissolver.getAlkahestTank(),
					(Predicate<FluidVariant>) fluid -> fluid.isOf(AoAFluids.ALKAHEST),
					Long.MAX_VALUE,
					trans
				);
				trans.commit();
				world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
			} else if (!world.isClient()) {
				player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
			}
			return ActionResult.SUCCESS;
		} else {
			return ActionResult.PASS;
		}

	}

}
