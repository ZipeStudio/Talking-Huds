package me.zipestudio.talkinghuds.client.render;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class TalkerAnimator {

	private static final float VOLUME_ATTACK = 0.05f;
	private static final float VOLUME_RELEASE = 0.20f;
	private static final float MAX_DT = 0.1f;

	private final Map<UUID, State> states = new HashMap<>();
	private long lastFrameNanos = -1;
	private float dt;

	private static final class State {
		private float volume;
		private boolean seen;
	}

	public void beginFrame() {
		beginFrame(false);
	}

	public void beginFrame(boolean paused) {
		long now = System.nanoTime();
		this.dt = paused || this.lastFrameNanos < 0 ? 0f : Math.min(MAX_DT, (now - this.lastFrameNanos) / 1.0e9f);
		this.lastFrameNanos = now;
		this.states.values().forEach(state -> state.seen = false);
	}

	public void endFrame() {
		this.states.values().removeIf(state -> !state.seen);
	}

	public float volume(Talker talker) {
		State state = this.states.computeIfAbsent(talker.uuid(), uuid -> new State());
		state.seen = true;
		float target = talker.talking() ? (float) talker.volume() : 0f;
		state.volume = Anim.approach(state.volume, target, this.dt, target > state.volume ? VOLUME_ATTACK : VOLUME_RELEASE);
		return state.volume;
	}

	public void clear() {
		this.states.clear();
		this.lastFrameNanos = -1;
	}
}
