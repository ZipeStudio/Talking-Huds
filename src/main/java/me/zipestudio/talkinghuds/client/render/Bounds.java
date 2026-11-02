package me.zipestudio.talkinghuds.client.render;

public record Bounds(int x, int y, int width, int height) {

	public static final Bounds NONE = new Bounds(0, 0, 0, 0);

	public boolean isEmpty() {
		return this.width <= 0 || this.height <= 0;
	}

	public int right() {
		return this.x + this.width;
	}

	public int bottom() {
		return this.y + this.height;
	}

	public boolean contains(double px, double py) {
		return !isEmpty() && px >= this.x && px <= right() && py >= this.y && py <= bottom();
	}
}
