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

package de.mhu.hair.trial;

import java.io.EOFException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import de.mhu.lib.io.CSVReader;
import de.mhu.lib.io.CSVWriter;

public class MergeRoleRights {

	private static Map entries = new TreeMap();

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String file1 = "D:/DLH/Content_Migration/CSV_Vorlagen/rights_old.csv";
		String file2 = "D:/DLH/Content_Migration/CSV_Vorlagen/rights_new.csv";
		String file3 = "D:/DLH/Content_Migration/CSV_Vorlagen/rights.csv";

		try {

			CSVReader f1 = new CSVReader(new FileReader(file1), ';', '"', true,
					true);
			read(f1);

			CSVReader f2 = new CSVReader(new FileReader(file2), ';', '"', true,
					true);
			read(f2);

			FileWriter writer = new FileWriter(file3);
			CSVWriter f3 = new CSVWriter(writer, 2, ';', '"', true);
			write(f3);
			writer.flush();
			writer.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void write(CSVWriter w1) throws IOException {

		w1.put("PCD_PATH");
		w1.put("TARGET_GROUP");
		w1.put("TARGET_GROUP_EXT");
		w1.put("dummy");
		w1.nl();

		for (Iterator i = entries.values().iterator(); i.hasNext();) {
			String[] line = (String[]) i.next();
			for (int j = 0; j < line.length; j++)
				w1.put(line[j]);
			w1.nl();
		}

	}

	private static void read(CSVReader r1) throws IOException {

		r1.getAllFieldsInLine();

		try {
			while (true) {
				String[] line = r1.getAllFieldsInLine();
				if (!entries.containsKey(line[0]))
					entries.put(line[0], line);
			}
		} catch (EOFException eofe) {
		}

	}

}
