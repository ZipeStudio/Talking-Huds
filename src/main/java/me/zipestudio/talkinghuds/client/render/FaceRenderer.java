package me.zipestudio.talkinghuds.client.render;

import me.zipestudio.talkinghuds.config.LeafyConfig;
import me.zipestudio.talkinghuds.config.option.SpeakingAnimation;
import net.minecraft.client.gui.GuiGraphics;

import java.util.UUID;

public final class FaceRenderer {

	private static final float FULL_VOLUME = 0.45f;
	private static final float PULSE_GROWTH = 0.5f;
	private static final long RING_PERIOD_MS = 900L;

	private static final String[] SPEAKER = {
			"...B.....",
			"..BB...2.",
			"BBBB.1..2",
			"BBBB.1..2",
			"BBBB.1..2",
			"..BB...2.",
			"...B.....",
	};
	public static final int SPEAKER_WIDTH = 9;
	public static final int SPEAKER_HEIGHT = 7;

	private FaceRenderer() {
	}

	public static int margin(SpeakingAnimation animation, int size) {
		return switch (animation) {
			case PULSE -> Math.round(size * PULSE_GROWTH * 0.8f / 2f) + 1;
			case RINGS -> Math.max(4, size / 3);
			case BOUNCE -> Math.round(size * 0.25f) + 1;
			default -> 1;
		};
	}

	public static void draw(GuiGraphics graphics, LeafyConfig config, FaceTexture face, int centerX, int centerY,
	                        int size, float volume, float alpha, UUID player) {
		draw(graphics, config, face, centerX, centerY, size, volume, alpha, player, config.getAccentColor().colorFor(player));
	}

	public static void draw(GuiGraphics graphics, LeafyConfig config, FaceTexture face, int centerX, int centerY,
	                        int size, float volume, float alpha, UUID player, int rgb) {
		if (size <= 0 || alpha <= 0.01f) {
			return;
		}
		SpeakingAnimation animation = config.getSpeakingAnimation();
		int seed = player.hashCode() & 0xFFFF;
		float strength = Anim.clamp01(volume / FULL_VOLUME);
		long time = Anim.millis();
		double phase = time + seed * 397L;

		int drawSize = size;
		int dy = animation == SpeakingAnimation.BOUNCE
				? -Math.round(size * 0.25f * strength * (float) Math.abs(Math.sin(phase / 130.0)))
				: 0;
		int x = centerX - drawSize / 2;
		int y = centerY - drawSize / 2 + dy;

		if (animation == SpeakingAnimation.RINGS) {
			drawRings(graphics, x, y, drawSize, strength, alpha, rgb, phase, margin(animation, size));
		}
		float scale = animation == SpeakingAnimation.PULSE ? 1f + PULSE_GROWTH * volume : 1f;
		boolean scaled = scale > 1.001f;
		if (scaled) {
			Draw.pushScale(graphics, centerX, centerY, scale);
			x = -drawSize / 2;
			y = -drawSize / 2;
		}
		if (config.isHighlight()) {
			graphics.fill(x - 2, y - 2, x + drawSize + 2, y + drawSize + 2, Draw.argb((0.12f + 0.5f * strength) * alpha, rgb));
		}
		graphics.fill(x - 1, y - 1, x + drawSize + 1, y + drawSize + 1, Draw.argb(0.5f * alpha, 0x000000));
		face.draw(graphics, x, y, drawSize, alpha);
		if (config.isHighlight() && strength > 0.02f) {
			Draw.frame(graphics, x - 2, y - 2, x + drawSize + 2, y + drawSize + 2, 1, Draw.argb(Math.min(1f, strength * 3f) * alpha, rgb));
		}
		if (scaled) {
			Draw.popScale(graphics);
		}
	}

	public static int backgroundArgb(LeafyConfig config, float alpha) {
		return Draw.argb(config.getBackgroundOpacity() / 100f * alpha, 0x000000);
	}

	private static void drawRings(GuiGraphics graphics, int x, int y, int size, float strength, float alpha, int rgb,
	                              double phase, int room) {
		if (strength <= 0.02f) {
			return;
		}
		for (int i = 0; i < 2; i++) {
			float p = (float) (((phase / RING_PERIOD_MS) + i * 0.5) % 1.0);
			int e = 2 + Math.round(p * (room - 1));
			float fade = (1f - p) * Math.min(1f, strength * 2.5f) * alpha;
			Draw.frame(graphics, x - e, y - e, x + size + e, y + size + e, 1, Draw.argb(fade, rgb));
		}
	}



	public static void drawSpeaker(GuiGraphics graphics, int x, int y, float strength, float alpha, int rgb) {
		boolean talking = strength > 0.02f;
		int body = talking ? Draw.argb(alpha, 0xFFFFFF) : Draw.argb(0.55f * alpha, 0xA0A0A0);
		int wave = Draw.argb(alpha, rgb);
		int waves = !talking ? 0 : strength < 0.45f ? 1 : 2;
		int shadow = Draw.argb(0.6f * alpha, 0x000000);
		for (int pass = 0; pass < 2; pass++) {
			for (int row = 0; row < SPEAKER.length; row++) {
				for (int col = 0; col < SPEAKER_WIDTH; col++) {
					char c = SPEAKER[row].charAt(col);
					if (c == 'B' || (c == '1' && waves >= 1) || (c == '2' && waves >= 2)) {
						int px = x + col + (pass == 0 ? 1 : 0);
						int py = y + row + (pass == 0 ? 1 : 0);
						graphics.fill(px, py, px + 1, py + 1, pass == 0 ? shadow : (c == 'B' ? body : wave));
					}
				}
			}
		}
	}
}
