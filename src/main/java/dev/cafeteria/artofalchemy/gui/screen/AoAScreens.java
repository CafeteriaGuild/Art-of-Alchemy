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
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry.Factory;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class AoAScreens {

	public static void registerScreens() {
		ScreenRegistry.<HandlerCalcinator, ScreenCalcinator>register(
			AoAHandlers.CALCINATOR,
			new Factory<HandlerCalcinator, ScreenCalcinator>() {
				@Override
				public ScreenCalcinator create(
					final HandlerCalcinator desc, final PlayerInventory inventory, final Text title
				) {
					return new ScreenCalcinator(desc, inventory.player);
				}
			}
		);
		ScreenRegistry.<HandlerDissolver, ScreenDissolver>register(
			AoAHandlers.DISSOLVER,
			new Factory<HandlerDissolver, ScreenDissolver>() {
				@Override
				public ScreenDissolver create(final HandlerDissolver desc, final PlayerInventory inventory, final Text title) {
					return new ScreenDissolver(desc, inventory.player);
				}
			}
		);
		ScreenRegistry.<HandlerDistiller, ScreenDistiller>register(
			AoAHandlers.DISTILLER,
			new Factory<HandlerDistiller, ScreenDistiller>() {
				@Override
				public ScreenDistiller create(final HandlerDistiller desc, final PlayerInventory inventory, final Text title) {
					return new ScreenDistiller(desc, inventory.player);
				}
			}
		);
		ScreenRegistry.<HandlerSynthesizer, ScreenSynthesizer>register(
			AoAHandlers.SYNTHESIZER,
			new Factory<HandlerSynthesizer, ScreenSynthesizer>() {
				@Override
				public ScreenSynthesizer create(
					final HandlerSynthesizer desc, final PlayerInventory inventory, final Text title
				) {
					return new ScreenSynthesizer(desc, inventory.player);
				}
			}
		);
		ScreenRegistry
			.<HandlerAnalyzer, ScreenAnalyzer>register(AoAHandlers.ANALYZER, new Factory<HandlerAnalyzer, ScreenAnalyzer>() {
				@Override
				public ScreenAnalyzer create(final HandlerAnalyzer desc, final PlayerInventory inventory, final Text title) {
					return new ScreenAnalyzer(desc, inventory.player);
				}
			});
		ScreenRegistry.<HandlerProjector, ScreenProjector>register(
			AoAHandlers.PROJECTOR,
			new Factory<HandlerProjector, ScreenProjector>() {
				@Override
				public ScreenProjector create(final HandlerProjector desc, final PlayerInventory inventory, final Text title) {
					return new ScreenProjector(desc, inventory.player);
				}
			}
		);
		ScreenRegistry
			.<HandlerJournal, ScreenJournal>register(AoAHandlers.JOURNAL, new Factory<HandlerJournal, ScreenJournal>() {
				@Override
				public ScreenJournal create(final HandlerJournal desc, final PlayerInventory inventory, final Text title) {
					return new ScreenJournal(desc, inventory.player);
				}
			});
	}

}
