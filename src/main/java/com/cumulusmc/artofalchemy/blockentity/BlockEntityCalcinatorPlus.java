package com.cumulusmc.artofalchemy.blockentity;

import com.cumulusmc.artofalchemy.AoAConfig;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class BlockEntityCalcinatorPlus extends BlockEntityCalcinator {

	private int operationTime;
	private float yield;

	public BlockEntityCalcinatorPlus(BlockPos pos, BlockState state) {
		super(AoABlockEntities.CALCINATOR_PLUS, pos, state);
		AoAConfig.CalcinatorSettings settings = AoAConfig.get().calcinatorSettings;
		operationTime = settings.opTimePlus;
		this.yield = settings.yieldPlus;
		maxProgress = getOperationTime();
	}

	@Override
	public int getOperationTime() {
		return operationTime;
	}

	@Override
	public float getYield() {
		return yield;
	}

}
