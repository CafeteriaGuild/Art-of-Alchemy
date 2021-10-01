package dev.cafeteria.artofalchemy.item;

import dev.cafeteria.artofalchemy.block.BlockMateria;
import dev.cafeteria.artofalchemy.util.MateriaRank;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

public class BlockItemMateria extends BlockItem {

	protected BlockMateria block;

	public BlockItemMateria(final BlockMateria block, final Settings settings) {
		super(block, settings.rarity(block.getRank().rarity));
		this.block = block;
	}

	public MateriaRank getRank() {
		return this.block.getRank();
	}

	public int getTier() {
		if (this.getRank() == null) {
			return 0;
		} else {
			return this.getRank().tier;
		}
	}

	@Override
	public boolean hasGlint(final ItemStack stack) {
		return stack.hasEnchantments() || (this.getTier() >= 6);
	}

}
