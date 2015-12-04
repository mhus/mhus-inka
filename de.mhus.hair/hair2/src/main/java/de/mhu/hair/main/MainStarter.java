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

import java.net.URL;
import java.util.Map;

import org.w3c.dom.Element;

import de.mhu.hair.plugin.PluginLoader;
import de.mhu.lib.ArgsParser;
import de.mhu.lib.resources.IResourceProvider;
import de.mhu.lib.resources.Resources;

public class MainStarter {

	public MainStarter(String[] args, URL[] jars) throws Exception {
		// parse arguments and start the required subclass
		ArgsParser.initialize(args);
		ArgsParser ap = ArgsParser.getInstance();

		PluginLoader.initialize(ap, jars);		
		
		// load start classes
		Map startMap = PluginLoader.getConfigElementsByName("start");
		
		String[] start = ap.getValues("st");
		if (start.length == 0) {
			// start = new String[] { "gui" };
			GuiMenu.show(startMap, args);
			return;
		}
		System.out.println("StartMap: " + startMap );
		String clazz = ((Element) startMap.get(start[0])).getAttribute("class");
		((MainIfc) Class.forName(clazz).newInstance()).startMain();

		/*
		 * if ( start[0].equals( "gui" ) ) DctmGui.main( args ); else if (
		 * start[0].equals( "cons" ) ) DctmConsole.main( args ); else if (
		 * start[0].equals( "wdk_browser" ) ) WdkBrowser.main( args ); else
		 * System.out.println(
		 * "*** ERROR: Target not found Usage: -st [gui|cons|wdk_browser]" );
		 */
	}

}
