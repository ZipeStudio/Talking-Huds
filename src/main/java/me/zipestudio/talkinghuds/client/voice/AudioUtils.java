package me.zipestudio.talkinghuds.client.voice;

import me.zipestudio.talkinghuds.client.THManager;

import java.util.UUID;

public final class AudioUtils {

	public static final double SILENCE_DB = -127.0;

	private AudioUtils() {
	}

	public static double levelDb(short[] samples) {
		if (samples == null || samples.length == 0) {
			return SILENCE_DB;
		}
		double sum = 0.0;
		for (short sample : samples) {
			double normalized = sample / (double) Short.MAX_VALUE;
			sum += normalized * normalized;
		}
		double rms = Math.sqrt(sum / samples.length);
		return rms > 0.0 ? Math.max(20.0 * Math.log10(rms), SILENCE_DB) : SILENCE_DB;
	}

	public static double volumeFromDb(double levelDb) {
		return Math.max(0.0, 0.8 * (1.0 - Math.abs(levelDb) / 60.0));
	}

	public static void onSamples(UUID player, short[] samples) {
		onLevel(player, levelDb(samples));
	}

	public static void onLevel(UUID player, double levelDb) {
		THManager.talk(player, volumeFromDb(levelDb), System.currentTimeMillis());
	}
}
