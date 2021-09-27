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
		ClientSidePacketRegistry.INSTANCE.register(AoANetworking.ESSENTIA_PACKET,
				(ctx, data) -> {
					int essentiaId = data.readInt();
					NbtCompound tag = data.readNbt();
					BlockPos pos = data.readBlockPos();
					ctx.getTaskQueue().execute(() -> {
						EssentiaContainer container = new EssentiaContainer(tag);
						MinecraftClient client = MinecraftClient.getInstance();
						Screen screen = client.currentScreen;
						if (screen instanceof EssentiaScreen) {
							((EssentiaScreen) screen).updateEssentia(essentiaId, container, pos);
						}
						client.close();
					});
				});

		ClientSidePacketRegistry.INSTANCE.register(AoANetworking.ESSENTIA_PACKET_REQ,
				(ctx, data) -> {
					int essentiaId = data.readInt();
					NbtCompound essentiaTag = data.readNbt();
					NbtCompound requiredTag = data.readNbt();
					BlockPos pos = data.readBlockPos();
					ctx.getTaskQueue().execute(() -> {
						EssentiaContainer container = new EssentiaContainer(essentiaTag);
						EssentiaStack required = new EssentiaStack(requiredTag);
						MinecraftClient client = MinecraftClient.getInstance();
						Screen screen = client.currentScreen;
						if (screen instanceof EssentiaScreen) {
							((EssentiaScreen) screen).updateEssentia(essentiaId, container, required, pos);
						}
						client.close();
					});
				});

		ClientSidePacketRegistry.INSTANCE.register(AoANetworking.JOURNAL_REFRESH_PACKET,
				(ctx, data) -> {
					ItemStack journal = data.readItemStack();
					ctx.getTaskQueue().execute(() -> {
						MinecraftClient client = MinecraftClient.getInstance();
						Screen screen = client.currentScreen;
						if (screen instanceof ScreenJournal) {
							((ScreenJournal) screen).refresh(journal);
						}
						client.close();
					});
				});

		ClientSidePacketRegistry.INSTANCE.register(AoANetworking.PIPE_FACE_UPDATE,
				(ctx, data) -> {
					Direction dir = data.readEnumConstant(Direction.class);
					BlockEntityPipe.IOFace face = data.readEnumConstant(BlockEntityPipe.IOFace.class);
					BlockPos pos = data.readBlockPos();
					ctx.getTaskQueue().execute(() -> {
						MinecraftClient client = MinecraftClient.getInstance();
						World world = client.world;
						BlockEntity be = world.getBlockEntity(pos);
						if (be instanceof BlockEntityPipe) {
							((BlockEntityPipe) be).setFace(dir, face);
							BlockPipe.scheduleChunkRebuild(world, pos);
						}
						client.close();
					});
				});
	}

	public static void sendJournalSelectPacket(Identifier id, Hand hand) {
		PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
		data.writeIdentifier(id);
		data.writeEnumConstant(hand);
		ClientSidePacketRegistry.INSTANCE.sendToServer(AoANetworking.JOURNAL_SELECT_PACKET, data);
	}

}
