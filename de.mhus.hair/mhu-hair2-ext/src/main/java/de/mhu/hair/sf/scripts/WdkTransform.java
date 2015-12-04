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
import java.io.FileInputStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;

import com.documentum.com.DfClientX;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfList;
//TODO DCTM6.5 import com.documentum.operations.nodes.impl.DfXMLTransformNode;
import com.documentum.operations.IDfOperation;
import com.documentum.operations.IDfOperationError;
import com.documentum.operations.IDfOperationMonitor;
import com.documentum.operations.IDfOperationNode;
import com.documentum.operations.IDfOperationStep;
import com.documentum.operations.IDfXMLTransformNode;
import com.documentum.operations.IDfXMLTransformOperation;
import com.documentum.operations.nodes.impl.DfXMLTransformNode;

import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.gui.ALogger;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.sf.ScriptIfc;
import de.mhu.hair.tools.ObjectTool;

public class WdkTransform implements ScriptIfc {

	private DMConnection con;
	private ALogger logger;
	private boolean testMode = true;
	private Hashtable xslCurrentCache = new Hashtable();
	private String[] renditions;
	private boolean isSetSpecialApp;
	private boolean isOverwrite = true;
	private String renderDeep;
	private boolean renderOnlyYellow;
	private String configFile;
	private static boolean debug = false;

