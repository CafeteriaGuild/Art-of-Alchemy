package dev.cafeteria.artofalchemy.gui.screen;

import dev.cafeteria.artofalchemy.gui.handler.AoAHandlers;
import dev.cafeteria.artofalchemy.gui.handler.HandlerAnalyzer;
import dev.cafeteria.artofalchemy.gui.handler.HandlerCalcinator;
import dev.cafeteria.artofalchemy.gui.handler.HandlerDissolver;
import dev.cafeteria.artofalchemy.gui.handler.HandlerDistiller;
import dev.cafeteria.artofalchemy.gui.handler.HandlerJournal;
import dev.cafeteria.artofalchemy.gui.handler.HandlerProjector;
import dev.cafeteria.artofalchemy.gui.handler.HandlerSynthesizer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;

@Environment(EnvType.CLIENT)
public class AoAScreens {

	public static void registerScreens() {
		ScreenRegistry.<HandlerCalcinator, ScreenCalcinator>register(AoAHandlers.CALCINATOR,
				(desc, inventory, title) -> new ScreenCalcinator(desc, inventory.player));
		ScreenRegistry.<HandlerDissolver, ScreenDissolver>register(AoAHandlers.DISSOLVER,
				(desc, inventory, title) -> new ScreenDissolver(desc, inventory.player));
		ScreenRegistry.<HandlerDistiller, ScreenDistiller>register(AoAHandlers.DISTILLER,
				(desc, inventory, title) -> new ScreenDistiller(desc, inventory.player));
		ScreenRegistry.<HandlerSynthesizer, ScreenSynthesizer>register(AoAHandlers.SYNTHESIZER,
				(desc, inventory, title) -> new ScreenSynthesizer(desc, inventory.player));
		ScreenRegistry.<HandlerAnalyzer, ScreenAnalyzer>register(AoAHandlers.ANALYZER,
				(desc, inventory, title) -> new ScreenAnalyzer(desc, inventory.player));
		ScreenRegistry.<HandlerProjector, ScreenProjector>register(AoAHandlers.PROJECTOR,
				(desc, inventory, title) -> new ScreenProjector(desc, inventory.player));
		ScreenRegistry.<HandlerJournal, ScreenJournal>register(AoAHandlers.JOURNAL,
				(desc, inventory, title) -> new ScreenJournal(desc, inventory.player));
	}

}
