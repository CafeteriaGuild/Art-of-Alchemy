package dev.cafeteria.artofalchemy.recipe;

import dev.cafeteria.artofalchemy.block.AoABlocks;
import dev.cafeteria.artofalchemy.essentia.EssentiaStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class RecipeDissolution implements Recipe<Inventory> {

	protected final Identifier id;
	protected final String group;
	protected final Ingredient input;
	protected final EssentiaStack essentia;
	protected final float factor;
	protected final ItemStack container;

	public RecipeDissolution(
		final Identifier id, final String group, final Ingredient input, final EssentiaStack essentia, final float factor,
		final ItemStack container
	) {
		this.id = id;
		this.group = group;
		this.input = input;
		this.essentia = essentia;
		this.factor = factor;
		this.container = container;
	}

	public RecipeDissolution(
		final Identifier id, final String group, final Ingredient input, final EssentiaStack essentia,
		final ItemStack container
	) {
		this(id, group, input, essentia, 1.0f, container);
	}

	@Override
	public ItemStack craft(final Inventory inv) {
		return ItemStack.EMPTY;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public ItemStack createIcon() {
		return new ItemStack(AoABlocks.DISSOLVER);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean fits(final int width, final int height) {
		return true;
	}

	public ItemStack getContainer() {
		return this.container;
	}

	public EssentiaStack getEssentia() {
		return (EssentiaStack) this.essentia.clone();
	}

	public float getFactor() {
		return this.factor;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public String getGroup() {
		return this.group;
	}

	@Override
	public Identifier getId() {
		return this.id;
	}

	public Ingredient getInput() {
		return this.input;
	}

	@Override
	public ItemStack getOutput() {
		return ItemStack.EMPTY;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return AoARecipes.DISSOLUTION_SERIALIZER;
	}

	@Override
	public RecipeType<?> getType() {
		return AoARecipes.DISSOLUTION;
	}

	@Override
	public boolean matches(final Inventory inv, final World world) {
		return this.input.test(inv.getStack(0));
	}

}
