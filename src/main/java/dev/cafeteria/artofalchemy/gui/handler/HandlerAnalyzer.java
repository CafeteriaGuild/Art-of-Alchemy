package dev.cafeteria.artofalchemy.gui.handler;

import dev.cafeteria.artofalchemy.ArtOfAlchemy;
import dev.cafeteria.artofalchemy.item.AoAItems;
import dev.cafeteria.artofalchemy.item.ItemAlchemyFormula;
import dev.cafeteria.artofalchemy.util.AoAHelper;
import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WItemSlot;
import io.github.cottonmc.cotton.gui.widget.WSprite;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class HandlerAnalyzer extends SyncedGuiDescription {
	public HandlerAnalyzer(final int syncId, final PlayerInventory playerInventory, final ScreenHandlerContext ctx) {
		super(AoAHandlers.ANALYZER, syncId, playerInventory);
		this.blockInventory = new SimpleInventory(4) {
			@Override
			public boolean isValid(final int slot, final ItemStack stack) {
				if (slot == 0) {
					return stack.getItem() == Items.PAPER;
				} else if (slot == 1) {
					return stack.getItem() == Items.INK_SAC;
				} else {
					return slot == 2;
				}
			}
		};

		final WGridPanel panel = AoAHandlers.makePanel(this);
		AoAHandlers.makeTitle(panel, new TranslatableText("block.artofalchemy.analysis_desk"));
		AoAHandlers.addInventory(panel, this);
		AoAHandlers.addBigOutput(panel, WItemSlot.outputOf(this.blockInventory, 3));

		// Paper Icon
		panel.add(
			new WSprite(new Identifier("minecraft", "textures/item/paper.png")),
			(2 * AoAHandlers.BASIS) + 7 + 1,
			(2 * AoAHandlers.BASIS) + 4 + 1,
			16,
			16
		);

		// Paper Slot
		panel.add(WItemSlot.of(this.blockInventory, 0), (2 * AoAHandlers.BASIS) + 7, (2 * AoAHandlers.BASIS) + 4);

		// Ink Icon
		panel.add(
			new WSprite(new Identifier("minecraft", "textures/item/ink_sac.png")),
			(4 * AoAHandlers.BASIS) + 7 + 1,
			AoAHandlers.BASIS + 1,
			16,
			16
		);

		// Ink Slot
		panel.add(WItemSlot.of(this.blockInventory, 1), (4 * AoAHandlers.BASIS) + 7, AoAHandlers.BASIS);

		// Target Icon
		panel.add(
			new WSprite(new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/target.png")),
			(4 * AoAHandlers.BASIS) + 7 + 1,
			(3 * AoAHandlers.BASIS) + 8 + 1,
			AoAHandlers.BASIS,
			AoAHandlers.BASIS
		);

		// Target Slot
		panel.add(WItemSlot.of(this.blockInventory, 2), (4 * AoAHandlers.BASIS) + 7, (3 * AoAHandlers.BASIS) + 8);

		// Progress Bar
		panel.add(
			new WSprite(new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/progress_off.png")),
			(3 * AoAHandlers.BASIS) + 7,
			(2 * AoAHandlers.BASIS) + 4,
			3 * AoAHandlers.BASIS,
			AoAHandlers.BASIS
		);

		panel.validate(this);
	}

	@Override
	public void close(final PlayerEntity player) {
		this.blockInventory.removeStack(3);
		this.dropInventory(player, this.blockInventory);
		super.close(player);
	}

	@Override
	public void onSlotClick(
		final int slotNumber, final int button, final SlotActionType action, final PlayerEntity player
	) {
		final boolean hadFormula = !this.blockInventory.getStack(3).isEmpty();
		super.onSlotClick(slotNumber, button, action, player);
		final boolean hasFormula = !this.blockInventory.getStack(3).isEmpty();

		if (hadFormula && !hasFormula) {
			this.blockInventory.getStack(0).decrement(1);
			this.blockInventory.getStack(1).decrement(1);
		}
		this.updateRecipe();
	}

	public void updateRecipe() {
		if (!this.world.isClient) {
			if (
				(this.blockInventory.getStack(0).getItem() == Items.PAPER)
					&& (this.blockInventory.getStack(1).getItem() == Items.INK_SAC)
					&& !this.blockInventory.getStack(2).isEmpty()
			) {
				final ItemStack formula = new ItemStack(AoAItems.ALCHEMY_FORMULA);
				ItemAlchemyFormula.setFormula(formula, AoAHelper.getTarget(this.blockInventory.getStack(2)));
				this.blockInventory.setStack(3, formula);
			} else {
				this.blockInventory.setStack(3, ItemStack.EMPTY);
			}
		}
	}

}
