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
		panel.add(itemSlot, 2 * 18, 18);

		WItemSlot fuelSlot = WItemSlot.of(blockInventory, 1);
		panel.add(fuelSlot, 2 * 18, 3 * 18);

		WItemSlot outSlot = WItemSlot.outputOf(blockInventory, 2);
		panel.add(outSlot, 6 * 18 + 4, 2 * 18);

		WBar fuelBar = new WBar(
			new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/fire_off.png"),
			new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/fire_on.png"),
			0,
			1,
			Direction.UP
		);
		panel.add(fuelBar, 2 * 18 + 1, 2 * 18 + 1, 18, 18);

		WBar progressBar = new WBar(
			ArtOfAlchemy.id("textures/gui/progress_off.png"),
			ArtOfAlchemy.id("textures/gui/progress_yellow.png"),
			2,
			3,
			Direction.RIGHT
		);
		panel.add(progressBar, 3 * 18, 2 * 18 + 1, 3 * 18, 18);

		panel.validate(this);
	}

}
