package me.zipestudio.talkinghuds.client.render;

import java.util.UUID;

public final class PlayerColors {

	private static final float BRIGHTNESS = 0.9f;

	private PlayerColors() {
	}

	public static int locatorColor(UUID player) {
		return withBrightness(player.hashCode() & 0xFFFFFF, BRIGHTNESS);
	}

	static int withBrightness(int rgb, float brightness) {
		int r = rgb >> 16 & 0xFF;
		int g = rgb >> 8 & 0xFF;
		int b = rgb & 0xFF;
		int max = Math.max(Math.max(r, g), b);
		int min = Math.min(Math.min(r, g), b);
		float range = max - min;
		float saturation = max != 0 ? range / max : 0f;
		if (saturation == 0f) {
			int v = Math.round(brightness * 255f);
			return v << 16 | v << 8 | v;
		}
		float hue;
		float rc = (max - r) / range;
		float gc = (max - g) / range;
		float bc = (max - b) / range;
		if (r == max) {
			hue = bc - gc;
		} else if (g == max) {
			hue = 2f + rc - bc;
		} else {
			hue = 4f + gc - rc;
		}
		hue /= 6f;
		if (hue < 0f) {
			hue++;
		}

		float h = (hue - (float) Math.floor(hue)) * 6f;
		float f = h - (float) Math.floor(h);
		float p = brightness * (1f - saturation);
		float q = brightness * (1f - saturation * f);
		float t = brightness * (1f - saturation * (1f - f));
		float[] out = switch ((int) h) {
			case 0 -> new float[]{brightness, t, p};
			case 1 -> new float[]{q, brightness, p};
			case 2 -> new float[]{p, brightness, t};
			case 3 -> new float[]{p, q, brightness};
			case 4 -> new float[]{t, p, brightness};
			default -> new float[]{brightness, p, q};
		};
		return Math.round(out[0] * 255f) << 16 | Math.round(out[1] * 255f) << 8 | Math.round(out[2] * 255f);
	}
}
