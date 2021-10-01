package dev.cafeteria.artofalchemy.util;

import dev.cafeteria.artofalchemy.AoAConfig;
import dev.cafeteria.artofalchemy.item.AoAItems;
import dev.cafeteria.artofalchemy.item.ItemAlchemyFormula;
import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class AoALoot {

	public static final Identifier[] LOOT_TABLES = {
		LootTables.NETHER_BRIDGE_CHEST, LootTables.DESERT_PYRAMID_CHEST, LootTables.JUNGLE_TEMPLE_CHEST,
		LootTables.STRONGHOLD_LIBRARY_CHEST, LootTables.WOODLAND_MANSION_CHEST, LootTables.BASTION_BRIDGE_CHEST,
		LootTables.FISHING_TREASURE_GAMEPLAY
	};

	public static void initialize() {
		if (AoAConfig.get().formulaLoot) {
			// Thanks, TheBrokenRail!
			LootTableLoadingCallback.EVENT.register(new LootTableLoadingCallback() {
				@Override
				public void onLootTableLoading(
					final ResourceManager resourceManager, final LootManager lootManager, final Identifier id,
					final FabricLootSupplierBuilder supplier, final LootTableSetter setter
				) {
					if (AoALoot.isSelectedLootTable(id)) {
						final FabricLootPoolBuilder poolBuilder = FabricLootPoolBuilder.builder()
							.rolls(ConstantLootNumberProvider.create(1))
							.withEntry(ItemEntry.builder(AoAItems.ALCHEMY_FORMULA).build()).withFunction(new LootFunction() {
								@Override
								public ItemStack apply(final ItemStack stack, final LootContext ctx) {
									ItemAlchemyFormula.setFormula(stack, AoAItems.PHILOSOPHERS_STONE);
									return stack;
								}

								@Override
								public LootFunctionType getType() {
									return null;
								}
							});
						supplier.withPool(poolBuilder.build());
					}
				}
			});
		}
	}

	private static boolean isSelectedLootTable(final Identifier lootTable) {
		for (final Identifier id : AoALoot.LOOT_TABLES) {
			if (id.equals(lootTable)) {
				return true;
			}
		}
		return false;
	}

}
