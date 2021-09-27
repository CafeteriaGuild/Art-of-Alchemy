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

	public RecipeDissolution(Identifier id, String group, Ingredient input, EssentiaStack essentia, float factor, ItemStack container) {
		this.id = id;
		this.group = group;
		this.input = input;
		this.essentia = essentia;
		this.factor = factor;
		this.container = container;
	}

	public RecipeDissolution(Identifier id, String group, Ingredient input, EssentiaStack essentia, ItemStack container) {
		this(id, group, input, essentia, 1.0f, container);
	}

	@Override
	public boolean matches(Inventory inv, World world) {
		return input.test(inv.getStack(0));
	}

	@Override
	public ItemStack craft(Inventory inv) {
		return ItemStack.EMPTY;
	}

	public Ingredient getInput() {
		return input;
	}

	public EssentiaStack getEssentia() {
		return (EssentiaStack) essentia.clone();
	}

	@Override
	public ItemStack getOutput() {
		return ItemStack.EMPTY;
	}

	@Override
	public Identifier getId() {
		return id;
	}

	public float getFactor() {
		return factor;
	}

	public ItemStack getContainer() {
		return container;
	}

	@Override
	public RecipeType<?> getType() {
		return AoARecipes.DISSOLUTION;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return AoARecipes.DISSOLUTION_SERIALIZER;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean fits(int width, int height) {
		return true;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public String getGroup() {
		return group;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public ItemStack createIcon() {
		return new ItemStack(AoABlocks.DISSOLVER);
	}

}
