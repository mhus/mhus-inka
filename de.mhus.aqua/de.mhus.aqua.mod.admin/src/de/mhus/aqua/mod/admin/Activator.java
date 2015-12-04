package de.mhus.aqua.mod.admin;

import org.osgi.framework.BundleContext;

import de.mhus.aqua.mod.AquaModule;
import de.mhus.aqua.mod.uiapp.UiAquaModule;

public class Activator extends UiAquaModule {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
	}

	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

}
