package me.zipestudio.talkinghuds.client.render;

//? if >=1.21.6 {
import me.zipestudio.talkinghuds.client.THClient;
import me.zipestudio.talkinghuds.client.THManager;
import me.zipestudio.talkinghuds.client.voice.TalkerProfile;
import me.zipestudio.talkinghuds.config.LeafyConfig;
import me.zipestudio.talkinghuds.config.option.AccentColor;
import me.zipestudio.talkinghuds.config.option.DisplayMode;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.waypoints.TrackedWaypoint;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

public final class TalkingBarRenderer {

	public static final int BAR_WIDTH = 182;
	public static final int BAR_HEIGHT = 5;
	public static final int DOT_SIZE = 9;
	private static final int VISIBLE_DEGREE_RANGE = 60;
	private static final int BAR_BOTTOM_OFFSET = 24 + 5;
	private static final TalkerAnimator BAR_ANIMATOR = new TalkerAnimator();

	private TalkingBarRenderer() {
	}

	public static int barLeft(int guiWidth) {
		return (guiWidth - BAR_WIDTH) / 2;
	}

	public static int barTop(int guiHeight) {
		return guiHeight - BAR_BOTTOM_OFFSET;
	}

	public static int dotCenterY(int guiHeight) {
		return barTop(guiHeight) - 2 + DOT_SIZE / 2;
	}

	public static int markerCenterX(int guiWidth, double yaw) {
		int barCenterX = Mth.ceil((guiWidth - DOT_SIZE) / 2.0F) + DOT_SIZE / 2;
		return barCenterX + Mth.floor(yaw * 173.0 / 2.0 / VISIBLE_DEGREE_RANGE);
	}

	public static void drawBackground(GuiGraphics graphics) {
		graphics.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,
				net.minecraft.resources.Identifier.withDefaultNamespace("hud/locator_bar_background"),
				barLeft(graphics.guiWidth()), barTop(graphics.guiHeight()), BAR_WIDTH, BAR_HEIGHT);
	}

	public static boolean coversWaypoint(TrackedWaypoint waypoint) {
		Minecraft mc = Minecraft.getInstance();
		LeafyConfig config = LeafyConfig.getInstance();
		UUID uuid = waypoint.id().left().orElse(null);
		Entity cameraEntity = mc.getCameraEntity();
		if (uuid == null || mc.level == null || cameraEntity == null || uuid.equals(cameraEntity.getUUID())
				|| !config.isEnableMod() || config.getDisplayMode() != DisplayMode.LOCATOR_BAR || THClient.hideTalkers(mc)) {
			return false;
		}
		float presence = THManager.getPresence(THManager.getProfile(uuid), config.getFadeMs(), System.currentTimeMillis());
		return presence > THManager.MIN_VISIBLE_ALPHA && mc.level.getPlayerByUUID(uuid) instanceof AbstractClientPlayer;
	}

	@Nullable
	private static DeltaTracker shownThisFrame;

	public static void markShown(DeltaTracker deltaTracker) {
		shownThisFrame = deltaTracker;
	}

	public static void renderIfShown(GuiGraphics graphics) {
		DeltaTracker deltaTracker = shownThisFrame;
		shownThisFrame = null;
		if (deltaTracker != null) {
			render(graphics, deltaTracker);
		}
	}

	private static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
		Minecraft mc = Minecraft.getInstance();
		LeafyConfig config = LeafyConfig.getInstance();
		if (!config.isEnableMod() || config.getDisplayMode() != DisplayMode.LOCATOR_BAR) {
			return;
		}
		Entity cameraEntity = mc.getCameraEntity();
		if (mc.player == null || mc.level == null || cameraEntity == null || THClient.hideTalkers(mc) || THManager.isEmpty()) {
			return;
		}

		Level level = cameraEntity.level();
		//? if >=26.2 {
		/*Camera camera = mc.gameRenderer.mainCamera();
		*///?} else {
		Camera camera = mc.gameRenderer.getMainCamera();
		//?}
		//? if >=1.21.9 {
		net.minecraft.world.waypoints.PartialTickSupplier partialTicks =
				entity -> deltaTracker.getGameTimeDeltaPartialTick(!level.tickRateManager().isEntityFrozen(entity));
		//?}

		long now = System.currentTimeMillis();
		long fadeMs = config.getFadeMs();
		Font font = mc.font;
		UUID selfUuid = cameraEntity.getUUID();

		BAR_ANIMATOR.beginFrame();
		mc.player.connection.getWaypointManager().forEachWaypoint(cameraEntity, waypoint -> {
			UUID uuid = waypoint.id().left().orElse(null);
			if (uuid == null || uuid.equals(selfUuid)) {
				return;
			}
			TalkerProfile profile = THManager.getProfile(uuid);
			float presence = THManager.getPresence(profile, fadeMs, now);
			if (profile == null || presence <= THManager.MIN_VISIBLE_ALPHA) {
				return;
			}
			//? if >=1.21.9 {
			double yaw = waypoint.yawAngleToCamera(level, camera, partialTicks);
			//?} else {
			/*double yaw = waypoint.yawAngleToCamera(level, camera);
			*///?}
			if (yaw <= -VISIBLE_DEGREE_RANGE || yaw > VISIBLE_DEGREE_RANGE
					|| !(mc.level.getPlayerByUUID(uuid) instanceof AbstractClientPlayer player)) {
				return;
			}

			Talker talker = new Talker(uuid, profile.getVolume(), presence, THManager.isTalking(profile, now),
					FaceTexture.of(player), player.getName());
			int rgb = config.getAccentColor() == AccentColor.PLAYER
					? waypoint.icon().color.map(color -> color & 0xFFFFFF).orElseGet(() -> PlayerColors.locatorColor(uuid))
					: config.getAccentColor().colorFor(uuid);
			drawTalker(graphics, font, config, talker, markerCenterX(graphics.guiWidth(), yaw), presence, BAR_ANIMATOR.volume(talker), rgb);
		});
		BAR_ANIMATOR.endFrame();
	}

	public static void drawTalker(GuiGraphics graphics, Font font, LeafyConfig config, Talker talker, int markerCenterX,
	                              float grow, float volume, int rgb) {
		int dotCenterY = dotCenterY(graphics.guiHeight());
		int headSize = config.getHeadSize();
		float smallest = DOT_SIZE / (headSize + 2f);
		float scale = smallest + (1f - smallest) * grow;
		float size = headSize * scale;
		float pivotShift = 0.5f - (headSize % 2) * 0.5f;
		Draw.pushScale(graphics, markerCenterX + pivotShift, dotCenterY + pivotShift, scale);
		FaceRenderer.draw(graphics, config, talker.face(), 0, 0, headSize, volume, 1f, talker.uuid(), rgb, true);
		Draw.popScale(graphics);

		if (config.isShowNames() && grow > 0f) {
			Component name = TalkingOverlayRenderer.ellipsize(talker.name());
			int top = Math.round(dotCenterY - size / 2) - FaceRenderer.margin(config.getSpeakingAnimation(), Math.round(size));
			Draw.text(graphics, font, name, markerCenterX - font.width(name) / 2, top - font.lineHeight - 2, Draw.argb(grow, 0xFFFFFF));
		}
	}
}
//?}
