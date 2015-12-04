/*
 *  Hair2 License
 *
 *  Copyright (C) 2008 Mike Hummel 
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.mhu.hair.wdk;

import java.io.File;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.mhu.hair.api.Api;
import de.mhu.hair.plugin.Plugin;
import de.mhu.hair.plugin.PluginConfig;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.lib.xml.XmlTool;

public class StructurePlugin implements Plugin, Api {

	private File dir;
	private DocumentBuilderFactory dbf;
	private DocumentBuilder builder;
	private File[] dirOrder;

	private Vector actions = new Vector();
	private Vector components = new Vector();
	private PluginNode node;

	public void initPlugin(PluginNode pNode, PluginConfig pConfig)
			throws Exception {

		node = pNode;

		dir = new File(pConfig.getNode().getAttribute("dir"));
		dbf = DocumentBuilderFactory.newInstance();
		builder = dbf.newDocumentBuilder();
		builder.setEntityResolver(new EntityResolver() {

			public InputSource resolveEntity(String publicId, String systemId)
					throws SAXException, IOException {
				// TODO Auto-generated method stub
				return new InputSource(new StringBufferInputStream(""));
			}

		});
		refreshStructure();

		node.addApi(StructurePlugin.class, this);
	}

	private void refreshStructure() throws Exception {

		// clear
		actions.clear();
		components.clear();

		// Load web.xml and find custom name
		String customName = "custom";

		File webXmlFile = new File(dir, "WEB-INF/web.xml");
		Document webXml = readXml(webXmlFile);
		NodeList paramSets = XmlTool.getLocalElements(webXml
				.getDocumentElement(), "context-param");
		for (int i = 0; i < paramSets.getLength(); i++) {
			Element param = (Element) paramSets.item(i);
			String name = XmlTool.getValue(((Element) param
					.getElementsByTagName("param-name").item(0)), true);
			if (name.equals("AppFolderName")) {
				customName = XmlTool.getValue(((Element) param
						.getElementsByTagName("param-value").item(0)), true);
			}
		}

		// load app.xml files and hierarchie
		File working = new File(dir, customName + "/app.xml");
		Vector v = new Vector();
		while (working != null) {
			File parent = working.getParentFile();
			v.add(parent);
			System.out.println(">>> Loading " + parent.getAbsolutePath());
			Document app = readXml(working);

			Element node = XmlTool.getElementByPath(app.getDocumentElement(),
					"scope/application");
			String next = node.getAttribute("extends");
			if (next.equals("")) {
				working = null;
			} else {
				working = new File(dir, next);
			}
		}

		dirOrder = (File[]) v.toArray(new File[v.size()]);

		// parse all xml files
		for (int i = 0; i < dirOrder.length; i++) {
			refreshXmlFiles(new File(dirOrder[i], "config"), dir);
		}

	}

	private void refreshXmlFiles(File file, File root) {

		File[] list = file.listFiles();
		if (list == null)
			return;

		for (int i = 0; i < list.length; i++) {
			String localPath = list[i].getAbsolutePath().substring(
					root.getAbsolutePath().length());
			String name = list[i].getName();
			if (list[i].isDirectory() && !name.startsWith("."))
				refreshXmlFiles(list[i], root);
			else if (list[i].isFile() && name.endsWith(".xml"))
				try {
					refreshXmlFile(list[i], localPath);
				} catch (Exception e) {
					e.printStackTrace();
				}
		}

	}

	private void refreshXmlFile(File file, String localPath) throws Exception {
		System.out.println(">>> Reading XML File: " + file.getAbsolutePath());
		Document dom = readXml(file);
		Element root = (Element) dom.getDocumentElement();
		if (!root.getNodeName().equals("config"))
			throw new Exception("not a config file");

		// scope
		NodeList scopes = XmlTool.getLocalElements(root, "scope");
		for (int s = 0; s < scopes.getLength(); s++) {
			Element scope = (Element) scopes.item(s);

			// scope to string representation
			NamedNodeMap scopeAttrs = scope.getAttributes();
			TreeMap sorted = new TreeMap();
			for (int sa = 0; sa < scopeAttrs.getLength(); sa++) {
				Node attr = scopeAttrs.item(sa);
				sorted.put(attr.getNodeName(), attr.getNodeValue());
			}
			StringBuffer sb = new StringBuffer();
			for (Iterator i = sorted.entrySet().iterator(); i.hasNext();) {
				Map.Entry entry = (Map.Entry) i.next();
				sb.append((String) entry.getKey());
				sb.append('=');
				sb.append((String) entry.getValue());
				sb.append(' ');
			}
			String scopeName = sb.toString();

			// find elements in scope
			scope.getAttributes();
			refreshXmlActions(scope, scopeName, file, localPath);
			refreshXmlComponent(scope, scopeName, file, localPath);
		}
	}

	private void refreshXmlComponent(Element scope, String scopeName,
			File file, String localPath) {
		NodeList componentList = XmlTool.getLocalElements(scope, "component");
		for (int i = 0; i < componentList.getLength(); i++) {
			Element component = (Element) componentList.item(i);
			System.out
					.println("--- Component: " + component.getAttribute("id"));
			components.add(new ComponentContainer(component, scopeName, file,
					localPath));
		}
	}

	private void refreshXmlActions(Element scope, String scopeName, File file,
			String localPath) {
		NodeList actionList = XmlTool.getLocalElements(scope, "action");
		for (int i = 0; i < actionList.getLength(); i++) {
			Element action = (Element) actionList.item(i);
			System.out.println("--- Action: " + action.getAttribute("id"));
			actions
					.add(new ActionContainer(action, scopeName, file, localPath));
		}
	}

	private synchronized Document readXml(File file) throws Exception {

		Document dom = builder.parse(file);
		return dom;

	}

	public void destroyPlugin() throws Exception {

	}

	public ListContainer getComponentsAsList() {
		return new ListContainer(new String[] { "Name", "Scope", "Extends",
				"File" }, (ComponentContainer[]) components
				.toArray(new ComponentContainer[components.size()]));
	}

	public ListContainer getActionsAsList() {
		return new ListContainer(new String[] { "Name", "Scope", "Extends",
				"File" }, (ActionContainer[]) actions
				.toArray(new ActionContainer[actions.size()]));
	}

	public class ComponentContainer extends ListContainer.Container {

		private File file;
		private String name;
		private String scope;
		private String extendz;
		private String localPath;

		public ComponentContainer(Element xml, String pScope, File pFile,
				String pLocalPath) {
			name = xml.getAttribute("id");
			scope = pScope;
			file = pFile;
			extendz = xml.getAttribute("extends");
			localPath = pLocalPath;
		}

		public Object[] getRow() {
			return new String[] { name, scope, extendz, localPath };
		}

		public File getFile() {
			return file;
		}

	}

	public class ActionContainer extends ListContainer.Container {

		private File file;
		private String name;
		private String scope;
		private String extendz;
		private String localPath;

		public ActionContainer(Element xml, String pScope, File pFile,
				String pLocalPath) {
			name = xml.getAttribute("id");
			scope = pScope;
			file = pFile;
			extendz = xml.getAttribute("extends");
			localPath = pLocalPath;
		}

		public Object[] getRow() {
			return new String[] { name, scope, extendz, localPath };
		}

		public File getFile() {
			return file;
		}

	}
}
