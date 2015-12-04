package de.mhus.aqua.mod.uiapp.wui;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import de.mhus.aqua.api.AquaRequest;
import de.mhus.aqua.mod.uiapp.Activator;
import de.mhus.lib.MException;
import de.mhus.lib.config.IConfig;

/**
 * Web Ui Container to store other Components (Boxes). The Components are
 * stored in different lists. In this way it's possible to have different
 * sets of boxes.
 * 
 * @author mikehummel
 *
 */
public abstract class IWUiContainer extends IWComponent implements WExternal {

	private static de.mhus.lib.logging.Log log = de.mhus.lib.logging.Log
			.getLog(IWUiContainer.class);

	public static final String DEFAULT = "";
	private LinkedList<IWComponent> resChildren = new LinkedList<IWComponent>();
	private HashMap<String, LinkedList<IWComponent>> children = new HashMap<String, LinkedList<IWComponent>>();

	protected IConfig config;
	
	@Override
	public void initWElement(AquaRequest request, String id, IConfig config) throws MException {
		this.config = config;
		setId(id);
		doInit();
	}

	protected abstract void doInit() throws MException;

	/**
	 * Add a child to a list of children on the end of the list. List names are free to use. Use null value
	 * to add to the default list.
	 * 
	 * @param listName
	 * @param child
	 */
	public void addChild(String listName, int pos, IWComponent child) {
		if (listName==null) listName=DEFAULT;
		synchronized (this) {			
			resChildren.add(child);
			LinkedList<IWComponent> list = children.get(listName);
			if (list == null) {
				list = new LinkedList<IWComponent>();
				children.put(listName, list);
			}
			if (list.size()<=pos)
				list.add(child);
			else
				list.add(pos, child);
		}
		
	}
	
	public void addChild(String listName, IWComponent child) {
		if (listName==null) listName=DEFAULT;
		synchronized (this) {			
			resChildren.add(child);
			LinkedList<IWComponent> list = children.get(listName);
			if (list == null) {
				list = new LinkedList<IWComponent>();
				children.put(listName, list);
			}
			list.add(child);
		}
	}
	
	/**
	 * Clear all lists. The container is empty after this.
	 */
	public void clear() {
		synchronized (this) {
			resChildren.clear();
			children.clear();
		}
	}

	/**
	 * Removes a specified list from the container.
	 * @param listName name of the list. Use null value to remove the default list
	 */
	public void clear(String listName) {
		if (listName==null) listName=DEFAULT;
		synchronized (this) {
			LinkedList<IWComponent> list = children.get(listName);
			if (list==null) return;
			for (IWComponent c : list)
				resChildren.remove(c);
			children.remove(listName);
		}
	}
	
	/*
	 * Paint the default list.
	 * 
	 * @see de.mhus.mod.wui.WComponent#paint(de.mhus.mod.aqua.core.AquaRequest, java.io.PrintWriter)
	 */
	public void paint(AquaRequest data, PrintWriter stream) throws MException {
		paint(data, DEFAULT, stream);
	}
	
	/*
	 * Paint a specified list
	 */
	public void paint(AquaRequest data, String listName, PrintWriter stream) throws MException {

//		if (getId().length()!=0) {
//			stream.print("<div");
//			paintTagAttributes(stream);
//			stream.print(">");
//		}
		synchronized (this) {
			LinkedList<IWComponent> list = children.get(listName);
			if (list!=null) {
				for ( IWComponent comp : list ) {
					comp.paint(data,stream);
				}
			} else {
				log.t("list not found",listName);
			}
		}
//		if (getId().length()!=0) {
//			stream.print("</div>");
//		}
	}
	
	public void getJsRequirements(AquaRequest data, List<WInclude> set) {
		super.getJsRequirements(data,set);
		synchronized (this) {
			for ( IWComponent comp : resChildren ) {
				comp.getJsRequirements(data,set);
			}
		}
	}
	
	public void getJsResRequirements(AquaRequest data, List<Resource> set) {
		super.getJsResRequirements(data,set);
		synchronized (this) {
			for ( IWComponent comp : resChildren ) { //TODO thread save?
				comp.getJsResRequirements(data,set);
			}
		}
	}
	
	public void getCssResRequirements(AquaRequest data, List<Resource> set) {
		super.getCssResRequirements(data,set);
		synchronized (this) {
			for ( IWComponent comp : resChildren ) { //TODO thread save?
				comp.getCssResRequirements(data,set);
			}
		}
	}

	public void getCssRequirements(AquaRequest data, List<WInclude> set) {
		super.getCssRequirements(data,set);
		synchronized (this) {
			for ( IWComponent comp : resChildren ) { //TODO thread save?
				comp.getCssRequirements(data,set);
			}
		}
	}

}
