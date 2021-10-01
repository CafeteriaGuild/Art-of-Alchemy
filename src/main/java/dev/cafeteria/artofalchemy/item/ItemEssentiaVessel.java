package dev.cafeteria.artofalchemy.item;

import java.util.HashSet;
import java.util.List;

import dev.cafeteria.artofalchemy.AoAConfig;
import dev.cafeteria.artofalchemy.essentia.Essentia;
import dev.cafeteria.artofalchemy.essentia.EssentiaContainer;
import dev.cafeteria.artofalchemy.transport.HasEssentia;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class ItemEssentiaVessel extends Item {

	public static int getColor(final ItemStack stack) {
		final NbtCompound tag = stack.getOrCreateNbt();
		if (tag.contains("color", NbtType.INT)) {
			return tag.getInt("color");
		} else {
			return 0xAA0077;
		}
	}

	public static EssentiaContainer getContainer(final ItemStack stack) {
		EssentiaContainer container = EssentiaContainer.of(stack);
		Essentia type = null;
		int capacity = 0;
		if (stack.getItem() instanceof ItemEssentiaVessel) {
			type = ((ItemEssentiaVessel) stack.getItem()).type;
			capacity = ((ItemEssentiaVessel) stack.getItem()).capacity;
		}
		if (container == null) {
			container = new EssentiaContainer().setCapacity(capacity);
			if (type != null) {
				container.whitelist(type).setWhitelistEnabled(true);
			}
		}
		return container;
	}

	public static void setColor(final ItemStack stack) {
		final NbtCompound tag = stack.getOrCreateNbt();
		tag.putInt("color", ItemEssentiaVessel.getContainer(stack).getColor());
	}

	public static void setColor(final ItemStack stack, final int color) {
		final NbtCompound tag = stack.getOrCreateNbt();
		tag.putInt("color", color);
	}

	public static int useStackOnBE(final ItemStack stack, final BlockEntity be) {
		final EssentiaContainer container = ItemEssentiaVessel.getContainer(stack);
		int transferred = 0;

		if (be instanceof final HasEssentia target) {
			for (int i = 0; (i < target.getNumContainers()) && (transferred == 0); i++) {
				final EssentiaContainer other = target.getContainer(i);
				final int pushed = container.pushContents(other).getCount();
				transferred -= pushed;
			}
			for (int i = 0; (i < target.getNumContainers()) && (transferred == 0); i++) {
				final EssentiaContainer other = target.getContainer(i);
				final int pulled = container.pullContents(other, container.isInput()).getCount();
				transferred += pulled;
			}
			final BlockPos pos = be.getPos();
			if (transferred > 0) {
				be.getWorld().playSound(null, pos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
			} else if (transferred < 0) {
				be.getWorld().playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
			}
		}

		container.in(stack);
		return transferred;
	}

	public final int capacity;

	public final Essentia type;

	private String translationKey;

	public ItemEssentiaVessel(final Settings settings) {
		this(settings, null);
	}

	public ItemEssentiaVessel(final Settings settings, final Essentia type) {
		super(settings.maxCount(1));
		this.capacity = AoAConfig.get().vesselCapacity;
		this.type = type;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void appendTooltip(
		final ItemStack stack, final World world, final List<Text> tooltip, final TooltipContext ctx
	) {

		if (world == null) {
			return;
		}

		final EssentiaContainer container = ItemEssentiaVessel.getContainer(stack);
		final String prefix = this.tooltipPrefix();

		if (((ItemEssentiaVessel) stack.getItem()).type != null) {
			tooltip.add(new TranslatableText(prefix + "deprecated").formatted(Formatting.DARK_RED));
		}

		if (container.isInfinite()) {
			tooltip.add(new TranslatableText(prefix + "infinite").formatted(Formatting.LIGHT_PURPLE));
			if (container.isWhitelistEnabled()) {
				if (container.getWhitelist().isEmpty()) {
					tooltip.add(new TranslatableText(prefix + "empty").formatted(Formatting.GRAY));
				} else {
					for (final Essentia essentia : container.getWhitelist()) {
						tooltip.add(new TranslatableText(prefix + "component_inf", essentia.getName()).formatted(Formatting.GOLD));
					}
				}
			}

		} else if (container.hasUnlimitedCapacity()) {
			if (container.isWhitelistEnabled() && (container.getWhitelist().size() == 1)) {
				for (final Essentia essentia : container.getWhitelist()) {
					tooltip.add(
						new TranslatableText(prefix + "single_unlim", essentia.getName(), container.getCount(essentia))
							.formatted(Formatting.GREEN)
					);
				}
			} else if (container.isWhitelistEnabled() && container.getWhitelist().isEmpty()) {
				tooltip.add(new TranslatableText(prefix + "empty").formatted(Formatting.GRAY));
			} else {
				tooltip.add(new TranslatableText(prefix + "mixed_unlim", container.getCount()).formatted(Formatting.AQUA));
				for (final Essentia essentia : container.getContents().sortedList()) {
					if ((container.getCount(essentia) != 0) && container.whitelisted(essentia)) {
						tooltip.add(
							new TranslatableText(prefix + "component", essentia.getName(), container.getCount(essentia))
								.formatted(Formatting.GOLD)
						);
					}
				}
			}

		} else if (container.isWhitelistEnabled() && (container.getWhitelist().size() == 1)) {
			for (final Essentia essentia : container.getWhitelist()) {
				tooltip.add(
					new TranslatableText(
						prefix + "single", essentia.getName(), container.getCount(essentia), container.getCapacity()
					).formatted(Formatting.GREEN)
				);
			}
		} else if (container.isWhitelistEnabled() && container.getWhitelist().isEmpty()) {
			tooltip.add(new TranslatableText(prefix + "empty").formatted(Formatting.GRAY));
		} else {
			tooltip.add(
				new TranslatableText(prefix + "mixed", container.getCount(), container.getCapacity()).formatted(Formatting.AQUA)
			);
			for (final Essentia essentia : container.getContents().sortedList()) {
				if ((container.getCount(essentia) != 0) && container.whitelisted(essentia)) {
					tooltip.add(
						new TranslatableText(prefix + "component", essentia.getName(), container.getCount(essentia))
							.formatted(Formatting.GOLD)
					);
				}
			}
		}

		if (!container.isInput() && !container.isOutput()) {
			tooltip.add(new TranslatableText(prefix + "locked").formatted(Formatting.RED));
		} else if (!container.isInput()) {
			tooltip.add(new TranslatableText(prefix + "output").formatted(Formatting.RED));
		} else if (!container.isOutput()) {
			tooltip.add(new TranslatableText(prefix + "input").formatted(Formatting.RED));
		}

		super.appendTooltip(stack, world, tooltip, ctx);
	}

	@Override
	protected String getOrCreateTranslationKey() {
		if (this.translationKey == null) {
			this.translationKey = Util.createTranslationKey("item", Registry.ITEM.getId(AoAItems.ESSENTIA_VESSEL));
		}
		return this.translationKey;
	}

	@Override
	public String getTranslationKey() {
		return this.getOrCreateTranslationKey();
	}

	@Override
	public void onCraft(final ItemStack stack, final World world, final PlayerEntity player) {
		final EssentiaContainer container = ItemEssentiaVessel.getContainer(stack);
		this.setContainer(stack, container);
		super.onCraft(stack, world, player);
	}

	public void setContainer(final ItemStack stack, final EssentiaContainer container) {
		if (this.type != null) {
			container.setWhitelist(new HashSet<>()).whitelist(this.type).setWhitelistEnabled(true);
		}
		container.in(stack);
	}

	private String tooltipPrefix() {
		return this.getTranslationKey() + ".tooltip_";
	}

	@Override
	public TypedActionResult<ItemStack> use(final World world, final PlayerEntity user, final Hand hand) {
		if (user.isSneaking()) {
			final ItemStack stack = user.getStackInHand(hand);
			final EssentiaContainer container = ItemEssentiaVessel.getContainer(stack);
			float pitch;
			if (container.isInput() && container.isOutput()) {
				user.sendMessage(new TranslatableText(this.tooltipPrefix() + "input"), true);
				container.setInput(true);
				container.setOutput(false);
				pitch = 0.80f;
			} else if (container.isInput() && !container.isOutput()) {
				user.sendMessage(new TranslatableText(this.tooltipPrefix() + "output"), true);
				container.setInput(false);
				container.setOutput(true);
				pitch = 0.95f;
			} else if (!container.isInput() && container.isOutput()) {
				user.sendMessage(new TranslatableText(this.tooltipPrefix() + "locked"), true);
				container.setInput(false);
				container.setOutput(false);
				pitch = 1.05f;
			} else {
				user.sendMessage(new TranslatableText(this.tooltipPrefix() + "unlocked"), true);
				container.setInput(true);
				container.setOutput(true);
				pitch = 0.65f;
			}
			container.in(stack);
			if (world.isClient) {
				user.playSound(SoundEvents.UI_BUTTON_CLICK, SoundCategory.PLAYERS, 0.5f, pitch);
			}
			return TypedActionResult.consume(stack);
		}
		return super.use(world, user, hand);
	}

	@Override
	public ActionResult useOnBlock(final ItemUsageContext ctx) {
		final PlayerEntity player = ctx.getPlayer();
		final BlockEntity be = ctx.getWorld().getBlockEntity(ctx.getBlockPos());
		final int transferred = ItemEssentiaVessel.useStackOnBE(ctx.getStack(), be);

		if (player != null) {
			if (transferred > 0) {
				player.sendMessage(new TranslatableText(this.tooltipPrefix() + "pulled", +transferred), true);
			} else if (transferred < 0) {
				player.sendMessage(new TranslatableText(this.tooltipPrefix() + "pushed", -transferred), true);
			}
		}

		if (transferred != 0) {
			be.markDirty();
			ItemEssentiaVessel.setColor(ctx.getStack());
			return ActionResult.SUCCESS;
		} else {
			return ActionResult.PASS;
		}
	}

}
