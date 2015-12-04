package de.mhus.aqua.mod.uiapp;

import java.io.PrintWriter;
import java.util.Map;

import de.mhus.aqua.api.AquaRequest;
import de.mhus.aqua.mod.uiapp.wui.WAquaResource;
import de.mhus.aqua.mod.uiapp.wui.WBreadcrumb;
import de.mhus.aqua.mod.uiapp.wui.WConsole;
import de.mhus.aqua.mod.uiapp.wui.WDataInclude;
import de.mhus.aqua.mod.uiapp.wui.WKit;
import de.mhus.aqua.mod.uiapp.wui.WLogin;
import de.mhus.aqua.mod.uiapp.wui.WNavigation;
import de.mhus.aqua.mod.uiapp.wui.IWPage;
import de.mhus.lib.MException;
import de.mhus.lib.config.IConfig;
import de.mhus.lib.logging.Log;

public class UiPage extends IWPage {

	private static Log log = Log.getLog(UiPage.class);
	private WKit kit;
	private AquaRequest request;

	//private static de.mhus.lib.logging.Log log = de.mhus.lib.logging.Log.getLog(UiPage.class);

	public void initWElement(AquaRequest request, String id, IConfig config) throws MException {
		this.request = request;
		super.initWElement(request, id, config);
	}
	
	@Override
	protected void doInit() throws MException {
//		super.doInit();
		setTplName(Activator.instance().getId() + "/wpage");
		kit = new WKit(request,Activator.instance(), "page");
		setNls(kit.getNls());
		try {
			
			IConfig definitions = Activator.instance().getConfiguration().getConfig("page");
			kit.fillContainer(this, definitions);
			
//			addChild("header",kit.create(WConsole.class));
//			addChild("header",kit.create(WBreadcrumb.class));
//			WLogin login = (WLogin) kit.create(WLogin.class);
//			((UiAjaxRes)Activator.instance().getRes("ajax")).addBox(login); // for ajax callbacks
//			addChild("header",login);
//			addChild("header",kit.create(WNavigation.class));
	
			engine = Activator.instance().getAqua().getTplEngine();
	
//			addJsResources( new WAquaResource(Activator.instance().getRes("yui")) );
//			addJsResources( new WAquaResource(Activator.instance().getRes("extjs"), "/adapter/ext/ext-base.js") );
//			addJsResources( new WAquaResource(Activator.instance().getRes("extjs")) );
//			addCssResources(new WAquaResource(Activator.instance().getRes("yui"), "/cssfonts/fonts-min.css") );
//			addCssResources(new WAquaResource(Activator.instance().getRes("extjs"), "/resources/css/ext-all.css") );
//			addCssResources(new WAquaResource(Activator.instance().getRes("yui"), "/cssgrids/grids.css") );
//	
//			addCssResources( new WAquaResource(Activator.instance().getRes("main"), "/main.css" ) );
//			addJsResources(new WAquaResource(Activator.instance().getRes("main"), "/main.js"));
	
			// use this wrapper to include a layout object from the request on this ...
//			addChild(null,new WDataInclude("layout"));
//			addChild("dialog",new WDataInclude("dialog"));
		} catch (Exception e) {
			log.e(e);
		}
	}

	@Override
	protected void doFillAttributes(AquaRequest data, Map<String, Object> attr) {
		super.doFillAttributes(data, attr);
		
		ContainerEditorBox editor = (ContainerEditorBox)data.getAttribute("editor");
		if (editor != null)
			attr.put("editor", editor.getId() );
	}

	public void processTplRequest(AquaRequest req, Map<String,Object> params, PrintWriter writer) throws MException {
		UiContainer cont = (UiContainer)req.getAttribute("container");
		if (cont != null) {
			if (cont.processTplRequest(req, params, writer))
				return;
		}
		
		super.processTplRequest(req, params, writer);
	}

}
