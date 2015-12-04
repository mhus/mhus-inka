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

package de.mhu.hair.dctm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.documentum.fc.common.IDfAttr;

import de.mhu.lib.xml.XmlTool;

public class RestoreContainer {

	private File zipFile;
	private FileInputStream is;
	private ZipInputStream zi;
	private File baseDir;
	private Hashtable ids = new Hashtable();
	private DocumentBuilderFactory dbf;
	private DocumentBuilder builder;

	public RestoreContainer(File pZipFile) throws Exception {
		zipFile = pZipFile;

		dbf = DocumentBuilderFactory.newInstance();
		builder = dbf.newDocumentBuilder();

		if (pZipFile.isFile()) {
			is = new FileInputStream(zipFile);
			zi = new ZipInputStream(is);
		} else {
			baseDir = pZipFile;
		}

	}

	public void close() {
		try {
			if (zi != null) {
				zi.close();
				is.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		is = null;
		zi = null;
	}

	public void restoreObject(String id, Listener listener) {

		if (ids.get(id) != null)
			return;

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 14; i = i + 2) {
			sb.append(id.charAt(i));
			sb.append(id.charAt(i + 1));
			sb.append('/');
		}
		File file = new File(baseDir, "/obj/" + sb.toString() + id + ".xml");
		ids.put(id, file.getPath());

		int errors = 0;
		try {
			Document dom = builder.parse(file);
			Element root = dom.getDocumentElement();

			restoreSingleObject(root, listener);

		} catch (Exception e2) {
			e2.printStackTrace();
			errors++;
		}

	}

	private void restoreSingleObject(Element root, Listener listener) {

		String id = root.getAttribute("id");
		String type = root.getAttribute("type");
		NodeList list1 = XmlTool.getLocalElements(root, "attributes");
		NodeList list2 = XmlTool.getLocalElements((Element) list1.item(0), "a");

		listener.openObject(id, type);

		for (int i = 0; i < list2.getLength(); i++) {
			Element attr = (Element) list2.item(i);
			String name = attr.getAttribute("name");
			boolean rep = "1".equals(attr.getAttribute("rep"));
			String strType = attr.getAttribute("type");
			int iType = IDfAttr.DM_UNDEFINED;
			if ("s".equals(strType)) {
				iType = IDfAttr.DM_STRING;
			} else if ("b".equals(strType)) {
				iType = IDfAttr.DM_BOOLEAN;
			} else if ("d".equals(strType)) {
				iType = IDfAttr.DM_DOUBLE;
			} else if ("id".equals(strType)) {
				iType = IDfAttr.DM_ID;
			} else if ("i".equals(strType)) {
				iType = IDfAttr.DM_INTEGER;
			} else if ("t".equals(strType)) {
				iType = IDfAttr.DM_TIME;
			}

			if (rep) {
				NodeList list3 = XmlTool.getLocalElements(attr, "v");
				String[] value = new String[list3.getLength()];
				for (int j = 0; j < list3.getLength(); j++)
					value[j] = ((Element) list3.item(j)).getAttribute("value");
				listener.setRepeatingAttr(id, name, iType, value);
			} else {
				listener.setAttr(id, name, iType, attr.getAttribute("value"));
			}
		}

		listener.closeObject(id);

	}

	public interface Listener {

		void setRepeatingAttr(String id, String name, int type, String[] value);

		void closeObject(String id);

		void openObject(String id, String type);

		void setAttr(String id, String name, int type, String value);

	}
}
