package me.zipestudio.talkinghuds.config;

import lombok.*;
import me.zipestudio.talkinghuds.TalkingHuds;
import me.zipestudio.talkinghuds.config.option.AccentColor;
import me.zipestudio.talkinghuds.config.option.DisplayMode;
import me.zipestudio.talkinghuds.config.option.HudAnchor;
import me.zipestudio.talkinghuds.config.option.SpeakingAnimation;
import me.zipestudio.talkinghuds.utils.CodecUtils;
import me.zipestudio.talkinghuds.utils.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import static me.zipestudio.talkinghuds.utils.CodecUtils.option;

@Getter
@Setter
@AllArgsConstructor
public class LeafyConfig {

	public static final int FACE_SIZE_MIN = 8;
	public static final int FACE_SIZE_MAX = 32;
	public static final double FADE_MIN = 0.5;
	public static final double FADE_MAX = 5.0;
	public static final int MAX_TALKERS_MIN = 1;
	public static final int MAX_TALKERS_MAX = 12;
	public static final int OFFSET_LIMIT = 10000;

	public static final Codec<LeafyConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			option("enableMod", true, Codec.BOOL, LeafyConfig::isEnableMod),
			option("usePlasmoVoice", true, Codec.BOOL, LeafyConfig::isUsePlasmoVoice),
			option("useSimpleVoiceChat", true, Codec.BOOL, LeafyConfig::isUseSimpleVoiceChat),
			option("displayMode", DisplayMode.HEADS, DisplayMode.CODEC, LeafyConfig::getDisplayMode),
			option("hudAnchor", HudAnchor.TOP_LEFT, HudAnchor.CODEC, LeafyConfig::getHudAnchor),
			option("offsetX", 0, Codec.INT, LeafyConfig::getOffsetX),
			option("offsetY", 0, Codec.INT, LeafyConfig::getOffsetY),
			option("faceSize", 10, Codec.INT, LeafyConfig::getFaceSize),
			option("speakingAnimation", SpeakingAnimation.PULSE, SpeakingAnimation.CODEC, LeafyConfig::getSpeakingAnimation),
			option("highlight", true, Codec.BOOL, LeafyConfig::isHighlight),
			option("accentColor", AccentColor.PLAYER, AccentColor.CODEC, LeafyConfig::getAccentColor),
			option("showNames", false, Codec.BOOL, LeafyConfig::isShowNames),
			option("showSelf", false, Codec.BOOL, LeafyConfig::isShowSelf),
			option("maxTalkers", 4, Codec.INT, LeafyConfig::getMaxTalkers),
			option("backgroundOpacity", 50, Codec.INT, LeafyConfig::getBackgroundOpacity),
			option("fadeSeconds", 1.5, Codec.DOUBLE, LeafyConfig::getFadeSeconds)
	).apply(instance, LeafyConfig::new));

	private static final Logger LOGGER = LoggerFactory.getLogger(TalkingHuds.MOD_NAME + "/Config");

	private static LeafyConfig INSTANCE;

	private boolean enableMod;
	private boolean usePlasmoVoice;
	private boolean useSimpleVoiceChat;
	private DisplayMode displayMode;
	private HudAnchor hudAnchor;
	private int offsetX;
	private int offsetY;
	private int faceSize;
	private SpeakingAnimation speakingAnimation;
	private boolean highlight;
	private AccentColor accentColor;
	private boolean showNames;
	private boolean showSelf;
	private int maxTalkers;
	private int backgroundOpacity;
	private double fadeSeconds;

	private LeafyConfig() { throw new IllegalArgumentException(); }

	public static LeafyConfig getInstance() {
		return INSTANCE == null ? reload() : INSTANCE;
	}

	public static LeafyConfig reload() {
		return INSTANCE = LeafyConfig.read();
	}

	public static LeafyConfig getNewInstance() {
		return CodecUtils.parseNewInstanceHacky(CODEC);
	}

	private static LeafyConfig read() {
		return ConfigUtils.readConfig(CODEC, configFile(), LOGGER).clamp();
	}

	public void saveAsync() {
		CompletableFuture.runAsync(this::save);
	}

	public void save() {
		ConfigUtils.saveConfig(this, CODEC, configFile(), LOGGER);
	}

	public LeafyConfig clamp() {
		//? if <1.21.6 {
		/*if (this.displayMode == DisplayMode.BAR) {
			this.displayMode = DisplayMode.HEADS;
		}
		*///?}
		this.faceSize = clamp(this.faceSize, FACE_SIZE_MIN, FACE_SIZE_MAX);
		this.maxTalkers = clamp(this.maxTalkers, MAX_TALKERS_MIN, MAX_TALKERS_MAX);
		this.backgroundOpacity = clamp(this.backgroundOpacity, 0, 100);
		this.fadeSeconds = Math.max(FADE_MIN, Math.min(FADE_MAX, this.fadeSeconds));
		this.offsetX = clamp(this.offsetX, -OFFSET_LIMIT, OFFSET_LIMIT);
		this.offsetY = clamp(this.offsetY, -OFFSET_LIMIT, OFFSET_LIMIT);
		return this;
	}

	public void setHudAnchor(HudAnchor hudAnchor) {
		if (this.hudAnchor != hudAnchor) {
			this.offsetX = 0;
			this.offsetY = 0;
		}
		this.hudAnchor = hudAnchor;
	}

	public long getFadeMs() {
		return (long) (Math.max(FADE_MIN, this.fadeSeconds) * 1000.0);
	}

	private static File configFile() {
		return getConfigDir().resolve(TalkingHuds.MOD_ID + ".json5").toFile();
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	//? if fabric {
	public static Path getConfigDir() {
		return net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir();
	}
	//?} else if neoforge {
	/*public static Path getConfigDir() {
		return net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get();
	}
	*///?} elif forge {
	/*public static Path getConfigDir() {
		return net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get();
	}
	*///?}

}
