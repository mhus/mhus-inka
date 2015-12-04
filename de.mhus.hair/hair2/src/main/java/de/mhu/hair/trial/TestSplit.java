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

public class TestSplit {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String toSplit = "";
		String[] parts = toSplit.split(",");
		System.out.println("Size: " + parts.length);
		for (int i = 0; i < parts.length; i++)
			System.out.println(i + ": [" + parts[i] + "]");
	}

}
