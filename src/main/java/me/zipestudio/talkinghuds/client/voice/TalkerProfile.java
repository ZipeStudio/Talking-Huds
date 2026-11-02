package me.zipestudio.talkinghuds.client.voice;

import me.zipestudio.talkinghuds.client.THManager;

import java.util.UUID;

public final class TalkerProfile {

	private final UUID uuid;
	private final long firstTalkMs;
	private volatile double volume;
	private volatile long lastTalkMs;
	private volatile long riseStartMs;

	public TalkerProfile(UUID uuid, double volume, long now) {
		this.uuid = uuid;
		this.volume = volume;
		this.firstTalkMs = now;
		this.lastTalkMs = now;
		this.riseStartMs = now;
	}

	public void talk(double volume, long now, long fadeMs) {
		if (!THManager.isTalking(this, now)) {
			this.riseStartMs = now - (long) (THManager.getPresence(this, fadeMs, now) * fadeMs);
		}
		this.volume = volume;
		this.lastTalkMs = now;
	}

	public UUID getUuid() {
		return this.uuid;
	}

	public double getVolume() {
		return this.volume;
	}

	public long getFirstTalkMs() {
		return this.firstTalkMs;
	}

	public long getRiseStartMs() {
		return this.riseStartMs;
	}

	public long getLastTalkMs() {
		return this.lastTalkMs;
	}
}
