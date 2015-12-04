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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Timer;

import javax.swing.FocusManager;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import net.infonode.docking.DockingWindow;
import net.infonode.docking.DockingWindowAdapter;
import net.infonode.docking.DockingWindowListener;
import net.infonode.docking.FloatingWindow;
import net.infonode.docking.OperationAbortedException;
import net.infonode.docking.RootWindow;
import net.infonode.docking.SplitWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;
import net.infonode.docking.ViewSerializer;
import net.infonode.docking.WindowBar;
import net.infonode.docking.internal.WriteContext;
import net.infonode.docking.mouse.DockingWindowActionMouseButtonListener;
import net.infonode.docking.properties.FloatingWindowProperties;
import net.infonode.docking.properties.RootWindowProperties;
import net.infonode.docking.theme.DefaultDockingTheme;
import net.infonode.docking.theme.DockingWindowsTheme;
import net.infonode.docking.theme.ShapedGradientDockingTheme;
import net.infonode.docking.title.DockingWindowTitleProvider;
import net.infonode.docking.util.DockingUtil;
import net.infonode.docking.util.ViewMap;
import net.infonode.gui.colorprovider.UIManagerColorProvider;
import net.infonode.gui.laf.InfoNodeLookAndFeel;
import net.infonode.gui.panel.SimplePanel;
import net.infonode.gui.shaped.panel.ShapedPanel;
import net.infonode.tabbedpanel.theme.DefaultTheme;
import net.infonode.util.Direction;

import org.w3c.dom.Element;

import de.mhu.hair.api.ApiLayout;
import de.mhu.hair.api.ApiMenuBar;
import de.mhu.hair.api.ApiPersistent;
import de.mhu.hair.api.ApiSystem;
import de.mhu.hair.api.ApiToolbar;
import de.mhu.hair.api.ApiLayout.Listener;
import de.mhu.hair.api.ApiPersistent.PersistentManager;
import de.mhu.hair.plugin.Plugin;
import de.mhu.hair.plugin.PluginConfig;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.lib.ACast;
import de.mhu.lib.AString;
import de.mhu.lib.ATimerTask;
import de.mhu.res.img.LUF;

public class IdwFrame implements Plugin, ApiToolbar, ApiLayout, ApiMenuBar {

	private ViewMap viewMap = new ViewMap();
	private HashMap<Integer,DynamicView> dynamicViews = new HashMap<Integer,DynamicView>();
	private HashMap<Component, DynamicView> panels = new HashMap<Component, DynamicView>();
	private HashMap<DynamicView, JMenuItem> windowManagerList = new HashMap<DynamicView, JMenuItem>(); 
	private JMenu windowManagerMenu;
	
	private RootWindowProperties properties = new RootWindowProperties();
	private DockingWindowsTheme currentTheme = new ShapedGradientDockingTheme();
	//private DockingWindowsTheme currentTheme = new DefaultDockingTheme();
	private RootWindow rootWindow;
	private JFrame frame = null;
	private JMenuBar menuBar;
	private JToolBar toolBar;
	//private TabWindow tabWindow;
	private Hashtable menuBarStructure = new Hashtable();
	private PluginNode node;
	protected PersistentManager persistents;
	
	private MyViewSerializer serializer;
	private JMenu windowSaveMenu;
	private JMenuItem mSave;
	private JMenuItem mRestore;
	private JMenuItem mRestoreDefault;
	// private JMenuItem mRemoveEmpty;
	private PersistentManager defaultLayout;
	private DockingWindowListener dockingCloseAdapter = new DockingWindowAdapter() {
//		@Override
//		public void viewFocusChanged(View previouslyFocusedView,
//				View focusedView) {
//			FloatingWindow fw = DockingUtil.getFloatingWindowFor(focusedView);
//			if ( fw == null ) return;
//			if ( focusedView instanceof DynamicView ) {
//				fw.setTitle(((DynamicView)focusedView).getTitle(null));
//				fw.setImageIcon(((DynamicView)focusedView).getImageIcon().getImage());
//			}
//		}

		public void windowAdded(DockingWindow addedToWindow,
				DockingWindow addedWindow) {
			//--- addView(addedWindow);
		}

		public void windowRemoved(DockingWindow removedFromWindow,
				DockingWindow removedWindow) {
			//--- removeView(removedWindow);
		}

		public void windowClosing(DockingWindow window)
				throws OperationAbortedException {
			
			if ( window instanceof FloatingWindow ) {
				for ( int i = 0; i < window.getChildWindowCount(); i++ )
					window.getChildWindow(i).closeWithAbort();
			} else
			
			if ( window instanceof TabWindow ) {
				for ( int i = 0; i < window.getChildWindowCount(); i++ )
					window.getChildWindow(i).closeWithAbort();					
			} else
			
			if ( window instanceof SplitWindow ) {
				for ( int i = 0; i < window.getChildWindowCount(); i++ )
					window.getChildWindow(i).closeWithAbort();					
			}
			
		}

	};
	
