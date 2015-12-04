package de.mhu.hair.plugin.ui;

import java.awt.BorderLayout;
import java.util.WeakHashMap;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.w3c.dom.Element;

import de.mhu.hair.HairDebug;
import de.mhu.hair.api.ApiLayout;
import de.mhu.hair.api.ApiSystem;
import de.mhu.hair.api.ApiLayout.Listener;
import de.mhu.hair.gui.ALogger;
import de.mhu.hair.gui.LoggerPanel;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.lib.ATimerTask;
import de.mhu.lib.xml.XmlTool;

public class SimpleTextWindow extends JPanel implements Listener {

	private PluginNode node;
	private boolean visible = false;
	private Element config;
	private LoggerPanel logger;
	private long timeout;
	private long lastAccess;
	
	private static WeakHashMap<SimpleTextWindow, String> windowmap = null;
	private static Object mutex = new Object();
	
	public SimpleTextWindow(PluginNode node, String title) throws Exception {
		this(node, XmlTool
				.loadXml(
						"<c pos=\"*\" size=\"600x300\" title=\"" + XmlTool.encode(title)
								+ "\"/>").getDocumentElement());
		setCloseTimeout(20);
	}

	public SimpleTextWindow(PluginNode node, Element config) {

		synchronized(mutex) {
			if (windowmap == null) {
				windowmap = new WeakHashMap<SimpleTextWindow, String>();
				((ApiSystem)node.getSingleApi(ApiSystem.class)).getTimer().schedule(new ATimerTask(){

					@Override
					public void run0() throws Exception {
						try {
							SimpleTextWindow[] list = windowmap.keySet().toArray( new SimpleTextWindow[0]);
							for ( SimpleTextWindow item : list )
								if (item.isTimeout()) item.closeWindow();
						} catch ( Throwable t ) {
							if (HairDebug.TRACE)
								t.printStackTrace();
						}
					}
					
				}, 10000, 10000);
			}
		}
		
		this.node = node;
		this.config = config;

		logger = new LoggerPanel(null,null);
		logger.getConsole().setEditable(false);
		setLayout(new BorderLayout());
		add(logger.getMainPanel(), BorderLayout.CENTER);

	}
	
	public void setCloseTimeout(int seconds) {
		timeout = seconds*1000;
		if (timeout == 0 ) 
			windowmap.remove(this);
		else
			windowmap.put(this, "");
	}
	
	public boolean isTimeout() {
		if (!visible || timeout == 0 || lastAccess == 0) return false;
		return System.currentTimeMillis() - lastAccess > timeout;
	}
	
	public ALogger getLogger() {
		showWindow();
		return logger;
	}

	public void showWindow() {
		lastAccess = System.currentTimeMillis();

		synchronized (this) {
			if (visible)
				return;
			ApiLayout layout = (ApiLayout) node.getSingleApi(ApiLayout.class);
			try {
				layout.setComponent(this, config, this);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
			//logger.start();
			visible = true;
		}
	}

	public void closeWindow() {
		synchronized (this) {
			if (!visible)
				return;
			try {
				ApiLayout layout = (ApiLayout) node.getSingleApi(ApiLayout.class);
				layout.removeComponent(this);
				visible=false;
				lastAccess=0;
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
			
		}
	}

	public void windowClosed(Object source) {
		synchronized (this) {
			visible = false;
			//logger.stop();
		}
	}
}
