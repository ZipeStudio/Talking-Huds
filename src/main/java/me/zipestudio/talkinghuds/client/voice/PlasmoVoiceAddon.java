package me.zipestudio.talkinghuds.client.voice;

import me.zipestudio.talkinghuds.TalkingHuds;
import me.zipestudio.talkinghuds.config.LeafyConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.addon.AddonInitializer;
import su.plo.voice.api.addon.InjectPlasmoVoice;
import su.plo.voice.api.addon.annotation.Addon;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.source.ClientAudioSource;
import su.plo.voice.api.client.event.audio.capture.AudioCaptureProcessedEvent;
import su.plo.voice.api.client.event.audio.source.AudioSourceWriteEvent;
import su.plo.voice.api.client.event.connection.UdpClientPacketReceivedEvent;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.proto.data.audio.source.PlayerSourceInfo;
import su.plo.voice.proto.packets.udp.clientbound.SelfAudioInfoPacket;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Addon(
		id = "pv-addon-" + TalkingHuds.MOD_ID,
		name = TalkingHuds.MOD_NAME,
		version = TalkingHuds.MOD_VERSION,
		authors = {TalkingHuds.MOD_AUTHORS}
)
public class PlasmoVoiceAddon implements AddonInitializer {

	@InjectPlasmoVoice
	private static PlasmoVoiceClient voiceClient;

	private static final Map<UUID, Double> LEVELS = new ConcurrentHashMap<>();
	private volatile double lastOwnLevelDb = AudioUtils.SILENCE_DB;

	public static void register() {
		PlasmoVoiceClient.getAddonsLoader().load(new PlasmoVoiceAddon());
	}

	@Override
	public void onAddonInitialize() {
		TalkingHuds.LOGGER.info("Plasmo Voice addon initialized");
	}

	public static void tick() {
		if (!isEnabled() || voiceClient == null) {
			return;
		}
		for (ClientAudioSource<?> source : voiceClient.getSourceManager().getSources()) {
			if (source.canHear() && source.getSourceInfo() instanceof PlayerSourceInfo playerSource) {
				UUID player = playerSource.getPlayerInfo().getPlayerId();
				AudioUtils.onLevel(player, LEVELS.getOrDefault(player, AudioUtils.SILENCE_DB));
			}
		}
	}

	@EventSubscribe
	public void onSourceWrite(@NotNull AudioSourceWriteEvent event) {
		if (isEnabled() && event.getSource().getSourceInfo() instanceof PlayerSourceInfo playerSource) {
			LEVELS.put(playerSource.getPlayerInfo().getPlayerId(), AudioUtils.levelDb(event.getSamples()));
		}
	}

	@EventSubscribe
	public void onAudioCapture(@NotNull AudioCaptureProcessedEvent event) {
		if (isEnabled()) {
			this.lastOwnLevelDb = AudioUtils.levelDb(event.getProcessed().getMono());
		}
	}

	@EventSubscribe
	public void onSelfAudioPacket(@NotNull UdpClientPacketReceivedEvent event) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (isEnabled() && event.getPacket() instanceof SelfAudioInfoPacket && player != null) {
			AudioUtils.onLevel(player.getUUID(), this.lastOwnLevelDb);
		}
	}

	private static boolean isEnabled() {
		LeafyConfig config = LeafyConfig.getInstance();
		return config.isEnableMod() && config.isUsePlasmoVoice();
	}
}
