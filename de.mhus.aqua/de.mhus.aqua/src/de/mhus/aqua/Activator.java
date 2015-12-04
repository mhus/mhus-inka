package de.mhus.aqua;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import de.mhus.aqua.api.IAqua;
import de.mhus.aqua.api.IRequestProcessor;
import de.mhus.aqua.core.Aqua;
import de.mhus.aqua.core.AquaDelegate;
import de.mhus.lib.MActivator;

public class Activator implements BundleActivator {

	private static BundleContext context;

	public static BundleContext getContext() {
		return context;
	}

	private static Aqua aqua;
	private static MActivator activator;
	private AquaDelegate myService;

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;

		activator = new MActivator(getClass().getClassLoader());
		aqua = new Aqua();
		aqua.init();
		
		myService = new AquaDelegate();
		context.registerService(IAqua.class.getName(), myService, null);
		context.registerService(IRequestProcessor.class.getName(), myService, null);
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

	public static Aqua getAqua() {
		return aqua;
	}

	public static MActivator getMActivator() {
		return activator;
	}

}
