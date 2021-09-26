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

		WItemSlot itemSlot = WItemSlot.of(blockInventory, 0);
		panel.add(itemSlot, 2 * 18, 2 * 18);

		WBar tankBar = new WBar(new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/tank_empty.png"),
				new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/tank_full.png"),
				0, 1, Direction.UP);
		tankBar.withTooltip("gui." + ArtOfAlchemy.MOD_ID + ".alkahest_tooltip");
		panel.add(tankBar, 0, 18, 2 * 18, 3 * 18);

		WItemSlot outSlot = WItemSlot.outputOf(blockInventory, 1);
		panel.add(outSlot, 6 * 18 + 4, 2 * 18);

		WBar progressBar = new WBar(new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/progress_off.png"),
				new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/progress_green.png"),
				2, 3, Direction.RIGHT);
		panel.add(progressBar, 3 * 18, 2 * 18 + 1, 3 * 18, 18);

		panel.validate(this);
	}

}
