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

	private final Direction dir;

	public NetworkNode(final World world, final Type type, final BlockPos pos) {
		this(world, type, pos, null);
	}

	public NetworkNode(final World world, final Type type, final BlockPos pos, final Direction dir) {
		this.world = world;
		this.type = type;
		this.pos = pos;
		this.dir = dir;
	}

	public BlockEntity getBlockEntity() {
		if (this.dir != null) {
			return this.world.getBlockEntity(this.pos.offset(this.dir));
		} else {
			return this.world.getBlockEntity(this.pos);
		}
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
