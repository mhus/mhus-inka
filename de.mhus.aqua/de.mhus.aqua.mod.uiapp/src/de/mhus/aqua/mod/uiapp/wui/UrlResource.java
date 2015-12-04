package de.mhus.aqua.mod.uiapp.wui;

/**
 * Handles a direct linked resource.
 * 
 * @author mikehummel
 *
 */
public class UrlResource extends Resource {

	private String url;

	public UrlResource(String url) {
		this.url = url;
	}
	
	public String toString() {
		return url;
	}
}
