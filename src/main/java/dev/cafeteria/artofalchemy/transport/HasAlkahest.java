package dev.cafeteria.artofalchemy.transport;

import dev.cafeteria.artofalchemy.fluid.AoAFluids;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

@SuppressWarnings("deprecation") // Experimental API
public interface HasAlkahest {

	default boolean setAlkahest(final int amount) {
		if ((amount > this.getAlkahestTank().getCapacity()) || (amount < 0)) {
			return false;
		}
		final Transaction trans = Transaction.openOuter();
		this.getAlkahestTank().extract(FluidVariant.of(AoAFluids.ALKAHEST), this.getAlkahest(), trans);
		this.getAlkahestTank().insert(FluidVariant.of(AoAFluids.ALKAHEST), amount, trans);
		trans.commit();
		return true;
	}

	default boolean addAlkahest(final long amount) {
		if (((this.getAlkahest() + amount) > this.getAlkahestTank().getCapacity()) || ((this.getAlkahest() + amount) < 0)) {
			return false;
		}
		final Transaction trans = Transaction.openOuter();
		if (amount > 0) { // insert requires positive value
			this.getAlkahestTank().insert(FluidVariant.of(AoAFluids.ALKAHEST), amount, trans);
		} else {
			this.getAlkahestTank().extract(FluidVariant.of(AoAFluids.ALKAHEST), -amount, trans);
		}
		trans.commit();
		return true;
	}

	default long getAlkahest() {
		return this.getAlkahestTank().getAmount();
	}

	default long getAlkahestCapacity() {
		return FluidConstants.BUCKET * 4;
	}

	SingleVariantStorage<FluidVariant> getAlkahestTank();

	default boolean hasAlkahest() {
		return this.getAlkahest() > 0;
	}

	default SingleVariantStorage<FluidVariant> makeAlkahestTank() {
		return new SingleVariantStorage<>() {
			@Override
			protected FluidVariant getBlankVariant() {
				return FluidVariant.of(AoAFluids.ALKAHEST);
			}

			@Override
			protected long getCapacity(final FluidVariant variant) {
				return HasAlkahest.this.getAlkahestCapacity();
			}

		};
	}

}
