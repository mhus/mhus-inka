package de.mhus.aqua.mod.uiapp;

import java.util.LinkedList;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.BundleContext;

import de.mhus.aqua.api.AquaSession;
import de.mhus.aqua.api.IAqua;
import de.mhus.aqua.api.IRequestProcessor;
import de.mhus.aqua.api.IUserRights;
import de.mhus.aqua.mod.AquaModule;
import de.mhus.aqua.mod.uiapp.wui.IWComponent;
import de.mhus.lib.MException;
import de.mhus.lib.MString;
import de.mhus.lib.MXml;
import de.mhus.lib.config.HashConfig;
import de.mhus.lib.config.IConfig;
import de.mhus.lib.config.JsonConfig;
import de.mhus.lib.config.XmlConfig;
import de.mhus.lib.logging.Log;

public class Activator extends AquaModule {

	private static Log log = Log.getLog(Activator.class);
	public static final String UI_PAGE = "de.mhus.aqua.mod.uiapp.PAGE";
	private static final String AQUA_WCOMPONENT = "de.mhus.aqua.mod.uiapp.wcomponent";
	private static Activator instance;

	public static Activator instance() {
		return instance;
	}

	private UiAquaDelegate myService;
	
	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);
		instance = this;
		
		myService = new UiAquaDelegate();
		context.registerService(UiAquaDelegate.class.getName(), myService, null);
		context.registerService(UiAquaDelegate.class.getName(), myService, null);

	}

	public void stop(BundleContext bundleContext) throws Exception {
		super.stop(bundleContext);
		instance = null;
	}

	public IWComponent createWComponent(String name,AquaSession session) throws MException {
		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(Activator.AQUA_WCOMPONENT);
		try {
			for (IConfigurationElement e : config) {
				String name2 = e.getAttribute("id");
				String acl2   = e.getAttribute("acl");
				if (name.equals(name2) && getAqua().getAcl(acl2).hasRight(session, IUserRights.READ)) {
					log.t("create wcomponent",name);
					Object o = e.createExecutableExtension("class");
					if (o instanceof IWComponent) {
						return (IWComponent) o;
					}
				}
			}
		} catch (Exception ex) {
			log.i(name,ex);
		}
		log.t("create wcomponent null",name);
		return null;
	}
	
	public ComponentInfo[] findWComponents(String group,AquaSession session) {
		LinkedList<ComponentInfo> out = new LinkedList<ComponentInfo>();
		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(Activator.AQUA_WCOMPONENT);
		for (IConfigurationElement e : config) {
			try {
				String group2 = e.getAttribute("group");
				String acl2   = e.getAttribute("acl");
				if (group.equals(group2) && getAqua().getAcl(acl2).hasRight(session, IUserRights.SHOW) ) {
					String id2     = e.getAttribute("id");
					String title2  = e.getAttribute("title");
					String config2 = e.getAttribute("config");
					IConfig config3 = null;
					if (MString.isEmpty(config2))
						config3 = new HashConfig();
					else
					if (config2.startsWith("<"))
						config3 = new XmlConfig(MXml.loadXml(config2).getDocumentElement());
					else
						config3 = new JsonConfig(config2);
					out.add(new ComponentInfo(id2, title2,config3));
				}
			} catch (Exception ex) {
				log.d(group,ex);
			}
		}
		return out.toArray(new ComponentInfo[out.size()]);
	}
	
}
