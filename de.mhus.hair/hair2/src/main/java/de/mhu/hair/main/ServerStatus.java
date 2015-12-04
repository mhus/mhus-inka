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

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import de.mhu.hair.sf.ScriptServer;

public class ServerStatus {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String host = "localhost";
		int port = 1234;

		if (args.length > 1) {
			host = args[0];
			port = Integer.parseInt(args[1]);
		}

		try {
			Socket socket = new Socket(host, port);
			ObjectInputStream ois = new ObjectInputStream(socket
					.getInputStream());
			ObjectOutputStream oos = new ObjectOutputStream(socket
					.getOutputStream());

			oos.writeUTF("state");
			oos.writeObject(ScriptServer.DUMMY);

			/*
			 * oos.writeLong( Runtime.getRuntime().freeMemory() );
			 * oos.writeLong( Runtime.getRuntime().totalMemory() );
			 * oos.writeInt( connections.size() ); oos.writeInt(
			 * AThread.poolWorkingSize() ); oos.writeInt( AThread.poolSize() );
			 * oos.writeInt( AThreadDaemon.poolWorkingSize() ); oos.writeInt(
			 * AThreadDaemon.poolSize() ); oos.writeLong(
			 * timekeeper.getCurrentTime() ); oos.writeObject( DUMMY );
			 */

			long fMem = ois.readLong();
			long tMem = ois.readLong();
			int conSize = ois.readInt();
			int tws = ois.readInt();
			int ts = ois.readInt();
			int tdws = ois.readInt();
			int tds = ois.readInt();
			long time = ois.readLong();
			ois.readObject();

			System.out.println("Memory : " + (fMem / 1024 / 1024 + 1) + " / "
					+ (tMem / 1024 / 1024 + 1) + " MB");
			System.out.println("Connections: " + conSize);
			System.out.println("Threads: " + tws + " / " + ts);
			System.out.println("Deamons: " + tdws + " / " + tds);
			System.out.println("Runtime: " + (time / 1000 / 60) + " min");

			int updateCnt = 0;
			int clientCnt = 0;
			int finishCnt = 0;
			long runTimeCnt = 0;
			while (ois.readUTF().equals("cstate")) {
				clientCnt++;
				ois.readObject();
				System.out
						.println("----------------------------------------------------");
				System.out.println("Client : " + clientCnt + ": "
						+ ois.readUTF());
				int cnt = ois.readInt();
				long lastTime = ois.readLong();
				fMem = ois.readLong();
				tMem = ois.readLong();
				tws = ois.readInt();
				ts = ois.readInt();
				tdws = ois.readInt();
				tds = ois.readInt();
				time = ois.readLong();
				ois.readObject();

				updateCnt += cnt;
				if (cnt > 1)
					finishCnt += cnt - 1;
				runTimeCnt += time;

				System.out.println("Updates: " + cnt + " "
						+ (lastTime / 1000 / 60) + " min ago");
				System.out.println("Memory : " + (fMem / 1024 / 1024 + 1)
						+ " / " + (tMem / 1024 / 1024 + 1) + " MB");
				System.out.println("Threads: " + tws + " / " + ts);
				System.out.println("Deamons: " + tdws + " / " + tds);
				System.out.println("Runtime: " + (time / 1000 / 60) + " min");
				if (cnt > 1)
					System.out.println("Speed  : "
							+ (time / (cnt - 1) / 1000 / 60) + " min/Update");
			}
			ois.readObject();
			socket.close();

			System.out
					.println("----------------------------------------------------");
			System.out.println("Clients : " + (clientCnt - 1));
			System.out.println("Updates : " + updateCnt);
			System.out.println("Finished: " + finishCnt);
			System.out
					.println("Runntime: " + (runTimeCnt / 1000 / 60) + " min");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
