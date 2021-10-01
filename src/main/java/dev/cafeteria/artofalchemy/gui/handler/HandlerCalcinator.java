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

public class HandlerCalcinator extends SyncedGuiDescription {

	public HandlerCalcinator(final int syncId, final PlayerInventory playerInventory, final ScreenHandlerContext ctx) {
		super(
			AoAHandlers.CALCINATOR, syncId, playerInventory, SyncedGuiDescription.getBlockInventory(ctx),
			SyncedGuiDescription.getBlockPropertyDelegate(ctx)
		);

		final WGridPanel panel = AoAHandlers.makePanel(this);
		AoAHandlers.makeTitle(panel, new TranslatableText("block.artofalchemy.calcination_furnace"));
		AoAHandlers.addInventory(panel, this);
		AoAHandlers.addCentralProgressBar(panel, new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/progress_yellow.png"));
		AoAHandlers.addBigOutput(panel, WItemSlot.outputOf(this.blockInventory, 2));

		// Input
		panel.add(WItemSlot.of(this.blockInventory, 0), 2 * AoAHandlers.BASIS + 4, AoAHandlers.BASIS + 4);

		// Fuel
		panel.add(WItemSlot.of(this.blockInventory, 1), 2 * AoAHandlers.BASIS + 4, 3 * AoAHandlers.BASIS + 4);

		// Fuel Bar
		panel.add(
			new WBar(
				new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/fire_off.png"),
				new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/fire_on.png"), 0, 1, Direction.UP
			),
			2 * AoAHandlers.BASIS + 4 + 1 + 2,
			2 * AoAHandlers.BASIS + 4 + 2,
			AoAHandlers.BASIS - 4,
			AoAHandlers.BASIS - 4
		);

		panel.validate(this);
	}

}
