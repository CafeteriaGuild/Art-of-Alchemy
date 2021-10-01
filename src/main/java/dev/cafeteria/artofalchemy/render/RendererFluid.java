package dev.cafeteria.artofalchemy.render;

import java.util.function.Function;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockRenderView;

@Environment(EnvType.CLIENT)
public class RendererFluid {

	public static void markTranslucent(final Fluid... fluids) {
		BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), fluids);
	}

	public static void setupFluidRendering(final Fluid still, final Fluid flowing, final Identifier texture) {
		RendererFluid.setupFluidRendering(still, flowing, texture, -1);
	}

	public static void setupFluidRendering(
		final Fluid still, final Fluid flowing, final Identifier texture, final int color
	) {
		final Identifier stillTexture = new Identifier(texture.getNamespace(), "block/" + texture.getPath() + "_still");
		final Identifier flowTexture = new Identifier(texture.getNamespace(), "block/" + texture.getPath() + "_flow");

		ClientSpriteRegistryCallback.event(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).register((atlas, registry) -> {
			registry.register(stillTexture);
			registry.register(flowTexture);
		});

		final Identifier fluidId = Registry.FLUID.getId(still);
		final Identifier listenerId = new Identifier(fluidId.getNamespace(), fluidId.getPath() + "_reload_listener");
		final Sprite[] sprites = {
			null, null
		};

		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
			.registerReloadListener(new SimpleSynchronousResourceReloadListener() {

				@Override
				public Identifier getFabricId() {
					return listenerId;
				}

				@Override
				public void reload(final ResourceManager manager) {
					final Function<Identifier, Sprite> atlas = MinecraftClient.getInstance()
						.getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
					sprites[0] = atlas.apply(stillTexture);
					sprites[1] = atlas.apply(flowTexture);
				}

			});

		final FluidRenderHandler renderHandler = new FluidRenderHandler() {

			@Override
			public int getFluidColor(final BlockRenderView view, final BlockPos pos, final FluidState state) {
				return color;
			}

			@Override
			public Sprite[] getFluidSprites(final BlockRenderView view, final BlockPos pos, final FluidState state) {
				return sprites;
			}

		};

		FluidRenderHandlerRegistry.INSTANCE.register(still, renderHandler);
		FluidRenderHandlerRegistry.INSTANCE.register(flowing, renderHandler);
	}
}