	public void destroyPlugin() throws Exception {
	}

	public void initPlugin(PluginNode pNode, PluginConfig pConfig)
			throws Exception {

		node = pNode;
		
		String luf = pConfig.getNode().getAttribute("lookandfeel");
		System.out.println("LookAndFeel: " + luf );
		if (AString.isEmpty(luf))
			UIManager.setLookAndFeel(new InfoNodeLookAndFeel());
		else
		if ("default".equals(luf)) {
			// nothing
		} else {
			try {
				LookAndFeel lufobj = (LookAndFeel)Class.forName(luf).newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		windowManagerMenu = new JMenu("Window");
		windowSaveMenu = new JMenu("Options");
		mSave = new JMenuItem( "Save" );
		mRestoreDefault = new JMenuItem( "Restore Default" );
		mRestore = new JMenuItem( "Restore" );
		//mRemoveEmpty = new JMenuItem("Remove Empty" );
		windowManagerMenu.add(windowSaveMenu);
		windowSaveMenu.add(mSave);
		windowSaveMenu.add(mRestore);
		//windowSaveMenu.add(mRemoveEmpty);
		windowSaveMenu.addSeparator();
		windowSaveMenu.add(mRestoreDefault);
		mSave.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				actionMenuSave(null);
			}
			
		});
		mRestore.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				actionMenuRestore(null);
			}
			
		});
		mRestoreDefault.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				actionMenuRestoreDefault();
			}
			
		});
