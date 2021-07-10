package com.cumulusmc.artofalchemy.blockentity;

import com.cumulusmc.artofalchemy.AoAConfig;
import com.cumulusmc.artofalchemy.block.AoABlocks;
import com.cumulusmc.artofalchemy.essentia.EssentiaContainer;
import com.cumulusmc.artofalchemy.network.AoANetworking;
import com.cumulusmc.artofalchemy.transport.HasEssentia;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class BlockEntityTank extends BlockEntity implements BlockEntityTicker<BlockEntityTank>, HasEssentia, BlockEntityClientSerializable {

	protected EssentiaContainer essentia = new EssentiaContainer()
			.setCapacity(AoAConfig.get().tankCapacity)
			.setInput(true)
			.setOutput(true);

	public BlockEntityTank(BlockPos pos, BlockState state) {
		super(AoABlockEntities.TANK, pos, state);
	}

	@Override
	public EssentiaContainer getContainer(Direction dir) {
		return essentia;
	}

	@Override
	public EssentiaContainer getContainer(int id) {
		if (id == 0) {
			return essentia;
		} else {
			return null;
		}
	}

	@Override
	public int getNumContainers() {
		return 1;
	}

	@Override
	public NbtCompound writeNbt(NbtCompound tag) {
		tag.put("essentia", essentia.writeNbt());
		return super.writeNbt(tag);
	}

	@Override
	public void readNbt(NbtCompound tag) {
		super.readNbt(tag);
		essentia = new EssentiaContainer(tag.getCompound("essentia"));
	}

	@Override
	public void fromClientTag(NbtCompound tag) {
		readNbt(tag);
	}

	@Override
	public NbtCompound toClientTag(NbtCompound tag) {
		return writeNbt(tag);
	}

	@Override
	public void markDirty() {
		super.markDirty();
		if (!world.isClient()) {
			sync();
		}
	}

	@Override
	public void sync() {
		AoANetworking.sendEssentiaPacket(world, pos, 0, essentia);
		BlockEntityClientSerializable.super.sync();
	}

	@Override
	public void tick(World world, BlockPos pos, BlockState state, BlockEntityTank blockEntity) {
		if (!essentia.isEmpty() && world.getBlockState(pos).getBlock() == AoABlocks.TANK && world.getBlockState(pos.down()).getBlock() == AoABlocks.TANK) {
			BlockEntity other = world.getBlockEntity(pos.down());
			if (other instanceof BlockEntityTank) {
				essentia.mixPushContents(((BlockEntityTank) other).essentia);
				this.markDirty();
				other.markDirty();
			}
		}
	}
}
