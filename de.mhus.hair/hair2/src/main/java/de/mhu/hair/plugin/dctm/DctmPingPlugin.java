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

package de.mhu.hair.plugin.dctm;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;

import de.mhu.hair.api.ApiToolbar;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.Plugin;
import de.mhu.hair.plugin.PluginConfig;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.res.img.LUF;

public class DctmPingPlugin extends JButton implements Plugin {

	private int interval;
	private String dql;
	private DMConnection con;
	private Timer timer;

	public void initPlugin(PluginNode pNode, PluginConfig pConfig)
			throws Exception {

		interval = Integer.parseInt(pConfig.getNode().getAttribute("interval"));
		dql = pConfig.getNode().getAttribute("dql");
		con = (DMConnection) pNode.getSingleApi(DMConnection.class);

		initUI();

		((ApiToolbar) pNode.getSingleApi(ApiToolbar.class))
				.addToolbarButton(this);

		timer = new Timer(true);
		timer.schedule(new TimerTask() {

			public void run() {
				actionPing();
			}

		}, 100, interval * 1000);

	}

	private void initUI() {
		setText("Ping");
		setMargin(new Insets(1, 1, 1, 1));
		
		addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				actionPing();
			}

		});
	}

	protected void actionPing() {

		setToolTipText(new Date().toString());
		
		try {

			IDfQuery query = con.createQuery(dql);
			IDfCollection res = query.execute(con.getSession(),
					IDfQuery.READ_QUERY);
			setToolTipText("Result: " + res.next());
			res.close();
			setIcon(LUF.DOT_GREEN);
		} catch (Exception e) {
			setToolTipText(e.toString());
			setIcon(LUF.DOT_RED);
		}

	}

	public void destroyPlugin() throws Exception {
		// TODO Auto-generated method stub

	}

}
