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

	public static Block register(final String name, final Block block) {
		AoAItems.register(name, new BlockItem(block, AoAItems.defaults()));
		return AoABlocks.registerItemless(name, block);
	}

	public static void registerBlocks() {
		AoABlocks.register("analysis_desk", AoABlocks.ANALYZER);
		AoABlocks.register("calcination_furnace", AoABlocks.CALCINATOR);
		AoABlocks.register("dissolution_chamber", AoABlocks.DISSOLVER);
		AoABlocks.register("distillation_apparatus", AoABlocks.DISTILLER);
		AoABlocks.register("synthesis_table", AoABlocks.SYNTHESIZER);
		AoABlocks.register("projection_altar", AoABlocks.PROJECTOR);
		AoABlocks.register("calcination_furnace_plus", AoABlocks.CALCINATOR_PLUS);
		AoABlocks.register("dissolution_chamber_plus", AoABlocks.DISSOLVER_PLUS);
		AoABlocks.register("synthesis_table_plus", AoABlocks.SYNTHESIZER_PLUS);
		AoABlocks.register("astrological_centrifuge", AoABlocks.ASTRO_CENTRIFUGE);
		AoABlocks.register("elemental_centrifuge", AoABlocks.ELEMENT_CENTRIFUGE);
		AoABlocks.register("essentia_tank", AoABlocks.TANK);
		AoABlocks.register("essentia_pipe", AoABlocks.PIPE);

		AoABlocks.registerItemless("alkahest", AoABlocks.ALKAHEST);

		// Register materia dusts
		for (final MateriaRank rank : MateriaRank.values()) {
			final String name = "materia_block_" + rank.toString().toLowerCase();
			final BlockMateria block = new BlockMateria(rank);
			AoABlocks.MATERIA_BLOCKS.put(rank, AoABlocks.registerItemless(name, block));
			AoAItems.register(name, new BlockItemMateria(block, AoAItems.defaults()));
		}

		// Register essentia fluid blocks; add-on essentia fluids will be registered to
		// THEIR namespace
		RegistryEssentia.INSTANCE.forEach((essentia, id) -> {
			final Identifier blockId = new Identifier(id.getNamespace(), "essentia_" + id.getPath());
			AoABlocks.ESSENTIA.put(essentia, AoABlocks.registerItemless(blockId, new BlockEssentia(essentia)));
		});
	}

	public static Block registerItemless(final Identifier id, final Block block) {
		return Registry.register(Registry.BLOCK, id, block);
	}

	public static Block registerItemless(final String name, final Block block) {
		return AoABlocks.registerItemless(ArtOfAlchemy.id(name), block);
	}

}
