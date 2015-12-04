package de.mhus.hair.db;

import java.io.InputStream;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import de.mhus.cap.core.ui.ICaoImageProvider;
import de.mhus.lib.adb.DbManager;
import de.mhus.lib.adb.DbSchema;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.db.AdbApplication;
import de.mhus.lib.cao.db.AdbData;
import de.mhus.lib.cao.db.CaoAdbObject;
import de.mhus.lib.cao.db.CaoAdbSchema;
import de.mhus.lib.logging.MLog;

public class ImageProvider implements ICaoImageProvider {

	@Override
	public Image getImage(CaoElement element, Image previous) {
		try {
			if (element instanceof AdbData) {
				Object obj = ((AdbData)element).getAdbObject();
				if (obj instanceof CaoAdbObject) {
					String iconName = ((CaoAdbObject)obj).getCaoIconName();
					if (iconName != null) {
						DbManager manager = ((AdbApplication)element.getApplication()).getManager();
						DbSchema schema = manager.getSchema();
						if (schema instanceof CaoAdbSchema) {
							InputStream is = ((CaoAdbSchema)schema).getCaoIconStream(iconName);
							if (is != null) {
								Image icon = new Image(Display.getCurrent(),is);
								return icon;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			MLog.w(e);
		}
		return previous;
	}

}
