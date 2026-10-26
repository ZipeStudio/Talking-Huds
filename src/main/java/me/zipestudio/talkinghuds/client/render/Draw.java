package me.zipestudio.talkinghuds.client.render;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public final class Draw {

	private static final int MIN_TEXT_ALPHA = 4;

	private Draw() {
	}

	public static void text(GuiGraphics graphics, Font font, Component text, int x, int y, int argb) {
		if ((argb >>> 24) < MIN_TEXT_ALPHA) {
			return;
		}
		//? if >=26.1 {
		/*graphics.text(font, text, x, y, argb, true);
		*///?} else {
		graphics.drawString(font, text, x, y, argb, true);
		//?}
	}

	public static void text(GuiGraphics graphics, Font font, String text, int x, int y, int argb) {
		if ((argb >>> 24) < MIN_TEXT_ALPHA) {
			return;
		}
		//? if >=26.1 {
		/*graphics.text(font, text, x, y, argb, true);
		*///?} else {
		graphics.drawString(font, text, x, y, argb, true);
		//?}
	}

	public static void text(GuiGraphics graphics, Font font, FormattedCharSequence text, int x, int y, int argb) {
		if ((argb >>> 24) < MIN_TEXT_ALPHA) {
			return;
		}
		//? if >=26.1 {
		/*graphics.text(font, text, x, y, argb, true);
		*///?} else {
		graphics.drawString(font, text, x, y, argb, true);
		//?}
	}

	public static int wrappedCenteredText(GuiGraphics graphics, Font font, Component text, int centerX, int y, int maxWidth, int argb) {
		int line = y;
		for (FormattedCharSequence part : font.split(text, Math.max(20, maxWidth))) {
			text(graphics, font, part, centerX - font.width(part) / 2, line, argb);
			line += font.lineHeight + 1;
		}
		return line - y;
	}

	public static void pushScale(GuiGraphics graphics, float centerX, float centerY, float scale) {
		//? if >=1.21.6 {
		graphics.pose().pushMatrix();
		graphics.pose().translate(centerX, centerY);
		graphics.pose().scale(scale, scale);
		//?} else {
		/*graphics.pose().pushPose();
		graphics.pose().translate(centerX, centerY, 0);
		graphics.pose().scale(scale, scale, 1);
		*///?}
	}

	public static void popScale(GuiGraphics graphics) {
		//? if >=1.21.6 {
		graphics.pose().popMatrix();
		//?} else {
		/*graphics.pose().popPose();
		*///?}
	}

	public static int argb(float alpha, int rgb) {
		int a = Math.round(Math.max(0f, Math.min(1f, alpha)) * 255f);
		return a << 24 | (rgb & 0xFFFFFF);
	}

	public static int fade(int argb, float alpha) {
		return argb((argb >>> 24) / 255f * alpha, argb);
	}

	public static void frame(GuiGraphics graphics, int left, int top, int right, int bottom, int thickness, int argb) {
		int t = Math.max(1, thickness);
		graphics.fill(left, top, right, top + t, argb);
		graphics.fill(left, bottom - t, right, bottom, argb);
		graphics.fill(left, top + t, left + t, bottom - t, argb);
		graphics.fill(right - t, top + t, right, bottom - t, argb);
	}
}
