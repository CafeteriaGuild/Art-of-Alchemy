package dev.cafeteria.artofalchemy.essentia;

import java.util.function.BiConsumer;

import com.mojang.serialization.Lifecycle;

import dev.cafeteria.artofalchemy.ArtOfAlchemy;
import dev.cafeteria.artofalchemy.mixin.RegistryAccessor;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;

public class RegistryEssentia extends SimpleRegistry<Essentia> {

	public static final RegistryKey<Registry<Essentia>> KEY = RegistryKey.ofRegistry(ArtOfAlchemy.id("essentia"));
	public static final RegistryEssentia INSTANCE = RegistryAccessor
		.create(RegistryEssentia.KEY, new RegistryEssentia(), null, Lifecycle.stable());

	public RegistryEssentia() {
		super(RegistryEssentia.KEY, Lifecycle.stable());
	}

	public void forEach(final BiConsumer<Essentia, Identifier> function) {
		for (final Essentia essentia : this) {
			function.accept(essentia, this.getId(essentia));
		}
	}

}
