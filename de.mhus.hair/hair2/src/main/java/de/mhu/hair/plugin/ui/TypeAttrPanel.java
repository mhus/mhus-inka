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

package de.mhu.hair.plugin.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.documentum.fc.client.IDfType;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfValue;

import de.mhu.lib.swing.AHeadline;
import de.mhu.lib.swing.LAF;
import de.mhu.lib.swing.table.TableSortFilter;
import de.mhu.lib.swing.table.TableSortFilterPanel;
import de.mhu.res.img.LUF;

public class TypeAttrPanel extends JPanel {

	private DefaultTableModel tableModel;
	private JTable table;
	private TableSortFilter sortModel;
	private TableSortFilterPanel searchPanel;
	private AHeadline header;

	public TypeAttrPanel(IDfType pType) {
		initUI();
		searchPanel.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				sortModel.setPattern(searchPanel.getText());
			}

		});

		try {
			show(pType);
		} catch (DfException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void show(IDfType type) throws DfException {

		tableModel.setRowCount(0);
		header.setText("");

		if (type == null)
			return;

		header.setText(type.getName());

		tableModel.addRow(new Object[] { "OWNER", type.getString("owner"),
				null, null });
		tableModel.addRow(new Object[] { "SUPER", type.getString("super_name"),
				null, null });

		// sort
		TreeMap sort = new TreeMap();

		for (int i = 0; i < type.getValueCount("attr_name"); i++) {
			// IDfValue attr = type.getRepeatingValue( "attr_name", i );
			sort.put(type.getRepeatingString("attr_name", i), new Integer(i));
		}

		for (Iterator i = sort.entrySet().iterator(); i.hasNext();) {

			Map.Entry entry = (Map.Entry) i.next();
			String name = (String) entry.getKey();
			int index = ((Integer) entry.getValue()).intValue();

			String t = "?";
			switch (type.getRepeatingInt("attr_type", index)) {
			case IDfValue.DF_BOOLEAN:
				t = "bool";
				break;
			case IDfValue.DF_DOUBLE:
				t = "double";
				break;
			case IDfValue.DF_ID:
				t = "ID";
				break;
			case IDfValue.DF_INTEGER:
				t = "int";
				break;
			case IDfValue.DF_STRING:
				t = "str";
				break;
			case IDfValue.DF_TIME:
				t = "time";
				break;
			case IDfValue.DF_UNDEFINED:
				t = "undef";
				break;
			}

			tableModel.addRow(new Object[] {
					name,
					t,
					new Boolean(type.getRepeatingBoolean("attr_repeating",
							index)),
					new Integer(type.getRepeatingInt("attr_length", index)) });

		}

	}

	private void initUI() {
		tableModel = new DefaultTableModel() {
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};

		sortModel = new TableSortFilter(tableModel);

		table = new JTable(sortModel);
		LAF.toListTable(table);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

		sortModel.setTableHeader(table.getTableHeader());

		tableModel.setDataVector(new Object[][] {}, new Object[] { "Name",
				"Type", "Rep", "Length" });

		JScrollPane scroller = new JScrollPane(table);

		setLayout(new BorderLayout());

		header = LAF.createHeadline(null);

		searchPanel = new TableSortFilterPanel(LUF.SEARCH_ICON);
		header.getToolBar().add(searchPanel);
		header.setVisibleToolBar(true);

		add(scroller, BorderLayout.CENTER);
		add(header, BorderLayout.NORTH);
	}

	public AHeadline getLabel() {
		return header;
	}

	public void clear() {
		header.setText(" ");
		tableModel.setNumRows(0);
	}
}
