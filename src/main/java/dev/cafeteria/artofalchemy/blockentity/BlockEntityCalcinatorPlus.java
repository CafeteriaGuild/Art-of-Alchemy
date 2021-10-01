package dev.cafeteria.artofalchemy.blockentity;

import dev.cafeteria.artofalchemy.AoAConfig;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class BlockEntityCalcinatorPlus extends BlockEntityCalcinator {

	private final int operationTime;
	private final float yield;

	public BlockEntityCalcinatorPlus(final BlockPos pos, final BlockState state) {
		super(AoABlockEntities.CALCINATOR_PLUS, pos, state);
		final AoAConfig.CalcinatorSettings settings = AoAConfig.get().calcinatorSettings;
		this.operationTime = settings.opTimePlus;
		this.yield = settings.yieldPlus;
		this.maxProgress = this.getOperationTime();
	}

	@Override
	public int getOperationTime() {
		return this.operationTime;
	}

	@Override
	public float getYield() {
		return this.yield;
	}

}
