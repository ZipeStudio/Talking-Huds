package me.zipestudio.talkinghuds.config;

import lombok.*;
import me.zipestudio.talkinghuds.TalkingHuds;
import me.zipestudio.talkinghuds.config.option.AccentColor;
import me.zipestudio.talkinghuds.config.option.DisplayMode;
import me.zipestudio.talkinghuds.config.option.HudAnchor;
import me.zipestudio.talkinghuds.config.option.SpeakingAnimation;
import me.zipestudio.talkinghuds.utils.CodecUtils;
import me.zipestudio.talkinghuds.utils.ConfigUtils;
import net.minecraft.util.Mth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import static me.zipestudio.talkinghuds.utils.CodecUtils.option;

@Getter
@Setter
@AllArgsConstructor
public class LeafyConfig {

	public static final int HEAD_SIZE_MIN = 8;
	public static final int HEAD_SIZE_MAX = 32;
	public static final double FADE_MIN = 0.0;
	public static final double FADE_MAX = 5.0;
	public static final int MAX_TALKERS_MIN = 1;
	public static final int MAX_TALKERS_MAX = 12;
	public static final int OFFSET_LIMIT = 10000;
	public static final int NAME_LENGTH_MIN = 3;
	public static final int NAME_LENGTH_MAX = 16;

	public static final Codec<LeafyConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			option("enableMod", true, Codec.BOOL, LeafyConfig::isEnableMod),
			option("usePlasmoVoice", true, Codec.BOOL, LeafyConfig::isUsePlasmoVoice),
			option("useSimpleVoiceChat", true, Codec.BOOL, LeafyConfig::isUseSimpleVoiceChat),
			option("displayMode", DisplayMode.HEADS_ROW, DisplayMode.CODEC, LeafyConfig::getDisplayMode),
			option("hudAnchor", HudAnchor.TOP_LEFT, HudAnchor.CODEC, LeafyConfig::getHudAnchor),
			option("offset", List.of(0, 0), Codec.INT.listOf(), config -> List.of(config.getOffsetX(), config.getOffsetY())),
			option("headSize", 10, Codec.INT, LeafyConfig::getHeadSize),
			option("speakingAnimation", SpeakingAnimation.PULSE, SpeakingAnimation.CODEC, LeafyConfig::getSpeakingAnimation),
			option("highlight", true, Codec.BOOL, LeafyConfig::isHighlight),
			option("accentColor", AccentColor.PLAYER, AccentColor.CODEC, LeafyConfig::getAccentColor),
			option("showNames", false, Codec.BOOL, LeafyConfig::isShowNames),
			option("nameLength", NAME_LENGTH_MAX, Codec.INT, LeafyConfig::getNameLength),
			option("showSelf", false, Codec.BOOL, LeafyConfig::isShowSelf),
			option("maxTalkers", 4, Codec.INT, LeafyConfig::getMaxTalkers),
			option("backgroundOpacity", 50, Codec.INT, LeafyConfig::getBackgroundOpacity),
			option("fadeSeconds", 1.2, Codec.DOUBLE, LeafyConfig::getFadeSeconds)
	).apply(instance, (enableMod, usePlasmoVoice, useSimpleVoiceChat, displayMode, hudAnchor, offset, headSize,
			speakingAnimation, highlight, accentColor, showNames, nameLength, showSelf, maxTalkers, backgroundOpacity, fadeSeconds) ->
			new LeafyConfig(enableMod, usePlasmoVoice, useSimpleVoiceChat, displayMode, hudAnchor,
					offset.isEmpty() ? 0 : offset.get(0), offset.size() < 2 ? 0 : offset.get(1), headSize, speakingAnimation,
					highlight, accentColor, showNames, nameLength, showSelf, maxTalkers, backgroundOpacity, fadeSeconds)));

	private static final Logger LOGGER = LoggerFactory.getLogger(TalkingHuds.MOD_NAME + "/Config");

	private static LeafyConfig INSTANCE;

	private boolean enableMod;
	private boolean usePlasmoVoice;
	private boolean useSimpleVoiceChat;
	private DisplayMode displayMode;
	private HudAnchor hudAnchor;
	private int offsetX;
	private int offsetY;
	private int headSize;
	private SpeakingAnimation speakingAnimation;
	private boolean highlight;
	private AccentColor accentColor;
	private boolean showNames;
	private int nameLength;
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
		/*if (this.displayMode == DisplayMode.LOCATOR_BAR) {
			this.displayMode = DisplayMode.HEADS_ROW;
		}
		*///?}
		this.headSize = Mth.clamp(this.headSize, HEAD_SIZE_MIN, HEAD_SIZE_MAX);
		this.nameLength = Mth.clamp(this.nameLength, NAME_LENGTH_MIN, NAME_LENGTH_MAX);
		this.maxTalkers = Mth.clamp(this.maxTalkers, MAX_TALKERS_MIN, MAX_TALKERS_MAX);
		this.backgroundOpacity = Mth.clamp(this.backgroundOpacity, 0, 100);
		this.fadeSeconds = Mth.clamp(this.fadeSeconds, FADE_MIN, FADE_MAX);
		this.offsetX = Mth.clamp(this.offsetX, -OFFSET_LIMIT, OFFSET_LIMIT);
		this.offsetY = Mth.clamp(this.offsetY, -OFFSET_LIMIT, OFFSET_LIMIT);
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
		return (long) (this.fadeSeconds * 1000.0);
	}

	private static File configFile() {
		return getConfigDir().resolve(TalkingHuds.MOD_ID + ".json5").toFile();
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
