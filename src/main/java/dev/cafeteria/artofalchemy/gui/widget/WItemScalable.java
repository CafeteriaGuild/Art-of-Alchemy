package dev.cafeteria.artofalchemy.gui.widget;

import java.util.List;

import io.github.cottonmc.cotton.gui.widget.WItem;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.Tag;

public class WItemScalable extends WItem {
	public WItemScalable(List<ItemStack> items) {
		super(items);
	}

	public WItemScalable(Tag<? extends ItemConvertible> tag) {
		super(tag);
	}

	public WItemScalable(ItemStack stack) {
		super(stack);
	}

	//	@Environment(EnvType.CLIENT)
	//	@Override
	//	public void paintBackground(int x, int y, int mouseX, int mouseY) {
	//		RenderSystem.pushMatrix();
	//		RenderSystem.enableDepthTest();
	//		RenderSystem.translatef(x, y, 0);
	//		RenderSystem.scalef((1.0f * width) / 16, (1.0f * height) / 16, 1.0f);
	//
	//		MinecraftClient mc = MinecraftClient.getInstance();
	//		ItemRenderer renderer = mc.getItemRenderer();
	//		renderer.zOffset = 100f;
	//		renderer.renderGuiItem(mc.player, getItems().get(0), getWidth() / 2 - 9, getHeight() / 2 - 9);
	//		renderer.zOffset = 0f;
	//
	//		RenderSystem.popMatrix();
	//	}
}
