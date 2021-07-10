package com.cumulusmc.artofalchemy.blockentity;

import com.cumulusmc.artofalchemy.AoAConfig;
import com.cumulusmc.artofalchemy.essentia.EssentiaContainer;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class BlockEntitySynthesizerPlus extends BlockEntitySynthesizer {

	private int maxTier;
	private float speedMod;
	private int tankSize;

	public BlockEntitySynthesizerPlus(BlockPos pos, BlockState state) {
		super(AoABlockEntities.SYNTHESIZER_PLUS, pos, state);
		AoAConfig.SynthesizerSettings settings = AoAConfig.get().synthesizerSettings;
		tankSize = settings.tankPlus;
		speedMod = settings.speedPlus;
		maxTier = settings.maxTierPlus.tier;
		essentiaContainer = new EssentiaContainer()
				.setCapacity(getTankSize())
				.setInput(true)
				.setOutput(false);
	}

	@Override
	public int getMaxTier() {
		return maxTier;
	}

	@Override
	public float getSpeedMod() {
		return speedMod;
	}

	@Override
	public int getTankSize() {
		return tankSize;
	}

}
