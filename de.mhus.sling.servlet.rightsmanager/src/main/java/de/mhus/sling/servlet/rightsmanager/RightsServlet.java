package de.mhus.sling.servlet.rightsmanager;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.security.Principal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.security.AccessControlEntry;
import javax.jcr.security.AccessControlList;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.AccessControlPolicy;
import javax.jcr.security.Privilege;
import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlEntry;
import org.apache.jackrabbit.api.security.principal.PrincipalManager;
import org.apache.jackrabbit.value.StringValue;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * http://localhost:4502/content/mhus/mig.html
 * 
 * @author mikehummel
 * 
 */
@Component(immediate = true, metatype = true, label = "RightsManagerServlet")
@Service(javax.servlet.Servlet.class)
@Properties({
		@Property(name = "sling.servlet.paths", value = { "sling/servlet/default/rights.GET", "sling/servlet/default/rights.POST" } )
//		@Property(name = "sling.servlet.resourceTypes", value = { "cq:Page", "sling:Folder", "sling:OrderedFolder", "nt:folder" }),
//		@Property(name = "sling.servlet.extensions", value = "rig"),
//		@Property(name = "sling.servlet.methods", value = { "GET", "POST" }) 
		})
public class RightsServlet extends SlingSafeMethodsServlet {

	private static final long serialVersionUID = 1L;
	private static Logger log = LoggerFactory.getLogger(RightsServlet.class);
	private String ext = "rights";
//	private String[] resourceTypes = { "cq:Page", "sling:Folder", "sling:OrderedFolder", "nt:folder" };

	// private ResourceResolver resolver;

	
	protected boolean mayService(SlingHttpServletRequest request,
			SlingHttpServletResponse response) throws ServletException,
			IOException {

		String method = request.getMethod();
		if (HttpConstants.METHOD_POST.equals(method)) {
			doPost(request, response);
			return true;
		}
		return super.mayService(request, response);
	}

	protected void doPost(SlingHttpServletRequest request,
			SlingHttpServletResponse response) throws ServletException,
			IOException {

		doGet(request, response);

	}

