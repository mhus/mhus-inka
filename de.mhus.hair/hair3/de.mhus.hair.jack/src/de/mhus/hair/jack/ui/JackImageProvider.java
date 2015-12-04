package de.mhus.hair.jack.ui;

import java.io.InputStream;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import de.mhus.cap.core.ui.ICaoImageProvider;
import de.mhus.lib.cao.CaoElement;

public class JackImageProvider implements ICaoImageProvider {

	private static de.mhus.lib.logging.Log log = de.mhus.lib.logging.Log.getLog(JackImageProvider.class);
	
	@Override
	public Image getImage(CaoElement element, Image previous) {
		try {
			Image icon = null;
			String primaryType = element.getString("jcr:primaryType");
			if (primaryType != null) {
				primaryType = primaryType.replaceAll(":", "_").toLowerCase();
				icon = getIcon(primaryType + ".png");
			}
			if (icon == null) {
				icon = getIcon("nt_folder.png");
			}
			return icon;
		} catch (Throwable t) {
			log.debug(t);
		}
		return previous;
	}

	private Image getIcon(String name) {
		InputStream is = getClass().getResourceAsStream("/de/mhus/hair/jack/ui/img/" + name);
		if (is==null) return null;
		return new Image(Display.getCurrent(),is);
	}
	
}
