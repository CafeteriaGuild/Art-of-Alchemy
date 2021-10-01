package dev.cafeteria.artofalchemy.gui.handler;

import java.util.function.BiFunction;

import dev.cafeteria.artofalchemy.ArtOfAlchemy;
import dev.cafeteria.artofalchemy.essentia.EssentiaContainer;
import dev.cafeteria.artofalchemy.gui.widget.WEssentiaPanel;
import dev.cafeteria.artofalchemy.transport.HasEssentia;
import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WBar;
import io.github.cottonmc.cotton.gui.widget.WBar.Direction;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WItemSlot;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class HandlerDissolver extends SyncedGuiDescription {

	private static EssentiaContainer getEssentia(final ScreenHandlerContext ctx) {
		return ctx.get(new BiFunction<World, BlockPos, EssentiaContainer>() {
			@Override
			public EssentiaContainer apply(final World world, final BlockPos pos) {
				final BlockEntity be = world.getBlockEntity(pos);
				if (be instanceof HasEssentia) {
					return ((HasEssentia) be).getContainer(0);
				} else {
					return new EssentiaContainer();
				}
			}
		}, new EssentiaContainer());
	}

	final BlockPos pos;

	final WEssentiaPanel essentiaPanel;

	public HandlerDissolver(final int syncId, final PlayerInventory playerInventory, final ScreenHandlerContext ctx) {
		super(
			AoAHandlers.DISSOLVER, syncId, playerInventory, SyncedGuiDescription.getBlockInventory(ctx),
			SyncedGuiDescription.getBlockPropertyDelegate(ctx)
		);

		this.pos = ctx.get(new BiFunction<World, BlockPos, BlockPos>() {
			@Override
			public BlockPos apply(final World world, final BlockPos pos) {
				return pos;
			}
		}, null);

		final WGridPanel panel = AoAHandlers.makePanel(this);
		AoAHandlers.makeTitle(panel, new TranslatableText("block.artofalchemy.dissolution_chamber"));
		AoAHandlers.addInventory(panel, this);
		AoAHandlers
			.addCentralProgressBar(panel, new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/progress_magenta.png"), 2, 3);

		// Item Input
		panel.add(WItemSlot.of(this.blockInventory, 0), 2 * AoAHandlers.BASIS + 7, 2 * AoAHandlers.BASIS + 4);

		final WBar tankBar = new WBar(
			new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/tank_empty.png"),
			new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/tank_full.png"), 0, 1, Direction.UP
		);
		tankBar.withTooltip("gui." + ArtOfAlchemy.MOD_ID + ".alkahest_tooltip");
		panel.add(tankBar, 0, AoAHandlers.BASIS, 2 * AoAHandlers.BASIS, 3 * AoAHandlers.BASIS + 6);

		// KG: Unsure if we should keep alerts. I can see cause for adding them to help
		// new players. A manual would achieve this too. They tend to look a little out
		// of place.
		/*
		 * WDynamicLabel alert = new WDynamicLabel(() -> { switch
		 * (propertyDelegate.get(4)) { case 2: return I18n.translate("gui." +
		 * ArtOfAlchemy.MOD_ID + ".alkahest_warning"); case 3: return
		 * I18n.translate("gui." + ArtOfAlchemy.MOD_ID + ".buffer_warning"); default:
		 * return ""; } }, 0xFF5555); alert.setAlignment(HorizontalAlignment.CENTER);
		 * panel.add( alert, 0, 0 * AoAHandlers.BASIS, 9 * AoAHandlers.BASIS,
		 * AoAHandlers.BASIS );
		 */

		this.essentiaPanel = new WEssentiaPanel(HandlerDissolver.getEssentia(ctx));
		panel.add(
			this.essentiaPanel,
			6 * AoAHandlers.BASIS - 1,
			AoAHandlers.BASIS - AoAHandlers.OFFSET,
			3 * AoAHandlers.BASIS,
			4 * AoAHandlers.BASIS
		);

		panel.validate(this);
	}

	public void updateEssentia(final int essentiaId, final EssentiaContainer essentia, final BlockPos pos) {
		if (pos.equals(this.pos)) {
			this.essentiaPanel.updateEssentia(essentia);
		}
	}

}
