package me.zipestudio.talkinghuds.client.render;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;

import java.util.UUID;

//? if >=1.21.9 {
import net.minecraft.world.entity.player.PlayerSkin;
//?} elif >=1.20.2 {
/*import net.minecraft.client.resources.PlayerSkin;
*///?} else {
/*import net.minecraft.resources.Identifier;
*///?}

public final class FaceTexture {

	//? if >=1.20.2 {
	private final PlayerSkin skin;

	private FaceTexture(PlayerSkin skin) {
		this.skin = skin;
	}

	public static FaceTexture of(AbstractClientPlayer player) {
		return new FaceTexture(player.getSkin());
	}

	public static FaceTexture byUuid(UUID uuid) {
		return new FaceTexture(DefaultPlayerSkin.get(uuid));
	}
	//?} else {
	/*private final Identifier skin;

	private FaceTexture(Identifier skin) {
		this.skin = skin;
	}

	public static FaceTexture of(AbstractClientPlayer player) {
		return new FaceTexture(player.getSkinTextureLocation());
	}

	/^* The default (Steve/Alex) skin a UUID maps to. ^/
	public static FaceTexture byUuid(UUID uuid) {
		return new FaceTexture(DefaultPlayerSkin.getDefaultSkin(uuid));
	}
	*///?}

	public void draw(GuiGraphics graphics, int x, int y, int size) {
		draw(graphics, x, y, size, 1f);
	}

	public void draw(GuiGraphics graphics, int x, int y, int size, float alpha) {
		//? if >=1.21.4 {
		int tint = Draw.argb(alpha, 0xFFFFFF);
		//?}
		//? if >=26.1 {
		/*net.minecraft.client.gui.components.PlayerFaceExtractor.extractRenderState(graphics, this.skin, x, y, size, tint);
		*///?} elif >=1.21.4 {
		net.minecraft.client.gui.components.PlayerFaceRenderer.draw(graphics, this.skin, x, y, size, tint);
		//?} else {
		/*com.mojang.blaze3d.systems.RenderSystem.enableBlend();
		com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
		net.minecraft.client.gui.components.PlayerFaceRenderer.draw(graphics, this.skin, x, y, size);
		com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		*///?}
	}
}
