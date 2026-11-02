package me.zipestudio.talkinghuds.client.render;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.world.entity.player.PlayerModelPart;

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
	private final boolean hat;

	private FaceTexture(PlayerSkin skin, boolean hat) {
		this.skin = skin;
		this.hat = hat;
	}

	public static FaceTexture of(AbstractClientPlayer player) {
		return new FaceTexture(player.getSkin(), player.isModelPartShown(PlayerModelPart.HAT));
	}

	public static FaceTexture byUuid(UUID uuid) {
		return new FaceTexture(DefaultPlayerSkin.get(uuid), true);
	}
	//?} else {
	/*private final Identifier skin;
	private final boolean hat;

	private FaceTexture(Identifier skin, boolean hat) {
		this.skin = skin;
		this.hat = hat;
	}

	public static FaceTexture of(AbstractClientPlayer player) {
		return new FaceTexture(player.getSkinTextureLocation(), player.isModelPartShown(PlayerModelPart.HAT));
	}

	public static FaceTexture byUuid(UUID uuid) {
		return new FaceTexture(DefaultPlayerSkin.getDefaultSkin(uuid), true);
	}
	*///?}

	public void draw(GuiGraphics graphics, int x, int y, int size) {
		draw(graphics, x, y, size, 1f);
	}

	public void draw(GuiGraphics graphics, int x, int y, int size, float alpha) {
		//? if >=1.21.9 {
		net.minecraft.resources.Identifier texture = this.skin.body().texturePath();
		//?} elif >=1.20.2 {
		/*net.minecraft.resources.Identifier texture = this.skin.texture();
		*///?} else {
		/*Identifier texture = this.skin;
		*///?}
		//? if >=26.1 {
		/*net.minecraft.client.gui.components.PlayerFaceExtractor.extractRenderState(graphics, texture, x, y, size, this.hat, false,
				Draw.argb(alpha, 0xFFFFFF));
		*///?} elif >=1.21.4 {
		net.minecraft.client.gui.components.PlayerFaceRenderer.draw(graphics, texture, x, y, size, this.hat, false, Draw.argb(alpha, 0xFFFFFF));
		//?} else {
		/*com.mojang.blaze3d.systems.RenderSystem.enableBlend();
		com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
		net.minecraft.client.gui.components.PlayerFaceRenderer.draw(graphics, texture, x, y, size, this.hat, false);
		com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		*///?}
	}
}
