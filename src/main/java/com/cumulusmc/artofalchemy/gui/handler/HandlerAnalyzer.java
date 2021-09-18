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

		WGridPanel root = new WGridPanel(1);
		setRootPanel(root);
		root.setSize(160, 128 + 36);

		WSprite background = new WSprite(new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/rune_bg.png"));
		root.add(background, 0, 0, 9 * 18, 5 * 18);

		WSprite paperIcon = new WSprite(new Identifier("minecraft", "textures/item/paper.png"));
		root.add(paperIcon, 2 * 18 + 5, 2 * 18 + 1, 16, 16);

		WItemSlot paperSlot = WItemSlot.of(inventory, 0);
		root.add(paperSlot, 2 * 18 + 4, 2 * 18);

		WSprite inkIcon = new WSprite(new Identifier("minecraft", "textures/item/ink_sac.png"));
		root.add(inkIcon, 4 * 18 + 1, 18 - 3, 16, 16);

		WItemSlot inkSlot = WItemSlot.of(inventory, 1);
		root.add(inkSlot, 4 * 18, 18 - 4);

		WSprite targetIcon = new WSprite(new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/target.png"));
		root.add(targetIcon, 4 * 18 + 1, 3 * 18 + 5, 18, 18);

		WItemSlot targetSlot = WItemSlot.of(inventory, 2);
		root.add(targetSlot, 4 * 18, 3 * 18 + 4);

		WItemSlot outSlot = WItemSlot.outputOf(inventory, 3);
		root.add(outSlot, 6 * 18, 2 * 18);

		WSprite arrow = new WSprite(new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/progress_off.png"));
		root.add(arrow, 3 * 18, 2 * 18, 3 * 18, 18);

		WLabel title = new WLabel(new TranslatableText("block.artofalchemy.analysis_desk"),
				WLabel.DEFAULT_TEXT_COLOR);
		title.setHorizontalAlignment(HorizontalAlignment.CENTER);
		root.add(title, 0, -1, 9 * 18, 18);

		root.add(this.createPlayerInventoryPanel(), 0, 5 * 18);

		root.validate(this);

	}

	@Override
	public void close(PlayerEntity player) {
		inventory.removeStack(3);
		dropInventory(player, inventory);
		super.close(player);
	}

	@Override
	public void onSlotClick(int slotNumber, int button, SlotActionType action, PlayerEntity player) {
		super.onSlotClick(slotNumber, button, action, player);
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
