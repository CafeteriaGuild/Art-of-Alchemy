package dev.cafeteria.artofalchemy.recipe;

import com.google.gson.JsonObject;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class SerializerProjection implements RecipeSerializer<RecipeProjection> {

	@Override
	public RecipeProjection read(final Identifier id, final JsonObject json) {
		final String group = JsonHelper.getString(json, "group", "");
		final Ingredient input = Ingredient.fromJson(JsonHelper.getObject(json, "ingredient"));
		final int cost = JsonHelper.getInt(json, "cost", 1);
		final ItemStack output = ShapedRecipe.outputFromJson(JsonHelper.getObject(json, "result"));
		final int alkahest = JsonHelper.getInt(json, "alkahest", 0);
		return new RecipeProjection(id, group, input, cost, output, alkahest);
	}

	@Override
	public RecipeProjection read(final Identifier id, final PacketByteBuf buf) {
		final String group = buf.readString(32767);
		final Ingredient input = Ingredient.fromPacket(buf);
		final int cost = buf.readVarInt();
		final ItemStack output = buf.readItemStack();
		final int alkahest = buf.readVarInt();
		return new RecipeProjection(id, group, input, cost, output, alkahest);
	}

	@Override
	public void write(final PacketByteBuf buf, final RecipeProjection recipe) {
		buf.writeString(recipe.group);
		recipe.input.write(buf);
		buf.writeVarInt(recipe.cost);
		buf.writeItemStack(recipe.output);
		buf.writeVarInt(recipe.alkahest);
	}

}
