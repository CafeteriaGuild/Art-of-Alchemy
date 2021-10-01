package dev.cafeteria.artofalchemy.fluid;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import dev.cafeteria.artofalchemy.ArtOfAlchemy;
import dev.cafeteria.artofalchemy.essentia.Essentia;
import dev.cafeteria.artofalchemy.essentia.RegistryEssentia;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class AoAFluids {

	public static FlowableFluid ALKAHEST;
	public static FlowableFluid ALKAHEST_FLOWING;
	public static final Map<Essentia, FlowableFluid> ESSENTIA_FLUIDS = new HashMap<>();
	public static final Map<Essentia, FlowableFluid> ESSENTIA_FLUIDS_FLOWING = new HashMap<>();

	public static FlowableFluid register(final Identifier id, final FlowableFluid fluid) {
		return Registry.register(Registry.FLUID, id, fluid);
	}

	public static FlowableFluid register(final String name, final FlowableFluid fluid) {
		return AoAFluids.register(ArtOfAlchemy.id(name), fluid);
	}

	public static void registerFluids() {
		AoAFluids.ALKAHEST = AoAFluids.register("alkahest", new FluidAlkahest.Still());
		AoAFluids.ALKAHEST_FLOWING = AoAFluids.register("flowing_alkahest", new FluidAlkahest.Flowing());

		// Register essentia fluids; add-on essentia fluids will be registered to THEIR
		// namespace
		RegistryEssentia.INSTANCE.forEach(new BiConsumer<Essentia, Identifier>() {
			@Override
			public void accept(final Essentia essentia, final Identifier id) {
				final Identifier stillId = new Identifier(id.getNamespace(), "essentia_" + id.getPath());
				final Identifier flowId = new Identifier(id.getNamespace(), "flowing_essentia_" + id.getPath());
				AoAFluids.ESSENTIA_FLUIDS.put(essentia, AoAFluids.register(stillId, new FluidEssentia.Still(essentia)));
				AoAFluids.ESSENTIA_FLUIDS_FLOWING
					.put(essentia, AoAFluids.register(flowId, new FluidEssentia.Flowing(essentia)));
			}
		});
	}

}
