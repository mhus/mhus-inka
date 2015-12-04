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

package de.mhu.hair.plugin.cons;

import java.net.InetAddress;

import org.w3c.dom.Element;

import de.mhu.hair.plugin.Plugin;
import de.mhu.hair.plugin.PluginConfig;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.lib.net.PortForwarder;
import de.mhu.lib.net.PortForwarder.Listener;

public class PortForwardPlugin implements Plugin, Listener {

	private PortForwarder service;
	private boolean logging;

	public PortForwardPlugin() {
		super();
	}

	public void initPlugin(PluginNode pNode, PluginConfig pConfig)
			throws Exception {

		Element config = pConfig.getNode();
		logging = "1".equals(config.getAttribute("logging"));
		service = new PortForwarder(Integer.parseInt(config
				.getAttribute("src_port")), config.getAttribute("dst_host"),
				Integer.parseInt(config.getAttribute("dst_port")), this, "1"
						.equals(config.getAttribute("trace")), 60 * 1000,
				Integer.parseInt(config.getAttribute("buffer_size")));
		service.start();
	}

	public void destroyPlugin() throws Exception {
		service.stop();
	}

	public void started(int arg0) {
		if (logging)
			System.out.println(">>> Started " + arg0);
	}

	public void trace(int arg0, int arg1, byte[] arg2, int arg3) {
		if (logging) {
			if (arg1 == PortForwarder.TRACE_IN)
				System.out.println(arg0
						+ " <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
			else
				System.out.println(arg0
						+ " >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			System.out.println(new String(arg2, 0, arg3));
			System.out.println(arg0
					+ " ------------------------------------------");
		}
	}

	public void exit(int arg0) {
		if (logging)
			System.out.println(">>> Exit " + arg0);
	}

	public void error(int arg0, Exception arg1) {
		if (logging) {
			System.out.println("*** Error " + arg0 + ": " + arg1);
			arg1.printStackTrace(System.out);
		}

	}

	public void connected(int arg0, InetAddress arg1, InetAddress arg2) {
		if (logging)
			System.out.println("--- Connected " + arg0 + ": " + arg1 + " --> "
					+ arg2);
	}

}
