package dev.cafeteria.artofalchemy.transport;

import dev.cafeteria.artofalchemy.essentia.EssentiaContainer;
import net.minecraft.util.math.Direction;

public interface HasEssentia {

	default EssentiaContainer getContainer() {
		return this.getContainer(0);
	}

	EssentiaContainer getContainer(Direction dir);

	EssentiaContainer getContainer(int id);

	int getNumContainers();

}
