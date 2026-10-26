package me.zipestudio.talkinghuds.client.screen.settings;

import net.minecraft.network.chat.Component;

public final class SettingsLayout {

	public static final int MIN_SIDEBAR_WIDTH = 120;
	public static final int MAX_SIDEBAR_WIDTH = 180;
	public static final int ROW_HEIGHT = 18;
	public static final int ROW_GAP = 2;
	public static final int GAP = 4;
	public static final int HEADING_HEIGHT = 11;
	public static final int GROUP_GAP = 7;
	public static final int FRAME_PAD = 6;
	public static final int EDGE = 6;
	public static final int TITLE_HEIGHT = 20;
	public static final int SCROLL_STEP = ROW_HEIGHT + ROW_GAP;

	public boolean sidebarLeft;
	public int sidebarWidth = MAX_SIDEBAR_WIDTH;
	public int panelLeft;
	public int panelTop;
	public int panelRight;
	public int panelBottom;
	public int contentLeft;
	public int contentTop;
	public int contentBottom;
	public int footerY;
	public int previewLeft;
	public int previewRight;
	public int screenHeight;

	public void measure(int screenWidth, int screenHeight, boolean sidebarLeft) {
		this.sidebarLeft = sidebarLeft;
		this.screenHeight = screenHeight;
		this.sidebarWidth = Math.max(MIN_SIDEBAR_WIDTH, Math.min(MAX_SIDEBAR_WIDTH, screenWidth / 3));
		this.panelLeft = sidebarLeft ? EDGE : screenWidth - EDGE - panelWidth();
		this.panelRight = panelLeft + panelWidth();
		this.panelTop = EDGE;
		this.contentLeft = panelLeft + FRAME_PAD;
		this.contentTop = panelTop + TITLE_HEIGHT + 1;

		this.previewLeft = sidebarLeft ? panelRight : 0;
		this.previewRight = sidebarLeft ? screenWidth : panelLeft;
		endAt(screenHeight - EDGE);
	}

	public void endAt(int bottom) {
		this.panelBottom = bottom;
		this.footerY = panelBottom - FRAME_PAD - ROW_HEIGHT;
		this.contentBottom = footerY - FRAME_PAD - 1;
	}

	public void frame(SettingsPainter painter, Component title) {
		painter.panel(panelLeft, panelTop, panelRight, panelBottom);
		painter.title(title, (panelLeft + panelRight) / 2, panelTop + (TITLE_HEIGHT - 8) / 2);
		painter.rule(contentLeft, contentTop - 1, sidebarWidth);
		painter.rule(contentLeft, contentBottom + FRAME_PAD / 2, sidebarWidth);
	}

	public Slot helpButton() {
		int size = 14;
		return new Slot(panelRight - FRAME_PAD - size, panelTop + (TITLE_HEIGHT - size) / 2 + 1, size, size);
	}

	public int panelWidth() {
		return sidebarWidth + 2 * FRAME_PAD;
	}

	public int viewportHeight() {
		return contentBottom - contentTop;
	}

	public int previewCenterX() {
		return (previewLeft + previewRight) / 2;
	}

	public boolean overSidebar(double x, double y) {
		return x >= panelLeft && x < panelRight && y >= panelTop && y < panelBottom;
	}

	public Column column(SettingsPainter painter, int scroll) {
		return new Column(painter, contentLeft, sidebarWidth, contentTop + GAP - scroll);
	}

	public Slot[] footer() {
		int half = (sidebarWidth - GAP) / 2;
		return new Slot[]{
				new Slot(contentLeft, footerY, half, ROW_HEIGHT),
				new Slot(contentLeft + half + GAP, footerY, sidebarWidth - GAP - half, ROW_HEIGHT)
		};
	}

	public record Slot(int x, int y, int width, int height) {
	}

	public static final class Column {

		private final SettingsPainter painter;
		private final int x;
		private final int width;
		private final int top;
		private int cursorY;
		private boolean afterRow;

		private Column(SettingsPainter painter, int x, int width, int top) {
			this.painter = painter;
			this.x = x;
			this.width = width;
			this.top = top;
			this.cursorY = top;
		}

		public void heading(Component label) {
			if (afterRow) {
				cursorY += GROUP_GAP - ROW_GAP;
			}
			painter.heading(label, x, cursorY);
			cursorY += HEADING_HEIGHT;
			afterRow = false;
		}

		public Slot full() {
			Slot slot = new Slot(x, cursorY, width, ROW_HEIGHT);
			cursorY += ROW_HEIGHT + ROW_GAP;
			afterRow = true;
			return slot;
		}

		public int height() {
			return cursorY - top;
		}
	}
}
