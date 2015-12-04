package de.mhus.aqua.httpbridge;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.mhus.aqua.api.AquaRequest;
import de.mhus.aqua.api.AquaSession;
import de.mhus.lib.logging.Log;

public class Request extends AquaRequest {

	private static Log log = Log.getLog(Request.class);
	private HttpServletRequest request;
	private HttpServletResponse response;

	public Request(HttpServletRequest req, HttpServletResponse resp) throws Exception {
		this.request = req;
		this.response = resp;
		
		String uri = ((HttpServletRequest)request).getRequestURI();
		
		// remove # anchor part
		if (uri.indexOf('#')>0)
			uri = uri.substring(0,uri.indexOf('#'));
		
		String[] parts = getSplitPath(uri);
		path = parts[0];
		setExtPath(parts[1]);
		
		// get Session
		session = (AquaSession) request.getSession().getAttribute(Activator.ATTRIBUTE_SESSION);
		if (session == null) {
			log.t("create session", request.getSession().getId());
			session = new Session(request.getSession());
			request.getSession().setAttribute(Activator.ATTRIBUTE_SESSION, session);
		}
		
	}

	@Override
	public Object getAttribute(String name) {
		return request.getAttribute(name);
	}

	@Override
	public void setAttribute(String name, Object value) {
		request.setAttribute(name, value);
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		return response.getWriter();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return response.getOutputStream();
	}
	
	public static String[] getSplitPath(String path) {
		if (path.endsWith("/")) path = path.substring(0,path.length()-1);
		String ext  = null;
		int pos = path.indexOf("/_");
		if (pos >= 0) {
			ext  = path.substring(pos+2);
			path = path.substring(0, pos);
		}
		return new String[] {path,ext};
	}

	@Override
	public void sendErrorForbidden() throws IOException {
		response.sendError(HttpServletResponse.SC_FORBIDDEN);
	}

	@Override
	public String getParameter(String name) {
		return request.getParameter(name);
	}

	@Override
	public void sendErrorNotFound() throws IOException {
		response.sendError(HttpServletResponse.SC_NOT_FOUND);
	}
	
	public String toString() {
		StringBuffer out = new StringBuffer().append('{').append(super.toString()).append(',').append(request.getRequestURL().toString());
		for (Enumeration<?> enu = request.getParameterNames();enu.hasMoreElements();) {
			String key = (String) enu.nextElement();
			out.append(',').append(key).append('=').append(request.getParameter(key));
		}
		out.append('}');
		return out.toString();
	}

	@Override
	public boolean notModifiedSince(long modified) {
		//TODO check if-modified-since and send not modified
		return false;
	}

	@Override
	public void markStaticContent() {
		//TODO check if header can be written and set pragma, cache etc. ....
	}

	@Override
	public void setContentType(String mime) {
		response.setContentType(mime);
	}
	
}
