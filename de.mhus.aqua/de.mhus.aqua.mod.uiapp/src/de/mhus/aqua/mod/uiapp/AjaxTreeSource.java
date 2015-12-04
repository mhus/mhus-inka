package de.mhus.aqua.mod.uiapp;

import java.io.PrintWriter;

import de.mhus.aqua.api.AquaRequest;
import de.mhus.aqua.mod.uiapp.wui.ITreeNode;
import de.mhus.aqua.mod.uiapp.wui.ITreeSource;
import de.mhus.lib.MException;
import de.mhus.lib.MString;

public class AjaxTreeSource extends AjaxSource implements ITreeSource {

	public AjaxTreeSource(UiBox box) {
		super(box);
	}

	@Override
	public ITreeNode[] getRoots() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ITreeNode[] getChildren(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void processAjax(AquaRequest request, PrintWriter writer)
			throws MException {
//		request.setContentType(AquaRequest.MIME_XML);
		String id = request.getParameter("id");
		ITreeNode[] ret = null;
		if (MString.isEmpty(id))
			ret = getRoots();
		else
			ret = getChildren(id);
		
		writer.write("{ rc:");
		if (ret==null) {
			writer.write("0}");
			return;
		}
		
		writer.write("1,size:" + ret.length + ", values: [");
		boolean isFirst = true;
		for (ITreeNode node : ret) {
			if (isFirst) isFirst = false; else writer.write(",");
			writer.write("{");
			writer.write(
					"id:'" + MString.replaceAll(node.getId(),"'", "\\'") + "', " +
					"title:'" + MString.replaceAll(node.getTitle(),"'", "\\'") + "', " +
					"leaf: " + !node.hasChildren() );
			writer.write("}");
		}
		writer.write("]}");
		
	}

}
