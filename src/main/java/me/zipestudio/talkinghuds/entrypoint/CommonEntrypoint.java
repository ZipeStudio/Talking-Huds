package me.zipestudio.talkinghuds.entrypoint;

//? if fabric {

import me.zipestudio.talkinghuds.TalkingHuds;
import net.fabricmc.api.ModInitializer;

public class CommonEntrypoint implements ModInitializer {

	@Override
	public void onInitialize() {
		TalkingHuds.onInitialize();
	}

}

//?} elif neoforge {
/*import me.zipestudio.talkinghuds.TalkingHuds;
import net.neoforged.fml.common.Mod;

@Mod(TalkingHuds.MOD_ID)
public class CommonEntrypoint {

	public CommonEntrypoint() {
		TalkingHuds.onInitialize();
	}

}

*///?} elif forge {

/*import me.zipestudio.talkinghuds.TalkingHuds;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(TalkingHuds.MOD_ID)
public class CommonEntrypoint {

	public CommonEntrypoint() {
		TalkingHuds.onInitialize();
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientEntrypoint::onInitializeClient);
	}

}

*///?}
