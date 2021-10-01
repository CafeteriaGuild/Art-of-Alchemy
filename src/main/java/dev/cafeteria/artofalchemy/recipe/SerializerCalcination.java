package dev.cafeteria.artofalchemy.recipe;

import com.google.gson.JsonObject;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class SerializerCalcination implements RecipeSerializer<RecipeCalcination> {

	@Override
	public RecipeCalcination read(final Identifier id, final JsonObject json) {
		final String group = JsonHelper.getString(json, "group", "");
		final Ingredient input = Ingredient.fromJson(JsonHelper.getObject(json, "ingredient"));
		final ItemStack output = ShapedRecipe.outputFromJson(JsonHelper.getObject(json, "result"));
		final float factor = JsonHelper.getFloat(json, "factor", 1.0f);
		ItemStack container = ItemStack.EMPTY;
		if (json.has("container")) {
			container = ShapedRecipe.outputFromJson(JsonHelper.getObject(json, "container"));
		}
		return new RecipeCalcination(id, group, input, output, factor, container);
	}

	@Override
	public RecipeCalcination read(final Identifier id, final PacketByteBuf buf) {
		final String group = buf.readString(32767);
		final Ingredient input = Ingredient.fromPacket(buf);
		final ItemStack output = buf.readItemStack();
		final float factor = buf.readFloat();
		final ItemStack container = buf.readItemStack();
		return new RecipeCalcination(id, group, input, output, factor, container);
	}

	@Override
	public void write(final PacketByteBuf buf, final RecipeCalcination recipe) {
		buf.writeString(recipe.group);
		recipe.input.write(buf);
		buf.writeItemStack(recipe.output);
		buf.writeFloat(recipe.factor);
		buf.writeItemStack(recipe.container);
	}

}
