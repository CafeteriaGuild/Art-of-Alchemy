package dev.cafeteria.artofalchemy.blockentity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import dev.cafeteria.artofalchemy.AoAConfig;
import dev.cafeteria.artofalchemy.essentia.Essentia;
import dev.cafeteria.artofalchemy.essentia.EssentiaContainer;
import dev.cafeteria.artofalchemy.transport.HasEssentia;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

abstract public class AbstractBlockEntityCentrifuge extends BlockEntity
	implements HasEssentia, BlockEntityTicker<AbstractBlockEntityCentrifuge> {

	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

	protected static EssentiaContainer outputOf(final Essentia... essentia) {
		final Set<Essentia> whitelist = new HashSet<>(Arrays.asList(essentia));
		return new EssentiaContainer().setCapacity(AoAConfig.get().centrifugeCapacity).setOutput(true).setInput(false)
			.setWhitelist(whitelist).setWhitelistEnabled(true);
	}

	protected EssentiaContainer input = new EssentiaContainer().setCapacity(AoAConfig.get().centrifugeCapacity)
		.setInput(true).setOutput(false);

	protected EssentiaContainer[] outputs;

	public AbstractBlockEntityCentrifuge(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
		super(type, pos, state);
	}

	@Override
	public EssentiaContainer getContainer(final Direction dir) {
		int horiz = dir.getHorizontal();
		if (horiz == -1) {
			return this.input;
		} else {
			horiz = ((horiz - this.world.getBlockState(this.pos).get(AbstractBlockEntityCentrifuge.FACING).getHorizontal())
				+ 4) % 4;
			return this.outputs[horiz];
		}
	}

	@Override
	public EssentiaContainer getContainer(final int id) {
		if (id == 0) {
			return this.input;
		} else if ((id > 0) && (id <= this.getNumContainers())) {
			return this.outputs[id - 1];
		} else {
			return null;
		}
	}

	@Override
	public int getNumContainers() {
		return this.outputs.length + 1;
	}

	@Override
	public void tick(
		final World world, final BlockPos pos, final BlockState state, final AbstractBlockEntityCentrifuge blockEntity
	) {
		if (!this.input.isEmpty()) {
			for (final EssentiaContainer output : this.outputs) {
				this.input.pushContents(output, true);
			}
		}
	}
}
