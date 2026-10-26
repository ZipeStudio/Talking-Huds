package me.zipestudio.talkinghuds.client.render;

import me.zipestudio.talkinghuds.client.THClient;
import me.zipestudio.talkinghuds.TalkingHuds;
import me.zipestudio.talkinghuds.client.THManager;
import me.zipestudio.talkinghuds.client.voice.TalkerProfile;
import me.zipestudio.talkinghuds.config.LeafyConfig;
import me.zipestudio.talkinghuds.config.option.DisplayMode;
import me.zipestudio.talkinghuds.mixin.SubtitleOverlayHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

import java.util.UUID;

public final class VanillaSubtitles {

	private static final Identifier SOUND_ID = TalkingHuds.id("voice");
	private static final float RANGE = 4096f;
	private static final RandomSource RANDOM = RandomSource.create();

	private VanillaSubtitles() {
	}

	public static void tick(Minecraft mc) {
		LeafyConfig config = LeafyConfig.getInstance();
		if (!config.isEnableMod() || config.getDisplayMode() != DisplayMode.SUBTITLES
				|| mc.player == null || mc.level == null || !isEnabledInOptions(mc) || THManager.isEmpty()
				|| THClient.hideTalkers(mc)) {
			return;
		}
		SubtitleOverlay overlay = overlay(mc);
		long now = System.currentTimeMillis();
		UUID self = mc.player.getUUID();
		for (TalkerProfile profile : THManager.getProfiles()) {
			if (!THManager.isTalking(profile, now) || (profile.getUuid().equals(self) && !config.isShowSelf())) {
				continue;
			}
			if (mc.level.getPlayerByUUID(profile.getUuid()) instanceof AbstractClientPlayer player) {
				report(overlay, I18n.get(TalkingHuds.MOD_ID + ".subtitle.talking", player.getName().getString()),
						player.getX(), player.getEyeY(), player.getZ());
			}
		}
	}

	public static boolean isEnabledInOptions(Minecraft mc) {
		return mc.options.showSubtitles().get();
	}

	public static void setEnabledInOptions(Minecraft mc, boolean enabled) {
		mc.options.showSubtitles().set(enabled);
		mc.options.save();
	}

	private static void report(SubtitleOverlay overlay, String text, double x, double y, double z) {
		WeighedSoundEvents event = new WeighedSoundEvents(SOUND_ID, text);
		SoundInstance sound = new SimpleSoundInstance(SOUND_ID, SoundSource.PLAYERS, 1f, 1f, RANDOM,
				false, 0, SoundInstance.Attenuation.LINEAR, x, y, z, false);
		//? if >=1.21 {
		overlay.onPlaySound(sound, event, RANGE);
		//?} else {
		/*overlay.onPlaySound(sound, event);
		*///?}
	}

	private static SubtitleOverlay overlay(Minecraft mc) {
		//? if >=26.2 {
		/*return ((SubtitleOverlayHolder) mc.gui.hud).talking_huds$getSubtitleOverlay();
		*///?} else {
		return ((SubtitleOverlayHolder) mc.gui).talking_huds$getSubtitleOverlay();
		//?}
	}
}
