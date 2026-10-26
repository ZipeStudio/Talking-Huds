package me.zipestudio.talkinghuds.client;

import me.zipestudio.talkinghuds.TalkingHuds;
import me.zipestudio.talkinghuds.client.render.VanillaSubtitles;
import me.zipestudio.talkinghuds.client.screen.SettingsScreen;
import me.zipestudio.talkinghuds.client.voice.PlasmoVoiceAddon;
import me.zipestudio.talkinghuds.config.LeafyConfig;
import me.zipestudio.talkinghuds.dev.DevTools;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public final class THClient {

	@Nullable
	private static Consumer<Minecraft> devTicker;

	private THClient() {
	}

	public static void onInitializeClient() {
		LeafyConfig.getInstance();
		if (TalkingHuds.isModLoaded("plasmovoice")) {
			PlasmoVoiceAddon.register();
		}
		if (TalkingHuds.isDevelopment()) {
			DevTools.init();
			TalkingHuds.LOGGER.info("{} Dev Tools Enabled", TalkingHuds.MOD_NAME);
		}
		TalkingHuds.LOGGER.info("{} Client Initialized", TalkingHuds.MOD_NAME);
	}

	public static void onClientTick(Minecraft client) {
		if (TalkingHuds.isModLoaded("plasmovoice")) {
			PlasmoVoiceAddon.tick();
		}
		THManager.cleanup(LeafyConfig.getInstance().getFadeMs(), System.currentTimeMillis());
		VanillaSubtitles.tick(client);
		THKeybinding.handle(client);
		if (devTicker != null) {
			devTicker.accept(client);
		}
	}

	public static void setDevTicker(@Nullable Consumer<Minecraft> ticker) {
		devTicker = ticker;
	}

	@Nullable
	public static Screen getScreen(Minecraft client) {
		//? if >=26.2 {
		/*return client.gui.screen();
		*///?} else {
		return client.screen;
		//?}
	}

	public static void setScreen(Minecraft client, @Nullable Screen screen) {
		//? if >=26.2 {
		/*client.setScreenAndShow(screen);
		*///?} else {
		client.setScreen(screen);
		//?}
	}

	public static boolean hideTalkers(Minecraft client) {
		//? if >=26.2 {
		/*boolean hidden = client.gui.hud.isHidden();
		*///?} else {
		boolean hidden = client.options.hideGui;
		//?}
		return hidden || getScreen(client) instanceof SettingsScreen;
	}
}
