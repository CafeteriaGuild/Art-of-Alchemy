package dev.cafeteria.artofalchemy.render;

import dev.cafeteria.artofalchemy.block.AoABlocks;
import dev.cafeteria.artofalchemy.blockentity.BlockEntityTank;
import dev.cafeteria.artofalchemy.essentia.EssentiaContainer;
import dev.cafeteria.artofalchemy.util.AoAHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

// Shoutouts to 2xsaiko

public class RendererTank implements BlockEntityRenderer<BlockEntityTank> {

	MinecraftClient client = MinecraftClient.getInstance();

	public RendererTank(final BlockEntityRendererFactory.Context ctx) {
	}

	@Override
	public void render(
		final BlockEntityTank blockEntity, final float tickDelta, final MatrixStack matrices,
		final VertexConsumerProvider vertexConsumers, final int light, final int overlay
	) {
		final Matrix4f model = matrices.peek().getPositionMatrix();
		final Matrix3f normal = matrices.peek().getNormalMatrix();
		VertexConsumer buffer;
		if (this.client.options.graphicsMode == GraphicsMode.FABULOUS) {
			// Translucent-layer quads don't show up under Fabulous settings, so here's a
			// hacky workaround :)
			buffer = vertexConsumers.getBuffer(RenderLayer.getTranslucentMovingBlock());
		} else {
			buffer = vertexConsumers.getBuffer(RenderLayer.getTranslucent());
		}
		final Sprite sprite = this.client.getBlockRenderManager().getModel(AoABlocks.ALKAHEST.getDefaultState())
			.getParticleSprite();
		final BlockState state = blockEntity.getWorld().getBlockState(blockEntity.getPos());
		state.getProperties();
		final EssentiaContainer container = blockEntity.getContainer();

		if ((container != null) && !blockEntity.getContainer().isEmpty()) {
			final World world = blockEntity.getWorld();
			final boolean connectedTop = world.getBlockState(blockEntity.getPos().up()).getBlock() == AoABlocks.TANK;
			final boolean connectedBottom = world.getBlockState(blockEntity.getPos().down()).getBlock() == AoABlocks.TANK;

			final float halfWidth = 7f / 16f;
			final float min = 0.5f - halfWidth;
			final float max = 0.5f + halfWidth;
			final float minY = connectedBottom ? 0.0f : min;
			final float maxY = connectedTop ? 1.0f : max;
			float midY = maxY;

			final float minU = sprite.getMinU();
			final float maxU = sprite.getMaxU();
			final float minV = sprite.getMinV();
			final float maxV = sprite.getMaxV();
			float midV = maxV;

			if (!container.isInfinite() && !container.hasUnlimitedCapacity()) {
				midY = minY + (((maxY - minY) * container.getCount()) / container.getCapacity());
				midV = minV + (((maxV - minV) * container.getCount()) / container.getCapacity());
			}

			final Vec3i color = AoAHelper.integerColor(blockEntity.getContainer().getColor());
			final int r = color.getX();
			final int g = color.getY();
			final int b = color.getZ();
			final int a = 255;

			buffer.vertex(model, min, minY, min).color(r, g, b, a).texture(minU, minV).light(light)
				.normal(normal, 0.0f, 0.0f, -1.0f).next();
			buffer.vertex(model, min, midY, min).color(r, g, b, a).texture(minU, midV).light(light)
				.normal(normal, 0.0f, 0.0f, -1.0f).next();
			buffer.vertex(model, max, midY, min).color(r, g, b, a).texture(maxU, midV).light(light)
				.normal(normal, 0.0f, 0.0f, -1.0f).next();
			buffer.vertex(model, max, minY, min).color(r, g, b, a).texture(maxU, minV).light(light)
				.normal(normal, 0.0f, 0.0f, -1.0f).next();

			buffer.vertex(model, min, minY, min).color(r, g, b, a).texture(minU, minV).light(light)
				.normal(normal, -1.0f, 0.0f, 0.0f).next();
			buffer.vertex(model, min, minY, max).color(r, g, b, a).texture(maxU, minV).light(light)
				.normal(normal, -1.0f, 0.0f, 0.0f).next();
			buffer.vertex(model, min, midY, max).color(r, g, b, a).texture(maxU, midV).light(light)
				.normal(normal, -1.0f, 0.0f, 0.0f).next();
			buffer.vertex(model, min, midY, min).color(r, g, b, a).texture(minU, midV).light(light)
				.normal(normal, -1.0f, 0.0f, 0.0f).next();

			buffer.vertex(model, min, minY, max).color(r, g, b, a).texture(minU, minV).light(light)
				.normal(normal, 0.0f, 0.0f, 1.0f).next();
			buffer.vertex(model, max, minY, max).color(r, g, b, a).texture(maxU, minV).light(light)
				.normal(normal, 0.0f, 0.0f, 1.0f).next();
			buffer.vertex(model, max, midY, max).color(r, g, b, a).texture(maxU, midV).light(light)
				.normal(normal, 0.0f, 0.0f, 1.0f).next();
			buffer.vertex(model, min, midY, max).color(r, g, b, a).texture(minU, midV).light(light)
				.normal(normal, 0.0f, 0.0f, 1.0f).next();

			buffer.vertex(model, max, minY, min).color(r, g, b, a).texture(minU, minV).light(light)
				.normal(normal, 1.0f, 0.0f, 0.0f).next();
			buffer.vertex(model, max, midY, min).color(r, g, b, a).texture(minU, midV).light(light)
				.normal(normal, 1.0f, 0.0f, 0.0f).next();
			buffer.vertex(model, max, midY, max).color(r, g, b, a).texture(maxU, midV).light(light)
				.normal(normal, 1.0f, 0.0f, 0.0f).next();
			buffer.vertex(model, max, minY, max).color(r, g, b, a).texture(maxU, minV).light(light)
				.normal(normal, 1.0f, 0.0f, 0.0f).next();

			if (!connectedBottom) {
				buffer.vertex(model, min, minY, min).color(r, g, b, a).texture(minU, minV).light(light)
					.normal(normal, 0.0f, -1.0f, 0.0f).next();
				buffer.vertex(model, max, minY, min).color(r, g, b, a).texture(maxU, minV).light(light)
					.normal(normal, 0.0f, -1.0f, 0.0f).next();
				buffer.vertex(model, max, minY, max).color(r, g, b, a).texture(maxU, maxV).light(light)
					.normal(normal, 0.0f, -1.0f, 0.0f).next();
				buffer.vertex(model, min, minY, max).color(r, g, b, a).texture(minU, maxV).light(light)
					.normal(normal, 0.0f, -1.0f, 0.0f).next();
			}

			boolean renderBottom = true;
			final BlockEntity topBE = world.getBlockEntity(blockEntity.getPos().up());
			if ((topBE instanceof BlockEntityTank) && !((BlockEntityTank) topBE).getContainer().isEmpty()) {
				renderBottom = false;
			}
			if (renderBottom) {
				buffer.vertex(model, min, midY, min).color(r, g, b, a).texture(minU, minV).light(light)
					.normal(normal, 0.0f, -1.0f, 0.0f).next();
				buffer.vertex(model, min, midY, max).color(r, g, b, a).texture(minU, maxV).light(light)
					.normal(normal, 0.0f, -1.0f, 0.0f).next();
				buffer.vertex(model, max, midY, max).color(r, g, b, a).texture(maxU, maxV).light(light)
					.normal(normal, 0.0f, -1.0f, 0.0f).next();
				buffer.vertex(model, max, midY, min).color(r, g, b, a).texture(maxU, minV).light(light)
					.normal(normal, 0.0f, -1.0f, 0.0f).next();
			}
		}
	}

}
