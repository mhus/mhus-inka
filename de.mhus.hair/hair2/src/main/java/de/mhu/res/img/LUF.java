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

package de.mhu.res.img;

import java.awt.Font;

import javax.swing.Icon;

import de.mhu.lib.resources.ImageProvider;

public class LUF {

//	public static final Icon SEARCH_ICON = ImageProvider.getInstance().getIcon(
//			"hair:/search.png");
//	public static final Icon DOT_RED = ImageProvider.getInstance().getIcon(
//			"hair:/dot-red.gif");
//	public static final Icon DOT_GREEN = ImageProvider.getInstance().getIcon(
//			"hair:/dot-green.gif");
//	public static final Icon MENU_ICON = ImageProvider.getInstance().getIcon(
//	"mhu:action:menu");
//	public static final Icon RELOAD_ICON = ImageProvider.getInstance().getIcon(
//	"hair:/reload.gif");
//	public static final Icon NOT_EDITABLE_ICON = ImageProvider.getInstance().getIcon("hair:/file_locked.png");
//	public static final Icon EDITABLE_ICON = ImageProvider.getInstance().getIcon("hair:/page_white_edit.png");

	public static final Icon SEARCH_ICON = ImageProvider.getInstance().getIcon(
	"mhu:mono:search");
	public static final Icon DOT_RED = ImageProvider.getInstance().getIcon(
			"mhu:action:throbber:color red");
	public static final Icon DOT_GREEN = ImageProvider.getInstance().getIcon(
			"mhu:action:throbber:color green");
	public static final Icon HAIR_ICON = ImageProvider.getInstance().getIcon(
			"hair:/hair.gif");
	public static final Icon MENU_ICON = ImageProvider.getInstance().getIcon(
			"mhu:action:menu");
	public static final Icon VIEWS_ICON = ImageProvider.getInstance().getIcon(
	"mhu:mono:view");
	public static final Icon RELOAD_ICON = ImageProvider.getInstance().getIcon(
			"mhu:mono:refresh");
	public static final Icon NOT_EDITABLE_ICON = ImageProvider.getInstance().getIcon(
			"mhu:mono:security_closed");
	public static final Icon EDITABLE_ICON = ImageProvider.getInstance().getIcon(
			"mhu:mono:edit");
	public static final Icon FILTER_ICON = ImageProvider.getInstance().getIcon(
			"mhu:mono:filter");

	public static final String OBJECT_ICON_PATH = "hair:/dctm_wp_icons/";
	public static final Font CONSOLE_FONT = Font.decode("Courier-PLAIN-12");

}
