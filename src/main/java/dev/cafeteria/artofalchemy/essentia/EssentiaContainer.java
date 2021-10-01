package dev.cafeteria.artofalchemy.essentia;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;

public class EssentiaContainer {

	public static EssentiaContainer of(final ItemStack item) {
		EssentiaContainer container;
		if (item.hasNbt() && item.getNbt().contains("contents")) {
			container = new EssentiaContainer(item.getNbt().getCompound("contents"));
		} else {
			container = null;
		}
		return container;
	}

	private final EssentiaStack contents = new EssentiaStack();
	private final Set<Essentia> whitelist = new HashSet<>();
	private Integer capacity = 0;
	private boolean input = true;
	private boolean output = true;
	private boolean infinite = false;

	private boolean whitelistEnabled = false;

	public EssentiaContainer() {
	}

	public EssentiaContainer(final NbtCompound tag) {
		if (tag != null) {
			if (tag.contains("essentia")) {
				final NbtCompound essentiaTag = tag.getCompound("essentia");
				this.setContents(new EssentiaStack(essentiaTag));
			}
			if (tag.contains("whitelist")) {
				this.whitelistEnabled = true;
				final NbtList list = tag.getList("whitelist", 8);
				for (int i = 0; i < list.size(); i++) {
					final Identifier id = new Identifier(list.getString(i));
					this.whitelist(RegistryEssentia.INSTANCE.get(id));
				}
			}
			if (tag.contains("capacity")) {
				if (tag.getString("capacity").equals("unlimited")) {
					this.setUnlimitedCapacity();
				} else {
					this.setCapacity(tag.getInt("capacity"));
				}

			}
			if (tag.contains("infinite")) {
				this.setInfinite(tag.getBoolean("infinite"));
			}
			if (tag.contains("whitelist_enabled")) {
				this.setWhitelistEnabled(tag.getBoolean("whitelist_enabled"));
			}
			if (tag.contains("input")) {
				this.setInput(tag.getBoolean("input"));
			}
			if (tag.contains("output")) {
				this.setOutput(tag.getBoolean("output"));
			}
		}
	}

	public boolean addEssentia(final EssentiaStack stack) {
		if (this.canAcceptIgnoreIO(stack)) {
			this.contents.add(stack);
			return true;
		} else {
			return false;
		}
	}

	public EssentiaContainer blacklist(final Essentia essentia) {
		this.whitelist.remove(essentia);
		return this;
	}

	public boolean canAccept(final EssentiaStack query) {
		if (!this.input) {
			return false;
		} else {
			return this.canAcceptIgnoreIO(query);
		}
	}

	public boolean canAcceptIgnoreIO(final EssentiaStack query) {
		if (this.whitelistEnabled) {
			for (final Essentia key : query.keySet()) {
				if (query.getOrDefault(key, 0) != 0 && !this.whitelisted(key)) {
					return false;
				}
			}
		}
		if (this.capacity == null) {
			return true;
		} else {
			return this.getCount() + query.getCount() <= this.capacity;
		}
	}

	public boolean canProvide(final EssentiaStack query) {
		if (!this.output) {
			return false;
		} else {
			return this.canProvideIgnoreIO(query);
		}
	}

	public boolean canProvideIgnoreIO(final EssentiaStack query) {
		if (this.whitelistEnabled) {
			for (final Essentia key : query.keySet()) {
				if (query.getOrDefault(key, 0) != 0 && !this.whitelisted(key)) {
					return false;
				}
			}
		}
		if (this.infinite) {
			return true;
		} else {
			return this.contains(query);
		}
	}

	public boolean contains(final EssentiaStack query) {
		return this.contents.contains(query);
	}

	public boolean emptyContents() {
		if (!this.isEmpty()) {
			this.contents.clear();
			return true;
		} else {
			return false;
		}
	}

	// Clears any essentia in violation of the whitelist; returns true if any
	// essentia was deleted
	public boolean enforceWhitelist() {
		if (this.whitelistEnabled) {
			boolean removed = false;
			for (final Essentia key : this.contents.keySet()) {
				if (!this.whitelist.contains(key)) {
					this.contents.remove(key);
					removed = true;
				}
			}
			return removed;
		} else {
			return false;
		}
	}

