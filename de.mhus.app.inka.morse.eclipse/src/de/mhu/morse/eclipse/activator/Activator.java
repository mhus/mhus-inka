package de.mhu.morse.eclipse.activator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.mhu.lib.config.ConfigManager;
import de.mhu.lib.log.ALUtilities;
import de.mhu.morse.eclipse.views.DocumentsView;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "de.mhu.morse.eclipse";

	// The shared instance
	private static Activator plugin;

	private DocumentsView documentsView;
	
	/**
	 * The constructor
	 */
	public Activator() {
		ConfigManager.initialize();
		ALUtilities.configure();
		try {
			Class.forName( "de.mhu.com.morse.eecm.MorseDriver" );
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	public void setDocumentsView( DocumentsView in ) {
		documentsView = in;
	}
	
	public DocumentsView getDocumentsView() {
		return documentsView;
	}
	
}
