package me.zipestudio.talkinghuds.client;

import com.mojang.blaze3d.platform.InputConstants;
import me.zipestudio.talkinghuds.TalkingHuds;
import me.zipestudio.talkinghuds.client.screen.SettingsScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import java.util.List;

public final class THKeybinding {

	//? if >=1.21.9 {
	private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(TalkingHuds.id(TalkingHuds.MOD_ID));
	//?} else {
	/*private static final String CATEGORY = "key.category." + TalkingHuds.MOD_ID + "." + TalkingHuds.MOD_ID;
	*///?}

	public static final KeyMapping OPEN_SETTINGS = new KeyMapping(
			TalkingHuds.MOD_ID + ".keybinding.modSettings",
			InputConstants.UNKNOWN.getValue(),
			CATEGORY
	);

	public static final List<KeyMapping> ALL = List.of(OPEN_SETTINGS);

	private THKeybinding() {
	}

	public static void handle(Minecraft client) {
		while (OPEN_SETTINGS.consumeClick()) {
			if (client.player != null) {
				THClient.setScreen(client, new SettingsScreen(THClient.getScreen(client)));
			}
		}
	}
}
