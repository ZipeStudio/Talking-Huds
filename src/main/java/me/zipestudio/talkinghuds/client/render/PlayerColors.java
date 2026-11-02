package me.zipestudio.talkinghuds.client.render;

import java.awt.Color;
import java.util.UUID;

public final class PlayerColors {

	private static final float BRIGHTNESS = 0.9f;

	private PlayerColors() {
	}

	public static int locatorColor(UUID player) {
		int rgb = player.hashCode();
		float[] hsb = Color.RGBtoHSB(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF, null);
		return Color.HSBtoRGB(hsb[0], hsb[1], BRIGHTNESS) & 0xFFFFFF;
	}
}
