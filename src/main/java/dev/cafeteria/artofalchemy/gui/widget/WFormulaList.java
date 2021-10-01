package dev.cafeteria.artofalchemy.gui.widget;

import java.util.Comparator;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import dev.cafeteria.artofalchemy.gui.handler.AoAHandlers;
import dev.cafeteria.artofalchemy.item.ItemJournal;
import io.github.cottonmc.cotton.gui.widget.WListPanel;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.registry.Registry;

public class WFormulaList extends WListPanel<Item, WFormulaListItem> {

	protected ItemStack journal;
	private final Hand hand;

	public WFormulaList(final ItemStack journal, final Hand hand) {
		super(ItemJournal.getEntries(journal), new Supplier<WFormulaListItem>() {
			@Override
			public WFormulaListItem get() {
				return new WFormulaListItem(journal, hand);
			}
		}, null);
		this.hand = hand;
		this.cellHeight = AoAHandlers.BASIS;
		this.journal = journal;
		this.configurator = new BiConsumer<Item, WFormulaListItem>() {
			@Override
			public void accept(final Item formula, final WFormulaListItem listItem) {
				listItem.refresh(WFormulaList.this.journal, formula);
				listItem.setSize(8 * AoAHandlers.BASIS, WFormulaList.this.cellHeight);
			}
		};
	}

	protected void reconfigure() {
		for (final Map.Entry<Item, WFormulaListItem> entry : this.configured.entrySet()) {
			this.configurator.accept(entry.getKey(), entry.getValue());
		}
	}

	public void refresh() {
		this.refresh(this.journal, "");
	}

	public void refresh(final ItemStack journal, final String filter) {
		this.journal = journal;
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			this.data = ItemJournal.getEntries(journal);
			this.data.sort(new Comparator<Item>() {
				@Override
				public int compare(final Item item1, final Item item2) {
					return item1.getName().toString().compareToIgnoreCase(item2.getName().toString());
				}
			});
			this.data.removeIf(new Predicate<Item>() {
				@Override
				public boolean test(final Item item) {
					final String lcFilter = filter.toLowerCase();
					if (item.getName().asString().toLowerCase().contains(lcFilter)) {
						return false;
					} else {
						return !Registry.ITEM.getId(item).getPath().contains(lcFilter);
					}
				}
			});
		}
		this.reconfigure();
		this.layout();
	}

}
