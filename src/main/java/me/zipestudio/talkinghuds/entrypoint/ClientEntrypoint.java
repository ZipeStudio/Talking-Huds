package me.zipestudio.talkinghuds.entrypoint;

//? if fabric {

import me.zipestudio.talkinghuds.TalkingHuds;
import me.zipestudio.talkinghuds.client.THClient;
import me.zipestudio.talkinghuds.client.THKeybinding;
import me.zipestudio.talkinghuds.client.render.TalkingOverlayRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;

public class ClientEntrypoint implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		THClient.onInitializeClient();

		THKeybinding.ALL.forEach(ClientEntrypoint::registerKey);
		ClientTickEvents.END_CLIENT_TICK.register(THClient::onClientTick);

		//? if >=1.21.6 {
		net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry.addLast(TalkingHuds.id("talking_hud"),
				(graphics, deltaTracker) -> TalkingOverlayRenderer.render(graphics));
		//?} else {
		/*net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback.EVENT.register(
				(graphics, tickDelta) -> TalkingOverlayRenderer.render(graphics));
		*///?}
	}

	private static void registerKey(KeyMapping key) {
		//? if >=26.1 {
		/*net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper.registerKeyMapping(key);
		*///?} else {
		net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper.registerKeyBinding(key);
		//?}
	}

}

//?} elif neoforge {
/*import me.zipestudio.talkinghuds.TalkingHuds;
import me.zipestudio.talkinghuds.client.THClient;
import me.zipestudio.talkinghuds.client.THKeybinding;
import me.zipestudio.talkinghuds.client.render.TalkingOverlayRenderer;
import me.zipestudio.talkinghuds.utils.modmenu.ModMenuIntegration;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = TalkingHuds.MOD_ID, dist = Dist.CLIENT)
public class ClientEntrypoint {

	public ClientEntrypoint(IEventBus modBus, ModContainer container) {
		THClient.onInitializeClient();

		ModMenuIntegration integration = new ModMenuIntegration();
		integration.register(container);

		modBus.addListener(RegisterKeyMappingsEvent.class, event -> THKeybinding.ALL.forEach(event::register));
		modBus.addListener(RegisterGuiLayersEvent.class, event -> event.registerAboveAll(TalkingHuds.id("talking_hud"),
				(graphics, deltaTracker) -> TalkingOverlayRenderer.render(graphics)));
		NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class,
				event -> THClient.onClientTick(Minecraft.getInstance()));
	}

}

*///?} elif forge {

/*import me.zipestudio.talkinghuds.client.THClient;
import me.zipestudio.talkinghuds.client.THKeybinding;
import me.zipestudio.talkinghuds.client.render.TalkingOverlayRenderer;
import me.zipestudio.talkinghuds.utils.modmenu.ModMenuIntegration;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ClientEntrypoint {

	public static void onInitializeClient() {
		THClient.onInitializeClient();

		ModMenuIntegration integration = new ModMenuIntegration();
		integration.register(ModLoadingContext.get().getContainer());

		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		modBus.addListener((RegisterKeyMappingsEvent event) -> THKeybinding.ALL.forEach(event::register));
		modBus.addListener((RegisterGuiOverlaysEvent event) -> event.registerAboveAll("talking_hud",
				(gui, graphics, partialTick, width, height) -> TalkingOverlayRenderer.render(graphics)));
		MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> {
			if (event.phase == TickEvent.Phase.END) THClient.onClientTick(Minecraft.getInstance());
		});
	}

}

*///?}
