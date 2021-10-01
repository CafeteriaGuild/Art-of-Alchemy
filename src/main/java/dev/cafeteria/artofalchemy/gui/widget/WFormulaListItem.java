package dev.cafeteria.artofalchemy.gui.widget;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import dev.cafeteria.artofalchemy.gui.handler.AoAHandlers;
import dev.cafeteria.artofalchemy.item.ItemJournal;
import dev.cafeteria.artofalchemy.network.AoAClientNetworking;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.util.registry.Registry;

public class WFormulaListItem extends WPlainPanel {

	private final ItemStack journal;
	private Item formula = Items.AIR;
	private final WItemScalable itemDisplay;
	private final WLabel formulaLabel;
	private final Hand hand;
	// private final WLabel typeLabel;
	private WButton setButton;

	public WFormulaListItem(final ItemStack journal, final Hand hand) {
		this(journal, Items.AIR, hand);
	}

	public WFormulaListItem(final ItemStack journal, final Item formula, final Hand hand) {
		this.journal = journal;
		this.hand = hand;

		final List<ItemStack> itemStackList = new ArrayList<>();
		itemStackList.add(new ItemStack(formula));
		this.itemDisplay = new WItemScalable(itemStackList);
		this.itemDisplay.setParent(this);
		this.add(this.itemDisplay, 0, 0, 16, 16);

		this.formulaLabel = new WLabel(itemStackList.get(0).getName());
		this.formulaLabel.setHorizontalAlignment(HorizontalAlignment.LEFT);
		this.formulaLabel.setParent(this);
		this.add(this.formulaLabel, 16, 3);

		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			this.setButton = new WButton(new LiteralText("✔"));
			this.setButton.setAlignment(HorizontalAlignment.CENTER);
			this.setButton.setParent(this);
			this.add(this.setButton, 8 * AoAHandlers.BASIS - 8, -4, AoAHandlers.BASIS + 2, AoAHandlers.BASIS + 2);
			this.setButton.setOnClick(new @Nullable Runnable() {
				@Override
				public void run() {
					AoAClientNetworking.sendJournalSelectPacket(Registry.ITEM.getId(WFormulaListItem.this.formula), hand);
				}
			});
		}

		this.refresh(journal, formula);
	}

	public void refresh(final ItemStack journal, final Item formula) {
		this.formula = formula;
		this.itemDisplay.getItems().clear();
		this.itemDisplay.getItems().add(new ItemStack(formula));
		this.formulaLabel.setText(this.itemDisplay.getItems().get(0).getName());
		final boolean selected = ItemJournal.getFormula(journal) == formula;
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			this.setButton.setEnabled(!selected);
		}
	}
}