	public void execute(PluginNode pNode, DMConnection pCon,
			IDfPersistentObject[] targets, ALogger pLogger) throws Exception {

		// System.setProperty( "com.documentum.xml.jaxp.DfTransformerFactory",
		// "de.mhu.hair.xml.HairTransformerFactoryImpl" );
		con = pCon;
		logger = pLogger;

		logger.setMaximum(targets.length);
		for (int i = 0; i < targets.length; i++) {
			logger.setValue(i);
			transform((IDfSysObject) targets[i]);

			try {
				if (configFile.length() != 0) {
					File f = new File(configFile);
					if (f.exists() && f.isFile()) {
						Properties p = new Properties();
						FileInputStream fis = new FileInputStream(f);
						p.load(fis);
						fis.close();
						if (p.getProperty("exit", "0").equals("1"))
							throw new Exception("EXIT");
						if (p.getProperty("sleep") != null) {
							logger.printMsg("Sleep: " + p.getProperty("sleep"));
							Thread
									.sleep(Long.parseLong(p
											.getProperty("sleep")));
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Transform a object into all transform configurations
	 * 
	 * @param object
	 * @throws DfException
	 */

	private void transform(IDfSysObject object) throws DfException {

		logger.out.println(">>> Object: " + ObjectTool.getPath(object));

		if (renderOnlyYellow) {
			String pSpecialApp = object.getString("a_special_app");
			if (!pSpecialApp.equals("1") && !pSpecialApp.startsWith("r:")
					&& !pSpecialApp.startsWith("2 1")
					&& !pSpecialApp.startsWith("2 r:")) {
				logger.out.println("+++ Skip: Mark is " + pSpecialApp);
				return;
			}
		}

		if (object.isImmutable()) {
			String setNotImmutableDql = "update dm_document (all) object set r_immutable_flag=FALSE where r_object_id='"
					+ object.getObjectId() + "'";
			logger.out.println("--- Remove immutable flag: "
					+ setNotImmutableDql);
			IDfQuery query = con.createQuery(setNotImmutableDql);
			IDfCollection collection = query.execute(con.getSession(),
					IDfQuery.READ_QUERY);
			collection.close();
			object.revert();
		}

		LinkedList existingFormats = null;
		if (!isOverwrite) {
			IDfQuery query = con
					.createQuery("select full_format FROM dm_sysobject (ALL) s, dmr_content r WHERE r_object_id = ID('"
							+ object.getObjectId().toString()
							+ "') AND ANY (parent_id=ID('"
							+ object.getObjectId().toString()
							+ "') AND page = 0)");
			IDfCollection res = query.execute(con.getSession(),
					IDfQuery.EXEC_QUERY);
			existingFormats = new LinkedList();
			while (res.next()) {
				existingFormats.add(res.getString("full_format"));
			}
			res.close();
		}

		IDfQuery query = con
				.createQuery("select child_id, description from dm_relation where parent_id='"
						+ object.getObjectId().toString()
						+ "' AND relation_name='wcm_layout_template'");
		IDfCollection res = query
				.execute(con.getSession(), IDfQuery.EXEC_QUERY);

		while (res.next()) {
			IDfId xsltActual = res.getId("child_id");
			IDfId xsltId = getCurrentXsl(xsltActual);
			try {
				String outputFormat = res.getString("description");
				int pos = outputFormat.indexOf('.');
				if (pos > 0)
					outputFormat = outputFormat.substring(0, pos);

				boolean ok = false;

				if (renditions != null) {
					for (int i = 0; i < renditions.length; i++)
						if (renditions[i].equals(outputFormat)) {
							ok = true;
							break;
						}
				} else
					ok = true;

				if (ok && existingFormats != null) {
					for (Iterator f = existingFormats.iterator(); f.hasNext();) {
						if (outputFormat.equals(f.next())) {
							logger.out.println("--- Rendition already exists: "
									+ outputFormat);
							ok = false;
						}
					}
				}

				if (ok) {
					if (testMode) {
						logger.out
								.println("*** Will Transform "
										+ object.getObjectId() + " XSLID: "
										+ xsltId + " (" + xsltActual
										+ ") Type: " + outputFormat);
					} else {
						logger.out
								.println("--- Transform "
										+ object.getObjectId() + " XSLID: "
										+ xsltId + " (" + xsltActual
										+ ") Type: " + outputFormat);

						if (renderDeep.length() != 0) {
							object.setString("a_special_app", renderDeep);
							object.save();
						}
						String out = null;

						try {
							out = createTransformation((IDfSysObject) object,
									xsltId, outputFormat);
						} catch (Exception e) {
							out = e.toString();
							e.printStackTrace();
						}

						if (out == null)
							logger.out.println("--- FINISH");
						else
							logger.out
									.println("*** FINISH WITH ERRORS: " + out);
						if (isSetSpecialApp) {
							object.setString("a_special_app", out == null ? "2"
									: "0");
							object.save();
						}
					}
				} else {
					logger.out.println("--- Ignore Rendition: " + outputFormat);
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.out.println("*** ERROR: " + e);
			}
		}
		res.close();

	}

	public void setTestMode(boolean in) {
		testMode = in;
	}

	public void setRenditions(String in) {
		if (in.length() == 0)
			renditions = null;
		else
			renditions = in.split(",");
	}

	public void setSetSpecialApp(boolean in) {
		isSetSpecialApp = in;
	}

	public void setIsOverwrite(boolean in) {
		isOverwrite = in;
	}

	public void setRenderDeep(String in) {
		renderDeep = in;
	}

	public void setRenderOnlyYellow(boolean in) {
		renderOnlyYellow = in;
	}

	public void setConfigFile(String in) {
		configFile = in;
	}
	
	public void setDebug(boolean in) {
		debug = in;
	}

	/**
	 * Transform a object with a specified xsl file
	 * 
	 * @param srcObj
	 * @param xsltId
	 * @param outputFormat
	 * @param description
	 * @return
	 * @throws DfException
	 */
	public String createTransformation(IDfSysObject srcObj, IDfId xsltId,
			String outputFormat) throws DfException {
		// logger.out.println( "--- Create: " + outputFormat );

		try {
			srcObj.removeRendition(outputFormat);
			srcObj.save();
		} catch (Exception e) {
			logger.out.println("--- " + e);
		}

		long start = System.currentTimeMillis();

		IDfXMLTransformOperation idfxmltransformoperation = null;
		IDfXMLTransformNode idfxmltransformnode = null;
		DfClientX dfclientx = new DfClientX();
		IDfSession idfsession = con.getSession();
		idfxmltransformoperation = (IDfXMLTransformOperation) dfclientx
				.getOperation("Transformation");
		IDfPersistentObject idfpersistentobject = null;
		idfpersistentobject = idfsession.getObject(xsltId);
		idfxmltransformnode = (IDfXMLTransformNode) idfxmltransformoperation
				.add(srcObj);
		idfxmltransformnode.setTransformation(idfpersistentobject);
		((DfXMLTransformNode) idfxmltransformnode).setInputObjectId(srcObj
				.getObjectId());
		idfxmltransformnode.setOutputFormat(outputFormat);
		// idfxmltransformoperation.setOperationMonitor(WcmUtil.
		// getDFCXMLOperationMonitor());
		idfxmltransformoperation.setOperationMonitor(new Progress());
		boolean flag = idfxmltransformoperation.execute();

		if (debug) System.out.println("$TRANSFORMTIME$"
				+ (System.currentTimeMillis() - start));

		if (!flag) {
			IDfList idflist = idfxmltransformoperation.getErrors();
			String s1 = "";

			for (int i = 0; i < idflist.getCount(); i++) {
				IDfOperationError idfoperationerror = (IDfOperationError) idflist
						.get(i);
				if (idfoperationerror != null
						&& idfoperationerror.getException() != null)
					s1 = s1 + idfoperationerror.getException().getMessage();
				String s2 = null;
				if (idfoperationerror != null)
					s2 = idfoperationerror.getMessage();
				s1 = s1 + s2;
			}

			return s1;
		} else {
			return null;
		}
	}

	private IDfId getCurrentXsl(IDfId actual) throws DfException {
		IDfId cur = (IDfId) xslCurrentCache.get(actual);
		if (cur != null)
			return cur;

		IDfQuery query = con
				.createQuery("SELECT r_object_id from dm_sysobject where i_chronicle_id = (SELECT i_chronicle_id from dm_sysobject (all) WHERE r_object_id='"
						+ actual + "')");
		IDfCollection res = query
				.execute(con.getSession(), IDfQuery.READ_QUERY);
		if (res.next()) {
			cur = res.getId("r_object_id");
			xslCurrentCache.put(actual, cur);
		}
		res.close();

		if (cur == null) {
			logger.out.println("*** ERROR: CURRENT XLST not found for "
					+ actual);
			cur = actual;
			xslCurrentCache.put(actual, cur);
		}
		return cur;

	}

	public void initialize(PluginNode pNode, DMConnection pCon, ALogger pLogger)
			throws Exception {
		// TODO Auto-generated method stub

	}

	public static class Progress implements IDfOperationMonitor {

		private long last;

		public Progress() {
			last = System.currentTimeMillis();
		}

		public int progressReport(IDfOperation op, int iPercentOpDone,
				IDfOperationStep step, int iPercentStepDone,
				IDfOperationNode node) throws DfException {
			/*
			 * String strStep = iPercentOpDone + "%   " + iPercentStepDone +
			 * "% " + step.getName() + " - " + step.getDescription();
			 * 
			 * String strNode = "     " + (node != null ?
			 * node.getId().toString() : "?" );
			 * 
			 * System.out.println( strStep ); System.out.println( strNode );
			 */
			long x = last;
			last = System.currentTimeMillis();
			if (debug) System.out.println('$' + step.getName() + '$' + (last - x));
			return IDfOperationMonitor.CONTINUE;
		}

		public int reportError(IDfOperationError error) throws DfException {
			return IDfOperationMonitor.CONTINUE;
		}

		public int getYesNoAnswer(IDfOperationError Question)
				throws DfException {
			return IDfOperationMonitor.YES;
		}

	}

	public void destroy(PluginNode node, DMConnection con, ALogger logger) {
		// TODO Auto-generated method stub

	}

}
