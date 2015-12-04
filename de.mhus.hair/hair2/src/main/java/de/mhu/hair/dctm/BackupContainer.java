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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfFormat;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfAttr;
import com.documentum.fc.common.IDfValue;

import de.mhu.lib.AString;

public class BackupContainer {

	public static final int LEVEL_SINGLE = 0;
	public static final int LEVEL_RELATIONS = 1;
	// public static final int LEVEL_FAMILY = 2;

	private File zipFile;
	private DMConnection con;
	private Hashtable ids = new Hashtable();
	private FileOutputStream os;
	private ZipOutputStream zo;
	private int level = LEVEL_SINGLE;
	private Hashtable contents = new Hashtable();
	private DocumentBuilderFactory dbf;
	private DocumentBuilder db;
	private File baseDir;
	private boolean backupContent = true;
	private String[] attrList;

	public BackupContainer(DMConnection pCon, File pZipFile) throws Exception {
		zipFile = pZipFile;
		con = pCon;

		if (zipFile.exists()) {

		}

		dbf = DocumentBuilderFactory.newInstance();
		db = dbf.newDocumentBuilder();

		if (pZipFile.isFile()) {
			os = new FileOutputStream(zipFile);
			zo = new ZipOutputStream(os);
		} else {
			baseDir = pZipFile;
		}

	}

	public void close() {
		try {
			if (zo != null) {
				zo.close();
				os.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		os = null;
		zo = null;
	}

	public void setBackupLevel(int in) {
		level = in;
	}

	public void backupObject(IDfCollection obj) throws Exception {
		String id = obj.getString("r_object_id");
		if (ids.get(id) != null)
			return;
		backupSingleObject(obj, backupContent);

		if (level > LEVEL_SINGLE) {
			IDfQuery query = con
					.createQuery("select r_object_id from dm_relation where parent_id='"
							+ id + "'");
			IDfCollection res = query.execute(con.getSession(),
					IDfQuery.READ_QUERY);
			while (res.next()) {
				backupSingleObject(con.getPersistentObject(res
						.getString("r_object_id")), false);
			}
		}

	}

	public void backupObject(IDfPersistentObject obj) throws Exception {

		String id = obj.getObjectId().toString();
		if (ids.get(id) != null)
			return;

		backupSingleObject(obj, backupContent);

		if (level > LEVEL_SINGLE) {
			IDfQuery query = con
					.createQuery("select r_object_id from dm_relation where parent_id='"
							+ id + "'");
			IDfCollection res = query.execute(con.getSession(),
					IDfQuery.READ_QUERY);
			while (res.next()) {
				backupSingleObject(con.getPersistentObject(res
						.getString("r_object_id")), false);
			}
		}

	}

	public void backupSingleObject(IDfPersistentObject obj, boolean content)
			throws Exception {

		String id = obj.getObjectId().toString();
		if (ids.get(id) != null)
			return;

		Document dom = db.newDocument();
		Element root = dom.createElement("object");
		dom.appendChild(root);

		root.setAttribute("type", obj.getType().getName());
		root.setAttribute("id", obj.getObjectId().toString());

		getObject(con, obj, dom, root);
		if (content)
			try {
				getValue(con, obj, dom, root);
			} catch (Exception e2) {
				e2.printStackTrace();
			}

		OutputFormat format = new OutputFormat(dom);
		format.setIndenting(true);

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 14; i = i + 2) {
			sb.append(id.charAt(i));
			sb.append(id.charAt(i + 1));
			sb.append('/');
		}
		String dir = "obj/" + sb.toString();
		ids.put(id, dir + obj.getObjectId() + ".xml");
		if (zo != null) {
			ZipEntry entry = new ZipEntry(dir + id + ".xml");
			zo.putNextEntry(entry);
			XMLSerializer serializer = new XMLSerializer(zo, format);
			serializer.serialize(dom);
			zo.closeEntry();
		} else {
			File d = new File(baseDir, dir);
			d.mkdirs();
			os = new FileOutputStream(new File(d, id + ".xml"));
			XMLSerializer serializer = new XMLSerializer(os, format);
			serializer.serialize(dom);
			os.close();
			os = null;
		}

	}

	public void backupSingleObject(IDfCollection obj, boolean content)
			throws Exception {

		String id = obj.getString("r_object_id");
		if (ids.get(id) != null)
			return;

		Document dom = db.newDocument();
		Element root = dom.createElement("object");
		dom.appendChild(root);

		root.setAttribute("type", obj.getString("r_object_type"));
		root.setAttribute("id", id);

		getObject(con, obj, dom, root);
		/*
		 * if ( content ) try { getValue( con, obj, dom, root ); } catch (
		 * Exception e2 ) { e2.printStackTrace(); }
		 */
		OutputFormat format = new OutputFormat(dom);
		format.setIndenting(true);

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 14; i = i + 2) {
			sb.append(id.charAt(i));
			sb.append(id.charAt(i + 1));
			sb.append('/');
		}
		String dir = "obj/" + sb.toString();
		ids.put(id, dir + obj.getObjectId() + ".xml");
		if (zo != null) {
			ZipEntry entry = new ZipEntry(dir + id + ".xml");
			zo.putNextEntry(entry);
			XMLSerializer serializer = new XMLSerializer(zo, format);
			serializer.serialize(dom);
			zo.closeEntry();
		} else {
			File d = new File(baseDir, dir);
			d.mkdirs();
			os = new FileOutputStream(new File(d, id + ".xml"));
			XMLSerializer serializer = new XMLSerializer(os, format);
			serializer.serialize(dom);
			os.close();
			os = null;
		}

	}

