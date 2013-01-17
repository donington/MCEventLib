package net.MCEventLib;

import java.util.Arrays;

import net.minecraftforge.common.Configuration;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;


public class MCEventLib extends DummyModContainer {
	private static final String version = "0.2.1";
	private Configuration config;
	private boolean enabled = true;

	public MCEventLib() {
		super(new ModMetadata());
    ModMetadata meta = getMetadata();

    meta.modId       = "MCEventLib";
    meta.name        = "Minecraft Event Library";
    meta.version     = version;
    meta.credits     = "Special thanks to the authors of Forge's EventBus system.";
    meta.authorList  = Arrays.asList("donington");
    meta.description = "Extendable Event Bus System Library written for MinecraftForge.";
//    meta.url         = "";
//    meta.updateUrl   = "";
//    meta.logoFile    = "/net/MCEventLib/resources/logo.png";
	}


	@Subscribe
	public void preInit(FMLPreInitializationEvent event) {
		config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		/* ... */
		config.save();
	}


	// fake forge event bus registration so the mod is detected as enabled.
	// this is a library mod which should be present for other mods that require it.
	//
	@Override
	public boolean registerBus(EventBus bus, LoadController controller) {
		// bus.register(this);
		return true;
	}

}
