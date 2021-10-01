package dev.cafeteria.artofalchemy.gui.widget;

import dev.cafeteria.artofalchemy.ArtOfAlchemy;
import dev.cafeteria.artofalchemy.essentia.Essentia;
import dev.cafeteria.artofalchemy.essentia.RegistryEssentia;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.WSprite;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class WEssentiaSubPanel extends WPlainPanel {

	private static class EssentiaLabel extends WLabel {

		public EssentiaLabel(final String text) {
			super(text);
		}

		@Environment(EnvType.CLIENT)
		@Override
		public InputResult onMouseScroll(final int x, final int y, final double amount) {
			this.parent.onMouseScroll(-1, -1, amount);
			return InputResult.PROCESSED;
		}

	}

	// Ideally, every WWidget would automatically pass any unhandled scroll events
	// up to the parent. Until that happens, we can manually emulate this behavior
	// without having to touch LibGui for the time being.
	private static class EssentiaSprite extends WSprite {

		public EssentiaSprite(final Identifier image) {
			super(image);
		}

		@Environment(EnvType.CLIENT)
		@Override
		public InputResult onMouseScroll(final int x, final int y, final double amount) {
			this.parent.onMouseScroll(-1, -1, amount);
			return InputResult.PROCESSED;
		}

	}

	private static final Identifier SYMBOLS_EMPTY = new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/symbols/empty.png");
	private Essentia essentia = null;
	private Integer amount = 0;
	private final WSprite bg = new EssentiaSprite(ArtOfAlchemy.id("textures/gui/essentia_banner.png"));
	private final WSprite sprite = new EssentiaSprite(WEssentiaSubPanel.SYMBOLS_EMPTY);

	private final WLabel amtLabel = new EssentiaLabel("0");

	private final WLabel typeLabel = new EssentiaLabel("Empty");

	public WEssentiaSubPanel() {
		this.bg.setParent(this);
		this.add(this.bg, -4, -4, 54, 18);

		this.sprite.setParent(this);
		this.add(this.sprite, 25, -4, 18, 18);

		this.amtLabel.setHorizontalAlignment(HorizontalAlignment.RIGHT);
		this.amtLabel.setParent(this);
		this.add(this.amtLabel, 8, -4);

		this.typeLabel.setHorizontalAlignment(HorizontalAlignment.LEFT);
		this.typeLabel.setParent(this);
		this.add(this.typeLabel, -3, 5);
	}

	public Integer getAmount() {
		return this.amount;
	}

	public Essentia getEssentia() {
		return this.essentia;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public InputResult onMouseScroll(final int x, final int y, final double amount) {
		this.parent.onMouseScroll(-1, -1, amount);
		return InputResult.PROCESSED;
	}

	public void setEssentia(final Essentia essentia, final Integer amount) {
		this.essentia = essentia;
		this.amount = amount;
		final Identifier id = RegistryEssentia.INSTANCE.getId(essentia);
		if (essentia != null) {
			this.bg.setTint(essentia.getColor());
			this.sprite.setImage(new Identifier(id.getNamespace(), "textures/gui/symbols/" + id.getPath() + ".png"));
			this.amtLabel.setText(new LiteralText(amount.toString()));
			this.typeLabel.setText(new TranslatableText("essentia." + id.getNamespace() + "." + id.getPath()));
			this.typeLabel.setColor(0xFFFFFF, 0xFFFFFF);
		} else {
			this.sprite.setImage(WEssentiaSubPanel.SYMBOLS_EMPTY);
			this.typeLabel.setText(new TranslatableText("gui." + ArtOfAlchemy.MOD_ID + ".empty"));
			this.amtLabel.setText(new LiteralText(""));
		}
		this.amtLabel.setColor(WLabel.DEFAULT_TEXT_COLOR, WLabel.DEFAULT_DARKMODE_TEXT_COLOR);
		this.layout();
	}

	public void setEssentia(final Essentia essentia, final Integer amount, final Integer required) {
		this.setEssentia(essentia, amount);
		if (required > 0) {
			if (amount < required) {
				this.amtLabel.setColor(0xAA0000, 0xFF5555);
				this.amtLabel.setText(new LiteralText(Integer.toString(amount - required)));
			} else {
				this.amtLabel.setColor(0x00AA00, 0x55FF55);
				this.amtLabel.setText(new LiteralText("+" + (amount - required)));
			}
		}
	}

}
