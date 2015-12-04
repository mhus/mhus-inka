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

package de.mhu.hair.plugin.log4j;

import java.io.OutputStreamWriter;

import org.apache.log4j.Layout;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.helpers.LogLog;

import de.mhu.hair.plugin.ui.OutputPlugin;
import de.mhu.lib.swing.OutputArea;

public class GuiAppender extends WriterAppender {

	protected String target = "log4j";
	protected OutputArea output = null;

	/**
	 * The default constructor does nothing.
	 */
	public GuiAppender() {
	}

	public GuiAppender(Layout layout) {
		this(layout, "log4j");
	}

	public GuiAppender(Layout layout, String target) {
		this.layout = layout;

		if (output == null)
			output = OutputPlugin.getInstance().getOutput(target);

		setWriter(new OutputStreamWriter(output.getOutputStream()));

	}

	/**
	 * Sets the value of the <b>Target</b> option. Recognized values are
	 * "System.out" and "System.err". Any other value will be ignored.
	 * */
	public void setTarget(String value) {
		String v = value.trim();

		target = v;

	}

	/**
	 * Returns the current value of the <b>Target</b> property. The default
	 * value of the option is "System.out".
	 * 
	 * See also {@link #setTarget}.
	 * */
	public String getTarget() {
		return target;
	}

	void targetWarn(String val) {
		LogLog.warn("[" + val + "] should be System.out or System.err.");
		LogLog.warn("Using previously set target, System.out by default.");
	}

	public void activateOptions() {

		if (output == null)
			output = OutputPlugin.getInstance().getOutput(target);

		setWriter(new OutputStreamWriter(output.getOutputStream()));

	}

	/**
	 * This method overrides the parent {@link WriterAppender#closeWriter}
	 * implementation to do nothing because the console stream is not ours to
	 * close.
	 * */
	protected final void closeWriter() {
	}

}
