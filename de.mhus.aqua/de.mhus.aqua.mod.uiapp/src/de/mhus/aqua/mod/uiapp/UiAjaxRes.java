package de.mhus.aqua.mod.uiapp;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.WeakHashMap;

import de.mhus.aqua.api.AquaContainer;
import de.mhus.aqua.api.AquaRequest;
import de.mhus.aqua.api.IUserRights;
import de.mhus.aqua.api.RequestDelegate;
import de.mhus.aqua.cao.AquaConnection;
import de.mhus.aqua.cao.AquaElement;
import de.mhus.aqua.res.AquaRes;
import de.mhus.lib.MException;
import de.mhus.lib.logging.Log;

public class UiAjaxRes extends AquaRes {

	private static Log log = Log.getLog(UiAjaxRes.class);
	protected WeakHashMap<String, UiBox> boxes = new WeakHashMap<String, UiBox>();

	@Override
	public void process(AquaRequest request) throws Exception {

		String nid = request.getParameter("nid");
		String bid = request.getParameter("bid");
		log.t(nid,bid,request);

		if ("".equals(nid) && boxes.containsKey(bid)) {
			// from not bound element
			processAjax(request, request, nid, bid);
			return;
		}
		
		// affected node
		AquaElement node = (AquaElement) Activator.instance().getAqua().getCaoApplication().queryTree(AquaConnection.TREE_NODE, request.getSession(), nid, null );
		
		// check rights, is done internal now
//		if (!node.getAcl().hasRight(request.getSession(), IUserRights.READ)) {
		if (node == null) {
			request.sendErrorForbidden();
			return;
		}
		
		RequestDelegate r2 = new RequestDelegate(request);
		r2.setPath(nid);
		r2.setNode(node);
		AquaContainer n = node.getApplication().getUiContainer(r2);
		if (n instanceof UiContainer) {
			((UiContainer)n).processAjax(request,r2,nid,bid);
		}
	}

	public void addBox(UiBox box) {
		boxes.put(box.getId(), box);
	}
	
	public void removeBox(UiBox box) {
		boxes.remove(box.getId());
	}
	
	public void processAjax(AquaRequest originalRequest, AquaRequest request, String nid, String bid) throws IOException, MException {
		
		UiBox box = boxes.get(bid);
		if (box==null) {
			log.t("unbound box not found",bid);
			return;
		}
		
		PrintWriter writer = originalRequest.getWriter();
		
		box.processAjax(request, writer);
		
	}

}
