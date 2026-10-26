package me.zipestudio.talkinghuds.client.screen.settings;

import me.zipestudio.talkinghuds.client.screen.settings.SettingOption.Slider;
import me.zipestudio.talkinghuds.config.LeafyConfig;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public final class ValueSlider extends AbstractSliderButton {

	private final Slider option;
	private final LeafyConfig config;
	private final Runnable onChange;

	ValueSlider(SettingsLayout.Slot slot, Slider option, LeafyConfig config, Runnable onChange) {
		super(slot.x(), slot.y(), slot.width(), slot.height(), Component.empty(), fraction(option, config));
		this.option = option;
		this.config = config;
		this.onChange = onChange;
		updateMessage();
	}

	private static double fraction(Slider option, LeafyConfig config) {
		return Mth.clamp((option.get(config) - option.min()) / (option.max() - option.min()), 0.0, 1.0);
	}

	public void sync() {
		this.value = fraction(option, config);
		updateMessage();
	}

	void nudge(double scroll) {
		if (scroll == 0) {
			return;
		}
		option.set(config, option.get(config) + (scroll > 0 ? option.step() : -option.step()));
		sync();
		onChange.run();
	}

	@Override
	protected void updateMessage() {
		setMessage(option.label().append(": ").append(option.format().apply(option.get(config))));
	}

	@Override
	protected void applyValue() {
		option.set(config, option.min() + this.value * (option.max() - option.min()));
		onChange.run();
	}
}
