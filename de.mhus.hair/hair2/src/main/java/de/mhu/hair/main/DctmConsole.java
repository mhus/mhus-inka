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

package de.mhu.hair.main;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Timer;

import de.mhu.hair.api.ApiSystem;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.lib.AFile;
import de.mhu.lib.ArgsParser;

public class DctmConsole implements MainIfc {

	public static PrintStream out = System.out;
	public static PrintStream err = System.err;
	public static OutputStream file = null;

	private static OutputStream osErr = new OutputStream() {

		public void write(int b) throws IOException {
			writeErr(b);
		}

	};

	private static OutputStream osOut = new OutputStream() {

		public void write(int b) throws IOException {
			writeOut(b);
		}

	};

	protected static void writeOut(int b) {
		try {

			if (file != null)
				file.write(b);
			out.write(b);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected static void writeErr(int b) {
		try {

			if (file != null)
				file.write(b);
			err.write(b);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static class OutPrintStream extends PrintStream {

		public OutPrintStream(OutputStream os) {
			super(os, true);
		}

	}

	private static class ErrPrintStream extends PrintStream {

		public ErrPrintStream(OutputStream os) {
			super(os, true);
		}

	}

	public void startMain() {

		ArgsParser args = ArgsParser.getInstance();

		if (args.getValues(ArgsParser.DEFAULT).length < 1) {
			System.out
					.println("Usage: java -jar Hair.jar -st cons <plugin config> [options...]");
			System.out
					.println("       plugin config: name of the config in plugins or, if start with . the path to the directory. Use always / instead of \\ !!");
			System.out
					.println("       options:       will be used in the plugin config as parameters like $xyz$ depends on the plugin config)");
			return;
		}
		// create logfile
		try {
			String logTime = AFile.getFileNameDateAsString();
			if (args.isSet("log")) {
				String logName = args.getValues(ArgsParser.DEFAULT)[0]
						.replaceAll("/", "_").replaceAll("\\\\", "_");
				logName = "cons_" + logName + "_" + logTime + ".log";
				file = new FileOutputStream((args.getSize("log") == 0 ? "log/"
						: args.getValue("log", 0) + '/')
						+ logName);
				System.setErr(new ErrPrintStream(osErr));
				System.setOut(new OutPrintStream(osOut));
				System.out.println("### Log: " + logName);
			}
			System.out.println("### Plugin: "
					+ args.getValues(ArgsParser.DEFAULT)[0]);
			System.out.println("### Time: " + logTime);

		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return;
		}

		for (Enumeration i = System.getProperties().keys(); i.hasMoreElements();) {
			String key = (String) i.nextElement();
			System.out.println("### Property: " + key + "="
					+ System.getProperty(key));
		}

		Hashtable pluginArgs = new Hashtable();
		for (Iterator i = args.getKeys(); i.hasNext();) {
			String key = (String) i.next();
			String[] values = args.getValues(key);
			if (values.length == 1) {
				System.out.println("### Arg: " + key + "=" + values[0]);
				pluginArgs.put(key, values[0]);
			} else if (values.length == 0) {
				System.out.println("### Arg: " + key + "=1");
				pluginArgs.put(key, "1");
			}

		}

		try {
			PluginNode root = new PluginNode(
					args.getValues(ArgsParser.DEFAULT)[0]);

			// PluginNode root = new PluginNode( "tree" );
			root.addApi(ApiSystem.class, new ApiSystem() {

				private Timer timer = new Timer();

				public void exit(int ret) {
					System.exit(ret);
				}

				public Timer getTimer() {
					return timer;
				}

			});
			root.start(pluginArgs, false);
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

}
