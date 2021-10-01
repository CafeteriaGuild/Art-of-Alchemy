package dev.cafeteria.artofalchemy.network;

import java.util.Collection;
import java.util.function.Consumer;

import dev.cafeteria.artofalchemy.ArtOfAlchemy;
import dev.cafeteria.artofalchemy.blockentity.BlockEntityPipe;
import dev.cafeteria.artofalchemy.essentia.EssentiaContainer;
import dev.cafeteria.artofalchemy.essentia.EssentiaStack;
import dev.cafeteria.artofalchemy.item.ItemJournal;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayChannelHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
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
		ServerPlayNetworking.registerGlobalReceiver(AoANetworking.JOURNAL_SELECT_PACKET, new PlayChannelHandler() {
			@Override
			public void receive(
				final MinecraftServer server, final ServerPlayerEntity _player, final ServerPlayNetworkHandler handler,
				final PacketByteBuf data, final PacketSender _sender
			) {
				final Identifier id = data.readIdentifier();
				final Hand hand = data.readEnumConstant(Hand.class);
				server.execute(new Runnable() {
					@Override
					public void run() {
						final ItemStack stack = handler.getPlayer().getStackInHand(hand);
						if (stack.getItem() instanceof ItemJournal) {
							ItemJournal.setFormula(stack, id);
							AoANetworking.sendJournalRefreshPacket(handler.getPlayer(), stack);
						}
					}
				});
			}
		});
	}

	public static void sendEssentiaPacket(
		final World world, final BlockPos pos, final int essentiaId, final EssentiaContainer container
	) {
		final Collection<ServerPlayerEntity> players = PlayerLookup.tracking((ServerWorld) world, pos);

		final PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
		data.writeInt(essentiaId);
		data.writeNbt(container.writeNbt());
		data.writeBlockPos(pos);

		players.forEach(new Consumer<ServerPlayerEntity>() {
			@Override
			public void accept(final ServerPlayerEntity player) {
				ServerPlayNetworking.send(player, AoANetworking.ESSENTIA_PACKET, data);
			}
		});
	}

	public static void sendEssentiaPacketWithRequirements(
		final World world, final BlockPos pos, final int essentiaId, final EssentiaContainer container,
		final EssentiaStack required
	) {
		final Collection<ServerPlayerEntity> players = PlayerLookup.tracking((ServerWorld) world, pos);

		final PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
		data.writeInt(essentiaId);
		data.writeNbt(container.writeNbt());
		data.writeNbt(required.toTag());
		data.writeBlockPos(pos);

		players.forEach(new Consumer<ServerPlayerEntity>() {
			@Override
			public void accept(final ServerPlayerEntity player) {
				ServerPlayNetworking.send(player, AoANetworking.ESSENTIA_PACKET_REQ, data);
			}
		});
	}

	public static void sendJournalRefreshPacket(final PlayerEntity player, final ItemStack journal) {
		final PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
		data.writeItemStack(journal);
		ServerPlayNetworking.send((ServerPlayerEntity) player, AoANetworking.JOURNAL_REFRESH_PACKET, data);
	}

	public static void sendPipeFaceUpdate(
		final World world, final BlockPos pos, final Direction dir, final BlockEntityPipe.IOFace face
	) {
		final Collection<ServerPlayerEntity> players = PlayerLookup.tracking((ServerWorld) world, pos);

		final PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
		data.writeEnumConstant(dir);
		data.writeEnumConstant(face);
		data.writeBlockPos(pos);

		players.forEach(new Consumer<ServerPlayerEntity>() {
			@Override
			public void accept(final ServerPlayerEntity player) {
				ServerPlayNetworking.send(player, AoANetworking.PIPE_FACE_UPDATE, data);
			}
		});
	}
}
