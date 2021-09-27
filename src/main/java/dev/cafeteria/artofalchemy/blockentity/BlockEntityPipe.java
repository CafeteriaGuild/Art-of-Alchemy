package dev.cafeteria.artofalchemy.blockentity;

import java.util.HashMap;
import java.util.Map;

import dev.cafeteria.artofalchemy.transport.NetworkNode;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

public class BlockEntityPipe extends BlockEntity implements BlockEntityClientSerializable, RenderAttachmentBlockEntity {

	private Map<Direction, IOFace> faces = new HashMap<>();

	public BlockEntityPipe(BlockPos pos, BlockState state) {
		super(AoABlockEntities.PIPE, pos, state);
		for (Direction dir : Direction.values()) {
			faces.put(dir, IOFace.NONE);
		}
	}

	@Override
	public NbtCompound writeNbt(NbtCompound tag) {
		for (Direction dir : Direction.values()) {
			tag.putString(dir.toString(), faces.get(dir).toString());
		}
		return super.writeNbt(tag);
	}

	@Override
	public void readNbt(NbtCompound tag) {
		super.readNbt(tag);
		for (Direction dir : Direction.values()) {
			faces.put(dir, IOFace.valueOf(tag.getString(dir.toString())));
		}
	}

	public Map<Direction, IOFace> getFaces() {
		return faces;
	}

	public void setFaces(Map<Direction, IOFace> faces) {
		this.faces = faces;
	}

	public IOFace getFace(Direction dir) {
		return faces.get(dir);
	}

	public void setFace(Direction dir, IOFace face) {
		faces.put(dir, face);
	}

	@Override
	public void fromClientTag(NbtCompound tag) {
		readNbt(tag);
	}

	@Override
	public NbtCompound toClientTag(NbtCompound tag) {
		return writeNbt(tag);
	}

	@Override
	public void sync() {
		BlockEntityClientSerializable.super.sync();
	}

	@Override
	public Object getRenderAttachmentData() {
		assert Direction.values().length == 6;
		IOFace[] faceConfig = new IOFace[6];
		for (final Map.Entry<Direction, IOFace> entry : faces.entrySet()) {
			faceConfig[entry.getKey().ordinal()] = entry.getValue();
		}
		return faceConfig;
	}

	// Convenience function which performs the correct downcast of the Object returned
	// in getRenderAttachmentData()
	public static IOFace[] getRenderAttachedFaceConfig(BlockRenderView blockRenderView, BlockPos blockPos) {
		RenderAttachedBlockView renderAttachedBlockView = ((RenderAttachedBlockView) blockRenderView);
		Object renderAttachment = renderAttachedBlockView.getBlockEntityRenderAttachment(blockPos);
		return (IOFace[]) renderAttachment;
	}

	public enum IOFace implements StringIdentifiable {
		NONE,
		CONNECT,
		BLOCK,
		INSERTER(NetworkNode.Type.PULL),
		EXTRACTOR(NetworkNode.Type.PUSH),
		PASSIVE(NetworkNode.Type.PASSIVE);

		private final String string;
		private final NetworkNode.Type type;

		IOFace() {
			this(null);
		}

		IOFace(NetworkNode.Type type) {
			this.string = toString().toLowerCase();
			this.type = type;
		}

		public NetworkNode.Type getType() {
			return type;
		}

		public boolean isNode() {
			return type != null;
		}

		@Override
		public String asString() {
			return string;
		}
	}

}
