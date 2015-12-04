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
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.table.DefaultTableModel;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfValue;
import com.documentum.fc.common.IDfAttr;
import com.documentum.fc.common.IDfValue;

import de.mhu.hair.api.Api;
import de.mhu.hair.api.ApiObjectView;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.tools.ObjectTool;
import de.mhu.lib.swing.AHeadline;
import de.mhu.lib.swing.LAF;
import de.mhu.lib.swing.PopupButton;
import de.mhu.lib.swing.PopupListener;
import de.mhu.lib.swing.table.TableSortFilter;
import de.mhu.lib.swing.table.TableSortFilterPanel;
import de.mhu.res.img.LUF;

public class AttributeListPanel extends JPanel {

	private DefaultTableModel tableModel;
	private JTable table;
	private Listener listener;
	private DMConnection con;
	private boolean editable;
	private TableSortFilter sortModel;
	private AHeadline label;
	private TableSortFilterPanel searchPanel;
	private JTextField id;
	private JButton bRefresh;
	private IDfPersistentObject obj;
	private PopupButton bMenu;
	private JToggleButton bEditable;
	private JPanel contentPanel;
	private ApiObjectView internalView;
	private LinkedList<ApiObjectView> listOfViews;
	private JScrollPane internalViewPanel;
	protected ApiObjectView currentView;
	private PopupButton bViews;
	protected LinkedList<String> preferedViews = new LinkedList<String>();

