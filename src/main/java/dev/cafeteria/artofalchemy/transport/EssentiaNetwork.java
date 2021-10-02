package dev.cafeteria.artofalchemy.transport;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import dev.cafeteria.artofalchemy.essentia.EssentiaContainer;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EssentiaNetwork {
	protected final World world;
	protected final Set<BlockPos> positions = new HashSet<>();
	protected final Set<NetworkNode> nodes = new HashSet<>();
	protected final Set<NetworkNode> pullers = new HashSet<>();
	protected final Set<NetworkNode> pushers = new HashSet<>();
	protected final Set<NetworkNode> passives = new HashSet<>();
	protected final UUID uuid = UUID.randomUUID();
	protected long lastTicked;
	protected boolean dirty;

	EssentiaNetwork(final World world) {
		this.world = world;
		this.lastTicked = world.getTime();
	}

	EssentiaNetwork(final World world, final NbtList tag) {
		this(world);
		this.fromTag(tag);
	}

	public boolean add(final BlockPos pos) {
		if (!this.positions.contains(pos)) {
			this.addNodes(pos);
			return this.positions.add(pos);
		} else {
			return false;
		}
	}

	public void addNodes(final BlockPos pos) {
		final Block block = this.world.getBlockState(pos).getBlock();
		if (block instanceof NetworkElement) {
			final Set<NetworkNode> newNodes = ((NetworkElement) block).getNodes(this.world, pos);
			for (final NetworkNode node : newNodes) {
				this.nodes.add(node);
				switch (node.getType()) {
					case PULL:
						this.pullers.add(node);
						break;
					case PUSH:
						this.pushers.add(node);
						break;
					case PASSIVE:
						this.passives.add(node);
						break;
				}
			}
		}
	}

	public boolean contains(final BlockPos pos) {
		return this.positions.contains(pos);
	}

	public void fromTag(final NbtList tag) {
		for (final NbtElement listElement : tag) {
			if (listElement instanceof final NbtList posTag) {
				final BlockPos pos = new BlockPos(posTag.getInt(0), posTag.getInt(1), posTag.getInt(2));
				if (this.world.getBlockState(pos).getBlock() instanceof NetworkElement) {
					this.add(pos);
				}
			}
		}
	}

	public Set<NetworkNode> getNodes() {
		return this.nodes;
	}

	public Set<BlockPos> getPositions() {
		return this.positions;
	}

	public int getSize() {
		return this.positions.size();
	}

	public UUID getUuid() {
		return this.uuid;
	}

	public World getWorld() {
		return this.world;
	}

	public void markDirty() {
		this.dirty = true;
	}

	public void rebuildNodes() {
		this.nodes.clear();
		for (final BlockPos pos : this.positions) {
			final Block block = this.world.getBlockState(pos).getBlock();
			if (block instanceof NetworkElement) {
				this.nodes.addAll(((NetworkElement) block).getNodes(this.world, pos));
			}
		}
		this.pullers.clear();
		this.pushers.clear();
		this.passives.clear();
		for (final NetworkNode node : this.nodes) {
			switch (node.getType()) {
				case PULL:
					this.pullers.add(node);
					break;
				case PUSH:
					this.pushers.add(node);
					break;
				case PASSIVE:
					this.passives.add(node);
					break;
			}
		}
	}

	public boolean remove(final BlockPos pos) {
		if (this.positions.contains(pos)) {
			this.removeNodes(pos);
			return this.positions.remove(pos);
		} else {
			return false;
		}
	}

	public void removeNodes(final BlockPos pos) {
		this.nodes.removeIf(node -> node.getPos().equals(pos));
		this.pullers.removeIf(node -> node.getPos().equals(pos));
		this.pushers.removeIf(node -> node.getPos().equals(pos));
		this.passives.removeIf(node -> node.getPos().equals(pos));
	}

	public void tick() {
		if (this.dirty) {
			this.rebuildNodes();
			this.dirty = false;
		}

		if (this.world.getTime() < (this.lastTicked + 5)) {
			return;
		}
		this.lastTicked = this.world.getTime();
		this.nodes.forEach(NetworkNode::checkBlockEntity); // KG: This should be run as irregularly as possible. Ideally
																												// node would listen for nearby updates and update on demand
																												// there.

		for (final NetworkNode pusher : this.pushers) {
			for (final NetworkNode puller : this.pullers) {
				this.transfer(pusher, puller);
			}
			for (final NetworkNode passive : this.passives) {
				this.transfer(pusher, passive);
			}
		}
		for (final NetworkNode puller : this.pullers) {
			for (final NetworkNode passive : this.passives) {
				this.transfer(passive, puller);
			}
		}
	}

	public NbtList toTag() {
		final NbtList tag = new NbtList();
		for (final BlockPos pos : this.positions) {
			final NbtList posTag = new NbtList();
			posTag.add(NbtInt.of(pos.getX()));
			posTag.add(NbtInt.of(pos.getY()));
			posTag.add(NbtInt.of(pos.getZ()));
			tag.add(posTag);
		}
		return tag;
	}

	public void transfer(final NetworkNode from, final NetworkNode to) {
		final BlockEntity fromBE = from.getBlockEntity();
		final BlockEntity toBE = to.getBlockEntity();
		if ((fromBE instanceof final HasEssentia fromEssenceBE) && (toBE instanceof HasEssentia toEssenceBE)) {
			for (int i = 0; i < fromEssenceBE.getNumContainers(); i++) {
				EssentiaContainer fromContainer;
				if (from.getDirection().isPresent()) {
					fromContainer = fromEssenceBE.getContainer(from.getDirection().get().getOpposite());
				} else {
					fromContainer = fromEssenceBE.getContainer();
				}
				for (int j = 0; j < toEssenceBE.getNumContainers(); j++) {
					EssentiaContainer toContainer;
					if (to.getDirection().isPresent()) {
						toContainer = toEssenceBE.getContainer(to.getDirection().get().getOpposite());
					} else {
						toContainer = toEssenceBE.getContainer();
					}
					fromContainer.pushContents(toContainer);
				}
			}
			fromBE.markDirty();
			toBE.markDirty();
		}
	}

}
