package me.zipestudio.talkinghuds.mixin;

//? if >=1.21.6 {
import me.zipestudio.talkinghuds.client.render.TalkingBarRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=26.2 {
/*@Mixin(net.minecraft.client.gui.contextualbar.LocatorBar.class)
*///?} else {
@Mixin(net.minecraft.client.gui.contextualbar.LocatorBarRenderer.class)
//?}
public class LocatorBarRendererMixin {

	//? if >=26.1 {
	/*@Inject(method = "extractRenderState", at = @At("TAIL"))
	*///?} else {
	@Inject(method = "render", at = @At("TAIL"))
	//?}
	private void talking_huds$renderTalkers(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		TalkingBarRenderer.render(graphics, deltaTracker);
	}
}
//?}
