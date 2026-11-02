package me.zipestudio.talkinghuds.client.render;

import me.zipestudio.talkinghuds.TalkingHuds;
import me.zipestudio.talkinghuds.config.LeafyConfig;
import me.zipestudio.talkinghuds.config.option.SpeakingAnimation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public final class FaceRenderer {

	public static final float FULL_VOLUME = 0.45f;
	public static final float SILENT = 0.02f;
	private static final float PULSE_GROWTH = 0.5f;
	private static final long RING_PERIOD_MS = 900L;

	private static final Identifier SPEAKER = TalkingHuds.id("speaker");
	private static final Identifier[] SPEAKER_WAVES = {
			TalkingHuds.id("speaker_waves_1"),
			TalkingHuds.id("speaker_waves_2"),
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
		draw(graphics, config, face, centerX, centerY, size, volume, alpha, player, rgb, false);
	}

	public static void draw(GuiGraphics graphics, LeafyConfig config, FaceTexture face, int centerX, int centerY,
	                        int size, float volume, float alpha, UUID player, int rgb, boolean steadyHighlight) {
		if (size <= 0 || alpha <= 0.01f) {
			return;
		}
		SpeakingAnimation animation = config.getSpeakingAnimation();
		int seed = player.hashCode() & 0xFFFF;
		float strength = Anim.clamp01(volume / FULL_VOLUME);
		double phase = Anim.millis() + seed * 397L;

		int dy = animation == SpeakingAnimation.BOUNCE
				? -Math.round(size * 0.25f * strength * (float) Math.abs(Math.sin(phase / 130.0)))
				: 0;
		int x = centerX - size / 2;
		int y = centerY - size / 2 + dy;

		if (animation == SpeakingAnimation.RINGS) {
			drawRings(graphics, x, y, size, strength, alpha, rgb, phase, margin(animation, size));
		}
		float scale = animation == SpeakingAnimation.PULSE ? 1f + PULSE_GROWTH * volume : 1f;
		boolean scaled = scale > 1.001f;
		if (scaled) {
			Draw.pushScale(graphics, centerX, centerY, scale);
			x = -size / 2;
			y = -size / 2;
		}
		face.draw(graphics, x, y, size, alpha);
		if (config.isHighlight()) {
			float glow = steadyHighlight ? 1f : 0.3f + 0.7f * Math.min(1f, strength * 3f);
			Draw.frame(graphics, x - 1, y - 1, x + size + 1, y + size + 1, 1, Draw.argb(glow * alpha, rgb));
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
		if (strength <= SILENT) {
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
		boolean talking = strength > SILENT;
		int body = talking ? Draw.argb(alpha, 0xFFFFFF) : Draw.argb(0.55f * alpha, 0xA0A0A0);
		int shadow = Draw.argb(0.6f * alpha, 0x000000);
		Identifier waves = talking ? SPEAKER_WAVES[strength < FULL_VOLUME ? 0 : 1] : null;
		Draw.sprite(graphics, SPEAKER, x + 1, y + 1, SPEAKER_WIDTH, SPEAKER_HEIGHT, shadow);
		if (waves != null) {
			Draw.sprite(graphics, waves, x + 1, y + 1, SPEAKER_WIDTH, SPEAKER_HEIGHT, shadow);
		}
		Draw.sprite(graphics, SPEAKER, x, y, SPEAKER_WIDTH, SPEAKER_HEIGHT, body);
		if (waves != null) {
			Draw.sprite(graphics, waves, x, y, SPEAKER_WIDTH, SPEAKER_HEIGHT, Draw.argb(alpha, rgb));
		}
	}
}