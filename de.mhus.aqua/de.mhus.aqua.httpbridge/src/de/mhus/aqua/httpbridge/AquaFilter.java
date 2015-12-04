package de.mhus.aqua.httpbridge;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import de.mhus.aqua.api.IRequestProcessor;
import de.mhus.lib.logging.Log;

public class AquaFilter extends javax.servlet.http.HttpServlet {

	private static Log log = Log.getLog(AquaFilter.class);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -216206236161471161L;
	private IRequestProcessor service;

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

//		Request request = new Request(req,resp);
//		
//		try {
//			service.processDeteteRequest(request);
//		} catch (Exception e) {
//			log.d(e);
//		}
		super.doDelete(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
				
		try {
			Request request = new Request(req,resp);
			service.processRequest(request);
		} catch (Exception e) {
			log.d(req.getRequestURI(),e);
		}
	}

	@Override
	protected void doHead(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		try {
			Request request = new Request(req,resp);
			service.processHeadRequest(request);
		} catch (Exception e) {
			log.d(e);
		}
		
	}

	@Override
	protected void doOptions(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.doOptions(arg0, arg1);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		
		try {
			Request request = new Request(req,resp);
			service.processRequest(request);
		} catch (Exception e) {
			log.d(e);
		}

	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.doPut(req, resp);
	}

	@Override
	protected void doTrace(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.doTrace(arg0, arg1);
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		BundleContext context = Activator.getContext();
		ServiceReference reference = context.getServiceReference(IRequestProcessor.class.getName());
		service = (IRequestProcessor) context.getService(reference);

	}

}
