package dev.cafeteria.artofalchemy.transport;

public interface HasAlkahest {

	default boolean addAlkahest(final int amount) {
		return this.setAlkahest(this.getAlkahest() + amount);
	}

	int getAlkahest();

	default boolean hasAlkahest() {
		return this.getAlkahest() > 0;
	}

	boolean setAlkahest(int amount);

}
