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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Timer;
import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.mhu.lib.AThread;
import de.mhu.lib.AThreadDaemon;
import de.mhu.lib.ATimekeeper;
import de.mhu.lib.ATimerTask;
import de.mhu.lib.xml.XmlTool;

public class ScriptServer {

	public static final Object DUMMY = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx\n\r\r\n";
	public static int state_old_cnt;
	public static long state_old_time;
	private int port;
	private Vector connections = new Vector();
	private Listener listener;
	private LinkedList taskQueue = new LinkedList();
	private Server server;
	private ATimekeeper timekeeper;
	private Element config;
	private Timer timer = null;
	private StatusWriter statusWriter;

	public ScriptServer(int pPort, Element pConfig, Listener pListener) {
		port = pPort;
		listener = pListener;
		config = pConfig;
		statusWriter = new StatusWriter();
	}

	public void start() {
		if (server != null)
			return;
		timekeeper = new ATimekeeper();
		timekeeper.start();
		server = new Server();
		server.start();

		state_old_cnt = 0;
		state_old_time = 0;

		if (timer == null) {
			timer = new Timer(true);
			timer.schedule(statusWriter, 1000, 1000 * 60 * 10);
		}

	}

	protected void clientIsReady() {

		synchronized (this) {
			if (taskQueue.size() == 0) {
				listener.scriptReady();
				return;
			} else {
				Object[] task = (Object[]) taskQueue.removeFirst();
				addTask((Object) task[0], (String[]) task[1]);
			}
		}

	}

	public boolean addTask(Object userObject, String[] task) {
		if (task == null || task.length == 0)
			return false;
		synchronized (this) {
			for (Iterator i = connections.iterator(); i.hasNext();) {
				Client client = (Client) i.next();
				if (client.isReady()) {
					listener.scriptStartTask(client, userObject, task);
					client.setTask(userObject, task);
					return true;
				}
			}
			taskQueue.addLast(new Object[] { userObject, task });
			return false;
		}
	}

	public boolean isQueued() {
		return taskQueue.size() != 0;
	}

	public void closeClient(Client client) {
		synchronized (this) {
			connections.remove(client);
			client.isEnd = true;
		}
	}

	private class Server extends AThread {

		private ServerSocket socket;

