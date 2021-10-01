package dev.cafeteria.artofalchemy.network;

import dev.cafeteria.artofalchemy.block.BlockPipe;
import dev.cafeteria.artofalchemy.blockentity.BlockEntityPipe;
import dev.cafeteria.artofalchemy.essentia.EssentiaContainer;
import dev.cafeteria.artofalchemy.essentia.EssentiaStack;
import dev.cafeteria.artofalchemy.gui.screen.EssentiaScreen;
import dev.cafeteria.artofalchemy.gui.screen.ScreenJournal;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

@SuppressWarnings("deprecation")
@Environment(EnvType.CLIENT)
public class AoAClientNetworking {

	@Environment(EnvType.CLIENT)
	public static void initializeClientNetworking() {
		ClientSidePacketRegistry.INSTANCE.register(AoANetworking.ESSENTIA_PACKET, (ctx, data) -> {
			final int essentiaId = data.readInt();
			final NbtCompound tag = data.readNbt();
			final BlockPos pos = data.readBlockPos();
			ctx.getTaskQueue().execute(() -> {
				final EssentiaContainer container = new EssentiaContainer(tag);
				final MinecraftClient client = MinecraftClient.getInstance();
				final Screen screen = client.currentScreen;
				if (screen instanceof EssentiaScreen) {
					((EssentiaScreen) screen).updateEssentia(essentiaId, container, pos);
				}
			});
		});

		ClientSidePacketRegistry.INSTANCE.register(AoANetworking.ESSENTIA_PACKET_REQ, (ctx, data) -> {
			final int essentiaId = data.readInt();
			final NbtCompound essentiaTag = data.readNbt();
			final NbtCompound requiredTag = data.readNbt();
			final BlockPos pos = data.readBlockPos();
			ctx.getTaskQueue().execute(() -> {
				final EssentiaContainer container = new EssentiaContainer(essentiaTag);
				final EssentiaStack required = new EssentiaStack(requiredTag);
				final MinecraftClient client = MinecraftClient.getInstance();
				final Screen screen = client.currentScreen;
				if (screen instanceof EssentiaScreen) {
					((EssentiaScreen) screen).updateEssentia(essentiaId, container, required, pos);
				}
			});
		});

		ClientSidePacketRegistry.INSTANCE.register(AoANetworking.JOURNAL_REFRESH_PACKET, (ctx, data) -> {
			final ItemStack journal = data.readItemStack();
			ctx.getTaskQueue().execute(() -> {
				final MinecraftClient client = MinecraftClient.getInstance();
				final Screen screen = client.currentScreen;
				if (screen instanceof ScreenJournal) {
					((ScreenJournal) screen).refresh(journal);
				}
			});
		});

		ClientSidePacketRegistry.INSTANCE.register(AoANetworking.PIPE_FACE_UPDATE, (ctx, data) -> {
			final Direction dir = data.readEnumConstant(Direction.class);
			final BlockEntityPipe.IOFace face = data.readEnumConstant(BlockEntityPipe.IOFace.class);
			final BlockPos pos = data.readBlockPos();
			ctx.getTaskQueue().execute(() -> {
				final MinecraftClient client = MinecraftClient.getInstance();
				final World world = client.world;
				final BlockEntity be = world.getBlockEntity(pos);
				if (be instanceof BlockEntityPipe) {
					((BlockEntityPipe) be).setFace(dir, face);
					BlockPipe.scheduleChunkRebuild(world, pos);
				}
			});
		});
	}

	public static void sendJournalSelectPacket(final Identifier id, final Hand hand) {
		final PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
		data.writeIdentifier(id);
		data.writeEnumConstant(hand);
		ClientSidePacketRegistry.INSTANCE.sendToServer(AoANetworking.JOURNAL_SELECT_PACKET, data);
	}

}
