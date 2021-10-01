package dev.cafeteria.artofalchemy.blockentity;

import dev.cafeteria.artofalchemy.AoAConfig;
import dev.cafeteria.artofalchemy.block.AoABlocks;
import dev.cafeteria.artofalchemy.essentia.EssentiaContainer;
import dev.cafeteria.artofalchemy.network.AoANetworking;
import dev.cafeteria.artofalchemy.transport.HasEssentia;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class BlockEntityTank extends BlockEntity
	implements BlockEntityTicker<BlockEntityTank>, HasEssentia, BlockEntityClientSerializable {

	protected EssentiaContainer essentia = new EssentiaContainer().setCapacity(AoAConfig.get().tankCapacity)
		.setInput(true).setOutput(true);

	public BlockEntityTank(final BlockPos pos, final BlockState state) {
		super(AoABlockEntities.TANK, pos, state);
	}

	@Override
	public void fromClientTag(final NbtCompound tag) {
		this.readNbt(tag);
	}

	@Override
	public EssentiaContainer getContainer(final Direction dir) {
		return this.essentia;
	}

	@Override
	public EssentiaContainer getContainer(final int id) {
		if (id == 0) {
			return this.essentia;
		} else {
			return null;
		}
	}

	@Override
	public int getNumContainers() {
		return 1;
	}

	@Override
	public void markDirty() {
		super.markDirty();
		if (!this.world.isClient()) {
			this.sync();
		}
	}

	@Override
	public void readNbt(final NbtCompound tag) {
		super.readNbt(tag);
		this.essentia = new EssentiaContainer(tag.getCompound("essentia"));
	}

	@Override
	public void sync() {
		AoANetworking.sendEssentiaPacket(this.world, this.pos, 0, this.essentia);
		BlockEntityClientSerializable.super.sync();
	}

	@Override
	public void tick(final World world, final BlockPos pos, final BlockState state, final BlockEntityTank blockEntity) {
		if (
			!this.essentia.isEmpty() && world.getBlockState(pos).getBlock() == AoABlocks.TANK
				&& world.getBlockState(pos.down()).getBlock() == AoABlocks.TANK
		) {
			final BlockEntity other = world.getBlockEntity(pos.down());
			if (other instanceof BlockEntityTank) {
				this.essentia.mixPushContents(((BlockEntityTank) other).essentia);
				this.markDirty();
				other.markDirty();
			}
		}
	}

	@Override
	public NbtCompound toClientTag(final NbtCompound tag) {
		return this.writeNbt(tag);
	}

	@Override
	public NbtCompound writeNbt(final NbtCompound tag) {
		tag.put("essentia", this.essentia.writeNbt());
		return super.writeNbt(tag);
	}
}
