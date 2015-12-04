package de.mhus.aqua.mod.uiapp.wui;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import de.mhus.aqua.api.AquaRequest;
import de.mhus.lib.MException;
import de.mhus.lib.cao.CaoException;

public class IWComponent {

	private String id;
	protected String cssClass = null;
	private LinkedList<Resource> cssRes = new LinkedList<Resource>();
	private LinkedList<WInclude> css = new LinkedList<WInclude>();
	private LinkedList<Resource> jsRes = new LinkedList<Resource>();
	private LinkedList<WInclude> js = new LinkedList<WInclude>();
	protected WNls nls;
	
	public IWComponent() {
		this(null);
	}
	
	public IWComponent(String id) {
		setId(id);
	}
	
	public void setNls(WNls nls) {
		this.nls = nls;
	}
	
	public WNls getNls() {
		return nls;
	}

	public synchronized void setId(String id) {
		if (id==null)
			id = createId();
		this.id = id.replace('-', '_');
	}
	
	public static String createId() {
		return "i" + UUID.randomUUID().toString();
	}

	/**
	 * Paint the html component. Do not call the super. This will
	 * paint/print a dummy command with the UUID.
	 * 
	 * @param stream
	 * @throws CaoException 
	 * @throws MException 
	 */
	public void paint(AquaRequest data, PrintWriter stream) throws MException {
		stream.print("<!-- WC:");
		stream.print(this.getClass().getCanonicalName());
		paintTagAttributes(stream);
		stream.print(" -->");
	}
	
	protected void paintTagAttributes(PrintWriter stream) {
		stream.print(" id=\"" + id + "\"");
		if (cssClass != null)
			stream.print(" class=\"" + cssClass + "\"");
	}
	
	public String getId() {
		if (id==null) setId(null);
		return id;
	}
	
	public String getCssClass() {
		return cssClass;
	}
	
	public void setCssClass(String in) {
		cssClass = in;
	}
	
	public void clearJsResources() {
		jsRes.clear();
	}
	
	public void clearJs() {
		js.clear();
	}
	
	public void clearCssResources() {
		cssRes.clear();
	}
	
	public void clearCss() {
		css.clear();
	}
	
	public void addJsResources(Resource res) {
		jsRes.add(res);
	}
	
	public void addJs(WInclude res) {
		js.add(res);
	}
	
	public void addCssResources(Resource res) {
		cssRes.add(res);
	}
	
	public void addCss(WInclude res) {
		css.add(res);
	}
	
	public void getJsRequirements(AquaRequest data, List<WInclude> set) {
		if (js!=null) for(WInclude r : js) set.add(r);
	}

	public void getJsResRequirements(AquaRequest data, List<Resource> set) {
		if (jsRes!=null) for(Resource r : jsRes) set.add(r);
	}
	
	public void getCssResRequirements(AquaRequest data, List<Resource> set) {
		if (cssRes!=null) for(Resource r : cssRes) set.add(r);
	}

	public void getCssRequirements(AquaRequest data, List<WInclude> set) {
		if (css!=null) for(WInclude r : css) set.add(r);
	}

	public void close() {
	}

}
