package dev.cafeteria.artofalchemy.essentia;

import dev.cafeteria.artofalchemy.ArtOfAlchemy;
import net.minecraft.util.registry.Registry;

public class AoAEssentia {

	public static final Essentia MERCURY = new Essentia(0x9291CC);
	public static final Essentia VENUS = new Essentia(0xEF91CF);
	public static final Essentia TELLUS = new Essentia(0x20761C);
	public static final Essentia MARS = new Essentia(0xB00A09);
	public static final Essentia JUPITER = new Essentia(0xE98765);
	public static final Essentia SATURN = new Essentia(0xD2D88C);
	public static final Essentia URANUS = new Essentia(0x00B3AD);
	public static final Essentia NEPTUNE = new Essentia(0x283B78);
	public static final Essentia APOLLO = new Essentia(0xDCB500);
	public static final Essentia DIANA = new Essentia(0x5BB0F0);
	public static final Essentia CERES = new Essentia(0x5D3E22);
	public static final Essentia PLUTO = new Essentia(0x185665);
	public static final Essentia VOID = new Essentia(0x5E1CD9);

	public static Essentia register(final String name, final Essentia essentia) {
		return Registry.register(RegistryEssentia.INSTANCE, ArtOfAlchemy.id(name), essentia);
	}

	public static void registerEssentia() {
		AoAEssentia.register("mercury", AoAEssentia.MERCURY);
		AoAEssentia.register("venus", AoAEssentia.VENUS);
		AoAEssentia.register("tellus", AoAEssentia.TELLUS);
		AoAEssentia.register("mars", AoAEssentia.MARS);
		AoAEssentia.register("jupiter", AoAEssentia.JUPITER);
		AoAEssentia.register("saturn", AoAEssentia.SATURN);
		AoAEssentia.register("uranus", AoAEssentia.URANUS);
		AoAEssentia.register("neptune", AoAEssentia.NEPTUNE);
		AoAEssentia.register("apollo", AoAEssentia.APOLLO);
		AoAEssentia.register("diana", AoAEssentia.DIANA);
		AoAEssentia.register("ceres", AoAEssentia.CERES);
		AoAEssentia.register("pluto", AoAEssentia.PLUTO);
		AoAEssentia.register("void", AoAEssentia.VOID);
	}

}
