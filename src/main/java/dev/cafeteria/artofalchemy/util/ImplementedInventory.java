package dev.cafeteria.artofalchemy.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

/**
 * A simple {@code Inventory} implementation with only default methods + an item
 * list getter.
 *
 * Originally by Juuz
 */
public interface ImplementedInventory extends Inventory {
	// Creation
	/**
	 * Creates an inventory from the item list.
	 */
	static ImplementedInventory of(final DefaultedList<ItemStack> items) {
		return () -> items;
	}

	/**
	 * Creates a new inventory with the size.
	 */
	static ImplementedInventory ofSize(final int size) {
		return ImplementedInventory.of(DefaultedList.ofSize(size, ItemStack.EMPTY));
	}

	@Override
	default boolean canPlayerUse(final PlayerEntity player) {
		return true;
	}

	/**
	 * Clears {@linkplain #getItems() the item list}}.
	 */
	@Override
	default void clear() {
		this.getItems().clear();
	}

	/**
	 * Gets the item list of this inventory. Must return the same instance every
	 * time it's called.
	 */
	DefaultedList<ItemStack> getItems();

	/**
	 * Gets the item in the slot.
	 */
	@Override
	default ItemStack getStack(final int slot) {
		return this.getItems().get(slot);
	}

	/**
	 * @return true if this inventory has only empty stacks, false otherwise
	 */
	@Override
	default boolean isEmpty() {
		for (int i = 0; i < this.size(); i++) {
			final ItemStack stack = this.getStack(i);
			if (!stack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	default void markDirty() {
		// Override if you want behavior.
	}

	/**
	 * Removes the current stack in the {@code slot} and returns it.
	 */
	@Override
	default ItemStack removeStack(final int slot) {
		return Inventories.removeStack(this.getItems(), slot);
	}

	/**
	 * Takes a stack of the size from the slot.
	 * <p>
	 * (default implementation) If there are less items in the slot than what are
	 * requested, takes all items in that slot.
	 */
	@Override
	default ItemStack removeStack(final int slot, final int count) {
		final ItemStack result = Inventories.splitStack(this.getItems(), slot, count);
		if (!result.isEmpty()) {
			this.markDirty();
		}
		return result;
	}

	/**
	 * Replaces the current stack in the {@code slot} with the provided stack.
	 * <p>
	 * If the stack is too big for this inventory
	 * ({@link Inventory#getInvMaxStackAmount()}), it gets resized to this
	 * inventory's maximum amount.
	 */
	@Override
	default void setStack(final int slot, final ItemStack stack) {
		this.getItems().set(slot, stack);
		if (stack.getCount() > this.getMaxCountPerStack()) {
			stack.setCount(this.getMaxCountPerStack());
		}
	}

	// Inventory
	/**
	 * Returns the inventory size.
	 */
	@Override
	default int size() {
		return this.getItems().size();
	}
}
