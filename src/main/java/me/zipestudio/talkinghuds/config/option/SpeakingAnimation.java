package me.zipestudio.talkinghuds.config.option;

import com.mojang.serialization.Codec;
import me.zipestudio.talkinghuds.TalkingHuds;
import me.zipestudio.talkinghuds.utils.CodecUtils;
import me.zipestudio.talkinghuds.utils.EnumWithText;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public enum SpeakingAnimation implements EnumWithText {

	PULSE,
	RINGS,
	BOUNCE,
	NONE;

	public static final Codec<SpeakingAnimation> CODEC = CodecUtils.enumCodec(values(), PULSE);

	@Override
	public Component getText() {
		return TalkingHuds.text("animation." + name().toLowerCase(Locale.ROOT));
	}
}
