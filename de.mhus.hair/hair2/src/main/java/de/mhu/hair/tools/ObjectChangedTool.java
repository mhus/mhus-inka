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

import com.documentum.fc.client.IDfPersistentObject;

import de.mhu.hair.api.ApiObjectChanged;
import de.mhu.hair.plugin.PluginNode;

public class ObjectChangedTool {

	public static boolean SEND_CHANGES = true;

	public static void objectsChanged(PluginNode node, int mode,
			IDfPersistentObject[] objects) {
		if (!SEND_CHANGES)
			return;
		ApiObjectChanged[] list = (ApiObjectChanged[]) node
				.getApi(ApiObjectChanged.class);
		if (list == null)
			return;

		for (int i = 0; i < list.length; i++) {
			try {
				list[i].objectsChanged(mode, objects);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}
