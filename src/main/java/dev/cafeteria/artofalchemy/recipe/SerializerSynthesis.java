package dev.cafeteria.artofalchemy.recipe;

import dev.cafeteria.artofalchemy.essentia.EssentiaStack;
import dev.cafeteria.artofalchemy.item.ItemMateria;
import com.google.gson.JsonObject;

import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

public class SerializerSynthesis implements RecipeSerializer<RecipeSynthesis> {

	@Override
	public RecipeSynthesis read(Identifier id, JsonObject json) {
		String group = JsonHelper.getString(json, "group", "");
		Ingredient target = Ingredient.fromJson(JsonHelper.getObject(json, "target"));
		Ingredient materia = Ingredient.fromJson(JsonHelper.getObject(json, "materia"));
		EssentiaStack essentia = new EssentiaStack(JsonHelper.getObject(json, "essentia"));
		Ingredient container = Ingredient.EMPTY;
		if (json.has("container")) {
			container = Ingredient.fromJson(JsonHelper.getObject(json, "container"));
		}
		int cost = JsonHelper.getInt(json, "cost", 1);
		int tier = JsonHelper.getInt(json, "tier", -1);
		if (tier == -1 && !materia.isEmpty()) {
			Item item = Registry.ITEM.get(materia.getMatchingItemIds().getInt(0));
			if (item instanceof ItemMateria) {
				tier = ((ItemMateria) item).getTier();
			}
		}
		return new RecipeSynthesis(id, group, target, materia, essentia, container, cost, tier);
	}

	@Override
	public RecipeSynthesis read(Identifier id, PacketByteBuf buf) {
		String group = buf.readString(32767);
		Ingredient target = Ingredient.fromPacket(buf);
		Ingredient materia = Ingredient.fromPacket(buf);
		EssentiaStack essentia = new EssentiaStack(buf.readNbt());
		Ingredient container = Ingredient.fromPacket(buf);
		int cost = buf.readVarInt();
		int tier = buf.readVarInt();
		return new RecipeSynthesis(id, group, target, materia, essentia, container, cost, tier);
	}

	@Override
	public void write(PacketByteBuf buf, RecipeSynthesis recipe) {
		buf.writeString(recipe.group);
		recipe.target.write(buf);
		recipe.materia.write(buf);
		buf.writeNbt(recipe.essentia.toTag());
		recipe.container.write(buf);
		buf.writeVarInt(recipe.cost);
		buf.writeVarInt(recipe.tier);
	}


}
