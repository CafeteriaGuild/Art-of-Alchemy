package dev.cafeteria.artofalchemy.blockentity;

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
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.tag.TagRegistry;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class BlockEntityCalcinator extends BlockEntity implements ImplementedInventory, BlockEntityTicker<BlockEntityCalcinator>,
PropertyDelegateHolder, BlockEntityClientSerializable, SidedInventory, ExtendedScreenHandlerFactory {

	protected static final int[] TOP_SLOTS = new int[]{0};
	protected static final int[] BOTTOM_SLOTS = new int[]{0, 2};
	protected static final int[] SIDE_SLOTS = new int[]{1, 2};
	private int operationTime;
	private float yield;
	protected int fuel = 0;
	protected int maxFuel = 20;
	protected int progress = 0;
	protected int maxProgress = getOperationTime();

	protected final DefaultedList<ItemStack> items = DefaultedList.ofSize(3, ItemStack.EMPTY);
	protected final PropertyDelegate delegate = new PropertyDelegate() {

		@Override
		public int size() {
			return 4;
		}

		@Override
		public void set(int index, int value) {
			switch(index) {
			case 0:
				fuel = value;
				break;
			case 1:
				maxFuel = value;
				break;
			case 2:
				progress = value;
				break;
			case 3:
				maxProgress = value;
				break;
			}
		}

		@Override
		public int get(int index) {
			switch(index) {
			case 0:
				return fuel;
			case 1:
				return maxFuel;
			case 2:
				return progress;
			case 3:
				return maxProgress;
			default:
				return 0;
			}
		}

	};

	public BlockEntityCalcinator(BlockPos pos, BlockState state) {
		this(AoABlockEntities.CALCINATOR, pos, state);
		AoAConfig.CalcinatorSettings settings = AoAConfig.get().calcinatorSettings;
		operationTime = settings.opTimeBasic;
		this.yield = settings.yieldBasic;
		maxProgress = getOperationTime();
	}

	protected BlockEntityCalcinator(BlockEntityType type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
		return new HandlerCalcinator(syncId, inv, ScreenHandlerContext.create(world, pos));
	}

	@Override
	public Text getDisplayName() {
		return new LiteralText("");
	}

	@Override
	public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
		buf.writeBlockPos(pos);
	}

	private boolean isBurning() {
		return fuel > 0;
	}

	private boolean canCraft(RecipeCalcination recipe) {
		ItemStack inSlot = items.get(0);
		ItemStack outSlot = items.get(2);

		if (recipe == null || inSlot.isEmpty()) {
			return false;
		} else {
			ItemStack outStack = recipe.getOutput();
			ItemStack container = recipe.getContainer();

			float factor = getYield() * recipe.getFactor();
			if (inSlot.isDamageable()) {
				factor *= 1.0F - (float) inSlot.getDamage() / inSlot.getMaxDamage();
			}
			int count = (int) Math.ceil(factor * outStack.getCount());

			if (container != ItemStack.EMPTY && inSlot.getCount() != container.getCount()) {
				return false;
			}

			if (outSlot.isEmpty()) {
				return true;
			} else if (outSlot.getItem() == outStack.getItem()) {
				return outSlot.getCount() <= outSlot.getMaxCount() - count;
			} else {
				return false;
			}
		}
	}

	// Be sure to check canCraft() first!
	private void doCraft(RecipeCalcination recipe) {
		ItemStack inSlot = items.get(0);
		ItemStack outSlot = items.get(2);
		ItemStack outStack = recipe.getOutput();
		ItemStack container = recipe.getContainer();

		float factor = getYield() * recipe.getFactor();
		if (inSlot.isDamageable()) {
			factor *= 1.0F - (float) inSlot.getDamage() / inSlot.getMaxDamage();
		}
		int count = AoAHelper.stochasticRound(factor * outStack.getCount());

		if (container != ItemStack.EMPTY) {
			items.set(0, container.copy());
		} else {
			inSlot.decrement(1);
		}

		if (outSlot.isEmpty()) {
			items.set(2, outStack.copy());
			items.get(2).setCount(count);
		} else {
			outSlot.increment(count);
		}

	}

	@Override
	public NbtCompound writeNbt(NbtCompound tag) {
		tag.putInt("fuel", fuel);
		tag.putInt("progress", progress);
		tag.putInt("maxFuel", maxFuel);
		Inventories.writeNbt(tag, items);
		return super.writeNbt(tag);
	}

	@Override
	public void readNbt(NbtCompound tag) {
		super.readNbt(tag);
		Inventories.readNbt(tag, items);
		fuel = tag.getInt("fuel");
		progress = tag.getInt("progress");
		maxFuel = tag.getInt("maxFuel");
		maxProgress = getOperationTime();
	}

	@Override
	public DefaultedList<ItemStack> getItems() {
		return items;
	}

	@Override
	public boolean isValid(int slot, ItemStack stack) {
		if (slot == 2) {
			return false;
		} else if (slot == 1) {
			return FuelHelper.isFuel(stack);
		} else {
			return true;
		}
	}


	@Override
	public void tick(World world, BlockPos pos, BlockState state, BlockEntityCalcinator blockEntity) {
		boolean wasBurning = isBurning();
		boolean dirty = false;

		if (!world.isClient()) {
			ItemStack inSlot = items.get(0);
			ItemStack fuelSlot = items.get(1);

			if (wasBurning) {
				fuel = MathHelper.clamp(fuel - 1, 0, maxFuel);
			}

			if (!inSlot.isEmpty() && (isBurning() || FuelHelper.isFuel(fuelSlot))) {
				RecipeCalcination recipe = world.getRecipeManager()
						.getFirstMatch(AoARecipes.CALCINATION, this, world).orElse(null);
				boolean craftable = canCraft(recipe);

				if (!isBurning()) {
					if (FuelHelper.isFuel(fuelSlot) && craftable) {
						maxFuel = FuelHelper.fuelTime(fuelSlot);
						fuel += maxFuel;
						fuelSlot.decrement(1);
						dirty = true;
					} else if (progress != 0) {
						progress = 0;
					}
				}

				if (isBurning() && craftable) {
					if (progress < maxProgress) {
						progress++;
					}
					if (progress >= maxProgress) {
						doCraft(recipe);
						progress -= maxProgress;
						dirty = true;
					}
				}

				if (!craftable && progress != 0) {
					progress = 0;
				}
			} else if (progress != 0) {
				progress = 0;
			}

			if (isBurning() != wasBurning) {
				dirty = true;
				world.setBlockState(pos, world.getBlockState(pos).with(BlockCalcinator.LIT, isBurning()));
			}

		}

		if (dirty) {
			markDirty();
		}
	}

	@Override
	public PropertyDelegate getPropertyDelegate() {
		return delegate;
	}

	@Override
	public void markDirty() {
		super.markDirty();
		if (!world.isClient()) {
			sync();
		}
	}

	@Override
	public void fromClientTag(NbtCompound tag) {
		readNbt(tag);
	}

	@Override
	public NbtCompound toClientTag(NbtCompound tag) {
		return writeNbt(tag);
	}

	@Override
	public int[] getAvailableSlots(Direction side) {
		if (side == Direction.UP) {
			return TOP_SLOTS;
		} else if (side == Direction.DOWN) {
			return BOTTOM_SLOTS;
		} else {
			return SIDE_SLOTS;
		}
	}

	@Override
	public boolean canInsert(int slot, ItemStack stack, Direction dir) {
		return isValid(slot, stack);
	}

	@Override
	public boolean canExtract(int slot, ItemStack stack, Direction dir) {
		if (dir == Direction.DOWN && slot == 0) {
			return TagRegistry.item(ArtOfAlchemy.id("containers")).contains(stack.getItem());
		} else {
			return true;
		}
	}

	public int getOperationTime() {
		return operationTime;
	}

	public float getYield() {
		return yield;
	}

}
