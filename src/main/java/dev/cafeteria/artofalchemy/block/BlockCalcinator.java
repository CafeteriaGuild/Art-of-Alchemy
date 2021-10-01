package dev.cafeteria.artofalchemy.block;

import java.util.function.ToIntFunction;

import dev.cafeteria.artofalchemy.blockentity.AoABlockEntities;
import dev.cafeteria.artofalchemy.blockentity.BlockEntityCalcinator;
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
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class BlockCalcinator extends BlockWithEntity {

	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
	public static final BooleanProperty LIT = Properties.LIT;
	public static final Settings SETTINGS = Settings.of(Material.STONE).strength(5.0f, 6.0f)
		.luminance(new ToIntFunction<BlockState>() {
			@Override
			public int applyAsInt(final BlockState state) {
				return state.get(BlockCalcinator.LIT) ? 15 : 0;
			}
		}).nonOpaque();

	public static Identifier getId() {
		return Registry.BLOCK.getId(AoABlocks.CALCINATOR);
	}

	public BlockCalcinator() {
		this(BlockCalcinator.SETTINGS);
	}

	protected BlockCalcinator(final Settings settings) {
		super(settings);
		this.setDefaultState(
			this.getDefaultState().with(BlockCalcinator.FACING, Direction.NORTH).with(BlockCalcinator.LIT, false)
		);
	}

	@Override
	protected void appendProperties(final Builder<Block, BlockState> builder) {
		builder.add(BlockCalcinator.FACING).add(BlockCalcinator.LIT);
	}

	@Override
	public BlockEntity createBlockEntity(final BlockPos pos, final BlockState state) {
		return new BlockEntityCalcinator(pos, state);
	}

	@Override
	public BlockState getPlacementState(final ItemPlacementContext ctx) {
		return super.getPlacementState(ctx).with(BlockCalcinator.FACING, ctx.getPlayerFacing().getOpposite());
	}

	@Override
	public BlockRenderType getRenderType(final BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
		final World world, final BlockState state, final BlockEntityType<T> type
	) {
		return BlockWithEntity.checkType(type, AoABlockEntities.CALCINATOR, new BlockEntityTicker<BlockEntity>() {
			@Override
			public void tick(final World world2, final BlockPos pos, final BlockState state2, final BlockEntity entity) {
				((BlockEntityCalcinator) entity).tick(world2, pos, state2, (BlockEntityCalcinator) entity);
			}
		});
	}

	@Override
	public BlockState mirror(final BlockState state, final BlockMirror mirror) {
		return state.rotate(mirror.getRotation(state.get(BlockCalcinator.FACING)));
	}

	@Override
	public void onStateReplaced(
		final BlockState state, final World world, final BlockPos pos, final BlockState newState, final boolean moved
	) {
		if (state.getBlock() != newState.getBlock()) {
			final BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity instanceof BlockEntityCalcinator) {
				ItemScatterer.spawn(world, pos, (Inventory) blockEntity);
			}

			super.onStateReplaced(state, world, pos, newState, moved);
		}
	}

	@Override
	public ActionResult onUse(
		final BlockState state, final World world, final BlockPos pos, final PlayerEntity player, final Hand hand,
		final BlockHitResult hit
	) {

		if (!world.isClient) {
			final BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity instanceof BlockEntityCalcinator) {
				player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
			}
		}

		return ActionResult.SUCCESS;
	}

	@Override
	public BlockState rotate(final BlockState state, final BlockRotation rotation) {
		return state.with(BlockCalcinator.FACING, rotation.rotate(state.get(BlockCalcinator.FACING)));
	}
}
