package dev.cafeteria.artofalchemy.transport;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.logging.log4j.Level;

import dev.cafeteria.artofalchemy.AoAConfig;
import dev.cafeteria.artofalchemy.ArtOfAlchemy;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.Block;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.dimension.DimensionType;

// Thanks, 2xsaiko!
public class EssentiaNetworker extends PersistentState {
	public static EssentiaNetworker fromNbt(final ServerWorld world, final NbtCompound tag) {
		final EssentiaNetworker networker = new EssentiaNetworker(world);
		networker.readNbt(tag);
		return networker;
	}

	public static EssentiaNetworker get(final ServerWorld world) {
		return world.getPersistentStateManager().getOrCreate(new Function<NbtCompound, EssentiaNetworker>() {
			@Override
			public EssentiaNetworker apply(final NbtCompound tag) {
				return EssentiaNetworker.fromNbt(world, tag);
			}
		}, new Supplier<EssentiaNetworker>() {
			@Override
			public EssentiaNetworker get() {
				return new EssentiaNetworker(world);
			}
		}, EssentiaNetworker.getName(world.getDimension()));
	}

	public static String getName(final DimensionType dimension) {
		return "essentia" + dimension.getSuffix();
	}

	public final int processingLimit;
	protected final ServerWorld world;

	protected final Set<EssentiaNetwork> networks = new HashSet<>();
	protected final Set<BlockPos> orphans = new HashSet<>();

	protected int processed = 0;

	protected final Set<BlockPos> legacyOrphans = new HashSet<>();

	protected final Map<BlockPos, EssentiaNetwork> cache = new HashMap<>();

	public EssentiaNetworker(final ServerWorld world) {
		this.world = world;
		this.processingLimit = AoAConfig.get().networkProcessingLimit;
	}

	public void add(final BlockPos pos) {
		this.processed++;
		this.orphans.remove(pos);
		if (!this.getNetwork(pos).isPresent()) {
			// Otherwise, add it to any connected networks, creating a new one or merging if
			// necessary
			final EssentiaNetwork network = this.merge(this.getConnectedNetworks(pos).toArray(new EssentiaNetwork[0]));
			network.add(pos.toImmutable());
			this.cache.put(pos.toImmutable(), network);
			this.markDirty();
		}
	}

	public Set<EssentiaNetwork> getConnectedNetworks(final BlockPos pos) {
		final Set<EssentiaNetwork> connectedNetworks = new HashSet<>();
		for (final BlockPos other : this.getConnections(pos)) {
			final Optional<EssentiaNetwork> network = this.getNetwork(other);
			network.ifPresent(connectedNetworks::add);
		}
		return connectedNetworks;
	}

	public Set<BlockPos> getConnections(final BlockPos pos) {
		final Block block = this.world.getBlockState(pos).getBlock();
		if (block instanceof NetworkElement) {
			return ((NetworkElement) block).getConnections(this.world, pos);
		} else {
			return new HashSet<>();
		}
	}

	public Optional<EssentiaNetwork> getNetwork(final BlockPos pos) {
		{
			final EssentiaNetwork network = this.cache.get(pos);
			if (network != null && network.contains(pos)) {
				return Optional.of(network);
			}
		}
		for (final EssentiaNetwork network : this.networks) {
			if (network.contains(pos)) {
				this.cache.put(pos.toImmutable(), network);
				return Optional.of(network);
			}
		}
		return Optional.empty();
	}

	// Merges n networks (where n can be 0, thus creating a new, empty network.)
	// Warning: WILL NOT rebuild nodes automatically.
	public EssentiaNetwork merge(final EssentiaNetwork... networks) {
		if (networks.length == 1) {
			// If given one network, there's nothing to merge, so just return it
			return networks[0];
		} else {
			// If given 0 or 2+ networks, create a new network with all the positions of the
			// old ones and delete them
			final EssentiaNetwork mergedNetwork = new EssentiaNetwork(this.world);
			this.networks.add(mergedNetwork);
			for (final EssentiaNetwork network : networks) {
				mergedNetwork.getPositions().addAll(network.getPositions());
				for (final BlockPos pos : mergedNetwork.getPositions()) {
					this.cache.put(pos, mergedNetwork);
				}
				this.networks.remove(network);
			}
			mergedNetwork.rebuildNodes();
			this.markDirty();
			return mergedNetwork;
		}
	}

