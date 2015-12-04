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

package de.mhu.hair.tools;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import com.documentum.fc.client.IDfPersistentObject;

import de.mhu.hair.api.ApiLayout;
import de.mhu.hair.api.ApiObjectHotSelect;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.Plugin;
import de.mhu.hair.plugin.PluginConfig;
import de.mhu.hair.plugin.PluginNode;

public class DumpPlugin extends JPanel implements Plugin, ApiObjectHotSelect {

	private PluginNode node;
	private PluginConfig config;
	private JTextArea dump;
	private DMConnection con;
	private static Font font = Font.decode("Courier-PLAIN-14");

	public void initPlugin(PluginNode pNode, PluginConfig pConfig)
			throws Exception {

		node = pNode;
		con = (DMConnection) pNode.getSingleApi(DMConnection.class);
		config = pConfig;

		initUI();

		((ApiLayout) pNode.getSingleApi(ApiLayout.class)).setComponent(this,
				pConfig.getNode());

		if (pConfig.getNode().getAttribute("listen").indexOf("_obj.hotselect_") >= 0) {
			pNode.addApi(ApiObjectHotSelect.class, this);
		}
		if (pConfig.getNode().getAttribute("listen").indexOf("_obj.last_") >= 0) {
			IDfPersistentObject obj = con.getPersistentObject(pConfig
					.getProperty("objid"));
			actionShow(obj);
		}

	}

	private void initUI() {

		setLayout(new BorderLayout());
		dump = new JTextArea();
		dump.setEditable(false);
		dump.setFont(font);
		dump.addMouseListener(new ObjectWorkerTool.TextIdGrapper(node, con));

		JScrollPane scroller = new JScrollPane(dump);
		add(scroller, BorderLayout.CENTER);

	}

	public void destroyPlugin() throws Exception {
		// TODO Auto-generated method stub
	}

	public void apiObjectDepricated() {
		// TODO Auto-generated method stub

	}

	public void apiObjectHotSelected(DMConnection pCon,
			IDfPersistentObject[] pParents, IDfPersistentObject[] pObj)
			throws Exception {
		if (pObj == null || pObj.length == 0)
			dump.setText("");
		else
			actionShow(pObj[0]);
	}

	private void actionShow(IDfPersistentObject obj) {
		try {
			dump.getDocument().remove(0, dump.getDocument().getLength());
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			dump.getDocument().insertString(0, obj.dump(), null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
