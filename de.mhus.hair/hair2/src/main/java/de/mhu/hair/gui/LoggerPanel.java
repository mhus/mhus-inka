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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import de.mhu.lib.swing.ScrollLockBoundedRangeModel;

public class LoggerPanel extends ALogger {

	private JTextArea console;
	private JScrollPane consoleScroller;
	private JProgressBar progress;
	private JPanel main;

	private JLabel label;
	private ScrollLockBoundedRangeModel boundedRangeModel;
	protected boolean truncateBuffer = true;

	public LoggerPanel(File pDir, String pPrefix) {
		super(pDir, pPrefix);
		initUI();
	}

	private void initUI() {

		main = new JPanel();

		main.setLayout(new BorderLayout());
		console = new JTextArea();
		consoleScroller = new JScrollPane(console);

		progress = new JProgressBar();
		progress.setStringPainted(true);

		label = new JLabel(" ");

		main.add(progress, BorderLayout.SOUTH);
		main.add(consoleScroller, BorderLayout.CENTER);
		main.add(label, BorderLayout.NORTH);

		boundedRangeModel = new ScrollLockBoundedRangeModel();
		if (consoleScroller.getVerticalScrollBar() == null)
			consoleScroller.setVerticalScrollBar(new JScrollBar());
		consoleScroller.getVerticalScrollBar().setModel(boundedRangeModel);

		console.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					JPopupMenu menu = new JPopupMenu();
					JMenuItem item = new JMenuItem("Clear");
					item.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent e) {
							clearOutput();
						}

					});
					menu.add(item);

					JCheckBoxMenuItem cbi = new JCheckBoxMenuItem("Scroll Lock");
					cbi.setSelected(boundedRangeModel.getScrollLock());
					cbi.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent e) {
							boundedRangeModel.setScrollLock(!boundedRangeModel
									.getScrollLock());
						}

					});
					menu.add(cbi);

					cbi = new JCheckBoxMenuItem("Truncate Buffer");
					cbi.setSelected(truncateBuffer);
					cbi.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent e) {
							truncateBuffer = !truncateBuffer;
						}

					});
					menu.add(cbi);

					cbi = new JCheckBoxMenuItem("Wrap Lines");
					cbi.setSelected(console.getLineWrap());
					cbi.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent e) {
							console.setLineWrap(!console.getLineWrap());
						}

					});
					menu.add(cbi);

					menu.show(console, e.getX(), e.getY());
				}
			}
		});

	}

	public void clearOutput() {
		synchronized (console) {
			try {
				console.getDocument().remove(0,
						console.getDocument().getLength());
			} catch (BadLocationException e1) {
			}
		}
	}

	public void start() {

		progress.setMinimum(0);
		progress.setMaximum(1);
		progress.setValue(0);

		super.start();
		label.setText(getFileLabel());

	}

	public void stop() {

		super.stop();

		progress.setMinimum(0);
		progress.setMaximum(1);
		progress.setValue(1);

	}

	protected void write(int b) {
		synchronized (console) {
			try {

				super.write(b);

				console.getDocument().insertString(
						console.getDocument().getLength(),
						new String(new char[] { (char) b }), null);
				/*
				 * if ( b == 13 || b == 10 ) { //TODO if (
				 * consoleScroller.getVerticalScrollBar() != null )
				 * consoleScroller.getVerticalScrollBar().setValue(
				 * consoleScroller.getVerticalScrollBar().getMaximum() ); }
				 */
				if (truncateBuffer
						&& console.getDocument().getLength() > 100000) {
					try {
						console.getDocument().remove(0,
								console.getDocument().getLength() - 10000);
					} catch (BadLocationException e) {
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void setMaximum(int i) {
		progress.setMaximum(i);
		progress.setString(i + " / " + progress.getMaximum());
	}

	public void setValue(int i) {
		progress.setValue(i);
		progress.setString(i + " / " + progress.getMaximum());
	}

	public JPanel getMainPanel() {
		return main;
	}

	public JTextArea getConsole() {
		return console;
	}

	public int getMaximum() {
		return progress.getMaximum();
	}

	public int getValue() {
		return progress.getValue();
	}

}
