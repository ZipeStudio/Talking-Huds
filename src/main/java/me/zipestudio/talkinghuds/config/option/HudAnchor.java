package me.zipestudio.talkinghuds.config.option;

import com.mojang.serialization.Codec;
import me.zipestudio.talkinghuds.TalkingHuds;
import me.zipestudio.talkinghuds.utils.CodecUtils;
import me.zipestudio.talkinghuds.utils.EnumWithText;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public enum HudAnchor implements EnumWithText {

	TOP_LEFT,
	TOP_CENTER,
	TOP_RIGHT,
	MIDDLE_LEFT,
	MIDDLE_CENTER,
	MIDDLE_RIGHT,
	BOTTOM_LEFT,
	BOTTOM_CENTER,
	BOTTOM_RIGHT;

	public static final Codec<HudAnchor> CODEC = CodecUtils.enumCodec(values(), TOP_CENTER);

	public static final int MARGIN = 4;

	public int originX(int guiWidth, int contentWidth) {
		return originX(guiWidth, contentWidth, MARGIN);
	}

	public int originX(int guiWidth, int contentWidth, int edge) {
		return switch (this) {
			case TOP_LEFT, MIDDLE_LEFT, BOTTOM_LEFT -> edge;
			case TOP_CENTER, MIDDLE_CENTER, BOTTOM_CENTER -> (guiWidth - contentWidth) / 2;
			case TOP_RIGHT, MIDDLE_RIGHT, BOTTOM_RIGHT -> guiWidth - contentWidth - edge;
		};
	}

	public int originY(int guiHeight, int contentHeight) {
		return originY(guiHeight, contentHeight, MARGIN);
	}

	public int originY(int guiHeight, int contentHeight, int edge) {
		return switch (this) {
			case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> edge;
			case MIDDLE_LEFT, MIDDLE_CENTER, MIDDLE_RIGHT -> (guiHeight - contentHeight) / 2;
			case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> guiHeight - contentHeight - edge;
		};
	}

	public int placeX(int guiWidth, int contentWidth, int offset, int edge) {
		return clamp(originX(guiWidth, contentWidth, edge) + offset, edge, guiWidth - contentWidth - edge);
	}

	public int placeY(int guiHeight, int contentHeight, int offset, int edge) {
		return clamp(originY(guiHeight, contentHeight, edge) + offset, edge, guiHeight - contentHeight - edge);
	}

	public static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(Math.max(min, max), value));
	}

	public static HudAnchor fromPoint(int x, int y, int guiWidth, int guiHeight) {
		int column = x < guiWidth / 3 ? 0 : x > guiWidth * 2 / 3 ? 2 : 1;
		int row = y < guiHeight / 3 ? 0 : y > guiHeight * 2 / 3 ? 2 : 1;
		return switch (row * 3 + column) {
			case 0 -> TOP_LEFT;
			case 1 -> TOP_CENTER;
			case 2 -> TOP_RIGHT;
			case 3 -> MIDDLE_LEFT;
			case 4 -> MIDDLE_CENTER;
			case 5 -> MIDDLE_RIGHT;
			case 6 -> BOTTOM_LEFT;
			case 7 -> BOTTOM_CENTER;
			default -> BOTTOM_RIGHT;
		};
	}

	public int column() {
		return ordinal() % 3;
	}

	public int row() {
		return ordinal() / 3;
	}

	public boolean isRight() {
		return this == TOP_RIGHT || this == MIDDLE_RIGHT || this == BOTTOM_RIGHT;
	}

	@Override
	public Component getText() {
		return TalkingHuds.text("anchor." + name().toLowerCase(Locale.ROOT));
	}
}
