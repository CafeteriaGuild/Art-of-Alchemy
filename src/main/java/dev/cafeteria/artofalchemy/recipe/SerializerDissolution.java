package dev.cafeteria.artofalchemy.recipe;

import com.google.gson.JsonObject;

import dev.cafeteria.artofalchemy.essentia.EssentiaStack;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class SerializerDissolution implements RecipeSerializer<RecipeDissolution> {

	@Override
	public RecipeDissolution read(final Identifier id, final JsonObject json) {
		final String group = JsonHelper.getString(json, "group", "");
		final Ingredient input = Ingredient.fromJson(JsonHelper.getObject(json, "ingredient"));
		final EssentiaStack essentia = new EssentiaStack(JsonHelper.getObject(json, "result"));
		final float factor = JsonHelper.getFloat(json, "factor", 1.0f);
		ItemStack container = ItemStack.EMPTY;
		if (json.has("container")) {
			container = ShapedRecipe.outputFromJson(JsonHelper.getObject(json, "container"));
		}
		return new RecipeDissolution(id, group, input, essentia, factor, container);
	}

	@Override
	public RecipeDissolution read(final Identifier id, final PacketByteBuf buf) {
		final String group = buf.readString(32767);
		final Ingredient input = Ingredient.fromPacket(buf);
		final EssentiaStack essentia = new EssentiaStack(buf.readNbt());
		final float factor = buf.readFloat();
		final ItemStack container = buf.readItemStack();
		return new RecipeDissolution(id, group, input, essentia, factor, container);
	}

	@Override
	public void write(final PacketByteBuf buf, final RecipeDissolution recipe) {
		buf.writeString(recipe.group);
		recipe.input.write(buf);
		buf.writeNbt(recipe.essentia.toTag());
		buf.writeFloat(recipe.factor);
		buf.writeItemStack(recipe.container);
	}

}
