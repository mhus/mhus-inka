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
 *  it under the terms of the GNU Lesser General Public License as published
 *  by the Free Software Foundation; either version 2.1 of the License, or
 *  (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.mhu.hair.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import de.mhu.lib.AFile;
import de.mhu.lib.ATimekeeper;

public abstract class ALogger {

	public PrintStream out = System.out;
	public PrintStream err = System.err;

	private OutputStream os = new OutputStream() {

		public void write(int b) throws IOException {
			ALogger.this.write(b);
		}

	};

	private String fileLabel;
	private File dir;
	private String prefix;
	private FileOutputStream file;
	private ATimekeeper keeper = new ATimekeeper();

	public ALogger(File pDir, String pPrefix) {

		out = new OutPrintStream(os);
		err = new ErrPrintStream(os);
		dir = pDir;
		if (dir != null)
			dir.mkdirs();
		prefix = pPrefix;

	}

	public String getFileLabel() {
		return fileLabel;
	}

	public void start() {

		fileLabel = prefix + AFile.getFileNameDateAsString() + ".log";

		if (dir != null) {
			try {
				file = new FileOutputStream(new File(dir, fileLabel));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		printMsgLine();
		printMsg("Started");
		if (file != null)
			printMsg("Log: " + fileLabel);
		printMsg();

		keeper.start();
	}

	public void printMsgLine() {
		out
				.println("### ######################################################################");
	}

	public void printMsg() {
		out.println("###");
	}

	public void printMsg(String string) {
		out.println("### " + string);
	}

	public void stop() {

		keeper.stop();
		printMsg();
		printMsg("Stopped");
		printMsg("Time: " + keeper.getCurrentTimeAsString(true));
		printMsgLine();

		if (file != null)
			try {
				file.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		file = null;

	}

	public abstract void setMaximum(int i);

	public abstract void setValue(int i);

	public abstract int getMaximum();

	public abstract int getValue();

	public void increaseValue() {
		setValue(getValue() + 1);
	}

	private class OutPrintStream extends PrintStream {

		public OutPrintStream(OutputStream os) {
			super(os, true);
		}

	}

	private class ErrPrintStream extends PrintStream {

		public ErrPrintStream(OutputStream os) {
			super(os, true);
		}

	}

	protected void write(int b) {
		try {

			if (file != null)
				file.write(b);
			System.out.write(b);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
