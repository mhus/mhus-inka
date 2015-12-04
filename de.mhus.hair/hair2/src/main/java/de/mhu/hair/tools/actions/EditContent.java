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

import javax.swing.JFrame;

import org.w3c.dom.Element;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;

import de.mhu.hair.api.ApiLayout;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.tools.ObjectTool;
import de.mhu.lib.ACast;
import de.mhu.lib.swing.ASwing;
import de.mhu.lib.swing.edi.EdiFullEditorArea;
import de.mhu.lib.swing.edi.EdiFullEditorArea.Listener;

public class EditContent implements ActionIfc {

	private Element config;

	public boolean isEnabled(PluginNode node, DMConnection con,
			IDfPersistentObject[] target) throws Exception {
		if (target == null)
			return false;
		for (int i = 0; i < target.length; i++) {
			int size = target[i].getInt("r_content_size");
			if (size > 0 && size < 1024 * 1024)
				return true;
		}
		return false;
	}

	public void actionPerformed(PluginNode node, DMConnection con,
			IDfPersistentObject[] target) throws Exception {

		ApiLayout layout = (ApiLayout) node.getSingleApi(ApiLayout.class);
		
		for (int i = 0; i < target.length; i++) {
			int size = target[i].getInt("r_content_size");
			if (size > 0 && size < 1024 * 1024) {
				// Connection.open( "DCTM_ID:" +
				// target[i].getSession().getDMCLSessionId() + "/" +
				// target[i].getObjectId() + "/" + ObjectTool.getName( target[i]
				// ) );

				new Editor(layout, target[i].getSession(), target[i]);

			}
		}
	}

	public void initAction(PluginNode node, DMConnection con, Element config) {
		this.config = config;
	}

	public void destroyAction() {
		// TODO Auto-generated method stub

	}

	public String getTitle() {
		return null;
	}

	private class Editor implements Listener, de.mhu.hair.api.ApiLayout.Listener {

		private IDfSession session;
		private IDfSysObject obj;
		private EdiFullEditorArea editor;

		public Editor(ApiLayout layout, IDfSession session, IDfPersistentObject dfPersistentObject)
				throws DfException {
			this.session = session;
			obj = (IDfSysObject) dfPersistentObject;

			editor = new EdiFullEditorArea(this);

			ByteArrayInputStream bais = obj.getContent();
			String strTemp = DMConnection.clientx
					.ByteArrayInputStreamToString(bais);
			editor.getText().setText(strTemp);

			if ( ACast.toboolean( config.getAttribute("openAsFrame"), false ) ) {
				JFrame frame = new JFrame();
				frame.add(editor);
				ASwing.tribleFrame(frame);
				ASwing.centerFrame(frame);
				frame.setTitle(ObjectTool.getName(obj));
				frame.show();
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			} else {
				try {
					layout.setComponent(editor, config, "Edit: " + ObjectTool.getName(obj), null );
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public void doSave() {

			ByteArrayOutputStream baios = DMConnection.clientx
					.StringToByteArrayOutputStream(editor.getText().getText());
			try {
				obj.setContent(baios);
				obj.save();
			} catch (DfException e) {
				e.printStackTrace();
			}
		}

		public void windowClosed(Object source) {
			
		}

	}
}
