package dev.cafeteria.artofalchemy.essentia;

import java.util.function.BiConsumer;

import dev.cafeteria.artofalchemy.ArtOfAlchemy;
import dev.cafeteria.artofalchemy.mixin.RegistryAccessor;
import com.mojang.serialization.Lifecycle;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;

public class RegistryEssentia extends SimpleRegistry<Essentia> {

	public static final RegistryKey<Registry<Essentia>> KEY = RegistryKey.ofRegistry(ArtOfAlchemy.id("essentia"));
	public static final RegistryEssentia INSTANCE = RegistryAccessor.create(KEY, new RegistryEssentia(), null, Lifecycle.stable());

	public RegistryEssentia() {
		super(KEY, Lifecycle.stable());
	}

	public void forEach(BiConsumer<Essentia, Identifier> function) {
		for (Essentia essentia : this) {
			function.accept(essentia, getId(essentia));
		}
	}

}
