package de.mhus.aqua.api;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

public class RequestDelegate extends AquaRequest {

	private AquaRequest master;

	public RequestDelegate(AquaRequest master) {
		this.master = master;
		this.path = master.path;
		this.extPath = master.extPath;
		this.node = master.node;
		this.session = master.getSession();
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	@Override
	public Object getAttribute(String name) {
		return master.getAttribute(name);
	}

	@Override
	public void setAttribute(String name, Object value) {
		master.setAttribute(name, value);
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		return master.getWriter();
	}

	@Override
	public void sendErrorForbidden() throws IOException {
		master.sendErrorForbidden();
	}

	@Override
	public String getParameter(String name) {
		return master.getParameter(name);
	}

	@Override
	public void sendErrorNotFound() throws IOException {
		master.sendErrorNotFound();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return master.getOutputStream();
	}

	@Override
	public boolean notModifiedSince(long modified) {
		return master.notModifiedSince(modified);
	}

	@Override
	public void markStaticContent() {
		master.markStaticContent();
	}
	
	public String toString() {
		return super.toString() + ":" + master.toString();
	}

	@Override
	public void setContentType(String mime) {
		master.setContentType(mime);
	}

}
