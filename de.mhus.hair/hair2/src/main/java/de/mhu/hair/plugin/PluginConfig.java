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

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.mhu.lib.xml.XmlTool;

public class PluginConfig {

	private Element node;
	private Map params;
	private Element original;
	private Document dom;
	private static DocumentBuilderFactory dbf = DocumentBuilderFactory
			.newInstance();

	public PluginConfig(Element pNode, Map pParams) {
		original = pNode;
		params = pParams;

		if (original != null) {
			try {
				DocumentBuilder db = dbf.newDocumentBuilder();

				dom = db.newDocument();
				node = dom.createElement(original.getNodeName());
				dom.appendChild(node);

				parseNode(original, node);
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
		}
	}

	private void parseNode(Element n, Element dest) {

		if (n == null)
			return;

		NamedNodeMap map = n.getAttributes();
		for (int i = 0; i < map.getLength(); i++) {
			Node attr = map.item(i);
			dest.setAttribute(attr.getNodeName(), parseString(attr
					.getNodeValue()));
		}
		NodeList list = XmlTool.getLocalElements(n);
		for (int i = 0; i < list.getLength(); i++) {
			Element e = (Element) list.item(i);

			if (e.getNodeName().equals("includeconfigs")) {
				System.out.println( "--- includeconfigs: " + e.getAttribute("name"));
				for (Iterator j = PluginLoader.getConfigElements(e
						.getAttribute("name")); j.hasNext();) {
					Element de = (Element) j.next();
					try {
						if (e.getAttribute("mode").equals("root")) {
							Element eNew = dom.createElement(de.getNodeName());
							dest.appendChild(eNew);
							parseNode(de, eNew);
						} else {
							NodeList list2 = XmlTool.getLocalElements(de);
							for (int i2 = 0; i2 < list2.getLength(); i2++) {
								Element e2 = (Element) list2.item(i2);
								Element eNew = dom.createElement(e2
										.getNodeName());
								dest.appendChild(eNew);
								parseNode(e2, eNew);

								// e2.getParentNode().removeChild( e2 );
								// n.insertBefore( e2, e );

							}
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}

			} else if (e.getNodeName().equals("includexml")) {
				String path = e.getAttribute("path");
				try {
					InputStream is = null;
					if (path.startsWith("res:")) {
						is = getClass().getResourceAsStream(path.substring(4));
					} else {
						is = new FileInputStream(path);
					}

					DocumentBuilder db = dbf.newDocumentBuilder();

					Document d = db.parse(is);
					is.close();

					Element de = d.getDocumentElement();

					if (e.getAttribute("mode").equals("root")) {
						Element eNew = dom.createElement(de.getNodeName());
						dest.appendChild(eNew);
						parseNode(de, eNew);
					} else {
						NodeList list2 = XmlTool.getLocalElements(de);
						for (int i2 = 0; i2 < list2.getLength(); i2++) {
							Element e2 = (Element) list2.item(i2);
							Element eNew = dom.createElement(e2.getNodeName());
							dest.appendChild(eNew);
							parseNode(e2, eNew);

							// e2.getParentNode().removeChild( e2 );
							// n.insertBefore( e2, e );

						}
					}

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			} else {

				Element eNew = dom.createElement(e.getNodeName());
				dest.appendChild(eNew);
				parseNode(e, eNew);

			}
		}
	}

	public Element getNode() {
		return node;
	}

	public Element getOriginalNode() {
		return original;
	}

	private String parseString(String str) {

		if (str == null || str.indexOf('$') < 0)
			return str;

		StringBuffer sb = new StringBuffer();
		int off = 0;
		int mode = 0;
		int keyPos = 0;

		while (true) {
			int pos = str.indexOf('$', off);
			if (pos < 0) {
				sb.append(str.substring(off));
				return sb.toString();
			}
			if (mode == 0) {
				sb.append(str.substring(off, pos));
				keyPos = pos + 1;
				mode = 1;
			} else {
				if (keyPos == pos) {
					sb.append('$');
				} else {
					String key = str.substring(keyPos, pos);

					// find the dafault value, if set
					int defaultPos = key.lastIndexOf('|');
					String defaultValue = null;
					if (defaultPos > 0) {
						defaultValue = key.substring(defaultPos + 1);
						key = key.substring(0, defaultPos);
					}

					// find the index if set
					int indexPos = key.lastIndexOf(':');
					if (indexPos > 0) {
						int index = Integer.parseInt(key
								.substring(indexPos + 1));
						key = key.substring(0, indexPos);
						if (getProperty(key) != null) {
							String value = getProperty(key);
							if (value == null) {
								System.out.println("--- PARSE: " + key
										+ " DEFAULT: " + defaultValue);
								value = defaultValue;
							}
							if (value != null) {
								System.out.println("--- PARSE: " + key
										+ " INDEX: " + index + " VALUE: "
										+ value);
								String[] args = value.split(",");
								if (index < args.length)
									sb.append(args[index]);
							} // else
							// sb.append( "" );
						}
					} else {
						String value = getProperty(key);
						if (value == null)
							value = defaultValue;
						if (value != null) {
							System.out.println("--- PARSE: " + key + " VALUE: "
									+ value);
							sb.append(value);
						}
						// else
						// sb.append( "" );
					}
				}
				mode = 0;
			}
			off = pos + 1;
		}

	}

	public String getProperty(String key) {
		Object obj = params.get(key);
		if (obj != null)
			return obj.toString();
		return null;
	}

	public Map getProperties() {
		return params;
	}

	public String getProperty(String key, String def) {
		String ret = getProperty(key);
		if (ret == null)
			ret = def;
		return ret;
	}

}
