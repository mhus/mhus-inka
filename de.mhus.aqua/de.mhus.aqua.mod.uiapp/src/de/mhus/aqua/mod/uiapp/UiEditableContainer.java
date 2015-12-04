package de.mhus.aqua.mod.uiapp;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

import de.mhus.aqua.api.AquaApplication;
import de.mhus.aqua.api.AquaRequest;
import de.mhus.aqua.mod.uiapp.UiContainer.CC;
import de.mhus.aqua.mod.uiapp.wui.IWComponent;
import de.mhus.aqua.mod.uiapp.wui.IWLayout;
import de.mhus.lib.MException;
import de.mhus.lib.MFile;
import de.mhus.lib.config.IConfig;
import de.mhus.lib.config.JsonConfig;
import de.mhus.lib.logging.Log;

@SuppressWarnings("serial")
public class UiEditableContainer extends UiContainer {
	
	private static Log log = Log.getLog(UiEditableContainer.class);
	private UiBox editor;
	
	public UiEditableContainer(AquaRequest request, AquaApplication application)
			throws MException {
		super(request, application);
				
	}

	@Override
	protected void doFillAttributes(AquaRequest request) {
		request.setAttribute("editor", editor);
		super.doFillAttributes(request);
	}

	@Override
	public void initContainer() throws MException {
		editor = createEditor(request);
		boxes.put(editor.getId(), editor);
		super.initContainer();
	}
	
	@Override
	protected UiBox createEditor(AquaRequest request) throws MException {

		try {
			File file = Activator.instance().getContentFile("config/admin_config.json");
	
			String txt = MFile.readFile(file);
			JsonConfig config = new JsonConfig( txt );
	
			return ((UiApplication)app).createBox(request, this, config);
		} catch (Exception e) {
			throw new MException(e);
		}
	}

	/**
	 * Add a box to the register and the configuration. Configuration is not been written.
	 * 
	 * @param box
	 * @throws Exception
	 */
	public IWComponent appendBox(UiBox box) throws Exception {
		log.t("appendBox",box.getId());
		boxes.put(box.getId(), box);
		
		IConfig cbox = config.createConfig("box");
		config.moveConfig(cbox, IConfig.MOVE_FIRST);
		cbox.setString("id", box.getId());
		cbox.setString("type", box.getType());
		
		IConfig clayout = config.getConfig("layout");
		IConfig celement = clayout.createConfig("element");
		celement.setInt("pos", layout.getDefaultContainer());
		celement.setString("id", box.getId());
		clayout.moveConfig(celement, IConfig.MOVE_FIRST);

		IWComponent ret = layout.insertBox(layout.getDefaultContainer(), layout.getDefaultContainer(), box);
		moveBox(box.getId(),layout.getDefaultContainer(),0);
		return ret;
	}

	@Override
	public void processAjax(AquaRequest originalRequest, AquaRequest request, String nid, String bid) throws IOException, MException {
		if ("editor".equals(bid)) {
			bid = editor.getId();
		}
		super.processAjax(originalRequest, request, nid, bid);
	}

	@Override
	public boolean processTplRequest(AquaRequest req, Map<String,Object> params, PrintWriter writer) throws MException {
		if ("editor".equals(params.get("name").toString() )) {
			editor.paint(req, writer);
			return true;
		}
		return super.processTplRequest(req, params, writer);
	}

	public IWLayout getLayout() {
		return layout;
	}

	public void moveBox(String boxId, int list, int pos) {
		log.t("moveBox",boxId,list,pos);
		// validate if box in set
		IConfig boxConfig = null;
		for ( IConfig bc : config.getConfigBundle("box") ) {
			if ( bc.getString("id","").equals(boxId) ) {
				boxConfig = bc;
				break;
			}
		}
		if (boxConfig == null) {
			log.t("moveBox","box not found",boxId);
			return;
		}
		IConfig clayout = config.getConfig("layout");
		IConfig[] cp = clayout.getConfigBundle("element");
		if (cp != null) {
			for (IConfig c : cp) {
				String id = c.getString("id","");
				if (id.equals(boxId)) {
					try {
						c.setInt("pos", pos);
						c.setInt("list", list);
						refreshLayout();
						return;
					} catch (MException e) {
						log.i(e); //should not be thrown
					}
				}
			}
		}
		try {
			log.t("moveBox","create entry",boxId);
			IConfig c = clayout.createConfig("element");
			c.setString("id", boxId);
			c.setInt("pos", pos);
			c.setInt("list", list);			
			refreshLayout();
		} catch (MException e) {
			log.i(e); //should not be thrown
		}
	}

	public void removeBox(String boxId) {
		UiBox box = boxes.remove(boxId);
		if (box == null) return;
		log.t("removeBox",boxId);
		for (IConfig c : config.getConfigBundle("box")) {
			if (c.getString("id", "").equals(boxId))
				try {
					config.removeConfig(c);
				} catch (MException e) {
				}
		}
		IConfig clayout = config.getConfig("layout");
		if (clayout != null) {
			for (IConfig c : clayout.getConfigBundle("element")) {
				if (c.getString("id", "").equals(boxId))
					try {
						config.removeConfig(c);
					} catch (MException e) {
					}
			}
		}
		refreshLayout();
	}


}
