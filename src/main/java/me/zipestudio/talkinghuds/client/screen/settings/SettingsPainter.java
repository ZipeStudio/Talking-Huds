package me.zipestudio.talkinghuds.client.screen.settings;

import me.zipestudio.talkinghuds.client.render.Draw;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class SettingsPainter {

	private static final int COLOR_BACKDROP = 0xC0101010;
	private static final int COLOR_BORDER = 0xFF555555;
	private static final int COLOR_HEADING = 0xFFB0B0B0;
	private static final int COLOR_TITLE = 0xFFFFFFFF;
	private static final int COLOR_SCROLLBAR = 0xFF808080;
	private static final int COLOR_SCROLLBAR_ACTIVE = 0xFFC0C0C0;
	private static final int COLOR_SCROLL_TRACK = 0x40000000;
	public static final int SCROLLBAR_WIDTH = 3;

	private final Font font;
	private final List<int[]> panels = new ArrayList<>();
	private final List<int[]> rules = new ArrayList<>();
	private final List<Heading> headings = new ArrayList<>();
	private Heading title;
	private int clipTop;
	private int clipBottom = Integer.MAX_VALUE;

	public SettingsPainter(Font font) {
		this.font = font;
	}

	public void clear() {
		panels.clear();
		rules.clear();
		headings.clear();
		title = null;
	}

	public void panel(int left, int top, int right, int bottom) {
		panels.add(new int[]{left, top, right, bottom});
	}

	public void rule(int x, int y, int width) {
		rules.add(new int[]{x, y, width});
	}

	public void heading(Component label, int x, int y) {
		headings.add(new Heading(label, x, y));
	}

	public void title(Component label, int centerX, int y) {
		title = new Heading(label, centerX - font.width(label) / 2, y);
	}

	public void clip(int top, int bottom) {
		this.clipTop = top;
		this.clipBottom = bottom;
	}

	public void drawBackdrops(GuiGraphics graphics) {
		for (int[] panel : panels) {
			graphics.fill(panel[0], panel[1], panel[2], panel[3], COLOR_BACKDROP);
		}
	}

	public void drawForeground(GuiGraphics graphics) {
		for (int[] panel : panels) {
			Draw.frame(graphics, panel[0], panel[1], panel[2], panel[3], 1, COLOR_BORDER);
		}
		for (int[] rule : rules) {
			graphics.fill(rule[0], rule[1], rule[0] + rule[2], rule[1] + 1, COLOR_BORDER);
		}
		for (Heading heading : headings) {
			if (heading.y() >= clipTop && heading.y() + font.lineHeight <= clipBottom) {
				Draw.text(graphics, font, heading.label(), heading.x(), heading.y(), COLOR_HEADING);
			}
		}
		if (title != null) {
			Draw.text(graphics, font, title.label(), title.x(), title.y(), COLOR_TITLE);
		}
	}

	public static int @Nullable [] scrollThumb(int top, int bottom, int scroll, int contentHeight) {
		int view = bottom - top;
		if (contentHeight <= view) {
			return null;
		}
		int height = Math.max(10, view * view / contentHeight);
		return new int[]{top + (view - height) * scroll / (contentHeight - view), height};
	}

	public void drawScrollbar(GuiGraphics graphics, int x, int top, int bottom, int scroll, int contentHeight, boolean active) {
		int[] thumb = scrollThumb(top, bottom, scroll, contentHeight);
		if (thumb == null) {
			return;
		}
		graphics.fill(x, top, x + SCROLLBAR_WIDTH, bottom, COLOR_SCROLL_TRACK);
		graphics.fill(x, thumb[0], x + SCROLLBAR_WIDTH, thumb[0] + thumb[1], active ? COLOR_SCROLLBAR_ACTIVE : COLOR_SCROLLBAR);
	}

	private record Heading(Component label, int x, int y) {
	}
}
