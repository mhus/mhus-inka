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

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.w3c.dom.Element;

import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfPersistentObject;

import de.mhu.hair.api.ApiObjectChanged;
import de.mhu.hair.api.ApiToolbar;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.plugin.dctm.ApiTypes;
import de.mhu.hair.plugin.ui.TypesTreePanel;
import de.mhu.hair.tools.ObjectChangedTool;

public class CreateCabinet implements ActionIfc {

	public void actionPerformed(PluginNode glue, DMConnection con,
			IDfPersistentObject[] target) throws Exception {

		TypesTreePanel type = new TypesTreePanel(con, (ApiTypes) glue
				.getSingleApi(ApiTypes.class), "dm_cabinet", null, null);
		JButton bOk = new JButton("OK");
		bOk.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				SwingUtilities.getWindowAncestor((JComponent) e.getSource())
						.dispose();

			}

		});
		JPanel typePanel = new JPanel();
		typePanel.setLayout(new BorderLayout());
		typePanel.add(type, BorderLayout.CENTER);
		typePanel.add(bOk, BorderLayout.SOUTH);

		JOptionPane.showOptionDialog(((ApiToolbar) glue
				.getSingleApi(ApiToolbar.class)).getMainComponent(),
				"Insert name and type", "Create Folder", 0,
				JOptionPane.QUESTION_MESSAGE, null, new Object[] { typePanel },
				null);

		String t = type.getSelected();
		if (t == null)
			return;

		String name = JOptionPane.showInputDialog(((ApiToolbar) glue
				.getSingleApi(ApiToolbar.class)).getMainComponent(),
				"Insert name for the new Folder:");
		if (name == null)
			return;

		// Create the cabinet- uses the IDfFolder interface
		IDfFolder myCabinet = (IDfFolder) con.getSession().newObject(t);
		myCabinet.setObjectName(name);
		// IDfValue value = myCabinet.getValue( "i_folder_id" );
		// myCabinet.setId( "i_folder_id", target.getObjectId() );
		// myCabinet.link( target.getString( "r_folder_path" ) );

		// commit object to Docbase
		myCabinet.save();
		System.out.println("Created cabinet " + myCabinet.getObjectName());
		// return myCabinet;

		// fire change
		ObjectChangedTool.objectsChanged(glue, ApiObjectChanged.CREATED,
				new IDfPersistentObject[] { myCabinet });

	}

	public boolean isEnabled(PluginNode node, DMConnection con,
			IDfPersistentObject[] target) throws Exception {
		return con != null;

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
