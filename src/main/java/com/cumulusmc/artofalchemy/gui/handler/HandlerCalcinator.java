package com.cumulusmc.artofalchemy.gui.handler;

import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.WBar.Direction;
import com.cumulusmc.artofalchemy.ArtOfAlchemy;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class HandlerCalcinator extends SyncedGuiDescription {

	public HandlerCalcinator(int syncId, PlayerInventory playerInventory, ScreenHandlerContext ctx) {
		super(AoAHandlers.CALCINATOR, syncId, playerInventory, getBlockInventory(ctx), getBlockPropertyDelegate(ctx));

		WGridPanel panel = AoAHandlers.makePanel(this);
		AoAHandlers.makeTitle(panel, new TranslatableText("block.artofalchemy.calcination_furnace"));
		AoAHandlers.addInventory(panel, this);

		// Input
		panel.add(
			WItemSlot.of(blockInventory, 0),
			2 * AoAHandlers.BASIS,
			AoAHandlers.BASIS + 4
		);

		// Fuel
		panel.add(
			WItemSlot.of(blockInventory, 1),
			2 * AoAHandlers.BASIS,
			3 * AoAHandlers.BASIS + 4
		);

		// Output
		panel.add(
			WItemSlot.outputOf(blockInventory, 2),
			6 * AoAHandlers.BASIS + 4,
			2 * AoAHandlers.BASIS + 4
		);
		
		// Fuel Bar
		panel.add(
			new WBar(
				new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/fire_off.png"),
				new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/fire_on.png"),
				0,
				1,
				Direction.UP
			),
			2 * AoAHandlers.BASIS + 1 + 2,
			2 * AoAHandlers.BASIS + 4 + 2,
			AoAHandlers.BASIS - 4,
			AoAHandlers.BASIS - 4
		);

		// Progress Bar
		panel.add(
			new WBar(
				ArtOfAlchemy.id("textures/gui/progress_off.png"),
				ArtOfAlchemy.id("textures/gui/progress_yellow.png"),
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