	protected void doGet(SlingHttpServletRequest request,
			SlingHttpServletResponse response) throws ServletException,
			IOException {

		boolean outputQuiet = false;
		boolean outputJson  = false;
		boolean createGroups = false;
		
		for (String selector : request.getRequestPathInfo().getSelectors()) {
			if ("quiet".equals(selector))
				outputQuiet = true;
			else
			if ("json".equals(selector))
				outputJson = true;
		}
		
		ResourceResolver resolver = request.getResourceResolver();
		
		JackrabbitSession session = (JackrabbitSession) resolver
				.adaptTo(Session.class);
		Resource res = request.getResource();

		if (res == null) {
			response.sendError(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			PrintWriter out = response.getWriter();
			response.setContentType("text/html");
			out.write("<html><body>");
			out.write("<h1>not found</h1>");
			return;
		}

		String path = res.getPath();
		// actions

		boolean doSave = false;
		String actOut = "";
		
		RequestParameter act = request.getRequestParameter("act");
		RequestParameter create = request.getRequestParameter("create");
		if (create != null) createGroups = "1".equals(create.getString());
		
		if (act != null && act.getString().trim().length() != 0) {

			java.util.Properties p = new java.util.Properties();
			p.load(new StringReader(act.getString()));

			String act2 = p.getProperty("act");

			if (act2 != null) {
				if (act2.equals("clr")) {
					try {
						AccessControlManager acm = session
								.getAccessControlManager();
						for (AccessControlPolicy policy : acm
								.getPolicies(path)) {
							acm.removePolicy(path, policy);
						}
						doSave = true;
					} catch (Throwable t) {
						error(response, t);
						return;
					}
					actOut = actOut + "[clr]";
				}
			}

			int nr = 0;
			while (p.containsKey("pid_" + nr)) {
				String principalId = "";
				try {
					String prefix = nr + "_";
					principalId = p.getProperty("pid_" + nr);
					
					// ignore groups with slashes
					if (principalId != null && principalId.indexOf('/') > 0 ) {
						nr++;
						continue;
					}
					
//					JackrabbitAccessControlManager acm = (JackrabbitAccessControlManager) session
//							.getAccessControlManager();
					PrincipalManager pm = session.getPrincipalManager();
					Principal principal = pm.getPrincipal(principalId);
					if (principal == null) {
						if (createGroups)
							principal = session.getUserManager().createGroup(principalId).getPrincipal();
						else {
							actOut = actOut + "[" + nr + ":nogroup:" + principalId+"]";
							nr++;
							continue;
						}
					}
					
					LinkedList<String> allowList = new LinkedList<String>();
					LinkedList<String> denyList = new LinkedList<String>();
					HashMap<String, Value> restrictions = null;
					
					for (Object attrName2obj : p.keySet()) {
						String attrName2 = (String) attrName2obj;
						if (attrName2.startsWith(prefix)) {

							String privilege = attrName2.substring(prefix
									.length());
							
							if ("restrictions".equals(privilege)) {
								restrictions = new HashMap<String, Value>();
								restrictions.put("rep:glob",new StringValue(p.getProperty(attrName2)));
						} else {
							
								boolean allow = p.getProperty(attrName2)
										.equals("1");

								if (allow)
									allowList.add(mapPrivilege(privilege));
								else
									denyList.add(mapPrivilege(privilege));
							}
						}
					}

					if (denyList.size() > 0) {
						addAccessControlEntry(
										session,
										path,
										principal,
										denyList.toArray(new String[denyList
												.size()]), false,restrictions);
					}
					if (allowList.size() > 0) {
						addAccessControlEntry(session,
								path, principal, allowList
										.toArray(new String[allowList
												.size()]), true,restrictions);
					}

					doSave = true;
					actOut = actOut + "[" + nr + ":ok:" + principalId+"]";

				} catch (Throwable t) {
					// error(response, t);
					// return;
					log.error(path + ": pid_" + nr + "=" + principalId,t);
				}
				nr++;
				
			}

			if (doSave) {
				try {
					session.save();
				} catch (Exception e) {
					error(response, e);
					return;
				}
			} else {
				try {
					session.refresh(false);
				} catch (Exception e) {
					error(response, e);
					return;
				}
			}
		}

		if (outputQuiet) {
			response.setContentType("text/plain");
			PrintWriter out = response.getWriter();
			out.write(actOut + "\n");
			return;
		}
		
		if (outputJson) {
			response.setContentType("application/json");
			PrintWriter out = response.getWriter();
			
			out.write("{");
			if (actOut.length() > 0 ) out.write("\"action\":\"" + actOut + "\",");
			out.write("\"policies\": [");
			
			try {
				AccessControlManager acm = session.getAccessControlManager();
				writePolicyJson(acm.getPolicies(path), out);
			} catch (Throwable t) {
				info(response, t);
			}

			out.write("], \"effective\" : [");

			try {
				AccessControlManager acm = session.getAccessControlManager();
				writePolicyJson(acm.getEffectivePolicies(path), out);
			} catch (Throwable t) {
				info(response, t);
			}

			out.write("]}");
			
			return;
			
		}
		
		// display
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.write("<html><head>");
		writeCSS(out);
		out.write("</head><body>");
		out.write("<h1>");
		out.write(path);
		out.write("</h1>");

		out.write("<p>User: " + request.getUserPrincipal().getName());
		if (actOut.length() > 0 ) out.write("<br/>Action: " + actOut);

		out.write("</p><h2>Policies</h2>");

		try {
			AccessControlManager acm = session.getAccessControlManager();
			writePolicy(acm.getPolicies(path), out);
		} catch (Throwable t) {
			info(response, t);
		}

		out.write("<h2>Effective Policies</h2>");

		try {
			AccessControlManager acm = session.getAccessControlManager();
			writePolicy(acm.getEffectivePolicies(path), out);
		} catch (Throwable t) {
			info(response, t);
		}

		out.write("<h2>Navigation</h2>");
		out.write("<table border=1 width=100%>");

		if (!path.equals("/") && path.indexOf("/") >= 0) {
			String subPath = request.getRequestPathInfo().getResourcePath();
			int p = subPath.lastIndexOf("/");
			out.write("<tr><td colspan=2><a href=\""
					+ subPath.substring(0,p) + "." + ext +"\">..</a></td></tr>\n");
		}
		for (Iterator<Resource> iter = res.listChildren(); iter.hasNext();) {
			Resource child = iter.next();
			
			if (isHandledResourceTyep(child.getResourceType())) {
				String subPath = child.getPath();
				out.write("<tr><td><a href=\""
						+ subPath + "." + ext +"\">" + child.getName() + "</a></td>");
				out.write("<td>" + child.getResourceType() + "</td></tr>\n");
			} else {
				out.write("<tr><td>" + child.getName() + "</td><td>"+ child.getResourceType() + "</td></tr>\n");
			}
		}

		out.write("</table>");

		out.write("<h2>Set Privileges</h2><form action=\"#\" method=post>");
		out.write("<textarea name=act cols=50 rows=10></textarea><br/>");
		out.write("<input type=submit></form>");

		out.write("Example:<br/><pre>act=clr\npid_0=admin\n0_read=1\n0_write=1\nRights:\nall,read,write,replicate,\nrep_write,versionManagement,\nmodifyAccessControl,readAccessControl,lockManagement,removeNode,delete,nodeTypeManagement,addChildNodes,removeChildNodes</pre>");

		// out.write("<>");
		out.write("</body></html>");

	}

	public boolean isHandledResourceTyep(String resourceType) {
//		for (String type : resourceTypes)
//			if (type.equals(resourceType)) return true;
//		return false;
		return true;
	}

	private void writeCSS(PrintWriter out) {
		out.write("<style type=\"text/css\">");
		out.write("body {font-family: Verdana, Arial, Helvetica, sans-serif;font-size: 10pt;color: #000;background-color:#fff}");
		out.write("p {margin: 0 0 10px 0;}");
		out.write("table {width: 100%;margin: 0 0 10px 0;border-width: 1px 0 0 1px;border-color: #666;border-style: solid}");
		out.write("table td {padding: 4px 4px 4px 4px;vertical-align: top;border-width: 0 1px 1px 0;border-color: #666;border-style: solid}");
		out.write("h1 {padding: 2px;margin: 0 0 10px 0;background-color: #eee}");
		out.write("h2 {padding: 2px;margin: 0 0 10px 0;border-left: 11px solid #5a5a5a;background-color: #eee}");
		out.write("ol {list-style: decimal outside;margin-left: 0px;padding-left: 33px;padding-bottom:0px}");
		out.write("ol li {white-space:nowrap;margin:0 0 1px 0;padding:2px 2px 2px 7px;vertical-align: text-bottom;clear:both; background-color:#ddd}");
		out.write("ol li.allow {background-color:#dfd}");
		out.write("ol li.deny {background-color:#fdd}");
		out.write("</style>");
	}

	private void addAccessControlEntry(JackrabbitSession session,
			String suffix, Principal principal, String[] allowList, boolean allow,
			HashMap<String, Value> restrictions) throws RepositoryException {

//		AccessControlManager acm = session.getAccessControlManager();
//		acm.setPolicy(absPath, policy)
		
		AccessControlUtils.addAccessControlEntry(session,
				suffix, principal, allowList, allow,restrictions);

	}

	private void info(SlingHttpServletResponse response, Throwable t) throws IOException {
		PrintWriter out = response.getWriter();
		log.info("",t);
		out.write("<p>"+t.toString()+"</p>");
	}

	private void error(SlingHttpServletResponse response, Throwable t) {
		log.error("",t);
		try {
			response.sendError(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (IOException e) {
		}

		try {
			PrintWriter out = response.getWriter();
			out.write("<html><body>");
			out.write("<h1>"+t.toString()+"</h1><pre>");
			t.printStackTrace(out);
			out.write("</pre></body></html>");
		} catch (IOException e) {
		}
	}

	private String mapPrivilege(String privilege) {
		privilege = privilege.trim();
		if (privilege.equals("all"))
			return Privilege.JCR_ALL;
		if (privilege.equals("read"))
			return Privilege.JCR_READ;
		if (privilege.equals("rep_write"))
			return "rep:write";
		if (privilege.equals("write"))
			return Privilege.JCR_WRITE;
		if (privilege.equals("replicate"))
			return "crx:replicate";
		if (privilege.equals("versionManagement"))
			return "jcr:versionManagement";
		if (privilege.equals("modifyAccessControl"))
			return "jcr:modifyAccessControl";
		if (privilege.equals("readAccessControl"))
			return "jcr:readAccessControl";
		if (privilege.equals("lockManagement"))
			return "jcr:lockManagement";
		if (privilege.equals("removeNode"))
			return "jcr:removeNode";
		if (privilege.equals("delete"))
			return "jcr:removeNode";
		if (privilege.equals("nodeTypeManagement"))
			return "jcr:nodeTypeManagement";
		if (privilege.equals("addChildNodes"))
			return "jcr:addChildNodes";
		if (privilege.equals("removeChildNodes"))
			return "jcr:removeChildNodes";

		return privilege;
	}

	void writePolicy(AccessControlPolicy[] policies, PrintWriter out) {
		try {
			for (AccessControlPolicy policy : policies) {
				AccessControlList acl = (AccessControlList) policy;
				// AbstractACLTemplate jrAcl = (AbstractACLTemplate)acl;

				// out.write("<b>List</b> "+jrAcl.getPath()+"<ol>" );
				out.write("<b>List</b><ol>");
				for (AccessControlEntry entry : acl.getAccessControlEntries()) {
					JackrabbitAccessControlEntry jrEntry = (JackrabbitAccessControlEntry) entry;

					out.write("<li class=\"" + (jrEntry.isAllow() ? "allow" : "deny")+ "\"><b>" + jrEntry.getPrincipal().getName()
							+ "</b> " + (jrEntry.isAllow() ? "Allow" : "Deny")
							+ "<i>");
					for (String restrictionName : jrEntry.getRestrictionNames()) {
						String restriction = jrEntry.getRestriction(
								restrictionName).getString();
						out.write(" " + restrictionName + "=" + restriction);
					}
					out.write("</i>");

					for (Privilege privilege : entry.getPrivileges()) {
						out.write(" " + privilege.getName());
					}
					out.write("</li>");
				}
				out.write("</ol><br/>");
			}
		} catch (Throwable t) {
			log.error("", t);
		}
	}
	
	void writePolicyJson(AccessControlPolicy[] policies, PrintWriter out) {
		try {
			boolean first0 = true;
			for (AccessControlPolicy policy : policies) {
				AccessControlList acl = (AccessControlList) policy;
				// AbstractACLTemplate jrAcl = (AbstractACLTemplate)acl;

				if (first0) first0 = false; else out.write(",");
				out.write("[");
				// out.write("<b>List</b> "+jrAcl.getPath()+"<ol>" );
				boolean first1 = true;
				for (AccessControlEntry entry : acl.getAccessControlEntries()) {
					if (first1) first1 = false; else out.write(",");
					out.write("{");
					JackrabbitAccessControlEntry jrEntry = (JackrabbitAccessControlEntry) entry;

					out.write("\"principal\" : \"" + jrEntry.getPrincipal().getName()
							+ "\", \"rule\" : \"" + (jrEntry.isAllow() ? "allow" : "deny")
							+ "\", \"restrictions\" : {");
					
					boolean first2 = true;
					for (String restrictionName : jrEntry.getRestrictionNames()) {
						String restriction = jrEntry.getRestriction(
								restrictionName).getString();
						if (first2) first2 = false; else out.write(",");
						out.write("\"" + restrictionName + "\" : \"" + restriction + "\"");
					}
					out.write("}, \"privileges\" : [");

					first2 = true;
					for (Privilege privilege : entry.getPrivileges()) {
						if (first2) first2 = false; else out.write(",");
						out.write("\"" + privilege.getName() + "\"");
					}
					out.write("]");
					out.write("}");
				}
				out.write("]");
			}
		} catch (Throwable t) {
			log.error("", t);
		}
	}

//	@Override
//	public void init() throws ServletException {
//		super.init();
//		
//		Properties props = getClass().getAnnotation(Properties.class);
//		if (props != null)
//			for (Property prop : props.value()) {
//				if (prop.name().equals("sling.servlet.resourceTypes")) {
//					resourceTypes = prop.value();
//				}
//			}
//		else
//			resourceTypes = new String[] {"cq:Page"};
//	}

}
