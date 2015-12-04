package de.mhus.aqua.mod.uiapp.wui;

import de.mhus.aqua.api.AquaRequest;
import de.mhus.aqua.mod.AquaModule;
import de.mhus.aqua.mod.uiapp.Activator;
import de.mhus.aqua.mod.uiapp.UiAjaxRes;
import de.mhus.aqua.mod.uiapp.UiBox;
import de.mhus.lib.MException;
import de.mhus.lib.config.IConfig;
import de.mhus.lib.logging.Log;

public class WKit {

	private static Log log = Log.getLog(WKit.class);
	private WNls nls;
	private AquaRequest request;
	

	public WKit(AquaRequest request, AquaModule module, String name) {
		this(request,new WNls(request, module, name));
	}
	
	public WKit(AquaRequest request,WNls nls) {
		this.nls = nls;
		this.request = request;
	}
	
	public WTree createInfoTree() throws MException {
		return (WTree) create(WTree.class);
	}

	public WTabPane createTabPane() throws MException {
		return (WTabPane) create(WTabPane.class);
	}

	public WNls getNls() {
		return nls;
	}

	public IWComponent create(Class<?> clazz) throws MException {
		return create(clazz,null,null);
	}
	
	public IWComponent create(Class<?> clazz, String id, IConfig config) throws MException {
		Object out = null;
		try {
			out = clazz.newInstance();
			((IWComponent)out).setNls(nls);
			if (out instanceof WExternal) ((WExternal)out).initWElement(request, id, config);
		} catch (Exception e) {
			throw new MException("can't create WComponent",clazz,id,config,e);
		}
		return (IWComponent) out;
	}
	
	public IWComponent create(String typeId) throws MException {
		return create(typeId,null,null);
	}
	
	public IWComponent create(String typeId, String id, IConfig config) throws MException {
		try {
			IWComponent out = Activator.instance().createWComponent(typeId,request.getSession());
			((IWComponent)out).setNls(nls);
			if (out instanceof WExternal) ((WExternal)out).initWElement(request, id, config);
			return out;
		} catch (Exception e) {
			throw new MException(typeId,id,e);
		}
	}

	public void fillContainer(IWUiContainer cont, IConfig definition) {
		for (IConfig element : definition.getConfigBundle("element")) {
			IWComponent comp;
			try {
				String include = element.getExtracted("include");
				if (include != null) {
					cont.addChild(element.getExtracted("list"), new WDataInclude(include));
				} else {
					comp = create(element.getExtracted("type"),element.getExtracted("id"),element.getConfig("config"));
					cont.addChild(element.getExtracted("list"), comp );
					if (element.getBoolean("ajax", false))
						((UiAjaxRes)Activator.instance().getRes("ajax")).addBox((UiBox) comp);
				}
			} catch (MException e) {
				log.e(element.getString("type", ""), e);
			}
		}
		
		for (IConfig element : definition.getConfigBundle("css")) {
			String res = element.getExtracted("resource");
			String path = element.getExtracted("path");
			cont.addCssResources(new WAquaResource(Activator.instance().getRes(res),path));
		}
		
		for (IConfig element : definition.getConfigBundle("js")) {
			String res = element.getExtracted("resource");
			String path = element.getExtracted("path");
			cont.addJsResources(new WAquaResource(Activator.instance().getRes(res),path));
		}
		
	}
	
}
