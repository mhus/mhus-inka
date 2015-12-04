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

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.common.DfException;

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
import de.mhu.lib.ArgsParser;
import de.mhu.lib.io.CSVReader;
import de.mhu.lib.xml.XmlTool;

public class ScriptConsole implements Plugin {

	private boolean foldersOnly;
	private boolean documentOnly;
	private boolean singleSelect;
	private boolean ignoreTargets;
	private Element config;
	private DocumentBuilder builder;
	private String scriptPath;
	private DMConnection con;
	private ALogger logger;
	private PluginConfig pluginConfig;
	private PluginNode node;
	private ScriptIfc script;
	private Hashtable parameters;
	private boolean serviceWillQuit;
	private boolean testMode;

	public void initPlugin(PluginNode pNode, PluginConfig pConfig)
			throws Exception {

		DocumentBuilderFactory dbf = null;

		try {
			dbf = DocumentBuilderFactory.newInstance();
			node = pNode;
			con = (DMConnection) pNode.getSingleApi(DMConnection.class);
			pluginConfig = pConfig;

			testMode = ArgsParser.getInstance() != null
					&& ArgsParser.getInstance().isSet("hair_cons_test");
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		try {
			builder = dbf.newDocumentBuilder();
			scriptPath = pConfig.getNode().getAttribute("script");

			initScript();
			initLogger();
			logger.start();
			if (con != null)
				logger.printMsg("Docbase: " + con.getDocbaseName());
			if (con != null)
				logger.printMsg("User: " + con.getUserName());
			for (Iterator i = parameters.keySet().iterator(); i.hasNext();) {
				String key = (String) i.next();
				if (key.toLowerCase().indexOf("pass") >= 0) {
					logger.printMsg(key + "=[xxx]");
				} else {
					logger.printMsg(key + "=" + parameters.get(key));
				}
			}
			logger.printMsg();

			if (!testMode)
				script.initialize(node, con, logger);

			if (pConfig.getNode().getAttribute("id").length() != 0) {
				String[] ids = pConfig.getNode().getAttribute("id").split(",");
				IDfPersistentObject[] targets = new IDfPersistentObject[ids.length];
				for (int i = 0; i < ids.length; i++)
					targets[i] = con.getPersistentObject(ids[i]);
				execute(targets);
			} else if ((pConfig.getNode().getAttribute("dql").length() != 0 && pConfig
					.getNode().getAttribute("cachefile").length() != 0)
					|| (pConfig.getNode().getAttribute("datfile").length() != 0)
					|| (pConfig.getNode().getAttribute("csvfile").length() != 0 && pConfig
							.getNode().getAttribute("cachefile").length() != 0)

			) {

				int allCnt = 0;
				String cacheFile = null;

				if (pConfig.getNode().getAttribute("dql").length() != 0) {
					cacheFile = pConfig.getNode().getAttribute("cachefile");

					FileOutputStream fos = new FileOutputStream(cacheFile);
					ObjectOutputStream oos = new ObjectOutputStream(fos);

					logger.printMsg("DQL: "
							+ pConfig.getNode().getAttribute("dql"));

					IDfQuery query = con.createQuery(pConfig.getNode()
							.getAttribute("dql"));
					Vector out = new Vector();
					IDfCollection res = query.execute(con.getSession(),
							IDfQuery.READ_QUERY);

					while (res.next()) {
						oos.writeUTF(res.getString("r_object_id"));
						allCnt++;
					}
					oos.close();
					fos.close();
					res.close();

				} else if (pConfig.getNode().getAttribute("datfile").length() != 0) {
					cacheFile = pConfig.getNode().getAttribute("datfile");

					FileInputStream fis = new FileInputStream(cacheFile);
					ObjectInputStream ois = new ObjectInputStream(fis);
					try {
						while (true) {
							String id = ois.readUTF();
							allCnt++;
						}
					} catch (EOFException eofe) {
					}
					ois.close();
					fis.close();
				} else if (pConfig.getNode().getAttribute("csvfile").length() != 0) {
					FileReader fr = new FileReader(pConfig.getNode()
							.getAttribute("csvfile"));
					CSVReader reader = new CSVReader(fr, ';', '"', true, false);
					reader.getAllFieldsInLine();

					cacheFile = pConfig.getNode().getAttribute("cachefile");

					FileOutputStream fos = new FileOutputStream(cacheFile);
					ObjectOutputStream oos = new ObjectOutputStream(fos);

					try {
						while (true) {
							String[] line = reader.getAllFieldsInLine();
							if (line.length > 0)
								oos.writeUTF(line[0]);
							allCnt++;
						}
					} catch (EOFException eofe) {
					}
					oos.close();
					fos.close();
					reader.close();
					fr.close();

				}

				logger.printMsg("Cached " + allCnt + " object ids");

				int pack = 100;
				if (pConfig.getNode().getAttribute("pack").length() != 0)
					pack = Integer.parseInt(pConfig.getNode().getAttribute(
							"pack"));

				FileInputStream fis = new FileInputStream(cacheFile);
				ObjectInputStream ois = new ObjectInputStream(fis);

				int cnt = 0;
				int packCnt = 0;
				IDfPersistentObject[] targets = new IDfPersistentObject[pack];
				try {
					while (ois.available() >= 0) {
						String id = ois.readUTF();
						try {
							targets[cnt] = con.getPersistentObject(id);
							cnt++;
							if (cnt >= targets.length) {
								logger.printMsgLine();
								logger.printMsg("Sending Pack: "
										+ (packCnt * pack + 1) + " to "
										+ (packCnt * pack + pack) + " / " + allCnt);
								logger.printMsgLine();
								execute(targets);
								cnt = 0;
								packCnt++;
							}
						} catch (DfException dfe) {
							logger.err.println("--- END fetch Object: " + dfe);
						}

					}
				} catch (EOFException eofe) {
					logger.out.println("*** " + eofe);
				}

				if (cnt != 0) {
					// send the rest
					IDfPersistentObject[] old = targets;
					targets = new IDfPersistentObject[cnt];
					System.arraycopy(old, 0, targets, 0, cnt);
					old = null;
					logger.printMsgLine();
					logger.printMsg("Sending Last Pack: "
							+ (packCnt * pack + 1) + " to "
							+ (packCnt * pack + cnt) + " / " + allCnt);
					logger.printMsgLine();
					execute(targets);
				}

				ois.close();
				fis.close();

			} else if (pConfig.getNode().getAttribute("dql").length() != 0) {

				logger
						.printMsg("DQL: "
								+ pConfig.getNode().getAttribute("dql"));

				IDfQuery query = con.createQuery(pConfig.getNode()
						.getAttribute("dql"));
				Vector out = new Vector();
				IDfCollection res = query.execute(con.getSession(),
						IDfQuery.READ_QUERY);
				while (res.next())
					out.add(con.getPersistentObject(res.getId("r_object_id")));
				res.close();
				IDfPersistentObject[] targets = (IDfPersistentObject[]) out
						.toArray(new IDfPersistentObject[0]);
				execute(targets);
			} else if (pConfig.getNode().getAttribute("server").length() != 0) {

				ATimekeeper timekeeper = new ATimekeeper();
				timekeeper.start();
				serviceWillQuit = false;

				AThread ioThread = new AThread() {
					public void run() {
						System.out.println("IO A");

						try {
							File f = new File("stop.srv");
							if (f.exists() && f.isFile()) {
								System.out
										.println("::::::: Delete \"stop.srv\"");
								if (!f.delete()) {
									System.out.println(":::::::");
									System.out.println("::::::: Can't delete");
									System.out.println(":::::::");
									return;
								}
							}

							System.out.println(":::::::");
							System.out
									.println("::::::: Switch into Client mode");
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
				ObjectInputStream ois = new ObjectInputStream(socket
						.getInputStream());
				ObjectOutputStream oos = new ObjectOutputStream(socket
						.getOutputStream());

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

					String cmd = ois.readUTF();
					ois.readObject(); // dummy
					logger.printMsg("CMD: " + cmd);
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
							logger.printMsg("OK");
							oos.writeUTF("ok");
							oos.writeObject(ScriptServer.DUMMY);
						} catch (Throwable e) {
							logger.printMsg("ERROR");
							e.printStackTrace(logger.err);
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

			} else {
				logger.printMsg("No Tragets");
				execute(new IDfPersistentObject[0]);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			if (!testMode)
				script.destroy(node, con, logger);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
		config = dom.getDocumentElement();
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

		// read parameters from config
		Hashtable values = new Hashtable();
		NodeList valuesList = XmlTool.getLocalElements(XmlTool
				.getElementByPath(pluginConfig.getNode(), "parameters"),
				"parameter");
		for (int i = 0; i < valuesList.getLength(); i++) {
			Element v = (Element) valuesList.item(i);
			values.put(v.getAttribute("name"), v.getAttribute("value"));
		}

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
				String value = (String) values.get(item.getAttribute("name"));
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
				} else if (name.equals("textarea")) {
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
				} else if (name.equals("files")) {
					String[] parts = new String[0];
					if (value != null && value.length() != 0)
						parts = value.split("\\|");
					script.getClass().getMethod(method,
							new Class[] { String[].class }).invoke(script,
							new Object[] { parts });
				} else {
					System.out.println( "*** Field type unknown: " + name );
				}
			}
		}

	}

	private void execute(IDfPersistentObject[] targets) throws Exception {

		if (!testMode)
			script.execute(node, con, targets, logger);

	}

	private void initLogger() {
		String logPrefix = config.getAttribute("log_prefix");
		String log = pluginConfig.getNode().getAttribute("log");
		if ("off".equals(log))
			log = null;
		else if ("".equals(log) || "on".equals(log))
			log = "log";

		if (logPrefix.equals(""))
			logPrefix = AFile.getFileSuffix(config.getAttribute("class"));

		logger = new ALogger(log == null ? null : new File(log), logPrefix) {

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
		// TODO Auto-generated method stub

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
