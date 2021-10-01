package dev.cafeteria.artofalchemy.util;

import net.minecraft.util.Rarity;

public enum MateriaRank {
	OMEGA(7, Rarity.EPIC), S(6, Rarity.RARE), A(5, Rarity.UNCOMMON), B(4), C(3), D(2), E(1), F(0);

	static MateriaRank ofTier(final int tier) {
		for (int i = 0; i < MateriaRank.values().length; i++) {
			final MateriaRank rank = MateriaRank.values()[i];
			if (tier == rank.tier) {
				return rank;
			}
		}
		return null;
	}

	public final int tier;

	public final Rarity rarity;

	MateriaRank(final int tier) {
		this.tier = tier;
		this.rarity = Rarity.COMMON;
	}

	MateriaRank(final int tier, final Rarity rarity) {
		this.tier = tier;
		this.rarity = rarity;
	}

}
