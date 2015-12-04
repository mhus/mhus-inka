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

import com.documentum.fc.client.IDfBusinessObject;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.common.DfException;
//import com.documentum.fc.util.reflection.proxy.BaseDoubleProxy;

import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.Plugin;
import de.mhu.hair.plugin.PluginConfig;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.plugin.ui.SimpleTextWindow;

public class ClassInformationsPlugin implements Plugin {

	private static SimpleTextWindow window = null;
	
	public void initPlugin(PluginNode pNode, PluginConfig pConfig)
			throws Exception {
		DMConnection con = (DMConnection) pNode
				.getSingleApi(DMConnection.class);
		IDfPersistentObject obj = con.getPersistentObject(pConfig
				.getProperty("objid"));
		
		if ( window == null )
			window = new SimpleTextWindow(pNode,"Class Information"); // a hack !!

		
		actionShow(obj);

		pNode.getParentNode().removeChild(pNode); // remove my node
		
	}

	private void actionShow(IDfPersistentObject obj) {

		try {
			window.getLogger().out.println(">>> Class of Object: "
					+ ObjectTool.getPath(obj));

			Class clazz = obj.getClass();
			while (clazz != null) {
				window.getLogger().out.println("--- Class: " + clazz);
				clazz = clazz.getSuperclass();
			}

			Class[] classes = obj.getClass().getInterfaces();
			for (int i = 0; i < classes.length; i++)
				window.getLogger().out.println("--- "
						+ (classes[i].isInterface() ? "IFC" : "CLS") + ": "
						+ classes[i].getName());

//			if (obj instanceof BaseDoubleProxy ) {
//				BaseDoubleProxy p = (BaseDoubleProxy)obj;
//				Object impl = p.____getImp____();
//				if ( impl != null ) {
//					clazz = obj.getClass();
//					while (clazz != null) {
//						window.getLogger().out.println("--- IMPL Class: " + clazz);
//						clazz = clazz.getSuperclass();
//					}
//					classes = obj.getClass().getInterfaces();
//					for (int i = 0; i < classes.length; i++)
//						window.getLogger().out.println("--- IMPL "
//								+ (classes[i].isInterface() ? "IFC" : "CLS") + ": "
//								+ classes[i].getName());
//				}
//			}
			
			if (obj instanceof IDfBusinessObject) {
				window.getLogger().out.println("--- BI: Version: "
						+ ((IDfBusinessObject) obj).getVersion());
				window.getLogger().out.println("--- BI: Vendor: "
						+ ((IDfBusinessObject) obj).getVendorString());
			}
			
		} catch (DfException e) {
			e.printStackTrace(window.getLogger().err);
		}
		window.getLogger().out.println("<<< END");
	}

	public void destroyPlugin() throws Exception {

	}

}
