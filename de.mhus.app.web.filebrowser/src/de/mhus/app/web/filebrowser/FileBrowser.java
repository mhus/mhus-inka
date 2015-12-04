package de.mhus.app.web.filebrowser;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.GenericServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;

import de.mhus.app.web.filebrowser.api.FileBrowserAction;
import de.mhus.app.web.filebrowser.api.FileBrowserContext;
import de.mhus.app.web.filebrowser.api.FileBrowserNode;
import de.mhus.app.web.filebrowser.api.FileBrowserPlug;
import de.mhus.app.web.filebrowser.api.ProvideInputStream;
import de.mhus.app.web.filebrowser.api.Space;
import de.mhus.app.web.filebrowser.plug.fs.FilePlug;
import de.mhus.app.web.filebrowser.plug.fs.FileSpace;
import de.mhus.app.web.filebrowser.plug.fs.OpenAction;
import de.mhus.app.web.filebrowser.vm.ActionDefinition;
import de.mhus.lib.MCast;
import de.mhus.lib.MString;
import de.mhus.lib.logging.Log;

public class FileBrowser extends HttpServlet implements LogChute, FileBrowserContext {

	private static final String DEFAULT_TEMPLATES = "bootstrap";

	static Log log = Log.getLog(FileBrowser.class);
	
	HashMap<String, Space> spaces = new HashMap<String, Space>();
	HashMap<String, FileBrowserAction> actions = new HashMap<String, FileBrowserAction>();
	HashMap<String, Class<? extends Space>> spaceTypes = new HashMap<String, Class<? extends Space>>();
	LinkedList<FileBrowserPlug> plugs = new LinkedList<FileBrowserPlug>();
	
	private FileSpace internalSpace;
	private OpenAction internalOpen;

	//private Template contentTemplate;

	private VelocityEngine velocityEngine;

