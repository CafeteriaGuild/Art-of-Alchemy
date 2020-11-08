package com.cumulusmc.artofalchemy.block;

import com.cumulusmc.artofalchemy.blockentity.BlockEntityElementCentrifuge;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.BlockView;

public class BlockElementCentrifuge extends AbstractBlockCentrifuge {
	@Override
	public BlockEntity createBlockEntity(BlockView world) {
		return new BlockEntityElementCentrifuge();
	}
}
