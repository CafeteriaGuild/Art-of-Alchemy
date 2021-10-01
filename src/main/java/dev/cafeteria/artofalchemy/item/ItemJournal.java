package dev.cafeteria.artofalchemy.item;

import java.util.ArrayList;
import java.util.List;

import dev.cafeteria.artofalchemy.gui.handler.HandlerJournal;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class ItemJournal extends AbstractItemFormula {

	public static boolean addFormula(final ItemStack stack, final Identifier formula) {
		final NbtList entries = ItemJournal.getOrCreateEntriesTag(stack);
		final NbtString newEntry = NbtString.of(formula.toString());
		if (!ItemJournal.hasFormula(stack, new Identifier(newEntry.asString()))) {
			entries.add(newEntry);
			return true;
		} else {
			return false;
		}
	}

	public static boolean addFormula(final ItemStack stack, final Item formula) {
		return ItemJournal.addFormula(stack, Registry.ITEM.getId(formula));
	}

	public static List<Item> getEntries(final ItemStack stack) {
		final List<Item> list = new ArrayList<>();
		final NbtList entries = ItemJournal.getOrCreateEntriesTag(stack);
		for (int i = 0; i < entries.size(); i++) {
			final Item item = Registry.ITEM.get(Identifier.tryParse(entries.getString(i)));
			if (item != Items.AIR) {
				list.add(item);
			}
		}
		return list;
	}

	public static NbtList getEntriesTag(final ItemStack stack) {
		final NbtCompound tag = stack.getOrCreateNbt();
		if (tag.contains("entries", 9)) {
			return tag.getList("entries", 8);
		} else {
			return null;
		}
	}

	public static Item getFormula(final ItemStack stack) {
		final NbtCompound tag = stack.getNbt();
		if (tag != null && tag.contains("selected")) {
			final Identifier id = new Identifier(tag.getString("selected"));
			return Registry.ITEM.get(id);
		} else {
			return Items.AIR;
		}
	}

	public static Identifier getId() {
		return Registry.ITEM.getId(AoAItems.JOURNAL);
	}

	public static NbtList getOrCreateEntriesTag(final ItemStack stack) {
		final NbtCompound tag = stack.getOrCreateNbt();
		if (tag.contains("entries", 9)) {
			return tag.getList("entries", 8);
		} else {
			final NbtList entries = new NbtList();
			tag.put("entries", entries);
			return entries;
		}
	}

	public static boolean hasFormula(final ItemStack stack, final Identifier formula) {
		if (formula.equals(Registry.ITEM.getId(Items.AIR))) {
			return true;
		} else {
			return ItemJournal.getOrCreateEntriesTag(stack).contains(NbtString.of(formula.toString()));
		}
	}

	public static boolean hasFormula(final ItemStack stack, final Item formula) {
		return ItemJournal.hasFormula(stack, Registry.ITEM.getId(formula));
	}

	public static boolean setFormula(final ItemStack stack, final Identifier formula) {
		if (ItemJournal.hasFormula(stack, formula) || formula == Registry.ITEM.getId(Items.AIR)) {
			final NbtCompound tag = stack.getOrCreateNbt();
			tag.put("selected", NbtString.of(formula.toString()));
			stack.setNbt(tag);
			return true;
		} else {
			return false;
		}
	}

	public static void setFormula(final ItemStack stack, final Item formula) {
		ItemJournal.setFormula(stack, Registry.ITEM.getId(formula));
	}

	public ItemJournal(final Settings settings) {
		super(settings.maxCount(1));
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void appendTooltip(
		final ItemStack stack, final World world, final List<Text> tooltip, final TooltipContext ctx
	) {
		final int entryCount = ItemJournal.getEntriesTag(stack) == null ? 0 : ItemJournal.getEntriesTag(stack).size();
		if (ItemJournal.getFormula(stack) != Items.AIR) {
			tooltip
				.add(new TranslatableText(ItemJournal.getFormula(stack).getTranslationKey()).formatted(Formatting.DARK_PURPLE));
		}
		tooltip.add(
			new TranslatableText("item.artofalchemy.alchemical_journal.tooltip_entries", entryCount)
				.formatted(Formatting.GRAY)
		);
		super.appendTooltip(stack, world, tooltip, ctx);
	}

	@Override
	public TypedActionResult<ItemStack> use(final World world, final PlayerEntity user, final Hand hand) {
		if (!world.isClient()) {
			user.openHandledScreen(new ExtendedScreenHandlerFactory() {
				@Override
				public ScreenHandler createMenu(final int syncId, final PlayerInventory inv, final PlayerEntity player) {
					return new HandlerJournal(syncId, inv, ScreenHandlerContext.EMPTY, hand);
				}

				@Override
				public Text getDisplayName() {
					return new LiteralText("");
				}

				@Override
				public void writeScreenOpeningData(final ServerPlayerEntity player, final PacketByteBuf buf) {
					buf.writeEnumConstant(hand);
				}
			});
		}
		return super.use(world, user, hand);
	}

}
