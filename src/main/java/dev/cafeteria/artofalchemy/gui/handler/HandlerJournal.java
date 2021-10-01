package dev.cafeteria.artofalchemy.gui.handler;

import org.jetbrains.annotations.Nullable;

import dev.cafeteria.artofalchemy.ArtOfAlchemy;
import dev.cafeteria.artofalchemy.gui.widget.WFormulaList;
import dev.cafeteria.artofalchemy.item.AbstractItemFormula;
import dev.cafeteria.artofalchemy.item.ItemJournal;
import dev.cafeteria.artofalchemy.network.AoAClientNetworking;
import dev.cafeteria.artofalchemy.util.AoAHelper;
import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WItemSlot;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WSprite;
import io.github.cottonmc.cotton.gui.widget.WTextField;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class HandlerJournal extends SyncedGuiDescription {

	Hand hand;
	WTextField searchBar;
	WButton clearButton;
	WFormulaList formulaList;
	ItemStack journal;

	Inventory inventory = new SimpleInventory(1) {
		@Override
		public boolean isValid(final int slot, final ItemStack stack) {
			return stack.getItem() instanceof AbstractItemFormula && !(stack.getItem() instanceof ItemJournal);
		}
	};

	public HandlerJournal(
		final int syncId, final PlayerInventory playerInventory, final ScreenHandlerContext ctx, final Hand hand
	) {
		super(AoAHandlers.JOURNAL, syncId, playerInventory);
		this.blockInventory = this.inventory;

		this.hand = hand;
		this.journal = playerInventory.player.getStackInHand(hand);

		final WGridPanel panel = new WGridPanel(1);
		this.setRootPanel(panel);
		panel.setSize(AoAHandlers.PANEL_WIDTH, AoAHandlers.PANEL_HEIGHT + 38);
		AoAHandlers.makeTitle(panel, new WLabel(this.journal.getName()));

		// Input Icon
		panel.add(
			new WSprite(new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/add_formula.png")),
			4 + 1,
			AoAHandlers.BASIS - 1,
			AoAHandlers.BASIS - 2,
			AoAHandlers.BASIS - 2
		);

		// Input Slot
		panel.add(WItemSlot.of(this.inventory, 0), 4, AoAHandlers.BASIS - 2);

		this.searchBar = new WTextField() {
			@Override
			public void onKeyPressed(final int ch, final int key, final int modifiers) {
				super.onKeyPressed(ch, key, modifiers);
				HandlerJournal.this.formulaList.refresh(HandlerJournal.this.journal, this.getText());
			}

			@Override
			public void setSize(final int x, final int y) {
				super.setSize(x, y);
			}
		};
		panel.add(
			this.searchBar,
			AoAHandlers.BASIS + 8,
			AoAHandlers.BASIS - 4,
			6 * AoAHandlers.BASIS + 12,
			AoAHandlers.BASIS - 6
		);

		// Background
		panel.add(
			new WSprite(new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/rune_bg.png")),
			4,
			2 * AoAHandlers.BASIS + 10,
			9 * AoAHandlers.BASIS,
			5 * AoAHandlers.BASIS
		);

		this.formulaList = new WFormulaList(this.journal, hand);
		this.formulaList.refresh();
		panel.add(this.formulaList, 0, 2 * AoAHandlers.BASIS, 9 * AoAHandlers.BASIS + 8, 6 * AoAHandlers.BASIS - 2);

		this.clearButton = new WButton(new LiteralText("❌"));
		this.clearButton.setAlignment(HorizontalAlignment.CENTER);
		this.clearButton.setParent(panel);
		panel.add(
			this.clearButton,
			8 * AoAHandlers.BASIS + 6,
			AoAHandlers.BASIS - 4,
			AoAHandlers.BASIS + 2,
			AoAHandlers.BASIS + 2
		);
		this.clearButton.setOnClick(new @Nullable Runnable() {
			@Override
			public void run() {
				AoAClientNetworking.sendJournalSelectPacket(Registry.ITEM.getId(Items.AIR), hand);
			}
		});
		this.clearButton.setEnabled(ItemJournal.getFormula(this.journal) != Items.AIR);

		panel.add(this.createPlayerInventoryPanel(), AoAHandlers.OFFSET, 8 * AoAHandlers.BASIS);

		panel.validate(this);
	}

	@Override
	public void close(final PlayerEntity player) {
		this.dropInventory(player, this.inventory);
		super.close(player);
	}

	@Override
	public void onSlotClick(
		final int slotNumber, final int button, final SlotActionType action, final PlayerEntity player
	) {
		if (slotNumber >= 0 && slotNumber < this.slots.size()) {
			final Slot slot = this.getSlot(slotNumber);
			if (slot != null && slot.getStack().getItem() instanceof ItemJournal) {
				return;
			}
		}
		super.onSlotClick(slotNumber, button, action, player);
		this.tryAddPage();
		this.refresh(this.journal);
	}

	public void refresh(ItemStack journal) {
		if (journal == null) {
			journal = this.playerInventory.player.getStackInHand(this.hand);
		}
		this.journal = journal;
		if (this.journal.getItem() instanceof ItemJournal) {
			this.formulaList.refresh(this.journal, this.searchBar.getText());
			this.clearButton.setEnabled(ItemJournal.getFormula(this.journal) != Items.AIR);
		} else {
			this.close(this.playerInventory.player);
		}
	}

	public void tryAddPage() {
		final ItemStack stack = this.inventory.getStack(0);
		if (
			stack.getItem() instanceof AbstractItemFormula && ItemJournal.addFormula(this.journal, AoAHelper.getTarget(stack))
		) {
			stack.decrement(1);
			this.inventory.markDirty();
			this.playerInventory.markDirty();
		}
	}

}
