package dev.cafeteria.artofalchemy.block;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import dev.cafeteria.artofalchemy.ArtOfAlchemy;
import dev.cafeteria.artofalchemy.blockentity.BlockEntityPipe;
import dev.cafeteria.artofalchemy.blockentity.BlockEntityPipe.IOFace;
import dev.cafeteria.artofalchemy.item.AoAItems;
import dev.cafeteria.artofalchemy.item.ItemEssentiaPort;
import dev.cafeteria.artofalchemy.network.AoANetworking;
import dev.cafeteria.artofalchemy.transport.EssentiaNetwork;
import dev.cafeteria.artofalchemy.transport.EssentiaNetworker;
import dev.cafeteria.artofalchemy.transport.NetworkElement;
import dev.cafeteria.artofalchemy.transport.NetworkNode;

import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class BlockPipe extends Block implements NetworkElement, BlockEntityProvider {

	private static VoxelShape boundingBox = VoxelShapes.cuboid(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);

	public BlockPipe() {
		super(Settings.of(Material.ORGANIC_PRODUCT).strength(0.1f).nonOpaque().noCollision().sounds(BlockSoundGroup.NETHERITE));
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return boundingBox;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return boundingBox;
	}

	@Override
	public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
		return boundingBox;
	}

	@Override
	public VoxelShape getCameraCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return boundingBox;
	}

	private Map<Direction, IOFace> getFaces(World world, BlockPos pos) {
		BlockEntity be = world.getBlockEntity(pos);
		if (be instanceof BlockEntityPipe) {
			return ((BlockEntityPipe) be).getFaces();
		} else {
			// Returning a dummy map here would just hide bugs
			return null;
		}
	}

	private IOFace getFace(World world, BlockPos pos, Direction dir) {
		return getFaces(world, pos).get(dir);
	}

	private void setFace(World world, BlockPos pos, Direction dir, IOFace face) {
		BlockEntity be = world.getBlockEntity(pos);
		if (be instanceof BlockEntityPipe) {
			((BlockEntityPipe) be).setFace(dir, face);
		}
	}

	public static void scheduleChunkRebuild(World world, BlockPos pos) {
		if (world.isClient()) {
			MinecraftClient client = MinecraftClient.getInstance();
			client.worldRenderer.updateBlock(world, pos, null, null, 0);
			client.close();
		}
	}

	// If a pipe exists at pos, and it was previously connected in Direction dir,
	// undo the connection
	private void closeFace(World world, BlockPos pos, Direction dir) {
		BlockEntity be = world.getBlockEntity(pos);
		if (be instanceof BlockEntityPipe) {
			BlockEntityPipe pipe = (BlockEntityPipe) be;
			IOFace previous = pipe.getFace(dir);
			if (previous == IOFace.CONNECT) {
				pipe.setFace(dir, IOFace.NONE);
			}
		}
	}

	// If a pipe exists at pos, and it is capable of connecting in Direction dir,
	// set it as connected
	private void connectFace(World world, BlockPos pos, Direction dir) {
		BlockEntity be = world.getBlockEntity(pos);
		if (be instanceof BlockEntityPipe) {
			BlockEntityPipe pipe = (BlockEntityPipe) be;
			IOFace previous = pipe.getFace(dir);
			if (previous == IOFace.NONE) {
				pipe.setFace(dir, IOFace.CONNECT);
			}
		}
	}

	public boolean hasNodes(World world, BlockPos pos) {
		return !getNodes(world, pos).isEmpty();
	}

	public Set<NetworkNode> getNodes(World world, BlockPos pos) {
		HashSet<NetworkNode> nodes = new HashSet<>();
		Map<Direction, IOFace> faces = getFaces(world, pos);
		for (Direction dir : faces.keySet()) {
			IOFace face = faces.get(dir);
			if (face.isNode()) {
				nodes.add(new NetworkNode(world, face.getType(), pos, dir));
			}
		}
		return nodes;
	}

	public boolean faceOpen(World world, BlockPos pos, Direction dir) {
		if (world.getBlockState(pos).getBlock() == this) {
			IOFace face = getFace(world, pos, dir);
			return (face == IOFace.NONE || face == IOFace.CONNECT);
		} else {
			return false;
		}
	}

	public boolean isConnected(World world, BlockPos pos, Direction dir) {
		Map<Direction, IOFace> theseFaces = getFaces(world, pos);
		Map<Direction, IOFace> otherFaces = getFaces(world, pos.offset(dir));
		return (theseFaces.get(dir) == IOFace.CONNECT && otherFaces.get(dir.getOpposite()) == IOFace.CONNECT);
	}

	public boolean isConnected(World world, BlockPos pos1, BlockPos pos2) {
		BlockPos difference = pos2.subtract(pos1);
		for (Direction dir : Direction.values()) {
			if (difference.equals(dir.getVector())) {
				return isConnected(world, pos1, dir);
			}
		}
		return false;
	}

	@Override
	public Set<BlockPos> getConnections(World world, BlockPos pos) {
		Set<BlockPos> connections = new HashSet<>();
		Map<Direction, IOFace> faces = getFaces(world, pos);
		for (Direction dir : faces.keySet()) {
			if (faces.get(dir) == IOFace.CONNECT) {
				connections.add(pos.offset(dir));
			}
		}
		return connections;
	}

	// Same as getConnections, but doesn't interact with the block entity at
	// 'pos' itself. Useful when said block entity might already be disposed
	// and not available (eg. on broken block).
	private Set<BlockPos> getConnectionsBlockless(World world, BlockPos pos) {
		Set<BlockPos> connections = new HashSet<>();
		for (Direction dir : Direction.values()) {
			BlockPos neighbour = pos.offset(dir);
			BlockEntity be = world.getBlockEntity(neighbour);
			if (be instanceof BlockEntityPipe) {
				BlockEntityPipe bep = (BlockEntityPipe) be;
				if (bep.getFace(dir.getOpposite()) == IOFace.CONNECT) {
					connections.add(neighbour);
				}
			}
		}
		return connections;
	}

	@Override
	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
		super.neighborUpdate(state, world, pos, block, fromPos, notify);
		for (Direction dir : Direction.values()) {
			if (fromPos.subtract(pos).equals(dir.getVector())) {
				if (faceOpen(world, pos, dir) && faceOpen(world, fromPos, dir.getOpposite())) {
					setFace(world, pos, dir, IOFace.CONNECT);
					AoANetworking.sendPipeFaceUpdate(world, pos, dir, IOFace.CONNECT);
				} else if (getFace(world, pos, dir) == IOFace.CONNECT) {
					setFace(world, pos, dir, IOFace.NONE);
					AoANetworking.sendPipeFaceUpdate(world, pos, dir, IOFace.NONE);
				}
				scheduleChunkRebuild(world, pos);
			}
		}
	}

	// Check surrounding blocks for open connections, and initialize this
	// blocks face configuration accordingly
	private void initConnectionFaces(World world, BlockPos pos) {
		for (Direction dir : Direction.values()) {
			if (faceOpen(world, pos.offset(dir), dir.getOpposite())) {
				setFace(world, pos, dir, IOFace.CONNECT);
			}
		}
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
		super.onPlaced(world, pos, state, placer, itemStack);
		initConnectionFaces(world, pos);
		if (!world.isClient()) {
			EssentiaNetworker.get((ServerWorld) world).add(pos);
		}
	}

	// Overriding afterBreak instead of onBroken means we still get access to the underlying block entity
	@Override
	public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack stack) {
		super.afterBreak(world, player, pos, state, blockEntity, stack);
		Map<Direction, IOFace> faces = ((BlockEntityPipe)blockEntity).getFaces();
		for (IOFace face : faces.values()) {
			if (face.isNode()) {
				dropStack(world, pos, new ItemStack(ItemEssentiaPort.getItem(face)));
			}
		}
	}

	@Override
	public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
		super.onBlockAdded(state, world, pos, oldState, notify);
		initConnectionFaces(world, pos);
		if (!world.isClient()) {
			EssentiaNetworker.get((ServerWorld) world).add(pos);
		}
	}

	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean notify) {
		super.onStateReplaced(state, world, pos, newState, notify);
		if (!world.isClient() && newState.getBlock() != this) {
			EssentiaNetworker.get((ServerWorld) world).remove(pos, getConnectionsBlockless(world, pos));
		}
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		Direction dir = player.isSneaking() ? hit.getSide().getOpposite() : hit.getSide();
		ItemStack heldStack = player.getStackInHand(hand);
		if (heldStack.getItem() == AoAItems.MYSTERIOUS_SIGIL) {
			if (!world.isClient()) {
				Optional<EssentiaNetwork> network = EssentiaNetworker.get((ServerWorld) world).getNetwork(pos);
				if (network.isPresent()) {
					player.sendSystemMessage(new LiteralText(network.get().getUuid().toString() + " w/ " + network.get().getNodes().size() + " nodes"), new UUID(0, 0));
				} else {
					player.sendSystemMessage(new LiteralText("no network"), new UUID(0, 0));
				}
			}
			return ActionResult.SUCCESS;
		}
		if (TagRegistry.item(ArtOfAlchemy.id("usable_on_pipes")).contains(heldStack.getItem())) {
			return ActionResult.PASS;
		}
		Set<BlockPos> oldConnections = getConnections(world, pos);
		IOFace face = getFace(world, pos, dir);
		switch (face) {
		case NONE:
		case CONNECT:
			world.playSound(null, pos, SoundEvents.BLOCK_NETHERITE_BLOCK_FALL, SoundCategory.BLOCKS, 0.6f, 1.0f);
			if (heldStack.getItem() instanceof ItemEssentiaPort) {
				setFace(world, pos, dir, ((ItemEssentiaPort) heldStack.getItem()).IOFACE);
				if (!player.getAbilities().creativeMode) {
					heldStack.decrement(1);
				}
			} else {
				setFace(world, pos, dir, IOFace.BLOCK);
			}
			closeFace(world, pos.offset(dir), dir.getOpposite());
			scheduleChunkRebuild(world, pos);
			break;
		case BLOCK:
		case INSERTER:
		case EXTRACTOR:
		case PASSIVE:
			world.playSound(null, pos, SoundEvents.BLOCK_NETHERITE_BLOCK_HIT, SoundCategory.BLOCKS, 0.6f, 1.0f);
			if (!player.getAbilities().creativeMode) {
				ItemStack stack = new ItemStack(ItemEssentiaPort.getItem(face));
				dropStack(world, pos, stack);
			}
			if (faceOpen(world, pos.offset(dir), dir.getOpposite())) {
				setFace(world, pos, dir, IOFace.CONNECT);
			} else {
				setFace(world, pos, dir, IOFace.NONE);
			}
			connectFace(world, pos.offset(dir), dir.getOpposite());
			scheduleChunkRebuild(world, pos);
			break;
		}
		if (!world.isClient()) {
			EssentiaNetworker networker = EssentiaNetworker.get((ServerWorld) world);
			networker.remove(pos, oldConnections);
			networker.add(pos);
		}
		return ActionResult.SUCCESS;
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new BlockEntityPipe(pos, state);
	}
}
