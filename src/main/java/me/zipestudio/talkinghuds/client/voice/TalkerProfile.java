package me.zipestudio.talkinghuds.client.voice;

import java.util.UUID;

public final class TalkerProfile {

	private final UUID uuid;
	private final long firstTalkMs;
	private volatile double volume;
	private volatile long lastTalkMs;

	public TalkerProfile(UUID uuid, double volume, long now) {
		this.uuid = uuid;
		this.volume = volume;
		this.firstTalkMs = now;
		this.lastTalkMs = now;
	}

	public void talk(double volume, long now) {
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

	public long getLastTalkMs() {
		return this.lastTalkMs;
	}
}
