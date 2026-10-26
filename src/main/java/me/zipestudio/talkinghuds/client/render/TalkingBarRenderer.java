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

import java.util.UUID;

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

	public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
		Minecraft mc = Minecraft.getInstance();
		LeafyConfig config = LeafyConfig.getInstance();
		if (!config.isEnableMod() || config.getDisplayMode() != DisplayMode.BAR) {
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
		int dotCenterY = dotCenterY(graphics.guiHeight());
		int faceSize = config.getFaceSize();
		Font font = mc.font;
		UUID selfUuid = cameraEntity.getUUID();

		BAR_ANIMATOR.beginFrame();
		mc.player.connection.getWaypointManager().forEachWaypoint(cameraEntity, waypoint -> {
			UUID uuid = waypoint.id().left().orElse(null);
			if (uuid == null || uuid.equals(selfUuid)) {
				return;
			}
			TalkerProfile profile = THManager.getProfile(uuid);
			float presence = THManager.getDisplayAlpha(profile, fadeMs, now);
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
					FaceTexture.of(player), player.getName(), player.position(), false);
			int markerCenterX = markerCenterX(graphics.guiWidth(), yaw);
			int size = Math.round(DOT_SIZE + (faceSize - DOT_SIZE) * presence);
			int rgb = config.getAccentColor() == AccentColor.PLAYER
					? waypoint.icon().color.map(color -> color & 0xFFFFFF).orElseGet(() -> PlayerColors.locatorColor(uuid))
					: config.getAccentColor().colorFor(uuid);
			FaceRenderer.draw(graphics, config, talker.face(), markerCenterX, dotCenterY, size, BAR_ANIMATOR.volume(talker), 1f,
					uuid, rgb);

			if (config.isShowNames() && presence > 0.6f) {
				Component name = TalkingOverlayRenderer.ellipsize(font, player.getName(), 90);
				Draw.text(graphics, font, name, markerCenterX - font.width(name) / 2,
						dotCenterY - size / 2 - font.lineHeight - 2 - FaceRenderer.margin(config.getSpeakingAnimation(), size), 0xFFFFFFFF);
			}
		});
		BAR_ANIMATOR.endFrame();
	}
}
//?}
