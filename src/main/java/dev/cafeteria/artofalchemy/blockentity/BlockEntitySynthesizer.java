package dev.cafeteria.artofalchemy.blockentity;

import dev.cafeteria.artofalchemy.AoAConfig;
import dev.cafeteria.artofalchemy.block.BlockSynthesizer;
import dev.cafeteria.artofalchemy.essentia.EssentiaContainer;
import dev.cafeteria.artofalchemy.essentia.EssentiaStack;
import dev.cafeteria.artofalchemy.gui.handler.HandlerSynthesizer;
import dev.cafeteria.artofalchemy.network.AoANetworking;
import dev.cafeteria.artofalchemy.recipe.AoARecipes;
import dev.cafeteria.artofalchemy.recipe.RecipeSynthesis;
import dev.cafeteria.artofalchemy.transport.HasEssentia;
import dev.cafeteria.artofalchemy.util.AoAHelper;
import dev.cafeteria.artofalchemy.util.AoATags;
import dev.cafeteria.artofalchemy.util.ImplementedInventory;

import io.github.cottonmc.cotton.gui.PropertyDelegateHolder;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
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

public class BlockEntitySynthesizer extends BlockEntity implements ImplementedInventory, BlockEntityTicker<BlockEntitySynthesizer>, SidedInventory,
PropertyDelegateHolder, BlockEntityClientSerializable, HasEssentia, ExtendedScreenHandlerFactory {

	private static final int[] TOP_SLOTS = new int[]{0};
	private static final int[] BOTTOM_SLOTS = new int[]{1, 2};
	private static final int[] SIDE_SLOTS = new int[]{1, 2};
	private int maxTier;
	private float speedMod;
	private int tankSize;
	private int progress = 0;
	private int maxProgress = 200;
	private int status = 0;
	// Status 0: Working
	// Status 1: Generic error (no message)
	// Status 2: Unknown or missing target item
	// Status 3: Needs materia
	// Status 4: Needs essentia
	// Status 5: Needs container
	// Status 6: Target is too complex
	private boolean lit = false;

	protected final DefaultedList<ItemStack> items = DefaultedList.ofSize(3, ItemStack.EMPTY);
	protected EssentiaContainer essentiaContainer;
	protected final PropertyDelegate delegate = new PropertyDelegate() {

		@Override
		public int size() {
			return 3;
		}

		@Override
		public void set(int index, int value) {
			switch(index) {
			case 0:
				progress = value;
				break;
			case 1:
				maxProgress = value;
				break;
			case 2:
				status = value;
				break;
			}
		}

		@Override
		public int get(int index) {
			switch(index) {
			case 0:
				return progress;
			case 1:
				return maxProgress;
			case 2:
				return status;
			default:
				return 0;
			}
		}

	};

	public BlockEntitySynthesizer(BlockPos pos, BlockState state) {
		this(AoABlockEntities.SYNTHESIZER, pos, state);
		AoAConfig.SynthesizerSettings settings = AoAConfig.get().synthesizerSettings;
		tankSize = settings.tankBasic;
		speedMod = settings.speedBasic;
		maxTier = settings.maxTierBasic.tier;
		essentiaContainer = new EssentiaContainer()
				.setCapacity(getTankSize())
				.setInput(true)
				.setOutput(false);
	}

	protected BlockEntitySynthesizer(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
		return new HandlerSynthesizer(syncId, inv, ScreenHandlerContext.create(world, pos));
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
			return essentiaContainer;
		} else {
			return null;
		}
	}

	@Override
	public int getNumContainers() {
		return 1;
	}

	private boolean updateStatus(int status) {
		if (this.status != status) {
			this.status = status;
			markDirty();
		}
		return (status == 0);
	}

	private boolean canCraft(RecipeSynthesis recipe) {
		ItemStack inSlot = items.get(0);
		ItemStack outSlot = items.get(1);
		ItemStack targetSlot = items.get(2);

		if (recipe == null || targetSlot.isEmpty()) {
			return updateStatus(2);
		} else if (recipe.getTier() > getMaxTier()) {
			return updateStatus(6);
		} else if (inSlot.isEmpty()) {
			return updateStatus(3);
		} else if (essentiaContainer.isEmpty()) {
			return updateStatus(4);
		} else {
			Ingredient container = recipe.getContainer();
			Ingredient materia = recipe.getMateria();
			EssentiaStack essentia = recipe.getEssentia();
			int cost = recipe.getCost();

			Item target = AoAHelper.getTarget(targetSlot);

			if (!materia.test(inSlot) || inSlot.getCount() < cost) {
				return updateStatus(3);
			}

			if (!essentiaContainer.contains(essentia)) {
				return updateStatus(4);
			}

			if (container != Ingredient.EMPTY) {
				if (container.test(outSlot)) {
					if (outSlot.getCount() != 1) {
						return updateStatus(1);
					} else {
						return updateStatus(0);
					}
				} else {
					return updateStatus(5);
				}
			} else {
				maxProgress = (int) Math.sqrt(essentia.getCount() / getSpeedMod());
				if (maxProgress < 2/getSpeedMod()) {
					maxProgress = (int) (2/getSpeedMod());
				}
				if (outSlot.isEmpty()) {
					return updateStatus(0);
				} else if (outSlot.getItem() == target) {
					if (outSlot.getCount() < outSlot.getMaxCount()) {
						return updateStatus(0);
					} else {
						return updateStatus(1);
					}
				} else {
					return updateStatus(1);
				}
			}
		}
	}

	// Be sure to check canCraft() first!
	private void doCraft(RecipeSynthesis recipe) {
		ItemStack inSlot = items.get(0);
		ItemStack outSlot = items.get(1);
		ItemStack targetSlot = items.get(2);
		Ingredient container = recipe.getContainer();
		EssentiaStack essentia = recipe.getEssentia();
		int cost = recipe.getCost();
		//		int xpCost = recipe.getXp(targetSlot);

		Item target = AoAHelper.getTarget(targetSlot);

		if (container != Ingredient.EMPTY || outSlot.isEmpty()) {
			items.set(1, new ItemStack(target));
		} else {
			outSlot.increment(1);
		}

		inSlot.decrement(cost);
		essentiaContainer.subtractEssentia(essentia);
		//		this.addXp(-xpCost);
	}

	@Override
	public NbtCompound writeNbt(NbtCompound tag) {
		tag.putInt("progress", progress);
		tag.putInt("max_progress", maxProgress);
		tag.putInt("status", status);
		tag.put("essentia", essentiaContainer.writeNbt());
		Inventories.writeNbt(tag, items);
		return super.writeNbt(tag);
	}

	@Override
	public void readNbt(NbtCompound tag) {
		super.readNbt(tag);
		Inventories.readNbt(tag, items);
		progress = tag.getInt("progress");
		maxProgress = tag.getInt("max_progress");
		status = tag.getInt("status");
		essentiaContainer = new EssentiaContainer(tag.getCompound("essentia"));
	}

	@Override
	public DefaultedList<ItemStack> getItems() {
		return items;
	}

	@Override
	public boolean isValid(int slot, ItemStack stack) {
		if (slot == 1) {
			return AoATags.CONTAINERS.contains(stack.getItem());
		} else {
			return true;
		}
	}


	@Override
	public void tick(World world, BlockPos pos, BlockState state, BlockEntitySynthesizer blockEntity) {
		boolean dirty = false;

		if (!world.isClient()) {
			ItemStack inSlot = items.get(0);
			ItemStack targetSlot = items.get(2);
			boolean isWorking = false;

			if (targetSlot.isEmpty()) {
				updateStatus(2);
			} else {
				RecipeSynthesis recipe = world.getRecipeManager()
						.getFirstMatch(AoARecipes.SYNTHESIS, this, world).orElse(null);

				if (canCraft(recipe)) {
					isWorking = true;
				}

				if (isWorking) {
					if (progress < maxProgress) {
						if (!lit) {
							world.setBlockState(pos, world.getBlockState(pos).with(BlockSynthesizer.LIT, true));
							lit = true;
						}
						progress++;
					}
					if (progress >= maxProgress) {
						progress -= maxProgress;
						doCraft(recipe);
						dirty = true;
					}
				}
			}

			if (!isWorking) {
				if (progress != 0) {
					progress = 0;
				}
				if (lit) {
					lit = false;
					world.setBlockState(pos, world.getBlockState(pos).with(BlockSynthesizer.LIT, false));
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
	public void fromClientTag(NbtCompound tag) {
		readNbt(tag);
	}

	@Override
	public NbtCompound toClientTag(NbtCompound tag) {
		return writeNbt(tag);
	}

	@Override
	public void sync() {
		recipeSync();
		BlockEntityClientSerializable.super.sync();
	}

	public void recipeSync() {
		EssentiaStack requirements = getRequirements();
		AoANetworking.sendEssentiaPacketWithRequirements(world, pos, 0, essentiaContainer, requirements);
	}

	public EssentiaStack getRequirements() {
		RecipeSynthesis recipe = world.getRecipeManager()
				.getFirstMatch(AoARecipes.SYNTHESIS, this, world).orElse(null);
		if (recipe == null || items.get(2).isEmpty()) {
			return new EssentiaStack();
		} else {
			return recipe.getEssentia();
		}
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
		if (slot == 1) {
			return world.isReceivingRedstonePower(pos) && isValid(slot, stack);
		} else {
			return isValid(slot, stack);
		}
	}

	@Override
	public boolean canExtract(int slot, ItemStack stack, Direction dir) {
		if (slot == 2) {
			return world.isReceivingRedstonePower(pos);
		} else {
			return true;
		}
	}

	public int getMaxTier() {
		return maxTier;
	}

	public float getSpeedMod() {
		return speedMod;
	}

	public int getTankSize() {
		return tankSize;
	}

}
