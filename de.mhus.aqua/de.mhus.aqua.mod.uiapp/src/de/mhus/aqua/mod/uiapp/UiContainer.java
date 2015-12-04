package de.mhus.aqua.mod.uiapp;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import de.mhus.aqua.api.AquaApplication;
import de.mhus.aqua.api.AquaContainer;
import de.mhus.aqua.api.AquaRequest;
import de.mhus.aqua.mod.uiapp.wui.IWComponent;
import de.mhus.aqua.mod.uiapp.wui.ContainerContributor;
import de.mhus.aqua.mod.uiapp.wui.IWLayout;
import de.mhus.aqua.mod.uiapp.wui.WDialog;
import de.mhus.aqua.mod.uiapp.wui.WLayoutLCR;
import de.mhus.aqua.mod.uiapp.wui.IWPage;
import de.mhus.lib.MException;
import de.mhus.lib.config.ConfigUtil;
import de.mhus.lib.config.IConfig;

/**
 * Aqua Ui Application. A Container for every node and session with UiApplication. This will store the specific
 * informations for this node / session.
 * 
 * @author mikehummel
 *
 */
@SuppressWarnings("serial")
public abstract class UiContainer extends AquaContainer implements Serializable {


	private static de.mhus.lib.logging.Log log = de.mhus.lib.logging.Log
			.getLog(UiContainer.class);


	protected IWLayout layout;
	protected HashMap<String, UiBox> boxes = new HashMap<String, UiBox>();
	protected LinkedList<WDialog> dialogs = new LinkedList<WDialog>();


	protected AquaRequest request;

	public UiContainer(AquaRequest request, AquaApplication application) throws MException {
		super(application, request.getNode(), request.getSession());
		this.request = request;
	}

	@Override
	public void initContainer() throws MException {
		log.t("create ui container",session,session.getUser(),node);
		if (log.isTrace()) ConfigUtil.dump(config, System.out);
		
		for ( IConfig boxConfig : config.getConfigBundle("box") ) {
			try {
				UiBox box = createBox(request,boxConfig);
				boxes.put(box.getId(), box);
			} catch (Exception e) {
				log.info(e);
			}
		}
		
		createLayout();
		refreshLayout();

	}
	/**
	 * Create a editor element for this container.
	 * 
	 * @param request
	 * @return
	 * @throws Exception
	 */
	protected abstract UiBox createEditor(AquaRequest request) throws MException;

	/**
	 * Create a UiBox for the container, Overwrite this if you don't want to create it from the apllication.
	 * 
	 * @param request
	 * @param config
	 * @return
	 * @throws Exception
	 */
	protected UiBox createBox(AquaRequest request,IConfig config) throws MException {
		return ((UiApplication)app).createBox(request,this,config);
	}
	
	/**
	 * Create a WLayout. Overwrite it you you have other plans.
	 * 
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public void createLayout() throws MException {
		IConfig clayout = config.getConfig("layout");
		if (clayout==null || clayout.getString("type", null) == null) {
			config.removeProperty("layout");
			clayout = config.createConfig("layout");
			clayout.setProperty("type", WLayoutLCR.class.getCanonicalName());
		}
		try {
			layout = (IWLayout) Activator.instance().createWComponent(clayout.getProperty("type"),getSession());
		} catch (Exception e) {
			throw new MException(e);
		}
	}

	protected void doFillAttributes(AquaRequest request) {
		request.setAttribute("layout", layout);
		if (dialogs.size() != 0) 
			request.setAttribute("dialog", dialogs.getLast());
		
		request.setAttribute("container", this);		
	}

	/**
	 * This is the callback to process a http request from the tomcat. The request is redirected from the filter implementation.
	 */
	@Override
	public void process(AquaRequest request) throws Exception {

		// cancel dialogs - this is a emergency function - maybe remove it and use a admin console for this.
		if ("true".equals(request.getParameter("__cancel_dialog"))) {
			for (Object dialog : dialogs.toArray())
				((WDialog)dialog).close();
		}
		
		// fill attributes
		doFillAttributes(request);
				
		// execute paint
		IWPage page = ((UiApplication)app).getPage(request);
		page.paint(request,request.getWriter());
		
	}

	/**
	 * Set the current dialog - maybe implement a queue of dialogs...
	 * 
	 * @param dialog
	 */
	public void addDialog(WDialog dialog) {
		this.dialogs.add(dialog);
	}
	
	public void removeDialog(WDialog dialog) {
		this.dialogs.remove(dialog);
	}

 class CC implements ContainerContributor {
//		private AquaRequest request;
		private IConfig config;

		CC(IConfig clayout){
			this.config = clayout;
		}
		
		public void appendContainers(IWLayout layout ) {
			HashMap<String, UiBox> cacheList = new HashMap<String, UiBox>(boxes);
			
			IConfig[] cp = config.getConfigBundle("element");
			if (cp != null) {
				
				for (IConfig c : cp) {
					int list = c.getInt("list", layout.getDefaultContainer());
					int pos = c.getInt("pos", -1);
					String id = c.getProperty("id");
					UiBox box = cacheList.get(id);
					if (box==null) 
						log.t("box not found or double",id);
					else {
						cacheList.remove(id);
						try {
							layout.appendBox(list, pos,box);
						} catch (MException e) {
							log.t(id,e);
						}
					}
	
				}

			}
			
			// append unattached
			for (UiBox box : cacheList.values()) {
				if ( box.isVisible() )
					try {
						layout.appendBox(layout.getDefaultContainer(), -1, box);
					} catch (MException e) {
						log.t(box.getId(),e);
					}
			}
		}

	}

	 /**
	  * Process a ajax request. The method is the callback from the ResAjax implementation. Implemens a 
	  * generic bid (box id) with the name 'admin'. Other ids are generated on the fly.
	  */
	public void processAjax(AquaRequest originalRequest, AquaRequest request, String nid, String bid) throws IOException, MException {
		
		UiBox box = null;
		
		box = boxes.get(bid);
		
		if (box == null) {
			log.t("box not found",bid);
			return;
		}
		
		PrintWriter writer = originalRequest.getWriter();
		
		box.processAjax(request, writer);
		
	}
	
	/**
	 * Process request from template execution wit the command \@process it will call this method.
	 * 
	 * @param req
	 * @param params
	 * @param writer
	 * @return
	 * @throws MException
	 */
	public boolean processTplRequest(AquaRequest req, Map<String,Object> params, PrintWriter writer) throws MException {
		return false;
	}
	
	public void refreshLayout() {
		IConfig clayout = config.getConfig("layout");
		layout.recreate(new CC(clayout));
	}
	public UiBox getBox(String boxId) {
		return boxes.get(boxId);
	}
	
}