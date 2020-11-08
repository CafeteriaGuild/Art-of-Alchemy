package com.cumulusmc.artofalchemy.block;

import com.cumulusmc.artofalchemy.blockentity.BlockEntityAstroCentrifuge;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.BlockView;

public class BlockAstroCentrifuge extends AbstractBlockCentrifuge {
	@Override
	public BlockEntity createBlockEntity(BlockView world) {
		return new BlockEntityAstroCentrifuge();
	}
}
