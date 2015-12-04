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

package de.mhu.hair.sf;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.documentum.fc.client.IDfPersistentObject;

import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.gui.ALogger;
import de.mhu.hair.plugin.Plugin;
import de.mhu.hair.plugin.PluginConfig;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.lib.AFile;
import de.mhu.lib.APassword;
import de.mhu.lib.AThread;
import de.mhu.lib.AThreadDaemon;
import de.mhu.lib.ATimekeeper;
import de.mhu.lib.xml.XmlTool;

public class ScriptClientPlugin implements Plugin {

	private boolean serviceWillQuit;
	private DMConnection con;
	private String scriptPath;
	private Hashtable pluginConfig;
	private DocumentBuilder builder;
	private ScriptIfc script;
	private ALogger logger;
	private Element config;
	private PluginNode node;
	private Hashtable parameters;

	public void initPlugin(PluginNode pNode, PluginConfig pConfig)
			throws Exception {

		config = pConfig.getNode();
		node = pNode;
		con = (DMConnection) pNode.getSingleApi(DMConnection.class);

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		builder = dbf.newDocumentBuilder();

		ATimekeeper timekeeper = new ATimekeeper();
		timekeeper.start();
		serviceWillQuit = false;

		AThread ioThread = new AThread() {
			public void run() {
				System.out.println("IO A");

				try {
					File f = new File("stop.srv");
					if (f.exists() && f.isFile()) {
						System.out.println("::::::: Delete \"stop.srv\"");
						if (!f.delete()) {
							System.out.println(":::::::");
							System.out.println("::::::: Can't delete");
							System.out.println(":::::::");
							return;
						}
					}

					System.out.println(":::::::");
					System.out.println("::::::: Switch into Client mode");
					System.out
							.println("::::::: create \"stop.srv\" file to stop the service after working task");
					System.out.println(":::::::");

					AThread.sleep(1000);
					System.out.println("IO B");

					while (!serviceWillQuit) {

						// int c = System.in.read();

						sleep(10000);

						if (f.exists() && f.isFile()) {
							System.out.println(":::::::");
							System.out
									.println("::::::: Service will Quit after next task");
							System.out.println(":::::::");
							serviceWillQuit = true;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		System.out.println("A");
		ioThread.start();
		System.out.println("B");
		AThread.sleep(1000);
		System.out.println("C");

		String host = pConfig.getNode().getAttribute("server");
		int port = 1234;
		int pos = host.indexOf(':');
		if (pos > 0) {
			port = Integer.parseInt(host.substring(pos + 1));
			host = host.substring(0, pos);
		}
		System.out.println("D: " + host + ":" + port);
		Socket socket = new Socket(host, port);
		ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
		ObjectOutputStream oos = new ObjectOutputStream(socket
				.getOutputStream());

		// get script attributes
		oos.writeUTF("config");
		oos.writeObject(ScriptServer.DUMMY);

		scriptPath = ois.readUTF();
		ois.readObject(); // dummy

		pluginConfig = new Hashtable();
		String cmd = ois.readUTF();
		ois.readObject(); // dummy
		while (cmd.equals("parameter")) {
			String name = ois.readUTF();
			String value = ois.readUTF();
			ois.readObject(); // dummy
			pluginConfig.put(name, value);
		}

		// create script
		initScript();
		initLogger();
		logger.start();
		if (con != null)
			logger.printMsg("Docbase: " + con.getSession().getDocbaseName());
		if (con != null)
			logger.printMsg("User: " + con.getUserName());
		for (Iterator i = parameters.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			logger.printMsg(key + "=" + parameters.get(key));
		}
		logger.printMsg();

		script.initialize(node, con, logger);

		// work on tasks

		while (!serviceWillQuit) {

			oos.writeUTF("cstate");
			oos.writeObject(ScriptServer.DUMMY);
			oos.writeLong(Runtime.getRuntime().freeMemory());
			oos.writeLong(Runtime.getRuntime().totalMemory());
			oos.writeInt(AThread.poolWorkingSize());
			oos.writeInt(AThread.poolSize());
			oos.writeInt(AThreadDaemon.poolWorkingSize());
			oos.writeInt(AThreadDaemon.poolSize());
			oos.writeLong(timekeeper.getCurrentTime());
			oos.writeObject(ScriptServer.DUMMY);

			oos.writeUTF("ready");
			oos.writeObject(ScriptServer.DUMMY);

			cmd = ois.readUTF();
			ois.readObject(); // dummy
			System.out.println("### CMD: " + cmd);
			if (cmd.equals("exec")) {
				try {
					String[] strIds = (String[]) ois.readObject();
					ois.readObject(); // dummy
					IDfPersistentObject[] obj = new IDfPersistentObject[strIds.length];
					for (int i = 0; i < strIds.length; i++) {
						System.out.println("ID: " + strIds[i]);
						obj[i] = con.getPersistentObject(strIds[i]);
					}
					execute(obj);
					System.out.println("### OK");
					oos.writeUTF("ok");
					oos.writeObject(ScriptServer.DUMMY);
				} catch (Throwable e) {
					System.out.println("### ERROR");
					e.printStackTrace(System.err);
					oos.writeUTF("error");
					oos.writeObject(ScriptServer.DUMMY);
					oos.writeObject(e);
					oos.writeObject(ScriptServer.DUMMY);
				}
			} else if (cmd.equals("end")) {
				break;
			}
		}

		serviceWillQuit = true;
		socket.close();

		logger.stop();

	}

	private void initScript() throws Exception {

		// Load config
		InputSource is = null;
		if (scriptPath.startsWith("res:")) {

			is = new InputSource(this.getClass().getResource(
					scriptPath.substring(4)).openStream());
		} else {
			is = new InputSource(new FileInputStream(scriptPath));
		}

		Document dom = builder.parse(is);
		Element config = dom.getDocumentElement();
		parameters = new Hashtable();

		/*
		 * String listen = scriptConfig.getAttribute( "listen" );
		 * 
		 * foldersOnly = ( listen.indexOf( "_folders.only_" ) >= 0 );
		 * documentOnly = ( listen.indexOf( "_documents.only_" ) >= 0 );
		 * singleSelect = ( listen.indexOf( "_single_" ) >= 0 ); ignoreTargets =
		 * ( listen.indexOf( "_ignore_" ) >= 0 );
		 */
		script = (ScriptIfc) this.getClass().getClassLoader().loadClass(
				config.getAttribute("class")).newInstance();

		Element pc = (Element) XmlTool.getLocalElements(config, "panel")
				.item(0);
		NodeList list = XmlTool.getLocalElements(pc);
		for (int i = 0; i < list.getLength(); i++) {

			Element item = (Element) list.item(i);
			if (!item.getAttribute("name").equals("")) {

				String name = item.getNodeName();
				String method = "set"
						+ item.getAttribute("name").substring(0, 1)
								.toUpperCase()
						+ item.getAttribute("name").substring(1);

				// get the defined value for this parameter (default is "" from
				// w3c)
				String value = (String) pluginConfig.get(item
						.getAttribute("name"));
				if (value == null)
					value = item.getAttribute("value");

				parameters.put(item.getAttribute("name"), value);
				if (name.equals("space")) {
				} else if (name.equals("title")) {
				} else if (name.equals("checkbox")) {
					script.getClass().getMethod(method,
							new Class[] { boolean.class }).invoke(script,
							new Object[] { new Boolean("1".equals(value)) });
				} else if (name.equals("input")) {
					script.getClass().getMethod(method,
							new Class[] { String.class }).invoke(script,
							new Object[] { value });
				} else if (name.equals("password")) {
					value = APassword.decode(value);
					script.getClass().getMethod(method,
							new Class[] { String.class }).invoke(script,
							new Object[] { value });
				} else if (name.equals("select")) {
					NodeList options = XmlTool.getLocalElements(item, "option");
					for (int j = 0; j < options.getLength(); j++)
						if (value.equals(((Element) options.item(j))
								.getAttribute("value"))) {
							script.getClass().getMethod(method,
									new Class[] { int.class, String.class })
									.invoke(
											script,
											new Object[] { new Integer(j),
													value });
							break;
						}
				} else if (name.equals("type")) {
					script.getClass().getMethod(method,
							new Class[] { String.class }).invoke(script,
							new Object[] { value });
				} else if (name.equals("file")) {
					script.getClass().getMethod(method,
							new Class[] { String.class }).invoke(script,
							new Object[] { value });
				} else {
					// TODO
				}
			}
		}

	}

	private void execute(IDfPersistentObject[] targets) throws Exception {

		script.execute(node, con, targets, logger);

	}

	private void initLogger() {
		String logPrefix = config.getAttribute("log_prefix");
		if (logPrefix.equals(""))
			logPrefix = AFile.getFileSuffix(config.getAttribute("class"));

		logger = new ALogger(new File("log"), logPrefix) {

			private int max;
			private int val;

			public void setMaximum(int arg0) {
				max = arg0;
			}

			public void setValue(int arg0) {
				val = arg0;
				printMsg((arg0 + 1) + " / " + (max));
			}

			public int getMaximum() {
				return max;
			}

			public int getValue() {
				return val;
			}

		};
	}

	public void destroyPlugin() throws Exception {

	}

}
