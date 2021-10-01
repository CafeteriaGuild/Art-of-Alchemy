package dev.cafeteria.artofalchemy.util;

import dev.cafeteria.artofalchemy.item.ItemAlchemyFormula;
import dev.cafeteria.artofalchemy.item.ItemJournal;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class AoAHelper {

	public static int combineColor(final Vec3d color) {
		final int r = (int) (color.getX() * 0xFF);
		final int g = (int) (color.getY() * 0xFF);
		final int b = (int) (color.getZ() * 0xFF);
		return (r << 16) | (g << 8) | b;
	}

	public static int combineColor(final Vec3i color) {
		return (color.getX() << 16) | (color.getY() << 8) | color.getZ();
	}

	public static Vec3d decimalColor(final int color) {
		final double r = (color & 0xFF0000) >> 16;
		final double g = (color & 0x00FF00) >> 8;
		final double b = color & 0x0000FF;
		return new Vec3d(r / 0xFF, g / 0xFF, b / 0xFF);
	}

	public static Item getTarget(final ItemStack stack) {
		if (stack.getItem() instanceof ItemAlchemyFormula) {
			return ItemAlchemyFormula.getFormula(stack);
		} else if (stack.getItem() instanceof ItemJournal) {
			return ItemJournal.getFormula(stack);
		} else {
			return stack.getItem();
		}
	}

	public static Vec3i integerColor(final int color) {
		final int r = (color & 0xFF0000) >> 16;
		final int g = (color & 0x00FF00) >> 8;
		final int b = color & 0x0000FF;
		return new Vec3i(r, g, b);
	}

	// Coerces a rational number x into its neighbouring integers such
	// that over many invocations, the average of returned values approaches x.
	// Eg. stochasticRound(4.7) has a 70% chance of returning 5, and
	// a 30% chance of returning 4.
	public static int stochasticRound(final double x) {
		final double frac = MathHelper.fractionalPart(x);
		final int rounding = Math.random() >= frac ? 0 : 1;
		return (int) (Math.floor(x) + rounding);
	}

}
