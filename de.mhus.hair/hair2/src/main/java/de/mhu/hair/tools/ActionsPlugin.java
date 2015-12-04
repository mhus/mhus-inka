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

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.common.DfException;

import de.mhu.hair.api.ApiLayout;
import de.mhu.hair.api.ApiMenuBar;
import de.mhu.hair.api.ApiObjectHotSelect;
import de.mhu.hair.api.ApiObjectWorker;
import de.mhu.hair.api.ApiObjectWorkerGui;
import de.mhu.hair.api.ApiToolbar;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.Plugin;
import de.mhu.hair.plugin.PluginConfig;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.tools.actions.ActionIfc;
import de.mhu.hair.tools.actions.ActionUIIfc;
import de.mhu.hair.wdk.StructurePlugin.ActionContainer;
import de.mhu.lib.log.AL;
import de.mhu.lib.resources.ImageProvider;
import de.mhu.lib.xml.XmlTool;

public class ActionsPlugin implements Plugin, ApiObjectHotSelect,
		ActionListener, ApiActionsManager {

	private static AL log = new AL(ActionsPlugin.class);
	
	private DMConnection con;
	private PluginNode node;

	private IDfPersistentObject[] target;

	private Hashtable<String, ActionStruc> actions;
	private Vector<AbstractButton> elements;
	private LinkedList<PopupEntry> popupEntries;

	private Color colorForeground = Color.BLACK; // TODO load from current LAF
	private Color colorDisabled = Color.GRAY;

	public void initPlugin(PluginNode pNode, PluginConfig pConfig)
			throws DfException {
		con = (DMConnection) pNode.getSingleApi(DMConnection.class);
		node = pNode;

		actions = new Hashtable<String, ActionStruc>();
		elements = new Vector<AbstractButton>();

		NodeList actionNodes = XmlTool.getLocalElements(pConfig.getNode(),
				"action");
		for (int i = 0; i < actionNodes.getLength(); i++) {
			Element actionNode = (Element) actionNodes.item(i);
			log.info("Load Action: " + actionNode.getAttribute("id"));
			try {

				Element actionConfig = null;
				NodeList n = XmlTool.getLocalElements(actionNode, "config");
				if (n.getLength() > 0)
					actionConfig = (Element) n.item(0);

				addAction(actionNode.getAttribute("id"), actionNode.getAttribute("class"), actionConfig);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		NodeList buttonNodes = XmlTool.getLocalElements(pConfig.getNode(),
				"button");
		for (int i = 0; i < buttonNodes.getLength(); i++) {
			Element buttonNode = (Element) buttonNodes.item(i);
			String title = buttonNode.getAttribute("title");
			if (title == null || title.length() == 0)
				title = actions.get(buttonNode.getAttribute("action")).title;
			JButton button = new JButton(title);
			if (!buttonNode.getAttribute("icon").equals("")) {
				button.setIcon(ImageProvider.getInstance().getIcon(
						buttonNode.getAttribute("icon")));
			}
			button.setToolTipText( buttonNode.getAttribute("tip"));
			button.setMargin(new Insets(2, 20, 2, 20));
			button.setActionCommand(buttonNode.getAttribute("action"));
			button.addActionListener(this);
			elements.add(button);
			((ApiToolbar) node.getSingleApi(ApiToolbar.class))
					.addToolbarButton(button);

		}

		NodeList menuNodes = XmlTool
				.getLocalElements(pConfig.getNode(), "menu");
		for (int i = 0; i < menuNodes.getLength(); i++) {
			Element menuNode = (Element) menuNodes.item(i);
			String title = menuNode.getAttribute("title");
			if (title == null || title.length() == 0)
				title = actions.get(menuNode.getAttribute("action")).title;
			JMenuItem menu = new JMenuItem(title);
			if (!menuNode.getAttribute("icon").equals("")) {
				menu.setIcon(ImageProvider.getInstance().getIcon(
						menuNode.getAttribute("icon")));
				// menu.setMargin( new Insets( 0, 0, 0, 0 ) );
			}
			if (menuNode.getAttribute("key").length() != 0)
				menu.setAccelerator(KeyStroke.getKeyStroke(menuNode
						.getAttribute("key")));

			menu.setActionCommand(menuNode.getAttribute("action"));
			menu.addActionListener(this);
			elements.add(menu);
			((ApiMenuBar) node.getSingleApi(ApiMenuBar.class)).addMenuItem(
					menuNode.getAttribute("location") + '/'
							+ menuNode.getAttribute("action"), menu);
		}

		popupEntries = new LinkedList<PopupEntry>();
		NodeList popupNodes = XmlTool.getLocalElements(pConfig.getNode(),
				"popup");
		for (int i = 0; i < popupNodes.getLength(); i++) {
			Element popupNode = (Element) popupNodes.item(i);
			try {
				PopupEntry entry = new PopupEntry();
				entry.title = popupNode.getAttribute("title");
				if (entry.title == null || entry.title.length() == 0)
					entry.title = actions.get(popupNode.getAttribute("action")).title;
				if (!popupNode.getAttribute("icon").equals("")) {
					entry.icon = ImageProvider.getInstance().getIcon(
							popupNode.getAttribute("icon"));
				}
				entry.action = popupNode.getAttribute("action");
				entry.location = popupNode.getAttribute("location");
				popupEntries.add(entry);
			} catch ( Exception e ) {
				log.error("POPUP " + popupNode.getAttribute("action"),e);
			}

		}

		refreshElements();

		node.addApi(ApiObjectHotSelect.class, this);
		node.addApi(ApiObjectWorker.class, new ActionObjectWorker());
	}

	public void apiObjectHotSelected(DMConnection pCon,
			IDfPersistentObject[] pParents, IDfPersistentObject[] pObj)
			throws Exception {
		con = pCon;
		target = pObj;
		refreshElements();
	}

	private synchronized void refreshElements() {

		for (AbstractButton b : elements) {
			String action = b.getActionCommand();
			try {
				ActionStruc struc = actions.get(action);
				if ( struc == null ) continue;
				ActionIfc act = struc.action;
				boolean enabled = act.isEnabled(node, con, target);
				if ( b instanceof JMenuItem ) {
					if ( act instanceof ActionUIIfc && ((ActionUIIfc)act).hasOwnTargetDialog() )
						b.setForeground( enabled ? colorForeground : colorDisabled );
					else {
						b.setForeground( colorForeground );
						b.setEnabled(enabled);						
					}
				} else
					b.setEnabled(enabled);

			} catch (Exception e) {
				b.setEnabled(false);
				log.error("Element Action: " + action, e );
			}

		}

	}

	public void destroyPlugin() throws Exception {

		for (Iterator<ActionStruc> i = actions.values().iterator(); i.hasNext();) {
			try {
				i.next().action.destroyAction();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		node.removeApi(this);
	}

	public void apiObjectDepricated() {

	}

	public void actionPerformed(ActionEvent e) {

		JComponent component = ((ApiLayout) node
				.getSingleApi(ApiLayout.class)).getMainComponent();
		
		String action = e.getActionCommand();
		ActionStruc struc = actions.get(action);
		if ( struc == null ) {
			JOptionPane.showMessageDialog(component, "Action was not correct initialized", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		ActionIfc act = struc.action;
		if (act == null) {
			JOptionPane.showMessageDialog(component, "Action was not correct initialized", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		try {

			// boolean enabled = act.isEnabled(node, con, target))

			if ( act instanceof ActionUIIfc )
				((ActionUIIfc)act).setActionTrustTargets( !(e.getSource() instanceof JMenuItem) );
			act.actionPerformed(node, con, target );

		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(component, ex.toString(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}

		if (target != null) {
			try {
				for (int i = 0; i < target.length; i++)
					if ( target[i] != null ) 
						target[i].fetch(null); // Update selected
						//target[i].getString("r_object_type");
			} catch (Exception ex) {
				ex.printStackTrace();
				target = null;
			}
		}
		refreshElements();

	}

	class ActionObjectWorker implements ApiObjectWorkerGui {

		public void appendPopupMenu(JPopupMenu menu, final PluginNode node,
				final DMConnection con, final IDfPersistentObject[] obj) {

			JMenu subMenu = new JMenu("Actions");
			boolean isOne = false;
			for (final PopupEntry action : popupEntries) {
				try {
					if ( action.getAction() != null ) {
						JMenuItem item = new JMenuItem(action.title);
						item.setEnabled(action.getAction()
								.isEnabled(node, con, obj));
						// find location
						String[] location = action.location.split("/");
						JMenu curMenu = subMenu;
						for (String subPart : location) {
							JMenu next = null;
							for (Component c : curMenu.getMenuComponents())
								if ((c instanceof JMenu)
										&& subPart.equals(((JMenu) c)
												.getActionCommand())) {
									next = (JMenu) c;
									break;
								}
							if (next == null) {
								next = new JMenu(subPart);
								curMenu.add(next);
							}
							curMenu = next;
						}
	
						curMenu.add(item);
						isOne = true;
						item.addActionListener(new ActionListener() {
	
							public void actionPerformed(ActionEvent e) {
								try {
									if ( action instanceof ActionUIIfc )
										((ActionUIIfc)action).setActionTrustTargets( true );
									action.getAction().actionPerformed(node, con,
											obj);
								} catch (Exception e1) {
									e1.printStackTrace();
								}
							}
	
						});
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (isOne) {
				menu.add(subMenu);
			}

		}

		public boolean canWorkOn(DMConnection con, IDfPersistentObject[] obj) {
			return true;
		}

		public String getTitleFor(DMConnection con, IDfPersistentObject[] obj) {
			return "";
		}

		public void workWith(DMConnection con, IDfPersistentObject[] obj) {
		}

	}

	private class ActionStruc {
		public ActionStruc( ActionIfc ai) {
			action = ai;
			// isPopup = ACast.toboolean( actionNode.getAttribute( "popup" ),
			// false );
			title = action.getTitle();
			if (title == null || title.length() == 0)
				title = action.getClass().getSimpleName();
		}

		ActionIfc action;
		// boolean isPopup;
		String title;
	}

	private class PopupEntry {

		public String location;
		public String action;
		public ImageIcon icon;
		public String title;

		public ActionIfc getAction() {
			ActionStruc a = actions.get(action);
			if (a==null) return null;
			return a.action;
		}

	}

	public void addAction(String id, String clazz, Element config) throws Exception {
		
		log.info("Load Action: " + id);
		ActionIfc ai = (ActionIfc) Class.forName( clazz ).newInstance();
		ai.initAction(node, con, config);

		if ( actions.put(id, new ActionStruc(ai)) != null )
			log.warn( "Action was already defined: " + id );

	}

	public void addMenu() throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void addPopup() throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void addToolbar() throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void removeAction(String id) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void removeMenu() throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void removePopup() throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void removeToolbar() throws Exception {
		// TODO Auto-generated method stub
		
	}
}
