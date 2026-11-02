package me.zipestudio.talkinghuds.config.option;

import com.mojang.serialization.Codec;
import me.zipestudio.talkinghuds.TalkingHuds;
import me.zipestudio.talkinghuds.utils.CodecUtils;
import me.zipestudio.talkinghuds.utils.EnumWithText;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public enum DisplayMode implements EnumWithText {

	HEADS_ROW,
	HEADS_COLUMN,
	LIST,
	SUBTITLES,
	LOCATOR_BAR;

	public static final Codec<DisplayMode> CODEC = CodecUtils.enumCodec(values(), HEADS_ROW);

	public static DisplayMode[] selectable() {
		//? if >=1.21.6 {
		return values();
		//?} else {
		/*return new DisplayMode[]{HEADS_ROW, HEADS_COLUMN, LIST, SUBTITLES};
		*///?}
	}

	public boolean isMovable() {
		return isHeads() || this == LIST;
	}

	public boolean isHeads() {
		return this == HEADS_ROW || this == HEADS_COLUMN;
	}

	@Override
	public Component getText() {
		return TalkingHuds.text("display." + name().toLowerCase(Locale.ROOT));
	}
}
