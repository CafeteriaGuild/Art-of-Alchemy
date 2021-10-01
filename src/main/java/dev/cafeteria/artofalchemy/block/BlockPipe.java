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
import net.fabricmc.fabric.api.tag.TagFactory;
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
	private HashSet<NetworkNode> nodes;

	public static void scheduleChunkRebuild(final World world, final BlockPos pos) {
		if (world.isClient()) {
			final MinecraftClient client = MinecraftClient.getInstance();
			client.worldRenderer.updateBlock(world, pos, null, null, 0);
		}
	}

	public BlockPipe() {
		super(
			Settings.of(Material.ORGANIC_PRODUCT).strength(0.1f).nonOpaque().noCollision().sounds(BlockSoundGroup.NETHERITE)
		);
	}

	// Overriding afterBreak instead of onBroken means we still get access to the
	// underlying block entity
	@Override
	public void afterBreak(
		final World world, final PlayerEntity player, final BlockPos pos, final BlockState state,
		final BlockEntity blockEntity, final ItemStack stack
	) {
		super.afterBreak(world, player, pos, state, blockEntity, stack);
		final Map<Direction, IOFace> faces = ((BlockEntityPipe) blockEntity).getFaces();
		for (final IOFace face : faces.values()) {
			if (face.isNode()) {
				Block.dropStack(world, pos, new ItemStack(ItemEssentiaPort.getItem(face)));
			}
		}
	}

	// If a pipe exists at pos, and it was previously connected in Direction dir,
	// undo the connection
	private void closeFace(final World world, final BlockPos pos, final Direction dir) {
		final BlockEntity be = world.getBlockEntity(pos);
		if (be instanceof final BlockEntityPipe pipe) {
			final IOFace previous = pipe.getFace(dir);
			if (previous == IOFace.CONNECT) {
				pipe.setFace(dir, IOFace.NONE);
			}
		}
	}

	// If a pipe exists at pos, and it is capable of connecting in Direction dir,
	// set it as connected
	private void connectFace(final World world, final BlockPos pos, final Direction dir) {
		final BlockEntity be = world.getBlockEntity(pos);
		if (be instanceof final BlockEntityPipe pipe) {
			final IOFace previous = pipe.getFace(dir);
			if (previous == IOFace.NONE) {
				pipe.setFace(dir, IOFace.CONNECT);
			}
		}
	}

	@Override
	public BlockEntity createBlockEntity(final BlockPos pos, final BlockState state) {
		return new BlockEntityPipe(pos, state);
	}

	public boolean faceOpen(final World world, final BlockPos pos, final Direction dir) {
		if (world.getBlockState(pos).getBlock() == this) {
			final IOFace face = this.getFace(world, pos, dir);
			return (face == IOFace.NONE) || (face == IOFace.CONNECT);
		} else {
			return false;
		}
	}

	@Override
	public VoxelShape getCameraCollisionShape(
		final BlockState state, final BlockView world, final BlockPos pos, final ShapeContext context
	) {
		return BlockPipe.boundingBox;
	}

	@Override
	public VoxelShape getCollisionShape(
		final BlockState state, final BlockView world, final BlockPos pos, final ShapeContext context
	) {
		return BlockPipe.boundingBox;
	}

	@Override
	public Set<BlockPos> getConnections(final World world, final BlockPos pos) {
		final Set<BlockPos> connections = new HashSet<>();
		final Map<Direction, IOFace> faces = this.getFaces(world, pos);
		for (final Direction dir : faces.keySet()) {
			if (faces.get(dir) == IOFace.CONNECT) {
				connections.add(pos.offset(dir));
			}
		}
		return connections;
	}

	// Same as getConnections, but doesn't interact with the block entity at
	// 'pos' itself. Useful when said block entity might already be disposed
	// and not available (eg. on broken block).
	private Set<BlockPos> getConnectionsBlockless(final World world, final BlockPos pos) {
		final Set<BlockPos> connections = new HashSet<>();
		for (final Direction dir : Direction.values()) {
			final BlockPos neighbour = pos.offset(dir);
			final BlockEntity be = world.getBlockEntity(neighbour);
			if (be instanceof final BlockEntityPipe bep && (bep.getFace(dir.getOpposite()) == IOFace.CONNECT)) {
				connections.add(neighbour);
			}
		}
		return connections;
	}

	private IOFace getFace(final World world, final BlockPos pos, final Direction dir) {
		return this.getFaces(world, pos).get(dir);
	}

	private Map<Direction, IOFace> getFaces(final World world, final BlockPos pos) {
		final BlockEntity be = world.getBlockEntity(pos);
		if (be instanceof BlockEntityPipe) {
			return ((BlockEntityPipe) be).getFaces();
		} else {
			// Returning a dummy map here would just hide bugs
			return null;
		}
	}

	@Override
	public Set<NetworkNode> getNodes(final World world, final BlockPos pos) {
		this.nodes = new HashSet<>(); // TODO: Only reset when needed
		final Map<Direction, IOFace> faces = this.getFaces(world, pos);
		for (final Direction dir : faces.keySet()) {
			final IOFace face = faces.get(dir);
			if (face.isNode()) {
				nodes.add(new NetworkNode(world, face.getType(), pos, dir));
			}
		}
		return nodes;
	}

	@Override
	public VoxelShape getOutlineShape(
		final BlockState state, final BlockView world, final BlockPos pos, final ShapeContext context
	) {
		return BlockPipe.boundingBox;
	}

	@Override
	public VoxelShape getRaycastShape(final BlockState state, final BlockView world, final BlockPos pos) {
		return BlockPipe.boundingBox;
	}

	@Override
	public boolean hasNodes(final World world, final BlockPos pos) {
		return !this.getNodes(world, pos).isEmpty();
	}

	// Check surrounding blocks for open connections, and initialize this
	// blocks face configuration accordingly
	private void initConnectionFaces(final World world, final BlockPos pos) {
		for (final Direction dir : Direction.values()) {
			if (this.faceOpen(world, pos.offset(dir), dir.getOpposite())) {
				this.setFace(world, pos, dir, IOFace.CONNECT);
			}
		}
	}

	@Override
	public boolean isConnected(final World world, final BlockPos pos1, final BlockPos pos2) {
		final BlockPos difference = pos2.subtract(pos1);
		for (final Direction dir : Direction.values()) {
			if (difference.equals(dir.getVector())) {
				return this.isConnected(world, pos1, dir);
			}
		}
		return false;
	}

	@Override
	public boolean isConnected(final World world, final BlockPos pos, final Direction dir) {
		final Map<Direction, IOFace> theseFaces = this.getFaces(world, pos);
		final Map<Direction, IOFace> otherFaces = this.getFaces(world, pos.offset(dir));
		return (theseFaces.get(dir) == IOFace.CONNECT) && (otherFaces.get(dir.getOpposite()) == IOFace.CONNECT);
	}

	@Override
	public void neighborUpdate(
		final BlockState state, final World world, final BlockPos pos, final Block block, final BlockPos fromPos,
		final boolean notify
	) {
		super.neighborUpdate(state, world, pos, block, fromPos, notify);

		for (final Direction dir : Direction.values()) {
			if (fromPos.subtract(pos).equals(dir.getVector())) {
				if (this.faceOpen(world, pos, dir) && this.faceOpen(world, fromPos, dir.getOpposite())) {
					this.setFace(world, pos, dir, IOFace.CONNECT);
					AoANetworking.sendPipeFaceUpdate(world, pos, dir, IOFace.CONNECT);
				} else if (this.getFace(world, pos, dir) == IOFace.CONNECT) {
					this.setFace(world, pos, dir, IOFace.NONE);
					AoANetworking.sendPipeFaceUpdate(world, pos, dir, IOFace.NONE);
				}
				BlockPipe.scheduleChunkRebuild(world, pos);
			}
		}
	}

	@Override
	public void onBlockAdded(
		final BlockState state, final World world, final BlockPos pos, final BlockState oldState, final boolean notify
	) {
		super.onBlockAdded(state, world, pos, oldState, notify);
		this.initConnectionFaces(world, pos);
		if (!world.isClient()) {
			EssentiaNetworker.get((ServerWorld) world).add(pos);
		}
	}

	@Override
	public void onPlaced(
		final World world, final BlockPos pos, final BlockState state, final LivingEntity placer, final ItemStack itemStack
	) {
		super.onPlaced(world, pos, state, placer, itemStack);
		this.initConnectionFaces(world, pos);
		if (!world.isClient()) {
			EssentiaNetworker.get((ServerWorld) world).add(pos);
		}
	}

	@Override
	public void onStateReplaced(
		final BlockState state, final World world, final BlockPos pos, final BlockState newState, final boolean notify
	) {
		super.onStateReplaced(state, world, pos, newState, notify);
		if (!world.isClient() && (newState.getBlock() != this)) {
			EssentiaNetworker.get((ServerWorld) world).remove(pos, this.getConnectionsBlockless(world, pos));
		}
	}

	@Override
	public ActionResult onUse(
		final BlockState state, final World world, final BlockPos pos, final PlayerEntity player, final Hand hand,
		final BlockHitResult hit
	) {
		final Direction dir = player.isSneaking() ? hit.getSide().getOpposite() : hit.getSide();
		final ItemStack heldStack = player.getStackInHand(hand);
		if (heldStack.getItem() == AoAItems.MYSTERIOUS_SIGIL) {
			if (!world.isClient()) {
				final Optional<EssentiaNetwork> network = EssentiaNetworker.get((ServerWorld) world).getNetwork(pos);
				if (network.isPresent()) {
					player.sendSystemMessage(
						new LiteralText(network.get().getUuid().toString() + " w/ " + network.get().getNodes().size() + " nodes"),
						new UUID(0, 0)
					);
				} else {
					player.sendSystemMessage(new LiteralText("no network"), new UUID(0, 0));
				}
			}
			return ActionResult.SUCCESS;
		}
		if (TagFactory.ITEM.create(ArtOfAlchemy.id("usable_on_pipes")).contains(heldStack.getItem())) {
			return ActionResult.PASS;
		}
		final Set<BlockPos> oldConnections = this.getConnections(world, pos);
		final IOFace face = this.getFace(world, pos, dir);
		switch (face) {
			case NONE:
			case CONNECT:
				world.playSound(null, pos, SoundEvents.BLOCK_NETHERITE_BLOCK_FALL, SoundCategory.BLOCKS, 0.6f, 1.0f);
				if (heldStack.getItem() instanceof ItemEssentiaPort) {
					this.setFace(world, pos, dir, ((ItemEssentiaPort) heldStack.getItem()).IOFACE);
					if (!player.getAbilities().creativeMode) {
						heldStack.decrement(1);
					}
				} else {
					this.setFace(world, pos, dir, IOFace.BLOCK);
				}
				this.closeFace(world, pos.offset(dir), dir.getOpposite());
				BlockPipe.scheduleChunkRebuild(world, pos);
				break;
			case BLOCK:
			case INSERTER:
			case EXTRACTOR:
			case PASSIVE:
				world.playSound(null, pos, SoundEvents.BLOCK_NETHERITE_BLOCK_HIT, SoundCategory.BLOCKS, 0.6f, 1.0f);
				if (!player.getAbilities().creativeMode) {
					final ItemStack stack = new ItemStack(ItemEssentiaPort.getItem(face));
					Block.dropStack(world, pos, stack);
				}
				if (this.faceOpen(world, pos.offset(dir), dir.getOpposite())) {
					this.setFace(world, pos, dir, IOFace.CONNECT);
				} else {
					this.setFace(world, pos, dir, IOFace.NONE);
				}
				this.connectFace(world, pos.offset(dir), dir.getOpposite());
				BlockPipe.scheduleChunkRebuild(world, pos);
				break;
		}
		if (!world.isClient()) {
			final EssentiaNetworker networker = EssentiaNetworker.get((ServerWorld) world);
			networker.remove(pos, oldConnections);
			networker.add(pos);
		}
		return ActionResult.SUCCESS;
	}

	private void setFace(final World world, final BlockPos pos, final Direction dir, final IOFace face) {
		final BlockEntity be = world.getBlockEntity(pos);
		if (be instanceof BlockEntityPipe) {
			((BlockEntityPipe) be).setFace(dir, face);
		}
	}
}
