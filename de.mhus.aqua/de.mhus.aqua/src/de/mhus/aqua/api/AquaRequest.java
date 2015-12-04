package de.mhus.aqua.api;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import de.mhus.aqua.cao.AquaElement;

public abstract class AquaRequest {

	public static final String MIME_XML = "text/xml";
	protected AquaElement node;
	protected AquaSession session;
	protected String path;
	protected String extPath;

	public AquaElement getNode() {
		return node;
	}

	public AquaSession getSession() {
		return session;
	}

	public String getPath() {
		return path;
	}

	public String getExtPath() {
		return extPath;
	}

	public void setExtPath(String in) {
		extPath = in;
	}

	public abstract Object getAttribute(String name);

	public abstract void setAttribute(String name, Object value);
	
	public abstract PrintWriter getWriter() throws IOException;
	
	public String toString() {
		return "{REQUEST: " + getPath() + " | " + getExtPath() + "}";
	}

	public abstract void sendErrorForbidden() throws IOException;

	/**
	 * Return a request parameter.
	 * 
	 * @param string
	 * @return
	 */
	public abstract String getParameter(String name);

	public void setNode(AquaElement node) {
		this.node = node;
	}

	public abstract void sendErrorNotFound() throws IOException;

	public abstract OutputStream getOutputStream() throws IOException;

	// if return true the'not modified' is already send !!!
	public abstract boolean notModifiedSince(long modified);
	
	// set this if the content can be cached as long as possible
	public abstract void markStaticContent();

	public abstract void setContentType(String mime);
	
}
