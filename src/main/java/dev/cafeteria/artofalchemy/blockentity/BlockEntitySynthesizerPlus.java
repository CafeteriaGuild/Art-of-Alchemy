package dev.cafeteria.artofalchemy.blockentity;

import dev.cafeteria.artofalchemy.AoAConfig;
import dev.cafeteria.artofalchemy.essentia.EssentiaContainer;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class BlockEntitySynthesizerPlus extends BlockEntitySynthesizer {

	private final int maxTier;
	private final float speedMod;
	private final int tankSize;

	public BlockEntitySynthesizerPlus(final BlockPos pos, final BlockState state) {
		super(AoABlockEntities.SYNTHESIZER_PLUS, pos, state);
		final AoAConfig.SynthesizerSettings settings = AoAConfig.get().synthesizerSettings;
		this.tankSize = settings.tankPlus;
		this.speedMod = settings.speedPlus;
		this.maxTier = settings.maxTierPlus.tier;
		this.essentiaContainer = new EssentiaContainer().setCapacity(this.getTankSize()).setInput(true).setOutput(false);
	}

	@Override
	public int getMaxTier() {
		return this.maxTier;
	}

	@Override
	public float getSpeedMod() {
		return this.speedMod;
	}

	@Override
	public int getTankSize() {
		return this.tankSize;
	}

}
