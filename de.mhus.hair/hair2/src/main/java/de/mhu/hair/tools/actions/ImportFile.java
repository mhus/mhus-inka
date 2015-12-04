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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.Timer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.w3c.dom.Element;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfVirtualDocument;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfList;
import com.documentum.operations.IDfCheckinNode;
import com.documentum.operations.IDfCheckinOperation;
import com.documentum.operations.IDfCheckoutNode;
import com.documentum.operations.IDfCheckoutOperation;
import com.documentum.operations.IDfFormatRecognizer;
import com.documentum.operations.IDfOperation;
import com.documentum.operations.IDfOperationError;
import com.documentum.operations.IDfOperationMonitor;
import com.documentum.operations.IDfOperationNode;
import com.documentum.operations.IDfOperationStep;

import de.mhu.hair.api.ApiLayout;
import de.mhu.hair.api.ApiObjectChanged;
import de.mhu.hair.api.ApiSystem;
import de.mhu.hair.api.ApiToolbar;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.plugin.dctm.ApiTypes;
import de.mhu.hair.plugin.ui.SimpleTextWindow;
import de.mhu.hair.plugin.ui.TypesTreePanel;
import de.mhu.hair.tools.ObjectChangedTool;
import de.mhu.hair.tools.ObjectTool;
import de.mhu.lib.AFile;
import de.mhu.lib.ASql;
import de.mhu.lib.ATimerTask;
import de.mhu.lib.swing.FileExtFilter;

public class ImportFile implements ActionIfc {

	private JFileChooser chooser = new JFileChooser();
	private JComboBox cbType;
	private JCheckBox importNew;
	private JCheckBox importRelations;
	private SimpleTextWindow window;
	
	public ImportFile() {
		chooser.addChoosableFileFilter(new FileExtFilter("Images",
				new String[] { "gif", "png", "jpg" }));
		chooser.addChoosableFileFilter(new FileExtFilter("XML Content",
				new String[] { "xml" }));

		chooser.setMultiSelectionEnabled(true);
		JPanel panel = new JPanel();
		JPanel panel1 = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel1.setLayout(new BorderLayout());

		cbType = new JComboBox();
		cbType.addItem("Ignore");
		cbType.addItem("Skip");
		cbType.addItem("VERSION_NOT_SET");
		cbType.addItem("NEXT_MAJOR");
		cbType.addItem("NEXT_MINOR");
		cbType.addItem("BRANCH_VERSION");
		cbType.addItem("Overwrite Content");

		panel.add(new JLabel("Doubles:"));
		panel.add(cbType);
		importNew = new JCheckBox("Import new Files");
		importNew.setSelected(true);
		panel.add(importNew);
		panel1.add(panel, BorderLayout.NORTH);
		chooser.setAccessory(panel1);

		importRelations = new JCheckBox("Export Relations Infos");
		importRelations.setSelected(false);
		importRelations.setEnabled(true);
		panel.add(importRelations);
	}

	public boolean isEnabled(PluginNode node, DMConnection con,
			IDfPersistentObject[] target) throws Exception {

		if (target == null || target.length != 1)
			return false;
		return target[0] instanceof IDfFolder;
	}

