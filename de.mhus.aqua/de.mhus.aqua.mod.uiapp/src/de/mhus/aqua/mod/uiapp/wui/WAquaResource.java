package de.mhus.aqua.mod.uiapp.wui;

import de.mhus.aqua.res.AquaRes;

/** 
 * Handles a resource out of the Auqa framework. It ask the resource how the path should be.
 * 
 * @author mikehummel
 *
 */
public class WAquaResource extends Resource {

	private AquaRes res;
	private String path;

	public WAquaResource(AquaRes res) {
		this(res,null);
	}
	
	public WAquaResource(AquaRes res, String path) {
		if (path==null) path="default";
		this.res = res;
		this.path = path;
	}
	
	public String toString() {
		return res.getPathFor(path); // TODO cache ...
	}
	
}
