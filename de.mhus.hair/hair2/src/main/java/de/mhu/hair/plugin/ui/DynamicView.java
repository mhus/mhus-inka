package de.mhu.hair.plugin.ui;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.w3c.dom.Element;

import de.mhu.hair.api.ApiPersistent;
import de.mhu.hair.api.ApiPersistent.PersistentManager;
import de.mhu.hair.api.ApiLayout.Listener;
import de.mhu.lib.resources.ImageProvider;

import net.infonode.docking.DockingWindow;
import net.infonode.docking.DockingWindowListener;
import net.infonode.docking.OperationAbortedException;
import net.infonode.docking.View;
import net.infonode.docking.title.DockingWindowTitleProvider;

public class DynamicView extends View implements DockingWindowListener, DockingWindowTitleProvider {

	private static int nextId = 0;
	
	public boolean isEmpty = true;
	private int id;
	private Listener listener;
	public Listener getListener() {
		return listener;
	}

	private PersistentManager manager;
	private String name;
	private String title;
	private IdwFrame frame;

	private Element attr;

	private ImageIcon imageicon;
	
	public Element getAttributes() {
		return attr;
	}

	DynamicView(IdwFrame pFrame, String name) {
		this(pFrame, "["+name+"]",null,new JLabel(name),null,null);
		title = "["+name+"]";
		isEmpty=true;
		this.name = name;
	}
	
	public ImageIcon getImageIcon() {
		return imageicon;
	}
	
	public void setOwner(String title, Icon icon, Component panel,
			Element attr, Listener pListener) {
		if ( ! isEmpty )  return; //TODO error
		isEmpty=false;
		listener = pListener;
		setComponent(panel);
		if ( attr == null ) return;
		this.attr = attr;
		name = "";
		if (attr.getAttribute("persistent").length() != 0) {
			name = attr.getAttribute("persistent");
			manager = ((ApiPersistent) frame.getNode()
					.getSingleApi(ApiPersistent.class)).getManager(attr
					.getAttribute("persistent"));
			
			getWindowProperties().setCloseEnabled( !attr.getAttribute("closable").equals("0") );
			getWindowProperties().setMaximizeEnabled(!attr.getAttribute("maximizable").equals("0"));
			getWindowProperties().setMinimizeEnabled(!attr.getAttribute("iconifiable").equals("0"));
			if ( icon == null ) {
				if (!attr.getAttribute("icon").equals("")) {
					imageicon = ImageProvider.getInstance().getIcon(
							attr.getAttribute("icon"));
					icon = imageicon;
				}
			}
			if ( icon != null ) {
				getViewProperties().setIcon(icon);
				frame.setWindowMenuIcon(this,icon);
			}

		}
		this.title = title;
		frame.setWindowMenuTitle(this,title);
		fireTitleChanged();
		revalidate();
		repaint();
	}
	/**
	 * Constructor.
	 * 
	 * @param title
	 *            the view title
	 * @param icon
	 *            the view icon
	 * @param component
	 *            the view component
	 * @param listener 
	 * @param id
	 *            the view id
	 */
	DynamicView(IdwFrame pFrame, String title, Icon icon, Component component, Element attr, Listener pListener) {
		super(title, icon, component);
		this.id = nextId++;
		frame = pFrame;
		getWindowProperties().setTitleProvider(this);
		this.addListener(this);
		
		setOwner(title, icon, component, attr, pListener);
	}

	/**
	 * Returns the view id.
	 * 
	 * @return the view id
	 */
	public int getId() {
		return id;
	}

	public void viewFocusChanged(View arg0, View arg1) {
		
	}

	public void windowAdded(final DockingWindow arg0, DockingWindow arg1) {
	}

	public void windowClosed(DockingWindow arg0) {
		// TODO remove from IdwFrame
		if ( arg0 == this ) {

			this.removeListener(this);
			frame.removeComponent((JComponent)getComponent());
			if ( listener != null )
				listener.windowClosed( getComponent() );
		}
	}

	public void windowClosing(DockingWindow arg0)
			throws OperationAbortedException {
		
	}

	public void windowDocked(DockingWindow arg0) {
	}

	public void windowDocking(DockingWindow arg0)
			throws OperationAbortedException {
		
	}

	public void windowHidden(DockingWindow arg0) {
		
	}

	public void windowMaximized(DockingWindow arg0) {

	}

	public void windowMaximizing(DockingWindow arg0)
			throws OperationAbortedException {
		
	}

	public void windowMinimized(DockingWindow arg0) {
	}

	public void windowMinimizing(DockingWindow arg0)
			throws OperationAbortedException {
		
	}

	public void windowRemoved(DockingWindow arg0, DockingWindow arg1) {
	}

	public void windowRestored(DockingWindow arg0) {
	}

	public void windowRestoring(DockingWindow arg0)
			throws OperationAbortedException {
		
	}

	public void windowShown(DockingWindow arg0) {
		
	}

	public void windowUndocked(DockingWindow arg0) {
	}

	public void windowUndocking(DockingWindow arg0)
			throws OperationAbortedException {
		
	}
	
	public String getTitle(DockingWindow window) {
		return title;
	}

	public String getViewName() {
		return name;
	}
	protected void internalClose() {
		
	}
	
}