	public void actionPerformed(final PluginNode node, final DMConnection con,
			final IDfPersistentObject[] target) throws Exception {

		if (window == null)
			window = new SimpleTextWindow(node, "Import");
			
		Timer timer = ((ApiSystem) node.getSingleApi(ApiSystem.class))
				.getTimer();

		if (target == null || target.length != 1)
			return;
		if (!(target[0] instanceof IDfFolder))
			return;

		JComponent component = ((ApiLayout) node.getSingleApi(ApiLayout.class))
				.getMainComponent();

		// files

		if (chooser.showOpenDialog(component) != JFileChooser.APPROVE_OPTION)
			return;

		String t = null;
		if (!importRelations.isSelected()
				&& (importNew.isSelected() || cbType.getSelectedIndex() == 0)) {
			// type
			TypesTreePanel type = new TypesTreePanel(con, (ApiTypes) node
					.getSingleApi(ApiTypes.class), "dm_document", null, null);
			JButton bOk = new JButton("OK");
			bOk.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					SwingUtilities
							.getWindowAncestor((JComponent) e.getSource())
							.dispose();

				}

			});
			JPanel typePanel = new JPanel();
			typePanel.setLayout(new BorderLayout());
			typePanel.add(type, BorderLayout.CENTER);
			typePanel.add(bOk, BorderLayout.SOUTH);

			JOptionPane.showOptionDialog(((ApiToolbar) node
					.getSingleApi(ApiToolbar.class)).getMainComponent(),
					"Insert name and type", "Create Folder", 0,
					JOptionPane.QUESTION_MESSAGE, null,
					new Object[] { typePanel }, null);

			t = type.getSelected();
			if (t == null)
				return;
		}

		final String docType = t;

		timer.schedule(new ATimerTask() {

			public void run0() throws Exception {

				int importType = cbType.getSelectedIndex();

				// Create object and set properties
				File[] list = chooser.getSelectedFiles();
				String dirPath = ObjectTool.getPath(target[0]) + '/';
				for (int f = 0; f < list.length; f++) {

					try {
						window.getLogger().out.println(">>> IMPORT " + (f + 1) + " / "
								+ list.length + ": " + list[f].getPath());

						Properties prop = null;

						if (importRelations.isSelected()) {
							// find prop file
							File propFile = new File(list[f].getAbsolutePath()
									+ ".properties");
							if (!propFile.exists() || !propFile.isFile()) {
								window.getLogger().out
										.println("*** Property File not found");
								continue;
							}

							prop = new Properties();
							FileInputStream fis = new FileInputStream(propFile);
							prop.load(fis);
							fis.close();

							// fix lang
							if (prop.getProperty("s.language_code", "").equals(
									""))
								prop.setProperty("s.language_code", " ");

						}

						boolean create = importType == 0;

						if (!create && importNew.isSelected()) {
							String path = dirPath + list[f].getName();
							window.getLogger().out.println("--- CHECK " + path);
							if (prop != null)
								create = (con.getExistingObject(dirPath
										+ prop.getProperty("s.object_name"),
										prop.getProperty("s.language_code")) == null);
							else
								create = (con.getSession()
										.getObjectByPath(path) == null);
							if (!create && importType == 1) {
								window.getLogger().out.println("--- SKIP");
								continue;
							}
						}

						if (create) {
							window.getLogger().out.println("--- CREATE");

							if (prop == null) {
								IDfFormatRecognizer formatRec = DMConnection.clientx
										.getFormatRecognizer(con.getSession(),
												chooser.getSelectedFile()
														.toString(), "crtext");
								IDfList formatList = formatRec
										.getSuggestedFileFormats();
								String format = "crtext";
								String suffix = AFile.getFileSuffix(list[f])
										.toLowerCase();
								for (int i = 0; i < formatList.getCount(); i++) {
									format = formatList.getString(i);
									System.out.println("--- "
											+ formatList.getString(i));
									if (format.equals(suffix))
										break;
									// do something with the Error
								}

								IDfDocument document = (IDfDocument) con
										.getSession().newObject(docType);
								document.setObjectName(list[f].getName());
								document.setContentType(format);
								// setFileEx parameters
								// (fileName,formatName,pageNumber,otherFile)
								document.setFileEx(list[f].toString(), format,
										0, null);

								// Specify the folder in which to create the
								// document
								document.link(target[0].getObjectId()
										.toString());

								// Save the document in the Docbase
								document.save();

								ObjectChangedTool.objectsChanged(node,
										ApiObjectChanged.CREATED,
										new IDfPersistentObject[] { document });

							} else {

								IDfDocument document = (IDfDocument) con
										.getSession()
										.newObject(
												prop
														.getProperty("s.object_type"));
								document.setObjectName(prop
										.getProperty("s.object_name"));
								document.setContentType(prop
										.getProperty("s.content_type"));
								document.setString("language_code", prop
										.getProperty("s.language_code"));
								document.setTitle(prop.getProperty("s.title"));
								document.setSubject(prop
										.getProperty("s.subject"));

								document.link(target[0].getObjectId()
										.toString());

								document.save();

								// relations
								int cnt = 0;
								while (true) {
									String relName = prop.getProperty("rel."
											+ cnt + ".name");
									if (relName == null)
										break;
									try {
										String relPath = prop
												.getProperty("rel." + cnt
														+ ".path");
										String relLang = prop
												.getProperty("rel." + cnt
														+ ".language_code");
										if (relLang.length() == 0)
											relLang = " "; // fix lang
										String relDesc = prop
												.getProperty("rel." + cnt
														+ ".description");

										window.getLogger().out.println("--- Relation: "
												+ relName + " " + relPath);
										IDfSysObject relObj = con
												.getExistingObject(relPath,
														relLang);
										if (relObj == null)
											window.getLogger().out
													.println("*** Relation Object not found !!");
										else {
											String dql = "CREATE dm_relation OBJECT "
													+ "SET relation_name='"
													+ relName
													+ "' "
													+ "SET parent_id='"
													+ document.getObjectId()
															.getId()
													+ "' "
													+ "SET child_id='"
													+ relObj.getChronicleId()
															.getId()
													+ "' "
													+ "SET description='"
													+ ASql.escape(relDesc)
													+ "'";
											IDfQuery query = con
													.createQuery(dql);
											IDfCollection res = query.execute(
													con.getSession(),
													IDfQuery.READ_QUERY);
											res.close();
										}

									} catch (Exception e) {
										window.getLogger().out.println("*** ERROR: " + e);
									}
									cnt++;
								}

							}

							importType = 6;

						}

						if (importType >= 2 && importType <= 5) {
							window.getLogger().out.println("--- CO / CI");
							IDfSysObject sysObj = (IDfSysObject) con
									.getSession().getObjectByPath(
											dirPath + list[f].getName());

							if (sysObj == null) {
								window.getLogger().out.println("+++ not found");
								continue;
							}

							// try to cancel a check out
							if (sysObj.isCheckedOut()) {
								window.getLogger().out.println("+++ LOCKED: "
										+ list[f].getAbsolutePath());
								sysObj.cancelCheckout();
								sysObj.save();
								if (sysObj.isCheckedOut()) {
									window.getLogger().out.println("*** LOCKED ");
									continue;
								}
							}

							// check out
							IDfCheckoutOperation coOperation = DMConnection.clientx
									.getCheckoutOperation();
							IDfCheckoutNode coNode;
							if (sysObj.isVirtualDocument()) {
								IDfVirtualDocument vDoc = sysObj
										.asVirtualDocument("CURRENT", false);
								coNode = (IDfCheckoutNode) coOperation
										.add(vDoc);
								window.getLogger().out.println("--- is Virtual");
							} else {
								coNode = (IDfCheckoutNode) coOperation
										.add(sysObj);
							}
							executeOperation(coOperation);

							String coFilePath = coNode.getFilePath();
							window.getLogger().out.println("--- Copy: " + list[f].getPath()
									+ " -> " + coFilePath);
							// copy existinf gile over co file
							File coFile = new File(coFilePath);
							AFile.copyFile(list[f], coFile);

							// check in
							IDfCheckinOperation ciOperation = DMConnection.clientx
									.getCheckinOperation();
							IDfCheckinNode ciNode;
							if (sysObj.isVirtualDocument()) {
								IDfVirtualDocument vDoc = sysObj
										.asVirtualDocument("CURRENT", false);
								ciNode = (IDfCheckinNode) ciOperation.add(vDoc);
							} else {
								ciNode = (IDfCheckinNode) ciOperation
										.add(sysObj);
							}
							String versionLabels = "";
							for (int i = 1; i < sysObj
									.getValueCount("r_version_label"); i++) {
								if (i != 1)
									versionLabels += ",";
								versionLabels += sysObj.getRepeatingString(
										"r_version_label", i);
							}
							window.getLogger().out
									.println("--- Versions: " + versionLabels);
							ciOperation.setVersionLabels(versionLabels);
							// Other options for setCheckinVersion:
							// VERSION_NOT_SET, NEXT_MAJOR,
							// NEXT_MINOR, BRANCH_VERSION
							if (importType == 2)
								ciOperation
										.setCheckinVersion(IDfCheckinOperation.VERSION_NOT_SET);
							else if (importType == 3)
								ciOperation
										.setCheckinVersion(IDfCheckinOperation.NEXT_MAJOR);
							else if (importType == 4)
								ciOperation
										.setCheckinVersion(IDfCheckinOperation.NEXT_MINOR);
							else if (importType == 5)
								ciOperation
										.setCheckinVersion(IDfCheckinOperation.BRANCH_VERSION);

							// see sample: Operations- Execute and Check Errors
							executeOperation(ciOperation);

							IDfSysObject newObj = con.getExistingObject(ciNode
									.getNewObjectId());
							window.getLogger().out.println("--- New ID: "
									+ newObj.getObjectId().toString());
							ObjectChangedTool.objectsChanged(node,
									ApiObjectChanged.DELETED,
									new IDfPersistentObject[] { sysObj });
							ObjectChangedTool.objectsChanged(node,
									ApiObjectChanged.CREATED,
									new IDfPersistentObject[] { newObj });

						} else if (importType == 6) {
							window.getLogger().out.println("--- OVERWRITE");
							IDfSysObject sysObj = null;
							if (prop != null)
								sysObj = (IDfSysObject) con
										.getExistingObject(
												dirPath
														+ prop
																.getProperty("s.object_name"),
												prop
														.getProperty("s.language_code"));
							else
								sysObj = (IDfSysObject) con.getSession()
										.getObjectByPath(
												dirPath + list[f].getName());

							if (sysObj == null) {
								window.getLogger().out.println("+++ not found");
								continue;
							}

							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							FileInputStream fis = new FileInputStream(list[f]);
							AFile.copyFile(fis, baos);

							sysObj.setContent(baos);
							sysObj.save();

						}

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				window.getLogger().out.println(">>> IMPORT FINISHED");
			}

		}, 100);
	}

	public void initAction(PluginNode node, DMConnection con, Element config) {
		// TODO Auto-generated method stub

	}

	public void destroyAction() {
		// TODO Auto-generated method stub

	}

	public void executeOperation(IDfOperation operation)
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

			window.getLogger().out.println("Errors:");
			window.getLogger().out.println(message);
			// Create a DfException to report the errors
			// DfException e = new DfException();
			// e.setMessage(message);
			// throw e;
		}
	}

	public class Progress implements IDfOperationMonitor {
		public int progressReport(IDfOperation op, int iPercentOpDone,
				IDfOperationStep step, int iPercentStepDone,
				IDfOperationNode node) throws DfException {
			String strStep = iPercentOpDone + "%   " + iPercentStepDone + "% "
					+ step.getName() + " - " + step.getDescription();

			String strNode = "     "
					+ (node != null ? node.getId().toString() : "?");

			window.getLogger().out.println(strStep);
			window.getLogger().out.println(strNode);
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
