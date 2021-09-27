package com.cumulusmc.artofalchemy.gui.handler;

import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.WBar.Direction;
import com.cumulusmc.artofalchemy.ArtOfAlchemy;
import com.cumulusmc.artofalchemy.essentia.EssentiaContainer;
import com.cumulusmc.artofalchemy.transport.HasEssentia;
import com.cumulusmc.artofalchemy.gui.widget.WEssentiaPanel;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class HandlerDistiller extends SyncedGuiDescription {

	final BlockPos pos;
	final WEssentiaPanel essentiaPanel;

	public HandlerDistiller(int syncId, PlayerInventory playerInventory, ScreenHandlerContext ctx) {
		super(AoAHandlers.DISTILLER, syncId, playerInventory, getBlockInventory(ctx), getBlockPropertyDelegate(ctx));

		pos = ctx.get((world, pos) -> pos, null);

		WGridPanel panel = AoAHandlers.makePanel(this);
		AoAHandlers.makeTitle(panel, new TranslatableText("block.artofalchemy.distillation_apparatus"));
		AoAHandlers.addInventory(panel, this);
		AoAHandlers.addCentralProgressBar(panel, new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/progress_magenta.png"));

		// Azoth Slot
		panel.add(
			WItemSlot.of(blockInventory, 0),
			AoAHandlers.BASIS * 2,
			AoAHandlers.BASIS * 1 + 3
		);
		
		// Fuel Slot
		panel.add(
			WItemSlot.of(blockInventory, 1),
			AoAHandlers.BASIS * 2,
			AoAHandlers.BASIS * 3 + 3 + 2
		);
		
		// Fuel Indicator
		panel.add(
			new WBar(
				new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/fire_off.png"),
				new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/fire_on.png"),
				2,
				3,
				Direction.UP
			),
			(AoAHandlers.BASIS * 2) + 2,
			(AoAHandlers.BASIS * 2) + 3 + 2,
			16,
			16
		);

		// Essentia Tank
		WBar essentiaTankBar = new WBar(
			new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/tank_empty.png"),
			new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/tank_full.png"),
			4,
			6,
			Direction.UP
		);
		essentiaTankBar.withTooltip("gui." + ArtOfAlchemy.MOD_ID + ".mixed_essentia_tooltip");
		panel.add(
			essentiaTankBar,
			0,
			AoAHandlers.BASIS + 3,
			AoAHandlers.BASIS * 2,
			AoAHandlers.BASIS * 3
		);
		
		WBar alkahestTankBar = new WBar(
			new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/tank_empty.png"),
			new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/tank_full.png"),
			5,
			6,
			Direction.UP
		);
		alkahestTankBar.withTooltip("gui." + ArtOfAlchemy.MOD_ID + ".alkahest_tooltip");
		panel.add(
			alkahestTankBar,
			AoAHandlers.PANEL_WIDTH - AoAHandlers.BASIS * 3,
			AoAHandlers.BASIS + 3,
			AoAHandlers.BASIS * 2,
			AoAHandlers.BASIS * 3
		);

		EssentiaContainer essentia = getEssentia(ctx);
		essentiaPanel = new WEssentiaPanel(essentia);

		panel.validate(this);
	}

	public void updateEssentia(int essentiaId, EssentiaContainer essentia, BlockPos pos) {
		if (pos.equals(this.pos)) essentiaPanel.updateEssentia(essentia);
	}

	private static EssentiaContainer getEssentia(ScreenHandlerContext ctx) {
		return ctx.get((world, pos) -> {
			BlockEntity be = world.getBlockEntity(pos);
			if (be instanceof HasEssentia) return ((HasEssentia) be).getContainer(0);
			else return new EssentiaContainer();
		}, new EssentiaContainer());
	}

}
