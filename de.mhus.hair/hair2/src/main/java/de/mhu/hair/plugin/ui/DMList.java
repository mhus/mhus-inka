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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfValue;
import com.documentum.fc.common.IDfValue;

import de.mhu.hair.dctm.DMConnection;
import de.mhu.lib.Rfc1738;
import de.mhu.lib.swing.AHeadline;
import de.mhu.lib.swing.ASwing;
import de.mhu.lib.swing.LAF;
import de.mhu.lib.swing.table.RowHeaderTable;
import de.mhu.lib.swing.table.TableSortFilter;
import de.mhu.lib.swing.table.TableSortFilterPanel;
import de.mhu.res.img.LUF;

public class DMList extends JPanel {

	private MyTableModel tableModel;
	private RowHeaderTable table;
	private DMConnection con;
	private Listener listener;
	private AHeadline lTitle;
	private String[] fields;
	private TableSortFilter sortModel;
	private TableSortFilterPanel searchPanel;
	private JPanel accessoryPanel;
	private int maxItems = 3000;
	private JToggleButton bEditable;
	private String userField;
	private LinkedList<String> userFieldObject;

	public DMList(String title, Listener pListener) {
		this(title, pListener, null, null);
	}

	public DMList(String title, Listener pListener, String[] pFields ) {
		this(title, pListener, pFields, null);		
	}
	
	public DMList(String title, Listener pListener, String[] pFields, String pUserField ) {
		listener = pListener;

		initUI();
		lTitle.setText(title);

		// create copy of fields
		if (pFields != null) {
			fields = new String[pFields.length];
			for (int i = 0; i < pFields.length; i++) {
				fields[i] = pFields[i];
			}
			tableModel.setDataVector(new Object[][] {}, fields);
		}
		userField = pUserField;

		table.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {

					public void valueChanged(ListSelectionEvent e) {
						
						if (listener == null) return;
						
						boolean consumed = false;
						if (!table.isCellEditable(table.getSelectedRow(), table.getSelectedColumn())) {
							int[] rows = table.getSelectedRows();
							String[] objs = new String[ rows.length ];
							for ( int i = 0; i < rows.length; i++ ) {
								Object obj = table.getValueAt(rows[i],table.getSelectedColumn());
								if (obj == null || obj instanceof TruncateObject)
									return;
								objs[i] = String.valueOf( obj );
							}
							consumed = listener.selectionValueChanged(e,objs);
						}
						consumed = listener.selectionValueChanged(e,null);
						if (consumed) return;
						
						int selectedRow = table.getSelectedRow();
						int selectedCol = table.getSelectedColumn();
						if (selectedRow < 0 || selectedCol < 0)
							return;

						Object obj = table.getValueAt(selectedRow, selectedCol);
						if (obj == null || obj instanceof TruncateObject)
							return;

						String id = (String) obj;
						if (id.length() == 16)
							listener.selectedEvent(DMList.this, id );
					}

				});

