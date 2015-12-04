package de.mhus.aqua.mod.uiapp;

import java.io.PrintWriter;

import de.mhus.aqua.api.AquaRequest;
import de.mhus.aqua.mod.uiapp.wui.ITreeNode;
import de.mhus.aqua.mod.uiapp.wui.ITreeSource;
import de.mhus.aqua.mod.uiapp.wui.WColumnSelector;
import de.mhus.lib.MException;

public abstract class XmlTreeSource extends AjaxSource implements ITreeSource {
	
	public XmlTreeSource(UiBox box) {
		super(box);
	}

	public abstract ITreeNode[] getRoots();
	
	public abstract ITreeNode[] getChildren(String id);
	

	@Override
	public void processAjax(AquaRequest request, PrintWriter writer)
			throws MException {
		request.setContentType(AquaRequest.MIME_XML);
		// at once
		//TODO use writer to write xml !!!!
		StringBuffer sb = new StringBuffer();
		//sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<c>");
		ITreeNode[] roots = getRoots();
		create(sb,roots);
		sb.append("</c>");
		// close();
		writer.print(sb.toString());
	}

	private void create(StringBuffer sb, ITreeNode[] roots) {
		if ( roots==null ) return;
		//TODO encode text !!!!
		for (ITreeNode e : roots) {
			sb.append("<n ");
			sb.append("id=\"").append(e.getId()).append("\" title=\"").append(e.getTitle()).append("\" leaf=\"").append(!e.hasChildren());
			sb.append("\">");

			ITreeNode[] children = getChildren(e.getId());
			create(sb,children);
			
			sb.append("</n>");
		}
	}
	
}
