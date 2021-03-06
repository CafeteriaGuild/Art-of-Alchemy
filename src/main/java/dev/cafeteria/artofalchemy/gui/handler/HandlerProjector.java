package dev.cafeteria.artofalchemy.gui.handler;

import dev.cafeteria.artofalchemy.ArtOfAlchemy;
import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WBar;
import io.github.cottonmc.cotton.gui.widget.WBar.Direction;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WItemSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class HandlerProjector extends SyncedGuiDescription {

	public HandlerProjector(final int syncId, final PlayerInventory playerInventory, final ScreenHandlerContext ctx) {
		super(
			AoAHandlers.PROJECTOR, syncId, playerInventory, SyncedGuiDescription.getBlockInventory(ctx),
			SyncedGuiDescription.getBlockPropertyDelegate(ctx)
		);

		final WGridPanel panel = AoAHandlers.makePanel(this);
		AoAHandlers.makeTitle(panel, new TranslatableText("block.artofalchemy.projection_altar"));
		AoAHandlers.addInventory(panel, this);
		AoAHandlers
			.addCentralProgressBar(panel, new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/progress_green.png"), 2, 3);
		AoAHandlers.addBigOutput(panel, WItemSlot.outputOf(this.blockInventory, 1));

		// Input Slot
		panel.add(WItemSlot.of(this.blockInventory, 0), (2 * AoAHandlers.BASIS) + 7, (2 * AoAHandlers.BASIS) + 4);

		final WBar tankBar = new WBar(
			new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/tank_empty.png"),
			new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/tank_full.png"), 0, 1, Direction.UP
		);
		tankBar.withTooltip("gui." + ArtOfAlchemy.MOD_ID + ".alkahest_tooltip");
		panel.add(tankBar, 0, AoAHandlers.BASIS, 2 * AoAHandlers.BASIS, (3 * AoAHandlers.BASIS) + 6);

		panel.validate(this);
	}
}