		table.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				// if ( e.getClickCount() >= 2 && e.getButton() ==
				// MouseEvent.BUTTON1 ) {
				// if ( listener != null ) listener.doubleClickedEvent( con,
				// (String)table.getValueAt( table.getSelectedRow(), 1 ) );
				// }
				if (listener != null && !table.isCellEditable(table.getSelectedRow(), table.getSelectedColumn())) {
					int[] rows = table.getSelectedRows();
					String[] objs = new String[ rows.length ];
					for ( int i = 0; i < rows.length; i++ ) {
						Object obj = table.getValueAt(rows[i],table.getSelectedColumn());
						if (obj == null || obj instanceof TruncateObject)
							return;
						objs[i] = String.valueOf( obj );
					}
					listener.mouseClickedEvent(DMList.this, objs, e);
				}
			}
		});

		searchPanel.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				sortModel.setPattern(searchPanel.getText());
			}

		});

	}

	public void show(DMConnection pCon, String nameAttrName, Vector list)
			throws DfException {
		con = pCon;
		tableModel.setRowCount(0);

		for (int j = 0; j < list.size(); j++) {
			String[] values = new String[fields.length];
			IDfPersistentObject obj = (IDfPersistentObject) list.elementAt(j);

			for (int i = 0; i < fields.length; i++)
				values[i] = obj.getString(fields[i]);
			tableModel.addRow(values);

			if (maxItems > 0 && j > maxItems) {
				System.out.println("*** Truncate DMList");
				tableModel.addRow(new Object[] { new TruncateObject() });
				break;
			}
		}

		ASwing.packTableColumns(table, 2);
		table.numberRowHeader(1);

	}

	public void show(DMConnection pCon, IDfCollection list) throws DfException {

		con = pCon;

		if (fields == null) {

			fields = new String[list.getAttrCount()];
			for (int i = 0; i < list.getAttrCount(); i++) {
				fields[i] = list.getAttr(i).getName();
			}
			tableModel.setDataVector(new Object[][] {}, fields);
			//for ( int i = 0; i < fields.length; i++ )
			//	table.getColumnModel().getColumn(i).setCellEditor(new MyTableCellEditor());
		}
		tableModel.setRowCount(0);

		int j = 0;
		if ( userField != null ) {
			userFieldObject = new LinkedList<String>();
		}
		while (list.next()) {
			
			appendRow( list );
			
			if (maxItems > 0 && j > maxItems) {
				System.out.println("*** Truncate DMList");
				tableModel.addRow(new Object[] { new TruncateObject() });
				break;
			}
			j++;
		}
		ASwing.packTableColumns(table, 2);
		table.numberRowHeader(1);
		resetEditable();
	}
	
	public void merge(IDfCollection list) throws DfException {
		if ( userField == null || fields == null || userFieldObject == null ) {
			show( con, list );
			return;
		}
		int[] used =  new int[userFieldObject.size()];
		while (list.next()) {
			String uf = list.getString(userField);
			int index = userFieldObject.indexOf(uf);
			if ( index < 0 ) 
				appendRow(list);
			else {
				mergeRow(index,list);
				if ( index < used.length ) used[index] = 1;
			}
		}
		
		for ( int i = used.length-1; i >= 0; i-- ) {
			if ( used[i] == 0 ) {
				tableModel.removeRow(i);
				userFieldObject.remove(i);
			}
		}
		
	}
	
	protected void mergeRow(int row, IDfCollection list ) throws DfException {

		for (int i = 0; i < fields.length; i++) {
			if (list.isAttrRepeating(fields[i])) {
				int cnt = list.getValueCount(fields[i]);
				StringBuffer sb = new StringBuffer();
				for (int k = 0; k < cnt; k++) {
					if (k != 0)
						sb.append('&');
					sb.append(Rfc1738.encode(list.getRepeatingString(fields[i], k)));
				}
				tableModel.mergeValueAt( sb.toString(), row, i );
			} else
				tableModel.mergeValueAt( list.getString(fields[i]), row, i );
		}
	}
	
	protected void appendRow(IDfCollection list ) throws DfException {
		String[] values = new String[fields.length];

		for (int i = 0; i < fields.length; i++) {
			if (list.isAttrRepeating(fields[i])) {
				int cnt = list.getValueCount(fields[i]);
				StringBuffer sb = new StringBuffer();
				for (int k = 0; k < cnt; k++) {
					if (k != 0)
						sb.append('&');
					sb.append(Rfc1738.encode(list.getRepeatingString(fields[i], k)));
				}
				values[i] = sb.toString();
			} else
				values[i] = list.getString(fields[i]);

		}
		tableModel.addRow(values);
		
		if ( userField != null ) {
			userFieldObject.add( list.getString( userField ) );
		}

	}

	public void setMaxItems(int max) {
		maxItems = max;
	}

	private void initUI() {

		tableModel = new MyTableModel(); {
			
		};

		sortModel = new TableSortFilter(tableModel);

		table = new RowHeaderTable(sortModel);
		// table.setCellEditor(new MyTableCellEditor());
		sortModel.setTableHeader(table.getTableHeader());
		LAF.toListTable(table);
		JScrollPane scroll = new JScrollPane(table);
		setLayout(new BorderLayout());
		add(scroll);
		setMinimumSize(new Dimension(150, 150));

		lTitle = LAF.createHeadline(null);
		lTitle.setVisibleToolBar(true);

		searchPanel = new TableSortFilterPanel(LUF.SEARCH_ICON);
		lTitle.getToolBar().add(searchPanel);

		bEditable = new JToggleButton();
		bEditable.setIcon(LUF.NOT_EDITABLE_ICON);
		bEditable.setSelectedIcon(LUF.EDITABLE_ICON);
		bEditable.setMargin(new Insets(0, 0, 0, 0));
		bEditable.setToolTipText("Editable");
		resetEditable();
		lTitle.getToolBar().add(bEditable);
		
		
		accessoryPanel = new JPanel();
		accessoryPanel.setLayout(new BorderLayout());

		lTitle.getToolBar().add(accessoryPanel, BorderLayout.EAST);

		add(lTitle, BorderLayout.NORTH);

	}
	
	public void resetEditable() {
		bEditable.setSelected(false);
		bEditable.setEnabled( fields != null || listener != null && listener.isEditable(this) );
	}

	public static interface Listener {

		public void selectedEvent(DMList list, String id);

		/**
		 * Call if the selection listener event is called.
		 * 
		 * @param e ListSelectionEvent object
		 * @param id Selected ids or null
		 * @return true if consumed.
		 */
		public boolean selectionValueChanged(ListSelectionEvent e,String[] ids);
		
		public void mouseClickedEvent(DMList list, String[] ids, MouseEvent me);

		public boolean isCellEditable(DMList list, int row, int col);
		
		public boolean valueChangedEvent(DMList list, int row, int col, Object oldValue,
				Object newValue);
		
		public boolean isEditable(DMList list);
	}

	public void setAccessory(JComponent accessory) {
		accessoryPanel.removeAll();
		accessoryPanel.add(accessory, BorderLayout.CENTER);
		lTitle.revalidate();
		lTitle.repaint();
	}

	public void clear() {
		if ( userFieldObject != null ) userFieldObject.clear();
		tableModel.setNumRows(0);
	}

	public void reset() {
		fields = null;
		resetEditable();
	}

	public AHeadline getLabel() {
		return lTitle;
	}

	private class TruncateObject {
		public String toString() {
			return "...";
		}
	}

	public JTable getTable() {
		return table;
	}

	public void updateHeader() {
		table.numberRowHeader(1);
	}
	
	public DMConnection getConnection() {
		return con;
	}
	
	public String getUserObject(int row) {
		return userFieldObject.get(sortModel.modelIndex(row));
	}

	private class MyTableCellEditor extends AbstractCellEditor implements TableCellEditor {
		
		JTextField editor = new JTextField();
		IDfValue value = null;

		public MyTableCellEditor() {
		
			editor.addKeyListener(new KeyListener() {
		
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER)
						fireEditingStopped();
				}
		
				public void keyReleased(KeyEvent e) {
					// TODO Auto-generated method stub
		
				}
		
				public void keyTyped(KeyEvent e) {
					// TODO Auto-generated method stub
		
				}
		
			});
		
			editor.addFocusListener(new FocusListener() {
		
				public void focusGained(FocusEvent e) {
				}
		
				public void focusLost(FocusEvent e) {
					fireEditingCanceled();
				}
		
			});
		}
		
		public boolean isCellEditable(EventObject e) {
			if (e instanceof MouseEvent) {
				return ((MouseEvent) e).getClickCount() >= 2;
			}
			return true;
		}

		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			editor.setText(value.toString());
			value = (IDfValue) value;
			editor.setBackground(Color.red);
			return editor;
		}

		public boolean stopCellEditing() {
			// value = new DfValue( editor.getText() );

			return true;
		}

		public Object getCellEditorValue() {
			return new DfValue(editor.getText());
		}

	}
	
	private class MyTableModel extends DefaultTableModel {
		
		public boolean isCellEditable(int row, int col) {
			if ( !bEditable.isSelected() ) return false;
			if ( listener != null )
				return listener.isCellEditable(DMList.this, row, col);
			return false;
		}
		
		public void setValueAt(Object val, int row, int col) {
			if ( !bEditable.isSelected() ) return;
			if (listener != null && val != null) {
			
				if (listener.valueChangedEvent(DMList.this, row, col, getValueAt(row, col), val))
					super.setValueAt(val, row, col);
			}
		}
		
		public void mergeValueAt(Object val, int row, int col) {
			Object orgval = super.getValueAt(row, col);
			if ( val != null || !val.equals(orgval)) {
				if (listener.valueChangedEvent(DMList.this, row, col, orgval, val))
					super.setValueAt(val, row, col);
			}
		}

	}

	public void setConnection(DMConnection con) {
		this.con = con;
	}
}
