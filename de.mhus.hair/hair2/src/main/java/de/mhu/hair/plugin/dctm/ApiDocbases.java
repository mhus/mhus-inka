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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Properties;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfDocbaseMap;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfAttr;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfList;
import com.documentum.fc.common.IDfTime;
import com.documentum.fc.common.IDfValue;

import de.mhu.hair.api.Api;
import de.mhu.hair.plugin.Plugin;
import de.mhu.hair.plugin.PluginConfig;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.tools.ObjectTool;
import de.mhu.lib.plugin.JarsClassLoader;

public class ApiDocbases implements Plugin, Api {

	// private IDfClientX xclient = new DfClientX();
	
	public void initPlugin(PluginNode pNode, PluginConfig config) {

		pNode.addApi(ApiDocbases.class, this);

	}

	public IDfDocbaseMap getMap() throws Exception {
		return getLocalClient().getDocbaseMap();
	}

	public IDfClient getLocalClient() throws Exception {
		
		return new DfClientX().getLocalClient();
		
	}

	public String getDocbase() {
		
		IDfTypedObject config;
		try {
			config = getLocalClient().getClientConfig();
			return config.getString("primary_host") + ":"
					+ config.getInt("primary_port");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "?";
	}

	public void destroyPlugin() throws Exception {
		
	}
	
}
