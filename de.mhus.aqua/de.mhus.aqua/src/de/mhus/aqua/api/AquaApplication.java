package de.mhus.aqua.api;

import de.mhus.lib.MException;
import de.mhus.lib.config.IConfig;

public abstract class AquaApplication {

	public abstract AquaContainer getUiContainer(AquaRequest request) throws MException;

	public abstract void process(AquaRequest request) throws Exception;

	public abstract void initialize() throws Exception;

	public abstract IConfig createDefaultConfig() throws Exception;

//	public abstract ComponentInfo[] getPossibleUIComponents(AquaRequest request, String parent);
	
	public AquaContainer getDefaultContainer() {
		return null;
	}
	
	
}
