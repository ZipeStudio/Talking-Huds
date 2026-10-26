package me.zipestudio.talkinghuds.config.option;

import com.mojang.serialization.Codec;
import me.zipestudio.talkinghuds.TalkingHuds;
import me.zipestudio.talkinghuds.client.render.PlayerColors;
import me.zipestudio.talkinghuds.utils.CodecUtils;
import me.zipestudio.talkinghuds.utils.EnumWithText;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.UUID;

public enum AccentColor implements EnumWithText {

	PLAYER(-1),
	GREEN(0x43B581),
	LIME(0x7CFC00),
	INDIGO(0x5865F2),
	CYAN(0x00B0F4),
	PINK(0xEB459E),
	RED(0xED4245),
	ORANGE(0xFFA500),
	YELLOW(0xFEE75C),
	WHITE(0xFFFFFF);

	public static final Codec<AccentColor> CODEC = CodecUtils.enumCodec(values(), PLAYER);

	private final int rgb;

	AccentColor(int rgb) {
		this.rgb = rgb;
	}

	public int colorFor(UUID player) {
		return this == PLAYER ? PlayerColors.locatorColor(player) : this.rgb;
	}

	@Override
	public Component getText() {
		return TalkingHuds.text("color." + name().toLowerCase(Locale.ROOT));
	}
}
