package dev.cafeteria.artofalchemy.item;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import dev.cafeteria.artofalchemy.ArtOfAlchemy;
import dev.cafeteria.artofalchemy.blockentity.BlockEntityPipe;
import dev.cafeteria.artofalchemy.essentia.Essentia;
import dev.cafeteria.artofalchemy.essentia.RegistryEssentia;
import dev.cafeteria.artofalchemy.fluid.AoAFluids;
import dev.cafeteria.artofalchemy.util.MateriaRank;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.Item.Settings;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

public class AoAItems {

	public static final Item ESSENTIA_PORT = new ItemEssentiaPort(AoAItems.defaults(), BlockEntityPipe.IOFace.PASSIVE);
	public static final Item ESSENTIA_INSERTER = new ItemEssentiaPort(
		AoAItems.defaults(), BlockEntityPipe.IOFace.INSERTER
	);
	public static final Item ESSENTIA_EXTRACTOR = new ItemEssentiaPort(
		AoAItems.defaults(), BlockEntityPipe.IOFace.EXTRACTOR
	);
	public static final Item ESSENTIA_VESSEL = new ItemEssentiaVessel(AoAItems.defaults());

	public static final Item MYSTERIOUS_SIGIL = new Item(new Item.Settings());
	public static final Item AZOTH = new Item(AoAItems.defaults());
	public static final Item AMARANTH_PEARL = new Item(AoAItems.defaults());
	public static final Item CRACKED_AMARANTH_PEARL = new Item(AoAItems.defaults());
	public static final Item ACTIVATED_AMARANTH_PEARL = new Item(AoAItems.defaults().rarity(Rarity.UNCOMMON));
	public static final Item PHILOSOPHERS_STONE = new Item(AoAItems.defaults().rarity(Rarity.EPIC)) {
		@Override
		public boolean hasGlint(final ItemStack stack) {
			return true;
		}
	};

	public static final Item JOURNAL = new ItemJournal(AoAItems.defaults());
	public static final Item ALCHEMY_FORMULA = new ItemAlchemyFormula(new Item.Settings().maxCount(1));

	public static final Map<MateriaRank, Item> MATERIA_DUSTS = new HashMap<>();

	public static final Item ALKAHEST_BUCKET = new BucketItem(AoAFluids.ALKAHEST, AoAItems.defaults().maxCount(1));
	public static final Map<Essentia, Item> ESSENTIA_BUCKETS = new HashMap<>();

	public static Settings defaults() {
		return new Item.Settings().group(ArtOfAlchemy.ALCHEMY_GROUP);
	}

	public static Item register(final Identifier id, final Item item) {
		return Registry.register(Registry.ITEM, id, item);
	}

	public static Item register(final String name, final Item item) {
		return AoAItems.register(ArtOfAlchemy.id(name), item);
	}

	public static void registerItems() {
		AoAItems.register("icon_item", AoAItems.MYSTERIOUS_SIGIL);
		AoAItems.register("alchemical_journal", AoAItems.JOURNAL);
		AoAItems.register("alchemy_formula", AoAItems.ALCHEMY_FORMULA);

		AoAItems.register("essentia_port", AoAItems.ESSENTIA_PORT);
		AoAItems.register("essentia_inserter", AoAItems.ESSENTIA_INSERTER);
		AoAItems.register("essentia_extractor", AoAItems.ESSENTIA_EXTRACTOR);
		AoAItems.register("essentia_vessel", AoAItems.ESSENTIA_VESSEL);

		AoAItems.register("azoth", AoAItems.AZOTH);
		AoAItems.register("amaranth_pearl", AoAItems.AMARANTH_PEARL);
		AoAItems.register("cracked_amaranth_pearl", AoAItems.CRACKED_AMARANTH_PEARL);
		AoAItems.register("activated_amaranth_pearl", AoAItems.ACTIVATED_AMARANTH_PEARL);
		AoAItems.register("philosophers_stone", AoAItems.PHILOSOPHERS_STONE);

		// Register materia dusts
		for (final MateriaRank rank : MateriaRank.values()) {
			final String name = "materia_" + rank.toString().toLowerCase();
			AoAItems.MATERIA_DUSTS.put(rank, AoAItems.register(name, new ItemMateria(AoAItems.defaults(), rank)));
		}

		AoAItems.register("alkahest_bucket", AoAItems.ALKAHEST_BUCKET);

		// Register essentia buckets; add-on essentia buckets will be registered to
		// THEIR namespace
		RegistryEssentia.INSTANCE.forEach(new BiConsumer<Essentia, Identifier>() {
			@Override
			public void accept(final Essentia essentia, final Identifier id) {
				final Identifier itemId = new Identifier(id.getNamespace(), "essentia_bucket_" + id.getPath());
				AoAItems.ESSENTIA_BUCKETS.put(
					essentia,
					AoAItems
						.register(itemId, new BucketItem(AoAFluids.ESSENTIA_FLUIDS.get(essentia), AoAItems.defaults().maxCount(1)))
				);
			}
		});

	}
}
