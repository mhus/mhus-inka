package de.mhus.cap.ui.imageprovider;

import org.eclipse.swt.graphics.Image;

import de.mhus.lib.cao.CaoElement;

public interface ICaoImageProvider {

	/**
	 * Return an image for this element. Maybe manipulate the previous image if one is set.
	 * 
	 * @param element The element where you need a image for
	 * @param previous A base image or null if none exists
	 * @return null or an image
	 */
	public Image getImage(CaoElement element,Image previous);
	
}
