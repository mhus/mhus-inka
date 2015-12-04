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

package de.mhu.hair.tools.actions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.documentum.com.DfClientX;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfList;
import com.documentum.operations.IDfOperationError;
import com.documentum.operations.IDfXMLTransformNode;
import com.documentum.operations.IDfXMLTransformOperation;
import com.documentum.operations.nodes.impl.DfXMLTransformNode;

import com.documentum.wcm.WcmUtil;
import com.documentum.xml.xdql.DfXmlQuery;

import de.mhu.hair.api.ApiLayout;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.lib.AFile;

public class OxygenAdapterStatic implements ActionIfc {

	private JFileChooser chooser = new JFileChooser();

	public boolean isEnabled(PluginNode node, DMConnection con,
			IDfPersistentObject[] target) throws Exception {

		return con != null;
	}

	public void actionPerformed(PluginNode node, DMConnection con,
			IDfPersistentObject[] target) throws Exception {

		JComponent component = ((ApiLayout) node.getSingleApi(ApiLayout.class))
				.getMainComponent();

		chooser.setMultiSelectionEnabled(true);
		if (chooser.showOpenDialog(component) != JFileChooser.APPROVE_OPTION)
			return;
		File dir = chooser.getSelectedFile().getParentFile();

		int res = JOptionPane.showOptionDialog(component,
				"What should I process?", "Question", 0,
				JOptionPane.QUESTION_MESSAGE, null, new Object[] { "Selected",
						"All New Queries in Dir", "All Queries in Dir",
						"Cancel" }, "Selected");
		if (res == 3)
			return; // Cancel

		File[] list = null;
		if (res == 0)
			list = chooser.getSelectedFiles();
		else
			list = dir.listFiles();

		for (int i = 0; i < list.length; i++) {

			if (list[i].isFile() && list[i].getName().endsWith(".properties")) {

				try {

					System.out.println(">>> " + list[i].getName());
					String newFile = list[i].getPath();
					newFile = newFile.substring(0, newFile.length()
							- ".properties".length())
							+ ".xml";

					if (!(res == 1 && new File(newFile).exists())) {

						Properties p = new Properties();
						FileInputStream fis = new FileInputStream(list[i]);
						p.load(fis);
						fis.close();

						String domString = doQuery(node, p, true);

						if (domString != null) {

							System.out.println("--- Out: " + newFile);
							AFile.writeFile(new File(newFile), domString);
						} else {
							System.out.println("*** DOM is null");
						}

					} else {
						System.out.println("--- Answer already exists");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

		}

	}

	public void initAction(PluginNode node, DMConnection con, Element config) {
		// TODO Auto-generated method stub

	}

	public void destroyAction() {
		// TODO Auto-generated method stub

	}

	public static String doQuery(PluginNode node, Properties p, boolean secure) {

		// check for special modes ....

		if ("100".equals(p.getProperty("mode"))) {

			try {
				String[] params = p.getProperty("dql").split(",");

				IDfSession session = ((DMConnection) node
						.getSingleApi(DMConnection.class)).getSession();

				IDfSysObject obj = (IDfSysObject) session.getObject(new DfId(
						params[0]));

				ByteArrayInputStream stream = null;
				if ("".equals(params[1]))
					stream = obj.getContent();
				else
					stream = obj.getContentEx2(params[1], 0, null);

				if (stream != null && stream.available() > 0) {
					StringBuffer sb = new StringBuffer();
					while (stream.available() > 0)
						sb.append((char) stream.read());
					return sb.toString();
				}
			} catch (Exception e) {
				return null;
			}

		}

		if ("101".equals(p.getProperty("mode"))) {

			String[] params = p.getProperty("dql").split(",");

			try {

				// exists format definition for this object

				IDfSession session = ((DMConnection) node
						.getSingleApi(DMConnection.class)).getSession();

				String dql = "select child_id, description "
						+ "from dm_relation " + "where parent_id='" + params[0]
						+ "' " + "AND relation_name='wcm_layout_template' "
						+ "AND description like '" + params[1] + ".%'";

				IDfQuery query = ((DMConnection) node
						.getSingleApi(DMConnection.class)).createQuery(dql);
				System.out.println("XSLTOOLS: DQL: " + dql);

				IDfCollection res = query.execute(session, IDfQuery.READ_QUERY);

				if (!res.next()) {
					System.out.println("XSLTOOLS: MSG_FORMAT_NOT_DEFINED:"
							+ params[0] + ":" + params[1]);
					res.close();
					return null;
				}

				IDfId xsltActual = res.getId("child_id");
				res.close();

				// find existing format
				IDfSysObject obj = (IDfSysObject) session.getObject(new DfId(
						params[0]));

				try {
					ByteArrayInputStream stream = obj.getContentEx2(params[1],
							0, null);
					if (stream != null && stream.available() > 0) {
						StringBuffer sb = new StringBuffer();
						while (stream.available() > 0)
							sb.append((char) stream.read());
						return sb.toString();
					}
				} catch (Exception ex) {
					// ex.printStackTrace();
				}

				// render new one
				System.out.println("XSLTOOLS: ... RENDER CB");
				// dql =
				// "SELECT r_object_id from dm_sysobject where i_chronicle_id = (SELECT i_chronicle_id from dm_sysobject (all) WHERE r_object_id='"
				// + xsltActual +"')";
				dql = "SELECT r_object_id from dm_sysobject where i_chronicle_id = '"
						+ xsltActual + "'";
				System.out.println("XSLTOOLS: DQL: " + dql);
				query = ((DMConnection) node.getSingleApi(DMConnection.class))
						.createQuery(dql);
				res = query.execute(session, IDfQuery.READ_QUERY);

				if (!res.next()) {
					System.out.println("XSLTOOLS: MSG_TEMPLATE_NOT_FOUND:"
							+ params[0] + ":" + params[1]);
					res.close();
					return null;
				}

				IDfId layoutId = res.getId("r_object_id");
				res.close();

				String result = createTransformation(session, obj, layoutId,
						params[1]);
				if (result != null) {
					System.out.println("XSLTOOLS: MSG_TRANSFORMATION:" + result
							+ params[0] + ":" + params[1]);
					return null;
				}
				if (params.length > 2 && "1".equals(params[2])) {
					obj.setString("a_special_app", result == null ? "2" : "1");
					obj.save();
				}
				// load and return rendition

				ByteArrayInputStream stream = obj.getContentEx2(params[1], 0,
						null);
				if (stream != null && stream.available() > 0) {
					StringBuffer sb = new StringBuffer();
					while (stream.available() > 0)
						sb.append((char) stream.read());
					return sb.toString();
				}

			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("XSLTOOLS: MSG_EXCEPTION:" + e + ":"
						+ params[0] + ":" + params[1]);
				return null;
			}
		}

		if (!secure || p.getProperty("dql").toUpperCase().startsWith("SELECT ")) {
			DfXmlQuery q = new DfXmlQuery();
			q.init();
			q.setDql(p.getProperty("dql"));
			if (p.get("root.node") != null)
				q.setRootNode(p.getProperty("root.node"));
			if (p.get("upper.case") != null)
				q.useUpperCaseTagNames();
			if (p.get("content") != null)
				q.includeContent(p.getProperty("content").equals("1"));
			if (p.get("content.as.link") != null)
				q
						.setContentAsLink(p.getProperty("content.as.link")
								.equals("1"));
			if (p.get("content.tag") != null)
				q.setContentTag(p.getProperty("content.tag"));
			if (p.get("encoding") != null)
				q.setContentEncoding(p.getProperty("encoding"));
			if (p.get("content.format") != null)
				q.setContentFormat(p.getProperty("content.format"));
			if (p.get("link.base") != null)
				q.setLinkBase(p.getProperty("link.base"));
			if (p.get("max.rows") != null)
				q.setMaxRows(Integer.parseInt(p.getProperty("max.rows")));
			if (p.get("meta.data.as.attr") != null)
				q.setMetaDataAsAttributes(p.getProperty("meta.data.as.attr")
						.equals("1"));
			if (p.get("repeating.as.nested.tag") != null)
				q.setRepeatingAsNestedTag(p
						.getProperty("repeating.as.nested.tag"));
			if (p.get("repeating.as.nested") != null)
				q.setRepeatingAsNested(p.getProperty("repeating.as.nested")
						.equals("1"));
			if (p.get("repeating.include.index") != null)
				q.setRepeatingIncludeIndex(p.getProperty(
						"repeating.include.index").equals("1"));
			if (p.get("row.id.attr.name") != null)
				q.setRowIDAttrName(p.getProperty("row.id.attr.name"));
			if (p.get("row.id.column") != null)
				q.setRowIDColumn(p.getProperty("row.id.column"));
			if (p.get("rowset.tag") != null)
				q.setRowsetTag(p.getProperty("rowset.tag"));
			if (p.get("stylesheet") != null)
				q.setStyleSheet(p.getProperty("stylesheet"));
			if (p.get("virtual.doc") != null)
				q.setVirtualDocumentNested(p.getProperty("virtual.doc").equals(
						"1"));
			if (p.get("given.case") != null)
				q.useGivenCaseTagNames();
			if (p.get("null.attr.indicator") != null)
				q.useNullAttributeIndicator(p
						.getProperty("null.attr.indicator").equals("1"));

			q.execute(p.getProperty("mode"), ((DMConnection) node
					.getSingleApi(DMConnection.class)).getSession()); // Yes ...
																		// deprecated
																		// , but
																		// this
																		// one
																		// works
																		// !!!

			Document dom = q.getXMLDOM();

			if (dom != null) {
				try {
					OutputFormat format = new OutputFormat(dom);
					format.setIndenting(true);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();

					XMLSerializer serializer = new XMLSerializer(baos, format);
					serializer.serialize(dom);
					baos.close();
					return new String(baos.toByteArray());
				} catch (Exception e) {
					System.out.println("*** DOM Error: " + e);
					return null;
				}
			} else {
				System.out.println("*** DOM is null");
			}

			return null;

		} else {
			System.out.println("*** DQL is not a SELECT !!!!");
		}
		return null;
	}

	private static String createTransformation(IDfSession session,
			IDfSysObject srcObj, IDfId xsltId, String outputFormat)
			throws DfException {
		// remove existing rendition
		try {
			srcObj.removeRendition(outputFormat);
			srcObj.save();
		} catch (Exception e) {
		}

		// create operation
		IDfXMLTransformOperation idfxmltransformoperation = null;
		IDfXMLTransformNode idfxmltransformnode = null;
		DfClientX dfclientx = new DfClientX();
		idfxmltransformoperation = (IDfXMLTransformOperation) dfclientx
				.getOperation("Transformation");
		IDfPersistentObject idfpersistentobject = null;
		idfpersistentobject = session.getObject(xsltId);
		idfxmltransformnode = (IDfXMLTransformNode) idfxmltransformoperation
				.add(srcObj);
		idfxmltransformnode.setTransformation(idfpersistentobject);
		((DfXMLTransformNode) idfxmltransformnode).setInputObjectId(srcObj
				.getObjectId());
		idfxmltransformnode.setOutputFormat(outputFormat);
		idfxmltransformoperation.setOperationMonitor(WcmUtil
				.getDFCXMLOperationMonitor());
		boolean flag = idfxmltransformoperation.execute();
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

	public String getTitle() {
		return null;
	}

}
