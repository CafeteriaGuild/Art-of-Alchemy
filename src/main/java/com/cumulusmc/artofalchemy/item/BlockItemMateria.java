package com.cumulusmc.artofalchemy.item;

import com.cumulusmc.artofalchemy.util.MateriaRank;
import com.cumulusmc.artofalchemy.block.BlockMateria;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

public class BlockItemMateria extends BlockItem {

	protected BlockMateria block;

	public BlockItemMateria(BlockMateria block, Settings settings) {
		super(block, settings.rarity(block.getRank().rarity));
		this.block = block;
	}

	public MateriaRank getRank() {
		return block.getRank();
	}

	public int getTier() {
		if (getRank() == null) {
			return 0;
		} else {
			return getRank().tier;
		}
	}

	@Override
	public boolean hasGlint(ItemStack stack) {
		return (stack.hasEnchantments() || getTier() >= 6);
	}

}
