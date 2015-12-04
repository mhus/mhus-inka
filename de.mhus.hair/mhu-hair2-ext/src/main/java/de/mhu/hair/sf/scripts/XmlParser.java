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

package de.mhu.hair.sf.scripts;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.common.DfException;

import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.gui.ALogger;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.sf.ScriptIfc;
import de.mhu.hair.tools.ObjectTool;
import de.mhu.lib.Rfc1738;

public class XmlParser implements ScriptIfc {

	private String format;
	private ALogger logger;
	private boolean parseNodes;
	private int level;

	public void execute(PluginNode pNode, DMConnection pCon,
			IDfPersistentObject[] pTargets, ALogger pLogger) throws Exception {

		logger = pLogger;
		logger.out.println("Go...");
		for (int i = 0; i < pTargets.length; i++) {
			if (pTargets[i] instanceof IDfDocument)
				try {
					parse((IDfDocument) pTargets[i]);
				} catch (Exception e) {
					e.printStackTrace(logger.out);
				}
		}

	}

	private void parse(IDfDocument doc) throws DfException, IOException,
			ParserConfigurationException, SAXException {
		logger.out.println(">>> " + ObjectTool.getPath(doc));

		ByteArrayInputStream stream = doc.getContentEx2(format, 0, null);

		/*
		 * byte[] b = new byte[ stream.available() ]; stream.read( b );
		 * stream.close();
		 */

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = dbf.newDocumentBuilder();
		Document dom = builder.parse(stream);
		level = 0;
		parseElement(dom.getDocumentElement());
		logger.out.println("<<<");
	}

	private void parseElement(Node element) {
		printLevelSpace();
		logger.out.println("[" + element.getNodeType() + "(" + element.getClass().getName() + "): "+ element.getNodeName());
		NamedNodeMap attrMap = element.getAttributes();
		if ( attrMap != null ) {
			for ( int i = 0; i < attrMap.getLength(); i++ ) {
				if (parseNodes) printLevelSpace();
				if (parseNodes) logger.out.println(" Attribute: " + attrMap.item(i).getNodeName() + "=" + attrMap.item(i).getNodeValue() );
			}
		}
		String value = element.getNodeValue();
		if ( value != null && value.length() != 0 ) {
			
			for(int i = 0; i < value.length(); i++)
            {
				char c = value.charAt(i);
				if ( c != '\n' && c > 127 && (c & 64512) != 55296) {
					logger.out.println("*** ");
					if (parseNodes) printLevelSpace();
					logger.out.println("Illegal character: " + (int)c + " at " + i);
				}
            }
			if (parseNodes) printLevelSpace();
			if (parseNodes) logger.out.println(" Value (encoded): " + Rfc1738.encode(value));
		}
		printLevelSpace();
		logger.out.println("]");
		if ( element instanceof Element ) {
			level++;
			NodeList children = ((Element)element).getChildNodes();
			for ( int i = 0; i < children.getLength(); i++ )
				parseElement( children.item(i) );
			level--;
		}
	}

	private void printLevelSpace() {
		for (int i = 0; i < level; i++)
			logger.out.print("  ");
	}

	public void initialize(PluginNode pNode, DMConnection pCon, ALogger pLogger)
			throws Exception {
		// TODO Auto-generated method stub

	}

	public void setFormat(String in) {
		format = in;
	}

	public void setParseNodes(boolean in) {
		parseNodes = in;
	}

	public void destroy(PluginNode node, DMConnection con, ALogger logger) {
		// TODO Auto-generated method stub

	}

}
