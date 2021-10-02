package dev.cafeteria.artofalchemy.blockentity;

import dev.cafeteria.artofalchemy.AoAConfig;
import dev.cafeteria.artofalchemy.ArtOfAlchemy;
import dev.cafeteria.artofalchemy.block.BlockDissolver;
import dev.cafeteria.artofalchemy.essentia.EssentiaContainer;
import dev.cafeteria.artofalchemy.essentia.EssentiaStack;
import dev.cafeteria.artofalchemy.gui.handler.HandlerDissolver;
import dev.cafeteria.artofalchemy.network.AoANetworking;
import dev.cafeteria.artofalchemy.recipe.AoARecipes;
import dev.cafeteria.artofalchemy.recipe.RecipeDissolution;
import dev.cafeteria.artofalchemy.transport.HasAlkahest;
import dev.cafeteria.artofalchemy.transport.HasEssentia;
import dev.cafeteria.artofalchemy.util.ImplementedInventory;
import io.github.cottonmc.cotton.gui.PropertyDelegateHolder;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

@SuppressWarnings("deprecation") // Experimental API
public class BlockEntityDissolver extends BlockEntity
	implements ImplementedInventory, BlockEntityTicker<BlockEntityDissolver>, PropertyDelegateHolder,
	BlockEntityClientSerializable, HasEssentia, HasAlkahest, SidedInventory, ExtendedScreenHandlerFactory {

	private static final int[] TOP_SLOTS = {
		0
	};
	private static final int[] BOTTOM_SLOTS = {
		0
	};
	private static final int[] SIDE_SLOTS = {
		0
	};
	private int tankSize;
	private float speedMod;
	private float yield;
	private final SingleVariantStorage<FluidVariant> alkahestTank = this.makeAlkahestTank();
	private int progress = 0;
	private int maxProgress = 100;
	private int status = 0;
	// Status 0: Can craft
	// Status 1: Generic error (no message)
	// Status 2: Insufficient alkahest
	// Status 3: Full output buffer
	private boolean lit = false;

	protected final DefaultedList<ItemStack> items = DefaultedList.ofSize(1, ItemStack.EMPTY);

	protected EssentiaContainer essentia;

	protected final PropertyDelegate delegate = new PropertyDelegate() {

		@Override
		public int get(final int index) {
			switch (index) {
				case 0:
					return (int) ((BlockEntityDissolver.this.getAlkahest() / FluidConstants.BUCKET) * 1000);
				case 1:
					return (int) ((BlockEntityDissolver.this.getAlkahestCapacity() / FluidConstants.BUCKET) * 1000);
				case 2:
					return BlockEntityDissolver.this.progress;
				case 3:
					return BlockEntityDissolver.this.maxProgress;
				case 4:
					return BlockEntityDissolver.this.status;
				default:
					return 0;
			}
		}

		@Override
		public void set(final int index, final int value) {
			switch (index) {
				case 0:
					BlockEntityDissolver.this.setAlkahest(value);
					break;
				case 1:
					// No action
					break;
				case 2:
					BlockEntityDissolver.this.progress = value;
					break;
				case 3:
					BlockEntityDissolver.this.maxProgress = value;
					break;
				case 4:
					BlockEntityDissolver.this.status = value;
					break;
			}
		}

		@Override
		public int size() {
			return 5;
		}

	};

	protected BlockEntityDissolver(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
		super(type, pos, state);
	}

	public BlockEntityDissolver(final BlockPos pos, final BlockState state) {
		this(AoABlockEntities.DISSOLVER, pos, state);
		final AoAConfig.DissolverSettings settings = AoAConfig.get().dissolverSettings;
		this.tankSize = settings.tankBasic;
		this.speedMod = settings.speedBasic;
		this.yield = settings.yieldBasic;
		this.essentia = new EssentiaContainer().setCapacity(this.tankSize).setInput(false).setOutput(true);
	}

	private boolean canCraft(final RecipeDissolution recipe) {
		final ItemStack inSlot = this.items.get(0);

		if ((recipe == null) || inSlot.isEmpty()) {
			return this.updateStatus(1);
		} else {
			final ItemStack container = recipe.getContainer();
			final EssentiaStack results = recipe.getEssentia();

			this.maxProgress = (int) Math.sqrt(results.getCount() / this.getSpeedMod());
			if (this.maxProgress < (2 / this.getSpeedMod())) {
				this.maxProgress = (int) (2 / this.getSpeedMod());
			}

			if ((container != ItemStack.EMPTY) && (inSlot.getCount() != container.getCount())) {
				return this.updateStatus(1);
			}

			float factor = this.getEfficiency() * recipe.getFactor();
			if (inSlot.isDamageable()) {
				factor *= 1.0 - ((float) inSlot.getDamage() / inSlot.getMaxDamage());
			}
			results.multiply(factor);

			if (results.getCount() > this.getAlkahest()) {
				return this.updateStatus(2);
			} else if (!this.essentia.canAcceptIgnoreIO(results)) {
				return this.updateStatus(3);
			} else {
				return this.updateStatus(0);
			}
		}
	}

	@Override
	public boolean canExtract(final int slot, final ItemStack stack, final Direction dir) {
		if (dir == Direction.DOWN) {
			return TagFactory.ITEM.create(ArtOfAlchemy.id("containers")).contains(stack.getItem());
		} else {
			return true;
		}
	}

	@Override
	public boolean canInsert(final int slot, final ItemStack stack, final Direction dir) {
		return this.isValid(slot, stack);
	}

	@Override
	public ScreenHandler createMenu(final int syncId, final PlayerInventory inv, final PlayerEntity player) {
		return new HandlerDissolver(syncId, inv, ScreenHandlerContext.create(this.world, this.pos));
	}

	// Be sure to check canCraft() first!
	private void doCraft(final RecipeDissolution recipe) {
		final ItemStack inSlot = this.items.get(0);
		final EssentiaStack results = recipe.getEssentia();
		final ItemStack container = recipe.getContainer();

		float factor = this.getEfficiency() * recipe.getFactor();
		if (inSlot.isDamageable()) {
			factor *= 1.0 - ((float) inSlot.getDamage() / inSlot.getMaxDamage());
		}
		results.multiplyStochastic(factor);

		if (container != ItemStack.EMPTY) {
			this.items.set(0, container.copy());
		} else {
			inSlot.decrement(1);
		}

		this.essentia.addEssentia(results);
		this.addAlkahest(-results.getCount());

	}

	@Override
	public void fromClientTag(final NbtCompound tag) {
		this.readNbt(tag);
	}

	@Override
	public long getAlkahestCapacity() {
		return (this.tankSize / 1000) * FluidConstants.BUCKET;
	}

	@Override
	public SingleVariantStorage<FluidVariant> getAlkahestTank() {
		return this.alkahestTank;
	}

	@Override
	public int[] getAvailableSlots(final Direction side) {
		if (side == Direction.UP) {
			return BlockEntityDissolver.TOP_SLOTS;
		} else if (side == Direction.DOWN) {
			return BlockEntityDissolver.BOTTOM_SLOTS;
		} else {
			return BlockEntityDissolver.SIDE_SLOTS;
		}
	}

	@Override
	public EssentiaContainer getContainer(final Direction dir) {
		return this.getContainer(0);
	}

	@Override
	public EssentiaContainer getContainer(final int id) {
		if (id == 0) {
			return this.essentia;
		} else {
			return null;
		}
	}

	@Override
	public Text getDisplayName() {
		return new LiteralText("");
	}

	public float getEfficiency() {
		return this.yield;
	}

	@Override
	public DefaultedList<ItemStack> getItems() {
		return this.items;
	}

	@Override
	public int getNumContainers() {
		return 1;
	}

	@Override
	public PropertyDelegate getPropertyDelegate() {
		return this.delegate;
	}

	public float getSpeedMod() {
		return this.speedMod;
	}

	@Override
	public boolean isValid(final int slot, final ItemStack stack) {
		return true;
	}

	@Override
	public void markDirty() {
		super.markDirty();
		if (!this.world.isClient()) {
			this.sync();
		}
	}

	@Override
	public void readNbt(final NbtCompound tag) {
		super.readNbt(tag);
		Inventories.readNbt(tag, this.items);
		this.setAlkahest(tag.getInt("alkahest"));
		this.progress = tag.getInt("progress");
		this.maxProgress = tag.getInt("max_progress");
		this.status = tag.getInt("status");
		this.essentia = new EssentiaContainer(tag.getCompound("essentia"));
	}

	@Override
	public void sync() {
		AoANetworking.sendEssentiaPacket(this.world, this.pos, 0, this.essentia);
		BlockEntityClientSerializable.super.sync();
	}

	@Override
	public void tick(
		final World world, final BlockPos pos, final BlockState state, final BlockEntityDissolver blockEntity
	) {

		if (!world.isClient()) {
			final ItemStack inSlot = this.items.get(0);
			boolean canWork = false;

			if (inSlot.isEmpty()) {
				this.updateStatus(1);
			} else if (!this.hasAlkahest()) {
				this.updateStatus(2);
			} else {
				final RecipeDissolution recipe = world.getRecipeManager().getFirstMatch(AoARecipes.DISSOLUTION, this, world)
					.orElse(null);
				canWork = this.canCraft(recipe);

				if (canWork) {
					if (this.progress < this.maxProgress) {
						if (!this.lit) {
							world.setBlockState(pos, world.getBlockState(pos).with(BlockDissolver.LIT, true));
							this.lit = true;
						}
						this.progress++;
					}
					if (this.progress >= this.maxProgress) {
						this.progress -= this.maxProgress;
						this.doCraft(recipe);
						if (!this.hasAlkahest()) {
							world.setBlockState(pos, world.getBlockState(pos).with(BlockDissolver.FILLED, false));
						}
					}
				}
			}

			if (!canWork) {
				if (this.progress != 0) {
					this.progress = 0;
				}
				if (this.lit) {
					this.lit = false;
					world.setBlockState(pos, world.getBlockState(pos).with(BlockDissolver.LIT, false));
				}
			}
			sync(); // TODO: Check if this is laggy & improve
		}
	}

	@Override
	public NbtCompound toClientTag(final NbtCompound tag) {
		return this.writeNbt(tag);
	}

	private boolean updateStatus(final int status) {
		if (this.status != status) {
			this.status = status;
			this.markDirty();
		}
		return status == 0;
	}

	@Override
	public NbtCompound writeNbt(final NbtCompound tag) {
		tag.putLong("alkahest", this.getAlkahest());
		tag.putInt("progress", this.progress);
		tag.putInt("max_progress", this.maxProgress);
		tag.putInt("status", this.status);
		tag.put("essentia", this.essentia.writeNbt());
		Inventories.writeNbt(tag, this.items);
		return super.writeNbt(tag);
	}

	@Override
	public void writeScreenOpeningData(final ServerPlayerEntity player, final PacketByteBuf buf) {
		buf.writeBlockPos(this.pos);
	}
}
