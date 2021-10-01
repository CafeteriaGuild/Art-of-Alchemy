package dev.cafeteria.artofalchemy.block;

import java.util.Random;

import dev.cafeteria.artofalchemy.essentia.Essentia;
import dev.cafeteria.artofalchemy.fluid.AoAFluids;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockEssentia extends FluidBlock {

	public static final Settings SETTINGS = Settings.copy(Blocks.WATER).luminance(state -> 9);
	protected static Essentia essentia;

	public BlockEssentia(final Essentia essentia) {
		super(AoAFluids.ESSENTIA_FLUIDS.get(essentia), BlockEssentia.SETTINGS);
	}

	public Essentia getEssentia() {
		return BlockEssentia.essentia;
	}

	@Override
	public void onEntityCollision(final BlockState state, final World world, final BlockPos pos, final Entity entity) {
		entity.damage(DamageSource.MAGIC, 2);
		// world.playSound(entity.getX(), entity.getY(), entity.getZ(),
		// SoundEvents.ENTITY_GENERIC_BURN,
		// entity.getSoundCategory(), 1.0F, 1.0F, false);
		world.addParticle(ParticleTypes.LARGE_SMOKE, entity.getX(), entity.getY(), entity.getZ(), 0.0D, 0.0D, 0.0D);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void randomDisplayTick(final BlockState state, final World world, final BlockPos pos, final Random random) {
		final double x = pos.getX() + random.nextDouble();
		final double y = pos.getY() + random.nextDouble();
		final double z = pos.getZ() + random.nextDouble();
		world.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
		super.randomDisplayTick(state, world, pos, random);
	}

}
