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

/*
 *  Copyright (C) 2002-2004 Mike Hummel
 *
 *  This library is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published
 *  by the Free Software Foundation; either version 2.1 of the License, or
 *  (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package de.mhu.hair.main;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

public class LocalArgsParser {

	public static final String DEFAULT = "";
	public static final String DEFAULT_SEPARATOR = ",";

	private Hashtable values = new Hashtable();
	private Hashtable usage;

	private static LocalArgsParser singleton = null;

	/**
	 * Initialize args singleton. If already initialized it will return false.
	 * 
	 * @param args
	 * @return
	 */
	public synchronized static boolean initialize(String[] args) {
		if (singleton != null) {
			// 
			return false;
		}
		singleton = new LocalArgsParser(args);
		return true;
	}

	public static LocalArgsParser getInstance() {
		return singleton;
	}

	public LocalArgsParser(String[] args) {
		this(args, null);
	}

	public LocalArgsParser(String[] args, String[] pUsage) {

		String name = DEFAULT;
		if (pUsage != null) {
			usage = new Hashtable();
			for (int i = 0; i < pUsage.length; i += 3)
				usage.put(pUsage[i], pUsage[i + 1]);
		}
		// parse
		boolean printUsage = false;

		for (int i = 0; i < args.length; i++) {

			String n = args[i];

			if (n.startsWith("-") && n.length() > 1) {
				// it's a new key

				name = n.substring(1);
				if (name.startsWith("\"") && name.endsWith("\"")
						|| name.startsWith("'") && name.endsWith("'"))
					name = name.substring(1, name.length() - 1);

				if (values.get(name) == null) {
					values.put(name, new Vector());
				}

				if (usage != null && !usage.containsKey(name))
					printUsage = true;

			} else {
				// it's a value

				if (usage != null
						&& (usage.get(name) == null || ((String) usage
								.get(name)).length() != 0))
					printUsage = true;

				if (n.startsWith("\"") && n.endsWith("\"") || n.startsWith("'")
						&& n.endsWith("'"))
					n = n.substring(1, n.length() - 1);

				Vector v = (Vector) values.get(name);
				if (v == null) {
					// for DEFAULT !!!
					v = new Vector();
					values.put(name, v);
				}
				v.add(n);
				name = DEFAULT;
			}

		}

		if (usage != null && (printUsage || isSet("?"))) {
			// print usage
			System.out.print("Usage: ");
			if (usage.containsKey(DEFAULT))
				System.out.println(usage.get(DEFAULT));
			else
				System.out.println();

			for (int i = 0; i < pUsage.length; i += 3) {
				if (pUsage[i].length() != 0) {
					String u = pUsage[i] + ' ' + pUsage[i + 1];
					System.out.print("  -" + u + "   ");
					for (int j = u.length(); j < 20; j++)
						System.out.print(' ');
					System.out.println(pUsage[i + 2]);
				}
			}
			System.exit(0);
		}

	}

	public boolean isSet(String name) {
		return values.get(name) != null;
	}

	protected Vector getRaw(String name) {
		return (Vector) values.get(name);
	}

	public int getSize(String name) {
		if (!isSet(name))
			return 0;
		return getRaw(name).size();
	}

	public String getValue(String name, String def, int index) {
		String ret = getValue(name, index);
		return ret == null ? def : ret;
	}

	public String getValue(String name, int index) {
		String[] ret = getValues(name);
		if (ret == null)
			return null;
		if (ret.length <= index)
			return null;
		return ret[index];
	}

	public String[] getValues(String name) {
		if (!isSet(name))
			return new String[0];
		return (String[]) getRaw(name).toArray(new String[0]);
	}

	public String getValueSubset(String name, String def, int index, int subset) {
		return getValueSubset(name, def, index, subset, DEFAULT_SEPARATOR);
	}

	public String getValueSubset(String name, int index, int subset) {
		return getValueSubset(name, index, subset, DEFAULT_SEPARATOR);
	}

	public String getValueSubset(String name, String def, int index,
			int subset, String separator) {
		String ret = getValueSubset(name, index, subset, separator);
		return ret == null ? def : ret;
	}

	public String getValueSubset(String name, int index, int subset,
			String separator) {
		String ret = getValue(name, index);
		if (ret == null)
			return null;
		String[] parts = ret.split(separator);
		if (parts.length <= subset)
			return null;
		return parts[subset];
	}

	public Iterator getKeys() {
		return values.keySet().iterator();
	}

}
