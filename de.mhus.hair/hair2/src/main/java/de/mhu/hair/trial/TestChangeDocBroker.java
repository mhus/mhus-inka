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

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfDocbaseMap;
import com.documentum.fc.client.IDfTypedObject;

public class TestChangeDocBroker {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		IDfClientX clientx = new DfClientX();
		try {

			IDfClient client = clientx.getLocalClient();

			// Show default Config
			IDfDocbaseMap myMap = client.getDocbaseMap();
			System.out
					.println("Docbases for Docbroker: " + myMap.getHostName());
			System.out.println("Total number of Docbases: "
					+ myMap.getDocbaseCount());

			// Change Broker
			IDfTypedObject config = client.getClientConfig();
			config.removeAll("backup_host");
			config.removeAll("backup_port");
			config.removeAll("backup_service");
			config.removeAll("backup_timeout");
			config.setString("primary_host", "10.1.1.11");
			config.setInt("primary_port", 1489);

			// Show new Config
			myMap = client.getDocbaseMap();
			System.out
					.println("Docbases for Docbroker: " + myMap.getHostName());
			System.out.println("Total number of Docbases: "
					+ myMap.getDocbaseCount());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
