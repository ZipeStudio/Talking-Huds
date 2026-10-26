package me.zipestudio.talkinghuds.mixin;

import me.zipestudio.talkinghuds.config.LeafyConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "su.plo.voice.client.render.voice.OverlayRenderer", remap = false)
public class PlasmoVoiceIconsMixin {

	@Inject(method = {"renderIcon", "renderEntity", "onRender"}, at = @At("HEAD"), cancellable = true, require = 0, remap = false)
	private void talking_huds$hideOverlay(CallbackInfo ci) {
		LeafyConfig config = LeafyConfig.getInstance();
		if (config.isEnableMod() && config.isUsePlasmoVoice()) {
			ci.cancel();
		}
	}
}
