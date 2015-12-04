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

import javax.swing.Icon;

import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfGroup;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.common.DfException;

import de.mhu.lib.resources.ImageProvider;
import de.mhu.res.img.LUF;

public class ObjectTool {

	public static Icon getIcon(String objectType, String contentType, boolean isFolder ) {
		Icon icon = null;

		if (contentType != null && !contentType.equals("")) {
			icon = ImageProvider.getInstance().getIcon(
					LUF.OBJECT_ICON_PATH + "format/f_" + contentType
							+ "_16.gif");
		}
		if (icon == null) {
			icon = ImageProvider.getInstance().getIcon(
					LUF.OBJECT_ICON_PATH + "type/t_" + objectType + "_16.gif");
		}
		
		if (icon == null) {
			if ( isFolder )
				icon = ImageProvider.getInstance().getIcon(
						LUF.OBJECT_ICON_PATH + "type/t_dm_folder_16.gif");
			else
				icon = ImageProvider.getInstance().getIcon(
						LUF.OBJECT_ICON_PATH + "format/f__16.gif");
		}
		return icon;
	}

	public static String getName(IDfPersistentObject obj) {

		try {

			if (obj instanceof IDfSysObject)
				return ((IDfSysObject) obj).getObjectName();
			return obj.getObjectId().toString();

		} catch (DfException e) {
			e.printStackTrace();
			return e.toString();
		}
	}

	public static void dump(String prefix, IDfTypedObject obj)
			throws DfException {
		for (int i = 0; i < obj.getAttrCount(); i++) {
			String name = obj.getAttr(i).getName();
			if (obj.isAttrRepeating(name)) {
				System.out.println(prefix + " Repeating " + name);
				for (int j = 0; j < obj.getValueCount(name); j++)
					System.out.println(prefix + ": " + name + "[" + j + "]="
							+ obj.getRepeatingString(name, j));
			} else
				System.out.println(prefix + ": " + name + "="
						+ obj.getString(name));
		}
	}

	public static String getPath(IDfPersistentObject obj) throws DfException {
		if (obj instanceof IDfSysObject) {

			IDfSysObject sys = (IDfSysObject) obj;

			if (sys.getFolderIdCount() == 0)
				return "/" + sys.getObjectName();
			IDfFolder folder = (IDfFolder) sys.getSession().getObject(
					sys.getFolderId(0));

			if (folder.getFolderPathCount() == 0)
				return "/" + folder.getObjectName() + "/" + sys.getObjectName();

			return folder.getFolderPath(0) + "/" + sys.getObjectName();
		} else
		if (obj instanceof IDfACL) {
			return ((IDfACL)obj).getObjectName();
		} else
		if (obj instanceof IDfGroup) {
			return ((IDfGroup)obj).getGroupDisplayName();
		} else
			return obj.getObjectId().toString();
	}

	public static boolean isTypeOf(String type, IDfPersistentObject obj) throws DfException {
		
		return obj != null && ( obj.getType().getName().equals(type) || obj.getType().isSubTypeOf(type) );
	}

}
