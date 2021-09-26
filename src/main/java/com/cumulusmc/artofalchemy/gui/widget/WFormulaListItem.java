package com.cumulusmc.artofalchemy.gui.widget;

import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;

import com.cumulusmc.artofalchemy.gui.handler.AoAHandlers;
import com.cumulusmc.artofalchemy.item.ItemJournal;
import com.cumulusmc.artofalchemy.network.AoAClientNetworking;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;

public class WFormulaListItem extends WPlainPanel {

	private ItemStack journal;
	private Item formula = Items.AIR;
	private final WItemScalable itemDisplay;
	private final WLabel formulaLabel;
	private final Hand hand;
//	private final WLabel typeLabel;
	private WButton setButton;

	@SuppressWarnings("MethodCallSideOnly")
	public WFormulaListItem(ItemStack journal, Item formula, Hand hand) {
		super();
		this.journal = journal;
		this.hand = hand;

		List<ItemStack> itemStackList = new ArrayList<>();
		itemStackList.add(new ItemStack(formula));
		itemDisplay = new WItemScalable(itemStackList);
		itemDisplay.setParent(this);
		add(itemDisplay, 0, 0, 16, 16);

		formulaLabel = new WLabel(itemStackList.get(0).getName());
		formulaLabel.setHorizontalAlignment(HorizontalAlignment.LEFT);
		formulaLabel.setParent(this);
		add(formulaLabel, 16, 3);

		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			setButton = new WButton(new LiteralText("✔"));
			setButton.setAlignment(HorizontalAlignment.CENTER);
			setButton.setParent(this);
			add(
				setButton,
				(8 * AoAHandlers.BASIS) - 8,
				-4,
				AoAHandlers.BASIS + 2,
				AoAHandlers.BASIS + 2
			);
			setButton.setOnClick(() -> {
				AoAClientNetworking.sendJournalSelectPacket(Registry.ITEM.getId(this.formula), hand);
			});
		}

		refresh(journal, formula);
	}

	public WFormulaListItem(ItemStack journal, Hand hand) {
		this(journal, Items.AIR, hand);
	}

	public void refresh(ItemStack journal, Item formula) {
		this.formula = formula;
		itemDisplay.getItems().clear();
		itemDisplay.getItems().add(new ItemStack(formula));
		formulaLabel.setText(itemDisplay.getItems().get(0).getName());
		boolean selected = ItemJournal.getFormula(journal) == formula;
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			setButton.setEnabled(!selected);
		}
	}
}
