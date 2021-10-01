package dev.cafeteria.artofalchemy.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import dev.cafeteria.artofalchemy.item.AoAItems;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

@Mixin(RecipeManager.class)
public abstract class MixinRecipeManager {

	@Inject(
		at = @At(
			value = "RETURN", ordinal = 0
		), method = "getRemainingStacks", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD
	)
	private <C extends Inventory, T extends Recipe<C>> void replaceRemainingStacks(
		final RecipeType<T> recipeType, final C inventory, final World world,
		final CallbackInfoReturnable<DefaultedList<ItemStack>> info, final Optional<T> optional
	) {
		if (optional.get().getOutput().getItem() == AoAItems.ALKAHEST_BUCKET) {
			info.setReturnValue(DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY));
		}
	}
}
