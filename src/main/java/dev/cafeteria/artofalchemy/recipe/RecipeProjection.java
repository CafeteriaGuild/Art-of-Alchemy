package dev.cafeteria.artofalchemy.recipe;

import dev.cafeteria.artofalchemy.block.AoABlocks;
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

public class RecipeProjection implements Recipe<Inventory> {

	protected final Identifier id;
	protected final String group;
	protected final Ingredient input;
	protected final int cost;
	protected final ItemStack output;
	protected final int alkahest;

	public RecipeProjection(
		final Identifier id, final String group, final Ingredient input, final int cost, final ItemStack output,
		final int alkahest
	) {
		this.id = id;
		this.group = group;
		this.input = input;
		this.cost = cost;
		this.output = output;
		this.alkahest = alkahest;
	}

	@Override
	public ItemStack craft(final Inventory inv) {
		return this.output.copy();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public ItemStack createIcon() {
		return new ItemStack(AoABlocks.PROJECTOR);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean fits(final int width, final int height) {
		return true;
	}

	public int getAlkahest() {
		return this.alkahest;
	}

	public int getCost() {
		return this.cost;
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
		return this.output;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return AoARecipes.PROJECTION_SERIALIZER;
	}

	@Override
	public RecipeType<?> getType() {
		return AoARecipes.PROJECTION;
	}

	@Override
	public boolean matches(final Inventory inv, final World world) {
		return this.input.test(inv.getStack(0));
	}

}
