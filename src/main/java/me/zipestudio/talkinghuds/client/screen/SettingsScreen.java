package me.zipestudio.talkinghuds.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import me.zipestudio.talkinghuds.client.THClient;
import me.zipestudio.talkinghuds.TalkingHuds;
//? if >=1.21.6 {
import me.zipestudio.talkinghuds.client.render.TalkingBarRenderer;
//?}
import me.zipestudio.talkinghuds.client.render.FaceRenderer;
import me.zipestudio.talkinghuds.client.render.TalkingOverlayRenderer;
import me.zipestudio.talkinghuds.client.render.VanillaSubtitles;
import me.zipestudio.talkinghuds.client.screen.settings.HudPreview;
import me.zipestudio.talkinghuds.client.screen.settings.SettingOptions;
import me.zipestudio.talkinghuds.client.screen.settings.SettingsLayout;
import me.zipestudio.talkinghuds.client.screen.settings.SettingsLayout.Column;
import me.zipestudio.talkinghuds.client.screen.settings.SettingsLayout.Slot;
import me.zipestudio.talkinghuds.client.screen.settings.SettingsPainter;
import me.zipestudio.talkinghuds.client.screen.settings.SettingsWidgets;
import me.zipestudio.talkinghuds.config.LeafyConfig;
import me.zipestudio.talkinghuds.config.option.DisplayMode;
import me.zipestudio.talkinghuds.config.option.HudAnchor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public class SettingsScreen extends Screen {

	private static boolean sidebarLeft;

	@Nullable
	private final Screen parent;
	private final LeafyConfig config;
	private final SettingsLayout layout = new SettingsLayout();
	private final SettingsWidgets widgets;
	private final HudPreview preview = new HudPreview();
	private SettingsPainter painter;
	private Button helpButton;
	private Button subtitlesButton;

	private int scroll;
	private int contentHeight;
	private boolean draggingHud;
	private boolean draggingScrollbar;
	private int scrollGrab;
	private int grabX;
	private int grabY;
	private boolean sideCheckPending;
	private boolean sideCheckDrawn;

	public SettingsScreen(@Nullable Screen parent) {
		super(TalkingHuds.text("settings.title"));
		this.parent = parent;
		this.config = LeafyConfig.getInstance();
		this.widgets = new SettingsWidgets(this.config, this::addRenderableWidget, this::onSettingChanged);
	}

	@Override
	protected void init() {
		if (painter == null) {
			painter = new SettingsPainter(this.font);
		}
		painter.clear();
		widgets.clear();
		widgets.setScreenHeight(this.height);

		layout.measure(this.width, this.height, sidebarLeft);
		layout.endAt(panelBottom());
		layout.frame(painter, this.title);
		buildHelp();

		Column column = layout.column(painter, scroll);
		buildGeneral(column);
		buildDisplay(column);
		buildLook(column);
		contentHeight = column.height() + SettingsLayout.GAP;
		buildFooter();
		buildSubtitlesButton();

		if (clampScroll()) {
			rebuildWidgets();
			return;
		}
		painter.clip(layout.contentTop, layout.contentBottom);
		refreshWidgets();
	}

	private void buildGeneral(Column column) {
		column.heading(TalkingHuds.text("settings.section.general"));
		widgets.toggle(column.full(), SettingOptions.ENABLE_MOD);
		widgets.toggle(column.full(), SettingOptions.PLASMO_VOICE);
		widgets.toggle(column.full(), SettingOptions.SIMPLE_VOICE_CHAT);
	}

	private void buildDisplay(Column column) {
		column.heading(TalkingHuds.text("settings.section.display"));
		widgets.choice(column.full(), SettingOptions.DISPLAY_MODE);
		widgets.choice(column.full(), SettingOptions.ANIMATION, this::showsFaces);
		widgets.choice(column.full(), SettingOptions.POSITION, this::movable, this::checkSideAfterFrame);
		widgets.slider(column.full(), SettingOptions.FACE_SIZE, this::showsFaces);
		widgets.slider(column.full(), SettingOptions.MAX_SHOWN, this::movable);
		widgets.slider(column.full(), SettingOptions.FADE);
	}

	private void buildLook(Column column) {
		column.heading(TalkingHuds.text("settings.section.look"));
		widgets.toggle(column.full(), SettingOptions.SHOW_NAMES, this::namesApply);
		widgets.toggle(column.full(), SettingOptions.SHOW_SELF);
		widgets.toggle(column.full(), SettingOptions.HIGHLIGHT, this::showsFaces);
		widgets.choice(column.full(), SettingOptions.COLOR, this::showsFaces);
		widgets.slider(column.full(), SettingOptions.BACKGROUND, this::backgroundApplies);
	}

	private void buildHelp() {
		helpButton = widgets.button(layout.helpButton(), "settings.help", () -> {
		});
		refreshHelp();
	}

	private void refreshHelp() {
		String key = movable() ? "settings.hint.move" : "settings.hint." + mode().name().toLowerCase(java.util.Locale.ROOT);
		SettingsWidgets.tip(helpButton, TalkingHuds.text(key), 0);
	}

	private void buildFooter() {
		Slot[] footer = layout.footer();
		widgets.button(footer[0], "settings.reset_position", this::resetPosition);
		widgets.button(footer[1], "settings.done", this::onClose);
	}

	private DisplayMode mode() {
		return config.getDisplayMode();
	}

	private boolean movable() {
		return mode().isMovable();
	}

	private boolean showsFaces() {
		return mode() != DisplayMode.SUBTITLES;
	}

	private boolean namesApply() {
		return mode().isFaces() || mode() == DisplayMode.BAR;
	}

	private boolean backgroundApplies() {
		return mode() == DisplayMode.LIST || (config.isShowNames() && namesApply());
	}

	private boolean clampScroll() {
		int clamped = Mth.clamp(scroll, 0, Math.max(0, contentHeight - layout.viewportHeight()));
		boolean moved = clamped != scroll;
		scroll = clamped;
		return moved;
	}

	private void onSettingChanged() {
		refreshHelp();
		refreshWidgets();
	}

	private static final int BAR_GAP = 4;

	private int panelBottom() {
		int bottom = this.height - SettingsLayout.EDGE;
		//? if >=1.21.6 {
		if (mode() == DisplayMode.BAR) {
			int barLeft = TalkingBarRenderer.barLeft(this.width);
			if (layout.panelLeft < barLeft + TalkingBarRenderer.BAR_WIDTH && layout.panelRight > barLeft) {
				int size = config.getFaceSize();
				int above = size / 2 + FaceRenderer.margin(config.getSpeakingAnimation(), size)
						+ (config.isShowNames() ? this.font.lineHeight + 2 : 2);
				int top = Math.min(TalkingBarRenderer.barTop(this.height), TalkingBarRenderer.dotCenterY(this.height) - above);
				return Math.min(bottom, top - BAR_GAP);
			}
		}
		//?}
		return bottom;
	}

	private void refreshWidgets() {
		widgets.refresh(layout.contentTop, layout.contentBottom, !draggingHud);
		subtitlesButton.visible = subtitlesOff();
	}

	private void buildSubtitlesButton() {
		int width = 140;
		subtitlesButton = addRenderableWidget(Button.builder(TalkingHuds.text("settings.subtitles_on"), button -> {
			VanillaSubtitles.setEnabledInOptions(this.minecraft, true);
			refreshWidgets();
		}).bounds(layout.previewCenterX() - width / 2, this.height / 2 + 12, width, 20).build());
	}

	private boolean subtitlesOff() {
		return mode() == DisplayMode.SUBTITLES && this.minecraft != null && !VanillaSubtitles.isEnabledInOptions(this.minecraft);
	}

	private void swapSide() {
		sidebarLeft = !sidebarLeft;
		rebuildWidgets();
	}

	private void resetPosition() {
		config.setHudAnchor(LeafyConfig.getNewInstance().getHudAnchor());
		config.setOffsetX(0);
		config.setOffsetY(0);
		rebuildWidgets();
	}

	//? if >=26.1 {
	/*@Override
	public void extractBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(graphics, mouseX, mouseY, partialTick);
		drawBackground(graphics);
	}

	@Override
	public void extractRenderState(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		boolean blocked = widgets.dropdown().isOpen();
		super.extractRenderState(graphics, blocked ? -1 : mouseX, blocked ? -1 : mouseY, partialTick);
		drawForeground(graphics, mouseX, mouseY);
	}
	*///?} elif >=1.20.2 {
	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.renderBackground(graphics, mouseX, mouseY, partialTick);
		drawBackground(graphics);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		boolean blocked = widgets.dropdown().isOpen();
		super.render(graphics, blocked ? -1 : mouseX, blocked ? -1 : mouseY, partialTick);
		drawForeground(graphics, mouseX, mouseY);
	}
	//?} else {
	/*@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		this.renderBackground(graphics);
		drawBackground(graphics);
		boolean blocked = widgets.dropdown().isOpen();
		super.render(graphics, blocked ? -1 : mouseX, blocked ? -1 : mouseY, partialTick);
		drawForeground(graphics, mouseX, mouseY);
	}
	*///?}

	private void drawBackground(GuiGraphics graphics) {
		preview.render(graphics, this.font, config, layout);
		sideCheckDrawn = sideCheckPending;
		preview.drawWarnings(graphics, this.font, config, layout, this.minecraft);
		if (!draggingHud) {
			painter.drawBackdrops(graphics);
		}
	}

	private void drawForeground(GuiGraphics graphics, int mouseX, int mouseY) {
		if (draggingHud) {
			return;
		}
		painter.drawForeground(graphics);
		boolean scrollbarActive = draggingScrollbar || (!widgets.dropdown().isOpen() && overScrollbar(mouseX, mouseY));
		painter.drawScrollbar(graphics, scrollbarX(), layout.contentTop, layout.contentBottom, scroll, contentHeight, scrollbarActive);
		if (movable() && hudUnderSidebar()) {
			preview.drawSelection(graphics);
		}
		widgets.dropdown().render(graphics, this.font, mouseX, mouseY);
	}

	private boolean hudUnderSidebar() {
		int[] bounds = preview.bounds();
		return bounds[2] > 0 && bounds[0] < layout.panelRight && bounds[0] + bounds[2] > layout.panelLeft
				&& bounds[1] < layout.panelBottom && bounds[1] + bounds[3] > layout.panelTop;
	}

	private boolean handleClick(double x, double y, int button) {
		if (widgets.dropdown().isOpen()) {
			return widgets.dropdown().mouseClicked(x, y, button == InputConstants.MOUSE_BUTTON_LEFT);
		}
		if (helpButton != null && helpButton.isMouseOver(x, y)) {
			return true;
		}
		if (button == InputConstants.MOUSE_BUTTON_LEFT && startScrollbarDrag(x, y)) {
			return true;
		}
		if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
			if (widgets.resetHovered(x, y)) {
				return true;
			}
			if (movable() && preview.overHud(x, y)) {
				resetPosition();
				return true;
			}
		}
		return false;
	}

	private boolean startDrag(double x, double y, int button) {
		if (button != InputConstants.MOUSE_BUTTON_LEFT || !movable() || !preview.overHud(x, y)) {
			return false;
		}
		int[] bounds = preview.bounds();
		setFocused(null);
		draggingHud = true;
		grabX = (int) Math.round(x) - bounds[0];
		grabY = (int) Math.round(y) - bounds[1];
		preview.setFrozen(true);
		refreshWidgets();
		return true;
	}

	private int scrollbarX() {
		return layout.panelRight - SettingsPainter.SCROLLBAR_WIDTH - 1;
	}

	private boolean overScrollbar(double x, double y) {
		return SettingsPainter.scrollThumb(layout.contentTop, layout.contentBottom, scroll, contentHeight) != null
				&& x >= layout.panelRight - SettingsLayout.FRAME_PAD && x < layout.panelRight && y >= layout.contentTop && y < layout.contentBottom;
	}

	private boolean startScrollbarDrag(double x, double y) {
		if (!overScrollbar(x, y)) {
			return false;
		}
		int[] thumb = SettingsPainter.scrollThumb(layout.contentTop, layout.contentBottom, scroll, contentHeight);
		boolean onThumb = y >= thumb[0] && y < thumb[0] + thumb[1];
		scrollGrab = onThumb ? (int) y - thumb[0] : thumb[1] / 2;
		draggingScrollbar = true;
		setFocused(null);
		dragScrollbar(y);
		return true;
	}

	private void dragScrollbar(double y) {
		int[] thumb = SettingsPainter.scrollThumb(layout.contentTop, layout.contentBottom, scroll, contentHeight);
		if (thumb == null) {
			return;
		}
		int track = layout.viewportHeight() - thumb[1];
		int thumbTop = Mth.clamp((int) y - scrollGrab - layout.contentTop, 0, track);
		int target = track <= 0 ? 0 : thumbTop * (contentHeight - layout.viewportHeight()) / track;
		if (target != scroll) {
			scroll = target;
			rebuildWidgets();
		}
	}

	private boolean handleRelease(int button) {
		if (button == InputConstants.MOUSE_BUTTON_LEFT && draggingScrollbar) {
			draggingScrollbar = false;
			return true;
		}
		if (button != InputConstants.MOUSE_BUTTON_LEFT || !draggingHud) {
			return false;
		}
		draggingHud = false;
		preview.setFrozen(false);
		if (!moveSidebarOutOfHud()) {
			refreshWidgets();
		}
		return true;
	}

	private boolean moveSidebarOutOfHud() {
		if (hudUnderSidebar() && fitsBesideOtherSide()) {
			swapSide();
			return true;
		}
		return false;
	}

	private void checkSideAfterFrame() {
		sideCheckPending = true;
		sideCheckDrawn = false;
	}

	@Override
	public void tick() {
		super.tick();
		if (sideCheckPending && sideCheckDrawn) {
			sideCheckPending = false;
			moveSidebarOutOfHud();
		}
		if (!isDragging() && !draggingHud && !draggingScrollbar && panelBottom() != layout.panelBottom) {
			rebuildWidgets();
		}
	}

	private boolean fitsBesideOtherSide() {
		int[] bounds = preview.bounds();
		int otherLeft = layout.sidebarLeft
				? this.width - SettingsLayout.EDGE - layout.panelWidth()
				: SettingsLayout.EDGE;
		return bounds[0] >= otherLeft + layout.panelWidth() || bounds[0] + bounds[2] <= otherLeft;
	}

	private boolean handleDrag(double x, double y) {
		if (draggingScrollbar) {
			dragScrollbar(y);
			return true;
		}
		if (!draggingHud) {
			return false;
		}
		moveHudTo((int) Math.round(x) - grabX, (int) Math.round(y) - grabY);
		return true;
	}

	private void moveHudTo(int x, int y) {
		int[] bounds = preview.bounds();
		int width = bounds[2];
		int height = bounds[3];
		int edge = TalkingOverlayRenderer.edge(config);
		int left = HudAnchor.clamp(x, edge, this.width - width - edge);
		int top = HudAnchor.clamp(y, edge, this.height - height - edge);
		HudAnchor anchor = HudAnchor.fromPoint(left + width / 2, top + height / 2, this.width, this.height);
		config.setHudAnchor(anchor);
		config.setOffsetX(left - anchor.originX(this.width, width, edge));
		config.setOffsetY(top - anchor.originY(this.height, height, edge));
		widgets.syncAll();
	}

	private boolean handleScroll(double x, double y, double amount) {
		if (amount == 0) {
			return false;
		}
		if (widgets.dropdown().isOpen()) {
			widgets.dropdown().close();
			return true;
		}
		if (layout.overSidebar(x, y)) {
			if (contentHeight > layout.viewportHeight()) {
				scroll -= (int) Math.signum(amount) * SettingsLayout.SCROLL_STEP;
				rebuildWidgets();
			} else {
				widgets.nudgeHovered(x, y, amount);
			}
			return true;
		}
		if (!showsFaces()) {
			return false;
		}
		SettingOptions.FACE_SIZE.set(config, config.getFaceSize() + Math.signum(amount));
		widgets.syncAll();
		return true;
	}

	private boolean handleKey(int key) {
		if (key == InputConstants.KEY_ESCAPE && widgets.dropdown().isOpen()) {
			widgets.dropdown().close();
			return true;
		}
		int dx = key == InputConstants.KEY_LEFT ? -1 : key == InputConstants.KEY_RIGHT ? 1 : 0;
		int dy = key == InputConstants.KEY_UP ? -1 : key == InputConstants.KEY_DOWN ? 1 : 0;
		if (dx == 0 && dy == 0) {
			return false;
		}
		int[] bounds = preview.bounds();
		if (getFocused() != null || !movable() || bounds[2] <= 0) {
			return false;
		}
		moveHudTo(bounds[0] + dx, bounds[1] + dy);
		return true;
	}

	private void deselectUnlessHit(boolean hitWidget) {
		if (!hitWidget) {
			setFocused(null);
		}
	}

	//? if >=1.21.9 {
	@Override
	public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
		if (handleClick(event.x(), event.y(), event.button())) {
			return true;
		}
		boolean hitWidget = super.mouseClicked(event, doubleClick);
		deselectUnlessHit(hitWidget);
		return hitWidget || startDrag(event.x(), event.y(), event.button());
	}

	@Override
	public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event) {
		return handleRelease(event.button()) || super.mouseReleased(event);
	}

	@Override
	public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event, double dragX, double dragY) {
		return handleDrag(event.x(), event.y()) || super.mouseDragged(event, dragX, dragY);
	}

	@Override
	public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
		return handleKey(event.key()) || super.keyPressed(event);
	}
	//?} else {
	/*@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (handleClick(mouseX, mouseY, button)) {
			return true;
		}
		boolean hitWidget = super.mouseClicked(mouseX, mouseY, button);
		deselectUnlessHit(hitWidget);
		return hitWidget || startDrag(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		return handleRelease(button) || super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		return handleDrag(mouseX, mouseY) || super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}

	@Override
	public boolean keyPressed(int key, int scanCode, int modifiers) {
		return handleKey(key) || super.keyPressed(key, scanCode, modifiers);
	}
	*///?}

	//? if >=1.20.2 {
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		return handleScroll(mouseX, mouseY, scrollY) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}
	//?} else {
	/*@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		return handleScroll(mouseX, mouseY, amount) || super.mouseScrolled(mouseX, mouseY, amount);
	}
	*///?}

	@Override
	public void onClose() {
		config.saveAsync();
		THClient.setScreen(this.minecraft, this.parent);
	}
}
