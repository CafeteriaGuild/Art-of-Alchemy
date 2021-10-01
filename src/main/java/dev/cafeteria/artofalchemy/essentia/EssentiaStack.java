package dev.cafeteria.artofalchemy.essentia;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import dev.cafeteria.artofalchemy.util.AoAHelper;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

@SuppressWarnings("serial")
public class EssentiaStack extends HashMap<Essentia, Integer> {

	// Non-mutating addition.
	public static EssentiaStack add(final EssentiaStack stack1, final EssentiaStack stack2) {
		final EssentiaStack outStack = new EssentiaStack();
		final Set<Essentia> union = new HashSet<>(stack1.keySet());
		union.addAll(stack2.keySet());
		union.forEach(new Consumer<Essentia>() {
			@Override
			public void accept(final Essentia essentia) {
				outStack.put(essentia, stack1.getOrDefault(essentia, 0) + stack2.getOrDefault(essentia, 0));
			}
		});
		return outStack;
	}

	// Non-mutating scalar multiplication. Can go negative - try not to break
	// things.
	public static EssentiaStack multiply(final EssentiaStack inStack, final double scalar) {
		final EssentiaStack outStack = new EssentiaStack();
		inStack.forEach(new BiConsumer<Essentia, Integer>() {
			@Override
			public void accept(final Essentia essentia, final Integer amount) {
				outStack.put(essentia, (int) (amount * scalar));
			}
		});
		return outStack;
	}

	// Non-mutating scalar multiplication. Can go negative - try not to break
	// things.
	public static EssentiaStack multiply(final EssentiaStack inStack, final int scalar) {
		final EssentiaStack outStack = new EssentiaStack();
		inStack.forEach(new BiConsumer<Essentia, Integer>() {
			@Override
			public void accept(final Essentia essentia, final Integer amount) {
				outStack.put(essentia, amount * scalar);
			}
		});
		return outStack;
	}

	// Non-mutating scalar multiplication. Can go negative - try not to break
	// things.
	public static EssentiaStack multiplyCeil(final EssentiaStack inStack, final double scalar) {
		final EssentiaStack outStack = new EssentiaStack();
		inStack.forEach(new BiConsumer<Essentia, Integer>() {
			@Override
			public void accept(final Essentia essentia, final Integer amount) {
				outStack.put(essentia, (int) Math.ceil(amount * scalar));
			}
		});
		return outStack;
	}

	// Non-mutating subtraction.
	public static EssentiaStack subtract(final EssentiaStack stack1, final EssentiaStack stack2) {
		final EssentiaStack outStack = new EssentiaStack();
		final Set<Essentia> union = stack1.keySet();
		union.addAll(stack2.keySet());
		union.forEach(new Consumer<Essentia>() {
			@Override
			public void accept(final Essentia essentia) {
				final int amount = Math.min(0, stack1.getOrDefault(essentia, 0) - stack2.getOrDefault(essentia, 0));
				outStack.put(essentia, amount);
			}
		});
		return outStack;
	}

	public EssentiaStack() {
	}

	public EssentiaStack(final JsonObject obj) {
		obj.entrySet().forEach(new Consumer<Entry<String, JsonElement>>() {
			@Override
			public void accept(final Entry<String, JsonElement> entry) {
				final Essentia essentia = RegistryEssentia.INSTANCE.get(new Identifier(entry.getKey()));
				if (essentia != null) {
					EssentiaStack.this.put(essentia, entry.getValue().getAsInt());
				} else {
					throw new JsonSyntaxException("Unknown essentia '" + entry.getKey() + "'");
				}
			}
		});
	}

	public EssentiaStack(final NbtCompound tag) {
		if (tag != null) {
			tag.getKeys().forEach(new Consumer<String>() {
				@Override
				public void accept(final String key) {
					final Essentia essentia = RegistryEssentia.INSTANCE.get(new Identifier(key));
					if (essentia != null) {
						EssentiaStack.this.put(essentia, tag.getInt(key));
					}
				}
			});
		}
	}

	// Mutating addition for a single essentia type.
	public void add(final Essentia essentia, final int amount) {
		this.put(essentia, this.getOrDefault(essentia, 0) + amount);
	}

	// Mutating addition.
	public void add(final EssentiaStack other) {
		other.forEach(this::add);
	}

	// Returns true if this stack contains at least as much essentia of all types as
	// the argument.
	public boolean contains(final EssentiaStack other) {
		final Set<Essentia> union = new HashSet<>(this.keySet());
		union.addAll(other.keySet());
		for (final Essentia essentia : union) {
			if (this.getOrDefault(essentia, 0) < other.getOrDefault(essentia, 0)) {
				return false;
			}
		}
		return true;
	}

	public int getColor() {
		Vec3d colorSum = new Vec3d(0, 0, 0);
		final double count = this.getCount();
		for (final Essentia essentia : this.keySet()) {
			Vec3d color = AoAHelper.decimalColor(essentia.getColor());
			color = color.multiply(this.get(essentia) / count);
			colorSum = colorSum.add(color);
		}
		return AoAHelper.combineColor(colorSum);
	}

	public int getCount() {
		int sum = 0;
		for (final int amount : this.values()) {
			sum += amount;
		}
		return sum;
	}

	public void multiply(final double scalar) {
		this.forEach(new BiConsumer<Essentia, Integer>() {
			@Override
			public void accept(final Essentia essentia, final Integer __) {
				EssentiaStack.this.multiply(essentia, scalar);
			}
		});
	}

	public void multiply(final Essentia essentia, final double scalar) {
		this.put(essentia, (int) (this.getOrDefault(essentia, 0) * scalar));
	}

	// Mutating scalar multiplication for a single essentia type. Can go negative -
	// try not to break things.
	public void multiply(final Essentia essentia, final int scalar) {
		this.put(essentia, this.getOrDefault(essentia, 0) * scalar);
	}

	// Mutating scalar multiplication. Can go negative - try not to break things.
	public void multiply(final int scalar) {
		this.forEach(new BiConsumer<Essentia, Integer>() {
			@Override
			public void accept(final Essentia essentia, final Integer __) {
				EssentiaStack.this.multiply(essentia, scalar);
			}
		});
	}

	public void multiplyStochastic(final double scalar) {
		this.replaceAll(new BiFunction<Essentia, Integer, Integer>() {
			@Override
			public Integer apply(final Essentia __, final Integer value) {
				return AoAHelper.stochasticRound(value * scalar);
			}
		});
	}

	public List<Essentia> sortedList() {
		final List<Essentia> list = new ArrayList<>();
		for (final Essentia key : this.keySet()) {
			if (this.get(key) > 0) {
				list.add(key);
			}
		}
		list.sort(new Comparator<Essentia>() {
			@Override
			public int compare(final Essentia item1, final Essentia item2) {
				return EssentiaStack.this.get(item2) - EssentiaStack.this.get(item1);
			}
		});
		return list;
	}

	// Mutating subtraction for a single essentia type.
	public void subtract(final Essentia essentia, final int amount) {
		this.put(essentia, Math.max(0, this.getOrDefault(essentia, 0) - amount));
	}

	// Mutating subtraction.
	public void subtract(final EssentiaStack other) {
		other.forEach(this::subtract);
	}

	public NbtCompound toTag() {
		final NbtCompound tag = new NbtCompound();
		for (final Essentia essentia : this.keySet()) {
			tag.putInt(RegistryEssentia.INSTANCE.getId(essentia).toString(), this.get(essentia));
		}
		return tag;
	}

}
