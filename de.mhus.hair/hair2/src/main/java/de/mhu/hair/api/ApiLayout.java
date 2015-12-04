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

package de.mhu.hair.api;

import javax.swing.JComponent;

import org.w3c.dom.Element;

public interface ApiLayout extends Api {

	public void setComponent(JComponent panel, Element attr) throws Exception;

	public void setComponent(JComponent panel, Element attr, Listener listener)
			throws Exception;

	public void setComponent(JComponent panel, Element attr, String title,
			Listener listener) throws Exception;

	public JComponent getMainComponent();

	public static interface Listener {

		void windowClosed(Object source);

	}

	public void removeComponent(JComponent panel);

	public boolean isComponent(JComponent panel);

}
