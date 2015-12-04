package de.mhus.cap.core.ui;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import de.mhus.lib.MString;
import de.mhus.lib.cao.CaoElement;

public class FileExtensionImageProvider implements ICaoImageProvider {

	private static de.mhus.lib.logging.Log log = de.mhus.lib.logging.Log.getLog(FileExtensionImageProvider.class);
	
	@Override
	public Image getImage(CaoElement element, Image previous) {
		
		try {
			String name = element.getName();
			if (name != null) {
				String ext = MString.afterIndex(name, '.');
				if (!MString.isEmpty(ext)) {
					
				}
			}
		} catch (Throwable e) {
			log.debug(e);
		}
		String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
		if (element.isNode())
		   imageKey = ISharedImages.IMG_OBJ_FOLDER;
		return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
	}

}
