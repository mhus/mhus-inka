package de.mhus.cap.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import de.mhus.lib.MActivator;
import de.mhus.lib.MSingleton;
import de.mhus.lib.cao.CaoDriver;
import de.mhus.lib.cao.CaoFactory;
import de.mhus.lib.logging.Log;

public class InitializeCaoFromOsgi {

	private static Log log = Log.getLog(InitializeCaoFromOsgi.class);
	
	public static void init() {
				
		CaoFactory factory = MSingleton.instance().getCaoFactory();
		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor("de.mhus.cao.model.driver");
		for (IConfigurationElement e : config) {
			String id    = e.getAttribute("driverIdentifier");
			String title = e.getAttribute("title");
			// String clazz = e.getAttribute("class");
			log.d("register",id,title);
			factory.registerDriver(id,title,new Activator(e));
		}

		config = Platform.getExtensionRegistry().getConfigurationElementsFor("de.mhus.cap.core.initialize");
		for (IConfigurationElement e : config) {
			try {
				log.d("initialize",e.getContributor());
				e.createExecutableExtension("class");
			} catch (CoreException e1) {
				log.w(e1);
			}
		}

	}
	
	public static class Activator extends MActivator {

		private IConfigurationElement config;
		//private String service;

		public Activator(IConfigurationElement config) {
			this.config = config;
		}
		
		@Override
		public Object createObject(String name) throws Exception {
			if (name.equals(CaoDriver.ACTIVATOR_SERVICE))
				return (CaoDriver)config.createExecutableExtension("class");
			return super.createObject(name);
		}
		
	}
}
