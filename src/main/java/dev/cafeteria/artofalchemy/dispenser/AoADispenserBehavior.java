package dev.cafeteria.artofalchemy.dispenser;

import dev.cafeteria.artofalchemy.item.AoAItems;
import dev.cafeteria.artofalchemy.item.ItemEssentiaVessel;
import dev.cafeteria.artofalchemy.transport.HasAlkahest;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.Direction;

public class AoADispenserBehavior {

	protected static final DispenserBehavior VESSEL_BEHAVIOR = new DispenserBehavior() {
		@Override
		public ItemStack dispense(final BlockPointer pointer, final ItemStack stack) {
			final Direction facing = pointer.getBlockState().get(DispenserBlock.FACING);
			final BlockEntity be = pointer.getWorld().getBlockEntity(pointer.getPos().offset(facing));
			ItemEssentiaVessel.useStackOnBE(stack, be);
			return stack;
		}
	};

	protected static final DispenserBehavior ALKAHEST_BEHAVIOR = new DispenserBehavior() {
		@Override
		public ItemStack dispense(final BlockPointer pointer, final ItemStack stack) {
			final Direction facing = pointer.getBlockState().get(DispenserBlock.FACING);
			final BlockEntity be = pointer.getWorld().getBlockEntity(pointer.getPos().offset(facing));
			if (be instanceof HasAlkahest) {
				final boolean successful = ((HasAlkahest) be).addAlkahest(1000);
				if (successful) {
					be.getWorld().playSound(null, be.getPos(), SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
					return new ItemStack(Items.BUCKET);
				}
			}
			return stack;
		}
	};

	public static void registerDispenserBehavior() {
		DispenserBlock.registerBehavior(AoAItems.ESSENTIA_VESSEL, AoADispenserBehavior.VESSEL_BEHAVIOR);
		DispenserBlock.registerBehavior(AoAItems.ALKAHEST_BUCKET, AoADispenserBehavior.ALKAHEST_BEHAVIOR);
	}

}
