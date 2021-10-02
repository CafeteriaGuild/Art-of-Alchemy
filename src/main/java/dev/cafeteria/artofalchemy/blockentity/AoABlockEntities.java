package dev.cafeteria.artofalchemy.blockentity;

import dev.cafeteria.artofalchemy.ArtOfAlchemy;
import dev.cafeteria.artofalchemy.block.AoABlocks;
import dev.cafeteria.artofalchemy.transport.HasAlkahest;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;

@SuppressWarnings("deprecation") // Experimental API
public class AoABlockEntities {

	public static final BlockEntityType<?> CALCINATOR = FabricBlockEntityTypeBuilder
		.create(BlockEntityCalcinator::new, AoABlocks.CALCINATOR).build(null);
	public static final BlockEntityType<?> CALCINATOR_PLUS = FabricBlockEntityTypeBuilder
		.create(BlockEntityCalcinatorPlus::new, AoABlocks.CALCINATOR_PLUS).build(null);
	public static final BlockEntityType<?> DISSOLVER = FabricBlockEntityTypeBuilder
		.create(BlockEntityDissolver::new, AoABlocks.DISSOLVER).build(null);
	public static final BlockEntityType<?> DISSOLVER_PLUS = FabricBlockEntityTypeBuilder
		.create(BlockEntityDissolverPlus::new, AoABlocks.DISSOLVER_PLUS).build(null);
	public static final BlockEntityType<?> DISTILLER = FabricBlockEntityTypeBuilder
		.create(BlockEntityDistiller::new, AoABlocks.DISTILLER).build(null);
	public static final BlockEntityType<?> SYNTHESIZER = FabricBlockEntityTypeBuilder
		.create(BlockEntitySynthesizer::new, AoABlocks.SYNTHESIZER).build(null);
	public static final BlockEntityType<?> SYNTHESIZER_PLUS = FabricBlockEntityTypeBuilder
		.create(BlockEntitySynthesizerPlus::new, AoABlocks.SYNTHESIZER_PLUS).build(null);
	public static final BlockEntityType<?> PROJECTOR = FabricBlockEntityTypeBuilder
		.create(BlockEntityProjector::new, AoABlocks.PROJECTOR).build(null);
	public static final BlockEntityType<BlockEntityTank> TANK = FabricBlockEntityTypeBuilder
		.create(BlockEntityTank::new, AoABlocks.TANK).build(null);
	public static final BlockEntityType<?> ASTRO_CENTRIFUGE = FabricBlockEntityTypeBuilder
		.create(BlockEntityAstroCentrifuge::new, AoABlocks.ASTRO_CENTRIFUGE).build(null);
	public static final BlockEntityType<?> ELEMENT_CENTRIFUGE = FabricBlockEntityTypeBuilder
		.create(BlockEntityElementCentrifuge::new, AoABlocks.ELEMENT_CENTRIFUGE).build(null);
	public static final BlockEntityType<?> PIPE = FabricBlockEntityTypeBuilder
		.create(BlockEntityPipe::new, AoABlocks.PIPE).build(null);

	public static void register(final String name, final BlockEntityType<? extends BlockEntity> blockEntity) {
		Registry.register(Registry.BLOCK_ENTITY_TYPE, ArtOfAlchemy.id(name), blockEntity);
	}

	public static void registerBlockEntities() {
		AoABlockEntities.register("calcination_furnace", AoABlockEntities.CALCINATOR);
		AoABlockEntities.register("calcination_furnace_plus", AoABlockEntities.CALCINATOR_PLUS);
		AoABlockEntities.register("dissolution_chamber", AoABlockEntities.DISSOLVER);
		AoABlockEntities.register("dissolution_chamber_plus", AoABlockEntities.DISSOLVER_PLUS);
		AoABlockEntities.register("distillation_apparatus", AoABlockEntities.DISTILLER);
		AoABlockEntities.register("synthesis_table", AoABlockEntities.SYNTHESIZER);
		AoABlockEntities.register("synthesis_table_plus", AoABlockEntities.SYNTHESIZER_PLUS);
		AoABlockEntities.register("projector", AoABlockEntities.PROJECTOR);
		AoABlockEntities.register("essentia_tank", AoABlockEntities.TANK);
		AoABlockEntities.register("astral_centrifuge", AoABlockEntities.ASTRO_CENTRIFUGE);
		AoABlockEntities.register("elemental_centrifuge", AoABlockEntities.ELEMENT_CENTRIFUGE);
		AoABlockEntities.register("pipe", AoABlockEntities.PIPE);

		BlockEntityType[] alkahestBEs = {
			AoABlockEntities.DISSOLVER, AoABlockEntities.DISSOLVER_PLUS, AoABlockEntities.DISTILLER,
			AoABlockEntities.PROJECTOR
		};
		for (BlockEntityType<? extends BlockEntity> alkahestBE : alkahestBEs)
			FluidStorage.SIDED.registerForBlockEntity((be, dir) -> ((HasAlkahest) be).getAlkahestTank(), alkahestBE);
	}

}