	public void readNbt(final NbtCompound tag) {
		final NbtList networkList = tag.getList("networks", NbtType.LIST);
		for (final NbtElement networkTag : networkList) {
			if (networkTag instanceof NbtList && ((NbtList) networkTag).size() > 0) {
				this.networks.add(new EssentiaNetwork(this.world, (NbtList) networkTag));
			}
		}
		final NbtList orphanList = tag.getList("orphans", NbtType.LIST);
		for (final NbtElement orphanTag : orphanList) {
			if (orphanTag instanceof final NbtList posTag) {
				final BlockPos pos = new BlockPos(posTag.getInt(0), posTag.getInt(1), posTag.getInt(2));
				this.orphans.add(pos.toImmutable());
			}
		}
		final NbtList legacyList = tag.getList("network_positions", NbtType.LIST);
		for (final NbtElement orphanTag : legacyList) {
			if (orphanTag instanceof final NbtList posTag) {
				final BlockPos pos = new BlockPos(posTag.getInt(0), posTag.getInt(1), posTag.getInt(2));
				this.legacyOrphans.add(pos.toImmutable());
			}
		}
		this.rebuildCache();
	}

	public void rebuildCache() {
		this.cache.clear();
		for (final EssentiaNetwork network : this.networks) {
			for (final BlockPos pos : network.getPositions()) {
				this.cache.put(pos.toImmutable(), network);
			}
		}
	}

	@Deprecated
	public void recursiveAdd(final BlockPos pos) {
		if (this.processed < this.processingLimit) {
			if (!this.getNetwork(pos).isPresent()) {
				this.add(pos.toImmutable());
				this.legacyOrphans.remove(pos);
				final Set<BlockPos> connections = this.getConnections(pos);
				connections.forEach(this::recursiveAdd);
			}
		} else {
			this.legacyOrphans.add(pos.toImmutable());
			ArtOfAlchemy.log(
				Level.WARN,
				"Reached essentia network processing limit at [" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "] in "
					+ this.world.getDimension().getSuffix()
			);
		}
	}

	public void remove(final BlockPos pos, final Set<BlockPos> connections) {
		this.processed++;
		this.getNetwork(pos).ifPresent(new Consumer<EssentiaNetwork>() {
			@Override
			public void accept(final EssentiaNetwork network) {
				EssentiaNetworker.this.cache.remove(pos);
				network.remove(pos);
				if (network.getSize() == 0 || connections.size() > 1) {
					for (final BlockPos netPos : network.getPositions()) {
						EssentiaNetworker.this.orphans.add(netPos.toImmutable());
						EssentiaNetworker.this.cache.remove(netPos);
					}
					EssentiaNetworker.this.networks.remove(network);
				}
				EssentiaNetworker.this.markDirty();
			}
		});
	}

	public void tick() {
		this.processed = 0;
		for (final BlockPos pos : new HashSet<>(this.orphans)) {
			if (this.processed < this.processingLimit) {
				this.add(pos.toImmutable());
			} else {
				break;
			}
		}
		for (final BlockPos pos : new HashSet<>(this.legacyOrphans)) {
			if (this.processed < this.processingLimit) {
				this.recursiveAdd(pos.toImmutable());
			} else {
				break;
			}
		}
		for (final EssentiaNetwork network : this.networks) {
			network.tick();
		}
	}

	@Override
	public NbtCompound writeNbt(final NbtCompound tag) {
		final NbtList networkList = new NbtList();
		for (final EssentiaNetwork network : this.networks) {
			if (network.getSize() > 0) {
				networkList.add(network.toTag());
			}
		}
		tag.put("networks", networkList);
		final NbtList orphanList = new NbtList();
		for (final BlockPos pos : this.orphans) {
			final NbtList posTag = new NbtList();
			posTag.add(NbtInt.of(pos.getX()));
			posTag.add(NbtInt.of(pos.getY()));
			posTag.add(NbtInt.of(pos.getZ()));
			orphanList.add(posTag);
		}
		tag.put("orphans", orphanList);
		return tag;
	}

}
