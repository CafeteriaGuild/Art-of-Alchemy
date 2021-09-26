package com.cumulusmc.artofalchemy.gui.handler;

import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WItemSlot;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WSprite;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import com.cumulusmc.artofalchemy.util.AoAHelper;
import com.cumulusmc.artofalchemy.ArtOfAlchemy;
import com.cumulusmc.artofalchemy.item.AoAItems;
import com.cumulusmc.artofalchemy.item.ItemAlchemyFormula;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class HandlerAnalyzer extends SyncedGuiDescription {

	Inventory inventory = new SimpleInventory(4) {
		@Override
		public boolean isValid(int slot, ItemStack stack) {
			if (slot == 0) {
				return stack.getItem() == Items.PAPER;
			} else if (slot == 1) {
				return stack.getItem() == Items.INK_SAC;
			} else return slot == 2;
		}
	};

	public HandlerAnalyzer(int syncId, PlayerInventory playerInventory, ScreenHandlerContext ctx) {
		super(AoAHandlers.ANALYZER, syncId, playerInventory);
		blockInventory = inventory;

		WGridPanel panel = AoAHandlers.makePanel(this);
		AoAHandlers.makeTitle(panel, new TranslatableText("block.artofalchemy.analysis_desk"));
		AoAHandlers.addInventory(panel, this);

		WSprite paperIcon = new WSprite(new Identifier("minecraft", "textures/item/paper.png"));
		panel.add(paperIcon, 2 * AoAHandlers.BASIS + 5, 2 * AoAHandlers.BASIS + 1, 16, 16);

		WItemSlot paperSlot = WItemSlot.of(inventory, 0);
		panel.add(paperSlot, 2 * AoAHandlers.BASIS + 4, 2 * AoAHandlers.BASIS);

		WSprite inkIcon = new WSprite(new Identifier("minecraft", "textures/item/ink_sac.png"));
		panel.add(inkIcon, 4 * AoAHandlers.BASIS + 1, AoAHandlers.BASIS - 3, 16, 16);

		WItemSlot inkSlot = WItemSlot.of(inventory, 1);
		panel.add(inkSlot, 4 * AoAHandlers.BASIS, AoAHandlers.BASIS - 4);

		WSprite targetIcon = new WSprite(new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/target.png"));
		panel.add(targetIcon, 4 * AoAHandlers.BASIS + 1, 3 * AoAHandlers.BASIS + 5, AoAHandlers.BASIS, AoAHandlers.BASIS);

		WItemSlot targetSlot = WItemSlot.of(inventory, 2);
		panel.add(targetSlot, 4 * AoAHandlers.BASIS, 3 * AoAHandlers.BASIS + 4);

		WItemSlot outSlot = WItemSlot.outputOf(inventory, 3);
		panel.add(outSlot, 6 * AoAHandlers.BASIS, 2 * AoAHandlers.BASIS);

		WSprite arrow = new WSprite(new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/progress_off.png"));
		panel.add(arrow, 3 * AoAHandlers.BASIS, 2 * AoAHandlers.BASIS, 3 * AoAHandlers.BASIS, AoAHandlers.BASIS);

		panel.validate(this);

	}

	@Override
	public void close(PlayerEntity player) {
		inventory.removeStack(3);
		dropInventory(player, inventory);
		super.close(player);
	}

	@Override
	public void onSlotClick(int slotNumber, int button, SlotActionType action, PlayerEntity player) {
		// If the output slot is the one that has been clicked
		if (slotNumber == 3) {
			// Take count of the number of items in the slot before updating
			int outputBefore = inventory.getStack(3).getCount();
			// Delegate to super
			super.onSlotClick(slotNumber, button, action, player);
			// Take count of the number of items in the slot after updating
			int outputAfter = inventory.getStack(3).getCount();
			// If the delta is negative (items were removed/crafted)
			int delta = outputAfter - outputBefore;
			if (delta < 0) {
				// Decrement the inputs accordingly
				inventory.getStack(0).decrement(-delta);
				inventory.getStack(1).decrement(-delta);
			}
			// Update the recipe
			updateRecipe();
		}
	}

	public void updateRecipe() {
		if (!world.isClient) {
			if (inventory.getStack(0).getItem() == Items.PAPER && inventory.getStack(1).getItem() == Items.INK_SAC
					&& !inventory.getStack(2).isEmpty()) {
				ItemStack formula = new ItemStack(AoAItems.ALCHEMY_FORMULA);
				ItemAlchemyFormula.setFormula(formula, AoAHelper.getTarget(inventory.getStack(2)));
				inventory.setStack(3, formula);
			} else {
				inventory.setStack(3, ItemStack.EMPTY);
			}
			inventory.markDirty();
			((ServerPlayerEntity) playerInventory.player).networkHandler.sendPacket(
					new ScreenHandlerSlotUpdateS2CPacket(syncId, 0, 3, inventory.getStack(3)));
		}
	}

}
