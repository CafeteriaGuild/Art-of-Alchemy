package dev.cafeteria.artofalchemy.blockentity;

import dev.cafeteria.artofalchemy.AoAConfig;
import dev.cafeteria.artofalchemy.block.BlockDissolver;
import dev.cafeteria.artofalchemy.gui.handler.HandlerProjector;
import dev.cafeteria.artofalchemy.recipe.AoARecipes;
import dev.cafeteria.artofalchemy.recipe.RecipeProjection;
import dev.cafeteria.artofalchemy.transport.HasAlkahest;
import dev.cafeteria.artofalchemy.util.AoAHelper;
import dev.cafeteria.artofalchemy.util.ImplementedInventory;
import io.github.cottonmc.cotton.gui.PropertyDelegateHolder;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
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
public class BlockEntityProjector extends BlockEntity
	implements ImplementedInventory, BlockEntityTicker<BlockEntityProjector>, PropertyDelegateHolder,
	BlockEntityClientSerializable, HasAlkahest, SidedInventory, ExtendedScreenHandlerFactory {

	private static final int[] TOP_SLOTS = {
		0
	};
	private static final int[] BOTTOM_SLOTS = {
		1
	};
	private static final int[] SIDE_SLOTS = {
		0, 1
	};
	private int tankSize;
	private int operationTime;
	private final SingleVariantStorage<FluidVariant> alkahestTank = this.makeAlkahestTank();
	private int progress = 0;
	private int maxProgress = this.getOperationTime();
	private boolean lit = false;

	protected final DefaultedList<ItemStack> items = DefaultedList.ofSize(2, ItemStack.EMPTY);

	protected final PropertyDelegate delegate = new PropertyDelegate() {

		@Override
		public int get(final int index) {
			switch (index) {
				case 0:
					return (int) ((BlockEntityProjector.this.getAlkahest() / FluidConstants.BUCKET) * 1000);
				case 1:
					return (int) ((BlockEntityProjector.this.getAlkahestCapacity() / FluidConstants.BUCKET) * 1000);
				case 2:
					return BlockEntityProjector.this.progress;
				case 3:
					return BlockEntityProjector.this.maxProgress;
				default:
					return 0;
			}
		}

		@Override
		public void set(final int index, final int value) {
		}

		@Override
		public int size() {
			return 4;
		}

	};

	protected BlockEntityProjector(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
		super(type, pos, state);
	}

	public BlockEntityProjector(final BlockPos pos, final BlockState state) {
		this(AoABlockEntities.PROJECTOR, pos, state);
		final AoAConfig.ProjectorSettings settings = AoAConfig.get().projectorSettings;
		this.operationTime = settings.opTime;
		this.tankSize = settings.tankSize;
		this.maxProgress = this.getOperationTime();
	}

	private boolean canCraft(final RecipeProjection recipe) {
		final ItemStack inSlot = this.items.get(0);
		final ItemStack outSlot = this.items.get(1);

		if ((recipe == null) || inSlot.isEmpty()) {
			return false;
		} else {
			final ItemStack outStack = recipe.getOutput();
			final int alkCost = recipe.getAlkahest();
			final int itemCost = recipe.getCost();

			if ((this.getAlkahest() < alkCost) || (inSlot.getCount() < itemCost)) {
				return false;
			} else if (outSlot.isEmpty()) {
				return true;
			} else if (outSlot.getItem() == outStack.getItem()) {
				return outSlot.getCount() <= (outSlot.getMaxCount() - outStack.getCount());
			} else {
				return false;
			}
		}
	}

	@Override
	public boolean canExtract(final int slot, final ItemStack stack, final Direction dir) {
		return true;
	}

	@Override
	public boolean canInsert(final int slot, final ItemStack stack, final Direction dir) {
		return this.isValid(slot, stack);
	}

	@Override
	public ScreenHandler createMenu(final int syncId, final PlayerInventory inv, final PlayerEntity player) {
		return new HandlerProjector(syncId, inv, ScreenHandlerContext.create(this.world, this.pos));
	}

	// Be sure to check canCraft() first!
	private void doCraft(final RecipeProjection recipe) {
		final ItemStack inSlot = this.items.get(0);
		final ItemStack outSlot = this.items.get(1);

		final ItemStack outStack = recipe.getOutput();

		inSlot.decrement(recipe.getCost());
		this.mBAddAlkahest(-recipe.getAlkahest());

		if (outSlot.isEmpty()) {
			this.items.set(1, outStack.copy());
		} else {
			outSlot.increment(outStack.getCount());
		}
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
			return BlockEntityProjector.TOP_SLOTS;
		} else if (side == Direction.DOWN) {
			return BlockEntityProjector.BOTTOM_SLOTS;
		} else {
			return BlockEntityProjector.SIDE_SLOTS;
		}
	}

	@Override
	public Text getDisplayName() {
		return new LiteralText("");
	}

	@Override
	public DefaultedList<ItemStack> getItems() {
		return this.items;
	}

	public int getOperationTime() {
		return this.operationTime;
	}

	@Override
	public PropertyDelegate getPropertyDelegate() {
		return this.delegate;
	}

	@Override
	public boolean isValid(final int slot, final ItemStack stack) {
		return slot == 0;
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
		this.setAlkahest((int) AoAHelper.mBToFluid(tag.getInt("alkahest"))); // As mB mostly for legacy reasons
		this.progress = tag.getInt("progress");
		this.maxProgress = this.getOperationTime();
	}

	@Override
	public void sync() {
		BlockEntityClientSerializable.super.sync();
	}

	@Override
	public void tick(
		final World world, final BlockPos pos, final BlockState state, final BlockEntityProjector blockEntity
	) {

		if (!world.isClient()) {
			final ItemStack inSlot = this.items.get(0);
			boolean canWork = false;

			if (inSlot.isEmpty() || !this.hasAlkahest()) {
				canWork = false;
			} else {
				final RecipeProjection recipe = world.getRecipeManager().getFirstMatch(AoARecipes.PROJECTION, this, world)
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
			this.sync();
		}
	}

	@Override
	public NbtCompound toClientTag(final NbtCompound tag) {
		return this.writeNbt(tag);
	}

	@Override
	public NbtCompound writeNbt(final NbtCompound tag) {
		tag.putInt("alkahest", AoAHelper.mBFromFluid(this.getAlkahest())); // As mB mostly for legacy reasons
		tag.putInt("progress", this.progress);
		Inventories.writeNbt(tag, this.items);
		return super.writeNbt(tag);
	}

	@Override
	public void writeScreenOpeningData(final ServerPlayerEntity player, final PacketByteBuf buf) {
		buf.writeBlockPos(this.pos);
	}

}
