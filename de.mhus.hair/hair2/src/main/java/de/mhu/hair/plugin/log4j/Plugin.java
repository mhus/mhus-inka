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

import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

import de.mhu.hair.plugin.PluginConfig;
import de.mhu.hair.plugin.PluginNode;

public class Plugin implements de.mhu.hair.plugin.Plugin {

	public void initPlugin(PluginNode pNode, PluginConfig pConfig)
			throws Exception {

		String path = pConfig.getNode().getAttribute("properties");

		if (path.length() != 0)
			PropertyConfigurator.configure(path);
		else {
			Properties properties = new Properties();
			properties.put("log4j.rootLogger", "DEBUG, A1");
			properties.put("log4j.appender.A1",
					"de.mhu.hair.plugin.log4j.GuiAppender");
			properties.put("log4j.appender.A1.layout",
					"org.apache.log4j.PatternLayout");
			String format = pConfig.getNode().getAttribute("format");
			if (format.length() != 0)
				properties.put("log4j.appender.A1.layout.ConversionPattern",
						format);
			else
				properties.put("log4j.appender.A1.layout.ConversionPattern",
						"%c %-5p %d - %m%n");
			PropertyConfigurator.configure(properties);
		}

	}

	public void destroyPlugin() throws Exception {

	}

}
