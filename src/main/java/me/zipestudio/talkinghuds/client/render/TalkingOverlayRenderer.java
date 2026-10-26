package me.zipestudio.talkinghuds.client.render;

import me.zipestudio.talkinghuds.client.THClient;
import me.zipestudio.talkinghuds.TalkingHuds;
import me.zipestudio.talkinghuds.config.LeafyConfig;
import me.zipestudio.talkinghuds.config.option.DisplayMode;
import me.zipestudio.talkinghuds.config.option.HudAnchor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class TalkingOverlayRenderer {

	private static final TalkerAnimator HUD_ANIMATOR = new TalkerAnimator();
	private static final int NAME_COLOR = 0xFFFFFF;
	private static final int QUIET_NAME_COLOR = 0xAAAAAA;
	private static final int MAX_NAME_WIDTH = 90;

	private TalkingOverlayRenderer() {
	}

	public static void render(GuiGraphics graphics) {
		Minecraft mc = Minecraft.getInstance();
		LeafyConfig config = LeafyConfig.getInstance();
		if (!config.isEnableMod() || !config.getDisplayMode().isMovable() || THClient.hideTalkers(mc)) {
			return;
		}
		List<Talker> talkers = Talker.collectVisible(mc, config);
		if (talkers.isEmpty()) {
			HUD_ANIMATOR.clear();
			return;
		}
		HUD_ANIMATOR.beginFrame();
		renderMode(graphics, mc.font, config, talkers, HUD_ANIMATOR);
		HUD_ANIMATOR.endFrame();
	}

	public static int edge(LeafyConfig config) {
		return config.getDisplayMode() == DisplayMode.LIST
				? 0
				: FaceRenderer.margin(config.getSpeakingAnimation(), config.getFaceSize()) + 2;
	}

	public static int[] renderMode(GuiGraphics graphics, Font font, LeafyConfig config, List<Talker> talkers, TalkerAnimator animator) {
		int shown = Math.min(talkers.size(), config.getMaxTalkers());
		int overflow = talkers.size() - shown;
		List<Talker> visible = talkers.subList(0, shown);
		return config.getDisplayMode() == DisplayMode.LIST
				? renderList(graphics, font, config, visible, overflow, animator)
				: renderHeads(graphics, font, config, visible, talkers.subList(shown, talkers.size()), animator);
	}

	private static int[] renderHeads(GuiGraphics graphics, Font font, LeafyConfig config, List<Talker> talkers,
	                                 List<Talker> hidden, TalkerAnimator animator) {
		HudAnchor anchor = config.getHudAnchor();
		boolean vertical = config.getDisplayMode() == DisplayMode.HEADS_COLUMN;
		boolean names = config.isShowNames();
		int size = config.getFaceSize();
		int gap = Math.max(5, 3 + 2 * FaceRenderer.margin(config.getSpeakingAnimation(), size));
		int labelGap = 3 + FaceRenderer.margin(config.getSpeakingAnimation(), size);
		int count = talkers.size() + (hidden.isEmpty() ? 0 : 1);

		int[] spans = new int[count];
		int maxName = 0;
		for (int i = 0; i < count; i++) {
			if (i >= talkers.size()) {
				spans[i] = size;
				continue;
			}
			int nameWidth = names ? font.width(ellipsize(font, talkers.get(i).name(), MAX_NAME_WIDTH)) : 0;
			maxName = Math.max(maxName, nameWidth);
			spans[i] = vertical ? size : Math.max(size, nameWidth + 2);
		}
		int along = 0;
		for (int i = 0; i < count; i++) {
			along += spans[i] + (i > 0 ? gap : 0);
		}
		int across = vertical
				? size + (names ? gap + maxName : 0)
				: size + (names ? labelGap + font.lineHeight : 0);
		int width = vertical ? across : along;
		int height = vertical ? along : across;

		int originX = anchor.placeX(graphics.guiWidth(), width, config.getOffsetX(), edge(config));
		int originY = anchor.placeY(graphics.guiHeight(), height, config.getOffsetY(), edge(config));
		boolean labelsLeft = anchor.isRight();

		int cursor = 0;
		for (int i = 0; i < count; i++) {
			int span = spans[i];
			int faceLeft = vertical ? originX + (labelsLeft ? width - size : 0) : originX + cursor + (span - size) / 2;
			int faceTop = vertical ? originY + cursor : originY;
			cursor += span + gap;
			int cx = faceLeft + size / 2;
			int cy = faceTop + size / 2;

			if (i >= talkers.size()) {
				drawOverflow(graphics, font, config, hidden, cx, cy, size, animator);
				continue;
			}
			Talker talker = talkers.get(i);
			float alpha = talker.presence();
			FaceRenderer.draw(graphics, config, talker.face(), cx, cy, size, animator.volume(talker), alpha, talker.uuid());

			if (names) {
				Component name = ellipsize(font, talker.name(), MAX_NAME_WIDTH);
				int nameW = font.width(name);
				int nameX = vertical ? (labelsLeft ? faceLeft - gap - nameW : faceLeft + size + gap) : cx - nameW / 2;
				int nameY = vertical ? cy - font.lineHeight / 2 : faceTop + size + labelGap;
				drawLabel(graphics, font, config, name, nameX, nameY, talker.talking() ? NAME_COLOR : QUIET_NAME_COLOR, alpha);
			}
		}
		return new int[]{originX, originY, width, height};
	}

	private static final float OVERFLOW_FACE_ALPHA = 0.45f;

	private static void drawOverflow(GuiGraphics graphics, Font font, LeafyConfig config, List<Talker> hidden,
	                                 int cx, int cy, int size, TalkerAnimator animator) {
		Talker first = hidden.get(0);
		FaceRenderer.draw(graphics, config, first.face(), cx, cy, size, animator.volume(first),
				first.presence() * OVERFLOW_FACE_ALPHA, first.uuid());
		Component more = Component.literal("+" + hidden.size());
		float textScale = Math.min(1f, (size - 2) / (float) font.width(more));
		Draw.pushScale(graphics, cx, cy, textScale);
		Draw.text(graphics, font, more, -font.width(more) / 2 + 1, -font.lineHeight / 2 + 1, 0xFFFFFFFF);
		Draw.popScale(graphics);
	}

	private static void drawLabel(GuiGraphics graphics, Font font, LeafyConfig config, Component text,
	                              int x, int y, int rgb, float alpha) {
		if (config.getBackgroundOpacity() > 0) {
			graphics.fill(x - 2, y - 1, x + font.width(text) + 1, y + font.lineHeight, FaceRenderer.backgroundArgb(config, alpha));
		}
		Draw.text(graphics, font, text, x, y, Draw.argb(alpha, rgb));
	}

	private static int[] renderList(GuiGraphics graphics, Font font, LeafyConfig config, List<Talker> talkers,
	                                int overflow, TalkerAnimator animator) {
		HudAnchor anchor = config.getHudAnchor();
		boolean mirrored = anchor.isRight();
		int size = config.getFaceSize();
		int pad = 3;
		int rowHeight = Math.max(size, font.lineHeight) + 2 + 2 * FaceRenderer.margin(config.getSpeakingAnimation(), size);
		int speakerWidth = FaceRenderer.SPEAKER_WIDTH;

		int maxName = 0;
		for (Talker talker : talkers) {
			maxName = Math.max(maxName, font.width(ellipsize(font, talker.name(), MAX_NAME_WIDTH)));
		}
		Component more = overflow > 0 ? TalkingHuds.text("list.more", overflow) : null;
		int facePad = pad + FaceRenderer.margin(config.getSpeakingAnimation(), size) - 1;
		int nameGap = 5 + FaceRenderer.margin(config.getSpeakingAnimation(), size);
		int content = Math.max(facePad - pad + size + nameGap + maxName + 6 + speakerWidth, more == null ? 0 : font.width(more));
		int rowWidth = pad + content + pad;
		int rows = talkers.size() + (more != null ? 1 : 0);
		int height = rows * rowHeight + (rows - 1);

		int originX = anchor.placeX(graphics.guiWidth(), rowWidth, config.getOffsetX(), edge(config));
		int originY = anchor.placeY(graphics.guiHeight(), height, config.getOffsetY(), edge(config));

		for (int i = 0; i < talkers.size(); i++) {
			Talker talker = talkers.get(i);
			float alpha = talker.presence();
			float volume = animator.volume(talker);
			int left = originX;
			int top = originY + i * (rowHeight + 1);
			int centerY = top + rowHeight / 2;

			graphics.fill(left, top, left + rowWidth, top + rowHeight, FaceRenderer.backgroundArgb(config, alpha));

			int faceCenterX = mirrored ? left + rowWidth - facePad - size / 2 : left + facePad + size / 2;
			FaceRenderer.draw(graphics, config, talker.face(), faceCenterX, centerY, size, volume, alpha, talker.uuid());

			Component name = ellipsize(font, talker.name(), MAX_NAME_WIDTH);
			int nameX = mirrored ? left + rowWidth - facePad - size - nameGap - font.width(name) : left + facePad + size + nameGap;
			Draw.text(graphics, font, name, nameX, centerY - font.lineHeight / 2 + 1,
					Draw.argb(alpha, talker.talking() ? NAME_COLOR : QUIET_NAME_COLOR));

			int speakerX = mirrored ? left + pad : left + rowWidth - pad - speakerWidth;
			FaceRenderer.drawSpeaker(graphics, speakerX, centerY - FaceRenderer.SPEAKER_HEIGHT / 2,
					Anim.clamp01(volume / 0.45f), alpha, config.getAccentColor().colorFor(talker.uuid()));
		}
		if (more != null) {
			int top = originY + talkers.size() * (rowHeight + 1);
			graphics.fill(originX, top, originX + rowWidth, top + rowHeight, FaceRenderer.backgroundArgb(config, 1f));
			Draw.text(graphics, font, more, originX + (rowWidth - font.width(more)) / 2, top + (rowHeight - font.lineHeight) / 2 + 1, 0xFFAAAAAA);
		}
		return new int[]{originX, originY, rowWidth, height};
	}

	public static Component ellipsize(Font font, Component name, int maxWidth) {
		String text = name.getString();
		if (font.width(text) <= maxWidth) {
			return name;
		}
		String ellipsis = "…";
		return Component.literal(font.plainSubstrByWidth(text, maxWidth - font.width(ellipsis)) + ellipsis);
	}
}
