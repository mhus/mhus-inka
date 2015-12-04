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

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.w3c.dom.Element;

import com.documentum.com.DfClientX;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfVirtualDocument;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfList;
import com.documentum.operations.IDfCheckinNode;
import com.documentum.operations.IDfCheckinOperation;
import com.documentum.operations.IDfCheckoutNode;
import com.documentum.operations.IDfCheckoutOperation;
import com.documentum.operations.IDfOperation;
import com.documentum.operations.IDfOperationError;
import com.documentum.operations.IDfOperationMonitor;
import com.documentum.operations.IDfOperationNode;
import com.documentum.operations.IDfOperationStep;
import com.documentum.operations.IDfXMLTransformNode;
import com.documentum.operations.IDfXMLTransformOperation;
import com.documentum.operations.nodes.impl.DfXMLTransformNode;
import com.documentum.wcm.WcmUtil;

import de.mhu.hair.api.ApiLayout;
import de.mhu.hair.api.ApiPersistent;
import de.mhu.hair.api.ApiSystem;
import de.mhu.hair.api.ApiPersistent.PersistentManager;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.gui.LoggerPanel;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.tools.DctmTool;
import de.mhu.hair.tools.ObjectTool;
import de.mhu.lib.AFile;
import de.mhu.lib.AString;
import de.mhu.lib.AThread;
import de.mhu.lib.ATimerTask;
import de.mhu.lib.swing.FileExtFilter;

public class WebPublisherAssistance implements ActionIfc {

	private JPanel panel;
	private JTextField tWPIncludes;
	private JTextField tLocalIncludes;
	private JTextField tFormats;
	private JTextField tScsIds;
	private JButton bExport;
	private JButton bCheckin;
	private DMConnection con;
	private PluginNode node;
	private ApiLayout layout;
	private Element config;
	private Timer timer;
	private LoggerPanel logger;
	private JButton bRender;
	private Vector targets;
	private DefaultListModel mExamples;
	private JList lExamples;
	private JTextField tScsFormats;
	private JButton bPublish;
	private JButton bWebView;
	private JTextField tWVMap;
	private JTextField tWVDir;
	private JButton bAll;
	private JCheckBox cbDebugRender;
	private JTextField tWVHeader;
	private JTextField tWVFooter;
	private JFileChooser chooser = new JFileChooser();
	private PersistentManager persistents;

	public void initAction(PluginNode pNode, DMConnection pCon, Element pConfig) {
		layout = ((ApiLayout) pNode.getSingleApi(ApiLayout.class));
		timer = ((ApiSystem) pNode.getSingleApi(ApiSystem.class)).getTimer();
		persistents = ((ApiPersistent) pNode.getSingleApi(ApiPersistent.class))
				.getManager(pConfig != null ? pConfig
						.getAttribute("persistent") : null);
		config = pConfig;
	}

	public void destroyAction() {

	}

	public boolean isEnabled(PluginNode pNode, DMConnection pCon,
			IDfPersistentObject[] pTarget) throws Exception {
		return pCon != null;
	}

	public void actionPerformed(PluginNode pNode, DMConnection pCon,
			IDfPersistentObject[] pTarget) throws Exception {

		con = pCon;
		node = pNode;
		if (panel == null) {
			initUI();
			File f = new File(persistents.getProperty("wpa.properties",
					"wpa.properties"));
			actionConfigLoad(f);
		}
		if (layout.isComponent(panel))
			layout.removeComponent(panel);
		else {
			targets = new Vector();
			if (pTarget != null)
				for (int i = 0; i < pTarget.length; i++)
					findExampleTargets(pTarget[i]);
			mExamples.removeAllElements();
			for (Enumeration i = targets.elements(); i.hasMoreElements();)
				mExamples.addElement(ObjectTool.getPath((IDfPersistentObject) i
						.nextElement()));
			layout.setComponent(panel, config);
		}

	}

