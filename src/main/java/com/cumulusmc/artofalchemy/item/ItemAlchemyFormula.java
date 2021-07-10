package com.cumulusmc.artofalchemy.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.List;

public class ItemAlchemyFormula extends AbstractItemFormula {

	public ItemAlchemyFormula(Settings settings) {
		super(settings);
	}

	public static Item getFormula(ItemStack stack) {
		NbtCompound tag = stack.hasNbt() ? stack.getNbt() : new NbtCompound();
		if (tag.contains("formula")) {
			Identifier id = new Identifier(tag.getString("formula"));
			return Registry.ITEM.get(id);
		} else {
			return Items.AIR;
		}
	}

	public static void setFormula(ItemStack stack, Item formula) {
		NbtCompound tag = stack.getOrCreateNbt();
		tag.put("formula", NbtString.of(Registry.ITEM.getId(formula).toString()));
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext ctx) {
		tooltip.add(new TranslatableText(getFormula(stack).getTranslationKey()).formatted(Formatting.GRAY));
		super.appendTooltip(stack, world, tooltip, ctx);
	}

}
