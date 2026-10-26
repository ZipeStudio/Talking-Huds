package me.zipestudio.talkinghuds.client.render;

public final class Anim {

	private static long clockOverride = -1;

	private Anim() {
	}

	public static long millis() {
		return clockOverride >= 0 ? clockOverride : System.currentTimeMillis();
	}

	public static void useClock(long millis) {
		clockOverride = millis;
	}

	public static void realClock() {
		clockOverride = -1;
	}

	public static float clamp01(float value) {
		return Math.max(0f, Math.min(1f, value));
	}

	public static float approach(float current, float target, float dtSeconds, float tau) {
		if (tau <= 0f) {
			return target;
		}
		float k = 1f - (float) Math.exp(-dtSeconds / tau);
		return current + (target - current) * k;
	}
}
