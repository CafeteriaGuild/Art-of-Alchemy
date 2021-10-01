package dev.cafeteria.artofalchemy.recipe;

import com.google.gson.JsonObject;

import dev.cafeteria.artofalchemy.essentia.EssentiaStack;
import dev.cafeteria.artofalchemy.item.ItemMateria;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

public class SerializerSynthesis implements RecipeSerializer<RecipeSynthesis> {

	@Override
	public RecipeSynthesis read(final Identifier id, final JsonObject json) {
		final String group = JsonHelper.getString(json, "group", "");
		final Ingredient target = Ingredient.fromJson(JsonHelper.getObject(json, "target"));
		final Ingredient materia = Ingredient.fromJson(JsonHelper.getObject(json, "materia"));
		final EssentiaStack essentia = new EssentiaStack(JsonHelper.getObject(json, "essentia"));
		Ingredient container = Ingredient.EMPTY;
		if (json.has("container")) {
			container = Ingredient.fromJson(JsonHelper.getObject(json, "container"));
		}
		final int cost = JsonHelper.getInt(json, "cost", 1);
		int tier = JsonHelper.getInt(json, "tier", -1);
		if (tier == -1 && !materia.isEmpty()) {
			final Item item = Registry.ITEM.get(materia.getMatchingItemIds().getInt(0));
			if (item instanceof ItemMateria) {
				tier = ((ItemMateria) item).getTier();
			}
		}
		return new RecipeSynthesis(id, group, target, materia, essentia, container, cost, tier);
	}

	@Override
	public RecipeSynthesis read(final Identifier id, final PacketByteBuf buf) {
		final String group = buf.readString(32767);
		final Ingredient target = Ingredient.fromPacket(buf);
		final Ingredient materia = Ingredient.fromPacket(buf);
		final EssentiaStack essentia = new EssentiaStack(buf.readNbt());
		final Ingredient container = Ingredient.fromPacket(buf);
		final int cost = buf.readVarInt();
		final int tier = buf.readVarInt();
		return new RecipeSynthesis(id, group, target, materia, essentia, container, cost, tier);
	}

	@Override
	public void write(final PacketByteBuf buf, final RecipeSynthesis recipe) {
		buf.writeString(recipe.group);
		recipe.target.write(buf);
		recipe.materia.write(buf);
		buf.writeNbt(recipe.essentia.toTag());
		recipe.container.write(buf);
		buf.writeVarInt(recipe.cost);
		buf.writeVarInt(recipe.tier);
	}

}
