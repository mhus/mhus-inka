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
import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.TimerTask;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import org.w3c.dom.Element;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfFormat;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSysObject;

import de.mhu.hair.api.ApiLayout;
import de.mhu.hair.api.ApiSystem;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.plugin.ui.SimpleTextWindow;
import de.mhu.hair.tools.ObjectTool;
import de.mhu.lib.ATimerTask;
import de.mhu.lib.swing.FileExtFilter;

public class ExportFile implements ActionIfc {

	private JFileChooser chooser = new JFileChooser();
	private boolean isAddExtension = true;
	private boolean isExportRelations = false;
	private JCheckBox sameDirectory;
	private JCheckBox exportRelations;
	private JCheckBox addExtension;
	private SimpleTextWindow window;
	private PluginNode node;
	private DMConnection con;
	private IDfPersistentObject[] target;

	public ExportFile() {
		chooser.addChoosableFileFilter(new FileExtFilter("Images",
				new String[] { "gif", "png", "jpg" }));
		chooser.addChoosableFileFilter(new FileExtFilter("XML Content",
				new String[] { "xml" }));
		chooser.setMultiSelectionEnabled(false);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		sameDirectory = new JCheckBox("Directory for All");
		panel.add(sameDirectory);

		exportRelations = new JCheckBox("Export Relations Infos");
		exportRelations.setSelected(isExportRelations);
		exportRelations.setEnabled(true);
		panel.add(exportRelations);

		addExtension = new JCheckBox("Add Extension");
		addExtension.setSelected(isAddExtension);
		panel.add(addExtension);

		chooser.setAccessory(panel);

	}

	public boolean isEnabled(PluginNode node, DMConnection con,
			IDfPersistentObject[] target) throws Exception {

		if (target == null)
			return false;

		for (int i = 0; i < target.length; i++) {
			int size = target[i].getInt("r_content_size");
			if (size > 0)
				return true;
		}

		return false;
	}

	public void actionPerformed(PluginNode node, DMConnection con,
			IDfPersistentObject[] target) throws Exception {
		this.node = node;
		this.con = con;
		this.target = target;
		((ApiSystem)node.getSingleApi(ApiSystem.class)).getTimer().schedule(new ATimerTask() {

			@Override
			protected void onError(Throwable t) {
				t.printStackTrace();
			}

			@Override
			public void run0() throws Exception {
					action();
			}
			
		}, 100);
	}

	protected void action() throws Exception {
		if (window == null)
			window = new SimpleTextWindow(node, "Export");
		
		JComponent component = ((ApiLayout) node.getSingleApi(ApiLayout.class))
				.getMainComponent();

		File dir = null;

		for (int i = 0; i < target.length; i++) {
			int size = target[i].getInt("r_content_size");
			if (size > 0) {
				IDfSysObject document = (IDfSysObject) target[i];
				IDfFormat myFormat = con.getSession().getFormat(
						document.getContentType());
				String name = document.getObjectName();
				if (isAddExtension && myFormat != null) {
					String ext = myFormat.getDOSExtension();
					if (!name.endsWith('.' + ext))
						name = name + "." + ext;
				}
				File file = null;
				if (dir == null) {
					chooser.setSelectedFile(new File(chooser
							.getCurrentDirectory(), name));

					if (chooser.showSaveDialog(component) != JFileChooser.APPROVE_OPTION)
						return;

					isAddExtension = addExtension.isSelected();
					isExportRelations = exportRelations.isSelected();

					file = chooser.getSelectedFile();
					if (sameDirectory.isSelected())
						dir = file.getParentFile();
				} else {
					file = new File(dir, name);
				}

				if (isExportRelations) {
					// insert language code
					file = new File(file.getParentFile(), document
							.getString("language_code")
							+ '_' + file.getName());
				}

				window.getLogger().out.println(">>> Export " + file.getPath());

				if (isExportRelations) {
					ByteArrayInputStream bais = document.getContent();
					byte[] buffer = new byte[bais.available()];
					bais.read(buffer);
					FileOutputStream fos = new FileOutputStream(file);
					fos.write(buffer);
					fos.close();
				} else {
					document.getFileEx2(file.getPath(), document
							.getContentType(), 0, "", false);
				}

				if (isExportRelations) {
					// insert language code
					file = new File(file.getParentFile(), file.getName()
							+ ".properties");
					Properties prop = new Properties();
					prop.setProperty("s.object_name", document.getObjectName());
					prop.setProperty("s.language_code", document
							.getString("language_code"));
					prop.setProperty("s.path", ObjectTool.getPath(document));
					prop.setProperty("s.subject", document.getSubject());
					prop.setProperty("s.title", document.getTitle());
					prop.setProperty("s.object_type", document.getTypeName());
					prop.setProperty("s.content_type", document
							.getContentType());

					String dql = "SELECT R.relation_name,R.child_id,S.i_chronicle_id,S.r_object_id,S.object_name,R.description "
							+ "FROM dm_relation R, dm_sysobject S "
							+ "WHERE R.parent_id='"
							+ document.getObjectId()
							+ "' AND ( S.i_chronicle_id=R.child_id OR S.r_object_id=R.child_id)";

					IDfQuery query = con.createQuery(dql);
					IDfCollection res = query.execute(con.getSession(),
							IDfQuery.EXEC_QUERY);
					int cnt = 0;
					while (res.next()) {
						String relName = res.getString("relation_name");
						String childId = res.getString("child_id");
						String chronicleId = res.getString("i_chronicle_id");
						String objectId = res.getString("r_object_id");
						String objectName = res.getString("object_name");
						String description = res.getString("description");

						if (relName.startsWith("wcm_")) {
							IDfSysObject child = con
									.getExistingObject(objectId);
							if (child != null) {
								String path = ObjectTool.getPath(child);
								System.out.println("--- Relation: " + relName
										+ " " + path);
								prop.setProperty("rel." + cnt + ".name",
										relName);
								prop.setProperty("rel." + cnt + ".path", path);
								prop.setProperty("rel." + cnt
										+ ".language_code", child
										.getString("language_code"));
								prop.setProperty("rel." + cnt + ".description",
										description);
								prop.setProperty("rel." + cnt + ".child_id",
										childId);
								prop.setProperty("rel." + cnt
										+ ".i_chronicle_id", chronicleId);
								prop.setProperty("rel." + cnt + ".r_object_id",
										objectId);

								cnt++;
							} else {
								window.getLogger().out
										.println("+++ Cant find relation: "
												+ relName + " " + objectId); // not
								// possible
								// !?
							}
						}

					}
					res.close();

					FileOutputStream fos = new FileOutputStream(file);
					prop.store(fos, "Object: " + document.getObjectId());
					fos.close();

				}

			}
		}
		window.getLogger().out.println("FINISHED Export");
	}

	public void initAction(PluginNode node, DMConnection con, Element config) {
		// TODO Auto-generated method stub

	}

	public void destroyAction() {
		// TODO Auto-generated method stub

	}

	public String getTitle() {
		return null;
	}

}
