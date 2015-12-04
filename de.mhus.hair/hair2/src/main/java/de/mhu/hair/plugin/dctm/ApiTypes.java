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

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfType;
import com.documentum.fc.common.DfException;

import de.mhu.hair.api.Api;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.Plugin;
import de.mhu.hair.plugin.PluginConfig;
import de.mhu.hair.plugin.PluginNode;

public class ApiTypes implements Plugin, Api {

	private PluginNode node;
	private Hashtable<String,Object> types = new Hashtable<String,Object>();
	private Hashtable<String,Hashtable> tree = new Hashtable<String,Hashtable>();
	private Hashtable treeHelper = new Hashtable();
	private DMConnection con;
	private LinkedList<String> namesList;

	public void initPlugin(PluginNode pNode, PluginConfig pConfig)
			throws Exception {
		node = pNode;
		con = (DMConnection) node.getSingleApi(DMConnection.class);
		refreshTypes();
		node.addApi(ApiTypes.class, this);
	}

	public void refreshTypes() {

		types.clear();
		tree.clear();
		treeHelper.clear();

		try {
			IDfQuery qry = con
					.createQuery("select name,super_name from dm_type");
			IDfCollection res = qry.execute(con.getSession(),
					IDfQuery.READ_QUERY);

			while (res.next()) {

				String name = res.getString("name");
				String sName = res.getString("super_name");

				System.out.println("--- TYPE: [" + name + "] from [" + sName
						+ "]");

				types.put(name, name);

				Hashtable h = (Hashtable) treeHelper.get(sName);
				// if ( sName.equals( "" ) ) h = new Hashtable();
				if (h == null) {
					h = new Hashtable();
					treeHelper.put(sName, h);
				}
				Hashtable i = (Hashtable) h.get(name);

				if (i == null) {
					i = (Hashtable) treeHelper.get(name);
					if (i != null)
						h.put(name, i);
				}

				if (i == null) {
					i = new Hashtable();
					if (treeHelper.get(name) == null)
						treeHelper.put(name, i);
					h.put(name, i);
				}
				if (sName.equals(""))
					tree.put(name, i);
			}

			res.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public boolean isNamesListInitialized() {
		synchronized (this) {
			return namesList != null;
		}
	}
	
	public List<String> getNamesList() {
		synchronized (this) {
			if ( namesList == null ) {
				namesList = new LinkedList<String>();
				namesList.add("r_object_id");
				String[] t = getTypes();
				try {
					for ( String s : t ) {
						if ( !namesList.contains(s) ) namesList.add(s);
						IDfType t2 = getType(s);
						for (int i = 0; i < t2.getValueCount("attr_name"); i++) {
							String s2 = t2.getRepeatingString("attr_name", i);
							if ( !namesList.contains(s2) ) namesList.add(s2);
						}
					}
				} catch ( DfException ex ) {
					ex.printStackTrace();
				}
			}
		}
		return namesList;
	}

	public Hashtable<String,Hashtable> getTree() {
		return tree;
	}

	public Hashtable getTreeFor(String name) {
		return (Hashtable) treeHelper.get(name);
	}

	public synchronized IDfType getType(String name) {
		Object out = types.get(name);
		if (out instanceof String) {
			try {
				out = con.getSession().getType(name);
			} catch (DfException e) {
				e.printStackTrace();
				return null;
			}
			types.put(name, out);
		}
		return (IDfType) out;
	}
	
	public String[] getTypes() {
		return types.keySet().toArray(new String[ types.size() ] );
	}
	

	public boolean isTypeOf(String type, IDfType obj) throws DfException {
		if (obj.getName().equals(type))
			return true;

		String supr = "";
		while (!(supr = obj.getString("super_name")).equals("")) {
			if (supr.equals(type))
				return true;
			obj = getType(supr);
		}
		return false;
	}

	public boolean isFolder(String objectId) {
		return objectId.startsWith("0b") || objectId.startsWith("0c");
	}

	public void destroyPlugin() throws Exception {

	}

}
