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

		WItemSlot itemSlot = WItemSlot.of(blockInventory, 0);
		panel.add(itemSlot, 2 * AoAHandlers.BASIS, AoAHandlers.BASIS);

		WItemSlot fuelSlot = WItemSlot.of(blockInventory, 1);
		panel.add(fuelSlot, 2 * AoAHandlers.BASIS, 3 * AoAHandlers.BASIS);

		WItemSlot outSlot = WItemSlot.outputOf(blockInventory, 2);
		panel.add(outSlot, 6 * AoAHandlers.BASIS + 4, 2 * AoAHandlers.BASIS);

		WBar fuelBar = new WBar(
			new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/fire_off.png"),
			new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/fire_on.png"),
			0,
			1,
			Direction.UP
		);
		panel.add(fuelBar, 2 * AoAHandlers.BASIS + 1, 2 * AoAHandlers.BASIS + 1, AoAHandlers.BASIS, AoAHandlers.BASIS);

		WBar progressBar = new WBar(
			ArtOfAlchemy.id("textures/gui/progress_off.png"),
			ArtOfAlchemy.id("textures/gui/progress_yellow.png"),
			2,
			3,
			Direction.RIGHT
		);
		panel.add(progressBar, 3 * AoAHandlers.BASIS, 2 * AoAHandlers.BASIS + 1, 3 * AoAHandlers.BASIS, AoAHandlers.BASIS);

		panel.validate(this);
	}

}
