package dev.cafeteria.artofalchemy.util;

import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class FuelHelper {

	public static int fuelTime(final Item item) {
		return AbstractFurnaceBlockEntity.createFuelTimeMap().getOrDefault(item, 0);
	}

	public static int fuelTime(final ItemStack stack) {
		if (stack.isEmpty()) {
			return 0;
		} else {
			return FuelHelper.fuelTime(stack.getItem());
		}
	}

	public static boolean isFuel(final Item item) {
		return AbstractFurnaceBlockEntity.createFuelTimeMap().containsKey(item);
	}

	public static boolean isFuel(final ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		} else {
			return FuelHelper.isFuel(stack.getItem());
		}
	}

}
