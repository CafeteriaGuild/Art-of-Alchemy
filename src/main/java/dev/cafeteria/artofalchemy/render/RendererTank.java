package dev.cafeteria.artofalchemy.render;

import java.util.Collection;

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
import net.minecraft.state.property.Property;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

// Shoutouts to 2xsaiko

public class RendererTank implements BlockEntityRenderer<BlockEntityTank> {

	MinecraftClient client = MinecraftClient.getInstance();

	public RendererTank(BlockEntityRendererFactory.Context ctx) {
	}

	@Override
	public void render(BlockEntityTank blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		Matrix4f model = matrices.peek().getModel();
		Matrix3f normal = matrices.peek().getNormal();
		VertexConsumer buffer;
		if (client.options.graphicsMode == GraphicsMode.FABULOUS) {
			// Translucent-layer quads don't show up under Fabulous settings, so here's a hacky workaround :)
			buffer = vertexConsumers.getBuffer(RenderLayer.getTranslucentMovingBlock());
		} else {
			buffer = vertexConsumers.getBuffer(RenderLayer.getTranslucent());
		}
		Sprite sprite = client.getBlockRenderManager().getModel(AoABlocks.ALKAHEST.getDefaultState()).getSprite();
		BlockState state = blockEntity.getWorld().getBlockState(blockEntity.getPos());
		Collection<Property<?>> properties = state.getProperties();
		EssentiaContainer container = blockEntity.getContainer();

		if (container != null && !blockEntity.getContainer().isEmpty()) {
			World world = blockEntity.getWorld();
			boolean connectedTop = world.getBlockState(blockEntity.getPos().up()).getBlock() == AoABlocks.TANK;
			boolean connectedBottom = world.getBlockState(blockEntity.getPos().down()).getBlock() == AoABlocks.TANK;

			float halfWidth = 7f/16f;
			float min = 0.5f - halfWidth;
			float max = 0.5f + halfWidth;
			float minY = connectedBottom ? 0.0f : min;
			float maxY = connectedTop ? 1.0f : max;
			float midY = maxY;

			float minU = sprite.getMinU();
			float maxU = sprite.getMaxU();
			float minV = sprite.getMinV();
			float maxV = sprite.getMaxV();
			float midV = maxV;

			if (!container.isInfinite() && !container.hasUnlimitedCapacity()) {
				midY = minY + (maxY - minY) * (float) container.getCount() / container.getCapacity();
				midV = minV + (maxV - minV) * (float) container.getCount() / container.getCapacity();
			}

			Vec3i color = AoAHelper.integerColor(blockEntity.getContainer().getColor());
			int r = color.getX();
			int g = color.getY();
			int b = color.getZ();
			int a = 255;

			buffer.vertex(model, min, minY, min).color(r, g, b, a).texture(minU, minV).light(light).normal(normal, 0.0f, 0.0f, -1.0f).next();
			buffer.vertex(model, min, midY, min).color(r, g, b, a).texture(minU, midV).light(light).normal(normal, 0.0f, 0.0f, -1.0f).next();
			buffer.vertex(model, max, midY, min).color(r, g, b, a).texture(maxU, midV).light(light).normal(normal, 0.0f, 0.0f, -1.0f).next();
			buffer.vertex(model, max, minY, min).color(r, g, b, a).texture(maxU, minV).light(light).normal(normal, 0.0f, 0.0f, -1.0f).next();

			buffer.vertex(model, min, minY, min).color(r, g, b, a).texture(minU, minV).light(light).normal(normal, -1.0f, 0.0f, 0.0f).next();
			buffer.vertex(model, min, minY, max).color(r, g, b, a).texture(maxU, minV).light(light).normal(normal, -1.0f, 0.0f, 0.0f).next();
			buffer.vertex(model, min, midY, max).color(r, g, b, a).texture(maxU, midV).light(light).normal(normal, -1.0f, 0.0f, 0.0f).next();
			buffer.vertex(model, min, midY, min).color(r, g, b, a).texture(minU, midV).light(light).normal(normal, -1.0f, 0.0f, 0.0f).next();

			buffer.vertex(model, min, minY, max).color(r, g, b, a).texture(minU, minV).light(light).normal(normal, 0.0f, 0.0f, 1.0f).next();
			buffer.vertex(model, max, minY, max).color(r, g, b, a).texture(maxU, minV).light(light).normal(normal, 0.0f, 0.0f, 1.0f).next();
			buffer.vertex(model, max, midY, max).color(r, g, b, a).texture(maxU, midV).light(light).normal(normal, 0.0f, 0.0f, 1.0f).next();
			buffer.vertex(model, min, midY, max).color(r, g, b, a).texture(minU, midV).light(light).normal(normal, 0.0f, 0.0f, 1.0f).next();

			buffer.vertex(model, max, minY, min).color(r, g, b, a).texture(minU, minV).light(light).normal(normal, 1.0f, 0.0f, 0.0f).next();
			buffer.vertex(model, max, midY, min).color(r, g, b, a).texture(minU, midV).light(light).normal(normal, 1.0f, 0.0f, 0.0f).next();
			buffer.vertex(model, max, midY, max).color(r, g, b, a).texture(maxU, midV).light(light).normal(normal, 1.0f, 0.0f, 0.0f).next();
			buffer.vertex(model, max, minY, max).color(r, g, b, a).texture(maxU, minV).light(light).normal(normal, 1.0f, 0.0f, 0.0f).next();

			if (!connectedBottom) {
				buffer.vertex(model, min, minY, min).color(r, g, b, a).texture(minU, minV).light(light).normal(normal, 0.0f, -1.0f, 0.0f).next();
				buffer.vertex(model, max, minY, min).color(r, g, b, a).texture(maxU, minV).light(light).normal(normal, 0.0f, -1.0f, 0.0f).next();
				buffer.vertex(model, max, minY, max).color(r, g, b, a).texture(maxU, maxV).light(light).normal(normal, 0.0f, -1.0f, 0.0f).next();
				buffer.vertex(model, min, minY, max).color(r, g, b, a).texture(minU, maxV).light(light).normal(normal, 0.0f, -1.0f, 0.0f).next();
			}

			boolean renderBottom = true;
			BlockEntity topBE = world.getBlockEntity(blockEntity.getPos().up());
			if (topBE instanceof BlockEntityTank) {
				if (!((BlockEntityTank) topBE).getContainer().isEmpty()) {
					renderBottom = false;
				}
			}
			if (renderBottom) {
				buffer.vertex(model, min, midY, min).color(r, g, b, a).texture(minU, minV).light(light).normal(normal, 0.0f, -1.0f, 0.0f).next();
				buffer.vertex(model, min, midY, max).color(r, g, b, a).texture(minU, maxV).light(light).normal(normal, 0.0f, -1.0f, 0.0f).next();
				buffer.vertex(model, max, midY, max).color(r, g, b, a).texture(maxU, maxV).light(light).normal(normal, 0.0f, -1.0f, 0.0f).next();
				buffer.vertex(model, max, midY, min).color(r, g, b, a).texture(maxU, minV).light(light).normal(normal, 0.0f, -1.0f, 0.0f).next();
			}
		}
	}

}
