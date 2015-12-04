package de.mhus.aqua.mod.uiapp;

import java.io.File;

import org.codehaus.jackson.map.ObjectMapper;

import de.mhus.aqua.api.AquaApplication;
import de.mhus.aqua.api.AquaContainer;
import de.mhus.aqua.api.AquaRequest;
import de.mhus.aqua.api.AquaSession;
import de.mhus.aqua.mod.uiapp.tst.ContGaga;
import de.mhus.aqua.mod.uiapp.wui.IWComponent;
import de.mhus.aqua.mod.uiapp.wui.IWPage;
import de.mhus.lib.MException;
import de.mhus.lib.MFile;
import de.mhus.lib.MXml;
import de.mhus.lib.config.IConfig;
import de.mhus.lib.config.JsonConfig;
import de.mhus.lib.config.XmlConfig;
import de.mhus.lib.logging.Log;

public class UiApplication extends AquaApplication {
	
	private static Log log = Log.getLog(UiApplication.class);
	private UiPage page;

	/**
	 * Overwrite this to return another container type.
	 * 
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public AquaContainer createUiContainer(AquaRequest request) throws MException {
		return new UiEditableContainer(request,this);
	}
	
	@Override
	public AquaContainer getUiContainer(AquaRequest request) throws MException {
		AquaContainer ac = null;
		AquaSession session = request.getSession();
		synchronized (session) {
			ac = (AquaContainer) session.getAttribute("container_" + request.getPath());
			if (ac == null) {
				ac = createUiContainer(request);
				session.setAttribute("container_" + request.getPath(), ac);
				ac.initContainer();
			}
		}
		return ac;
	}
	
	@Override
	public void process(AquaRequest request) throws Exception {
			log.t(request.getPath(),request.getExtPath());

			AquaContainer ac = getUiContainer(request);
			if (ac != null)
				ac.process(request);
	}

	@Override
	public void initialize() throws Exception {
//		ResApplication resApp = (ResApplication) ((Aqua)MSingleton.instance()).getAquaApplication(ResApplication.class.getCanonicalName());
//		IConfig config = ((Aqua)MSingleton.instance()).getModule(Activator.ID).getConfiguration();
//		IConfig resources = config.getConfig("resources");
//		for ( IConfig resource : resources.getConfigBundle("resource") ) {
//			AquaRes res = (AquaRes) MSingleton.instance().getActivator().createObject(resource.getProperty("type"));
//			res.setConfig(resource);
//			resApp.register(res);
//			this.resources.put(resource.getProperty("name"),res);
//		}

	}
	
//	private IConfig getConfig(AquaConnection con, String string) throws ParserConfigurationException, SAXException, IOException {
//		String file = con.getBaseDirectory() + Aqua.APP_UI_CONFIG;
//		FileInputStream is = new FileInputStream(file);
//		IConfig config = new XmlConfig(null,MXml.loadXml(is).getDocumentElement());
//		is.close();
//		return config;
//	}

	/**
	 * Overwrite this to return another config object.
	 */
	@Override
	public IConfig createDefaultConfig() throws Exception {
		
		File file = Activator.instance().getContentFile("config/app_config.json");
		String txt = MFile.readFile(file);
		return new JsonConfig( txt );
	}

	/**
	 * Overwrite this to return another set of components.
	 * 
	 * @param request
	 * @param parent
	 * @return
	 */
	public ComponentInfo[] getPossibleUIComponents(AquaSession session) {

		return Activator.instance().findWComponents("box",session);
		
	}
	
	public ComponentInfo getUIComponent(String id,AquaSession session) {
		for (ComponentInfo info : getPossibleUIComponents(session))
			if (info.getId().equals(id))
				return info;
		log.t("component info not found",id);
		return null;
	}
	
	/**
	 * Overwrite this to return another page. The page is stored in the MSingleton. This means there is only 
	 * ONE page for the engine.
	 * 
	 * @param request
	 * @return
	 * @throws MException
	 */
	public IWPage getPage(AquaRequest request) throws MException {
		if (page==null) {
			page = (UiPage)Activator.instance().getAqua().getObject(Activator.UI_PAGE);
			if (page==null) {
				page = new UiPage();
				page.initWElement(request, null, null);
				Activator.instance().getAqua().setObject(Activator.UI_PAGE, page);
			}
		}
		return page;
	}

	public UiBox createBox(AquaRequest request, UiContainer uiContainer, IConfig config) throws MException {
		String type = config.getProperty("type");
		
		UiBox box = (UiBox) Activator.instance().createWComponent(type,uiContainer.getSession());
		box.initWElement(request, null, config);
		
		if (box.isValid())
			return box;
		throw new MException("can't create box",type,uiContainer);
	}

	public UiBox createBox(AquaRequest request, UiContainer container, String id) throws Exception {
		ComponentInfo info = getUIComponent(id,request.getSession());
		IConfig config = info.getConfig();
		config.setProperty("type", info.getId());
		config.setProperty("id",IWComponent.createId());
		return createBox(request,container,config);
	}

}