	public AttributeListPanel(DMConnection pCon, boolean pEditable,
			Listener pListener) {
		con = pCon;
		listener = pListener;
		editable = pEditable;

		initUI();

		internalView = new MyView();

		searchPanel.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				sortModel.setPattern(searchPanel.getText());
			}

		});

	}

	private void initUI() {
		setLayout(new BorderLayout());
		tableModel = new DefaultTableModel() {
			public boolean isCellEditable(int row, int col) {
				// return ( col == 2); // only for value
				return (bEditable.isSelected() && col == 3);
			}

			public void setValueAt(Object val, int row, int col) {
				if (!bEditable.isSelected())
					return;

				if (listener != null && val != null) {
					int nr = -1;

					if ("---".equals(getValueAt(row, 2))) {
						listener.newRepeatingValue((String) table.getValueAt(
								row, 0), new DfValue(val.toString()));
						return;
					}

					Integer nrInt = (Integer) getValueAt(row, 2);
					if (nrInt != null)
						nr = nrInt.intValue();

					if (listener.valueChangedEvent((String) getValueAt(row, 0),
							nr, (IDfValue) getValueAt(row, 3), (IDfValue) val))
						super.setValueAt(val, row, col);
				}
			}
		};

		sortModel = new TableSortFilter(tableModel);

		table = new JTable(sortModel);
		sortModel.setTableHeader(table.getTableHeader());

		internalViewPanel = new JScrollPane(table);
		LAF.toListTable(table);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

		contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout());
		contentPanel.add(internalViewPanel, BorderLayout.CENTER);

		add(contentPanel, BorderLayout.CENTER);

		label = LAF.createHeadline(null);
		label.setVisibleToolBar(true);

		id = new JTextField();
		id.setEditable(false);
		label.getToolBar().add(id);
		id.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (listener != null)
					listener.clickedEvent(con, "r_object_id", "ID", 0,
							new com.documentum.fc.common.DfValue(id.getText()),
							e);
			}

		});

		searchPanel = new TableSortFilterPanel(LUF.SEARCH_ICON);
		label.getToolBar().add(searchPanel);

		bEditable = new JToggleButton();
		if (editable) {
			bEditable.setIcon(LUF.NOT_EDITABLE_ICON);
			bEditable.setSelectedIcon(LUF.EDITABLE_ICON);
			bEditable.setMargin(new Insets(0, 0, 0, 0));
			bEditable.setToolTipText("Editable");
			label.getToolBar().add(bEditable);
		}
		bEditable.setSelected(false);
		bEditable.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (currentView == null ) return;
				currentView.setEditable(bEditable.isSelected());
			}
			
		});

		bMenu = new PopupButton(LUF.MENU_ICON, "Options") {
			protected void actionMenu(ActionEvent e) {
				if (currentView == null)
					return;
				JPopupMenu menu = new JPopupMenu();
				JMenuItem[] items = currentView.getMenuItems();
				if (items == null)
					return;

				for (JMenuItem item : items) {
					for (ActionListener al : item.getActionListeners()) {
						if (al instanceof PopupListener) {
							try {
								((PopupListener) al).beforeVisible(item);
							} catch (Throwable t) {
								t.printStackTrace();
							}
						}
					}
					menu.add(item);
				}

				menu.show(this, 0, this.getHeight());
			}
		};
		
		bViews = new PopupButton(LUF.VIEWS_ICON, "Views") {
			protected void actionMenu(ActionEvent e) {
				if ( listOfViews == null || listOfViews.size() == 0 ) return;
				JPopupMenu menu = new JPopupMenu();
				for ( final ApiObjectView view : listOfViews ) {
					JMenuItem item = new JMenuItem(view.getTitle());
					item.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							setView(view);
							try {
								view.show(con, obj);
								preferedViews.remove(obj.getType().getName() + ":" + view.getTitle());
								preferedViews.addFirst(obj.getType().getName() + ":" + view.getTitle());
								if ( preferedViews.size() > 100 ) preferedViews.removeLast();
							} catch (DfException e1) {
								e1.printStackTrace();
								setView(null);
							}
						}
					});
					menu.add(item);
				}
				menu.show(this, 0, this.getHeight());
				
			}
		};

		table.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {

				if (listener != null) {
					int sel = table.getSelectedRow();
					if (sel < 0)
						return;

					// sel = sortModel.modelIndex( sel );
					Object v = table.getValueAt(sel, 3);

					Integer nr = null;
					Object nrObj = table.getValueAt(sel, 2);
					if (nrObj != null && nrObj instanceof Integer)
						nr = (Integer) nrObj;
					else if (nrObj != null && nrObj.toString().equals("---"))
						nr = -1;

					listener.clickedEvent(con, (String) table
							.getValueAt(sel, 0), (String) table.getValueAt(sel,
							1), nr, !(v instanceof IDfValue) ? new DfValue(v
							.toString()) : (IDfValue) v, e);
				}
			}
		});

		label.getToolBar().add(bMenu);
		label.getToolBar().add(bViews);

		bRefresh = new JButton();
		bRefresh.setIcon(LUF.RELOAD_ICON);
		bRefresh.setMargin(new Insets(0, 0, 0, 0));
		bRefresh.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				refreshObject();
			}

		});
		label.getToolBar().add(bRefresh);

		add(label, BorderLayout.NORTH);

		tableModel.setColumnCount(4);
		tableModel.setDataVector(new Object[][] {}, new Object[] { "Name",
				"Type", "Nr", "Value" });

	}

	protected void setView(ApiObjectView view) {
		if ( view == null ) {
			contentPanel.removeAll();
			contentPanel.revalidate();
			contentPanel.repaint();
			currentView = null;
			return;
		}
		JComponent comp = view.getComponent();
		if (contentPanel.getComponentCount() == 0
				|| contentPanel.getComponent(0) != comp) {
			contentPanel.removeAll();
			contentPanel.add(comp, BorderLayout.CENTER);
			contentPanel.revalidate();
			contentPanel.repaint();
		}
		currentView = view;
		currentView.setEditable(bEditable.isSelected());
	}

	protected void refreshObject() {
		if (obj == null) {
			setView(internalView);
			return;
		}
		try {

			System.out.println("--- Refresh cache for: " + obj.getObjectId());

			obj.revert();
			obj.fetch(null);

		} catch (DfException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			show(null, obj);
		} catch (DfException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public AHeadline getLabel() {
		return label;
	}

	public void clear() {
		tableModel.setNumRows(0);
		id.setText("");
		label.setText("");
		obj = null;
	}

	public void show(DMConnection con, IDfPersistentObject pObj)
			throws DfException {

		obj = pObj;

		tableModel.setNumRows(0);

		id.setText(obj.getObjectId().getId());
		label.setText(ObjectTool.getName(obj) + " [" + obj.getType().getName()
				+ "]");

		listOfViews = new LinkedList<ApiObjectView>();
		listOfViews.add(internalView);

		ApiObjectView[] views = listener.getAttributeViews();

		if (views != null) {
			for (ApiObjectView view : views) {
				if (view != null && view.canWorkOn(con, obj)) {
					listOfViews.add(view);
				}
			}

		}

		ApiObjectView selected = null;
		if (listOfViews.size() > 1 ) {
			for (String def : preferedViews ) {
				if ( selected != null )
					break;
				for ( ApiObjectView view : listOfViews ) {
					if ( def.equals( obj.getType().getName() + ":" + view.getTitle() )) {
						selected = view;
						break;
					}
				}
			}
		}
		
		if ( selected == null ) selected = listOfViews.getLast();
		setView(selected);
		selected.show(con, pObj);

	}

	public static interface Listener {

		public void clickedEvent(DMConnection con, String name, String type,
				Integer nr, IDfValue value, MouseEvent e);

		/**
		 * Return a list of attribute views or null if not supported the
		 * application will check if the view is responsible for this object.
		 * 
		 * @return List of existing views
		 */
		public ApiObjectView[] getAttributeViews();

		public void removeRepeatingEntry(String name, int nr);

		public void newRepeatingEntry(String name, int nr);

		public void newRepeatingValue(String name, DfValue dfValue);

		public boolean valueChangedEvent(String name, int nr,
				IDfValue oldValue, IDfValue newValue);

	}

	class MyView implements ApiObjectView {

		JMenuItem[] menuItems = new JMenuItem[2];

		public MyView() {
			menuItems[0] = new JMenuItem("---");
			menuItems[1] = new JMenuItem("Remove repeating value");

			menuItems[0].addActionListener(new PopupListener() {

				public void beforeVisible(AbstractButton c) {
					int sel = table.getSelectedRow();
					if (sel >= 0 && bEditable.isSelected()) {
						Object nrObj = table.getValueAt(sel, 2);
						if (nrObj != null) {
							c.setEnabled(true);
							c.setText("Insert repeating: "
									+ table.getValueAt(sel, 0));
							return;
						}
					}
					c.setEnabled(false);
					c.setText("Insert repeating value");
				}

				public void actionPerformed(ActionEvent e) {
					int sel = table.getSelectedRow();
					if (sel >= 0 && bEditable.isSelected()) {
						Object nrObj = table.getValueAt(sel, 2);
						if (nrObj != null) {
							listener
									.newRepeatingEntry(
											(String) table.getValueAt(sel, 0),
											nrObj instanceof Integer ? ((Integer) nrObj)
													.intValue()
													: -1);
							refreshObject();
						}
					}
				}

			});

			menuItems[1].addActionListener(new PopupListener() {

				public void beforeVisible(AbstractButton c) {
					int sel = table.getSelectedRow();
					if (sel >= 0 && bEditable.isSelected()) {
						Object nrObj = table.getValueAt(sel, 2);
						if (nrObj != null && nrObj instanceof Integer) {
							c.setEnabled(true);
							c.setText("Remove repeating: "
									+ table.getValueAt(sel, 0) + "[" + nrObj
									+ "]");
							return;
						}
					}
					c.setEnabled(false);
					c.setText("Remove repeating value");
				}

				public void actionPerformed(ActionEvent e) {
					int sel = table.getSelectedRow();
					if (sel >= 0 && bEditable.isSelected()) {
						Object nrObj = table.getValueAt(sel, 2);
						if (nrObj != null && nrObj instanceof Integer) {
							listener.removeRepeatingEntry((String) table
									.getValueAt(sel, 0), ((Integer) nrObj)
									.intValue());
							refreshObject();
						}
					}
				}
			});
		}

		public boolean canWorkOn(DMConnection con, IDfPersistentObject obj) {
			return true;

		}

		public JComponent getComponent() {
			return internalViewPanel;
		}

		public void show(DMConnection con, IDfPersistentObject obj)
				throws DfException {
			for (int i = 0; i < obj.getAttrCount(); i++) {
				IDfAttr attr = obj.getAttr(i);
				IDfValue attrValue = obj.getValue(attr.getName());

				String name = attr.getName();
				String type = "?";
				switch (attrValue.getDataType()) {
				case IDfValue.DF_BOOLEAN:
					type = "bool";
					break;
				case IDfValue.DF_DOUBLE:
					type = "double";
					break;
				case IDfValue.DF_ID:
					type = "ID";
					break;
				case IDfValue.DF_INTEGER:
					type = "int";
					break;
				case IDfValue.DF_STRING:
					type = "str";
					break;
				case IDfValue.DF_TIME:
					type = "time";
					break;
				case IDfValue.DF_UNDEFINED:
					type = "undef";
					break;
				}

				if (obj.isAttrRepeating(attr.getName())) {
					boolean writen = false;
					for (int j = 0; j < obj.getValueCount(attr.getName()); j++) {
						writen = true;
						IDfValue value = obj.getRepeatingValue(attr.getName(),
								j);
						// if ( j == 0 )
						tableModel.addRow(new Object[] { name, type,
								new Integer(j), value });
						// else
						// tableModel.addRow( new Object[] { "", "", new
						// Integer( j
						// ), value } );
					}
					if (!writen)
						tableModel
								.addRow(new Object[] { name, type, "---", "" });

				} else
					tableModel.addRow(new Object[] { name, type, null,
							attrValue });

			}
		}

		public JMenuItem[] getMenuItems() {
			return menuItems;
		}

		public void setEditable(boolean editable) {
			
		}

		public String getTitle() {
			return "All attributes";
		}

	}

	public JTable getTable() {
		return table;
	}
}
