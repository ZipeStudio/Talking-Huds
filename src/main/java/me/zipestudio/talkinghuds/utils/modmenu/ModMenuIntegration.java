package me.zipestudio.talkinghuds.utils.modmenu;

import me.zipestudio.talkinghuds.client.screen.SettingsScreen;
import net.minecraft.client.gui.screens.Screen;

public class ModMenuIntegration extends AbstractModMenuIntegration {

	@Override
	protected Screen createConfigScreen(Screen parent) {
		return new SettingsScreen(parent);
	}

}
