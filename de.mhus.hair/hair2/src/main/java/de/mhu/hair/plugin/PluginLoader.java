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

package de.mhu.hair.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.mhu.lib.ACast;
import de.mhu.lib.ArgsParser;
import de.mhu.lib.resources.IResourceProvider;
import de.mhu.lib.resources.Resources;
import de.mhu.lib.xml.XmlTool;

public class PluginLoader {

	// private Vector jars = new Vector();
	private TreeMap<String,String> starter = new TreeMap<String,String>();
	private Hashtable<String,Element> configs = new Hashtable<String,Element>();

	private DocumentBuilder builder;
	// private ClassLoader loader;
	private int sortCnt = 0;

	public static boolean testMode = false;
	private static LinkedList<Element> staticConfigurations;

	public static void initialize(ArgsParser ap, URL[] jars) {
		if (ap.isSet("hair_test_mode"))
			testMode = true;

		String[] xmlConfigs = ap.getValues("hair_config");
		if (xmlConfigs != null && xmlConfigs.length != 0) {
			jars = new URL[xmlConfigs.length];
			for (int i = 0; i < jars.length; i++)
				try {
					jars[i] = new File(xmlConfigs[i]).toURL();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
		}

		staticConfigurations = new LinkedList<Element>();

		if (jars != null) {

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder xmlBuilder = null;
			try {
				xmlBuilder = dbf.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			for (int i = 0; i < jars.length; i++) {

				try {
					if (jars[i].getFile().endsWith(".jar")) {
						JarFile f = new JarFile(jars[i].getFile());
						Manifest mf = f.getManifest();
						if (mf == null)
							continue;
						if (mf.getAttributes("hair") == null)
							continue;
						String res = mf.getAttributes("hair")
								.getValue("config");
						// String res = mf.getMainAttributes().getValue(
						// "Hair-Config" );
						if (res == null)
							continue;
						String[] parts = res.split(",");
						for (int j = 0; j < parts.length; j++) {
							InputStream is = f.getInputStream(f
									.getEntry(parts[j]));
							Document dom = xmlBuilder.parse(is);
							System.out.println(">>> SC: "
									+ f.getName()
									+ " --> "
									+ dom.getDocumentElement().getAttribute(
											"name"));
							dom.getDocumentElement().setAttribute( "file", f.getName() );
							staticConfigurations.add(dom.getDocumentElement());
						}
					} else if (jars[i].getFile().endsWith(".xml")) {
						InputStream is = new FileInputStream(jars[i].getFile());
						Document dom = xmlBuilder.parse(is);
						System.out
								.println(">>> SC: "
										+ jars[i].getFile()
										+ " --> "
										+ dom.getDocumentElement()
												.getAttribute("name"));
						dom.getDocumentElement().setAttribute( "file", jars[i].getFile() );
						staticConfigurations.add(dom.getDocumentElement());
					}
				} catch (Exception e) {
					System.out.println("*** Jar: " + jars[i].getFile());
					e.printStackTrace();
				}

			}
		}

		// load resource handler
		Map<String,Element> resourcesMap = getConfigElementsByName("resource");
		for ( Map.Entry<String, Element> entry : resourcesMap.entrySet() ) {
			try {
				System.out.println( "--- Resource: " + entry.getKey() );
				String clazz = entry.getValue().getAttribute( "class" );
				IResourceProvider obj = (IResourceProvider)Class.forName(clazz).newInstance();
				Resources.getInstance().registerProvider(entry.getKey(), obj );
			} catch ( Exception e ) {
				e.printStackTrace();
			}
		}

	}

	public static Iterator<Element> getConfigElements(String name) {
		TreeMap<String,Element> out = new TreeMap<String,Element>();
		int cnt = 0;
		for (Iterator<Element> i = staticConfigurations.iterator(); i.hasNext();) {
			cnt++;
			Element e = (Element) i.next();
			NodeList list = XmlTool.getLocalElements(e, name);
			if ( list.getLength() > 0 )
				System.out.println( "    [from " + e.getAttribute("file") + ']');
			for (int j = 0; j < list.getLength(); j++) {
				out.put(toSort(ACast.toint(((Element) list.item(j))
						.getAttribute("sort")
						+ toSort(cnt), 100)), (Element)list.item(j));
			}
		}

		return out.values().iterator();
	}

	public static Map<String,Element> getConfigElementsByName(String name) {
		Hashtable<String,Element> out = new Hashtable<String,Element>();
		for (Iterator<Element> i = staticConfigurations.iterator(); i.hasNext();) {
			Element e = (Element) i.next();
			NodeList list = XmlTool.getLocalElements(e, name);
			for (int j = 0; j < list.getLength(); j++) {
				out.put(((Element) list.item(j)).getAttribute("name"), (Element)list.item(j));
			}
		}

		return out;
	}

	public PluginLoader(String config) throws IOException {
		System.out.println(">>> Load Plugins from: " + config);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			builder = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		File[] list = null;
		list = new File(config).listFiles();

		if (list == null) {
			System.out.println("*** Plugin directory not found");
			throw new IOException("Plugin directory not found " + config);
		}
		for (int i = 0; i < list.length; i++) {
			if (list[i].isFile() && list[i].getName().endsWith(".xml")) {
				try {
					System.out.println("--- Load " + list[i].getName());
					processConfig(list[i]);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		// loader = new URLClassLoader( (URL[])jars.toArray( new URL[
		// jars.size() ] ) );

	}

	public PluginLoader(Element config, File rootDir)
			throws MalformedURLException {
		load(config, rootDir);
		// loader = getClass().getClassLoader();
	}

	private void processConfig(File file) throws SAXException, IOException {

		Document dom = builder.parse(file);
		Element root = dom.getDocumentElement();
		load(root, file.getParentFile());
	}

	private void load(Element root, File rootDir) throws MalformedURLException {

		/*
		 * NodeList jarNodes = XmlTool.getLocalElements( root, "jar" ); for (
		 * int i = 0; i < jarNodes.getLength(); i++ ) { Element element =
		 * (Element)jarNodes.item( i ); File f = null; String mode =
		 * element.getAttribute( "mode" ); if ( "relative".equals( mode ) ) f =
		 * new File( rootDir, element.getAttribute( "file" ) ); else if (
		 * "absolut".equals( mode ) ) f = new File( element.getAttribute( "file"
		 * ) ); else f = new File( rootDir, element.getAttribute( "file" ) );
		 * 
		 * URL url = f.toURL(); if ( !jars.contains( url ) ) {
		 * System.out.println( ">>> Load JAR " + url ); jars.add( url ); } }
		 */

		NodeList startNodes = XmlTool.getLocalElements(root, "start");
		for (int i = 0; i < startNodes.getLength(); i++) {
			sortCnt++;
			Element element = (Element) startNodes.item(i);
			if ( ! element.getAttribute("include").equals("") ) {
				File f = new File(rootDir, element.getAttribute("include"));
				System.out.println(">>> Include " + f.getPath());
				try {
					processConfig(f);
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.exit(1);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.exit(1);
				}
			} else
			if ( ! element.getAttribute("includeconfigs").equals("") ) {
				for (Iterator<Element> j = PluginLoader.getConfigElements(element.getAttribute("includeconfigs")); j.hasNext();) {
					Element de = j.next();
					load( de, rootDir );
				}
			} else {
				String sp = element.getAttribute("class");
				NodeList configList = XmlTool.getLocalElements(element,
						"config");
				Element config = null;
				if (configList.getLength() > 0)
					config = (Element) configList.item(0);
				String sort = root.getAttribute("sort") + "_" + toSort(sortCnt);
				starter.put(sort, sp);
				if (config != null)
					configs.put(sp + ":" + sort, config);
				System.out.println(">>> Loaded " + sp + ":" + sort + " "
						+ config);
			} 
		}

	}

	private static String toSort(int i) {
		if (i < 10)
			return "00000" + i;
		if (i < 100)
			return "0000" + i;
		if (i < 1000)
			return "000" + i;
		if (i < 10000)
			return "00" + i;
		if (i < 100000)
			return "0" + i;
		return String.valueOf(i);
	}

	public boolean start(PluginNode glue, Map params, boolean allowErrors) {

		for (Iterator<String> i = starter.keySet().iterator(); i.hasNext();) {
			try {
				String sort  = i.next();
				String clazz = (String) starter.get(sort);

				System.out.println("--- Init " + clazz + " (" + sort + ")");
				Element config = (Element) configs.get(clazz + ":" + sort);
				Plugin plugin = (Plugin) Class.forName(clazz).newInstance();
				if (testMode) {
					System.out.println("*** TEST MODE: don't init Plugin");
					if (config != null) {
						System.out.println("--- START Config");
						PluginConfig pc = new PluginConfig(config, params);
						OutputFormat format = new OutputFormat();
						format.setIndenting(true);
						XMLSerializer serializer = new XMLSerializer(
								System.out, format);
						serializer.serialize(pc.getNode());
						System.out.println("--- END Config");
					}
				} else {
					plugin.initPlugin(glue, new PluginConfig(config, params));
				}
			} catch (Exception e) {
				e.printStackTrace();
				if (!allowErrors)
					return false;
			}
		}
		return true;

	}

}
