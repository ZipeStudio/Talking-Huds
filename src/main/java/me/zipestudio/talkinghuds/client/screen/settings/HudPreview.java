package me.zipestudio.talkinghuds.client.screen.settings;

import me.zipestudio.talkinghuds.TalkingHuds;
import me.zipestudio.talkinghuds.client.THManager;
import me.zipestudio.talkinghuds.client.render.Anim;
import me.zipestudio.talkinghuds.client.render.Draw;
import me.zipestudio.talkinghuds.client.render.FaceRenderer;
import me.zipestudio.talkinghuds.client.render.FaceTexture;
//? if >=1.21.6 {
import me.zipestudio.talkinghuds.client.render.TalkingBarRenderer;
//?}
import me.zipestudio.talkinghuds.client.render.Talker;
import me.zipestudio.talkinghuds.client.render.TalkerAnimator;
import me.zipestudio.talkinghuds.client.render.TalkingOverlayRenderer;
import me.zipestudio.talkinghuds.client.render.VanillaSubtitles;
import me.zipestudio.talkinghuds.client.voice.TalkerProfile;
import me.zipestudio.talkinghuds.config.LeafyConfig;
import me.zipestudio.talkinghuds.config.option.DisplayMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class HudPreview {

	private static final String[] NAMES = {"Steve", "Alex", "Notch", "Jeb_"};
	private static final int SELECT_COLOR = 0x50FFFFFF;
	private static final int SELECT_FILL = 0x0CFFFFFF;
	private static final int[] NO_BOUNDS = {0, 0, 0, 0};

	private final List<Speaker> speakers = new ArrayList<>();
	private final TalkerAnimator animator = new TalkerAnimator();
	private int[] bounds = NO_BOUNDS;
	private boolean frozen;
	private long pausedMs;
	private long pausedAt;
	private int selectionPad = 3;

	public HudPreview() {
		for (int i = 0; i < NAMES.length; i++) {
			speakers.add(new Speaker(i, NAMES[i]));
		}
	}

	public int[] bounds() {
		return bounds;
	}

	public void setFrozen(boolean frozen) {
		if (frozen == this.frozen) {
			return;
		}
		long now = System.currentTimeMillis();
		if (frozen) {
			pausedAt = now;
		} else {
			pausedMs += now - pausedAt;
		}
		this.frozen = frozen;
	}

	private long clock() {
		return (frozen ? pausedAt : System.currentTimeMillis()) - pausedMs;
	}

	public boolean overHud(double x, double y) {
		return bounds[2] > 0 && x >= bounds[0] && x <= bounds[0] + bounds[2]
				&& y >= bounds[1] && y <= bounds[1] + bounds[3];
	}

	public void render(GuiGraphics graphics, Font font, LeafyConfig config, SettingsLayout layout) {
		Anim.useClock(clock());
		try {
			renderAt(graphics, font, config, layout);
		} finally {
			Anim.realClock();
		}
	}

	private void renderAt(GuiGraphics graphics, Font font, LeafyConfig config, SettingsLayout layout) {
		List<Talker> talkers = collectTalkers(config);
		selectionPad = TalkingOverlayRenderer.edge(config) + 2;
		animator.beginFrame(frozen);
		DisplayMode mode = config.getDisplayMode();
		if (mode.isMovable()) {
			bounds = TalkingOverlayRenderer.renderMode(graphics, font, config, talkers, animator);
		} else if (mode == DisplayMode.BAR) {
			bounds = NO_BOUNDS;
			renderBar(graphics, font, config, talkers);
		} else {
			bounds = NO_BOUNDS;
			renderSubtitles(graphics, font, talkers, layout);
		}
		animator.endFrame();
		if (mode.isMovable()) {
			drawSelection(graphics);
		}
	}

	private List<Talker> collectTalkers(LeafyConfig config) {
		long now = clock();
		long fadeMs = config.getFadeMs();
		List<Talker> talkers = new ArrayList<>();
		for (Speaker speaker : speakers) {
			long phase = (now + speaker.offset) % speaker.period;
			boolean talking = phase < speaker.period * 0.55;
			if (talking) {
				double volume = 0.2 + 0.3 * (0.5 + 0.5 * Math.sin(now / 110.0 + speaker.offset));
				if (speaker.profile == null) {
					speaker.profile = new TalkerProfile(speaker.uuid, volume, now);
				} else {
					speaker.profile.talk(volume, now);
				}
			}
			if (speaker.profile == null) {
				continue;
			}
			float presence = THManager.getDisplayAlpha(speaker.profile, fadeMs, now);
			if (!talking && presence <= THManager.MIN_VISIBLE_ALPHA) {
				speaker.profile = null;
				continue;
			}
			talkers.add(new Talker(speaker.uuid, speaker.profile.getVolume(), presence,
					THManager.isTalking(speaker.profile, now), speaker.face, speaker.name, null, false));
		}
		talkers.sort((first, second) -> Long.compare(firstTalk(first.uuid()), firstTalk(second.uuid())));
		return talkers;
	}

	private long firstTalk(UUID uuid) {
		for (Speaker speaker : speakers) {
			if (speaker.uuid.equals(uuid) && speaker.profile != null) {
				return speaker.profile.getFirstTalkMs();
			}
		}
		return Long.MAX_VALUE;
	}

	private static final double[] BAR_YAWS = {-42, -14, 17, 45};

	private void renderBar(GuiGraphics graphics, Font font, LeafyConfig config, List<Talker> talkers) {
		//? if >=1.21.6 {
		TalkingBarRenderer.drawBackground(graphics);
		int dotCenterY = TalkingBarRenderer.dotCenterY(graphics.guiHeight());
		int faceSize = config.getFaceSize();
		for (int i = 0; i < talkers.size(); i++) {
			Talker talker = talkers.get(i);
			int x = TalkingBarRenderer.markerCenterX(graphics.guiWidth(), BAR_YAWS[i % BAR_YAWS.length]);
			int size = Math.round(TalkingBarRenderer.DOT_SIZE + (faceSize - TalkingBarRenderer.DOT_SIZE) * talker.presence());
			FaceRenderer.draw(graphics, config, talker.face(), x, dotCenterY, size, animator.volume(talker), 1f, talker.uuid());
			if (config.isShowNames() && talker.presence() > 0.6f) {
				Component name = TalkingOverlayRenderer.ellipsize(font, talker.name(), 90);
				Draw.text(graphics, font, name, x - font.width(name) / 2,
						dotCenterY - size / 2 - font.lineHeight - 2 - FaceRenderer.margin(config.getSpeakingAnimation(), size), 0xFFFFFFFF);
			}
		}
		//?}
	}

	private void renderSubtitles(GuiGraphics graphics, Font font, List<Talker> talkers, SettingsLayout layout) {
		String[] arrows = {"<", ">", " ", "<"};
		int maxWidth = 0;
		List<Component> lines = new ArrayList<>();
		for (Talker talker : talkers) {
			Component line = TalkingHuds.text("subtitle.talking", talker.name().getString());
			lines.add(line);
			maxWidth = Math.max(maxWidth, font.width(line));
		}
		int boxWidth = maxWidth + font.width("<") * 2 + 8;
		int right = layout.previewRight - 4;
		for (int i = 0; i < lines.size(); i++) {
			Talker talker = talkers.get(i);
			int y = layout.screenHeight - 35 - i * (font.lineHeight + 1);
			float alpha = talker.talking() ? 1f : Math.max(0.3f, talker.presence());
			graphics.fill(right - boxWidth, y - 1, right, y + font.lineHeight + 1, Draw.argb(0.8f * alpha, 0x000000));
			int textColor = Draw.argb(alpha, 0xFFFFFF);
			Component line = lines.get(i);
			Draw.text(graphics, font, line, right - boxWidth / 2 - font.width(line) / 2, y, textColor);
			Draw.text(graphics, font, arrows[i % arrows.length], right - boxWidth + 2, y, textColor);
		}
	}

	public void drawSelection(GuiGraphics graphics) {
		int left = Math.max(0, bounds[0] - selectionPad);
		int top = Math.max(0, bounds[1] - selectionPad);
		int right = Math.min(graphics.guiWidth(), bounds[0] + bounds[2] + selectionPad);
		int bottom = Math.min(graphics.guiHeight(), bounds[1] + bounds[3] + selectionPad);
		if (bounds[2] > 0 && bounds[3] > 0) {
			graphics.fill(left, top, right, bottom, SELECT_FILL);
			Draw.frame(graphics, left, top, right, bottom, 1, SELECT_COLOR);
		}
	}

	public void drawWarnings(GuiGraphics graphics, Font font, LeafyConfig config, SettingsLayout layout, @Nullable Minecraft minecraft) {
		if (config.getDisplayMode() == DisplayMode.SUBTITLES && minecraft != null && !VanillaSubtitles.isEnabledInOptions(minecraft)) {
			int maxWidth = layout.previewRight - layout.previewLeft - 16;
			Draw.wrappedCenteredText(graphics, font, TalkingHuds.text("settings.hint.subtitles_off"),
					layout.previewCenterX(), layout.screenHeight / 2, maxWidth, 0xFFFFC857);
		}
	}

	private static final class Speaker {
		private final UUID uuid;
		private final Component name;
		private final FaceTexture face;
		private final long period;
		private final long offset;
		@Nullable
		private TalkerProfile profile;

		private Speaker(int index, String name) {
			this.uuid = UUID.nameUUIDFromBytes(("talking_huds-preview-" + index).getBytes());
			this.name = Component.literal(name);
			this.face = FaceTexture.byUuid(this.uuid);
			this.period = 4200L + index * 900L;
			this.offset = index * 1300L;
		}
	}
}
