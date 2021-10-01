package dev.cafeteria.artofalchemy.gui.handler;

import java.lang.reflect.InvocationTargetException;

import dev.cafeteria.artofalchemy.ArtOfAlchemy;
import dev.cafeteria.artofalchemy.block.BlockAnalyzer;
import dev.cafeteria.artofalchemy.block.BlockCalcinator;
import dev.cafeteria.artofalchemy.block.BlockDissolver;
import dev.cafeteria.artofalchemy.block.BlockDistiller;
import dev.cafeteria.artofalchemy.block.BlockProjector;
import dev.cafeteria.artofalchemy.block.BlockSynthesizer;
import dev.cafeteria.artofalchemy.item.ItemJournal;
import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WBar;
import io.github.cottonmc.cotton.gui.widget.WBar.Direction;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WItemSlot;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WSprite;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

public class AoAHandlers {
	// Constants
	public static int PANEL_WIDTH = 176;
	public static int PANEL_HEIGHT = 180;
	public static final int OFFSET = 7;
	public static final int BASIS = 18;

	public static ScreenHandlerType<HandlerCalcinator> CALCINATOR;
	public static ScreenHandlerType<HandlerDissolver> DISSOLVER;
	public static ScreenHandlerType<HandlerDistiller> DISTILLER;
	public static ScreenHandlerType<HandlerSynthesizer> SYNTHESIZER;
	public static ScreenHandlerType<HandlerAnalyzer> ANALYZER;
	public static ScreenHandlerType<HandlerProjector> PROJECTOR;
	public static ScreenHandlerType<HandlerJournal> JOURNAL;

	public static void addBigOutput(final WGridPanel panel, final WItemSlot slot) {
		panel.add(slot, (6 * AoAHandlers.BASIS) + 7 + 4, (2 * AoAHandlers.BASIS) + 4);
	}

	public static void addCentralProgressBar(final WGridPanel panel, final Identifier type) {
		AoAHandlers.addCentralProgressBar(panel, type, 0, 1);
	}

	public static void addCentralProgressBar(
		final WGridPanel panel, final Identifier type, final int progressId, final int maxProgressId
	) {
		panel.add(
			new WBar(
				new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/progress_off.png"), type, progressId, maxProgressId,
				Direction.RIGHT
			),
			(3 * AoAHandlers.BASIS) + 7,
			(2 * AoAHandlers.BASIS) + 4,
			3 * AoAHandlers.BASIS,
			AoAHandlers.BASIS
		);
	}

	public static void addInventory(final WGridPanel panel, final SyncedGuiDescription gui) {
		panel.add(gui.createPlayerInventoryPanel(), AoAHandlers.OFFSET, (5 * AoAHandlers.BASIS) - AoAHandlers.OFFSET);
	}

	private static <T extends ScreenHandler> ScreenHandlerRegistry.ExtendedClientHandlerFactory<T> defaultFactory(
		final Class<T> klass
	) {
		return (syncId, inventory, buf) -> {
			try {
				return klass.getDeclaredConstructor(int.class, PlayerInventory.class, ScreenHandlerContext.class)
					.newInstance(syncId, inventory, ScreenHandlerContext.create(inventory.player.world, buf.readBlockPos()));
			} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
				return null;
			}
		};
	}

	public static void makeBackground(final WGridPanel panel) {
		final WSprite background = new WSprite(new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/rune_bg.png"));
		panel.add(
			background,
			AoAHandlers.OFFSET,
			AoAHandlers.OFFSET,
			9 * AoAHandlers.BASIS,
			(5 * AoAHandlers.BASIS) - AoAHandlers.OFFSET
		);
	}

	public static WGridPanel makePanel(final SyncedGuiDescription gui) {
		final WGridPanel panel = new WGridPanel(1);
		gui.setRootPanel(panel);
		panel.setSize(AoAHandlers.PANEL_WIDTH, AoAHandlers.PANEL_HEIGHT);
		AoAHandlers.makeBackground(panel);
		return panel;
	}

	public static void makeTitle(final WGridPanel panel, final TranslatableText text) {
		AoAHandlers.makeTitle(panel, new WLabel(text, WLabel.DEFAULT_TEXT_COLOR));
	}

	public static void makeTitle(final WGridPanel panel, final WLabel title) {
		title.setHorizontalAlignment(HorizontalAlignment.CENTER);
		panel.add(title, 0, 3, 9 * AoAHandlers.BASIS, AoAHandlers.BASIS);
	}

	public static void registerHandlers() {
		AoAHandlers.CALCINATOR = ScreenHandlerRegistry
			.registerExtended(BlockCalcinator.getId(), AoAHandlers.defaultFactory(HandlerCalcinator.class));
		AoAHandlers.DISSOLVER = ScreenHandlerRegistry
			.registerExtended(BlockDissolver.getId(), AoAHandlers.defaultFactory(HandlerDissolver.class));
		AoAHandlers.DISTILLER = ScreenHandlerRegistry
			.registerExtended(BlockDistiller.getId(), AoAHandlers.defaultFactory(HandlerDistiller.class));
		AoAHandlers.SYNTHESIZER = ScreenHandlerRegistry
			.registerExtended(BlockSynthesizer.getId(), AoAHandlers.defaultFactory(HandlerSynthesizer.class));
		AoAHandlers.PROJECTOR = ScreenHandlerRegistry
			.registerExtended(BlockProjector.getId(), AoAHandlers.defaultFactory(HandlerProjector.class));
		AoAHandlers.ANALYZER = ScreenHandlerRegistry.registerExtended(
			BlockAnalyzer.getId(),
			(syncId, inventory, buf) -> new HandlerAnalyzer(syncId, inventory, ScreenHandlerContext.EMPTY)
		);
		AoAHandlers.JOURNAL = ScreenHandlerRegistry.registerExtended(
			ItemJournal.getId(),
			(
				syncId, inventory, buf
			) -> new HandlerJournal(syncId, inventory, ScreenHandlerContext.EMPTY, buf.readEnumConstant(Hand.class))
		);
	}
}
