package me.zipestudio.talkinghuds.client.screen.settings;

import me.zipestudio.talkinghuds.TalkingHuds;
import me.zipestudio.talkinghuds.config.LeafyConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.ObjDoubleConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

public sealed interface SettingOption permits SettingOption.Toggle, SettingOption.Choice, SettingOption.Slider {

	String key();

	void reset(LeafyConfig config, LeafyConfig defaults);

	default MutableComponent label() {
		return TalkingHuds.text("settings." + key());
	}

	default MutableComponent tooltip() {
		return TalkingHuds.text("settings." + key() + ".tooltip");
	}

	record Toggle(String key, Predicate<LeafyConfig> getter, BiConsumer<LeafyConfig, Boolean> setter) implements SettingOption {

		public boolean get(LeafyConfig config) {
			return getter.test(config);
		}

		public void set(LeafyConfig config, boolean value) {
			setter.accept(config, value);
		}

		@Override
		public void reset(LeafyConfig config, LeafyConfig defaults) {
			set(config, get(defaults));
		}
	}

	record Choice<T>(String key, Supplier<T[]> values, Function<T, Component> text,
	                 Function<LeafyConfig, T> getter, BiConsumer<LeafyConfig, T> setter,
	                 int columns, @Nullable CellIcon<T> icon,
	                 @Nullable Function<LeafyConfig, Component> custom) implements SettingOption {

		public Choice(String key, Supplier<T[]> values, Function<T, Component> text,
		              Function<LeafyConfig, T> getter, BiConsumer<LeafyConfig, T> setter) {
			this(key, values, text, getter, setter, 1, null, null);
		}

		public Component valueText(LeafyConfig config) {
			Component label = custom == null ? null : custom.apply(config);
			return label != null ? label : text.apply(get(config));
		}

		public T get(LeafyConfig config) {
			return getter.apply(config);
		}

		public void set(LeafyConfig config, T value) {
			setter.accept(config, value);
		}

		@Override
		public void reset(LeafyConfig config, LeafyConfig defaults) {
			set(config, get(defaults));
		}
	}

	interface CellIcon<T> {
		void draw(GuiGraphics graphics, T value, int centerX, int centerY, int argb);
	}

	record Slider(String key, double min, double max, double step, DoubleFunction<String> format,
	              ToDoubleFunction<LeafyConfig> getter, ObjDoubleConsumer<LeafyConfig> setter) implements SettingOption {

		public double get(LeafyConfig config) {
			return getter.applyAsDouble(config);
		}

		public void set(LeafyConfig config, double value) {
			setter.accept(config, snap(value));
		}

		public double snap(double value) {
			double snapped = Math.round(value / step) * step;
			return Math.max(min, Math.min(max, snapped));
		}

		@Override
		public void reset(LeafyConfig config, LeafyConfig defaults) {
			set(config, get(defaults));
		}
	}
}
