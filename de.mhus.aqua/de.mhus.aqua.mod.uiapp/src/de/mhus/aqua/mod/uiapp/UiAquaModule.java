package de.mhus.aqua.mod.uiapp;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import de.mhus.aqua.mod.AquaModule;

public class UiAquaModule extends AquaModule {

	private UiAquaDelegate uiAqua;

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		
		ServiceReference reference = bundleContext.getServiceReference(UiAquaDelegate.class.getName());
		uiAqua = (UiAquaDelegate) bundleContext.getService(reference);
		
		super.start(bundleContext);
	}
	
	@Override
	public void stop(BundleContext arg0) throws Exception {
		super.stop(arg0);
		uiAqua = null;
	}

	public UiAquaDelegate getUiAqua() {
		return uiAqua;
	}
	
}
