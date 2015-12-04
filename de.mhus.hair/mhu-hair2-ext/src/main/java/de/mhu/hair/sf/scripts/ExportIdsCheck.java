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

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import java.util.Iterator;

import com.documentum.fc.client.IDfPersistentObject;

import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.gui.ALogger;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.sf.ScriptIfc;

public class ExportIdsCheck implements ScriptIfc {

	private String file;
	private boolean validate;
	private boolean write;

	public void execute(PluginNode pNode, DMConnection pCon,
			IDfPersistentObject[] pTargets, ALogger pLogger) throws Exception {

		File f = new File(file);

		// Open file with affected ids
		FileInputStream fis = new FileInputStream(f);
		ObjectInputStream ois = new ObjectInputStream(fis);

		Hashtable ids = new Hashtable();
		Hashtable validated = new Hashtable();

		int cnt = 0;
		int cntDouble = 0;
		int cntNotFound = 0;
		try {
			while (ois.available() >= 0) {
				String id = ois.readUTF();
				if (ids.get(id) != null) {
					cntDouble++;
				} else {

					if (validate) {
						if (validated.get(id) != null) {
							validated.put(id, "");
							try {
								if (pCon.getPersistentObject(id) != null)
									ids.put(id, "");
								else
									cntNotFound++;
							} catch (Exception ex) {
								cntNotFound++;
							}
						}
					} else {
						ids.put(id, "");
					}
				}
				cnt++;
				if (cnt % 100 == 0) {
					pLogger.out.print('.');
				}
				if (cnt % 10000 == 0)
					pLogger.out.println();

			}
		} catch (EOFException eofe) {
			pLogger.out.println("*** " + eofe);
		}

		pLogger.out.println("--- Count    : " + cnt);
		pLogger.out.println("--- Double   : " + cntDouble);
		pLogger.out.println("--- Not Found: " + cntNotFound);

		ois.close();
		fis.close();

		if (write) {
			pLogger.out.println(">>> Write");
			FileOutputStream fo = new FileOutputStream(f);
			ObjectOutputStream oos = new ObjectOutputStream(fo);
			cnt = 0;
			for (Iterator i = ids.keySet().iterator(); i.hasNext();) {
				oos.writeUTF((String) i.next());
				cnt++;
			}
			pLogger.out.println("--- Write Count: " + cnt);
			oos.close();
			fo.close();
		}

	}

	public void setFile(String in) {
		file = in;
	}

	public void setValidate(boolean in) {
		validate = in;
	}

	public void setWrite(boolean in) {
		write = in;
	}

	public void initialize(PluginNode pNode, DMConnection pCon, ALogger pLogger)
			throws Exception {
		// TODO Auto-generated method stub

	}

	public void destroy(PluginNode node, DMConnection con, ALogger logger) {
		// TODO Auto-generated method stub

	}

}
