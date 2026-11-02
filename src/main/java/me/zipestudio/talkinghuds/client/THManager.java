package me.zipestudio.talkinghuds.client;

import me.zipestudio.talkinghuds.client.voice.TalkerProfile;
import me.zipestudio.talkinghuds.config.LeafyConfig;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class THManager {

	private static final Map<UUID, TalkerProfile> TALKERS = new ConcurrentHashMap<>();

	public static final float MIN_VISIBLE_ALPHA = 0.02f;
	static final long TALKING_WINDOW_MS = 300L;

	private THManager() {
	}

	public static void talk(UUID uuid, double volume, long now) {
		long fadeMs = LeafyConfig.getInstance().getFadeMs();
		TALKERS.compute(uuid, (key, profile) -> {
			if (profile == null) {
				return new TalkerProfile(key, volume, now);
			}
			profile.talk(volume, now, fadeMs);
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

	public static float getPresence(@Nullable TalkerProfile profile, long fadeMs, long now) {
		if (profile == null) {
			return 0f;
		}
		long talkEnd = profile.getLastTalkMs() + TALKING_WINDOW_MS;
		if (fadeMs <= 0) {
			return now < talkEnd ? 1f : 0f;
		}
		float fadeIn = (float) (now - profile.getRiseStartMs()) / fadeMs;
		float fadeOut = (float) (talkEnd + fadeMs - now) / fadeMs;
		return Math.max(0f, Math.min(1f, Math.min(fadeIn, fadeOut)));
	}

	public static boolean isTalking(@Nullable TalkerProfile profile, long now) {
		return profile != null && now - profile.getLastTalkMs() < TALKING_WINDOW_MS;
	}

	public static void cleanup(long fadeMs, long now) {
		TALKERS.values().removeIf(profile -> now - profile.getLastTalkMs() >= TALKING_WINDOW_MS + fadeMs);
	}

	public static void forget(UUID uuid) {
		TALKERS.remove(uuid);
	}

	public static void clear() {
		TALKERS.clear();
	}
}
