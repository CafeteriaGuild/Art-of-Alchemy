package dev.cafeteria.artofalchemy.blockentity;

import dev.cafeteria.artofalchemy.AoAConfig;
import dev.cafeteria.artofalchemy.essentia.EssentiaContainer;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class BlockEntityDissolverPlus extends BlockEntityDissolver {

	private float speedMod;
	private int tankSize;
	private float yield;

	public BlockEntityDissolverPlus(BlockPos pos, BlockState state) {
		super(AoABlockEntities.DISSOLVER_PLUS, pos, state);
		AoAConfig.DissolverSettings settings = AoAConfig.get().dissolverSettings;
		this.tankSize = settings.tankPlus;
		this.speedMod = settings.speedPlus;
		this.yield = settings.yieldPlus;
		this.maxAlkahest = getTankSize();
		this.essentia = new EssentiaContainer()
				.setCapacity(getTankSize())
				.setInput(false)
				.setOutput(true);
	}

	@Override
	public float getSpeedMod() {
		return speedMod;
	}

	@Override
	public int getTankSize() {
		return tankSize;
	}

	@Override
	public float getEfficiency() {
		return yield;
	}

}