	public Integer getCapacity() {
		return this.capacity;
	}

	public int getColor() {
		return this.getContents().getColor();
	}

	public EssentiaStack getContents() {
		return this.contents;
	}

	public int getCount() {
		return this.contents.getCount();
	}

	public int getCount(final Essentia essentia) {
		if (essentia != null) {
			return this.contents.getOrDefault(essentia, 0);
		} else {
			return 0;
		}
	}

	public Integer getFreeCapacity() {
		if (this.hasUnlimitedCapacity()) {
			return null;
		} else {
			return this.getCapacity() - this.getCount();
		}
	}

	public Set<Essentia> getWhitelist() {
		return this.whitelist;
	}

	public boolean hasUnlimitedCapacity() {
		return this.capacity == null;
	}

	public ItemStack in(final ItemStack item) {
		NbtCompound tag;
		if (item.hasNbt()) {
			tag = item.getNbt();
		} else {
			tag = new NbtCompound();
		}
		tag.put("contents", this.writeNbt());
		item.setNbt(tag);
		return item;
	}

	public boolean isEmpty() {
		for (final int amount : this.contents.values()) {
			if (amount != 0) {
				return false;
			}
		}
		return true;
	}

	public boolean isFull() {
		if (this.capacity != null) {
			return this.getCount() >= this.capacity;
		} else {
			return false;
		}
	}

	public boolean isInfinite() {
		return this.infinite;
	}

	public boolean isInput() {
		return this.input;
	}

	public boolean isOutput() {
		return this.output;
	}

	public boolean isWhitelistEnabled() {
		return this.whitelistEnabled;
	}

	public void mixPushContents(final EssentiaContainer other) {
		if (!this.output || !other.input) {
			return;
		}
		final EssentiaContainer mixed = new EssentiaContainer().setUnlimitedCapacity();
		this.pushContents(mixed, true);
		other.pushContents(mixed, true);
		mixed.pushContents(other, true);
		mixed.pushContents(this, true);
	}

	public EssentiaStack pullContents(final EssentiaContainer other) {
		return this.pullContents(other, false);
	}

	// Pull as much as possible of another container's contents, returning the
	// essentia transferred
	public EssentiaStack pullContents(final EssentiaContainer other, final boolean force) {
		return other.pushContents(this, force);
	}

	// Pull the entire contents of another container, failing if any essentia
	// couldn't transfer
	public boolean pullEntireContents(final EssentiaContainer other) {
		return this.pullEntireStack(other, other.contents);
	}

	// Pull an entire stack from another container, failing if any essentia couldn't
	// transfer
	public boolean pullEntireStack(final EssentiaContainer other, final EssentiaStack stack) {
		return other.pushEntireStack(this, stack);
	}

	public EssentiaStack pullStack(final EssentiaContainer other, final EssentiaStack stack) {
		return this.pullStack(other, stack, false);
	}

	// Pull as much as possible of a stack from another container, returning the
	// essentia transferred
	public EssentiaStack pullStack(final EssentiaContainer other, final EssentiaStack stack, final boolean force) {
		return other.pushStack(this, stack, force);
	}

	public EssentiaStack pushContents(final EssentiaContainer other) {
		return this.pushContents(other, false);
	}

	// Push as much as possible of this container's contents to another, returning
	// the essentia transferred
	public EssentiaStack pushContents(final EssentiaContainer other, final boolean force) {
		if (other.hasUnlimitedCapacity() || other.getFreeCapacity() >= this.getCount()) {
			return this.pushStack(other, this.contents, force);
		} else {
			return this.pushStack(
				other,
				EssentiaStack.multiplyCeil(this.contents, (float) other.getFreeCapacity() / this.getCount()),
				force
			);
		}
	}

	// Push the entire contents of this container, failing if any essentia couldn't
	// transfer
	public boolean pushEntireContents(final EssentiaContainer other) {
		return this.pushEntireStack(other, this.contents);
	}

