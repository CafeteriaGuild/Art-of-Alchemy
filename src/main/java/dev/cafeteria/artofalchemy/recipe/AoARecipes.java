package dev.cafeteria.artofalchemy.recipe;

import dev.cafeteria.artofalchemy.ArtOfAlchemy;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.registry.Registry;

public class AoARecipes {

	public static RecipeType<RecipeCalcination> CALCINATION;
	public static RecipeSerializer<RecipeCalcination> CALCINATION_SERIALIZER;

	public static RecipeType<RecipeDissolution> DISSOLUTION;
	public static RecipeSerializer<RecipeDissolution> DISSOLUTION_SERIALIZER;

	public static RecipeType<RecipeSynthesis> SYNTHESIS;
	public static RecipeSerializer<RecipeSynthesis> SYNTHESIS_SERIALIZER;

	public static RecipeType<RecipeProjection> PROJECTION;
	public static RecipeSerializer<RecipeProjection> PROJECTION_SERIALIZER;

	public static <T extends Recipe<?>> RecipeType<T> register(final String name) {
		return Registry.register(Registry.RECIPE_TYPE, ArtOfAlchemy.id(name), new RecipeType<T>() {
			@Override
			public String toString() {
				return ArtOfAlchemy.id(name).toString();
			}
		});
	}

	public static <S extends RecipeSerializer<T>, T extends Recipe<?>> S register(final String name, final S serializer) {
		return Registry.register(Registry.RECIPE_SERIALIZER, ArtOfAlchemy.id(name), serializer);
	}

	public static void registerRecipes() {
		AoARecipes.CALCINATION = AoARecipes.register("calcination");
		AoARecipes.CALCINATION_SERIALIZER = AoARecipes.register("calcination", new SerializerCalcination());

		AoARecipes.DISSOLUTION = AoARecipes.register("dissolution");
		AoARecipes.DISSOLUTION_SERIALIZER = AoARecipes.register("dissolution", new SerializerDissolution());

		AoARecipes.SYNTHESIS = AoARecipes.register("synthesis");
		AoARecipes.SYNTHESIS_SERIALIZER = AoARecipes.register("synthesis", new SerializerSynthesis());

		AoARecipes.PROJECTION = AoARecipes.register("projection");
		AoARecipes.PROJECTION_SERIALIZER = AoARecipes.register("projection", new SerializerProjection());
	}

}
