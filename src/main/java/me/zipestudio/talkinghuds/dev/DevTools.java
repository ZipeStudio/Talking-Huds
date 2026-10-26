package me.zipestudio.talkinghuds.dev;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import me.zipestudio.talkinghuds.client.THClient;
import me.zipestudio.talkinghuds.client.THManager;
import me.zipestudio.talkinghuds.client.voice.AudioUtils;
import me.zipestudio.talkinghuds.config.LeafyConfig;
import me.zipestudio.talkinghuds.config.option.DisplayMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public final class DevTools {

	private static final int MAX_DUMMIES = 8;
	private static final double RADIUS = 4.0;
	private static final int FIRST_ENTITY_ID = -7_000_000;
	private static final int FIRST_CUSTOM_ENTITY_ID = -7_100_000;

	private static final List<Dummy> DUMMIES = new ArrayList<>();
	private static final Set<RemotePlayer> PINNED = new HashSet<>();
	private static ClientLevel dummyLevel;
	private static boolean talking = true;
	private static boolean orbit = false;
	private static boolean selfTalking = false;
	private static double orbitAngle;
	private static int customCount;

	private record Dummy(RemotePlayer player, double baseAngle, long talkSeed) {
	}

	private DevTools() {
	}

	public static void init() {
		THClient.setDevTicker(DevTools::tick);
		registerCommands();
	}

	private static void registerCommands() {
		//? if fabric {
		net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback.EVENT.register(
				(dispatcher, registryAccess) -> register(dispatcher));
		//?} elif neoforge {
		/*net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
				net.neoforged.neoforge.client.event.RegisterClientCommandsEvent.class, event -> register(event.getDispatcher()));
		*///?} elif forge {
		/*net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(
				(net.minecraftforge.client.event.RegisterClientCommandsEvent event) -> register(event.getDispatcher()));
		*///?}
	}

	private static <S> void register(CommandDispatcher<S> dispatcher) {
		LiteralArgumentBuilder<S> mode = DevTools.<S>literal("mode");
		for (DisplayMode displayMode : DisplayMode.selectable()) {
			mode.then(DevTools.<S>literal(displayMode.name().toLowerCase(Locale.ROOT)).executes(ctx -> setMode(displayMode)));
		}

		dispatcher.register(DevTools.<S>literal("thdev")
				.then(DevTools.<S>literal("spawn")
						.executes(ctx -> spawn(3))
						.then(RequiredArgumentBuilder.<S, Integer>argument("count", IntegerArgumentType.integer(1, MAX_DUMMIES))
								.executes(ctx -> spawn(IntegerArgumentType.getInteger(ctx, "count")))))
				.then(DevTools.<S>literal("customspawn")
						.then(RequiredArgumentBuilder.<S, String>argument("player", StringArgumentType.word())
								.executes(ctx -> customSpawn(StringArgumentType.getString(ctx, "player")))))
				.then(DevTools.<S>literal("tp")
						.then(RequiredArgumentBuilder.<S, String>argument("dummy", StringArgumentType.word())
								.suggests((ctx, builder) -> SharedSuggestionProvider.suggest(dummyNames(), builder))
								.executes(ctx -> teleport(StringArgumentType.getString(ctx, "dummy")))))
				.then(DevTools.<S>literal("remove")
						.then(RequiredArgumentBuilder.<S, String>argument("dummy", StringArgumentType.word())
								.suggests((ctx, builder) -> SharedSuggestionProvider.suggest(dummyNames(), builder))
								.executes(ctx -> remove(StringArgumentType.getString(ctx, "dummy")))))
				.then(DevTools.<S>literal("clear").executes(ctx -> clear()))
				.then(DevTools.<S>literal("talk")
						.then(DevTools.<S>literal("on").executes(ctx -> setTalking(true)))
						.then(DevTools.<S>literal("off").executes(ctx -> setTalking(false))))
				.then(DevTools.<S>literal("orbit")
						.then(DevTools.<S>literal("on").executes(ctx -> setOrbit(true)))
						.then(DevTools.<S>literal("off").executes(ctx -> setOrbit(false))))
				.then(DevTools.<S>literal("self")
						.then(DevTools.<S>literal("on").executes(ctx -> setSelfTalking(true)))
						.then(DevTools.<S>literal("off").executes(ctx -> setSelfTalking(false))))
				.then(mode));
	}

	private static <S> LiteralArgumentBuilder<S> literal(String name) {
		return LiteralArgumentBuilder.literal(name);
	}

	private static int spawn(int count) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null) {
			return 0;
		}
		removeDummies();
		dummyLevel = mc.level;
		for (int i = 0; i < count; i++) {
			UUID uuid = UUID.nameUUIDFromBytes(("talking_huds-dummy-" + i).getBytes());
			RemotePlayer dummy = new RemotePlayer(mc.level, newProfile(uuid, "Dummy" + (i + 1)));
			dummy.setId(FIRST_ENTITY_ID - i);
			Dummy entry = new Dummy(dummy, Math.toRadians(mc.player.getYRot()) + Math.PI * 2 * i / count, 1000L + i * 3571L);
			place(entry, mc.player, 0);
			addToLevel(mc.level, dummy);
			DUMMIES.add(entry);
		}
		return 1;
	}

	private static int customSpawn(String player) {
		//? if >=1.21.9 {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null) {
			return 0;
		}
		java.util.concurrent.CompletableFuture.supplyAsync(() -> lookUp(mc, player))
				.thenAcceptAsync(profile -> profile.ifPresent(found -> spawnProfile(mc, found)), mc);
		return 1;
		//?} else {
		/*return 0;
		*///?}
	}

	//? if >=1.21.9 {
	private static java.util.Optional<GameProfile> lookUp(Minecraft mc, String player) {
		net.minecraft.server.players.ProfileResolver resolver = mc.services().profileResolver();
		try {
			return resolver.fetchById(UUID.fromString(player));
		} catch (IllegalArgumentException notUuid) {
			return resolver.fetchByName(player).flatMap(found -> resolver.fetchById(found.id()));
		}
	}

	private static void spawnProfile(Minecraft mc, GameProfile profile) {
		if (mc.level == null || mc.player == null) {
			return;
		}
		if (mc.level != dummyLevel) {
			removeDummies();
			dummyLevel = mc.level;
		}
		net.minecraft.client.multiplayer.PlayerInfo info = new net.minecraft.client.multiplayer.PlayerInfo(profile, false);
		RemotePlayer dummy = new RemotePlayer(mc.level, profile) {
			{
				getEntityData().set(DATA_PLAYER_MODE_CUSTOMISATION, (byte) 0x7F);
			}

			@Override
			protected net.minecraft.client.multiplayer.PlayerInfo getPlayerInfo() {
				return info;
			}
		};
		dummy.setId(FIRST_CUSTOM_ENTITY_ID - customCount);
		Dummy entry = new Dummy(dummy, 0, 1000L + customCount * 2903L);
		customCount++;
		pinTo(dummy, mc.player);
		addToLevel(mc.level, dummy);
		DUMMIES.add(entry);
	}
	//?}

	private static List<String> dummyNames() {
		return DUMMIES.stream().map(dummy -> dummy.player().getName().getString()).toList();
	}

	private static Dummy find(String name) {
		return DUMMIES.stream().filter(dummy -> dummy.player().getName().getString().equalsIgnoreCase(name)).findFirst().orElse(null);
	}

	private static int teleport(String name) {
		Minecraft mc = Minecraft.getInstance();
		Dummy dummy = find(name);
		if (dummy == null || mc.player == null) {
			return 0;
		}
		pinTo(dummy.player(), mc.player);
		return 1;
	}

	private static int remove(String name) {
		Dummy dummy = find(name);
		if (dummy == null) {
			return 0;
		}
		removeDummy(dummy);
		DUMMIES.remove(dummy);
		THManager.forget(dummy.player().getUUID());
		return 1;
	}

	private static void pinTo(RemotePlayer dummy, Player target) {
		PINNED.add(dummy);
		dummy.setPos(target.getX(), target.getY(), target.getZ());
		dummy.setYRot(target.getYRot());
		dummy.setXRot(target.getXRot());
		dummy.setYHeadRot(target.getYHeadRot());
		dummy.setYBodyRot(target.getYRot());
		dummy.setOldPosAndRot();
		DevWaypoints.update(dummy);
	}

	private static int clear() {
		removeDummies();
		THManager.clear();
		return 1;
	}

	private static int setTalking(boolean value) {
		talking = value;
		return 1;
	}

	private static int setOrbit(boolean value) {
		orbit = value;
		return 1;
	}

	private static int setSelfTalking(boolean value) {
		selfTalking = value;
		return 1;
	}

	private static int setMode(DisplayMode mode) {
		LeafyConfig config = LeafyConfig.getInstance();
		config.setDisplayMode(mode);
		config.saveAsync();
		return 1;
	}

	private static void tick(Minecraft mc) {
		if (mc.level != dummyLevel || mc.player == null) {
			DUMMIES.clear();
			PINNED.clear();
			dummyLevel = null;
			return;
		}
		long now = System.currentTimeMillis();
		if (orbit) {
			orbitAngle += 0.02;
		}
		for (Dummy dummy : DUMMIES) {
			place(dummy, mc.player, orbitAngle);
			if (talking) {
				simulateVoice(dummy.player().getUUID(), dummy.talkSeed(), now);
			}
		}
		if (selfTalking) {
			simulateVoice(mc.player.getUUID(), 42L, now);
		}
	}

	private static void simulateVoice(UUID uuid, long seed, long now) {
		long period = 2500L + seed % 2000L;
		long phase = (now + seed * 7L) % period;
		if (phase < period * 0.6) {
			double wobble = Math.sin(now / 90.0 + seed) * 0.5 + Math.sin(now / 37.0 + seed * 3) * 0.5;
			AudioUtils.onLevel(uuid, -22.0 + 14.0 * wobble);
		}
	}

	private static void place(Dummy dummy, Player around, double extraAngle) {
		RemotePlayer player = dummy.player();
		if (PINNED.contains(player) || (!orbit && player.tickCount > 0)) {
			return;
		}
		double angle = dummy.baseAngle() + extraAngle;
		double x = around.getX() - Math.sin(angle) * RADIUS;
		double z = around.getZ() + Math.cos(angle) * RADIUS;
		player.setPos(x, around.getY(), z);
		float yaw = (float) Math.toDegrees(Math.atan2(-(around.getX() - x), around.getZ() - z));
		player.setYRot(yaw);
		player.setYHeadRot(yaw);
		player.setYBodyRot(yaw);
		DevWaypoints.update(player);
	}

	private static GameProfile newProfile(UUID uuid, String name) {
		return new GameProfile(uuid, name);
	}

	private static void addToLevel(ClientLevel level, RemotePlayer player) {
		//? if >=1.20.2 {
		level.addEntity(player);
		//?} else {
		/*level.addPlayer(player.getId(), player);
		*///?}
		DevWaypoints.track(player);
	}

	private static void removeDummies() {
		DUMMIES.forEach(DevTools::removeDummy);
		DUMMIES.clear();
		customCount = 0;
	}

	private static void removeDummy(Dummy dummy) {
		PINNED.remove(dummy.player());
		DevWaypoints.untrack(dummy.player());
		if (dummyLevel != null) {
			dummyLevel.removeEntity(dummy.player().getId(), Entity.RemovalReason.DISCARDED);
		}
	}
}
