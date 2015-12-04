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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSysObject;

import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.gui.ALogger;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.sf.ScriptIfc;
import de.mhu.hair.tools.ObjectTool;
import de.mhu.lib.AFile;

public class RenditionViewer implements ScriptIfc {

	private String tmpDir;
	private Properties properties;
	private File fileHeader;
	private File fileFooter;

	public void initialize(PluginNode pNode, DMConnection pCon, ALogger pLogger)
			throws Exception {
		// TODO Auto-generated method stub

	}

	public void execute(PluginNode pNode, DMConnection pCon,
			IDfPersistentObject[] pTargets, ALogger pLogger) throws Exception {

		for (int t = 0; t < pTargets.length; t++) {

			IDfSysObject obj = (IDfSysObject) pTargets[t];
			String id = obj.getObjectId().toString();
			String dql = "select f.dos_extension,f.name "
					+ "FROM dm_sysobject (ALL) s, dmr_content r, dm_store t, dm_format f "
					+ "WHERE r_object_id = ID('" + id + "') "
					+ "AND ANY (parent_id=ID('" + id + "') AND page = 0) "
					+ "AND r.storage_id=t.r_object_id "
					+ "AND f.r_object_id=r.format";

			IDfQuery query = pCon.createQuery(dql);
			IDfCollection res = query.execute(pCon.getSession(),
					IDfQuery.READ_QUERY);

			while (res.next()) {

				try {
					String name = res.getString("name");
					pLogger.out.println(">>> " + name + "@"
							+ ObjectTool.getName(obj));
					String ext = res.getString("dos_extension");
					ByteArrayInputStream stream = obj.getContentEx2(name, 0,
							null);
					String content = AFile.readFile(stream);
					stream.close();

					for (Enumeration i = properties.keys(); i.hasMoreElements();) {
						String key = (String) i.nextElement();
						content = content.replaceAll(key, properties
								.getProperty(key));
					}

					content = AFile.readFile(fileHeader) + content
							+ AFile.readFile(fileFooter);

					File f = new File(tmpDir, obj.getObjectName() + "_"
							+ obj.getString("language_code") + "_" + name + "."
							+ ext);
					pLogger.out.println("--- WRITE: " + f.getAbsolutePath());
					AFile.writeFile(f, content);

				} catch (Exception e) {
					pLogger.out.println(e.toString());
				}
			}
			res.close();

		}
	}

	public void setExportDir(String in) {
		tmpDir = in;
	}

	public void setMapProperties(String in) {
		properties = new Properties();
		try {
			properties.load(new FileInputStream(in));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setHeader(String in) {
		fileHeader = new File(in);
	}

	public void setFooter(String in) {
		fileFooter = new File(in);
	}

	public void destroy(PluginNode node, DMConnection con, ALogger logger) {
		// TODO Auto-generated method stub

	}
}
