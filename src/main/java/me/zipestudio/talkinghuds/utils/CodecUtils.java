package me.zipestudio.talkinghuds.utils;

import com.google.gson.*;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.zipestudio.talkinghuds.TalkingHuds;

import java.util.*;
import java.util.function.*;

@SuppressWarnings("unused")
public final class CodecUtils {

	public static <A> A parseNewInstanceHacky(Codec<A> codec) {
		try {
			return codec.decode(JsonOps.INSTANCE, JsonParser.parseString("{}"))/*? if >=1.20.5 {*/.getOrThrow()/*?} else {*//*.getOrThrow(false, TalkingHuds.LOGGER::error)*//*?}*/.getFirst();
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed to create new instance of config in the %s mod".formatted(TalkingHuds.MOD_NAME), e);
		}
	}

	public static <A, B> RecordCodecBuilder<A, B> option(String optionId, B defValue, Codec<B> codec, Function<A, B> getter) {
		return codec.optionalFieldOf(optionId).xmap((o) -> o.orElse(defValue), Optional::ofNullable).forGetter(getter);
	}

	public static <E extends Enum<E>> Codec<E> enumCodec(E[] values, E fallback) {
		return Codec.STRING.xmap(name -> {
			for (E value : values) {
				if (value.name().equalsIgnoreCase(name)) {
					return value;
				}
			}
			return fallback;
		}, value -> value.name().toUpperCase(Locale.ROOT));
	}

}
