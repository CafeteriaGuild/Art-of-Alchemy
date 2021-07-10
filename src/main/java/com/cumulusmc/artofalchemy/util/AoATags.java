package com.cumulusmc.artofalchemy.util;

import com.cumulusmc.artofalchemy.ArtOfAlchemy;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;

public class AoATags {
    public static final Tag<Item> CONTAINERS = TagRegistry.item(ArtOfAlchemy.id("containers"));

    public static void init() { }
}
