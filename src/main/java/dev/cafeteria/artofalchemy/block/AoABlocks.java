package dev.cafeteria.artofalchemy.block;

import java.util.HashMap;
import java.util.Map;

import dev.cafeteria.artofalchemy.ArtOfAlchemy;
import dev.cafeteria.artofalchemy.essentia.Essentia;
import dev.cafeteria.artofalchemy.essentia.RegistryEssentia;
import dev.cafeteria.artofalchemy.item.AoAItems;
import dev.cafeteria.artofalchemy.item.BlockItemMateria;
import dev.cafeteria.artofalchemy.util.MateriaRank;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class AoABlocks {

	public static final Block ANALYZER = new BlockAnalyzer();
	public static final Block CALCINATOR = new BlockCalcinator();
	public static final Block DISSOLVER = new BlockDissolver();
	public static final Block DISTILLER = new BlockDistiller();
	public static final Block SYNTHESIZER = new BlockSynthesizer();
	public static final Block PROJECTOR = new BlockProjector();
	public static final Block CALCINATOR_PLUS = new BlockCalcinatorPlus();
	public static final Block DISSOLVER_PLUS = new BlockDissolverPlus();
	public static final Block SYNTHESIZER_PLUS = new BlockSynthesizerPlus();
	public static final Block ASTRO_CENTRIFUGE = new BlockAstroCentrifuge();
	public static final Block ELEMENT_CENTRIFUGE = new BlockElementCentrifuge();
	public static final Block PIPE = new BlockPipe();
	public static final Block TANK = new BlockTank();

	public static final Map<MateriaRank, Block> MATERIA_BLOCKS = new HashMap<>();

	public static final Block ALKAHEST = new BlockAlkahest();
	public static final Map<Essentia, Block> ESSENTIA = new HashMap<>();

	public static void registerBlocks() {
		register("analysis_desk", ANALYZER);
		register("calcination_furnace", CALCINATOR);
		register("dissolution_chamber", DISSOLVER);
		register("distillation_apparatus", DISTILLER);
		register("synthesis_table", SYNTHESIZER);
		register("projection_altar", PROJECTOR);
		register("calcination_furnace_plus", CALCINATOR_PLUS);
		register("dissolution_chamber_plus", DISSOLVER_PLUS);
		register("synthesis_table_plus", SYNTHESIZER_PLUS);
		register("astrological_centrifuge", ASTRO_CENTRIFUGE);
		register("elemental_centrifuge", ELEMENT_CENTRIFUGE);
		register("essentia_tank", TANK);
		register("essentia_pipe", PIPE);

		registerItemless("alkahest", ALKAHEST);

		// Register materia dusts
		for (MateriaRank rank : MateriaRank.values()) {
			String name = "materia_block_" + rank.toString().toLowerCase();
			BlockMateria block = new BlockMateria(rank);
			MATERIA_BLOCKS.put(rank, registerItemless(name, block));
			AoAItems.register(name, new BlockItemMateria(block, AoAItems.defaults()));
		}

		// Register essentia fluid blocks; add-on essentia fluids will be registered to THEIR namespace
		RegistryEssentia.INSTANCE.forEach((Essentia essentia, Identifier id) -> {
			Identifier blockId = new Identifier(id.getNamespace(), "essentia_" + id.getPath());
			ESSENTIA.put(essentia, registerItemless(blockId, new BlockEssentia(essentia)));
		});
	}


	public static Block register(String name, Block block) {
		AoAItems.register(name, new BlockItem(block, AoAItems.defaults()));
		return registerItemless(name, block);
	}

	public static Block registerItemless(String name, Block block) {
		return registerItemless(ArtOfAlchemy.id(name), block);
	}

	public static Block registerItemless(Identifier id, Block block) {
		return Registry.register(Registry.BLOCK, id, block);
	}

}
