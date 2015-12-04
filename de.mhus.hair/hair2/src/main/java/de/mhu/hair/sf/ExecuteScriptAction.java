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

package de.mhu.hair.sf;

import java.io.FileInputStream;
import java.util.Timer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSysObject;

import de.mhu.hair.api.ApiLayout;
import de.mhu.hair.api.ApiSystem;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.tools.actions.ActionIfc;
import de.mhu.hair.tools.actions.ActionUIIfc;

public class ExecuteScriptAction implements ActionUIIfc {

	private PluginNode node;
	private DMConnection con;
	private DocumentBuilder builder;
	private Element scriptConfig;
	private Element config;
	private boolean foldersOnly;
	private boolean singleSelect;
	private boolean ignoreTargets;
	private boolean documentOnly;
	private Timer timer;
	private boolean targetUntrusted;

	public void initAction(PluginNode pNode, DMConnection pCon, Element pConfig) {
		node = pNode;
		con = pCon;
		config = pConfig;
		timer = ((ApiSystem) node.getSingleApi(ApiSystem.class)).getTimer();

		String scriptPath = pConfig.getAttribute("script");

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			builder = dbf.newDocumentBuilder();

			// Load config
			InputSource is = null;
			if (scriptPath.startsWith("res:")) {

				is = new InputSource(this.getClass().getResource(
						scriptPath.substring(4)).openStream());
			} else {
				is = new InputSource(new FileInputStream(scriptPath));
			}

			Document dom = builder.parse(is);
			scriptConfig = dom.getDocumentElement();

			// get behavior parameters
			String listen = scriptConfig.getAttribute("listen");

			foldersOnly = (listen.indexOf("_folders.only_") >= 0);
			documentOnly = (listen.indexOf("_documents.only_") >= 0);
			singleSelect = (listen.indexOf("_single_") >= 0);
			ignoreTargets = (listen.indexOf("_ignore_") >= 0);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void destroyAction() {

	}

	public boolean isEnabled(PluginNode pNode, DMConnection pCon,
			IDfPersistentObject[] pTarget) throws Exception {
		if (ignoreTargets)
			return true;
		if (pTarget == null || pTarget.length == 0)
			return false;
		if (singleSelect && (pTarget.length != 1))
			return false;
		if (foldersOnly) {
			for (int i = 0; i < pTarget.length; i++)
				if (!(pTarget[i] instanceof IDfFolder))
					return false;
		}
		if (documentOnly) {
			for (int i = 0; i < pTarget.length; i++)
				if (!(pTarget[i] instanceof IDfSysObject)
						|| (pTarget[i] instanceof IDfFolder))
					return false;
		}
		return true;
	}

	public void actionPerformed(PluginNode pNode, DMConnection pCon,
			IDfPersistentObject[] pTarget) throws Exception {

		ApiLayout layout = (ApiLayout) node.getSingleApi(ApiLayout.class);
		ExecuteScriptUI script = new ExecuteScriptUI(node, scriptConfig, pCon,
				pTarget,isEnabled(pNode, pCon, pTarget),targetUntrusted);
		layout.setComponent(script.getConfigPanel(), config);

	}

	public String getTitle() {
		return config.getAttribute("title");
	}

	public void setActionTrustTargets(boolean trust) {
		targetUntrusted = !trust;
	}

	public boolean hasOwnTargetDialog() {
		return true;
	}

}
