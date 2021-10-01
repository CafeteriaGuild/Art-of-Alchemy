package dev.cafeteria.artofalchemy.item;

import dev.cafeteria.artofalchemy.util.MateriaRank;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemMateria extends Item {

	private final MateriaRank rank;

	public ItemMateria(final Settings settings, final MateriaRank rank) {
		super(settings.rarity(rank.rarity));
		this.rank = rank;
	}

	public MateriaRank getRank() {
		return this.rank;
	}

	public int getTier() {
		if (this.rank == null) {
			return 0;
		} else {
			return this.rank.tier;
		}
	}

	@Override
	public boolean hasGlint(final ItemStack stack) {
		return stack.hasEnchantments() || this.getTier() >= 6;
	}

}
