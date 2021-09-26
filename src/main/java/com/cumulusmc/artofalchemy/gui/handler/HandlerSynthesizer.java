package com.cumulusmc.artofalchemy.gui.handler;

import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.WBar.Direction;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import com.cumulusmc.artofalchemy.ArtOfAlchemy;
import com.cumulusmc.artofalchemy.blockentity.BlockEntitySynthesizer;
import com.cumulusmc.artofalchemy.essentia.EssentiaContainer;
import com.cumulusmc.artofalchemy.essentia.EssentiaStack;
import com.cumulusmc.artofalchemy.transport.HasEssentia;
import com.cumulusmc.artofalchemy.gui.widget.WEssentiaPanel;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class HandlerSynthesizer extends SyncedGuiDescription {

	final BlockPos pos;
	final WEssentiaPanel essentiaPanel;

	@SuppressWarnings("MethodCallSideOnly")
	public HandlerSynthesizer(int syncId, PlayerInventory playerInventory, ScreenHandlerContext ctx) {
		super(AoAHandlers.SYNTHESIZER, syncId, playerInventory, getBlockInventory(ctx), getBlockPropertyDelegate(ctx));

		pos = ctx.get((world, pos) -> pos, null);

		WGridPanel panel = AoAHandlers.makePanel(this);
		AoAHandlers.makeTitle(panel, new TranslatableText("block.artofalchemy.synthesis_table"));
		AoAHandlers.addInventory(panel, this);

		WItemSlot inSlot = WItemSlot.of(blockInventory, 0);
		panel.add(inSlot, 4 * AoAHandlers.BASIS, AoAHandlers.BASIS - 4);

		WItemSlot outSlot = WItemSlot.outputOf(blockInventory, 1);
		panel.add(outSlot, 6 * AoAHandlers.BASIS + 4, 2 * AoAHandlers.BASIS);

		WSprite targetIcon = new WSprite(new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/target.png"));
		panel.add(targetIcon, 4 * AoAHandlers.BASIS + 1, 3 * AoAHandlers.BASIS + 5, AoAHandlers.BASIS, AoAHandlers.BASIS);

		WItemSlot targetSlot = WItemSlot.of(blockInventory, 2);
		panel.add(targetSlot, 4 * AoAHandlers.BASIS, 3 * AoAHandlers.BASIS + 4);

		WBar progressBar = new WBar(new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/progress_off.png"),
				new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/progress_cyan.png"), 0, 1, Direction.RIGHT);
		panel.add(progressBar, 3 * AoAHandlers.BASIS, 2 * AoAHandlers.BASIS + 1, 3 * AoAHandlers.BASIS, AoAHandlers.BASIS);

		WDynamicLabel alert = new WDynamicLabel(() -> {
			switch (propertyDelegate.get(2)) {
			case 2:
				return I18n.translate("gui." + ArtOfAlchemy.MOD_ID + ".target_warning");
			case 3:
				return I18n.translate("gui." + ArtOfAlchemy.MOD_ID + ".materia_warning");
			case 4:
				return I18n.translate("gui." + ArtOfAlchemy.MOD_ID + ".essentia_warning");
			case 5:
				return I18n.translate("gui." + ArtOfAlchemy.MOD_ID + ".container_warning");
			case 6:
				return I18n.translate("gui." + ArtOfAlchemy.MOD_ID + ".tier_warning");
			default:
				return "";
			}
		}, 0xFF5555);
		alert.setAlignment(HorizontalAlignment.CENTER);
		panel.add(alert, 0, -1 * AoAHandlers.BASIS, 9 * AoAHandlers.BASIS, AoAHandlers.BASIS);

		essentiaPanel = new WEssentiaPanel(getEssentia(ctx), getRequirements(ctx));
		panel.add(essentiaPanel, 2, AoAHandlers.BASIS - 7, 3 * AoAHandlers.BASIS, 4 * AoAHandlers.BASIS);

		panel.validate(this);

	}

	public void updateEssentia(int essentiaId, EssentiaContainer essentia, BlockPos pos) {
		if (pos.equals(this.pos)) {
			essentiaPanel.updateEssentia(essentia);
		}
	}

	public void updateEssentia(int essentiaId, EssentiaContainer essentia,
							   EssentiaStack required, BlockPos pos) {
		if (pos.equals(this.pos)) {
			essentiaPanel.updateEssentia(essentia, required);
		}
	}

	private static EssentiaContainer getEssentia(ScreenHandlerContext ctx) {
		return ctx.get((world, pos) -> {
			BlockEntity be = world.getBlockEntity(pos);
			if (be instanceof HasEssentia) {
				return ((HasEssentia) be).getContainer(0);
			} else {
				return new EssentiaContainer();
			}
		}, new EssentiaContainer());
	}

	private static EssentiaStack getRequirements(ScreenHandlerContext ctx) {
		return ctx.get((world, pos) -> {
			BlockEntity be = world.getBlockEntity(pos);
			if (be instanceof BlockEntitySynthesizer) {
				return ((BlockEntitySynthesizer) be).getRequirements();
			} else {
				return new EssentiaStack();
			}
		}, new EssentiaStack());
	}

}
