package de.mhus.aqua.mod;

import de.mhus.lib.MString;
import de.mhus.lib.jmx.JmxObject;


public class JmxAquaModule extends JmxObject implements JmxAquaModuleMBean {

	private AquaModule activator;

	public JmxAquaModule(AquaModule activator) {
		this.activator = activator;
		setName(activator.getId());
		setPackage(MString.beforeLastIndex(activator.getClass().getCanonicalName(),'.'));
	}
	
	public void publish() throws Exception {
		((AquaModule)activator).publish();
	}
	
	public void loadResources() throws Exception {
		((AquaModule)activator).loadResources();
	}

}
