package dev.cafeteria.artofalchemy.blockentity;

import org.jetbrains.annotations.Nullable;

import dev.cafeteria.artofalchemy.AoAConfig;
import dev.cafeteria.artofalchemy.ArtOfAlchemy;
import dev.cafeteria.artofalchemy.block.BlockCalcinator;
import dev.cafeteria.artofalchemy.gui.handler.HandlerCalcinator;
import dev.cafeteria.artofalchemy.recipe.AoARecipes;
import dev.cafeteria.artofalchemy.recipe.RecipeCalcination;
import dev.cafeteria.artofalchemy.util.AoAHelper;
import dev.cafeteria.artofalchemy.util.FuelHelper;
import dev.cafeteria.artofalchemy.util.ImplementedInventory;
import io.github.cottonmc.cotton.gui.PropertyDelegateHolder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.block.Block;
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
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class BlockEntityCalcinator extends BlockEntity
	implements ImplementedInventory, BlockEntityTicker<BlockEntityCalcinator>, PropertyDelegateHolder,
	SidedInventory, ExtendedScreenHandlerFactory {

	protected static final int[] TOP_SLOTS = {
		0
	};
	protected static final int[] BOTTOM_SLOTS = {
		0, 2
	};
	protected static final int[] SIDE_SLOTS = {
		1, 2
	};
	private int operationTime;
	private float yield;
	protected int fuel = 0;
	protected int maxFuel = 20;
	protected int progress = 0;
	protected int maxProgress = this.getOperationTime();

	protected final DefaultedList<ItemStack> items = DefaultedList.ofSize(3, ItemStack.EMPTY);
	protected final PropertyDelegate delegate = new PropertyDelegate() {

		@Override
		public int get(final int index) {
			switch (index) {
				case 0:
					return BlockEntityCalcinator.this.fuel;
				case 1:
					return BlockEntityCalcinator.this.maxFuel;
				case 2:
					return BlockEntityCalcinator.this.progress;
				case 3:
					return BlockEntityCalcinator.this.maxProgress;
				default:
					return 0;
			}
		}

		@Override
		public void set(final int index, final int value) {
			switch (index) {
				case 0:
					BlockEntityCalcinator.this.fuel = value;
					break;
				case 1:
					BlockEntityCalcinator.this.maxFuel = value;
					break;
				case 2:
					BlockEntityCalcinator.this.progress = value;
					break;
				case 3:
					BlockEntityCalcinator.this.maxProgress = value;
					break;
			}
		}

		@Override
		public int size() {
			return 4;
		}

	};

	protected BlockEntityCalcinator(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
		super(type, pos, state);
	}

	public BlockEntityCalcinator(final BlockPos pos, final BlockState state) {
		this(AoABlockEntities.CALCINATOR, pos, state);
		final AoAConfig.CalcinatorSettings settings = AoAConfig.get().calcinatorSettings;
		this.operationTime = settings.opTimeBasic;
		this.yield = settings.yieldBasic;
		this.maxProgress = this.getOperationTime();
	}

	private boolean canCraft(final RecipeCalcination recipe) {
		final ItemStack inSlot = this.items.get(0);
		final ItemStack outSlot = this.items.get(2);

		if ((recipe == null) || inSlot.isEmpty()) {
			return false;
		} else {
			final ItemStack outStack = recipe.getOutput();
			final ItemStack container = recipe.getContainer();

			float factor = this.getYield() * recipe.getFactor();
			if (inSlot.isDamageable()) {
				factor *= 1.0F - ((float) inSlot.getDamage() / inSlot.getMaxDamage());
			}
			final int count = (int) Math.ceil(factor * outStack.getCount());

			if ((container != ItemStack.EMPTY) && (inSlot.getCount() != container.getCount())) {
				return false;
			}

			if (outSlot.isEmpty()) {
				return true;
			} else if (outSlot.getItem() == outStack.getItem()) {
				return outSlot.getCount() <= (outSlot.getMaxCount() - count);
			} else {
				return false;
			}
		}
	}

	@Override
	public boolean canExtract(final int slot, final ItemStack stack, final Direction dir) {
		if ((dir == Direction.DOWN) && (slot == 0)) {
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
		return new HandlerCalcinator(syncId, inv, ScreenHandlerContext.create(this.world, this.pos));
	}

	// Be sure to check canCraft() first!
	private void doCraft(final RecipeCalcination recipe) {
		final ItemStack inSlot = this.items.get(0);
		final ItemStack outSlot = this.items.get(2);
		final ItemStack outStack = recipe.getOutput();
		final ItemStack container = recipe.getContainer();

		float factor = this.getYield() * recipe.getFactor();
		if (inSlot.isDamageable()) {
			factor *= 1.0F - ((float) inSlot.getDamage() / inSlot.getMaxDamage());
		}
		final int count = AoAHelper.stochasticRound(factor * outStack.getCount());

		if (container != ItemStack.EMPTY) {
			this.items.set(0, container.copy());
		} else {
			inSlot.decrement(1);
		}

		if (outSlot.isEmpty()) {
			this.items.set(2, outStack.copy());
			this.items.get(2).setCount(count);
		} else {
			outSlot.increment(count);
		}

	}

	@Nullable
	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public int[] getAvailableSlots(final Direction side) {
		if (side == Direction.UP) {
			return BlockEntityCalcinator.TOP_SLOTS;
		} else if (side == Direction.DOWN) {
			return BlockEntityCalcinator.BOTTOM_SLOTS;
		} else {
			return BlockEntityCalcinator.SIDE_SLOTS;
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

	public float getYield() {
		return this.yield;
	}

	private boolean isBurning() {
		return this.fuel > 0;
	}

	@Override
	public boolean isValid(final int slot, final ItemStack stack) {
		if (slot == 2) {
			return false;
		} else if (slot == 1) {
			return FuelHelper.isFuel(stack);
		} else {
			return true;
		}
	}

	@Override
	public void markDirty() {
		super.markDirty();
		if (!this.world.isClient()) {
			this.sync();
		}
	}
	
	public void sync() {
		world.updateListeners(pos, world.getBlockState(pos), world.getBlockState(pos), Block.NOTIFY_LISTENERS);
	}

	@Override
	public void readNbt(final NbtCompound tag) {
		super.readNbt(tag);
		Inventories.readNbt(tag, this.items);
		this.fuel = tag.getInt("fuel");
		this.progress = tag.getInt("progress");
		this.maxFuel = tag.getInt("maxFuel");
		this.maxProgress = this.getOperationTime();
	}

	@Override
	public void tick(
		final World world, final BlockPos pos, final BlockState state, final BlockEntityCalcinator blockEntity
	) {
		final boolean wasBurning = this.isBurning();
		boolean dirty = false;

		if (!world.isClient()) {
			final ItemStack inSlot = this.items.get(0);
			final ItemStack fuelSlot = this.items.get(1);

			if (wasBurning) {
				this.fuel = MathHelper.clamp(this.fuel - 1, 0, this.maxFuel);
			}

			if (!inSlot.isEmpty() && (this.isBurning() || FuelHelper.isFuel(fuelSlot))) {
				final RecipeCalcination recipe = world.getRecipeManager().getFirstMatch(AoARecipes.CALCINATION, this, world)
					.orElse(null);
				final boolean craftable = this.canCraft(recipe);

				if (!this.isBurning()) {
					if (FuelHelper.isFuel(fuelSlot) && craftable) {
						this.maxFuel = FuelHelper.fuelTime(fuelSlot);
						this.fuel += this.maxFuel;
						fuelSlot.decrement(1);
						dirty = true;
					} else if (this.progress != 0) {
						this.progress = 0;
					}
				}

				if (this.isBurning() && craftable) {
					if (this.progress < this.maxProgress) {
						this.progress++;
					}
					if (this.progress >= this.maxProgress) {
						this.doCraft(recipe);
						this.progress -= this.maxProgress;
						dirty = true;
					}
				}

				if (!craftable && (this.progress != 0)) {
					this.progress = 0;
				}
			} else if (this.progress != 0) {
				this.progress = 0;
			}

			if (this.isBurning() != wasBurning) {
				dirty = true;
				world.setBlockState(pos, world.getBlockState(pos).with(BlockCalcinator.LIT, this.isBurning()));
			}

		}

		if (dirty) {
			this.markDirty();
		}
	}

	@Override
	public NbtCompound toInitialChunkDataNbt() {
		return createNbt();
	}

	@Override
	public void writeNbt(final NbtCompound tag) {
		tag.putInt("fuel", this.fuel);
		tag.putInt("progress", this.progress);
		tag.putInt("maxFuel", this.maxFuel);
		Inventories.writeNbt(tag, this.items);
		super.writeNbt(tag);
	}

	@Override
	public void writeScreenOpeningData(final ServerPlayerEntity player, final PacketByteBuf buf) {
		buf.writeBlockPos(this.pos);
	}

}