//		mRemoveEmpty.addActionListener(new ActionListener() {
//
//			public void actionPerformed(ActionEvent e) {
//				actionMenuRemoveEmpty();
//			}
//			
//		});
	
		persistents = ((ApiPersistent) node.getSingleApi(ApiPersistent.class))
		.getManager(pConfig.getNode().getAttribute("persistent"));
		
		defaultLayout = ((ApiPersistent) node.getSingleApi(ApiPersistent.class))
		.getManager("dock.layout");
		//defaultLayout.setLazyChanged();
		
		
		String themeClass = persistents.getProperty("idw.theme.class");
		if ( themeClass != null ) {
			currentTheme = (DockingWindowsTheme)Class.forName(themeClass).newInstance();
		}
		
		String bgColorTxt = persistents.getProperty("frame.bgcolor");
		if (bgColorTxt != null) {
			currentTheme.getRootWindowProperties().getComponentProperties().setBackgroundColor( Color.decode(bgColorTxt) );
			currentTheme.getRootWindowProperties().getWindowAreaProperties().setBackgroundColor( Color.decode(bgColorTxt) );
		}
				
		actionMenuRestore(null);
		
		menuBar = new JMenuBar();
		toolBar = new JToolBar();
		
		frame = new JFrame();
		frame.setSize(900, 700);
		AbstractFrame.configFrame(node, persistents, frame, pConfig.getNode(), null);
		frame.getContentPane().add(toolBar, BorderLayout.NORTH);
		frame.getContentPane().add(rootWindow, BorderLayout.CENTER);
		frame.setJMenuBar(menuBar);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (JOptionPane.showConfirmDialog(e.getComponent(),
						"Exit Program?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
					((ApiSystem) node.getSingleApi(ApiSystem.class)).exit(0);
			}
		});
		frame.setVisible(true);

		node.addApi(ApiMenuBar.class, this);
		node.addApi(ApiToolbar.class, this);
		node.addApi(ApiLayout.class, this);

		// internal functions
		menuBar.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				if ( arg0.isAltDown() && arg0.isShiftDown() && arg0.isControlDown() && arg0.getButton() == MouseEvent.BUTTON1 && arg0.getClickCount() > 1 )
					actionInternal( arg0 );
			}
			
		});
		
		windowManagerMenu.addMenuListener(new MenuListener() {

			public void menuCanceled(MenuEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void menuDeselected(MenuEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void menuSelected(MenuEvent e) {
				actionMenuRemoveEmpty();
			}
			
		});
	}

	protected void actionInternal(MouseEvent arg0) {
		
		JPopupMenu menu = new JPopupMenu();
		
		JMenuItem itm = null;
		
		itm = new JMenuItem( "Save Layout to default" );
		itm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionMenuSave(defaultLayout);
			}
		});
		menu.add(itm);
		
		menu.show((JComponent)arg0.getSource(), arg0.getX(), arg0.getY());

	}

	protected void actionMenuRestoreDefault() {
		actionMenuRestore(defaultLayout);
	}

	protected void actionMenuRestore( PersistentManager source ) {
		
		if ( source == null ) {
			source = persistents;
			if ( source.getProperty("dock.layout") == null ) {
				source = defaultLayout;
			}
		}
		
		String docLayout = source.getProperty("dock.layout");
		if ( serializer == null ) serializer = new MyViewSerializer();
	
		Object[] oldDynamicViews = dynamicViews.values().toArray();
		
		for ( JMenuItem item : windowManagerList.values() )
			windowManagerMenu.remove(item);
		
		for ( Object v : oldDynamicViews )
			rootWindow.removeView( (View)v );
		
		dynamicViews.clear();
		panels.clear();
		windowManagerList.clear();
				
		if ( docLayout != null ) {
			try {
				if ( rootWindow == null ) 
					rootWindow = DockingUtil.createRootWindow(viewMap, serializer, true );
				properties.addSuperObject(currentTheme.getRootWindowProperties());
				properties.getFloatingWindowProperties().setUseFrame(true);
				properties.getFloatingWindowProperties().setAutoCloseEnabled(true);
				rootWindow.getRootWindowProperties().addSuperObject(properties);
				rootWindow.getWindowProperties().setCloseEnabled(true);

				byte[] array = ACast.fromBinaryString(docLayout);
				rootWindow.read( new ObjectInputStream( new ByteArrayInputStream( array ) ) );
			} catch ( Exception ex ) {
				ex.printStackTrace();
				docLayout = null;
			}
		}
		
		if ( rootWindow == null && docLayout == null )
			createRootWindow();

		for ( Object v : oldDynamicViews ) {
			DynamicView dv = (DynamicView)v;
			addPanelToViews(dv.getViewName(), dv.getTitle(), dv.getIcon(), (JComponent)dv.getComponent(), dv.getAttributes(), dv.getListener() );
		}
		
		
	}

	protected void actionMenuSave(PersistentManager source) {
		
		if ( source == null )
			source = persistents;
		
		System.out.println( "--- Save window layout");
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			rootWindow.write( oos, false );
			oos.close();
			byte[] array = bos.toByteArray();
			source.setProperty( "dock.layout", ACast.toBinaryString( array ) );
			
			source.setLazyChanged();
		} catch (IOException e) {
			e.printStackTrace();
			source.setProperty("dock.layout", null);
		}
		
	}
	
	protected void actionMenuRemoveEmpty() {
		for( DynamicView view : dynamicViews.values().toArray( new DynamicView[0] ) )
			if ( view.isEmpty )
				removeView(view);
	}

	private void createRootWindow() {

		System.out.println( "--- Initialize layout" );
		
		rootWindow = DockingUtil.createRootWindow(viewMap, serializer, true);

		// rootWindow = new RootWindow( null );
		DynamicView view = new DynamicView(this,"#");
		dynamicViews.put(view.getId(), view);
		viewMap.addView(view.getId(), view);
		

		// Enable the bottom window bar
		rootWindow.getWindowBar(Direction.DOWN).setEnabled(true);
		rootWindow.getWindowBar(Direction.LEFT).setEnabled(true);
		rootWindow.getWindowBar(Direction.RIGHT).setEnabled(true);

		// Add a listener which shows dialogs when a window is closing or
		// closed.

		rootWindow.addListener(dockingCloseAdapter);
		
		// Add a mouse button listener that closes a window when it's clicked
		// with the middle mouse button.
		rootWindow
				.addTabMouseButtonListener(DockingWindowActionMouseButtonListener.MIDDLE_BUTTON_CLOSE_LISTENER);

		TabWindow tabWindow = new TabWindow();
		tabWindow.addTab(getDynamicView(0));
		rootWindow.setWindow(tabWindow);

		WindowBar windowBar = rootWindow.getWindowBar(Direction.DOWN);

		while (windowBar.getChildWindowCount() > 0)
			windowBar.getChildWindow(0).close();

	}

	private View getDynamicView(String name) {
		for ( DynamicView view : dynamicViews.values() )
			if ( name.equals(view.getViewName() ) ) 
					return view;
		return null;
	}
	
	private View getDynamicView(int id) {
		View view = (View) dynamicViews.get(new Integer(id));

		// if (view == null)
		// view = new DynamicView("Dynamic View " + id, VIEW_ICON,
		// createViewComponent("Dynamic View " + id), id);

		return view;
	}

	private void addView(DockingWindow window) {
		if (window instanceof View) {
			if (window instanceof DynamicView) {
				DynamicView view = (DynamicView)window;
				dynamicViews
						.put(new Integer(view.getId()), view);
				Component panel = view.getComponent();
				panels.put(panel, view);
				viewMap.addView(view.getId(), view);
				
				JMenuItem item = new JMenuItem(view.getTitle() );
				item.addActionListener(new WindowActionListener(view));
				windowManagerList.put(view,item);
				windowManagerMenu.add(item);
				
			}		
		} else {
			for (int i = 0; i < window.getChildWindowCount(); i++)
				addView(window.getChildWindow(i) );
		}
	}
	
	private void removeView(DockingWindow window) {
		if (window instanceof View) {
			if (window instanceof DynamicView) {	
				DynamicView view = (DynamicView)window;
				dynamicViews.remove(new Integer(view.getId()));
				Component panel = view.getComponent();
				panels.remove(panel);
				((DynamicView) window).remove(panel);
				viewMap.removeView(view.getId());
				window.close();
				JMenuItem item = windowManagerList.remove(view);
				if ( item != null ) windowManagerMenu.remove(item);
				
			}
		} else {
			for (int i = 0; i < window.getChildWindowCount(); i++)
				removeView(window.getChildWindow(i));

		}
	}
	
	public void addToolbarButton(JComponent button) {
		toolBar.add(button);
	}

	public JComponent getMainComponent() {
		return rootWindow;
	}

	public boolean isComponent(JComponent panel) {

		return panels.containsKey(panel);
	}

	public void removeComponent(JComponent panel) {
		DynamicView view = panels.get(panel);
		if (view == null)
			return;
		removeView(view);
	}

	public void setComponent(JComponent panel, Element attr) throws Exception {
		setComponent(panel, attr, null);
	}

	public void setComponent(JComponent panel, Element attr, Listener listener)
			throws Exception {
		setComponent(panel, attr, attr.getAttribute("title"), listener);
	}

	public void setComponent(JComponent panel, Element attr, String title,
			Listener listener) throws Exception {

		if (isComponent(panel))
			return;

		String name = null;
		if (attr.getAttribute("persistent").length() != 0) {
			name = attr.getAttribute("persistent");
		}
		
		DynamicView view = addPanelToViews( name, title, null, panel, attr, listener );
		
		view.grabFocus();

	}
	
	private DynamicView addPanelToViews(String name, String title, Icon icon,
			JComponent panel, Element attr, Listener listener) {

		TabWindow tabWindow = null;
		
		for ( DynamicView view : dynamicViews.values() ) {
			if ( name != null && name.equals( view.getViewName() )  ) {
				System.out.println( "--- found owner for: " + name );
				view.setOwner( title, null, panel, attr, listener );
				panels.put(panel, view);
				if ( !view.isVisible() )
					view.setVisible(true);
				return view;
			}
			if ( view.getWindowParent() instanceof TabWindow )
				tabWindow = (TabWindow)view.getWindowParent();
				
		}
		System.out.println( "--- owner not found for: " + name + " (" + title + ")" );
		
		if ( tabWindow == null ) {
			for ( int i = 0; i < rootWindow.getChildWindowCount(); i++) {
				DockingWindow w = rootWindow.getChildWindow(i);
				if ( w instanceof TabWindow )
					tabWindow = (TabWindow)w;
			}
		}
		
		DynamicView view = new DynamicView(this, title, icon, panel, attr, listener);
		
		if ( attr != null ) {
			String pos = attr.getAttribute("pos");
			if ( tabWindow == null || pos.equals("**") ) {
				tabWindow = new TabWindow();
				FloatingWindow fw = createFloatingWindow(view,attr);
			} else {
				tabWindow.addTab(view);
			}
		} else {
			tabWindow = new TabWindow();
			FloatingWindow fw = createFloatingWindow(view,attr);		
		}
		// find predefined ...
		addView(view);

		// TODO maybe remove
		if (getDynamicView("#") != null) {
			removeView(getDynamicView("#"));
		}
		
		return view;
	}

	private FloatingWindow createFloatingWindow(DynamicView view, Element attr) {
		View focus = rootWindow.getFocusedView();
		Window curWin = FocusManager.getCurrentManager().getActiveWindow();
		try {
			if ( curWin != null && 
				 curWin instanceof JDialog && 
				 ((JDialog)curWin).getContentPane().getComponent(0) instanceof FloatingWindow ) {
				FloatingWindow cur = (FloatingWindow)((JDialog)curWin).getContentPane().getComponent(0);
				ShapedPanel sp = (ShapedPanel)cur.getComponent(0);
				TabWindow   curtab = (TabWindow)sp.getComponent(0);
				
				curtab.addTab(view);
				curtab.setSelectedTab(curtab.getChildWindowCount()-1);
				return cur;
				
			}
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Point start = AbstractFrame.getWinPoint( attr.getAttribute("location"), screenSize.width, screenSize.height, 50, 50);
		Dimension size = AbstractFrame.getWinSize( start, attr.getAttribute("size") , screenSize.width-50, screenSize.height-50);
		FloatingWindow fw =  rootWindow.createFloatingWindow( start , size, view);
		fw.addListener(dockingCloseAdapter);
		fw.setPreferredMinimizeDirection(Direction.DOWN);
		fw.getTopLevelAncestor().setVisible(true);	
		if (LUF.HAIR_ICON != null)
			fw.setImageIcon(((ImageIcon)LUF.HAIR_ICON).getImage());
		return fw;
	}

	public void addMenuItem(String location, JComponent item) {

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

			if (componentPos < 0)
				lastMenu.add(item);
			else
				lastMenu.add(item, componentPos);
			last.put(parts[parts.length - 1], new Object[] { new Hashtable(),
					item });
		}

	}

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

	class MyViewSerializer implements ViewSerializer {

		public View readView(ObjectInputStream in) throws IOException {
			String name = in.readUTF();
			System.out.println( "--- read view: " + name );
			DynamicView view = new DynamicView( IdwFrame.this, name );
//			dynamicViews.put(view.getId(), view);
//			viewMap.addView(view.getId(), view);
			addView(view);
			return view;
		}

		public void writeView(View view, ObjectOutputStream out)
				throws IOException {
			
			if ( ! ( view instanceof DynamicView ) ) {
				out.writeUTF("");
				return;
			}
			
			DynamicView dv = (DynamicView)view;
			
			out.writeUTF( dv.getViewName() );
			
		}
		
	}

	public PluginNode getNode() {
		return node;
	}

	public JComponent getWindowManagerMenu() {
		return windowManagerMenu;
	}

	private class WindowActionListener implements ActionListener {

		private DynamicView view;

		public WindowActionListener(DynamicView pView) {
			view = pView;
		}

		public void actionPerformed(ActionEvent e) {
			// view.grabFocus();
			// view.getWindowParent()
			// parent = view.getWindowParent();
			FloatingWindow fw = DockingUtil.getFloatingWindowFor(view);
			
			if ( fw != null ) {
				Container cur = fw;
				while ( cur != null ) {
					if ( ! cur.isVisible() ) {
						createFloatingWindow(view, null);
						return;
					}
					cur = cur.getParent();
				}
			}
			
			if ( fw != null ) fw.toFront();
			
			Container cur = view;
			Container old = null;
			while ( cur != null ) {
				if ( ! ( cur instanceof SimplePanel ) && ! cur.isVisible() ) {
					createFloatingWindow(view, null);
					return;
				}
				old = cur;
				cur = cur.getParent();
			}
			
			if ( ! ( old instanceof Window ) ) {
				createFloatingWindow(view, null);
				return;				
			}
			
			
			net.infonode.docking.FocusManager.focusWindow(view);
		}

	}
	

	void setWindowMenuTitle(DynamicView view, String title) {
		JMenuItem item = windowManagerList.get(view);
		if ( item == null ) return;
		item.setText(title);
	}
	
	void setWindowMenuIcon(DynamicView view, Icon icon) {
		JMenuItem item = windowManagerList.get(view);
		if ( item == null ) return;
		item.setIcon(icon);
	}
	
}
