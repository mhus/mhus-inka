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

package de.mhu.hair.wdk;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import de.mhu.lib.swing.table.TableSortFilter;
import de.mhu.lib.swing.table.TableSortFilterPanel;

public class ListTablePanel extends JPanel {

	private DefaultTableModel tableModel;
	private JTable table;
	private JLabel label;
	private Listener listener;
	private ListContainer list;
	private TableSortFilter sorterModel;
	private TableSortFilterPanel searchPanel;

	public ListTablePanel(String pLabel, Listener pListener) {

		listener = pListener;
		initUI();
		label.setText(pLabel);

		table.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {

				if (listener != null) {
					int sel = table.getSelectedRow();
					if (sel >= 0 && sel < list.getSize()) {
						sel = sorterModel.modelIndex(sel);
						listener.clickedEvent(list.getContainer(sel), e);
					}
				}
			}
		});

		searchPanel.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				sorterModel.setPattern(searchPanel.getText());
			}

		});
	}

	private void initUI() {

		setLayout(new BorderLayout());
		tableModel = new DefaultTableModel() {
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};

		sorterModel = new TableSortFilter(tableModel);

		table = new JTable(sorterModel);
		sorterModel.setTableHeader(table.getTableHeader());

		JScrollPane scroller = new JScrollPane(table);

		add(scroller, BorderLayout.CENTER);

		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 2));

		label = new JLabel();
		panel.add(label);

		searchPanel = new TableSortFilterPanel();
		panel.add(searchPanel);

		add(panel, BorderLayout.NORTH);

	}

	public void showList(ListContainer pList) {

		list = pList;

		tableModel.setDataVector(new Object[][] {}, list.getIdentifiers());

		int len = list.getSize();
		for (int i = 0; i < len; i++) {
			tableModel.addRow(list.getRow(i));
		}

	}

	public static interface Listener {

		void clickedEvent(ListContainer.Container selected, MouseEvent e);

	}

}
