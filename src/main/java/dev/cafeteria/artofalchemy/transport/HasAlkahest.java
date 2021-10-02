package dev.cafeteria.artofalchemy.transport;

import dev.cafeteria.artofalchemy.fluid.AoAFluids;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

@SuppressWarnings("deprecation") // Experimental API
public interface HasAlkahest {
	default SingleVariantStorage<FluidVariant> makeAlkahestTank() {
		return makeAlkahestTank(FluidConstants.BUCKET * 4);
	}

	default SingleVariantStorage<FluidVariant> makeAlkahestTank(final long capacity) {
		return new SingleVariantStorage<FluidVariant>() {
			@Override
			protected FluidVariant getBlankVariant() {
				return FluidVariant.of(AoAFluids.ALKAHEST);
			}

			@Override
			protected long getCapacity(FluidVariant variant) {
				return capacity;
			}

		};
	}

	default boolean addAlkahest(final long amount) {
		if (this.getAlkahest() + amount > this.getAlkahestTank().getCapacity() || this.getAlkahest() + amount < 0)
			return false;
		final Transaction trans = Transaction.openOuter();
		this.getAlkahestTank().insert(FluidVariant.of(AoAFluids.ALKAHEST), amount, trans);
		trans.commit();
		return true;
	}

	SingleVariantStorage<FluidVariant> getAlkahestTank();

	default long getAlkahest() {
		return this.getAlkahestTank().getAmount();
	}

	default long getAlkahestCapacity() {
		return this.getAlkahestTank().getCapacity();
	}

	default boolean hasAlkahest() {
		return this.getAlkahest() > 0;
	}

	default boolean setAlkahest(int amount) {
		if (amount > getAlkahestTank().getCapacity() || amount < 0)
			return false;
		final Transaction trans = Transaction.openOuter();
		this.getAlkahestTank().extract(FluidVariant.of(AoAFluids.ALKAHEST), getAlkahest(), trans);
		this.getAlkahestTank().insert(FluidVariant.of(AoAFluids.ALKAHEST), amount, trans);
		trans.commit();
		return true;
	}

}
