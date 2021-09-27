package dev.cafeteria.artofalchemy.transport;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
	public final int processingLimit;
	protected final ServerWorld world;
	protected final Set<EssentiaNetwork> networks = new HashSet<>();
	protected final Set<BlockPos> orphans = new HashSet<>();
	protected int processed = 0;

	protected final Set<BlockPos> legacyOrphans = new HashSet<>();
	protected final Map<BlockPos, EssentiaNetwork> cache = new HashMap<>();

	public EssentiaNetworker(ServerWorld world) {
		super();
		this.world = world;
		processingLimit = AoAConfig.get().networkProcessingLimit;
	}

	public static EssentiaNetworker fromNbt(ServerWorld world, NbtCompound tag) {
		EssentiaNetworker networker = new EssentiaNetworker(world);
		networker.readNbt(tag);
		return networker;
	}

	public void readNbt(NbtCompound tag) {
		NbtList networkList = tag.getList("networks", NbtType.LIST);
		for (NbtElement networkTag : networkList) {
			if (networkTag instanceof NbtList && ((NbtList) networkTag).size() > 0) {
				networks.add(new EssentiaNetwork(world, (NbtList) networkTag));
			}
		}
		NbtList orphanList = tag.getList("orphans", NbtType.LIST);
		for (NbtElement orphanTag : orphanList) {
			if (orphanTag instanceof NbtList) {
				NbtList posTag = (NbtList) orphanTag;
				BlockPos pos = new BlockPos(posTag.getInt(0), posTag.getInt(1), posTag.getInt(2));
				orphans.add(pos.toImmutable());
			}
		}
		NbtList legacyList = tag.getList("network_positions", NbtType.LIST);
		for (NbtElement orphanTag : legacyList) {
			if (orphanTag instanceof NbtList) {
				NbtList posTag = (NbtList) orphanTag;
				BlockPos pos = new BlockPos(posTag.getInt(0), posTag.getInt(1), posTag.getInt(2));
				legacyOrphans.add(pos.toImmutable());
			}
		}
		rebuildCache();
	}

	@Override
	public NbtCompound writeNbt(NbtCompound tag) {
		NbtList networkList = new NbtList();
		for (EssentiaNetwork network : networks) {
			if (network.getSize() > 0) {
				networkList.add(network.toTag());
			}
		}
		tag.put("networks", networkList);
		NbtList orphanList = new NbtList();
		for (BlockPos pos : orphans) {
			NbtList posTag = new NbtList();
			posTag.add(NbtInt.of(pos.getX()));
			posTag.add(NbtInt.of(pos.getY()));
			posTag.add(NbtInt.of(pos.getZ()));
			orphanList.add(posTag);
		}
		tag.put("orphans", orphanList);
		return tag;
	}

	public void rebuildCache() {
		cache.clear();
		for (EssentiaNetwork network : networks) {
			for (BlockPos pos : network.getPositions()) {
				cache.put(pos.toImmutable(), network);
			}
		}
	}

	public static EssentiaNetworker get(ServerWorld world) {
		return world.getPersistentStateManager().getOrCreate((tag) -> EssentiaNetworker.fromNbt(world, tag), () -> new EssentiaNetworker(world), getName(world.getDimension()));
	}

	public static String getName(DimensionType dimension) {
		return "essentia" + dimension.getSuffix();
	}

	public void tick() {
		processed = 0;
		for (BlockPos pos : new HashSet<>(orphans)) {
			if (processed < processingLimit) {
				add(pos.toImmutable());
			} else {
				break;
			}
		}
		for (BlockPos pos : new HashSet<>(legacyOrphans)) {
			if (processed < processingLimit) {
				recursiveAdd(pos.toImmutable());
			} else {
				break;
			}
		}
		for (EssentiaNetwork network : networks) {
			network.tick();
		}
	}

	public Optional<EssentiaNetwork> getNetwork(BlockPos pos) {
		{
			EssentiaNetwork network = cache.get(pos);
			if (network != null) {
				if (network.contains(pos)) {
					return Optional.of(network);
				}
			}
		}
		for (EssentiaNetwork network : networks) {
			if (network.contains(pos)) {
				cache.put(pos.toImmutable(), network);
				return Optional.of(network);
			}
		}
		return Optional.empty();
	}

	public Set<EssentiaNetwork> getConnectedNetworks(BlockPos pos) {
		Set<EssentiaNetwork> connectedNetworks = new HashSet<>();
		for (BlockPos other : getConnections(pos)) {
			Optional<EssentiaNetwork> network = getNetwork(other);
			network.ifPresent(connectedNetworks::add);
		}
		return connectedNetworks;
	}

	public Set<BlockPos> getConnections(BlockPos pos) {
		Block block = world.getBlockState(pos).getBlock();
		if (block instanceof NetworkElement) {
			return ((NetworkElement) block).getConnections(world, pos);
		} else {
			return new HashSet<>();
		}
	}

	public void add(BlockPos pos) {
		processed++;
		orphans.remove(pos);
		if (!getNetwork(pos).isPresent()) {
			// Otherwise, add it to any connected networks, creating a new one or merging if necessary
			EssentiaNetwork network = merge(getConnectedNetworks(pos).toArray(new EssentiaNetwork[0]));
			network.add(pos.toImmutable());
			cache.put(pos.toImmutable(), network);
			markDirty();
		}
	}

	// Merges n networks (where n can be 0, thus creating a new, empty network.)
	// Warning: WILL NOT rebuild nodes automatically.
	public EssentiaNetwork merge(EssentiaNetwork... networks) {
		if (networks.length == 1) {
			// If given one network, there's nothing to merge, so just return it
			return networks[0];
		} else {
			// If given 0 or 2+ networks, create a new network with all the positions of the old ones and delete them
			EssentiaNetwork mergedNetwork = new EssentiaNetwork(world);
			this.networks.add(mergedNetwork);
			for (EssentiaNetwork network : networks) {
				mergedNetwork.getPositions().addAll(network.getPositions());
				for (BlockPos pos : mergedNetwork.getPositions()) {
					cache.put(pos, mergedNetwork);
				}
				this.networks.remove(network);
			}
			mergedNetwork.rebuildNodes();
			markDirty();
			return mergedNetwork;
		}
	}

	public void remove(BlockPos pos, Set<BlockPos> connections) {
		processed++;
		getNetwork(pos).ifPresent((network) -> {
			cache.remove(pos);
			network.remove(pos);
			if (network.getSize() == 0 || connections.size() > 1) {
				for (BlockPos netPos : network.getPositions()) {
					orphans.add(netPos.toImmutable());
					cache.remove(netPos);
				}
				networks.remove(network);
			}
			markDirty();
		});
	}

	@Deprecated
	public void recursiveAdd(BlockPos pos) {
		if (processed < processingLimit) {
			if (!getNetwork(pos).isPresent()) {
				add(pos.toImmutable());
				legacyOrphans.remove(pos);
				Set<BlockPos> connections = getConnections(pos);
				connections.forEach(this::recursiveAdd);
			}
		} else {
			legacyOrphans.add(pos.toImmutable());
			ArtOfAlchemy.log(Level.WARN, "Reached essentia network processing limit at [" + pos.getX() +
					", " + pos.getY() + ", " + pos.getZ()+ "] in " + world.getDimension().getSuffix());
		}
	}


}
