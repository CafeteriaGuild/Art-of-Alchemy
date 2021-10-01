package dev.cafeteria.artofalchemy.gui.widget;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;

import dev.cafeteria.artofalchemy.essentia.Essentia;
import dev.cafeteria.artofalchemy.essentia.EssentiaContainer;
import dev.cafeteria.artofalchemy.essentia.EssentiaStack;
import io.github.cottonmc.cotton.gui.widget.WListPanel;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class WEssentiaPanel extends WListPanel<Essentia, WEssentiaSubPanel> {

	protected EssentiaContainer container;
	protected EssentiaStack required = new EssentiaStack();

	public WEssentiaPanel() {
		this(new EssentiaContainer());
	}

	public WEssentiaPanel(final EssentiaContainer container) {
		super(new ArrayList<>(), null, null);
		this.container = container;
		this.supplier = WEssentiaSubPanel::new;
		this.configurator = null;
		this.updateEssentia(container);
	}

	public WEssentiaPanel(final EssentiaContainer container, final EssentiaStack required) {
		super(new ArrayList<>(), null, null);
		this.container = container;
		this.required = required;
		this.supplier = WEssentiaSubPanel::new;
		this.configurator = null;
		this.updateEssentia(container, required);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public InputResult onMouseScroll(final int x, final int y, final double amount) {
		// Pass this as a fake event to the scrollbar for centralized scroll handling
		this.scrollBar.onMouseScroll(-1, -1, amount);
		return InputResult.PROCESSED;
	}

	protected void rebuildList() {
		final Set<Essentia> essentiaSet = new HashSet<>(this.container.getContents().keySet());
		essentiaSet.addAll(this.required.keySet());

		final Map<Essentia, Integer> sortOrder = new HashMap<>();
		for (final Essentia key : essentiaSet) {
			int value;
			if (this.required.getOrDefault(key, 0) > 0) {
				value = 10000 + this.required.get(key) - this.container.getCount(key);
			} else {
				value = this.container.getCount(key);
			}
			if (value != 0) {
				sortOrder.put(key, value);
			}
		}

		this.data.clear();
		this.data.addAll(sortOrder.keySet());
		this.data.sort(new Comparator<Essentia>() {
			@Override
			public int compare(final Essentia key1, final Essentia key2) {
				return sortOrder.get(key2) - sortOrder.get(key1);
			}
		});

		if (this.data.isEmpty()) {
			this.data.add(null);
		}
	}

	protected void reconfigure() {
		for (final Entry<Essentia, WEssentiaSubPanel> entry : this.configured.entrySet()) {
			this.configurator.accept(entry.getKey(), entry.getValue());
		}
	}

	public void updateEssentia(final EssentiaContainer container) {
		this.container = container;
		this.configurator = new BiConsumer<Essentia, WEssentiaSubPanel>() {
			@Override
			public void accept(final Essentia essentia, final WEssentiaSubPanel panel) {
				panel.setEssentia(essentia, container.getCount(essentia));
			}
		};
		this.rebuildList();
		this.reconfigure();
		this.layout();
	}

	public void updateEssentia(final EssentiaContainer container, final EssentiaStack required) {
		this.container = container;
		this.required = required;
		this.configurator = new BiConsumer<Essentia, WEssentiaSubPanel>() {
			@Override
			public void accept(final Essentia essentia, final WEssentiaSubPanel panel) {
				panel.setEssentia(essentia, container.getCount(essentia), required.getOrDefault(essentia, 0));
			}
		};
		this.rebuildList();
		this.reconfigure();
		this.layout();
	}

}