	private void getObject(DMConnection con, IDfPersistentObject obj,
			Document dom, Element root) throws DfException {

		Element attrNode = dom.createElement("attributes");
		root.appendChild(attrNode);

		for (int i = 0; i < obj.getAttrCount(); i++) {
			IDfAttr attr = obj.getAttr(i);
			IDfValue attrValue = obj.getValue(attr.getName());

			String name = attr.getName();
			boolean ok = true;
			if (attrList != null) {
				ok = false;
				for (int j = 0; j < attrList.length; j++)
					if (AString.compareSQLLikePattern(name, attrList[j])) {
						ok = true;
						break;
					}
			}

			if (ok) {
				String type = "-";
				switch (attrValue.getDataType()) {
				case IDfValue.DF_BOOLEAN:
					type = "b";
					break;
				case IDfValue.DF_DOUBLE:
					type = "d";
					break;
				case IDfValue.DF_ID:
					type = "id";
					break;
				case IDfValue.DF_INTEGER:
					type = "i";
					break;
				case IDfValue.DF_STRING:
					type = "s";
					break;
				case IDfValue.DF_TIME:
					type = "t";
					break;
				case IDfValue.DF_UNDEFINED:
					type = "?";
					break;
				}

				Element aNode = dom.createElement("a");
				aNode.setAttribute("name", name);
				aNode.setAttribute("type", type);
				aNode.setAttribute("rep",
						obj.isAttrRepeating(attr.getName()) ? "1" : "0");
				attrNode.appendChild(aNode);

				if (obj.isAttrRepeating(attr.getName())) {
					for (int j = 0; j < obj.getValueCount(attr.getName()); j++) {
						IDfValue value = obj.getRepeatingValue(attr.getName(),
								j);
						Element vNode = dom.createElement("v");
						aNode.appendChild(vNode);
						vNode.setAttribute("value", value.toString());
					}
				} else
					aNode.setAttribute("value", attrValue.toString());
			}
		}

	}

	private void getObject(DMConnection con, IDfCollection obj, Document dom,
			Element root) throws DfException {

		Element attrNode = dom.createElement("attributes");
		root.appendChild(attrNode);

		for (int i = 0; i < obj.getAttrCount(); i++) {
			IDfAttr attr = obj.getAttr(i);
			IDfValue attrValue = obj.getValue(attr.getName());

			String name = attr.getName();
			boolean ok = true;
			if (attrList != null) {
				ok = false;
				for (int j = 0; j < attrList.length; j++)
					if (AString.compareSQLLikePattern(name, attrList[j])) {
						ok = true;
						break;
					}
			}

			if (ok) {
				String type = "-";
				switch (attrValue.getDataType()) {
				case IDfValue.DF_BOOLEAN:
					type = "b";
					break;
				case IDfValue.DF_DOUBLE:
					type = "d";
					break;
				case IDfValue.DF_ID:
					type = "id";
					break;
				case IDfValue.DF_INTEGER:
					type = "i";
					break;
				case IDfValue.DF_STRING:
					type = "s";
					break;
				case IDfValue.DF_TIME:
					type = "t";
					break;
				case IDfValue.DF_UNDEFINED:
					type = "?";
					break;
				}

				Element aNode = dom.createElement("a");
				aNode.setAttribute("name", name);
				aNode.setAttribute("type", type);
				aNode.setAttribute("rep",
						obj.isAttrRepeating(attr.getName()) ? "1" : "0");
				attrNode.appendChild(aNode);

				if (obj.isAttrRepeating(attr.getName())) {
					for (int j = 0; j < obj.getValueCount(attr.getName()); j++) {
						IDfValue value = obj.getRepeatingValue(attr.getName(),
								j);
						Element vNode = dom.createElement("v");
						aNode.appendChild(vNode);
						vNode.setAttribute("value", value.toString());
					}
				} else
					aNode.setAttribute("value", attrValue.toString());
			}
		}

	}

	private void getValue(DMConnection con, IDfPersistentObject obj,
			Document dom, Element root) throws Exception {

		if (obj instanceof IDfSysObject) {
			IDfSysObject sys = (IDfSysObject) obj;

			int pages = sys.getPageCount();
			if (pages <= 0)
				return;

			IDfFormat myFormat = con.getSession().getFormat(
					sys.getContentType());
			if (myFormat == null)
				return;

			String ext = myFormat.getDOSExtension();

			String contId = sys.getString("i_contents_id");
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < 14; i = i + 2) {
				sb.append(contId.charAt(i));
				sb.append(contId.charAt(i + 1));
				sb.append('/');
			}
			String dir = "cont/" + sb.toString();

			byte b[] = new byte[512];

			String filePath = dir + sys.getObjectId() + "." + ext;
			if (contents.get(filePath) == null) {

				contents.put(filePath, "");

				if (zo != null) {
					ZipEntry entry = new ZipEntry(filePath);
					zo.putNextEntry(entry);
					System.out.println("Load Conten: " + contId);
					ByteArrayInputStream is = sys.getContent();
					int len = 0;
					while ((len = is.read(b)) != -1) {
						zo.write(b, 0, len);
					}
					zo.closeEntry();
				} else {
					File d = new File(baseDir, dir);
					d.mkdirs();

					if (!new File(filePath).exists()) {
						System.out.println("Load Conten: " + contId);
						os = new FileOutputStream(new File(baseDir, filePath));
						ByteArrayInputStream is = sys.getContent();
						int len = 0;
						while ((len = is.read(b)) != -1) {
							os.write(b, 0, len);
						}
						os.close();
					}

				}

			}

			root.setAttribute("name", filePath);
		}

	}

	public void setAttrList(String[] in) {
		attrList = in;
	}

	public void setBackupContent(boolean in) {
		backupContent = in;
	}

	public File getFile() {
		return zipFile;
	}

}
