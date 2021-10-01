package dev.cafeteria.artofalchemy.block;

import dev.cafeteria.artofalchemy.essentia.EssentiaContainer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

abstract public class AbstractBlockCentrifuge extends Block implements BlockEntityProvider {

	public static final int TANK_SIZE = 4000;
	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
	public static final Settings SETTINGS = Settings.of(Material.STONE).strength(5.0f, 6.0f);
	protected EssentiaContainer input = new EssentiaContainer().setCapacity(AbstractBlockCentrifuge.TANK_SIZE)
		.setInput(true).setOutput(false);
	protected EssentiaContainer[] outputs;

	public AbstractBlockCentrifuge() {
		super(AbstractBlockCentrifuge.SETTINGS);
		this.setDefaultState(this.getDefaultState().with(AbstractBlockCentrifuge.FACING, Direction.NORTH));
	}

	@Override
	protected void appendProperties(final Builder<Block, BlockState> builder) {
		builder.add(AbstractBlockCentrifuge.FACING);
	}

	@Override
	public BlockState getPlacementState(final ItemPlacementContext ctx) {
		return super.getPlacementState(ctx).with(AbstractBlockCentrifuge.FACING, ctx.getPlayerFacing().getOpposite());
	}

	@Override
	public BlockState mirror(final BlockState state, final BlockMirror mirror) {
		return state.rotate(mirror.getRotation(state.get(AbstractBlockCentrifuge.FACING)));
	}

	@Override
	public ActionResult onUse(
		final BlockState state, final World world, final BlockPos pos, final PlayerEntity player, final Hand hand,
		final BlockHitResult hit
	) {
		if (world.isClient) {
			return ActionResult.SUCCESS;
		}

		if (player.isSneaking()) {
			world.setBlockState(pos, this.rotate(state, BlockRotation.CLOCKWISE_90));
		} else {
			world.setBlockState(pos, this.rotate(state, BlockRotation.COUNTERCLOCKWISE_90));
		}
		return ActionResult.SUCCESS;
	}

	@Override
	public BlockState rotate(final BlockState state, final BlockRotation rotation) {
		return state.with(AbstractBlockCentrifuge.FACING, rotation.rotate(state.get(AbstractBlockCentrifuge.FACING)));
	}
}