	private void findExampleTargets(IDfPersistentObject object)
			throws DfException {
		if (object instanceof IDfDocument) {
			targets.add(object);
			return;
		} else if (object instanceof IDfFolder) {
			IDfCollection res = ((IDfFolder) object).getContents("r_object_id");
			Vector folders = new Vector();
			while (res.next()) {
				IDfPersistentObject child = con.getPersistentObject(res
						.getString("r_object_id"));
				folders.add(child);
			}
			res.close();

			// iterate childs
			for (Enumeration i = folders.elements(); i.hasMoreElements();) {
				findExampleTargets((IDfPersistentObject) i.nextElement());
			}
		}
	}

	private void initUI() {
		panel = new JPanel();

		// ---------------------------------------------------
		// Config

		JPanel confPanel = new JPanel();
		confPanel.setLayout(new GridLayout(11, 2));

		// 1
		confPanel.add(new JLabel("WP Includes: "));
		tWPIncludes = new JTextField("");
		confPanel.add(tWPIncludes);

		// 2
		confPanel.add(new JLabel("Local Includes: "));
		tLocalIncludes = new JTextField("");
		confPanel.add(tLocalIncludes);

		// 3
		confPanel.add(new JLabel("Formats: "));
		tFormats = new JTextField();
		confPanel.add(tFormats);

		// 4
		confPanel.add(new JLabel("SCS Export Ids: "));
		tScsIds = new JTextField("");
		confPanel.add(tScsIds);

		// 5
		confPanel.add(new JLabel("SCS Export Formats: "));
		tScsFormats = new JTextField("");
		confPanel.add(tScsFormats);

		// 6
		confPanel.add(new JLabel("WebView Map File: "));
		tWVMap = new JTextField("");
		confPanel.add(tWVMap);

		// 7
		confPanel.add(new JLabel("WebView Export Dir: "));
		tWVDir = new JTextField("");
		confPanel.add(tWVDir);

		// 8
		confPanel.add(new JLabel("WebView Header: "));
		tWVHeader = new JTextField("");
		confPanel.add(tWVHeader);

		// 9
		confPanel.add(new JLabel("WebView Footer: "));
		tWVFooter = new JTextField("");
		confPanel.add(tWVFooter);

		// 10
		confPanel.add(new JLabel("Debug Rendering: "));
		cbDebugRender = new JCheckBox("");
		confPanel.add(cbDebugRender);

		// 11
		JPanel panel4 = new JPanel();
		panel4.setLayout(new BoxLayout(panel4, BoxLayout.X_AXIS));
		JButton button = new JButton("Save");
		button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				actionConfigSave();
			}

		});
		panel4.add(button);

		button = new JButton("Load");
		button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				actionConfigLoad();
			}

		});
		panel4.add(button);

		confPanel.add(panel4);
		// -----------------------------------------------------------------
		// Actions

		JPanel actPanel = new JPanel();
		actPanel.setLayout(new GridLayout(8, 1));

		bExport = new JButton("Export to Local");
		bExport.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				actionExport();
			}

		});
		actPanel.add(bExport);

		actPanel.add(new JLabel(" "));

		bCheckin = new JButton("Checkin to Docbase");
		bCheckin.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				bCheckin.setEnabled(false);
				timer.schedule(new ATimerTask() {

					public void run0() throws Exception {
						actionCheckin();
					}

					protected void onFinal(boolean isError) {
						bCheckin.setEnabled(true);
					}

				}, 1);

			}

		});
		actPanel.add(bCheckin);

		bPublish = new JButton("Publish Includes");
		bPublish.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				bPublish.setEnabled(false);
				timer.schedule(new ATimerTask() {

					public void run0() throws Exception {
						actionPublish();
					}

					protected void onFinal(boolean isError) {
						bPublish.setEnabled(true);
					}

				}, 1);

			}

		});
		actPanel.add(bPublish);

		bRender = new JButton("Render Examples");
		bRender.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				bRender.setEnabled(false);
				timer.schedule(new ATimerTask() {

					public void run0() throws Exception {
						actionRender();
					}

					protected void onFinal(boolean isError) {
						bRender.setEnabled(true);
					}

				}, 1);

			}

		});
		actPanel.add(bRender);

		bWebView = new JButton("Export Example Renditions");
		bWebView.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				bWebView.setEnabled(false);
				timer.schedule(new ATimerTask() {

					public void run0() throws Exception {
						actionWebView();
					}

					protected void onFinal(boolean isError) {
						bWebView.setEnabled(true);
					}

				}, 1);

			}

		});
		actPanel.add(bWebView);

		actPanel.add(new JLabel(" "));

		bAll = new JButton("All Import");
		bAll.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				bAll.setEnabled(false);
				timer.schedule(new ATimerTask() {

					public void run0() throws Exception {
						actionAllImport();
					}

					protected void onFinal(boolean isError) {
						bAll.setEnabled(true);
					}

				}, 1);

			}

		});
		actPanel.add(bAll);

		// -----------------------------------------------------------------
		// Example list

		mExamples = new DefaultListModel();
		lExamples = new JList(mExamples);
		JScrollPane spExamples = new JScrollPane(lExamples);

		// -----------------------------------------------------------------
		// compose

		JPanel panel1 = new JPanel();
		panel1.setLayout(new BorderLayout());
		panel1.add(confPanel, BorderLayout.NORTH);

		JPanel panel2 = new JPanel();
		panel2.setLayout(new BorderLayout());
		panel2.add(actPanel, BorderLayout.NORTH);
		logger = new LoggerPanel(null, null);
		panel2.add(logger.getMainPanel(), BorderLayout.CENTER);

		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Config", panel1);
		tabs.addTab("Examples", spExamples);
		tabs.addTab("Actions", panel2);

		panel.setLayout(new BorderLayout());
		panel.add(tabs, BorderLayout.CENTER);

		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setFileFilter(new FileExtFilter("Properties",
				new String[] { "properties" }));
		chooser.setMultiSelectionEnabled(false);
	}

	protected void actionConfigLoad() {

		if (chooser.showOpenDialog(panel) != JFileChooser.APPROVE_OPTION)
			return;

		actionConfigLoad(chooser.getSelectedFile());

	}

	protected void actionConfigLoad(File file) {
		try {
			Properties prop = new Properties();
			FileInputStream fis = new FileInputStream(file);
			prop.load(fis);
			fis.close();

			// 1
			tWPIncludes.setText(prop.getProperty("wp_include"));

			// 2
			tLocalIncludes.setText(prop.getProperty("local_include"));

			// 3
			tFormats.setText(prop.getProperty("formats"));

			// 4
			tScsIds.setText(prop.getProperty("scs_ids"));

			// 5
			tScsFormats.setText(prop.getProperty("scs_formats"));

			// 6
			tWVMap.setText(prop.getProperty("wv_map"));

			// 7
			tWVDir.setText(prop.getProperty("wv_dir"));

			// 8
			tWVHeader.setText(prop.getProperty("wv_header"));

			// 9
			tWVFooter.setText(prop.getProperty("wv_footer"));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void actionConfigSave() {

		if (chooser.showSaveDialog(panel) != JFileChooser.APPROVE_OPTION)
			return;

		File f = chooser.getSelectedFile();
		if (!f.getName().endsWith(".properties"))
			f = new File(f.getAbsolutePath() + ".properties");
		actionConfigSave(f);
	}

	protected void actionConfigSave(File file) {
		try {
			Properties prop = new Properties();
			FileOutputStream fos = new FileOutputStream(file);

			// 1
			prop.setProperty("wp_include", tWPIncludes.getText());

			// 2
			prop.setProperty("local_include", tLocalIncludes.getText());

			// 3
			prop.setProperty("formats", tFormats.getText());

			// 4
			prop.setProperty("scs_ids", tScsIds.getText());

			// 5
			prop.setProperty("scs_formats", tScsFormats.getText());

			// 6
			prop.setProperty("wv_map", tWVMap.getText());

			// 7
			prop.setProperty("wv_dir", tWVDir.getText());

			// 8
			prop.setProperty("wv_header", tWVHeader.getText());

			// 9
			prop.setProperty("wv_footer", tWVFooter.getText());

			prop.store(fos, "");
			fos.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void actionAllImport() throws Exception {

		logger.printMsgLine();
		actionCheckin();
		actionPublish();
		logger.printMsg("Sleep");
		AThread.sleep(2000);
		actionRender();
		actionWebView();
		logger.printMsgLine();

	}

	protected void actionWebView() throws Exception {
		logger.printMsg("Rendition Export");

		String[] renditions = tScsFormats.getText().split(",");
		if (renditions.length == 0)
			renditions = null;
		Properties properties = new Properties();
		FileInputStream fis = new FileInputStream(tWVMap.getText());
		properties.load(fis);
		fis.close();
		File tmpDir = new File(tWVDir.getText());
		tmpDir.mkdirs();

		FileOutputStream fos = new FileOutputStream(new File(tmpDir,
				"index.html"));
		fos.write("<html><body>".getBytes());

		logger.setMaximum(targets.size());
		int cnt = 0;
		for (Enumeration i = targets.elements(); i.hasMoreElements();) {
			cnt++;
			logger.setValue(cnt);
			IDfSysObject obj = (IDfSysObject) i.nextElement();
			Vector files = webViewExport(obj, properties, tmpDir, renditions);

			fos.write(("<p><b>" + obj.getObjectName() + "</b><br>").getBytes());
			for (Iterator j = files.iterator(); j.hasNext();) {
				String fileName = (String) j.next();
				fos.write(("<a href=\"" + fileName + "\" target=_blank>"
						+ fileName + "</a><br>").getBytes());
			}
			fos.write("</p>".getBytes());
		}

		fos.write("</body></html>".getBytes());
		fos.close();

		logger.printMsg("FINISH");

	}

	private Vector webViewExport(IDfSysObject obj, Properties properties,
			File tmpDir, String[] renditions) throws DfException {
		String id = obj.getObjectId().toString();
		String dql = "select f.dos_extension,f.name "
				+ "FROM dm_sysobject (ALL) s, dmr_content r, dm_store t, dm_format f "
				+ "WHERE r_object_id = ID('" + id + "') "
				+ "AND ANY (parent_id=ID('" + id + "') AND page = 0) "
				+ "AND r.storage_id=t.r_object_id "
				+ "AND f.r_object_id=r.format";

		IDfQuery query = con.createQuery(dql);
		IDfCollection res = query
				.execute(con.getSession(), IDfQuery.READ_QUERY);

		Vector out = new Vector();
		while (res.next()) {

			try {
				String name = res.getString("name");
				boolean ok = false;
				if (renditions != null) {
					for (int i = 0; i < renditions.length; i++)
						if (renditions[i].equals(name)) {
							ok = true;
							break;
						}
				} else
					ok = true;

				if (ok) {
					logger.out.println(">>> " + name + "@"
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

					content = AFile.readFile(new File(tWVHeader.getText()))
							+ content
							+ AFile.readFile(new File(tWVFooter.getText()));

					/*
					 * content = "<html><head>" +
					 * "<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">"
					 * + "<title></title>" +
					 * "<link rel=\"stylesheet\" href=\"file:///D:/tmp_trans/COMMON/_STYLES/cont.css\">"
					 * +
					 * "<script type=\"text/javascript\" src=\"file:///D:/tmp_trans/COMMON/_JS/common.js\"></script></head>"
					 * + "<body class=\"body_1\">\n" + content +
					 * "</body></html>";
					 */

					String fileName = obj.getObjectName() + "_"
							+ obj.getString("language_code") + "_" + name + "."
							+ ext;
					File f = new File(tmpDir, fileName);
					logger.out.println("--- WRITE: " + f.getAbsolutePath());
					AFile.writeFile(f, content);
					out.add(fileName);
				}
			} catch (Exception e) {
				logger.out.println(e.toString());
			}
		}
		res.close();

		return out;

	}

	protected void actionPublish() throws DfException {
		logger.printMsg("Publish");
		String[] ids = tScsIds.getText().split(",");
		for (int i = 0; i < ids.length; i++) {
			String ret = con
					.getSession()
					.apiGet(
							"apply",
							ids[i]
									+ ",WEBCACHE_PUBLISH,ARGUMENTS,S,-full_refresh false -method_trace_level 0");
			if (con.getSession().apiExec("next", ret))
				// write the result to the log file
				logger.out.println("Apply result: "
						+ con.getSession().apiGet("dump", ret));
			else
				logger.out.println("Error with apply command: "
						+ con.getSession().apiGet("getmessage", ""));
			con.getSession().apiExec("close", ret);
		}
		logger.printMsg("FINISH");
	}

	protected void actionRender() {
		logger.printMsg("Render");

		if (cbDebugRender.isSelected())
			System.setProperty("com.documentum.xml.jaxp.DfTransformerFactory",
					"de.mhu.hair.xml.HairTransformerFactoryImpl");
		// else
		// System.setProperty( "com.documentum.xml.jaxp.DfTransformerFactory",
		// null );

		String[] renditions = tScsFormats.getText().split(",");
		if (renditions.length == 0
				|| (renditions.length == 1 && renditions[0].length() == 0))
			renditions = null;

		logger.setMaximum(targets.size());
		int cnt = 0;
		for (Enumeration i = targets.elements(); i.hasMoreElements();)
			try {
				cnt++;
				logger.setValue(cnt);
				transform((IDfPersistentObject) i.nextElement(), renditions);
			} catch (DfException e) {
				// TODO Auto-generated catch block
				e.printStackTrace(logger.err);
			}

		logger.printMsg("FINISH");

	}

	private void transform(IDfPersistentObject object, String[] renditions)
			throws DfException {

		logger.out.println(">>> Object: " + ObjectTool.getPath(object));

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

				if (ok) {
					logger.out.println("--- Transform " + object.getObjectId()
							+ " XSLID: " + xsltId + " (" + xsltActual
							+ ") Type: " + outputFormat);
					createTransformation((IDfSysObject) object, xsltId,
							outputFormat);
					logger.out.println("--- FINISH");
				}

			} catch (DfException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.out.println("*** ERROR: " + e);
			}
		}
		res.close();

	}

	public boolean createTransformation(IDfSysObject srcObj, IDfId xsltId,
			String outputFormat) throws DfException {
		// logger.out.println( "--- Create: " + outputFormat );

		try {
			srcObj.removeRendition(outputFormat);
			srcObj.save();
		} catch (Exception e) {
			logger.out.println("--- " + e);
		}

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
		idfxmltransformoperation.setOperationMonitor(WcmUtil
				.getDFCXMLOperationMonitor());
		boolean flag = idfxmltransformoperation.execute();
		if (!flag) {
			IDfList idflist = idfxmltransformoperation.getErrors();
			String s1 = "";
			Object obj = null;
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

			return true;
		} else {
			return false;
		}
	}

	private IDfId getCurrentXsl(IDfId actual) throws DfException {
		IDfId cur = null;
		IDfQuery query = con
				.createQuery("SELECT r_object_id from dm_sysobject where i_chronicle_id = (SELECT i_chronicle_id from dm_sysobject (all) WHERE r_object_id='"
						+ actual + "')");
		IDfCollection res = query
				.execute(con.getSession(), IDfQuery.READ_QUERY);
		if (res.next()) {
			cur = res.getId("r_object_id");
		}
		res.close();

		if (cur == null) {
			logger.out.println("*** ERROR: CURRENT XSLT not found for "
					+ actual);
			cur = actual;
		}
		return cur;

	}

	protected void actionCheckin() throws Exception {

		logger.printMsg("CI");

		File localFolder = new File(tLocalIncludes.getText());
		localFolder.mkdirs();

		// find changed files
		Hashtable changed = new Hashtable();
		findLocalChanged(localFolder, changed);

		// checkin changed files
		logger.setMaximum(changed.size());
		int cnt = 0;
		for (Iterator i = changed.entrySet().iterator(); i.hasNext();) {
			cnt++;
			logger.setValue(cnt);

			Map.Entry entry = (Map.Entry) i.next();
			File file = new File((String) entry.getKey());
			String id = (String) entry.getValue();
			File folder = file.getParentFile();
			String name = file.getName();

			// check out
			IDfSysObject sysObj = con.getExistingObject(id);

			if (sysObj.isCheckedOut()) {
				logger.out.println("+++ LOCKED: " + file.getAbsolutePath());
				sysObj.cancelCheckout();
				sysObj.save();
				if (sysObj.isCheckedOut()) {
					logger.out.println("*** LOCKED ");
					continue;
				}
			}

			logger.out.println(">>> CI: " + file.getAbsolutePath());

			IDfCheckoutOperation coOperation = DMConnection.clientx
					.getCheckoutOperation();
			IDfCheckoutNode coNode;
			if (sysObj.isVirtualDocument()) {
				IDfVirtualDocument vDoc = sysObj.asVirtualDocument("CURRENT",
						false);
				coNode = (IDfCheckoutNode) coOperation.add(vDoc);
			} else {
				coNode = (IDfCheckoutNode) coOperation.add(sysObj);
			}
			executeOperation(coOperation);

			String coFilePath = coNode.getFilePath();

			// copy
			File coFile = new File(coFilePath);
			AFile.copyFile(file, coFile);

			// check in
			IDfCheckinOperation ciOperation = DMConnection.clientx
					.getCheckinOperation();
			IDfCheckinNode ciNode;
			if (sysObj.isVirtualDocument()) {
				IDfVirtualDocument vDoc = sysObj.asVirtualDocument("CURRENT",
						false);
				ciNode = (IDfCheckinNode) ciOperation.add(vDoc);
			} else {
				ciNode = (IDfCheckinNode) ciOperation.add(sysObj);
			}

			// Other options for setCheckinVersion: VERSION_NOT_SET, NEXT_MAJOR,
			// NEXT_MINOR, BRANCH_VERSION
			ciOperation.setCheckinVersion(IDfCheckinOperation.SAME_VERSION);

			// see sample: Operations- Execute and Check Errors
			executeOperation(ciOperation);

			String newId = ciNode.getNewObjectId().toString();

			// rewrite props

			Properties prop = new Properties();
			try {
				FileInputStream fis = new FileInputStream(folder
						.getAbsolutePath()
						+ '/' + ".properties");
				prop.load(fis);
				fis.close();
			} catch (FileNotFoundException fnf) {
			}
			prop.setProperty(name, AString.join(new String[] { newId,
					String.valueOf(file.lastModified()),
					String.valueOf(file.length()) }, ','));

			FileOutputStream fos = new FileOutputStream(folder
					.getAbsolutePath()
					+ '/' + ".properties");
			prop.store(fos, "");
			fos.close();
		}
		logger.printMsg("FINISH");

	}

	protected void findLocalChanged(File folder, Hashtable changed)
			throws Exception {

		Properties prop = new Properties();
		try {
			FileInputStream fis = new FileInputStream(folder.getAbsolutePath()
					+ '/' + ".properties");
			prop.load(fis);
			fis.close();
		} catch (FileNotFoundException fnf) {
			fnf.printStackTrace();
			return;
		}

		for (Iterator i = prop.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			String name = (String) entry.getKey();
			String[] parts = ((String) entry.getValue()).split(",");

			String id = parts[0];
			long modified = Long.parseLong(parts[1]);
			long length = Long.parseLong(parts[2]);

			File file = new File(folder, name);
			if (file.isFile()
					&& file.exists()
					&& (file.lastModified() != modified || file.length() != length)) {
				logger.out.println("--- " + file.getAbsolutePath());
				changed.put(file.getAbsolutePath(), id);
			}

		}

		// iterate folders
		File[] list = folder.listFiles();
		for (int i = 0; i < list.length; i++)
			if (list[i].isDirectory() && !list[i].getName().startsWith("."))
				findLocalChanged(list[i], changed);

	}

	protected void actionExport() {

		bExport.setEnabled(false);
		timer.schedule(new ATimerTask() {

			public void run0() throws Exception {
				File localFolder = new File(tLocalIncludes.getText());
				localFolder.mkdirs();

				IDfFolder dmFolder = (IDfFolder) con.getSession()
						.getObjectByPath(tWPIncludes.getText());

				Hashtable documents = new Hashtable();
				findExportDocs(dmFolder, localFolder, documents);

				// export files
				for (Iterator i = documents.entrySet().iterator(); i.hasNext();) {
					Map.Entry entry = (Map.Entry) i.next();
					IDfDocument document = (IDfDocument) entry.getKey();
					File folder = (File) entry.getValue();
					folder.mkdirs();

					File file = new File(folder, document.getObjectName());
					logger.out.println(">>> EXPORT: " + file);

					document.getFile(file.getAbsolutePath());

					// set properties
					Properties prop = new Properties();
					try {
						FileInputStream fis = new FileInputStream(folder
								.getAbsolutePath()
								+ '/' + ".properties");
						prop.load(fis);
						fis.close();
					} catch (FileNotFoundException fnf) {
					}
					prop.setProperty(document.getObjectName(), AString.join(
							new String[] { document.getObjectId().toString(),
									String.valueOf(file.lastModified()),
									String.valueOf(file.length()) }, ','));
					FileOutputStream fos = new FileOutputStream(folder
							.getAbsolutePath()
							+ '/' + ".properties");
					prop.store(fos, "");
					fos.close();
				}
				logger.printMsg("FINISH");
			}

			protected void onFinal(boolean isError) {
				bExport.setEnabled(true);
			}

		}, 1);

	}

	private void findExportDocs(IDfFolder dmFolder, File localFolder,
			Hashtable documents) throws DfException {

		// remove the properties file
		File propFile = new File(localFolder, ".properties");
		if (propFile.exists())
			propFile.delete();

		// find childs
		IDfCollection res = dmFolder.getContents("r_object_id");
		Vector folders = new Vector();
		while (res.next()) {
			IDfSysObject child = con.getExistingObject(res
					.getString("r_object_id"));
			if (DctmTool.isFolder(child)) {
				folders.add(child);
			} else if (child instanceof IDfDocument) {
				documents.put(child, localFolder);
			} else
				logger.out.println("+++ UNKNOWN: " + ObjectTool.getPath(child));
		}
		res.close();

		// iterate subfolders
		for (Enumeration i = folders.elements(); i.hasMoreElements();) {
			IDfFolder nextDmFolder = (IDfFolder) i.nextElement();
			File nextLocalFolder = new File(localFolder, nextDmFolder
					.getObjectName());
			findExportDocs(nextDmFolder, nextLocalFolder, documents);
		}

	}

	public static void executeOperation(IDfOperation operation)
			throws DfException {
		// see the operation monitor sample
		operation.setOperationMonitor(new Progress());

		// Execute the operation
		boolean executeFlag = operation.execute();

		// Check if any errors occured during the execution of the operation
		if (executeFlag == false) {
			// Get the list of errors
			IDfList errorList = operation.getErrors();
			String message = "";
			IDfOperationError error = null;

			// Iterate through the errors and concatenate the error messages
			for (int i = 0; i < errorList.getCount(); i++) {
				error = (IDfOperationError) errorList.get(i);
				message += error.getMessage();
			}

			System.out.println("Errors:");
			System.out.println(message);
			// Create a DfException to report the errors
			// DfException e = new DfException();
			// e.setMessage(message);
			// throw e;
		}
	}

	public static class Progress implements IDfOperationMonitor {
		public int progressReport(IDfOperation op, int iPercentOpDone,
				IDfOperationStep step, int iPercentStepDone,
				IDfOperationNode node) throws DfException {
			String strStep = iPercentOpDone + "%   " + iPercentStepDone + "% "
					+ step.getName() + " - " + step.getDescription();

			String strNode = "     "
					+ (node != null ? node.getId().toString() : "?");

			System.out.println(strStep);
			System.out.println(strNode);
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

	public String getTitle() {
		return null;
	}

}
