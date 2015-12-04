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

package de.mhu.hair.trial;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.w3c.dom.Element;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfId;

import de.mhu.hair.api.ApiLayout;
import de.mhu.hair.api.ApiObjectWorker;
import de.mhu.hair.api.ApiLayout.Listener;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.tools.actions.ActionIfc;

public class TestPlugin extends JPanel implements ActionIfc, ApiObjectWorker,
		Listener {

	private PluginNode node;
	private Element config;
	private JTextField tPageTemplate;
	private JButton bAction;
	private boolean visiblePlugin = false;
	private JLabel lPageTemplate;
	private JLabel lPageTemplateName;
	private DMConnection con;
	private JLabel lPageTemplatePath;
	private JTextField tDestFolder;
	private JLabel lDestFolder;
	private JLabel lDestFolderName;
	private JLabel lDestFolderPath;

	public void initAction(PluginNode node, DMConnection con, Element pConfig) {

		config = pConfig;

		initUI();

		node.addApi(ApiObjectWorker.class, this);
	}

	protected void setPluginVisible(boolean b) throws Exception {
		if (visiblePlugin == b)
			return;
		visiblePlugin = b;
		if (visiblePlugin)
			((ApiLayout) node.getSingleApi(ApiLayout.class)).setComponent(this,
					config, this);
		else
			((ApiLayout) node.getSingleApi(ApiLayout.class))
					.removeComponent(this);

	}

	private void initUI() {

		setLayout(new BorderLayout());

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		panel.add(new JLabel("Vorlage f�r 'Page':"));
		tPageTemplate = new JTextField();
		tPageTemplate.addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					refreshPageTemplate();
			}

		});
		panel.add(tPageTemplate);
		lPageTemplate = new JLabel();
		panel.add(lPageTemplate);
		lPageTemplatePath = new JLabel();
		panel.add(lPageTemplatePath);
		lPageTemplateName = new JLabel();
		panel.add(lPageTemplateName);

		panel.add(new JLabel(" "));

		panel.add(new JLabel("Destination Folder:"));
		tDestFolder = new JTextField();
		tDestFolder.addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					refreshDestFolder();
			}

		});
		panel.add(tDestFolder);
		lDestFolder = new JLabel();
		panel.add(lDestFolder);
		lDestFolderPath = new JLabel();
		panel.add(lDestFolderPath);
		lDestFolderName = new JLabel();
		panel.add(lDestFolderName);

		panel.add(new JLabel(" "));

		bAction = new JButton(" Action ");
		panel.add(bAction);

		add(panel, BorderLayout.NORTH);

	}

	private void refreshPageTemplate() {
		try {

			IDfSysObject obj = con.getExistingObject(tPageTemplate.getText());
			if (!isValidPageTemplate(obj))
				throw new Exception("Not Valide");

			lPageTemplate.setText(obj.getObjectId().toString());
			lPageTemplatePath.setText(con.getExistingObject(
					obj.getId("i_folder_id")).getString("r_folder_path"));
			lPageTemplateName.setText(obj.getObjectName());

		} catch (Exception e) {
			e.printStackTrace();
			lPageTemplate.setText("");
			lPageTemplatePath.setText("");
			lPageTemplateName.setText("");
		}

	}

	private void refreshDestFolder() {
		try {

			IDfSysObject obj = con.getExistingObject(tDestFolder.getText());
			if (!isValidDestFolder(obj))
				throw new Exception("Not Valide");

			lDestFolder.setText(obj.getObjectId().toString());
			lDestFolderPath.setText(obj.getId("r_folder_path").toString());
			lDestFolderName.setText(obj.getObjectName());

		} catch (Exception e) {
			e.printStackTrace();
			lDestFolder.setText("");
			lDestFolderPath.setText("");
			lDestFolderName.setText("");
		}
	}

	private boolean isValidDestFolder(IDfPersistentObject obj) {

		if (obj instanceof IDfFolder)
			return true;
		return false;

	}

	private boolean isValidPageTemplate(IDfPersistentObject obj) {

		if (obj instanceof IDfFolder)
			return false;

		if (getLayouts(obj).length == 0 || getRules(obj).length == 0)
			return false;

		return true;
	}

	private IDfId[] getLayouts(IDfPersistentObject obj) {
		try {
			IDfQuery query = con
					.createQuery("SELECT r_object_id FROM dm_relation WHERE parent_id='"
							+ obj.getObjectId().toString()
							+ "' and relation_name='wcm_layout_template'");
			IDfCollection res;
			res = query.execute(con.getSession(), IDfQuery.READ_QUERY);
			Vector out = new Vector();
			while (res.next()) {
				out.add(res.getId("r_object_id"));
			}
			res.close();
			return (IDfId[]) out.toArray(new IDfId[out.size()]);
		} catch (DfException e) {
			e.printStackTrace();
		}
		return new IDfId[] {};
	}

	private IDfId[] getRules(IDfPersistentObject obj) {
		try {
			IDfQuery query = con
					.createQuery("SELECT r_object_id FROM dm_relation WHERE parent_id='"
							+ obj.getObjectId().toString()
							+ "' and relation_name='wcm_rules_template'");
			IDfCollection res;
			res = query.execute(con.getSession(), IDfQuery.READ_QUERY);
			Vector out = new Vector();
			while (res.next()) {
				out.add(res.getId("r_object_id"));
			}
			res.close();
			return (IDfId[]) out.toArray(new IDfId[out.size()]);
		} catch (DfException e) {
			e.printStackTrace();
		}
		return new IDfId[] {};
	}

	public void windowClosed(Object source) {
		try {
			setPluginVisible(false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getTitleFor(DMConnection pCon, IDfPersistentObject[] pObj) {
		if (pObj == null || pObj.length != 1)
			return null;
		if (pObj[0] instanceof IDfFolder)
			return "Migrate PCD: Dest Folder";
		else
			return "Migrate PCD: Page Template";
	}

	public boolean canWorkOn(DMConnection pCon, IDfPersistentObject[] pObj) {
		if (!visiblePlugin)
			return false;
		if (pObj == null || pObj.length != 1)
			return false;
		if (pObj[0] instanceof IDfFolder)
			return isValidDestFolder(pObj[0]);
		else
			return isValidPageTemplate(pObj[0]);
	}

	public void workWith(DMConnection pCon, IDfPersistentObject[] pObj) {
		if (pObj == null || pObj.length != 1)
			return;

		if (pObj[0] instanceof IDfFolder) {
			try {
				tDestFolder.setText(pObj[0].getObjectId().toString());
			} catch (DfException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			refreshDestFolder();
		} else {
			try {
				tPageTemplate.setText(pObj[0].getObjectId().toString());
			} catch (DfException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			refreshPageTemplate();
		}
	}

	public void destroyAction() {

	}

	public boolean isEnabled(PluginNode node, DMConnection con,
			IDfPersistentObject[] target) throws Exception {

		return con != null;
	}

	public void actionPerformed(PluginNode pNode, DMConnection pCon,
			IDfPersistentObject[] target) throws Exception {
		node = pNode;
		con = pCon;
		setPluginVisible(true);
	}

	public String getTitle() {
		return null;
	}

}
