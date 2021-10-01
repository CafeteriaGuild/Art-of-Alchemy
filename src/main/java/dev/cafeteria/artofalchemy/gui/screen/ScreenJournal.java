package dev.cafeteria.artofalchemy.gui.screen;

import dev.cafeteria.artofalchemy.gui.handler.HandlerJournal;
import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

@Environment(EnvType.CLIENT)
public class ScreenJournal extends CottonInventoryScreen<HandlerJournal> {

	public ScreenJournal(final HandlerJournal container, final PlayerEntity player) {
		super(container, player);
	}

	public void refresh(final ItemStack journal) {
		((HandlerJournal) this.description).refresh(journal);
	}

}
