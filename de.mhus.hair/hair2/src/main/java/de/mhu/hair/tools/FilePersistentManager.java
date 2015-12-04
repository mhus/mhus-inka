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

package de.mhu.hair.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import de.mhu.hair.api.ApiPersistent;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.lib.log.AL;

public class FilePersistentManager implements ApiPersistent {

	private static AL log = new AL(FilePersistentManager.class);
	private File dir;
	private Timer timer = new Timer(true);
	private PersistentManager def;

	public FilePersistentManager(File pDir, PluginNode node) {
		dir = pDir;

		String profile = "none";
		DMConnection con = (DMConnection) node.getSingleApi(DMConnection.class);
		if (con != null)
			try {
				profile = con.getDocbaseName().replace(':', '_');
			} catch (Exception e) {
			}
		if ( log.t() ) log.debug("Profile: " + profile);
		File profileFile = new File(dir, "profile_" + profile + ".ini");
		if ( ! profileFile.exists() ) {
			log.warn( "Profile file " + profileFile.getAbsolutePath() + " not found, use default");
			profileFile = new File(dir,"profile.ini");
		}
		def = new PersistentManager( profileFile );
	}

	public ApiPersistent.PersistentManager getManager(String name) {
		if (name == null || name.length() == 0)
			return def;
		return new PersistentManager(new File(dir, name + ".ini"));
	}

	private class PersistentManager extends Properties implements
			ApiPersistent.PersistentManager {

		private File file;
		private boolean changed = false;

		public PersistentManager(File pFile) {
			file = pFile;
			try {
				FileInputStream fis = new FileInputStream(file);
				load(fis);
				fis.close();
			} catch (Exception e) {
				System.out.println("PersistentManager: " + e);
			}
			timer.schedule(new TimerTask() {

				public void run() {
					if (changed)  {
						if ( log.t() ) log.debug( "save " + file.getName() );
						save();
					}
				}

			}, 3000, 3000);
		}

		public void save() {
			try {
				FileOutputStream fos = new FileOutputStream(file);
				store(fos, "");
				fos.close();
				changed = false;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void setLazyChanged() {
			changed = true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Properties#setProperty(java.lang.String,
		 * java.lang.String)
		 */
		public synchronized Object setProperty(String key, String value) {
			if (value == null)
				return remove(key);
			return super.setProperty(key, value);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Properties#getProperty(java.lang.String)
		 */
		public String getProperty(String key) {
			// TODO Auto-generated method stub
			String val = super.getProperty(key);
			if (val == null && def != this)
				val = def.getProperty(key);
			return val;
		}

	}

}
