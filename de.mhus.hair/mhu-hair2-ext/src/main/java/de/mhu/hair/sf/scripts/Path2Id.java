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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;

import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.gui.ALogger;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.sf.ScriptIfc;
import de.mhu.hair.tools.ObjectTool;
import de.mhu.lib.ASql;
import de.mhu.lib.io.CSVReader;
import de.mhu.lib.io.CSVWriter;

public class Path2Id implements ScriptIfc {

	private String where;
	private String outputPath;
	private String inputPath;
	private int type;
	private File outputFile;
	private boolean inputHasHeader;
	private String objectType;
	private boolean multi;
	private boolean versions;

	public void initialize(PluginNode pNode, DMConnection pCon, ALogger pLogger)
			throws Exception {

		outputFile = new File(outputPath);

		FileWriter os = new FileWriter(outputFile);
		CSVWriter writer = new CSVWriter(os, 2, ';', '"', true);

		writer.put("R_OBJECT_ID");
		writer.put("PATH");
		writer.put("LANGUAGE_CODE");
		if (versions)
			writer.put("VERSIONS");
		writer.nl();

		if (type == 0) {

			File inputFile = new File(inputPath);
			FileReader is = new FileReader(inputFile);
			CSVReader reader = new CSVReader(is, ';', '"', true, true);
			if (inputHasHeader)
				reader.skipToNextLine();

			try {
				while (true) {
					String[] line = reader.getAllFieldsInLine();
					if (line.length > 0 && line[0] != null
							&& line[0].length() != 0) {

						String dql = null;
						String in = line[0].trim();
						pLogger.out.println(">>> " + in);

						try {
							int pos = in.lastIndexOf('/');
							/*
							 * if ( pos < 0 && in.length() == 16 ) { in =
							 * ObjectTool.getPath( pCon.getPersistentObject( in
							 * ) ); if ( in == null ) in = ""; pos =
							 * in.lastIndexOf( '/' ); }
							 */
							if (pos < 0) {
								if (in.length() == 16) {
									dql = "SELECT r_object_id,language_code FROM "
											+ objectType
											+ " WHERE "
											+ "r_object_id='" + in + "'";
									in = ObjectTool.getPath(pCon
											.getPersistentObject(in));
								}
							} else if (pos == 1) {
								dql = "SELECT r_object_id,language_code FROM "
										+ objectType + " WHERE "
										+ "object_name='"
										+ ASql.escape(in.substring(1)) + "'";
							} else {
								String path = in.substring(0, pos);
								String name = in.substring(pos + 1);
								dql = "SELECT r_object_id,language_code FROM "
										+ objectType + " WHERE FOLDER('"
										+ ASql.escape(path) + "')"
										+ "AND object_name='"
										+ ASql.escape(name) + "'";

							}

							if (dql != null) {
								if (where != null && where.length() != 0)
									dql = dql + " AND (" + where + ")";

								pLogger.out.println("--- DQL: " + dql);

								IDfQuery query = pCon.createQuery(dql);
								IDfCollection res = query.execute(pCon
										.getSession(), IDfQuery.READ_QUERY);
								int cnt = 0;
								if (multi) {
									while (res.next()) {
										String id = res
												.getString("r_object_id");
										writer.put(id);
										writer.put(in);
										writer.put(res
												.getString("language_code"));
										if (versions) {
											StringBuffer v = new StringBuffer();
											IDfPersistentObject o = pCon
													.getPersistentObject(id);
											for (int i = 0; i < o
													.getValueCount("r_version_label"); i++) {
												if (i != 0)
													v.append(',');
												v.append(o.getRepeatingString(
														"r_version_label", i));
											}
											writer.put(v.toString());
										}
										writer.nl();
										cnt++;
									}
								} else {
									if (res.next()) {
										String id = res
												.getString("r_object_id");
										writer.put(id);
										writer.put(in);
										writer.put(res
												.getString("language_code"));
										if (versions) {
											StringBuffer v = new StringBuffer();
											IDfPersistentObject o = pCon
													.getPersistentObject(id);
											for (int i = 0; i < o
													.getValueCount("r_version_label"); i++) {
												if (i != 0)
													v.append(',');
												v.append(o.getRepeatingString(
														"r_version_label", i));
											}
											writer.put(v.toString());
										}
										writer.nl();
										cnt++;
									}
								}
								res.close();
								pLogger.out.println("+++ CNT:" + cnt);
							}
						} catch (Throwable t) {
							t.printStackTrace();
							pLogger.out.println("*** ERROR: " + t);
						}
					}
				}
			} catch (IOException e) {
				pLogger.out.println("*** " + e);
				e.printStackTrace();
			}

			is.close();

		}

		os.close();
	}

	public void execute(PluginNode pNode, DMConnection pCon,
			IDfPersistentObject[] pTargets, ALogger pLogger) throws Exception {

		if (type == 1) {

			FileWriter os = new FileWriter(outputFile, true);
			CSVWriter writer = new CSVWriter(os, 2, ';', '"', true);

			pLogger.setMaximum(pTargets.length);
			for (int i = 0; i < pTargets.length; i++) {
				pLogger.setValue(i);
				if (pTargets[i] != null) {
					String path = ObjectTool.getPath(pTargets[i]);
					pLogger.out.println(">>> " + path);
					writer.put(pTargets[i].getObjectId().getId());
					writer.put(path);
					writer.put(pTargets[i].getString("language_code"));
					writer.nl();
				}
			}

			os.close();

		}
	}

	public void setType(int in, String inStr) {
		type = in;
	}

	public void setInputCsv(String in) {
		inputPath = in;
	}

	public void setOutputCsv(String in) {
		outputPath = in;
	}

	public void setWhere(String in) {
		where = in;
	}

	public void setInputHasHeader(boolean in) {
		inputHasHeader = in;
	}

	public void setObjectType(String in) {
		objectType = in;
	}

	public void setMulti(boolean in) {
		multi = in;
	}

	public void setVersions(boolean in) {
		versions = in;
	}

	public void destroy(PluginNode node, DMConnection con, ALogger logger) {
		// TODO Auto-generated method stub

	}

}
