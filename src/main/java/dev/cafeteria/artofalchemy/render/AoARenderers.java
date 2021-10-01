package dev.cafeteria.artofalchemy.render;

import java.util.function.Consumer;
import java.util.function.Function;

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
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.mixin.object.builder.ModelPredicateProviderRegistrySpecificAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.client.item.UnclampedModelPredicateProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockRenderView;

@Environment(EnvType.CLIENT)
public class AoARenderers {

	@Environment(EnvType.CLIENT)
	public static void registerRenderers() {
		BlockEntityRendererRegistry.register(AoABlockEntities.TANK, RendererTank::new);
		BlockRenderLayerMap.INSTANCE.putBlock(AoABlocks.TANK, RenderLayer.getCutout());

		RendererFluid.setupFluidRendering(
			AoAFluids.ALKAHEST,
			AoAFluids.ALKAHEST_FLOWING,
			new Identifier("minecraft", "water"),
			0xAA0077
		);
		RendererFluid.markTranslucent(AoAFluids.ALKAHEST, AoAFluids.ALKAHEST_FLOWING);

		RegistryEssentia.INSTANCE.forEach(new Consumer<Essentia>() {
			@Override
			public void accept(final Essentia essentia) {
				final Fluid still = AoAFluids.ESSENTIA_FLUIDS.get(essentia);
				final Fluid flowing = AoAFluids.ESSENTIA_FLUIDS_FLOWING.get(essentia);
				RendererFluid.setupFluidRendering(still, flowing, new Identifier("minecraft", "water"), essentia.getColor());
				RendererFluid.markTranslucent(still, flowing);
			}
		});

		ColorProviderRegistry.BLOCK.register(new BlockColorProvider() {
			@Override
			public int getColor(final BlockState state, final BlockRenderView view, final BlockPos pos, final int tintIndex) {
				return 0xAA0077;
			}
		}, AoABlocks.DISSOLVER, AoABlocks.DISSOLVER_PLUS);

		ColorProviderRegistry.ITEM.register(new ItemColorProvider() {
			@Override
			public int getColor(final ItemStack stack, final int tintIndex) {
				if (tintIndex == 0) {
					return ItemEssentiaVessel.getColor(stack);
				} else {
					return 0xFFFFFF;
				}
			}
		}, AoAItems.ESSENTIA_VESSEL);

		ModelPredicateProviderRegistrySpecificAccessor
			.callRegister(AoAItems.ESSENTIA_VESSEL, new Identifier("level"), new UnclampedModelPredicateProvider() {
				@Override
				public float unclampedCall(
					final ItemStack stack, final ClientWorld world, final LivingEntity entity, final int seed
				) {
					final EssentiaContainer contents = ItemEssentiaVessel.getContainer(stack);
					double level = contents.getCount();
					if (!contents.hasUnlimitedCapacity()) {
						level /= contents.getCapacity();
					}
					return (float) MathHelper.clamp(level, 0.0, 1.0);
				}
			});

		final Identifier PIPE_MODEL = new Identifier("artofalchemy", "block/essentia_pipe_core_dynamic");
		ModelLoadingRegistry.INSTANCE.registerResourceProvider(new Function<ResourceManager, ModelResourceProvider>() {
			@Override
			public ModelResourceProvider apply(final ResourceManager rm) {
				return new ModelResourceProvider() {
					@Override
					public UnbakedModel loadModelResource(
						final Identifier identifier, final ModelProviderContext modelProviderContext
					) throws ModelProviderException {
						if (identifier.equals(PIPE_MODEL)) {
							return new ModelPipe();
						} else {
							return null;
						}
					}
				};
			}
		});
	}

}
