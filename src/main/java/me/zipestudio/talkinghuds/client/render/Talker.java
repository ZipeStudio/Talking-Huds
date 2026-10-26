package me.zipestudio.talkinghuds.client.render;

import me.zipestudio.talkinghuds.client.THManager;
import me.zipestudio.talkinghuds.client.voice.TalkerProfile;
import me.zipestudio.talkinghuds.config.LeafyConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record Talker(UUID uuid, double volume, float presence, boolean talking, FaceTexture face,
                     Component name, @Nullable Vec3 position, boolean self) {

	public static List<Talker> collectVisible(Minecraft mc, LeafyConfig config) {
		List<Talker> talkers = new ArrayList<>();
		if (mc.player == null || mc.level == null || THManager.isEmpty()) {
			return talkers;
		}
		long now = System.currentTimeMillis();
		long fadeMs = config.getFadeMs();
		UUID selfUuid = mc.player.getUUID();

		List<TalkerProfile> profiles = new ArrayList<>(THManager.getProfiles());
		profiles.sort(Comparator.comparingLong(TalkerProfile::getFirstTalkMs));
		for (TalkerProfile profile : profiles) {
			boolean self = profile.getUuid().equals(selfUuid);
			if (self && !config.isShowSelf()) {
				continue;
			}
			float presence = THManager.getDisplayAlpha(profile, fadeMs, now);
			if (presence <= THManager.MIN_VISIBLE_ALPHA) {
				continue;
			}
			if (mc.level.getPlayerByUUID(profile.getUuid()) instanceof AbstractClientPlayer player) {
				talkers.add(new Talker(profile.getUuid(), profile.getVolume(), presence,
						THManager.isTalking(profile, now), FaceTexture.of(player), player.getName(),
						player.position(), self));
			}
		}
		return talkers;
	}
}
