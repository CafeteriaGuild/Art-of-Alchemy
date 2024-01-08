package dev.cafeteria.artofalchemy.blockentity;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import dev.cafeteria.artofalchemy.transport.NetworkNode;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

public class BlockEntityPipe extends BlockEntity implements RenderAttachmentBlockEntity {

	public enum IOFace implements StringIdentifiable {
		NONE, CONNECT, BLOCK, INSERTER(NetworkNode.Type.PULL), EXTRACTOR(NetworkNode.Type.PUSH),
		PASSIVE(NetworkNode.Type.PASSIVE);

		private final String string;
		private final NetworkNode.Type type;

		IOFace() {
			this(null);
		}

		IOFace(final NetworkNode.Type type) {
			this.string = this.toString().toLowerCase();
			this.type = type;
		}

		@Override
		public String asString() {
			return this.string;
		}

		public NetworkNode.Type getType() {
			return this.type;
		}

		public boolean isNode() {
			return this.type != null;
		}
	}

	// Convenience function which performs the correct downcast of the Object
	// returned
	// in getRenderAttachmentData()
	public static IOFace[] getRenderAttachedFaceConfig(final BlockRenderView blockRenderView, final BlockPos blockPos) {
		final RenderAttachedBlockView renderAttachedBlockView = (RenderAttachedBlockView) blockRenderView;
		final Object renderAttachment = renderAttachedBlockView.getBlockEntityRenderAttachment(blockPos);
		return (IOFace[]) renderAttachment;
	}

	private Map<Direction, IOFace> faces = new HashMap<>();

	public BlockEntityPipe(final BlockPos pos, final BlockState state) {
		super(AoABlockEntities.PIPE, pos, state);
		for (final Direction dir : Direction.values()) {
			this.faces.put(dir, IOFace.NONE);
		}
	}

	@Nullable
	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	public IOFace getFace(final Direction dir) {
		return this.faces.get(dir);
	}

	public Map<Direction, IOFace> getFaces() {
		return this.faces;
	}

	@Override
	public Object getRenderAttachmentData() {
		assert Direction.values().length == 6;
		final IOFace[] faceConfig = new IOFace[6];
		for (final Map.Entry<Direction, IOFace> entry : this.faces.entrySet()) {
			faceConfig[entry.getKey().ordinal()] = entry.getValue();
		}
		return faceConfig;
	}

	@Override
	public void readNbt(final NbtCompound tag) {
		super.readNbt(tag);
		for (final Direction dir : Direction.values()) {
			this.faces.put(dir, IOFace.valueOf(tag.getString(dir.toString())));
		}
	}

	public void setFace(final Direction dir, final IOFace face) {
		this.faces.put(dir, face);
	}

	public void setFaces(final Map<Direction, IOFace> faces) {
		this.faces = faces;
	}

	public void sync() {
		world.updateListeners(pos, world.getBlockState(pos), world.getBlockState(pos), Block.NOTIFY_LISTENERS);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt() {
		return createNbt();
	}

	@Override
	public void writeNbt(final NbtCompound tag) {
		for (final Direction dir : Direction.values()) {
			tag.putString(dir.toString(), this.faces.get(dir).toString());
		}
		super.writeNbt(tag);
	}

}
