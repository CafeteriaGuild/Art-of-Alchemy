package dev.cafeteria.artofalchemy.blockentity;

import org.jetbrains.annotations.Nullable;

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
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
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
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
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

public class BlockEntitySynthesizer extends BlockEntity
	implements ImplementedInventory, BlockEntityTicker<BlockEntitySynthesizer>, SidedInventory, PropertyDelegateHolder,
	HasEssentia, ExtendedScreenHandlerFactory {

	private static final int[] TOP_SLOTS = {
		0
	};
	private static final int[] BOTTOM_SLOTS = {
		1, 2
	};
	private static final int[] SIDE_SLOTS = {
		1, 2
	};
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
		public int get(final int index) {
			switch (index) {
				case 0:
					return BlockEntitySynthesizer.this.progress;
				case 1:
					return BlockEntitySynthesizer.this.maxProgress;
				case 2:
					return BlockEntitySynthesizer.this.status;
				default:
					return 0;
			}
		}

		@Override
		public void set(final int index, final int value) {
			switch (index) {
				case 0:
					BlockEntitySynthesizer.this.progress = value;
					break;
				case 1:
					BlockEntitySynthesizer.this.maxProgress = value;
					break;
				case 2:
					BlockEntitySynthesizer.this.status = value;
					break;
			}
		}

		@Override
		public int size() {
			return 3;
		}

	};

	protected BlockEntitySynthesizer(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
		super(type, pos, state);
	}

	public BlockEntitySynthesizer(final BlockPos pos, final BlockState state) {
		this(AoABlockEntities.SYNTHESIZER, pos, state);
		final AoAConfig.SynthesizerSettings settings = AoAConfig.get().synthesizerSettings;
		this.tankSize = settings.tankBasic;
		this.speedMod = settings.speedBasic;
		this.maxTier = settings.maxTierBasic.tier;
		this.essentiaContainer = new EssentiaContainer().setCapacity(this.getTankSize()).setInput(true).setOutput(false);
	}

	private boolean canCraft(final RecipeSynthesis recipe) {
		final ItemStack inSlot = this.items.get(0);
		final ItemStack outSlot = this.items.get(1);
		final ItemStack targetSlot = this.items.get(2);

		if ((recipe == null) || targetSlot.isEmpty()) {
			return this.updateStatus(2);
		} else if (recipe.getTier() > this.getMaxTier()) {
			return this.updateStatus(6);
		} else if (inSlot.isEmpty()) {
			return this.updateStatus(3);
		} else if (this.essentiaContainer.isEmpty()) {
			return this.updateStatus(4);
		} else {
			final Ingredient container = recipe.getContainer();
			final Ingredient materia = recipe.getMateria();
			final EssentiaStack essentia = recipe.getEssentia();
			final int cost = recipe.getCost();

			final Item target = AoAHelper.getTarget(targetSlot);

			if (!materia.test(inSlot) || (inSlot.getCount() < cost)) {
				return this.updateStatus(3);
			}

			if (!this.essentiaContainer.contains(essentia)) {
				return this.updateStatus(4);
			}

			if (container != Ingredient.EMPTY) {
				if (container.test(outSlot)) {
					if (outSlot.getCount() != 1) {
						return this.updateStatus(1);
					} else {
						return this.updateStatus(0);
					}
				} else {
					return this.updateStatus(5);
				}
			} else {
				this.maxProgress = (int) Math.sqrt(essentia.getCount() / this.getSpeedMod());
				if (this.maxProgress < (2 / this.getSpeedMod())) {
					this.maxProgress = (int) (2 / this.getSpeedMod());
				}
				if (outSlot.isEmpty()) {
					return this.updateStatus(0);
				} else if (outSlot.getItem() == target) {
					if (outSlot.getCount() < outSlot.getMaxCount()) {
						return this.updateStatus(0);
					} else {
						return this.updateStatus(1);
					}
				} else {
					return this.updateStatus(1);
				}
			}
		}
	}

	@Override
	public boolean canExtract(final int slot, final ItemStack stack, final Direction dir) {
		if (slot == 2) {
			return this.world.isReceivingRedstonePower(this.pos);
		} else {
			return true;
		}
	}

	@Override
	public boolean canInsert(final int slot, final ItemStack stack, final Direction dir) {
		if (slot == 1) {
			return this.world.isReceivingRedstonePower(this.pos) && this.isValid(slot, stack);
		} else {
			return this.isValid(slot, stack);
		}
	}

	@Override
	public ScreenHandler createMenu(final int syncId, final PlayerInventory inv, final PlayerEntity player) {
		return new HandlerSynthesizer(syncId, inv, ScreenHandlerContext.create(this.world, this.pos));
	}

	// Be sure to check canCraft() first!
	private void doCraft(final RecipeSynthesis recipe) {
		final ItemStack inSlot = this.items.get(0);
		final ItemStack outSlot = this.items.get(1);
		final ItemStack targetSlot = this.items.get(2);
		final Ingredient container = recipe.getContainer();
		final EssentiaStack essentia = recipe.getEssentia();
		final int cost = recipe.getCost();
		// int xpCost = recipe.getXp(targetSlot);

		final Item target = AoAHelper.getTarget(targetSlot);

		if ((container != Ingredient.EMPTY) || outSlot.isEmpty()) {
			this.items.set(1, new ItemStack(target));
		} else {
			outSlot.increment(1);
		}

		inSlot.decrement(cost);
		this.essentiaContainer.subtractEssentia(essentia);
		// this.addXp(-xpCost);
	}

	@Nullable
	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public int[] getAvailableSlots(final Direction side) {
		if (side == Direction.UP) {
			return BlockEntitySynthesizer.TOP_SLOTS;
		} else if (side == Direction.DOWN) {
			return BlockEntitySynthesizer.BOTTOM_SLOTS;
		} else {
			return BlockEntitySynthesizer.SIDE_SLOTS;
		}
	}

	@Override
	public EssentiaContainer getContainer(final Direction dir) {
		return this.getContainer(0);
	}

	@Override
	public EssentiaContainer getContainer(final int id) {
		if (id == 0) {
			return this.essentiaContainer;
		} else {
			return null;
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

	public int getMaxTier() {
		return this.maxTier;
	}

	@Override
	public int getNumContainers() {
		return 1;
	}

	@Override
	public PropertyDelegate getPropertyDelegate() {
		return this.delegate;
	}

	public EssentiaStack getRequirements() {
		final RecipeSynthesis recipe = this.world.getRecipeManager().getFirstMatch(AoARecipes.SYNTHESIS, this, this.world)
			.orElse(null);
		if ((recipe == null) || this.items.get(2).isEmpty()) {
			return new EssentiaStack();
		} else {
			return recipe.getEssentia();
		}
	}

	public float getSpeedMod() {
		return this.speedMod;
	}

	public int getTankSize() {
		return this.tankSize;
	}

	@Override
	public boolean isValid(final int slot, final ItemStack stack) {
		if (slot == 1) {
			return AoATags.CONTAINERS.contains(stack.getItem());
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

	@Override
	public void readNbt(final NbtCompound tag) {
		super.readNbt(tag);
		Inventories.readNbt(tag, this.items);
		this.progress = tag.getInt("progress");
		this.maxProgress = tag.getInt("max_progress");
		this.status = tag.getInt("status");
		this.essentiaContainer = new EssentiaContainer(tag.getCompound("essentia"));
	}

	public void recipeSync() {
		final EssentiaStack requirements = this.getRequirements();
		AoANetworking.sendEssentiaPacketWithRequirements(this.world, this.pos, 0, this.essentiaContainer, requirements);
	}

	public void sync() {
		this.recipeSync();
		world.updateListeners(pos, world.getBlockState(pos), world.getBlockState(pos), Block.NOTIFY_LISTENERS);
	}

	@Override
	public void tick(
		final World world, final BlockPos pos, final BlockState state, final BlockEntitySynthesizer blockEntity
	) {
		boolean dirty = false;

		if (!world.isClient()) {
			this.items.get(0);
			final ItemStack targetSlot = this.items.get(2);
			boolean isWorking = false;

			if (targetSlot.isEmpty()) {
				this.updateStatus(2);
			} else {
				final RecipeSynthesis recipe = world.getRecipeManager().getFirstMatch(AoARecipes.SYNTHESIS, this, world)
					.orElse(null);

				if (this.canCraft(recipe)) {
					isWorking = true;
				}

				if (isWorking) {
					if (this.progress < this.maxProgress) {
						if (!this.lit) {
							world.setBlockState(pos, world.getBlockState(pos).with(BlockSynthesizer.LIT, true));
							this.lit = true;
						}
						this.progress++;
					}
					if (this.progress >= this.maxProgress) {
						this.progress -= this.maxProgress;
						this.doCraft(recipe);
						dirty = true;
					}
				}
			}

			if (!isWorking) {
				if (this.progress != 0) {
					this.progress = 0;
				}
				if (this.lit) {
					this.lit = false;
					world.setBlockState(pos, world.getBlockState(pos).with(BlockSynthesizer.LIT, false));
				}
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

	private boolean updateStatus(final int status) {
		if (this.status != status) {
			this.status = status;
			this.markDirty();
		}
		return status == 0;
	}

	@Override
	public void writeNbt(final NbtCompound tag) {
		tag.putInt("progress", this.progress);
		tag.putInt("max_progress", this.maxProgress);
		tag.putInt("status", this.status);
		tag.put("essentia", this.essentiaContainer.writeNbt());
		Inventories.writeNbt(tag, this.items);
		super.writeNbt(tag);
	}

	@Override
	public void writeScreenOpeningData(final ServerPlayerEntity player, final PacketByteBuf buf) {
		buf.writeBlockPos(this.pos);
	}

}
