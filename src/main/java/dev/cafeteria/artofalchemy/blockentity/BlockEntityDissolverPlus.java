package dev.cafeteria.artofalchemy.blockentity;

import dev.cafeteria.artofalchemy.AoAConfig;
import dev.cafeteria.artofalchemy.essentia.EssentiaContainer;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class BlockEntityDissolverPlus extends BlockEntityDissolver {

	private final float speedMod;
	private final int tankSize;
	private final float yield;

	public BlockEntityDissolverPlus(final BlockPos pos, final BlockState state) {
		super(AoABlockEntities.DISSOLVER_PLUS, pos, state);
		final AoAConfig.DissolverSettings settings = AoAConfig.get().dissolverSettings;
		this.tankSize = settings.tankPlus;
		this.speedMod = settings.speedPlus;
		this.yield = settings.yieldPlus;
		this.maxAlkahest = this.getTankSize();
		this.essentia = new EssentiaContainer().setCapacity(this.getTankSize()).setInput(false).setOutput(true);
	}

	@Override
	public float getEfficiency() {
		return this.yield;
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
