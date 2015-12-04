package de.mhus.app.web.filebrowser.api;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;

import javax.servlet.http.HttpServletRequest;

import de.mhus.lib.MString;
import de.mhus.lib.util.Rfc1738;

public abstract class FileBrowserNode {

	protected HttpServletRequest req;

	public abstract String getTitle();
	public abstract String getPath();
	public abstract long getLength();
	public abstract String getName();
	public abstract boolean isFile();
	public abstract void fillBreadcrumb(LinkedList<FileBrowserNode> breadcrumb);
	
	public void setRequest(HttpServletRequest req) {
		this.req = req;
	}
	
	public abstract String toLink();

	public String toLinkUrl() {
		String link = toLink();
		try {
			link = URLEncoder.encode(link,MString.CHARSET_ISO_8859_1);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		link = link.replaceAll("%2F", "/");
		link = link.replaceAll("\\+", "%20");
		return link;
	}
	
}
