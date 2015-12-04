package de.mhu.morse.eclipse.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.*;
import de.mhu.lib.log.AL;
import de.mhu.lib.log.LoggerTableViewer;

public class LoggerView extends ViewPart {
	
	private AL log = new AL( LoggerView.class );
	private LoggerTableViewer viewer;
	
	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new LoggerTableViewer(parent);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	
}