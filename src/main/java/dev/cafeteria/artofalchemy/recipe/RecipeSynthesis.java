package dev.cafeteria.artofalchemy.recipe;

import dev.cafeteria.artofalchemy.block.AoABlocks;
import dev.cafeteria.artofalchemy.essentia.EssentiaStack;
import dev.cafeteria.artofalchemy.util.AoAHelper;
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

public class RecipeSynthesis implements Recipe<Inventory> {

	protected final Identifier id;
	protected final String group;
	protected final Ingredient target;
	protected final Ingredient materia;
	protected final EssentiaStack essentia;
	protected final Ingredient container;
	protected final int cost;
	protected final int tier;

	public RecipeSynthesis(
		final Identifier id, final String group, final Ingredient target, final Ingredient materia,
		final EssentiaStack essentia, final Ingredient container, final int cost, final int tier
	) {
		this.id = id;
		this.group = group;
		this.target = target;
		this.materia = materia;
		this.essentia = essentia;
		this.container = container;
		this.cost = cost;
		this.tier = tier;
	}

	@Override
	public ItemStack craft(final Inventory inv) {
		return ItemStack.EMPTY;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public ItemStack createIcon() {
		return new ItemStack(AoABlocks.SYNTHESIZER);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean fits(final int width, final int height) {
		return true;
	}

	public Ingredient getContainer() {
		return this.container;
	}

	public int getCost() {
		return this.cost;
	}

	public EssentiaStack getEssentia() {
		return (EssentiaStack) this.essentia.clone();
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

	public Ingredient getMateria() {
		return this.materia;
	}

	@Override
	public ItemStack getOutput() {
		return ItemStack.EMPTY;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return AoARecipes.SYNTHESIS_SERIALIZER;
	}

	public int getTier() {
		return this.tier;
	}

	@Override
	public RecipeType<?> getType() {
		return AoARecipes.SYNTHESIS;
	}

	@Override
	public boolean matches(final Inventory inv, final World world) {
		final ItemStack stack = inv.getStack(2);
		return this.target.test(new ItemStack(AoAHelper.getTarget(stack)));
	}

}