	// Push an entire stack to another container, failing if any essentia couldn't
	// transfer
	public boolean pushEntireStack(final EssentiaContainer other, final EssentiaStack stack) {
		if (this.canProvide(stack) && other.canAccept(stack)) {
			if (!this.infinite) {
				this.contents.subtract(stack);
			}
			if (!other.infinite) {
				other.contents.add(stack);
			}
			return true;
		} else {
			return false;
		}
	}

	public EssentiaStack pushStack(final EssentiaContainer other, final EssentiaStack stack) {
		return this.pushStack(other, stack, false);
	}

	// Push as much as possible of a stack to another container, returning the
	// essentia transferred
	public EssentiaStack pushStack(final EssentiaContainer other, final EssentiaStack stack, final boolean force) {
		if (!force && (!this.output || !other.input)) {
			return new EssentiaStack();
		} else {
			final EssentiaStack transferred = new EssentiaStack();
			for (final Entry<Essentia, Integer> entry : stack.entrySet()) {
				final Essentia key = entry.getKey();
				final int value = entry.getValue();
				if (this.whitelisted(key) && other.whitelisted(key)) {
					int transferAmt = value;
					if (other.capacity != null) {
						transferAmt = Math.min(transferAmt, other.getCapacity() - other.getCount());
					}
					if (!this.infinite) {
						transferAmt = Math.min(transferAmt, this.getCount(key));
						this.contents.subtract(key, transferAmt);
					}
					if (!other.infinite) {
						other.contents.add(key, transferAmt);
					}
					transferred.put(key, transferAmt);
				}
			}
			return transferred;
		}
	}

	public EssentiaContainer setCapacity(final int capacity) {
		this.capacity = capacity;
		return this;
	}

	public EssentiaContainer setContents(final EssentiaStack essentia) {
		this.contents.clear();
		if (essentia != null) {
			this.contents.putAll(essentia);
		}
		return this;
	}

	public EssentiaContainer setInfinite(final boolean infinite) {
		this.infinite = infinite;
		return this;
	}

	public EssentiaContainer setInput(final boolean input) {
		this.input = input;
		return this;
	}

	public EssentiaContainer setOutput(final boolean output) {
		this.output = output;
		return this;
	}

	public EssentiaContainer setUnlimitedCapacity() {
		this.capacity = null;
		return this;
	}

	public EssentiaContainer setWhitelist(final Set<Essentia> whitelist) {
		this.whitelist.clear();
		if (whitelist != null) {
			this.whitelist.addAll(whitelist);
		}
		return this;
	}

	public EssentiaContainer setWhitelistEnabled(final boolean whitelistEnabled) {
		this.whitelistEnabled = whitelistEnabled;
		return this;
	}

	public boolean subtractEssentia(final EssentiaStack stack) {
		if (this.canProvideIgnoreIO(stack)) {
			this.contents.subtract(stack);
			return true;
		} else {
			return false;
		}
	}

	public EssentiaContainer whitelist(final Essentia essentia) {
		this.whitelist.add(essentia);
		return this;
	}

	public boolean whitelisted(final Essentia essentia) {
		return !this.whitelistEnabled || this.whitelist.contains(essentia);
	}

	public NbtCompound writeNbt() {
		final NbtCompound tag = new NbtCompound();
		tag.put("essentia", this.getContents().toTag());
		final NbtList list = new NbtList();
		for (final Essentia essentia : this.getWhitelist()) {
			list.add(NbtString.of(RegistryEssentia.INSTANCE.getId(essentia).toString()));
		}
		tag.put("whitelist", list);
		tag.putBoolean("whitelist_enabled", this.isWhitelistEnabled());
		tag.putBoolean("infinite", this.isInfinite());
		if (this.capacity == null) {
			tag.putString("capacity", "unlimited");
		} else {
			tag.putInt("capacity", this.getCapacity());
		}
		tag.putBoolean("input", this.isInput());
		tag.putBoolean("output", this.isOutput());
		return tag;
	}

}
