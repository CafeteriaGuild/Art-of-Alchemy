package dev.cafeteria.artofalchemy.block;

import java.util.function.ToIntFunction;

import dev.cafeteria.artofalchemy.blockentity.AoABlockEntities;
import dev.cafeteria.artofalchemy.blockentity.BlockEntityDistiller;
import dev.cafeteria.artofalchemy.item.AoAItems;
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
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
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

public class BlockDistiller extends BlockWithEntity {

	public static final BooleanProperty LIT = Properties.LIT;
	public static final Settings SETTINGS = Settings.of(Material.STONE).strength(5.0f, 6.0f)
		.luminance(new ToIntFunction<BlockState>() {
			@Override
			public int applyAsInt(final BlockState state) {
				return state.get(BlockDistiller.LIT) ? 15 : 0;
			}
		}).nonOpaque();

	public static Identifier getId() {
		return Registry.BLOCK.getId(AoABlocks.DISTILLER);
	}

	public BlockDistiller() {
		this(BlockDistiller.SETTINGS);
	}

	protected BlockDistiller(final Settings settings) {
		super(settings);
		this.setDefaultState(this.getDefaultState().with(BlockDistiller.LIT, false));
	}

	@Override
	protected void appendProperties(final Builder<Block, BlockState> builder) {
		builder.add(BlockDistiller.LIT);
	}

	@Override
	public BlockEntity createBlockEntity(final BlockPos pos, final BlockState state) {
		return new BlockEntityDistiller(pos, state);
	}

	@Override
	public int getComparatorOutput(final BlockState state, final World world, final BlockPos pos) {
		final BlockEntity be = world.getBlockEntity(pos);
		if (be instanceof BlockEntityDistiller) {
			final int capacity = ((BlockEntityDistiller) be).getTankSize();
			final int filled = ((BlockEntityDistiller) be).getAlkahest();
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
		return BlockWithEntity.checkType(type, AoABlockEntities.DISTILLER, new BlockEntityTicker<BlockEntity>() {
			@Override
			public void tick(final World world2, final BlockPos pos, final BlockState state2, final BlockEntity entity) {
				((BlockEntityDistiller) entity).tick(world2, pos, state2, (BlockEntityDistiller) entity);
			}
		});
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
			if (blockEntity instanceof BlockEntityDistiller) {
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

		final ItemStack inHand = player.getStackInHand(hand);

		final BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof final BlockEntityDistiller distiller) {
			if (inHand.getItem() == Items.BUCKET && distiller.withdrawAlkahest(1000)) {
				if (!player.getAbilities().creativeMode) {
					inHand.decrement(1);
					player.giveItemStack(new ItemStack(AoAItems.ALKAHEST_BUCKET));
				}
				world.playSound(null, pos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
				return ActionResult.SUCCESS;
			} else if (inHand.getItem() == AoAItems.ESSENTIA_VESSEL) {
				final ItemUsageContext itemContext = new ItemUsageContext(player, hand, hit);
				final ActionResult itemResult = inHand.useOnBlock(itemContext);
				if (itemResult != ActionResult.PASS) {
					return itemResult;
				}
			}
			if (!world.isClient()) {
				player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
			}
			return ActionResult.SUCCESS;
		} else {
			return ActionResult.PASS;
		}

	}

}
