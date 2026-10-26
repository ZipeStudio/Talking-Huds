package me.zipestudio.talkinghuds;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TalkingHuds {

	public static final String MOD_NAME = /*$ mod_name*/ "Talking Huds";
	public static final String MOD_ID = /*$ mod_id*/ "talking_huds";
	public static final String MOD_VERSION = /*$ mod_version*/ "1.2.0+1.21.11+fabric";
	public static final String MOD_AUTHORS = /*$ mod_authors*/ "MrLemonHog, ZipeStudio";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

	private TalkingHuds() {
	}

	public static void onInitialize() {
		LOGGER.info("{} Initialized", MOD_NAME);
	}

	public static Identifier id(String path) {
		//? if >=1.21 {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
		//?} else {
		/*return new Identifier(MOD_ID, path);
		*///?}
	}

	public static MutableComponent text(String path, Object... args) {
		return Component.translatable(MOD_ID + "." + path, args);
	}

	public static Component onOff(boolean state) {
		return text(state ? "settings.on" : "settings.off");
	}

	public static boolean isModLoaded(String modId) {
		//? if fabric {
		return net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded(modId);
		//?} elif neoforge {
		/*return net.neoforged.fml.ModList.get().isLoaded(modId);
		*///?} elif forge {
		/*return net.minecraftforge.fml.ModList.get().isLoaded(modId);
		*///?}
	}

	public static boolean isDevelopment() {
		//? if fabric {
		return net.fabricmc.loader.api.FabricLoader.getInstance().isDevelopmentEnvironment();
		//?} elif neoforge && >=1.21.9 {
		/*return !net.neoforged.fml.loading.FMLEnvironment.isProduction();
		*///?} elif neoforge {
		/*return !net.neoforged.fml.loading.FMLEnvironment.production;
		*///?} elif forge {
		/*return !net.minecraftforge.fml.loading.FMLLoader.isProduction();
		*///?}
	}
}
