package dev.cafeteria.artofalchemy.blockentity;

import dev.cafeteria.artofalchemy.AoAConfig;
import dev.cafeteria.artofalchemy.ArtOfAlchemy;
import dev.cafeteria.artofalchemy.block.BlockDissolver;
import dev.cafeteria.artofalchemy.essentia.EssentiaContainer;
import dev.cafeteria.artofalchemy.gui.handler.HandlerDistiller;
import dev.cafeteria.artofalchemy.item.AoAItems;
import dev.cafeteria.artofalchemy.transport.HasAlkahest;
import dev.cafeteria.artofalchemy.transport.HasEssentia;
import dev.cafeteria.artofalchemy.util.FuelHelper;
import dev.cafeteria.artofalchemy.util.ImplementedInventory;
import io.github.cottonmc.cotton.gui.PropertyDelegateHolder;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.tag.TagFactory;
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

public class BlockEntityDistiller extends BlockEntity
	implements ImplementedInventory, BlockEntityTicker<BlockEntityDistiller>, PropertyDelegateHolder,
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

	// Constant TODO: Allow config
	private static final int TANK_MAX = 16000;
	private static final int PROGRESS_MAX = 100;
	private static final int DISTILL_GAIN = 1000;
	private static final int DISTILL_ESSENTIA_COST = 1200;
	private static final int DISTILL_AZOTH_COST = 1;
	private static final int SLOT_AZOTH = 0;
	private static final int SLOT_FUEL = 1;

	// Settable
	private int tankSize;
	private float speedMod;
	private float yield;

	protected int progress = 0;
	protected int fuel = 0;
	private int essentia = 0;
	private int alkahest = 0;

	private boolean lit = false;

	protected final DefaultedList<ItemStack> items = DefaultedList.ofSize(2, ItemStack.EMPTY);
	protected EssentiaContainer essentiaInput;
	protected final PropertyDelegate delegate = new PropertyDelegate() {
		@Override
		public int get(final int index) {
			switch (index) {
				case 0:
					return BlockEntityDistiller.this.progress;
				case 1:
					return BlockEntityDistiller.PROGRESS_MAX;
				case 2:
					return BlockEntityDistiller.this.fuel;
				case 3:
					return 20; // Fuel Indicator
				case 4:
					return BlockEntityDistiller.this.essentia;
				case 5:
					return BlockEntityDistiller.this.alkahest;
				case 6:
					return BlockEntityDistiller.this.tankSize;
				default:
					return 0;
			}
		}

		@Override
		public void set(final int index, final int value) {
		}

		@Override
		public int size() {
			return 5;
		}

	};

	protected BlockEntityDistiller(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
		super(type, pos, state);
	}

	public BlockEntityDistiller(final BlockPos pos, final BlockState state) {
		this(AoABlockEntities.DISTILLER, pos, state);
		final AoAConfig.DissolverSettings settings = AoAConfig.get().dissolverSettings;
		this.tankSize = BlockEntityDistiller.TANK_MAX; // settings.tankBasic;
		this.speedMod = settings.speedBasic;
		this.yield = settings.yieldBasic;

		this.essentiaInput = new EssentiaContainer().setCapacity(this.tankSize - this.essentia).setInput(true)
			.setOutput(false);
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
		return new HandlerDistiller(syncId, inv, ScreenHandlerContext.create(this.world, this.pos));
	}

	// Assumes prerequisites have been met
	private void distill() {
		if (this.hasEnoughEssentia()) {
			this.essentia -= BlockEntityDistiller.DISTILL_ESSENTIA_COST;
		} else if (this.hasAzoth()) {
			this.items.get(BlockEntityDistiller.SLOT_AZOTH).decrement(1);
		}
		// Else: Throw?
		this.alkahest += BlockEntityDistiller.DISTILL_GAIN;
		this.updateEssentiaTankSize();
	}

	@Override
	public void fromClientTag(final NbtCompound tag) {
		this.readNbt(tag);
	}

	@Override
	public int getAlkahest() {
		return this.alkahest;
	}

	@Override
	public int[] getAvailableSlots(final Direction side) {
		if (side == Direction.UP) {
			return BlockEntityDistiller.TOP_SLOTS;
		} else if (side == Direction.DOWN) {
			return BlockEntityDistiller.BOTTOM_SLOTS;
		} else {
			return BlockEntityDistiller.SIDE_SLOTS;
		}
	}

	@Override
	public EssentiaContainer getContainer(final Direction dir) {
		return this.getContainer(0);
	}

	@Override
	public EssentiaContainer getContainer(final int id) {
		if (id == 0) {
			return this.essentiaInput;
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

	public int getTankSize() {
		return this.tankSize;
	}

	private boolean hasAzoth() {
		final ItemStack azothSlot = this.items.get(BlockEntityDistiller.SLOT_AZOTH);
		return azothSlot.getItem().equals(AoAItems.AZOTH)
			&& (azothSlot.getCount() >= BlockEntityDistiller.DISTILL_AZOTH_COST);
	}

	private boolean hasEnoughEssentia() {
		return this.essentia >= BlockEntityDistiller.DISTILL_ESSENTIA_COST;
	}

	private boolean hasFuel() {
		if (this.fuel <= 0) {
			final ItemStack fuelSlot = this.items.get(BlockEntityDistiller.SLOT_FUEL);
			if (FuelHelper.isFuel(fuelSlot)) {
				this.fuel = FuelHelper.fuelTime(fuelSlot);
				fuelSlot.decrement(1);
			} else {
				return false;
			}
		}
		return true;
	}

	private boolean hasInput() {
		return this.hasEnoughEssentia() || this.hasAzoth();
	}

	private boolean isFull() {
		return this.alkahest > (this.tankSize - BlockEntityDistiller.DISTILL_GAIN);
	}

	@Override
	public boolean isValid(final int slot, final ItemStack stack) {
		switch (slot) {
			case 0:
				return stack.isOf(AoAItems.AZOTH);
			case 1:
				return FuelHelper.isFuel(stack);
			default:
				return false;
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
		this.fuel = tag.getInt("fuel");
		this.essentia = tag.getInt("essentia");
		this.alkahest = tag.getInt("alkahest");
		this.essentiaInput = new EssentiaContainer(tag.getCompound("essentiaInput"));
	}

	@Override
	public boolean setAlkahest(final int amount) {
		return false; // Alkahest is output
	}

	private void setLit(final boolean lit) {
		this.lit = lit;
		this.updateLit();
	}

	@Override
	public void sync() {
		// AoANetworking.sendEssentiaPacket(world, pos, 0, essentiaInput); // KG: Is
		// this needed?
		BlockEntityClientSerializable.super.sync();
	}

	@Override
	public void tick(
		final World world, final BlockPos pos, final BlockState state, final BlockEntityDistiller blockEntity
	) {
		if (!world.isClient()) {
			this.tryConvertEssentia();
			if (this.hasFuel() && this.hasInput() && !this.isFull()) {
				if (this.fuel > 0) {
					this.fuel -= 2; // KG: Maybe this should replicate furnaces (don't burn new but do lose energy
													// when nothing to do)
				}
				if (!this.lit) {
					this.setLit(true);
				}

				if (++this.progress >= BlockEntityDistiller.PROGRESS_MAX) {
					this.progress = 0;
					this.distill();
				}
			} else {
				if (this.progress > 0) {
					this.progress--;
				}
				if (this.lit) {
					this.setLit(true);
				}
			}
			this.sync(); // KG: Maybe this is laggy?
		}
	}

	@Override
	public NbtCompound toClientTag(final NbtCompound tag) {
		return this.writeNbt(tag);
	}

	private void tryConvertEssentia() { // KG: Not clean, could have issues if this function is missed for a tick.
		if (!this.essentiaInput.isEmpty()) {
			this.essentia += this.essentiaInput.getCount();
			this.essentiaInput.emptyContents();
			this.updateEssentiaTankSize();
		}
	}

	private void updateEssentiaTankSize() {
		this.essentiaInput.setCapacity(BlockEntityDistiller.TANK_MAX - this.essentia);
	}

	private void updateLit() {
		this.world.setBlockState(this.pos, this.world.getBlockState(this.pos).with(BlockDissolver.LIT, this.lit));
	}

	public boolean withdrawAlkahest(final int amount) {
		if (this.alkahest > amount) {
			this.alkahest -= amount;
			return true;
		}
		return false; // Alkahest is output
	}

	@Override
	public NbtCompound writeNbt(final NbtCompound tag) {
		tag.putInt("progress", this.progress);
		tag.putInt("fuel", this.fuel);
		tag.putInt("essentia", this.essentia);
		tag.putInt("alkahest", this.alkahest);
		tag.put("essentiaInput", this.essentiaInput.writeNbt());
		Inventories.writeNbt(tag, this.items);
		return super.writeNbt(tag);
	}

	@Override
	public void writeScreenOpeningData(final ServerPlayerEntity player, final PacketByteBuf buf) {
		buf.writeBlockPos(this.pos);
	}
}
