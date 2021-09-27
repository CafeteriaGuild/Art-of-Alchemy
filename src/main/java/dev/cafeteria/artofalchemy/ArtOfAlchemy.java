package dev.cafeteria.artofalchemy;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.cafeteria.artofalchemy.block.AoABlocks;
import dev.cafeteria.artofalchemy.blockentity.AoABlockEntities;
import dev.cafeteria.artofalchemy.dispenser.AoADispenserBehavior;
import dev.cafeteria.artofalchemy.essentia.AoAEssentia;
import dev.cafeteria.artofalchemy.fluid.AoAFluids;
import dev.cafeteria.artofalchemy.gui.handler.AoAHandlers;
import dev.cafeteria.artofalchemy.item.AoAItems;
import dev.cafeteria.artofalchemy.network.AoANetworking;
import dev.cafeteria.artofalchemy.recipe.AoARecipes;
import dev.cafeteria.artofalchemy.transport.EssentiaNetworker;
import dev.cafeteria.artofalchemy.util.AoALoot;
import dev.cafeteria.artofalchemy.util.AoATags;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.event.world.WorldTickCallback;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public class ArtOfAlchemy implements ModInitializer {

	public static final String MOD_ID = "artofalchemy";
	public static final String MOD_NAME = "Art of Alchemy";

	public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

	public static final ItemGroup ALCHEMY_GROUP = FabricItemGroupBuilder.create(id("alchemy"))
			.icon(() -> new ItemStack(AoAItems.MYSTERIOUS_SIGIL)).build();

	@Override
	public void onInitialize() {
		log(Level.INFO, "Humankind cannot gain anything without first giving something in return. "
				+ "To obtain, something of equal value must be lost.");

		AoATags.init();
		AutoConfig.register(AoAConfig.class, GsonConfigSerializer::new);
		AoAEssentia.registerEssentia();
		AoAFluids.registerFluids();
		AoABlocks.registerBlocks();
		AoAItems.registerItems();
		AoABlockEntities.registerBlockEntities();
		AoAHandlers.registerHandlers();
		AoARecipes.registerRecipes();
		AoADispenserBehavior.registerDispenserBehavior();
		AoANetworking.initializeNetworking();
		AoALoot.initialize();
		WorldTickCallback.EVENT.register((world) -> {
			if (!world.isClient()) {
				EssentiaNetworker.get((ServerWorld) world).tick();
			}
		});
	}

	public static Identifier id(String name) {
		return new Identifier(MOD_ID, name);
	}

	public static void log(Level level, String message){
		LOGGER.log(level, message);
	}

}
