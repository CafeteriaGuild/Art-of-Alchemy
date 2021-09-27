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
import net.minecraft.world.World;

public class BlockEntityDissolver extends BlockEntity implements ImplementedInventory, BlockEntityTicker<BlockEntityDissolver>, PropertyDelegateHolder,
BlockEntityClientSerializable, HasEssentia, HasAlkahest, SidedInventory, ExtendedScreenHandlerFactory {

	private static final int[] TOP_SLOTS = new int[]{0};
	private static final int[] BOTTOM_SLOTS = new int[]{0};
	private static final int[] SIDE_SLOTS = new int[]{0};
	private int tankSize;
	private float speedMod;
	private float yield;
	private int alkahest = 0;
	protected int maxAlkahest = getTankSize();
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
		public int size() {
			return 5;
		}

		@Override
		public void set(int index, int value) {
			switch(index) {
			case 0:
				alkahest = value;
				break;
			case 1:
				maxAlkahest = value;
				break;
			case 2:
				progress = value;
				break;
			case 3:
				maxProgress = value;
				break;
			case 4:
				status = value;
				break;
			}
		}

		@Override
		public int get(int index) {
			switch(index) {
			case 0:
				return alkahest;
			case 1:
				return maxAlkahest;
			case 2:
				return progress;
			case 3:
				return maxProgress;
			case 4:
				return status;
			default:
				return 0;
			}
		}

	};

	public BlockEntityDissolver(BlockPos pos, BlockState state) {
		this(AoABlockEntities.DISSOLVER, pos, state);
		AoAConfig.DissolverSettings settings = AoAConfig.get().dissolverSettings;
		tankSize = settings.tankBasic;
		speedMod = settings.speedBasic;
		this.yield = settings.yieldBasic;
		maxAlkahest = getTankSize();
		essentia = new EssentiaContainer()
				.setCapacity(getTankSize())
				.setInput(false)
				.setOutput(true);
	}

	protected BlockEntityDissolver(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
		return new HandlerDissolver(syncId, inv, ScreenHandlerContext.create(world, pos));
	}

	@Override
	public Text getDisplayName() {
		return new LiteralText("");
	}

	@Override
	public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
		buf.writeBlockPos(pos);
	}

	@Override
	public EssentiaContainer getContainer(Direction dir) {
		return getContainer(0);
	}

	@Override
	public EssentiaContainer getContainer(int id) {
		if (id == 0) {
			return essentia;
		} else {
			return null;
		}
	}

	@Override
	public int getNumContainers() {
		return 1;
	}

	@Override
	public int getAlkahest() {
		return alkahest;
	}

	@Override
	public boolean setAlkahest(int amount) {
		if (amount >= 0 && amount <= maxAlkahest) {
			alkahest = amount;
			world.setBlockState(pos, world.getBlockState(pos).with(BlockDissolver.FILLED, alkahest > 0));
			markDirty();
			return true;
		} else {
			return false;
		}
	}

	private boolean updateStatus(int status) {
		if (this.status != status) {
			this.status = status;
			markDirty();
		}
		return (status == 0);
	}

	private boolean canCraft(RecipeDissolution recipe) {
		ItemStack inSlot = items.get(0);

		if (recipe == null || inSlot.isEmpty()) {
			return updateStatus(1);
		} else {
			ItemStack container = recipe.getContainer();
			EssentiaStack results = recipe.getEssentia();

			maxProgress = (int) Math.sqrt(results.getCount() / getSpeedMod());
			if (maxProgress < 2/getSpeedMod()) {
				maxProgress = (int) (2/getSpeedMod());
			}

			if (container != ItemStack.EMPTY && inSlot.getCount() != container.getCount()) {
				return updateStatus(1);
			}

			float factor = getEfficiency() * recipe.getFactor();
			if (inSlot.isDamageable()) {
				factor *= 1.0 - (float) inSlot.getDamage() / inSlot.getMaxDamage();
			}
			results.multiply(factor);

			if (results.getCount() > alkahest) {
				return updateStatus(2);
			} else {
				if (!essentia.canAcceptIgnoreIO(results)) {
					return updateStatus(3);
				} else {
					return updateStatus(0);
				}
			}
		}
	}

	// Be sure to check canCraft() first!
	private void doCraft(RecipeDissolution recipe) {
		ItemStack inSlot = items.get(0);
		EssentiaStack results = recipe.getEssentia();
		ItemStack container = recipe.getContainer();

		float factor = getEfficiency() * recipe.getFactor();
		if (inSlot.isDamageable()) {
			factor *= 1.0 - (float) inSlot.getDamage() / inSlot.getMaxDamage();
		}
		results.multiplyStochastic(factor);

		if (container != ItemStack.EMPTY) {
			items.set(0, container.copy());
		} else {
			inSlot.decrement(1);
		}

		essentia.addEssentia(results);
		alkahest -= results.getCount();

	}

	@Override
	public NbtCompound writeNbt(NbtCompound tag) {
		tag.putInt("alkahest", alkahest);
		tag.putInt("progress", progress);
		tag.putInt("max_progress", maxProgress);
		tag.putInt("status", status);
		tag.put("essentia", essentia.writeNbt());
		Inventories.writeNbt(tag, items);
		return super.writeNbt(tag);
	}

	@Override
	public void readNbt(NbtCompound tag) {
		super.readNbt(tag);
		Inventories.readNbt(tag, items);
		alkahest = tag.getInt("alkahest");
		progress = tag.getInt("progress");
		maxProgress = tag.getInt("max_progress");
		status = tag.getInt("status");
		essentia = new EssentiaContainer(tag.getCompound("essentia"));
		maxAlkahest = getTankSize();
	}

	@Override
	public DefaultedList<ItemStack> getItems() {
		return items;
	}

	@Override
	public boolean isValid(int slot, ItemStack stack) {
		return true;
	}


	@Override
	public void tick(World world, BlockPos pos, BlockState state, BlockEntityDissolver blockEntity) {
		boolean dirty = false;

		if (!world.isClient()) {
			ItemStack inSlot = items.get(0);
			boolean canWork = false;

			if (inSlot.isEmpty()) {
				updateStatus(1);
			} else if (!hasAlkahest()) {
				updateStatus(2);
			} else {
				RecipeDissolution recipe = world.getRecipeManager()
						.getFirstMatch(AoARecipes.DISSOLUTION, this, world).orElse(null);
				canWork = canCraft(recipe);

				if (canWork) {
					if (progress < maxProgress) {
						if (!lit) {
							world.setBlockState(pos, world.getBlockState(pos).with(BlockDissolver.LIT, true));
							lit = true;
						}
						progress++;
						dirty = true;
					}
					if (progress >= maxProgress) {
						progress -= maxProgress;
						doCraft(recipe);
						if (alkahest <= 0) {
							world.setBlockState(pos, world.getBlockState(pos).with(BlockDissolver.FILLED, false));
						}
						dirty = true;
					}
				}
			}

			if (!canWork) {
				if (progress != 0) {
					progress = 0;
					dirty = true;
				}
				if (lit) {
					lit = false;
					world.setBlockState(pos, world.getBlockState(pos).with(BlockDissolver.LIT, false));
					dirty = true;
				}
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
	public void sync() {
		AoANetworking.sendEssentiaPacket(world, pos, 0, essentia);
		BlockEntityClientSerializable.super.sync();
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
		if (dir == Direction.DOWN) {
			return TagRegistry.item(ArtOfAlchemy.id("containers")).contains(stack.getItem());
		} else {
			return true;
		}
	}

	public int getTankSize() {
		return tankSize;
	}

	public float getSpeedMod() {
		return speedMod;
	}

	public float getEfficiency() {
		return yield;
	}
}
