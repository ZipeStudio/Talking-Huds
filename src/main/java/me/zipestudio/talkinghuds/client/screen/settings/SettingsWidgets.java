package me.zipestudio.talkinghuds.client.screen.settings;

import me.zipestudio.talkinghuds.TalkingHuds;
import me.zipestudio.talkinghuds.client.screen.settings.SettingOption.Choice;
import me.zipestudio.talkinghuds.client.screen.settings.SettingOption.Slider;
import me.zipestudio.talkinghuds.client.screen.settings.SettingOption.Toggle;
import me.zipestudio.talkinghuds.client.screen.settings.SettingsLayout.Slot;
import me.zipestudio.talkinghuds.config.LeafyConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class SettingsWidgets {

	private static final long TOOLTIP_DELAY_MS = 500L;
	private static final BooleanSupplier ALWAYS = () -> true;

	private final LeafyConfig config;
	private final Consumer<AbstractWidget> sink;
	private final Runnable onChange;
	private final List<Entry> entries = new ArrayList<>();
	private final List<ValueSlider> sliders = new ArrayList<>();
	private final List<Button> buttons = new ArrayList<>();
	private final ChoiceDropdown dropdown = new ChoiceDropdown();
	private int screenHeight;

	public SettingsWidgets(LeafyConfig config, Consumer<AbstractWidget> sink, Runnable onChange) {
		this.config = config;
		this.sink = sink;
		this.onChange = onChange;
	}

	public void clear() {
		entries.clear();
		sliders.clear();
		buttons.clear();
		dropdown.close();
	}

	public static <T extends AbstractWidget> T tip(T widget, Component text) {
		return tip(widget, text, TOOLTIP_DELAY_MS);
	}

	public static <T extends AbstractWidget> T tip(T widget, Component text, long delayMs) {
		widget.setTooltip(Tooltip.create(text));
		//? if >=1.21 {
		widget.setTooltipDelay(Duration.ofMillis(delayMs));
		//?} else {
		/*widget.setTooltipDelay((int) delayMs);
		*///?}
		return widget;
	}

	public Button button(Slot slot, String key, Runnable action) {
		Button widget = Button.builder(TalkingHuds.text(key), ignored -> action.run())
				.bounds(slot.x(), slot.y(), slot.width(), slot.height())
				.build();
		tip(widget, TalkingHuds.text(key + ".tooltip"));
		sink.accept(widget);
		buttons.add(widget);
		return widget;
	}

	public Button toggle(Slot slot, Toggle option) {
		return toggle(slot, option, ALWAYS);
	}

	public Button toggle(Slot slot, Toggle option, BooleanSupplier relevant) {
		Button widget = Button.builder(toggleLabel(option), button -> {
			option.set(config, !option.get(config));
			button.setMessage(toggleLabel(option));
			onChange.run();
		}).bounds(slot.x(), slot.y(), slot.width(), slot.height()).build();
		register(widget, option, relevant, () -> widget.setMessage(toggleLabel(option)));
		return widget;
	}

	public <T> Button choice(Slot slot, Choice<T> option) {
		return choice(slot, option, ALWAYS);
	}

	public <T> Button choice(Slot slot, Choice<T> option, BooleanSupplier relevant) {
		return choice(slot, option, relevant, () -> {
		});
	}

	public <T> Button choice(Slot slot, Choice<T> option, BooleanSupplier relevant, Runnable afterPick) {
		Button widget = Button.builder(choiceLabel(option), button -> toggleDropdown(button, option, afterPick))
				.bounds(slot.x(), slot.y(), slot.width(), slot.height()).build();
		register(widget, option, relevant, () -> widget.setMessage(choiceLabel(option)));
		return widget;
	}

	private <T> void toggleDropdown(Button button, Choice<T> option, Runnable afterPick) {
		if (dropdown.isOpenFor(button)) {
			dropdown.close();
			return;
		}
		dropdown.open(button, screenHeight, List.of(option.values().get()), option.get(config), option.text(),
				option.columns(), option.icon(), value -> {
					pick(button, option, value);
					afterPick.run();
				});
	}

	private <T> void pick(Button button, Choice<T> option, T value) {
		option.set(config, value);
		button.setMessage(choiceLabel(option));
		onChange.run();
	}

	public ChoiceDropdown dropdown() {
		return dropdown;
	}

	public void setScreenHeight(int screenHeight) {
		this.screenHeight = screenHeight;
	}

	public ValueSlider slider(Slot slot, Slider option) {
		return slider(slot, option, ALWAYS);
	}

	public ValueSlider slider(Slot slot, Slider option, BooleanSupplier relevant) {
		ValueSlider widget = new ValueSlider(slot, option, config, onChange);
		register(widget, option, relevant, widget::sync);
		sliders.add(widget);
		return widget;
	}

	private void register(AbstractWidget widget, SettingOption option, BooleanSupplier relevant, Runnable refreshLabel) {
		tip(widget, option.tooltip().append("\n\n").append(TalkingHuds.text("settings.hint.reset").withStyle(ChatFormatting.DARK_GRAY)));
		sink.accept(widget);
		entries.add(new Entry(widget, option, relevant, refreshLabel));
	}

	private Component toggleLabel(Toggle option) {
		return label(option, TalkingHuds.onOff(option.get(config)));
	}

	private <T> Component choiceLabel(Choice<T> option) {
		return label(option, option.valueText(config));
	}

	private static Component label(SettingOption option, Component value) {
		MutableComponent name = option.label();
		return name.append(": ").append(value);
	}

	public void refresh(int top, int bottom, boolean shown) {
		for (Entry entry : entries) {
			AbstractWidget widget = entry.widget();
			widget.visible = shown && widget.getY() >= top && widget.getY() + widget.getHeight() <= bottom;
			widget.active = entry.relevant().getAsBoolean();
		}
		for (Button button : buttons) {
			button.visible = shown;
		}
	}

	public boolean resetHovered(double mouseX, double mouseY) {
		Entry entry = hovered(mouseX, mouseY);
		if (entry == null) {
			return false;
		}
		entry.option().reset(config, LeafyConfig.getNewInstance());
		entry.refreshLabel().run();
		onChange.run();
		return true;
	}

	public boolean nudgeHovered(double mouseX, double mouseY, double scroll) {
		for (ValueSlider slider : sliders) {
			if (slider.visible && slider.active && slider.isMouseOver(mouseX, mouseY)) {
				slider.nudge(scroll);
				return true;
			}
		}
		return false;
	}

	public void syncAll() {
		for (Entry entry : entries) {
			entry.refreshLabel().run();
		}
	}

	@Nullable
	private Entry hovered(double mouseX, double mouseY) {
		for (Entry entry : entries) {
			AbstractWidget widget = entry.widget();
			if (widget.visible && widget.active && widget.isMouseOver(mouseX, mouseY)) {
				return entry;
			}
		}
		return null;
	}

	private record Entry(AbstractWidget widget, SettingOption option, BooleanSupplier relevant, Runnable refreshLabel) {
	}
}
