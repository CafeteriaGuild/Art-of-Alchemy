package dev.cafeteria.artofalchemy.network;

import java.util.Collection;

import dev.cafeteria.artofalchemy.ArtOfAlchemy;
import dev.cafeteria.artofalchemy.blockentity.BlockEntityPipe;
import dev.cafeteria.artofalchemy.essentia.EssentiaContainer;
import dev.cafeteria.artofalchemy.essentia.EssentiaStack;
import dev.cafeteria.artofalchemy.item.ItemJournal;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class AoANetworking {

	public static final Identifier ESSENTIA_PACKET = ArtOfAlchemy.id("update_essentia");
	public static final Identifier ESSENTIA_PACKET_REQ = ArtOfAlchemy.id("update_essentia_req");
	public static final Identifier JOURNAL_SELECT_PACKET = ArtOfAlchemy.id("journal_select");
	public static final Identifier JOURNAL_REFRESH_PACKET = ArtOfAlchemy.id("journal_refresh");
	public static final Identifier PIPE_FACE_UPDATE = ArtOfAlchemy.id("pipe_face_update");

	public static void initializeNetworking() {
		ServerPlayNetworking.registerGlobalReceiver(JOURNAL_SELECT_PACKET,
				(server, _player, handler, data, _sender) -> {
					Identifier id = data.readIdentifier();
					Hand hand = data.readEnumConstant(Hand.class);
					server.execute(() -> {
						ItemStack stack = handler.getPlayer().getStackInHand(hand);
						if (stack.getItem() instanceof ItemJournal) {
							ItemJournal.setFormula(stack, id);
							sendJournalRefreshPacket(handler.getPlayer(), stack);
						}
					});
				});
	}


	public static void sendEssentiaPacket(World world, BlockPos pos, int essentiaId, EssentiaContainer container) {
		Collection<ServerPlayerEntity> players = PlayerLookup.tracking((ServerWorld) world, pos);

		PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
		data.writeInt(essentiaId);
		data.writeNbt(container.writeNbt());
		data.writeBlockPos(pos);

		players.forEach(player -> ServerPlayNetworking.send(player, ESSENTIA_PACKET, data));
	}

	public static void sendEssentiaPacketWithRequirements(World world, BlockPos pos, int essentiaId,
			EssentiaContainer container, EssentiaStack required) {
		Collection<ServerPlayerEntity> players = PlayerLookup.tracking((ServerWorld) world, pos);

		PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
		data.writeInt(essentiaId);
		data.writeNbt(container.writeNbt());
		data.writeNbt(required.toTag());
		data.writeBlockPos(pos);

		players.forEach(player -> ServerPlayNetworking.send(player, ESSENTIA_PACKET_REQ, data));
	}

	public static void sendJournalRefreshPacket(PlayerEntity player, ItemStack journal) {
		PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
		data.writeItemStack(journal);
		ServerPlayNetworking.send((ServerPlayerEntity) player, JOURNAL_REFRESH_PACKET, data);
	}

	public static void sendPipeFaceUpdate(World world, BlockPos pos, Direction dir, BlockEntityPipe.IOFace face) {
		Collection<ServerPlayerEntity> players = PlayerLookup.tracking((ServerWorld) world, pos);

		PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
		data.writeEnumConstant(dir);
		data.writeEnumConstant(face);
		data.writeBlockPos(pos);

		players.forEach(player -> ServerPlayNetworking.send(player, PIPE_FACE_UPDATE, data));
	}
}
