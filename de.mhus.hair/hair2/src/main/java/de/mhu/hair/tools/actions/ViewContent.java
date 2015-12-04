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

import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.w3c.dom.Element;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSysObject;

import de.mhu.hair.api.ApiLayout;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.tools.ObjectTool;
import de.mhu.lib.ACast;
import de.mhu.lib.swing.ASwing;

public class ViewContent implements ActionIfc {

	private static Font font = Font.decode("Courier-PLAIN-14");
	private Element config;

	public boolean isEnabled(PluginNode node, DMConnection con,
			IDfPersistentObject[] target) throws Exception {
		if (target == null)
			return false;
		for (int i = 0; i < target.length; i++) {
			int size = target[i].getInt("r_content_size");
			if (size > 0 && size < 1024 * 200)
				return true;
		}
		return false;
	}

	public void actionPerformed(PluginNode node, DMConnection con,
			IDfPersistentObject[] target) throws Exception {

		ApiLayout layout = (ApiLayout) node.getSingleApi(ApiLayout.class);
		
		for (int i = 0; i < target.length; i++) {

			String dql = "select name, dos_extension from dm_format where name in (select full_format from dmr_content where any parent_id = '"
					+ target[i].getObjectId().getId() + "') order by name";
			IDfCollection res = con.createQuery(dql).execute(con.getSession(),
					IDfQuery.READ_QUERY);
			LinkedList<String> formats = new LinkedList<String>();
			while (res.next())
				formats.add(res.getString("name"));
			res.close();

			JTabbedPane tabs = new JTabbedPane();
			
			for (String format : formats) {
				ByteArrayInputStream bais = ((IDfSysObject) target[i])
						.getContentEx(format, 0);
				if (bais.available() > 0) {
					// Data successfully fetched from the server...
					// Optionally convert the Input Stream to a String
					String strTemp = DMConnection.clientx
							.ByteArrayInputStreamToString(bais);

					JTextArea area = new JTextArea(strTemp);
					area.setFont(font);
					// area.setEditable( false );
					JScrollPane scroll = new JScrollPane(area);
					tabs.addTab(format, scroll);
					/*
					 * final EditorFrame frame = new EditorFrame(
					 * ObjectTool.getPath( target[i] ), "editor.prp", new
					 * CloseListener() {
					 * 
					 * public void closed() { // frame.dispose(); }
					 * 
					 * }, true );
					 * 
					 * 
					 * EditorPanel V=frame.newPanel();
					 * 
					 * V.TD.syntax( "" ); try { BufferedReader in=new
					 * BufferedReader(new InputStreamReader(
					 * getClass().getResourceAsStream(filename.substring(6))));
					 * V.TD.load(in); in.close(); } catch (Exception e) { if
					 * (errormessage) { Warning w=new
					 * Warning(this,Global.name("string.file.loaderror"),
					 * Global.name("warning"),true); w.center(this);
					 * w.setVisible(true); } return; } add(filename,V);
					 * V.setVerticalScrollbar(); show(filename);
					 */

				}
			}

			if ( ACast.toboolean( config.getAttribute("openAsFrame"), false ) ) {
				final JFrame frame = new JFrame();
				frame.getContentPane().add(tabs);
				frame.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						frame.dispose();
					}
				});
	
				ASwing.tribleFrame(frame);
				ASwing.centerFrame(frame);
				frame.setTitle("View: " + target[i].getString("object_name"));
				frame.show();
			} else {
				try {
					layout.setComponent(tabs, config, "View: " + target[i].getString("object_name"), null );
				} catch (Exception e) {
					e.printStackTrace();
				}
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

}