	private boolean logVelocity;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		configure();
//		contentTemplate = velocityEngine.getTemplate("content.vm");
	}
	
	private void configure() throws ServletException {
		
		//TODO close all before!
		spaces.clear(); 
		plugs.clear(); 
		actions.clear();
		spaceTypes.clear();
		
		Properties properties = new Properties();
		de.mhus.lib.MSystem.loadProperties(this, properties, "filebrowser.properties");
		
		String spaceNames = properties.getProperty("spaces");
		if (MString.isEmpty(spaceNames)) {
			log.w("spaces not found");
			return;
		}
		
		
		internalSpace = new FileSpace(new File(getServletContext().getRealPath(DEFAULT_TEMPLATES)), "Internal" );
		String contentPath = properties.getProperty("content");
		if (contentPath != null) {
			if (contentPath.startsWith("@"))
				contentPath = getServletContext().getRealPath(contentPath.substring(1));
			internalSpace.setPath( new File(contentPath) );
		}
		internalOpen = new OpenAction();

		// init plugs
		registerPlug(new FilePlug());
		
		logVelocity = MCast.toboolean(properties.getProperty("velocity.log"), false );
		
		for ( String spaceName : MString.split(spaceNames, ",") ) {
			
			try {
				Properties spaceProps = new Properties();
				for (Object keyO : properties.keySet()) {
					String key = (String)keyO;
					if (key.startsWith(spaceName + ".")) {
						spaceProps.put(key.substring(spaceName.length() + 1), properties.get(key));
					}
				}
				
				String type = properties.getProperty(spaceName + ".type");
				
				Space space = createSpace(type,spaceName, spaceProps);
				
				log.i("space",spaceName, type, space);
				spaces.put(spaceName, space);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		
		
		Properties vmProp = new Properties();
		vmProp.put("resource.loader", "file");
		vmProp.put("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
		vmProp.put("file.resource.loader.path", internalSpace.getNode("templates").getPath() );
		
		velocityEngine = new VelocityEngine(vmProp);
		velocityEngine.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM, this);
		velocityEngine.init();
//		velocityEngine.setProperty("esc", new org.apache.velocity.tools.generic.EscapeTool());
	}
	
	private Space createSpace(String typeName, String spaceName,
			Properties spaceProps) throws ServletException {
		
		log.i("createspace",spaceName, typeName, spaceProps);

		if (typeName == null) typeName = "fs";
		Class<? extends Space> type = spaceTypes.get(typeName);
		if (type == null) {
			throw new ServletException("space type unknown " + typeName);
		}
		Space space;
		try {
			space = type.newInstance();
		} catch (Exception e) {
			throw new ServletException(e);
		}
		space.initialize(spaceName, spaceProps);
		return space;
	}



	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		
		String path = req.getPathInfo();
		log.i("request",path);
		
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		
		if (path.startsWith("~/")) {
			// internal path
			if (path.startsWith("~/templates"))
				throw new ServletException("not found");
			if (path.startsWith("~/configure")) {
				configure();
				res.getOutputStream().print("OK " + System.currentTimeMillis());
				return;
			}
			FileBrowserNode node = internalSpace.getNode(path.substring(2));
			if (node == null) {
				res.setStatus(404);
				return;
			}
			internalOpen.doExecute(node, getServletContext(), res);
			return;
		}
		

		LinkedList<FileBrowserNode> list = new LinkedList<FileBrowserNode>();
		LinkedList<FileBrowserNode> breadcrumb = new LinkedList<FileBrowserNode>();
		
		FileBrowserNode node = null;
		Space space = null;
		if (MString.isEmpty(path)) {
			list.addAll(spaces.values());
		} else {
			String spaceName = "";
			if (MString.isIndex(path, '/')) {
				spaceName = MString.beforeIndex(path, '/');
				path = MString.afterIndex(path, '/');
			} else {
				spaceName = path;
				path = "";
			}
			
			space = spaces.get(spaceName);
			if (space == null)
				throw new ServletException("Space not found " + spaceName);
			
			node = space.getNode(path);
			if (node == null)
				throw new ServletException("Path not found " + path);
			
				
		}
		
		if (node != null) {
			String actionName = req.getParameter("action");
			if (actionName != null) {
				FileBrowserAction action = actions.get(actionName);
				if (action != null) {
					if (action.canExecute(node)) {
						if (!action.doExecute(node, getServletContext(), res))
							return;
					}
				}
			}
		}
		
		if (space != null) space.fillList(path, list);
		if (node != null) node.fillBreadcrumb(breadcrumb);
		
		// view
		String viewName = req.getParameter("view");
		if (MString.countCharacters(viewName, new char[] {'.', '/', '\\', '~', '\'', '"'}) > 0)
			viewName = null; // for security reasons
		if (viewName == null) {
			if (space == null)
				viewName = "spaces";
			else
			if (node != null && node.isFile())
				viewName = "file";
			else
				viewName = "content";
			
		}

		generateDirOutput(viewName, node, list, breadcrumb, req, res);
	}

	private void generateDirOutput(String viewName, FileBrowserNode node, LinkedList<FileBrowserNode> list, LinkedList<FileBrowserNode> breadcrumb, HttpServletRequest req,
			HttpServletResponse res) throws IOException, ServletException {

		res.setContentType("text/html");
		ServletOutputStream os = res.getOutputStream();
		OutputStreamWriter writer = new OutputStreamWriter(os);
		
		if (node != null) node.setRequest(req);
		if (list != null) {
			for (FileBrowserNode n : list)
				if (n != null) n.setRequest(req);
		}
		if (breadcrumb != null) {
			for (FileBrowserNode n : breadcrumb)
				if (n != null) n.setRequest(req);
		}
		
		LinkedList<ActionDefinition> fActions = new LinkedList<ActionDefinition>();
		for (Map.Entry<String,FileBrowserAction> entry : actions.entrySet() ) {
			if (entry.getValue().canExecute(node)) {
				fActions.add(new ActionDefinition( entry.getKey(), entry.getValue()));
			}
		}

		VelocityContext context = new VelocityContext();
		
		context.put("root", req.getContextPath());

		if (list != null) context.put("list", list);
		
		if (node != null) context.put("node", node);
		if (node != null) context.put("backlink", toBackLink(req,node));
		if (breadcrumb != null) context.put("breadcrumb", breadcrumb);
		context.put("actions", fActions);
		
		context.put("esc",new org.apache.velocity.tools.generic.EscapeTool());

		
		Template contentTemplate = velocityEngine.getTemplate(viewName + ".vm");
		contentTemplate.merge(context, writer);
		writer.close();
		os.close();
        
	}

	private String toBackLink(HttpServletRequest req, FileBrowserNode parent) {
		String path = req.getPathInfo();
		if (path.endsWith("/"))
			path = MString.beforeLastIndex(path, '/');
		path = MString.beforeLastIndex(path, '/');
		return req.getContextPath() + path;
	}
	


	@Override
	public void init(RuntimeServices arg0) throws Exception {
		
	}
	
	
	
	@Override
	public boolean isLevelEnabled(int arg0) {
		if (!logVelocity) return false;
		return true;
	}
	
	
	
	@Override
	public void log(int arg0, String arg1) {
		if (!logVelocity) return;
		log.i("ve",arg0,arg1);
	}
	
	
	
	@Override
	public void log(int arg0, String arg1, Throwable arg2) {
		if (!logVelocity) return;
		log.i("ve",arg0,arg1,arg2);
	}



	@Override
	public void registerAction(FileBrowserAction action) {
		actions.put(action.toString(), action);
	}

	@Override
	public void unregisterAction(FileBrowserAction action) {
		actions.remove(action.toString() );
	}

	@Override
	public void registerSpaceType(String name, Class<? extends Space> clazz) {
		spaceTypes.put(name, clazz);
	}

	@Override
	public void unregisterSpaceType(String name) {
		spaceTypes.remove(name);
	}

	public void registerPlug(FileBrowserPlug plug) {
		plug.start(this);
		plugs.add(plug);
	}

	public void unregisterPlug(FileBrowserPlug plug) {
		plugs.remove(plug);
		plug.stop(this);
	}

}