		public void run() {

			try {
				socket = new ServerSocket(port);
				while (true) {
					Socket child = socket.accept();
					new Client(child).start();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public interface Listener {
		public void scriptTaskOk(Object userObject, String[] task);

		public void scriptTaskError(Client client, Object userObject,
				String[] task, Throwable exception);

		public void scriptReady();

		public void scriptStartTask(Client client, Object userObject,
				String[] task);
	}

	public class StatusWriter extends ATimerTask {

		public void run0() throws Exception {
			System.out.println("+++ STATUS +++ " + new Date());
			System.out.println("Free Memory  : "
					+ Runtime.getRuntime().freeMemory());
			System.out.println("Total Memory : "
					+ Runtime.getRuntime().totalMemory());
			System.out.println("Connections  : " + connections.size());
			System.out.println("Runtime      : "
					+ timekeeper.getCurrentTimeAsString(true));

			int stateCnt = state_old_cnt;
			long stateTime = state_old_time;

			synchronized (ScriptServer.this) {
				for (Iterator i = connections.iterator(); i.hasNext();) {
					Client client = (Client) i.next();

					stateCnt += client.state_cnt;
					stateTime += client.state_time;

				}
			}

			System.out.println("Total Time   : " + stateTime);
			System.out.println("Total Updates: " + stateCnt);

		}

	}

	public class Client extends AThread {

		private Socket socket;
		private boolean isReady = false;
		private boolean isEnd = false;
		private String[] task = null;
		private Object userObject = null;
		private String name;
		private long state_fMem;
		private long state_tMem;
		private int state_tws;
		private int state_ts;
		private int state_tdws;
		private int state_tds;
		private long state_time;
		private int state_cnt = 0;
		private long state_time_last;

		public Client(Socket pSocket) {
			socket = pSocket;
			name = socket.toString();
			synchronized (ScriptServer.this) {
				connections.add(this);
			}
			System.out.println(socket + ": START");

		}

		public synchronized void setTask(Object pUserObject, String[] pTask) {
			if (!isReady)
				return;
			task = pTask;
			userObject = pUserObject;
			isReady = false;
		}

		public boolean isReady() {
			return isReady;
		}

		public boolean isWorking() {
			return task != null;
		}

		public void run() {
			try {

				ObjectOutputStream oos = new ObjectOutputStream(socket
						.getOutputStream());
				ObjectInputStream ois = new ObjectInputStream(socket
						.getInputStream());
				while (!isEnd) {
					String cmd = ois.readUTF();
					ois.readObject(); // dummy
					System.out.println(socket + ": CMD: " + cmd);
					if (cmd.equals("ready")) {
						isReady = true;
						clientIsReady();
						// wait for task or end
						while (task == null && isEnd == false) {
							AThread.sleep(100);
						}
						if (isEnd) {
							oos.writeUTF("end");
							oos.writeObject(DUMMY);
							break;
						} else {
							oos.writeUTF("exec");
							oos.writeObject(DUMMY);
							oos.writeObject(task);
							oos.writeObject(DUMMY);
						}
					} else if (cmd.equals("ok")) {
						System.out.println(socket + ": Result: ok");
						listener.scriptTaskOk(userObject, task);
						task = null;
						userObject = null;
					} else if (cmd.equals("error")) {
						Throwable exception = (Throwable) ois.readObject();
						ois.readObject(); // dummy
						System.out.println(socket + ": Result: error: "
								+ exception);
						listener.scriptTaskError(this, userObject, task,
								exception);
						task = null;
						userObject = null;
					} else if (cmd.equals("state")) {
						oos.writeLong(Runtime.getRuntime().freeMemory());
						oos.writeLong(Runtime.getRuntime().totalMemory());
						oos.writeInt(connections.size());
						oos.writeInt(AThread.poolWorkingSize());
						oos.writeInt(AThread.poolSize());
						oos.writeInt(AThreadDaemon.poolWorkingSize());
						oos.writeInt(AThreadDaemon.poolSize());
						oos.writeLong(timekeeper.getCurrentTime());
						oos.writeObject(DUMMY);
						synchronized (ScriptServer.this) {
							for (Iterator i = connections.iterator(); i
									.hasNext();) {
								Client client = (Client) i.next();
								oos.writeUTF("cstate");
								oos.writeObject(DUMMY);
								Socket cs = client.socket;
								oos.writeUTF(cs == null ? "?" : cs.toString());
								oos.writeInt(client.state_cnt);
								oos.writeLong(System.currentTimeMillis()
										- client.state_time_last);
								oos.writeLong(client.state_fMem);
								oos.writeLong(client.state_tMem);
								oos.writeInt(client.state_tws);
								oos.writeInt(client.state_ts);
								oos.writeInt(client.state_tdws);
								oos.writeInt(client.state_tds);
								oos.writeLong(client.state_time);
								oos.writeObject(DUMMY);
							}
						}
						oos.writeUTF("end");
						oos.writeObject(DUMMY);

					} else if (cmd.equals("cstate")) {
						state_cnt++;
						state_time_last = System.currentTimeMillis();
						state_fMem = ois.readLong();
						state_tMem = ois.readLong();
						state_tws = ois.readInt();
						state_ts = ois.readInt();
						state_tdws = ois.readInt();
						state_tds = ois.readInt();
						state_time = ois.readLong();
						ois.readObject();
					} else if (cmd.equals("config")) {
						String script = config.getAttribute("script");
						oos.writeUTF(script);
						NodeList list = XmlTool.getLocalElements(config,
								"parameter");
						for (int i = 0; i < list.getLength(); i++) {
							Element p = (Element) list.item(i);
							oos.writeUTF("parameter");
							oos.writeObject(DUMMY);
							oos.writeUTF(p.getAttribute("name"));
							oos.writeUTF(p.getAttribute("value"));
							oos.writeObject(DUMMY);
						}
						oos.writeUTF("end");
						oos.writeObject(DUMMY);
					} else {
						System.out
								.println(socket + ": Unknown command: " + cmd);
						oos.writeUTF("end");
						oos.writeObject(DUMMY);
						break;
					}
				}

			} catch (Exception e) {
				System.out.println(socket + ": ERROR");
				e.printStackTrace();
			}
			System.out.println(socket + ": CLOSE");
			synchronized (ScriptServer.this) {
				connections.remove(this);
			}
			try {
				if (state_cnt > 0) {
					ScriptServer.state_old_cnt += state_cnt;
					ScriptServer.state_old_time += state_time;
				}
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public String getClientName() {
			return name;
		}

	}

	public void close() {
		// TODO
		if (server == null)
			return;
		try {
			server.socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		server = null;

		Client[] clients = (Client[]) connections.toArray(new Client[0]);
		for (int i = 0; i < clients.length; i++)
			clients[i].isEnd = true;

		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	public boolean allClientsReady() {
		synchronized (this) {
			for (Enumeration i = connections.elements(); i.hasMoreElements();)
				if (!((Client) i.nextElement()).isReady())
					return false;
		}
		return true;
	}

}
