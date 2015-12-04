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

package de.mhu.hair.main;

import java.util.Hashtable;
import java.util.Timer;

import de.mhu.hair.api.ApiSystem;
import de.mhu.hair.plugin.PluginNode;

public class WdkBrowser {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			PluginNode root = new PluginNode("plugins/hair.wdk_browser");
			// PluginNode root = new PluginNode( "tree" );
			root.addApi(ApiSystem.class, new ApiSystem() {

				private Timer timer = new Timer();

				public void exit(int ret) {
					System.exit(ret);
				}

				public Timer getTimer() {
					return timer;
				}

			});
			root.start(new Hashtable(), true);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
