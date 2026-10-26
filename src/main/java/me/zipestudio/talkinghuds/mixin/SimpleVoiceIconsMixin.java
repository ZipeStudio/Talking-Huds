package me.zipestudio.talkinghuds.mixin;

import me.zipestudio.talkinghuds.config.LeafyConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "de.maxhenkel.voicechat.voice.client.GroupChatManager", remap = false)
public class SimpleVoiceIconsMixin {

	@Inject(method = "renderIcons", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
	private static void talking_huds$hideGroupHud(CallbackInfo ci) {
		LeafyConfig config = LeafyConfig.getInstance();
		if (config.isEnableMod() && config.isUseSimpleVoiceChat()) {
			ci.cancel();
		}
	}
}
