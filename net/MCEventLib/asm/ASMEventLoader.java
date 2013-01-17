package net.MCEventLib.asm;

import java.util.Map;
import java.util.logging.Logger;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

public class ASMEventLoader implements IFMLLoadingPlugin {


	@Override
	public String[] getASMTransformerClass() {
		return new String[] {"net.MCEventLib.asm.ASMEventTransformer"};
	}


	@Override
	public String getModContainerClass() {
		return "net.MCEventLib.MCEventLib";
	}


	@Override
	public String[] getLibraryRequestClass() { return null; }

	@Override
	public String getSetupClass() { return null; }

	@Override
	public void injectData(Map<String, Object> data) {}

}
