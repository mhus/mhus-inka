package de.mhus.aqua.mod.uiapp.wui;

import java.io.PrintWriter;
import java.util.Map;

import de.mhus.aqua.api.AquaRequest;
import de.mhus.aqua.mod.uiapp.Activator;
import de.mhus.aqua.mod.uiapp.UiBox;
import de.mhus.aqua.tpl.Engine;
import de.mhus.lib.MCast;
import de.mhus.lib.MException;
import de.mhus.lib.cao.CaoException;

public class WLayoutLCR extends IWLayout {

	private IWUiContainer[] columns;

	public WLayoutLCR() throws MException {
		setTplName(Activator.instance().getId() + "/wlayout_lcr");
	}
		
	public void paint(AquaRequest data, PrintWriter stream) throws MException {
		
		Engine engine = Activator.instance().getAqua().getTplEngine();
		Map<String, Object> attr = engine.createAttributes(data);		

		engine.execute(this, null, attr, stream);

	}
	
	@Override
	public void recreate(ContainerContributor contributor) {

		clear();
		
		columns = new IWUiContainer[3];
		
		
		columns[0] = new IWUiComposit();
		columns[1] = new IWUiComposit();
		columns[2] = new IWUiComposit();
		
		addChild(null,columns[0]);
		addChild(null,columns[1]);
		addChild(null,columns[2]);
		
		contributor.appendContainers(this);
		
	}
	
	public void processTplRequest(AquaRequest req, Map<String,Object> params, PrintWriter writer) throws MException {
		if ("paint".equals(params.get("name").toString() )) {
			int nr = MCast.toint(params.get("slot").toString(),0);
			if (nr >= getContainerSize() || nr < 0) return;
			PrintWriter stream = new PrintWriter(writer);
			try {
				columns[nr].paint(req, stream);
			} catch (CaoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			stream.flush();
			return;
		}
		super.processTplRequest(req, params, writer);
	}

	@Override
	public int getDefaultContainer() {
		return 1;
	}

	@Override
	public int getContainerSize() {
		return 3;
	}

	@Override
	public IWComponent appendBox(int list, int pos, UiBox box) throws MException {
		if (list<0 || list >= getContainerSize())
			list = getDefaultContainer();
		WAppContainerBox ret = new WAppContainerBox(box);
		if (pos < 0 )
			columns[list].addChild(null,ret);
		else
			columns[list].addChild(null,pos,ret);
		return ret;
	}

	@Override
	protected void doInit() throws MException {
	}

	@Override
	protected void doFillAttributes(AquaRequest data, Map<String, Object> attr) {
		
	}

	@Override
	public IWComponent insertBox(int posContainer, int pos, UiBox box) throws MException {
		if (pos<0 || pos >= getContainerSize())
			pos = getDefaultContainer();
		WAppContainerBox ret = new WAppContainerBox(box);
		columns[pos].addChild(null,pos,ret);
		return ret;
	}

}
