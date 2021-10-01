package dev.cafeteria.artofalchemy.gui.handler;

import java.util.function.BiFunction;

import dev.cafeteria.artofalchemy.ArtOfAlchemy;
import dev.cafeteria.artofalchemy.essentia.EssentiaContainer;
import dev.cafeteria.artofalchemy.gui.widget.WEssentiaPanel;
import dev.cafeteria.artofalchemy.transport.HasEssentia;
import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WBar;
import io.github.cottonmc.cotton.gui.widget.WBar.Direction;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WItemSlot;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class HandlerDistiller extends SyncedGuiDescription {

	private static EssentiaContainer getEssentia(final ScreenHandlerContext ctx) {
		return ctx.get(new BiFunction<World, BlockPos, EssentiaContainer>() {
			@Override
			public EssentiaContainer apply(final World world, final BlockPos pos) {
				final BlockEntity be = world.getBlockEntity(pos);
				if (be instanceof HasEssentia) {
					return ((HasEssentia) be).getContainer(0);
				} else {
					return new EssentiaContainer();
				}
			}
		}, new EssentiaContainer());
	}

	final BlockPos pos;

	final WEssentiaPanel essentiaPanel;

	public HandlerDistiller(final int syncId, final PlayerInventory playerInventory, final ScreenHandlerContext ctx) {
		super(
			AoAHandlers.DISTILLER, syncId, playerInventory, SyncedGuiDescription.getBlockInventory(ctx),
			SyncedGuiDescription.getBlockPropertyDelegate(ctx)
		);

		this.pos = ctx.get(new BiFunction<World, BlockPos, BlockPos>() {
			@Override
			public BlockPos apply(final World world, final BlockPos pos) {
				return pos;
			}
		}, null);

		final WGridPanel panel = AoAHandlers.makePanel(this);
		AoAHandlers.makeTitle(panel, new TranslatableText("block.artofalchemy.distillation_apparatus"));
		AoAHandlers.addInventory(panel, this);
		AoAHandlers.addCentralProgressBar(panel, new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/progress_magenta.png"));

		// Azoth Slot
		panel.add(WItemSlot.of(this.blockInventory, 0), AoAHandlers.BASIS * 2, AoAHandlers.BASIS * 1 + 3);

		// Fuel Slot
		panel.add(WItemSlot.of(this.blockInventory, 1), AoAHandlers.BASIS * 2, AoAHandlers.BASIS * 3 + 3 + 2);

		// Fuel Indicator
		panel.add(
			new WBar(
				new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/fire_off.png"),
				new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/fire_on.png"), 2, 3, Direction.UP
			),
			AoAHandlers.BASIS * 2 + 2,
			AoAHandlers.BASIS * 2 + 3 + 2,
			16,
			16
		);

		// Essentia Tank
		final WBar essentiaTankBar = new WBar(
			new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/tank_empty.png"),
			new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/tank_full.png"), 4, 6, Direction.UP
		);
		essentiaTankBar.withTooltip("gui." + ArtOfAlchemy.MOD_ID + ".mixed_essentia_tooltip");
		panel.add(essentiaTankBar, 0, AoAHandlers.BASIS + 3, AoAHandlers.BASIS * 2, AoAHandlers.BASIS * 3);

		final WBar alkahestTankBar = new WBar(
			new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/tank_empty.png"),
			new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/tank_full.png"), 5, 6, Direction.UP
		);
		alkahestTankBar.withTooltip("gui." + ArtOfAlchemy.MOD_ID + ".alkahest_tooltip");
		panel.add(
			alkahestTankBar,
			AoAHandlers.PANEL_WIDTH - AoAHandlers.BASIS * 3,
			AoAHandlers.BASIS + 3,
			AoAHandlers.BASIS * 2,
			AoAHandlers.BASIS * 3
		);

		final EssentiaContainer essentia = HandlerDistiller.getEssentia(ctx);
		this.essentiaPanel = new WEssentiaPanel(essentia);

		panel.validate(this);
	}

	public void updateEssentia(final int essentiaId, final EssentiaContainer essentia, final BlockPos pos) {
		if (pos.equals(this.pos)) {
			this.essentiaPanel.updateEssentia(essentia);
		}
	}

}
