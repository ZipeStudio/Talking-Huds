package me.zipestudio.talkinghuds.client.voice;

import de.maxhenkel.voicechat.api.Position;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.ClientReceiveSoundEvent;
import de.maxhenkel.voicechat.api.events.ClientSoundEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import me.zipestudio.talkinghuds.TalkingHuds;
import me.zipestudio.talkinghuds.config.LeafyConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

//? if !fabric {
/*@de.maxhenkel.voicechat.api.ForgeVoicechatPlugin
*///?}
public class SimpleVoiceAddon implements VoicechatPlugin {

	@Override
	public String getPluginId() {
		return "vc-addon-" + TalkingHuds.MOD_ID;
	}

	@Override
	public void registerEvents(EventRegistration registration) {
		registration.registerEvent(ClientReceiveSoundEvent.EntitySound.class, this::onOtherPlayerSound);
		registration.registerEvent(ClientReceiveSoundEvent.LocationalSound.class, this::onOtherPlayerSound);
		registration.registerEvent(ClientReceiveSoundEvent.StaticSound.class, this::onOtherPlayerSound);
		registration.registerEvent(ClientSoundEvent.class, this::onOwnSound);
	}

	private void onOtherPlayerSound(ClientReceiveSoundEvent event) {
		if (!isEnabled()) {
			return;
		}
		double gain = distanceGain(event) * event.getVoicechat().getVolumeConfig().getVolume(event.getId());
		if (gain <= 0.0) {
			return;
		}
		AudioUtils.onSamples(event.getId(), event.getRawAudio());
	}

	private static double distanceGain(ClientReceiveSoundEvent event) {
		Vec3 soundPos;
		float maxDistance;
		Minecraft mc = Minecraft.getInstance();
		if (event instanceof ClientReceiveSoundEvent.LocationalSound sound) {
			Position pos = sound.getPosition();
			soundPos = new Vec3(pos.getX(), pos.getY(), pos.getZ());
			maxDistance = sound.getDistance();
		} else if (event instanceof ClientReceiveSoundEvent.EntitySound sound) {
			Player speaker = mc.level == null ? null : mc.level.getPlayerByUUID(sound.getEntityId());
			if (speaker == null) {
				return 1.0;
			}
			soundPos = speaker.position();
			maxDistance = sound.getDistance();
		} else {
			return 1.0;
		}
		Entity listener = mc.getCameraEntity() != null ? mc.getCameraEntity() : mc.player;
		if (maxDistance <= 0.0F || listener == null) {
			return 1.0;
		}
		double distance = Math.min(listener.position().distanceTo(soundPos), maxDistance);
		return 1.0 - distance / maxDistance;
	}

	private void onOwnSound(ClientSoundEvent event) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (isEnabled() && player != null) {
			AudioUtils.onSamples(player.getUUID(), event.getRawAudio());
		}
	}

	private static boolean isEnabled() {
		LeafyConfig config = LeafyConfig.getInstance();
		return config.isEnableMod() && config.isUseSimpleVoiceChat();
	}
}
