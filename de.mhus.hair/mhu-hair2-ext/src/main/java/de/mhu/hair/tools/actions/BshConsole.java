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

package de.mhu.hair.tools.actions;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.Timer;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import bsh.EvalError;
import bsh.ExternalNameSpace;
import bsh.Interpreter;
import bsh.NameSource;
import bsh.classpath.BshClassPath;
import bsh.util.JConsole;
import bsh.util.NameCompletion;
import bsh.util.NameCompletionTable;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfPersistentObject;

import de.mhu.hair.api.ApiLayout;
import de.mhu.hair.api.ApiSystem;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.sf.scripts.BshInputArea;
import de.mhu.hair.tools.actions.ActionIfc;
import de.mhu.lib.ATimerTask;
import de.mhu.lib.swing.ASwing;
import de.mhu.lib.xml.XmlTool;
import de.mhu.res.img.LUF;

public class BshConsole implements ActionIfc {

	private Element config;
	private Properties predefined;
	private Timer timer;

	public boolean isEnabled(PluginNode node, DMConnection con,
			IDfPersistentObject[] target) throws Exception {
		return true;
	}

	public void actionPerformed(PluginNode node, DMConnection con,
			IDfPersistentObject[] target) throws Exception {

		ApiLayout layout = (ApiLayout) node.getSingleApi(ApiLayout.class);
		Console console = new Console(node, con, target);
		layout.setComponent(console, config);
	}

	public void initAction(PluginNode node, DMConnection con, Element pConfig) {
		config = pConfig;
		timer = ((ApiSystem) node.getSingleApi(ApiSystem.class)).getTimer();
		NodeList list = XmlTool.getLocalElements(config, "pre");
		predefined = new Properties();
		for (int i = 0; i < list.getLength(); i++) {
			Element e = (Element) list.item(i);
			predefined.setProperty(e.getAttribute("name"), e
					.getAttribute("expr"));
		}

		if (!"".equals(config.getAttribute("file"))) {
			Properties out = loadPredefined(new File(config
					.getAttribute("file")));
			if (out != null)
				predefined = out;
		}

	}

	public static Properties loadPredefined(File file) {
		if (file == null)
			return new Properties();
		try {
			Properties p = new Properties();

			p.load(new FileInputStream(file));
			return p;

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return null;

	}

	public void destroyAction() {
		// TODO Auto-generated method stub

	}

	private class Console extends JPanel {

		private JTextArea script;
		private Thread thread;
		private Interpreter interpreter;
		private PluginNode node;

		private Console(PluginNode pNode, DMConnection con,
				IDfPersistentObject[] target) {
			node = pNode;
			setLayout(new BorderLayout());

			JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			script = new JTextArea();
			script.setFont(LUF.CONSOLE_FONT);
			script.addKeyListener(new KeyAdapter() {

				public void keyPressed(KeyEvent e) {
					if (e.isControlDown()
							&& e.getKeyCode() == KeyEvent.VK_ENTER) {
						actionExecute();
					}
				}

			});
			JScrollPane scroller = new JScrollPane(script);

			try {
				BshInputArea console = new BshInputArea();
			
				split.setTopComponent(scroller);
				split.setBottomComponent(console);
				add(split, BorderLayout.CENTER);
	
				// Interpreter.TRACE = true;
				// Interpreter.DEBUG=true;
				interpreter = new Interpreter(console);
			
				if ( con != null ) {
					interpreter.set("con", con);
					interpreter.set("session", con.getSession());
					interpreter.set("clientx", con.clientx);
				}
				if ( target != null )
					interpreter.set("targets", target);
				interpreter.set("parent__", this);
				interpreter.eval("import "
						+ DfQuery.class.getPackage().getName() + ".*;");
				interpreter.eval("exit() { parent__.close(); }");
				NodeList list = XmlTool.getLocalElements(config, "eval");
				for (int i = 0; i < list.getLength(); i++) {
					String code = ((Element) list.item(i)).getAttribute("code");
					System.out.println("--- Code: " + code);
					try {
						interpreter.eval(code);
					} catch (Exception e) {
						System.out.println("*** BSH Config: Code: " + code);
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			thread = new Thread(interpreter);
			thread.start();

			// try {
			// new Interpreter().eval( "desktop()" );
			// } catch ( TargetError te ) {
			// te.printStackTrace();
			// System.out.println( te.getTarget() );
			// te.getTarget().printStackTrace();
			// } catch ( EvalError evalError ) {
			// System.out.println( evalError );
			// evalError.printStackTrace();
			// }

		}

		public void close() {
			thread.stop();
			ApiLayout layout = (ApiLayout) node.getSingleApi(ApiLayout.class);
			layout.removeComponent(this);
		}

		protected void actionExecute() {

			final String s = ASwing.getSelectedPart(script);
			timer.schedule(new ATimerTask() {

				public void run0() throws Exception {
					interpreter.eval(s);
				}

			}, 100);

		}

	}

	public String getTitle() {
		return config.getAttribute("title");
	}

}
