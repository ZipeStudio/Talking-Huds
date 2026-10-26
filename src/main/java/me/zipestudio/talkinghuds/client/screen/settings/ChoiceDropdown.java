package me.zipestudio.talkinghuds.client.screen.settings;

import me.zipestudio.talkinghuds.client.render.Draw;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public final class ChoiceDropdown {

	private static final int ROW_HEIGHT = 12;
	private static final int GRID_ROW_HEIGHT = 14;
	private static final int COLOR_BACKDROP = 0xF0100010;
	private static final int COLOR_BORDER = 0xFF808080;
	private static final int COLOR_HOVERED = 0x66FFFFFF;
	private static final int COLOR_CHOSEN = 0x33FFFFFF;
	private static final int COLOR_LABEL = 0xFFB0B0B0;
	private static final int COLOR_LABEL_CHOSEN = 0xFFFFFFFF;

	private boolean open;
	private AbstractWidget owner;
	private List<Object> values = List.of();
	private Object chosen;
	private Function<Object, Component> labels = value -> Component.empty();
	@Nullable
	private SettingOption.CellIcon<Object> icon;
	private Consumer<Object> onPick = value -> {
	};
	private int columns = 1;
	private int rowHeight = ROW_HEIGHT;
	private int left;
	private int top;
	private int right;
	private int bottom;

	public boolean isOpen() {
		return open;
	}

	public boolean isOpenFor(AbstractWidget widget) {
		return open && owner == widget;
	}

	public void close() {
		open = false;
	}

	@SuppressWarnings("unchecked")
	public <T> void open(AbstractWidget owner, int screenHeight, List<T> values, T chosen,
	                     Function<T, Component> labels, int columns, @Nullable SettingOption.CellIcon<T> icon,
	                     Consumer<T> onPick) {
		this.owner = owner;
		this.values = (List<Object>) values;
		this.chosen = chosen;
		this.labels = value -> labels.apply((T) value);
		this.icon = icon == null ? null : (graphics, value, x, y, argb) -> icon.draw(graphics, (T) value, x, y, argb);
		this.onPick = value -> onPick.accept((T) value);
		this.columns = Math.max(1, columns);
		this.rowHeight = this.columns > 1 ? GRID_ROW_HEIGHT : ROW_HEIGHT;
		this.open = true;

		int rows = (values.size() + this.columns - 1) / this.columns;
		int height = rows * rowHeight + 2;
		this.left = owner.getX();
		this.right = owner.getX() + owner.getWidth();
		int below = owner.getY() + owner.getHeight();
		if (below + height <= screenHeight) {
			this.top = below;
		} else if (owner.getY() - height >= 0) {
			this.top = owner.getY() - height;
		} else {
			this.top = Math.max(0, screenHeight - height);
		}
		this.bottom = top + height;
	}

	public boolean over(double mouseX, double mouseY) {
		return open && mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom;
	}

	public boolean mouseClicked(double mouseX, double mouseY, boolean leftButton) {
		if (!open) {
			return false;
		}
		boolean onList = over(mouseX, mouseY);
		open = false;
		if (leftButton && onList) {
			int index = indexAt(mouseX, mouseY);
			if (index >= 0) {
				chosen = values.get(index);
				onPick.accept(chosen);
			}
		}
		return true;
	}

	private int indexAt(double mouseX, double mouseY) {
		int row = (int) ((mouseY - top - 1) / rowHeight);
		int column = (int) ((mouseX - left) * columns / (right - left));
		int index = row * columns + Math.min(columns - 1, column);
		return row >= 0 && index >= 0 && index < values.size() ? index : -1;
	}

	public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY) {
		if (!open || values.isEmpty()) {
			return;
		}
		//? if >=1.21.6 {
		graphics.nextStratum();
		//?} else {
		/*graphics.pose().pushPose();
		graphics.pose().translate(0, 0, 400);
		*///?}
		graphics.fill(left, top, right, bottom, COLOR_BACKDROP);
		int hovered = over(mouseX, mouseY) ? indexAt(mouseX, mouseY) : -1;
		int cellWidth = (right - left) / columns;
		for (int i = 0; i < values.size(); i++) {
			int cellLeft = left + 1 + (i % columns) * cellWidth;
			int cellRight = (i % columns == columns - 1) ? right - 1 : cellLeft + cellWidth;
			int cellTop = top + 1 + (i / columns) * rowHeight;
			boolean picked = values.get(i).equals(chosen);
			if (i == hovered) {
				graphics.fill(cellLeft, cellTop, cellRight, cellTop + rowHeight, COLOR_HOVERED);
			} else if (picked) {
				graphics.fill(cellLeft, cellTop, cellRight, cellTop + rowHeight, COLOR_CHOSEN);
			}
			int color = picked || i == hovered ? COLOR_LABEL_CHOSEN : COLOR_LABEL;
			if (icon != null) {
				icon.draw(graphics, values.get(i), (cellLeft + cellRight) / 2, cellTop + rowHeight / 2, color);
			} else {
				int textY = cellTop + (rowHeight - font.lineHeight) / 2 + 1;
				Draw.text(graphics, font, labels.apply(values.get(i)), cellLeft + 4, textY, color);
			}
		}
		Draw.frame(graphics, left, top, right, bottom, 1, COLOR_BORDER);
		//? if <1.21.6 {
		/*graphics.pose().popPose();
		*///?}
	}
}
