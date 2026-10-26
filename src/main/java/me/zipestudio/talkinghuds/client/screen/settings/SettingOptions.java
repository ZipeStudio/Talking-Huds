package me.zipestudio.talkinghuds.client.screen.settings;

import me.zipestudio.talkinghuds.TalkingHuds;
import me.zipestudio.talkinghuds.client.render.Draw;
import me.zipestudio.talkinghuds.client.screen.settings.SettingOption.Choice;
import me.zipestudio.talkinghuds.client.screen.settings.SettingOption.Slider;
import me.zipestudio.talkinghuds.client.screen.settings.SettingOption.Toggle;
import me.zipestudio.talkinghuds.config.LeafyConfig;
import me.zipestudio.talkinghuds.config.option.AccentColor;
import me.zipestudio.talkinghuds.config.option.DisplayMode;
import me.zipestudio.talkinghuds.config.option.HudAnchor;
import me.zipestudio.talkinghuds.config.option.SpeakingAnimation;
import me.zipestudio.talkinghuds.utils.EnumWithText;
import net.minecraft.client.gui.GuiGraphics;

public final class SettingOptions {

	public static final Toggle ENABLE_MOD =
			new Toggle("enable_mod", LeafyConfig::isEnableMod, LeafyConfig::setEnableMod);
	public static final Toggle PLASMO_VOICE =
			new Toggle("plasmo_voice", LeafyConfig::isUsePlasmoVoice, LeafyConfig::setUsePlasmoVoice);
	public static final Toggle SIMPLE_VOICE_CHAT =
			new Toggle("simple_voice_chat", LeafyConfig::isUseSimpleVoiceChat, LeafyConfig::setUseSimpleVoiceChat);

	public static final Choice<DisplayMode> DISPLAY_MODE = new Choice<>("display_mode", DisplayMode::selectable,
			EnumWithText::getText, LeafyConfig::getDisplayMode, LeafyConfig::setDisplayMode);
	public static final Choice<HudAnchor> POSITION = new Choice<>("position", HudAnchor::values,
			EnumWithText::getText, LeafyConfig::getHudAnchor, (config, anchor) -> {
				config.setHudAnchor(anchor);
				config.setOffsetX(0);
				config.setOffsetY(0);
			}, 3, SettingOptions::drawPosition,
			config -> config.getOffsetX() != 0 || config.getOffsetY() != 0 ? TalkingHuds.text("settings.position.custom") : null);
	public static final Slider FACE_SIZE = new Slider("face_size", LeafyConfig.FACE_SIZE_MIN, LeafyConfig.FACE_SIZE_MAX, 1,
			value -> (int) value + " px", LeafyConfig::getFaceSize, (config, value) -> config.setFaceSize((int) value));
	public static final Slider MAX_SHOWN = new Slider("max_shown", LeafyConfig.MAX_TALKERS_MIN, LeafyConfig.MAX_TALKERS_MAX, 1,
			value -> String.valueOf((int) value), LeafyConfig::getMaxTalkers, (config, value) -> config.setMaxTalkers((int) value));

	public static final Toggle SHOW_SELF =
			new Toggle("show_self", LeafyConfig::isShowSelf, LeafyConfig::setShowSelf);
	public static final Toggle SHOW_NAMES =
			new Toggle("show_names", LeafyConfig::isShowNames, LeafyConfig::setShowNames);
	public static final Slider FADE = new Slider("fade_time", LeafyConfig.FADE_MIN, LeafyConfig.FADE_MAX, 0.1,
			value -> String.format("%.1f s", value), LeafyConfig::getFadeSeconds, LeafyConfig::setFadeSeconds);

	public static final Choice<SpeakingAnimation> ANIMATION = new Choice<>("animation", SpeakingAnimation::values,
			EnumWithText::getText, LeafyConfig::getSpeakingAnimation, LeafyConfig::setSpeakingAnimation);
	public static final Toggle HIGHLIGHT =
			new Toggle("highlight", LeafyConfig::isHighlight, LeafyConfig::setHighlight);
	public static final Choice<AccentColor> COLOR = new Choice<>("color", AccentColor::values,
			EnumWithText::getText, LeafyConfig::getAccentColor, LeafyConfig::setAccentColor);
	public static final Slider BACKGROUND = new Slider("background", 0, 100, 5,
			value -> (int) value + "%", LeafyConfig::getBackgroundOpacity, (config, value) -> config.setBackgroundOpacity((int) value));

	private SettingOptions() {
	}

	private static final int SCREEN_ICON_WIDTH = 15;
	private static final int SCREEN_ICON_HEIGHT = 10;
	private static final int MARKER_WIDTH = 5;
	private static final int MARKER_HEIGHT = 2;

	private static void drawPosition(GuiGraphics graphics, HudAnchor anchor, int centerX, int centerY, int argb) {
		int left = centerX - SCREEN_ICON_WIDTH / 2;
		int top = centerY - SCREEN_ICON_HEIGHT / 2;
		Draw.frame(graphics, left, top, left + SCREEN_ICON_WIDTH, top + SCREEN_ICON_HEIGHT, 1, Draw.fade(argb, 0.6f));
		int innerWidth = SCREEN_ICON_WIDTH - 4 - MARKER_WIDTH;
		int innerHeight = SCREEN_ICON_HEIGHT - 4 - MARKER_HEIGHT;
		int markerLeft = left + 2 + innerWidth * anchor.column() / 2;
		int markerTop = top + 2 + innerHeight * anchor.row() / 2;
		graphics.fill(markerLeft, markerTop, markerLeft + MARKER_WIDTH, markerTop + MARKER_HEIGHT, argb);
	}
}
