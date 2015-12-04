package de.mhus.hair.dctm.ui;

import java.io.InputStream;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import de.mhus.cap.core.ui.ICaoImageProvider;
import de.mhus.hair.dctm.DctmConstant;
import de.mhus.lib.cao.CaoElement;

public class DctmImageProvider implements ICaoImageProvider {

	private static de.mhus.lib.logging.Log log = de.mhus.lib.logging.Log.getLog(DctmImageProvider.class);
	
	@Override
	public Image getImage(CaoElement element, Image previous) {
		
		try {
			Image icon = null;
	
			String contentType = null;
			if ( element.getMetadata().getDefinition(DctmConstant.ATTR_CONTENT_TYPE) != null )
				contentType = element.getString(DctmConstant.ATTR_CONTENT_TYPE);
			String objectType  = element.getString(DctmConstant.ATTR_OBJECT_TYPE);
			boolean isFolder   = element.isNode();
			
			if (contentType != null && !contentType.equals("")) {
				icon = getIcon("f_" + contentType + "_16.gif");
			}
			if (icon == null) {
				icon = getIcon("t_" + objectType + "_16.gif");
			}
			
			if (icon == null) {
				if (previous != null) return previous;
				if ( isFolder )
					icon = getIcon("t_dm_folder_16.gif");
				else
					icon = getIcon("f__16.gif");
			}
			
			return icon;

		} catch (Throwable t) {
			log.debug(t);
		}
		return previous;
	}

	private Image getIcon(String name) {
		InputStream is = getClass().getResourceAsStream("/de/mhus/hair/dctm/ui/img/dctm/" + name);
		if (is==null) return null;
		return new Image(Display.getCurrent(),is);
	}

}
