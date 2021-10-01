package dev.cafeteria.artofalchemy.util;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Chain {

	public static class ChainList<E> {
		private final List<E> contents;

		private ChainList(final List<E> list) {
			this.contents = list;
		}

		public ChainList<E> add(final E element) {
			this.contents.add(element);
			return ChainList.this;
		}

		public ChainList<E> add(final int i, final E element) {
			this.contents.add(i, element);
			return ChainList.this;
		}

		public ChainList<E> addAll(final int i, final List<E> list) {
			this.contents.addAll(i, list);
			return ChainList.this;
		}

		public ChainList<E> addAll(final List<E> list) {
			this.contents.addAll(list);
			return ChainList.this;
		}

		public List<E> finish() {
			return this.contents;
		}
	}

	public static class ChainMap<K, V> {
		private final Map<K, V> contents;

		private ChainMap(final Map<K, V> map) {
			this.contents = map;
		}

		public Map<K, V> finish() {
			return this.contents;
		}

		public ChainMap<K, V> put(final K key, final V value) {
			this.contents.put(key, value);
			return ChainMap.this;
		}

		public ChainMap<K, V> putAll(final Map<K, V> map) {
			this.contents.putAll(map);
			return ChainMap.this;
		}

		public ChainMap<K, V> putIfAbsent(final K key, final V value) {
			this.contents.putIfAbsent(key, value);
			return ChainMap.this;
		}
	}

	public static class ChainSet<E> {
		private final Set<E> contents;

		private ChainSet(final Set<E> set) {
			this.contents = set;
		}

		public ChainSet<E> add(final E element) {
			this.contents.add(element);
			return ChainSet.this;
		}

		public ChainSet<E> addAll(final Set<E> set) {
			this.contents.addAll(set);
			return ChainSet.this;
		}

		public Set<E> finish() {
			return this.contents;
		}
	}

	public static <E> ChainList<E> start(final List<E> list) {
		return new ChainList<>(list);
	}

	public static <K, V> ChainMap<K, V> start(final Map<K, V> map) {
		return new ChainMap<>(map);
	}

	public static <E> ChainSet<E> start(final Set<E> set) {
		return new ChainSet<>(set);
	}
}
