package dev.cafeteria.artofalchemy.render;

import static dev.cafeteria.artofalchemy.item.AoAItems.ESSENTIA_VESSEL;

import dev.cafeteria.artofalchemy.block.AoABlocks;
import dev.cafeteria.artofalchemy.blockentity.AoABlockEntities;
import dev.cafeteria.artofalchemy.essentia.Essentia;
import dev.cafeteria.artofalchemy.essentia.EssentiaContainer;
import dev.cafeteria.artofalchemy.essentia.RegistryEssentia;
import dev.cafeteria.artofalchemy.fluid.AoAFluids;
import dev.cafeteria.artofalchemy.item.AoAItems;
import dev.cafeteria.artofalchemy.item.ItemEssentiaVessel;
import dev.cafeteria.artofalchemy.render.model.ModelPipe;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.mixin.object.builder.ModelPredicateProviderRegistrySpecificAccessor;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class AoARenderers {

	@Environment(EnvType.CLIENT)
	public static void registerRenderers() {
		BlockEntityRendererRegistry.INSTANCE.register(AoABlockEntities.TANK, RendererTank::new);
		BlockRenderLayerMap.INSTANCE.putBlock(AoABlocks.TANK, RenderLayer.getCutout());

		RendererFluid.setupFluidRendering(AoAFluids.ALKAHEST, AoAFluids.ALKAHEST_FLOWING,
				new Identifier("minecraft", "water"), 0xAA0077);
		RendererFluid.markTranslucent(AoAFluids.ALKAHEST, AoAFluids.ALKAHEST_FLOWING);

		RegistryEssentia.INSTANCE.forEach((Essentia essentia) -> {
			Fluid still = AoAFluids.ESSENTIA_FLUIDS.get(essentia);
			Fluid flowing = AoAFluids.ESSENTIA_FLUIDS_FLOWING.get(essentia);
			RendererFluid.setupFluidRendering(still, flowing, new Identifier("minecraft", "water"), essentia.getColor());
			RendererFluid.markTranslucent(still, flowing);
		});

		ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> 0xAA0077,
				AoABlocks.DISSOLVER, AoABlocks.DISSOLVER_PLUS);

		ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
			if (tintIndex == 0) {
				return ItemEssentiaVessel.getColor(stack);
			} else {
				return 0xFFFFFF;
			}
		}, ESSENTIA_VESSEL);

		ModelPredicateProviderRegistrySpecificAccessor.callRegister(AoAItems.ESSENTIA_VESSEL,
				new Identifier("level"), (stack, world, entity, seed) -> {
					EssentiaContainer contents = ItemEssentiaVessel.getContainer(stack);
					double level = contents.getCount();
					if (!contents.hasUnlimitedCapacity()) {
						level /= contents.getCapacity();
					}
					return (float) MathHelper.clamp(level, 0.0, 1.0);
				});

		final Identifier PIPE_MODEL = new Identifier("artofalchemy", "block/essentia_pipe_core_dynamic");
		ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm -> new ModelResourceProvider() {
			@Override
			public UnbakedModel loadModelResource(Identifier identifier, ModelProviderContext modelProviderContext) throws ModelProviderException {
				if(identifier.equals(PIPE_MODEL)) {
					return new ModelPipe();
				} else {
					return null;
				}
			}
		});
	}

}
