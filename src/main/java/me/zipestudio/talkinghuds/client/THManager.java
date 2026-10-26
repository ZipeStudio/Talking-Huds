package me.zipestudio.talkinghuds.client;

import me.zipestudio.talkinghuds.client.voice.TalkerProfile;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class THManager {

	private static final Map<UUID, TalkerProfile> TALKERS = new ConcurrentHashMap<>();

	static final long FADE_IN_MS = 120L;
	static final double HOLD_FRACTION = 0.4;
	public static final float MIN_VISIBLE_ALPHA = 0.02f;
	static final long TALKING_WINDOW_MS = 300L;

	private THManager() {
	}

	public static void talk(UUID uuid, double volume, long now) {
		TALKERS.compute(uuid, (key, profile) -> {
			if (profile == null) {
				return new TalkerProfile(key, volume, now);
			}
			profile.talk(volume, now);
			return profile;
		});
	}

	@Nullable
	public static TalkerProfile getProfile(UUID uuid) {
		return TALKERS.get(uuid);
	}

	public static Collection<TalkerProfile> getProfiles() {
		return TALKERS.values();
	}

	public static boolean isEmpty() {
		return TALKERS.isEmpty();
	}

	public static float getDisplayAlpha(@Nullable TalkerProfile profile, long fadeMs, long now) {
		if (profile == null) {
			return 0f;
		}
		long sinceTalk = now - profile.getLastTalkMs();
		if (sinceTalk >= fadeMs) {
			return 0f;
		}
		long hold = (long) (fadeMs * HOLD_FRACTION);
		float fadeOut = sinceTalk <= hold
				? 1f
				: 1f - (float) (sinceTalk - hold) / (float) Math.max(1L, fadeMs - hold);
		float fadeIn = Math.min(1f, (float) (now - profile.getFirstTalkMs()) / (float) FADE_IN_MS);
		return Math.max(0f, Math.min(1f, Math.min(fadeIn, fadeOut)));
	}

	public static boolean isTalking(@Nullable TalkerProfile profile, long now) {
		return profile != null && now - profile.getLastTalkMs() < TALKING_WINDOW_MS;
	}

	public static void cleanup(long fadeMs, long now) {
		TALKERS.values().removeIf(profile -> now - profile.getLastTalkMs() >= fadeMs);
	}

	public static void forget(UUID uuid) {
		TALKERS.remove(uuid);
	}

	public static void clear() {
		TALKERS.clear();
	}
}
