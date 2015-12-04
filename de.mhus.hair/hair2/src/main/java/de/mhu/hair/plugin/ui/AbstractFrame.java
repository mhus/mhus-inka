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

package de.mhu.hair.plugin.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyVetoException;
import java.util.Hashtable;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.mhu.hair.api.ApiLayout;
import de.mhu.hair.api.ApiMenuBar;
import de.mhu.hair.api.ApiPersistent;
import de.mhu.hair.api.ApiToolbar;
import de.mhu.hair.api.ApiPersistent.PersistentManager;
import de.mhu.hair.plugin.Plugin;
import de.mhu.hair.plugin.PluginConfig;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.lib.resources.ImageProvider;
import de.mhu.lib.swing.ASwing;
import de.mhu.lib.xml.XmlTool;

public abstract class AbstractFrame extends JPanel implements Plugin,
		ApiToolbar, ApiLayout, ApiMenuBar {

	private JToolBar toolbar;
	private JDesktopPane desktop;
	private Hashtable positions = new Hashtable();
	PluginNode node;
	private PluginConfig config;
	private JMenuBar menuBar;
	private Hashtable menuBarStructure;
	private static int innerLocationX = -10;
	private static int innerLocationY = -10;
	private JMenu windowManagerMenu = new JMenu("Window");
	private JToolBar windowManagerPanel = null;
	private Hashtable windowList = new Hashtable();
	protected PersistentManager persistents;

	public void initPlugin(PluginNode pNode, PluginConfig pConfig)
			throws Exception {
		node = pNode;
		config = pConfig;
		persistents = ((ApiPersistent) node.getSingleApi(ApiPersistent.class))
				.getManager(pConfig.getNode().getAttribute("persistent"));

		initUI();

		pNode.addApi(ApiMenuBar.class, this);
		pNode.addApi(ApiToolbar.class, this);
		pNode.addApi(ApiLayout.class, this);

	}

	public static Dimension getWinSize(String attribute, int maxX, int maxY ) {
		
		int pos = attribute.indexOf('x');
		int w = maxX;
		int h = maxY;
		if ( pos > 0 ) {
			String sub = attribute.substring(0, pos).trim();
			if ( sub.endsWith("%") )
				w = (int)((double)maxX / 100d * (double)Integer.parseInt(sub.substring(0,sub.length()-1) ));
			else
				w = Integer.parseInt(sub);
			sub = attribute.substring(pos + 1).trim();
			if ( sub.endsWith("%") )
				h = (int)((double)maxY / 100d * (double)Integer.parseInt(sub.substring(0,sub.length()-1) ));
			else
				h = Integer.parseInt(sub);
		}
		if ( w > maxX ) w = maxX;
		if ( h > maxY ) h = maxY;
		if ( w < 0 ) w = 0;
		if ( h < 0 ) h = 0;
		return new Dimension( w, h );
	}

	public static Dimension getWinSize(Point start, String attribute, int maxX, int maxY ) {
		
		int pos = attribute.indexOf('x');
		maxX = Math.max( 10, maxX - start.x );
		maxY = Math.max( 10, maxY - start.y );
		
		int w = maxX;
		int h = maxY;
		if ( pos > 0 ) {
			String sub = attribute.substring(0, pos).trim();
			if ( sub.endsWith("%") )
				w = (int)((double)maxX / 100d * (double)Integer.parseInt(sub.substring(0,sub.length()-1) ));
			else
				w = Integer.parseInt(sub);
			sub = attribute.substring(pos + 1).trim();
			if ( sub.endsWith("%") )
				h = (int)((double)maxY / 100d * (double)Integer.parseInt(sub.substring(0,sub.length()-1) ));
			else
				h = Integer.parseInt(sub);
		}
		if ( w > maxX ) w = maxX;
		if ( h > maxY ) h = maxY;
		if ( w < 0 ) w = 0;
		if ( h < 0 ) h = 0;
		return new Dimension( w, h );
	}

	public static Point getWinPoint(String attribute, int maxX, int maxY) {
		int pos = attribute.indexOf('x');
		int x = 0;
		int y = 0;
		if ( pos > 0 ) {
			String sub = attribute.substring(0, pos).trim();
			if ( sub.endsWith("%") )
				x = (int)((double)maxX / 100d * (double)Integer.parseInt(sub.substring(0,sub.length()-1) ));
			else
				x = Integer.parseInt(sub);
			
			sub = attribute.substring(pos + 1).trim();
			if ( sub.endsWith("%") )
				y = (int)((double)maxY / 100d * (double)Integer.parseInt(sub.substring(0,sub.length()-1) ));
			else
				y = Integer.parseInt(sub);
		}
		if ( x > maxX ) x = maxX;
		if ( y > maxY ) y = maxY;
		if ( x < 0 ) x = 0;
		if ( y < 0 ) y = 0;
		return new Point(x,y);
	}

	public static Point getWinPoint(String attribute, int maxX, int maxY, int defaultX, int defaultY) {
		int pos = attribute.indexOf('x');
		int x = defaultX;
		int y = defaultY;
		if ( pos > 0 ) {
			String sub = attribute.substring(0, pos).trim();
			if ( sub.endsWith("%") )
				x = (int)((double)maxX / 100d * (double)Integer.parseInt(sub.substring(0,sub.length()-1) ));
			else
				x = Integer.parseInt(sub);
			
			sub = attribute.substring(pos + 1).trim();
			if ( sub.endsWith("%") )
				y = (int)((double)maxY / 100d * (double)Integer.parseInt(sub.substring(0,sub.length()-1) ));
			else
				y = Integer.parseInt(sub);
		}
		if ( x > maxX ) x = maxX;
		if ( y > maxY ) y = maxY;
		if ( x < 0 ) x = 0;
		if ( y < 0 ) y = 0;
		return new Point(x,y);
	}
	
	private void initUI() {

		windowManagerPanel = new JToolBar();
		windowManagerPanel.setLayout(new GridLayout( 1, 1 ));
		
		JPanel tbPanel = new JPanel();
		tbPanel.setLayout(new BorderLayout());

		toolbar = new JToolBar();
		String bgColorTxt = persistents.getProperty("frame.toolbar.bgcolor");
		if (bgColorTxt != null)
			toolbar.setBackground(Color.decode(bgColorTxt));
		tbPanel.add(toolbar, BorderLayout.NORTH);
		tbPanel.add(windowManagerPanel, BorderLayout.SOUTH );

		NodeList list = XmlTool.getLocalElements(config.getNode(), "*");
		if (list.getLength() > 0)
			tbPanel.add(initComponent((Element) list.item(0)),
					BorderLayout.CENTER);

		setLayout(new BorderLayout());
		add(tbPanel, BorderLayout.CENTER);

	}

	private JComponent initComponent(Element n) {

		String name = n.getNodeName();
		if (name.equals("position")) {
			JPanel out = new JPanel();
			out.setLayout(new BorderLayout());
			configComponent(out, n);
			positions.put(n.getAttribute("pos"), out);
			return out;
		} else if (name.equals("panel")) {
			JPanel panel = new JPanel();
			if (n.getAttribute("layout").equals("box_x"))
				panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
			else if (n.getAttribute("layout").equals("box_y"))
				panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			else
				panel.setLayout(new BorderLayout());

			NodeList list = XmlTool.getLocalElements(n, "*");
			for (int i = 0; i < list.getLength(); i++) {
				Element n2 = (Element) list.item(i);
				panel.add(initComponent(n2), (n2.getAttribute("constraint")
						.equals("") ? null : BorderLayout.CENTER));
			}

			return panel;
		} else if (name.equals("split")) {
			JSplitPane split = new JSplitPane("v".equals(n
					.getAttribute("orientation")) ? JSplitPane.VERTICAL_SPLIT
					: JSplitPane.HORIZONTAL_SPLIT);

			split.setOneTouchExpandable(true);
			split.setTopComponent(initComponent((Element) XmlTool
					.getLocalElements(n, "*").item(0)));
			split.setBottomComponent(initComponent((Element) XmlTool
					.getLocalElements(n, "*").item(1)));
			return split;
		} else if (name.equals("desktop")) {
			if (desktop != null)
				return null;
			desktop = new JDesktopPane();
			String bgColorTxt = persistents.getProperty("frame.bgcolor");
			if (bgColorTxt != null)
				desktop.setBackground(Color.decode(bgColorTxt));
			// JScrollPane scroller = new JScrollPane( desktop );
			configComponent(desktop, n);
			// return scroller;
			return desktop;

		} else if (name.equals("tabs")) {
			JTabbedPane out = new JTabbedPane();
			configComponent(out, n);
			if (n.getAttribute("pos").length() != 0) {
				positions.put(n.getAttribute("pos"), out);
			}
			NodeList list = XmlTool.getLocalElements(n, "*");
			for (int i = 0; i < list.getLength(); i++) {
				Element n2 = (Element) list.item(i);
				out.addTab(n2.getAttribute("name"), initComponent(n2));
			}

			return out;
		}

		return new JPanel();
	}

	private void configComponent(JComponent out, Element n) {
		if (n.getAttribute("min").length() != 0)
			out.setMinimumSize(getDimension(n.getAttribute("min")));
		if (n.getAttribute("max").length() != 0)
			out.setMinimumSize(getDimension(n.getAttribute("max")));
		if (n.getAttribute("pref").length() != 0)
			out.setMinimumSize(getDimension(n.getAttribute("pref")));
	}

	private Dimension getDimension(String attribute) {
		int pos = attribute.indexOf('x');
		return new Dimension(Integer.parseInt(attribute.substring(0, pos)
				.trim()), Integer.parseInt(attribute.substring(pos + 1).trim()));
	}

	public void addToolbarButton(JComponent button) {
		String bgColorTxt = persistents.getProperty("frame.toolbar.bgcolor");
		if (bgColorTxt != null)
			button.setBackground(Color.decode(bgColorTxt));

		toolbar.add(button);
	}

	public void setComponent(JComponent panel, Element attr) throws Exception {
		setComponent(panel, attr, null, null);
	}

	public void setComponent(JComponent panel, Element attr,
			ApiLayout.Listener listener) throws Exception {
		setComponent(panel, attr, null, listener);
	}

	public void setComponent(JComponent panel, Element attr, String title,
			ApiLayout.Listener listener) throws Exception {

		Cont cont = new Cont(panel, attr, title, listener);

	}

	public static void configFrame(PluginNode node, JDesktopPane desktop, JInternalFrame frame, Element attr, String title) {

		boolean isSet = false;
		if (attr.getAttribute("persistent").length() != 0) {
			final PersistentManager manager = ((ApiPersistent) node
					.getSingleApi(ApiPersistent.class)).getManager(attr
					.getAttribute("persistent"));
			if (manager.getProperty("frame.location") != null) {
				frame.setLocation(getWinPoint(manager
						.getProperty("frame.location"), desktop.getWidth(), desktop.getHeight() ));
				frame.setSize(getWinSize(manager.getProperty("frame.size"), desktop.getWidth()-frame.getX(), desktop.getHeight()-frame.getY() ));
				isSet = true;
			}
			/*
			 * if ( manager.getProperty( "frame.iconified" ) != null ) { try {
			 * frame.setIcon( "1".equals( manager.getProperty( "frame.iconified"
			 * ) ) ); } catch (PropertyVetoException e1) { // TODO
			 * Auto-generated catch block e1.printStackTrace(); } }
			 */
			frame.addInternalFrameListener(new InternalFrameAdapter() {

				public void internalFrameDeiconified(InternalFrameEvent e) {
					manager.setProperty("frame.iconified", "0");
					manager.setLazyChanged();
				}

				public void internalFrameIconified(InternalFrameEvent e) {
					manager.setProperty("frame.iconified", "1");
					manager.setLazyChanged();
				}

			});
			frame.addComponentListener(new ComponentAdapter() {

				public void componentMoved(ComponentEvent e) {
					manager.setProperty("frame.location", e.getComponent()
							.getX()
							+ "x" + e.getComponent().getY());
					manager.setProperty("frame.size", e.getComponent()
							.getWidth()
							+ "x" + e.getComponent().getHeight());
					manager.setLazyChanged();
				}

				public void componentResized(ComponentEvent e) {
					manager.setProperty("frame.location", e.getComponent()
							.getX()
							+ "x" + e.getComponent().getY());
					manager.setProperty("frame.size", e.getComponent()
							.getWidth()
							+ "x" + e.getComponent().getHeight());
					manager.setLazyChanged();
				}

			});
		}

		if (!isSet) {

			if (attr.getAttribute("location").length() != 0)
				try {
					frame
							.setLocation(getWinPoint(attr
									.getAttribute("location"), desktop.getWidth(), desktop.getHeight() ));
				} catch (Exception e) {
					frame.setLocation(createNewInnerLocation(desktop, frame.getSize()));
				}
			else
				frame.setLocation(createNewInnerLocation(desktop, frame.getSize()));
			
			if (attr.getAttribute("size").length() != 0)
				try {
					frame.setSize(getWinSize(attr.getAttribute("size"), desktop.getWidth()-frame.getX(), desktop.getHeight()-frame.getY() ));
				} catch (Exception e) {
					frame.pack();
				}
			else
				frame.pack();

		}

		frame.setResizable(!attr.getAttribute("resizable").equals("0"));
		frame.setClosable(!attr.getAttribute("closable").equals("0"));
		frame.setIconifiable(!attr.getAttribute("iconifiable").equals("0"));
		frame.setMaximizable(!attr.getAttribute("maximizable").equals("0"));

		if (!attr.getAttribute("icon").equals("")) {
			frame.setFrameIcon(ImageProvider.getInstance().getIcon(
					attr.getAttribute("icon")));
		}

		if (title == null)
			title = attr.getAttribute("title");
		// -- title = config.parseString( title );
		frame.setTitle(title);

		frame.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
		if (attr.getAttribute("close.operation").equals("dispose"))
			frame.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);

	}

	public static Point createNewInnerLocation(JDesktopPane desktop, Dimension size) {

		innerLocationX += 20;
		innerLocationY += 20;

		if (desktop != null) {
			if (innerLocationX + size.width > desktop.getWidth())
				innerLocationX = 0;
			if (innerLocationY + size.height > desktop.getHeight())
				innerLocationY = 0;
		}

		return new Point(innerLocationX, innerLocationY);
	}

	public static void configFrame(PluginNode node, PersistentManager persistents, JFrame frame, Element attr, String title) {

		boolean isSet = false;

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		
		if (attr.getAttribute("persistent").length() != 0) {
			final PersistentManager manager = ((ApiPersistent) node
					.getSingleApi(ApiPersistent.class)).getManager(attr
					.getAttribute("persistent"));
			if (manager.getProperty("frame.location") != null) {
				frame.setLocation(getWinPoint(manager
						.getProperty("frame.location"), screenSize.width, screenSize.height ));
				frame.setSize(getWinSize(manager.getProperty("frame.size"), screenSize.width-frame.getX(),screenSize.height-frame.getY()));
				isSet = true;
			}
			frame.addWindowListener(new WindowAdapter() {

				public void windowDeiconified(WindowEvent e) {
					manager.setProperty("frame.iconified", "0");
					manager.setLazyChanged();
				}

				public void windowIconified(WindowEvent e) {
					manager.setProperty("frame.iconified", "1");
					manager.setLazyChanged();
				}

			});
			frame.addComponentListener(new ComponentAdapter() {

				public void componentMoved(ComponentEvent e) {
					manager.setProperty("frame.location", e.getComponent()
							.getX()
							+ "x" + e.getComponent().getY());
					manager.setProperty("frame.size", e.getComponent()
							.getWidth()
							+ "x" + e.getComponent().getHeight());
					manager.setLazyChanged();
				}

				public void componentResized(ComponentEvent e) {
					manager.setProperty("frame.location", e.getComponent()
							.getX()
							+ "x" + e.getComponent().getY());
					manager.setProperty("frame.size", e.getComponent()
							.getWidth()
							+ "x" + e.getComponent().getHeight());
					manager.setLazyChanged();
				}

			});

		}

		if (!isSet) {

			if (attr.getAttribute("location").length() != 0)
				frame.setLocation(getWinPoint(attr.getAttribute("location"),screenSize.width,screenSize.height));
			if (attr.getAttribute("size").length() != 0)
				frame.setSize(getWinSize(attr.getAttribute("size"),screenSize.width-frame.getX(),screenSize.height-frame.getY()));
		}

		frame.setResizable(!attr.getAttribute("resizable").equals("0"));
		// frame.setClosable( !attr.getAttribute( "closable").equals("0") );

		if (title == null)
			title = attr.getAttribute("title");
		if (persistents.getProperty("frame.title") != null)
			title = persistents.getProperty("frame.title") + " - " + title;

		// -- title = config.parseString( title );

		if (!attr.getAttribute("icon").equals("")) {
			ImageIcon icon = ImageProvider.getInstance().getIcon(
					attr.getAttribute("icon"));
			if (icon != null)
				frame.setIconImage(icon.getImage());
		}

		frame.setTitle(title);

		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		if (attr.getAttribute("close.operation").equals("dispose"))
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

	}

	public JComponent getMainComponent() {
		return toolbar;
	}

	public void destroyPlugin() {
		// TODO Auto-generated method stub

	}

	public void removeComponent(JComponent panel) {
		removeComponent(panel, true);
	}

	public boolean isComponent(JComponent panel) {
		return windowList.containsKey(panel);
	}

	protected void removeComponent(JComponent panel, boolean close) {
		Object[] objects = (Object[]) windowList.get(panel);
		if (objects == null)
			return;

		Object frame = objects[0];

		if (close) {
			if (frame instanceof JInternalFrame) {
				((JInternalFrame) frame).dispose();
			} else if (frame instanceof JFrame) {
				((JFrame) frame).dispose();
			}
		}

		JMenuItem item = (JMenuItem) objects[1];
		windowManagerMenu.remove(item);
		windowManagerPanel.remove( (JButton)objects[2] );
		windowManagerPanel.revalidate();
		windowManagerPanel.repaint();
		windowList.remove(panel);

	}

	public void addMenuItem(String location, JComponent item) {

		String bgColorTxt = persistents.getProperty("frame.menubar.bgcolor");

		if (menuBar == null) {
			menuBar = new JMenuBar();
			if (bgColorTxt != null)
				menuBar.setBackground(Color.decode(bgColorTxt));
			menuBarStructure = new Hashtable();
			setMenuBarInternal(menuBar);
		}

		String[] parts = location.split("/");

		Hashtable last = menuBarStructure;
		JComponent lastMenu = menuBar;

		for (int i = 0; i < parts.length - 1; i++) {

			Object[] out = (Object[]) last.get(parts[i]);
			if (out == null) {

				JMenu nextMenu = new JMenu(parts[i]);
				if (i == 0 && bgColorTxt != null)
					nextMenu.setBackground(Color.decode(bgColorTxt));
				Hashtable next = new Hashtable();
				lastMenu.add(nextMenu);

				out = new Object[] { next, nextMenu };
				last.put(parts[i], out);

			}

			last = (Hashtable) out[0];
			lastMenu = (JComponent) out[1];

		}

		int componentPos = -1;

		Object[] occupied = (Object[]) last.get(parts[parts.length - 1]);
		if (occupied != null) {
			for (int i = 0; i < lastMenu.getComponentCount(); i++)
				if (lastMenu.getComponent(i) == occupied[1]) {
					componentPos = i;
					break;
				}
			removeMenuItem(location);
		}

		if (item != null) {

			if (parts.length == 1 && bgColorTxt != null)
				item.setBackground(Color.decode(bgColorTxt));

			if (componentPos < 0)
				lastMenu.add(item);
			else
				lastMenu.add(item, componentPos);
			last.put(parts[parts.length - 1], new Object[] { new Hashtable(),
					item });
		}

	}

	protected abstract void setMenuBarInternal(JMenuBar menuBar2);

	public void removeMenuItem(String location) {

		if (menuBar == null)
			return;

		String[] parts = location.split("/");
		Hashtable last = menuBarStructure;
		JComponent lastMenu = menuBar;

		for (int i = 0; i < parts.length - 1; i++) {

			Object[] out = (Object[]) last.get(parts[i]);
			if (out == null) {

				JMenu nextMenu = new JMenu(parts[i]);
				Hashtable next = new Hashtable();
				lastMenu.add(nextMenu);

				out = new Object[] { next, nextMenu };
				last.put(parts[i], out);

			}

			last = (Hashtable) out[0];
			lastMenu = (JComponent) out[1];

		}

		Object[] out = (Object[]) last.get(parts[parts.length - 1]);
		if (out == null)
			return;

		lastMenu.remove((JComponent) out[1]);
		last.remove(parts[parts.length - 1]);

	}

	public JComponent getWindowManagerMenu() {
		return windowManagerMenu;
	}

	private class Cont {// Component Container

		private JComponent panel;
		private Listener listener;
		private String title;
		private Icon icon;

		public Cont(JComponent pPanel, Element attr, String pTitle,
				Listener pListener) throws Exception {

			panel = pPanel;
			title = pTitle;
			listener = pListener;

			String pos = attr.getAttribute("pos");
			if (title == null)
				title = attr.getAttribute("title");

			JComponent px = (JComponent) positions.get(pos);
			if (px == null)
				pos = "*"; // fallback

			if ((pos.startsWith("*tab:") || pos.equals("*"))
					&& (positions.get(pos) instanceof JTabbedPane)) {
				JTabbedPane tab = (JTabbedPane) positions.get(pos);
				tab.addTab(title, panel);

			} else if (pos.equals("*")) {

				if (windowList.get(panel) != null)
					throw new Exception("Panel already in use");

				if (desktop != null) {

					JInternalFrame frame = new JInternalFrame();
					frame.getContentPane().add(panel);
					frame.pack();

					configFrame(node, desktop, frame, attr, title);
					icon = frame.getFrameIcon();
					frame.addInternalFrameListener(new InternalFrameListener() {

						public void internalFrameActivated(InternalFrameEvent e) {
						}

						public void internalFrameClosed(InternalFrameEvent e) {
							if (listener != null)
								listener.windowClosed(e.getSource());
							removeComponent(panel, false);
						}

						public void internalFrameClosing(InternalFrameEvent e) {
						}

						public void internalFrameDeactivated(
								InternalFrameEvent e) {
						}

						public void internalFrameDeiconified(
								InternalFrameEvent e) {
						}

						public void internalFrameIconified(InternalFrameEvent e) {
						}

						public void internalFrameOpened(InternalFrameEvent e) {
						}

					});

					// put frame into a list of available windows
					JMenuItem item = new JMenuItem(title);
					if ( icon != null ) item.setIcon(icon);
					item.addActionListener(new WindowActionListener(panel));
					windowManagerMenu.add(item);
					JButton button = new JButton( title );	
					button.setMaximumSize(new Dimension(150,40));
					button.setToolTipText(title);
					button.setMargin(new Insets(2,2,2,2));
					if ( icon != null ) button.setIcon(icon);
					button.addActionListener(new WindowActionListener(panel));
					windowManagerPanel.add(button);
					windowList.put(panel, new Object[] { frame, item, button });

					frame.setVisible(true);
					desktop.add(frame);
					try {
						frame.setSelected(true);
					} catch (java.beans.PropertyVetoException e) {
					}
				} else {
					JFrame frame = new JFrame();
					frame.getContentPane().add(panel);
					frame.pack();
					configFrame(node, persistents, frame, attr, title);
					if ( frame.getIconImage() != null )
						icon = new ImageIcon( frame.getIconImage() );
					frame.addWindowListener(new WindowListener() {

						public void windowActivated(WindowEvent e) {
						}

						public void windowClosed(WindowEvent e) {
							if (listener != null)
								listener.windowClosed(e.getSource());
						}

						public void windowClosing(WindowEvent e) {
						}

						public void windowDeactivated(WindowEvent e) {
						}

						public void windowDeiconified(WindowEvent e) {
						}

						public void windowIconified(WindowEvent e) {
						}

						public void windowOpened(WindowEvent e) {
						}

					});

					// put frame into a list of available windows
					JMenuItem item = new JMenuItem(title);
					if ( icon != null ) item.setIcon(icon);
					item.addActionListener(new WindowActionListener(panel));
					windowManagerMenu.add(item);
					JButton button = new JButton( title );					
					button.setMaximumSize(new Dimension(150,40));
					button.setToolTipText(title);
					button.setMargin(new Insets(2,2,2,2));
					if ( icon != null ) button.setIcon(icon);
					button.addActionListener(new WindowActionListener(panel));
					windowManagerPanel.add(button);
					windowList.put(panel, new Object[] { frame, item, button });

					frame.setVisible(true);
				}
			} else {
				JComponent p = (JComponent) positions.get(pos);
				if (p == null)
					return;
				if (p instanceof JTabbedPane) {
					// -- title = config.parseString( title );
					((JTabbedPane) p).addTab(title, panel);
				} else if (p instanceof JPanel) {
					p.removeAll();
					p.add(panel, BorderLayout.CENTER);
					p.revalidate();
					p.repaint();
				}
			}

		}
	}

	private class WindowActionListener implements ActionListener {

		private JComponent panel;

		public WindowActionListener(JComponent pPanel) {
			panel = pPanel;
		}

		public void actionPerformed(ActionEvent e) {

			Object[] objects = (Object[]) windowList.get(panel);
			if (objects == null)
				return;
			Object frame = objects[0];

			if (frame instanceof JInternalFrame) {
				JInternalFrame f = ((JInternalFrame) frame);
				try {
					f.setIcon(false);
				} catch (PropertyVetoException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				f.toFront();
				f.grabFocus();
			} else if (frame instanceof JFrame) {
				JFrame f = ((JFrame) frame);
				f.toFront();
			}

		}

	}
}
