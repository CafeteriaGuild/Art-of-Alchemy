package dev.cafeteria.artofalchemy.item;

import dev.cafeteria.artofalchemy.block.AoABlocks;
import dev.cafeteria.artofalchemy.blockentity.BlockEntityPipe;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ItemEssentiaPort extends Item {
	public static Item getItem(final BlockEntityPipe.IOFace ioFace) {
		switch (ioFace) {
			case INSERTER:
				return AoAItems.ESSENTIA_INSERTER;
			case EXTRACTOR:
				return AoAItems.ESSENTIA_EXTRACTOR;
			case PASSIVE:
				return AoAItems.ESSENTIA_PORT;
			default:
				return Items.AIR;
		}
	}

	public final BlockEntityPipe.IOFace IOFACE;

	public ItemEssentiaPort(final Settings settings, final BlockEntityPipe.IOFace ioFace) {
		super(settings);
		this.IOFACE = ioFace;
	}

	@Override
	public ActionResult useOnBlock(final ItemUsageContext context) {
		final World world = context.getWorld();
		final BlockPos pos = context.getBlockPos();
		final BlockState state = world.getBlockState(pos);
		final Direction side = context.getSide();
		final PlayerEntity player = context.getPlayer();
		if (state.getBlock() == AoABlocks.PIPE) {
			return state.onUse(
				world,
				player,
				context.getHand(),
				new BlockHitResult(context.getHitPos(), side, pos, context.hitsInsideBlock())
			);
		} else {
			final BlockPos offPos = pos.offset(side);
			final BlockState offState = world.getBlockState(offPos);
			if (offState.getBlock() == AoABlocks.PIPE) {
				return offState.onUse(
					world,
					player,
					context.getHand(),
					new BlockHitResult(
						context.getHitPos(), player.isSneaking() ? side : side.getOpposite(), offPos, context.hitsInsideBlock()
					)
				);
			}
		}
		return super.useOnBlock(context);
	}
}
