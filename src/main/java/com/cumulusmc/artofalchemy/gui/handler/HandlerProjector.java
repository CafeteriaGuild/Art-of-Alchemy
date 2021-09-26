package com.cumulusmc.artofalchemy.gui.handler;

import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.WBar.Direction;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import com.cumulusmc.artofalchemy.ArtOfAlchemy;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class HandlerProjector extends SyncedGuiDescription {

	public HandlerProjector(int syncId, PlayerInventory playerInventory, ScreenHandlerContext ctx) {
		super(AoAHandlers.PROJECTOR, syncId, playerInventory, getBlockInventory(ctx), getBlockPropertyDelegate(ctx));

		WGridPanel panel = AoAHandlers.makePanel(this);
		AoAHandlers.makeTitle(panel, new TranslatableText("block.artofalchemy.projection_altar"));
		AoAHandlers.addInventory(panel, this);

		// Input Slot
		panel.add(
			WItemSlot.of(blockInventory, 0),
			2 * AoAHandlers.BASIS,
			2 * AoAHandlers.BASIS + 4
		);

		WBar tankBar = new WBar(
			new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/tank_empty.png"),
			new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/tank_full.png"),
			0,
			1,
			Direction.UP
		);
		tankBar.withTooltip("gui." + ArtOfAlchemy.MOD_ID + ".alkahest_tooltip");
		panel.add(
			tankBar,
			0,
			AoAHandlers.BASIS,
			2 * AoAHandlers.BASIS,
			3 * AoAHandlers.BASIS + 6
		);

		// Output Slot
		panel.add(
			WItemSlot.outputOf(blockInventory, 1),
			6 * AoAHandlers.BASIS + 4,
			2 * AoAHandlers.BASIS + 4
		);

		// Progress Bar
		panel.add(
			new WBar(
				new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/progress_off.png"),
				new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/progress_green.png"),
				2,
				3,
				Direction.RIGHT
			),
			3 * AoAHandlers.BASIS,
			2 * AoAHandlers.BASIS + 4,
			3 * AoAHandlers.BASIS,
			AoAHandlers.BASIS
		);

		panel.validate(this);
	}
}
