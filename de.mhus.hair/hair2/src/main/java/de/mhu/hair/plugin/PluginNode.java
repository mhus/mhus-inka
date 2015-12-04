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

package de.mhu.hair.plugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import org.w3c.dom.Element;

import de.mhu.hair.api.Api;
import de.mhu.hair.api.ApiPersistent;
import de.mhu.hair.api.ApiPluginNode;
import de.mhu.hair.tools.FilePersistentManager;

public class PluginNode implements ApiPluginNode {

	private Hashtable apis = new Hashtable();
	private PluginNode parent = null;
	private Vector childs = new Vector();
	private PluginLoader loader;
	private String configName;
	private File rootDir;

	public PluginNode(String config) throws IOException {
		loadConfig(config);
	}

	private PluginNode(PluginNode pParent) {
		parent = pParent;
		rootDir = parent.rootDir;
	}

	public boolean start(Map params, boolean allowErrors) {
		if (loader == null)
			return false;
		if (params != null)
			params.put("_rootDir", rootDir);
		boolean ret = loader.start(this, params, allowErrors);
		loader = null;
		return ret;
	}

	public PluginNode createChild(String config) throws IOException {
		PluginNode child = new PluginNode(this);
		child.loadConfig(config);
		childs.add(child);
		return child;
	}

	public PluginNode createChild(Element config) throws MalformedURLException {
		PluginNode child = new PluginNode(this);
		child.loadConfig(config, rootDir);
		childs.add(child);
		return child;
	}

	private void loadConfig(String config) throws IOException {
		if (loader != null)
			return;
		addApi(ApiPluginNode.class, this); // add my api first

		if (rootDir == null)
			rootDir = new File(config);
		FilePersistentManager pm = new FilePersistentManager(new File(config),
				this);
		addApi(ApiPersistent.class, pm);

		configName = config;
		loader = new PluginLoader(config);
	}

	private void loadConfig(Element config, File rootDir)
			throws MalformedURLException {
		if (loader != null)
			return;
		addApi(ApiPluginNode.class, this); // add my api first
		addApi(ApiPersistent.class, (Api) getSingleApi(ApiPersistent.class));

		loader = new PluginLoader(config, rootDir);

	}

	public void addApi(Class clazz, Api api) {
		Vector v = (Vector) apis.get(clazz);
		if (v == null) {
			v = new Vector();
			apis.put(clazz, v);
		}
		if (!v.contains(api))
			v.add(api);
	}

	public void removeApi(Class clazz, Api api) {
		Vector v = (Vector) apis.get(clazz);
		if (v == null)
			return;
		v.remove(api);
	}

	public Object[] getApi(Class clazz) {
		Vector v = (Vector) apis.get(clazz);
		Object[] obj = null;
		if (v != null)
			obj = v.toArray((Object[]) Array.newInstance(clazz, 0));

		if (obj == null || obj.length == 0) {
			if (parent == null)
				return null;
			else
				return parent.getApi(clazz);
		}
		return obj;
	}

	public PluginNode getNode() {
		return this;
	}

	public PluginNode getParentNode() {
		return parent;
	}

	public PluginNode[] getChildNodes() {
		return (PluginNode[]) childs.toArray(new PluginNode[childs.size()]);
	}

	public void removeApi(Api api) {
		Object[] keys = apis.keySet().toArray();
		for (int i = 0; i < keys.length; i++)
			removeApi((Class) keys[i], api);
	}

	public Object getSingleApi(Class clazz) {
		Object[] ret = getApi(clazz);
		if (ret == null || ret.length == 0)
			return null;
		return ret[0];
	}

	public void removeChild(PluginNode node) {
		childs.remove(node);
	}

	public String getConfigName() {
		return configName;
	}

}
