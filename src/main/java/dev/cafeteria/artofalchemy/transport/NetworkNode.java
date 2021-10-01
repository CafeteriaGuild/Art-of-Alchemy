package dev.cafeteria.artofalchemy.transport;

import java.util.Optional;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class NetworkNode {

	public enum Type implements StringIdentifiable {
		PULL, PUSH, PASSIVE;

		private final String string;

		Type() {
			this.string = this.toString().toLowerCase();
		}

		@Override
		public String asString() {
			return this.string;
		}
	}

	private final World world;
	private final NetworkNode.Type type;
	private final BlockPos pos;
	private BlockEntity blockEntity;

	private final Direction dir;

	public NetworkNode(final World world, final Type type, final BlockPos pos) {
		this(world, type, pos, null);
	}

	public NetworkNode(final World world, final Type type, final BlockPos pos, final Direction dir) {
		this.world = world;
		this.type = type;
		this.pos = pos;
		this.dir = dir;
		updateBlockEntity();
	}

	public void checkBlockEntity() {
		if (this.blockEntity == null || this.blockEntity.isRemoved())
			this.updateBlockEntity();
	}

	public void updateBlockEntity() {
		this.blockEntity = this.world.getBlockEntity(this.dir == null ? this.pos : this.pos.offset(this.dir));
	}

	public BlockEntity getBlockEntity() {
		return this.blockEntity;
	}

	public Optional<Direction> getDirection() {
		return Optional.of(this.dir);
	}

	public BlockPos getPos() {
		return this.pos;
	}

	public NetworkNode.Type getType() {
		return this.type;
	}

}
