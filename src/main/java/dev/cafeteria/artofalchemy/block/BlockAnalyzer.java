package dev.cafeteria.artofalchemy.block;

import dev.cafeteria.artofalchemy.gui.handler.HandlerAnalyzer;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class BlockAnalyzer extends Block implements ExtendedScreenHandlerFactory {

	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

	public static final Settings SETTINGS = Settings.of(Material.STONE).strength(5.0f, 6.0f).nonOpaque();

	public static Identifier getId() {
		return Registry.BLOCK.getId(AoABlocks.ANALYZER);
	}

	public BlockAnalyzer() {
		super(BlockAnalyzer.SETTINGS);
	}

	@Override
	protected void appendProperties(final StateManager.Builder<Block, BlockState> builder) {
		builder.add(BlockAnalyzer.FACING);
	}

	@Override
	public ScreenHandler createMenu(final int syncId, final PlayerInventory inv, final PlayerEntity player) {
		return new HandlerAnalyzer(syncId, inv, ScreenHandlerContext.EMPTY);
	}

	@Override
	public Text getDisplayName() {
		return new LiteralText("");
	}

	@Override
	public BlockState getPlacementState(final ItemPlacementContext ctx) {
		return super.getPlacementState(ctx).with(BlockAnalyzer.FACING, ctx.getPlayerFacing());
	}

	@Override
	public ActionResult onUse(
		final BlockState state, final World world, final BlockPos pos, final PlayerEntity player, final Hand hand,
		final BlockHitResult hit
	) {
		if (world.isClient) {
			return ActionResult.SUCCESS;
		}

		player.openHandledScreen(this);

		return ActionResult.SUCCESS;
	}

	@Override
	public void writeScreenOpeningData(final ServerPlayerEntity player, final PacketByteBuf buf) {
	}
}
