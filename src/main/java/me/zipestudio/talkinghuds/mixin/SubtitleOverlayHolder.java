package me.zipestudio.talkinghuds.mixin;

import net.minecraft.client.gui.components.SubtitleOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

//? if >=26.2 {
/*@Mixin(net.minecraft.client.gui.Hud.class)
*///?} else {
@Mixin(net.minecraft.client.gui.Gui.class)
//?}
public interface SubtitleOverlayHolder {

	@Accessor("subtitleOverlay")
	SubtitleOverlay talking_huds$getSubtitleOverlay();
}
