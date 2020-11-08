package com.cumulusmc.artofalchemy.transport;

import com.cumulusmc.artofalchemy.essentia.EssentiaContainer;
import net.minecraft.util.math.Direction;

public interface HasEssentia {

	EssentiaContainer getContainer(Direction dir);
	EssentiaContainer getContainer(int id);
	int getNumContainers();

	default EssentiaContainer getContainer() {
		return getContainer(0);
	}

}
