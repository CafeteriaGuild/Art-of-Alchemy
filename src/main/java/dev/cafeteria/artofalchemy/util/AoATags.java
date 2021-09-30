package dev.cafeteria.artofalchemy.util;

import dev.cafeteria.artofalchemy.ArtOfAlchemy;

import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;

public class AoATags {
	public static final Tag<Item> CONTAINERS = TagFactory.ITEM.create(ArtOfAlchemy.id("containers"));

	public static void init() { }
}
