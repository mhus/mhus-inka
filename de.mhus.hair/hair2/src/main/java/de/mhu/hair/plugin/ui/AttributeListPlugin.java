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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.Timer;

import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfValue;
import com.documentum.fc.common.IDfValue;

import de.mhu.hair.api.ApiObjectView;
import de.mhu.hair.api.ApiLayout;
import de.mhu.hair.api.ApiObjectChanged;
import de.mhu.hair.api.ApiObjectHotSelect;
import de.mhu.hair.api.ApiSystem;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.Plugin;
import de.mhu.hair.plugin.PluginConfig;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.plugin.ui.AttributeListPanel.Listener;
import de.mhu.hair.tools.ObjectChangedTool;
import de.mhu.hair.tools.ObjectWorkerTool;
import de.mhu.res.img.LUF;

public class AttributeListPlugin extends AbstractHotSelectMenu implements
		Plugin, ApiObjectHotSelect, Listener {

	private AttributeListPanel table;
	private DMConnection con;
	private Timer timer;
	private PluginNode node;
	private IDfPersistentObject obj;

	public void initPlugin(PluginNode pNode, PluginConfig pConfig)
			throws Exception {

		con = (DMConnection) pNode.getSingleApi(DMConnection.class);
		timer = ((ApiSystem) pNode.getSingleApi(ApiSystem.class)).getTimer();
		node = pNode;

		initUI();

		((ApiLayout) pNode.getSingleApi(ApiLayout.class)).setComponent(this,
				pConfig.getNode());

		if (pConfig.getNode().getAttribute("listen").indexOf("_obj.hotselect_") >= 0) {
			initHotSelectMenu(pNode, pConfig, this);
			pNode.addApi(ApiObjectHotSelect.class, this);
		}
		if (pConfig.getNode().getAttribute("listen").indexOf("_obj.last_") >= 0) {
			IDfPersistentObject obj = con.getPersistentObject(pConfig
					.getProperty("objid"));
			showObj(con, obj);
		}

	}

	private void actionMenu(int x, int y, String type, IDfValue value,
			JComponent src) throws DfException {
		if (!type.equals("ID"))
			return;

		IDfPersistentObject obj = con.getPersistentObject(value.asId());

		ObjectWorkerTool.showMenu(node, con, new IDfPersistentObject[] { obj },
				src, x, y);

	}

	/*
	 * private void actionSelect() throws DfException {
	 * 
	 * if ( !((String)table.getValueAt( table.getSelectedRow(), 1 )).equals(
	 * "ID" ) ) return;
	 * 
	 * final IDfPersistentObject obj = con.getPersistentObject(
	 * ((IDfValue)table.getValueAt( table.getSelectedRow(), 3 )).asId() );
	 * 
	 * final ApiObjectSelect[] list = (ApiObjectSelect[])node.getApi(
	 * ApiObjectSelect.class );
	 * 
	 * if ( list == null ) return;
	 * 
	 * timer.schedule( new TimerTask() {
	 * 
	 * public void run() { for ( int i = 0; i < list.length; i++ ) try {
	 * list[i].apiObjectSelected( con, obj ); } catch ( Throwable t ) {
	 * t.printStackTrace(); } }
	 * 
	 * },1);
	 * 
	 * }
	 */

	private void initUI() {
		setLayout(new BorderLayout());

		table = new AttributeListPanel(con, true, this);
		table.getTable().getColumnModel().getColumn(3).setCellEditor(
				new MyTableCellEditor());

		//JScrollPane scroll = new JScrollPane(table);
		add(table, BorderLayout.CENTER);

		// tableModel.setColumnCount( 4 );
		// tableModel.setDataVector( new Object[][] {}, new Object[] { "Name",
		// "Type", "Nr", "Value" } );

	}

	public void apiObjectHotSelected0(DMConnection pCon,
			IDfPersistentObject[] pParents, IDfPersistentObject[] obj)
			throws DfException {
		if (obj == null || obj.length != 1)
			table.clear();
		else
			showObj(pCon, obj[0]);
		table.getLabel().setIcon(LUF.DOT_GREEN);
	}

	private void showObj(DMConnection pCon, IDfPersistentObject pObj)
			throws DfException {
		obj = pObj;
		table.show(con, pObj);

		/*
		 * con = pCon;
		 * 
		 * label.setText( obj.getString( "r_object_id" ) );
		 * tableModel.setNumRows( 0 );
		 * 
		 * for ( int i = 0; i < obj.getAttrCount(); i++ ) { IDfAttr attr =
		 * obj.getAttr( i ); IDfValue attrValue = obj.getValue(attr.getName());
		 * 
		 * String name = attr.getName(); String type = "?"; switch(
		 * attrValue.getDataType() ) { case IDfValue.DF_BOOLEAN: type =
		 * "bool";break; case IDfValue.DF_DOUBLE: type = "double";break; case
		 * IDfValue.DF_ID: type = "ID";break; case IDfValue.DF_INTEGER: type =
		 * "int";break; case IDfValue.DF_STRING: type = "str";break; case
		 * IDfValue.DF_TIME: type = "time";break; case IDfValue.DF_UNDEFINED:
		 * type = "undef";break; }
		 * 
		 * if ( obj.isAttrRepeating( attr.getName() ) ) { for ( int j = 0; j <
		 * obj.getValueCount( attr.getName() ); j++ ) { IDfValue value =
		 * obj.getRepeatingValue( attr.getName(), j ); //if ( j == 0 )
		 * tableModel.addRow( new Object[] { name,type, new Integer( j ), value
		 * } ); //else // tableModel.addRow( new Object[] { "", "", new Integer(
		 * j ), value } ); } } else tableModel.addRow( new Object[] { name,type,
		 * null, attrValue } );
		 * 
		 * }
		 */
	}

	public void destroyPlugin() throws Exception {
		destroyHotSelectMenu();
	}

	public void clickedEvent(DMConnection con, String name, String type,
			Integer nr, IDfValue value, MouseEvent e) {

		
		
		if (e.getButton() == MouseEvent.BUTTON3) {

			try {
				actionMenu(e.getX(), e.getY(), type, value, (JComponent) e
						.getSource());
			} catch (DfException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}
	}

	public void newRepeatingValue(String name, DfValue dfValue) {
		try {
			obj = con.getPersistentObject(obj.getObjectId());
			obj.appendValue(name, dfValue);
			obj.save();
			table.refreshObject();
			ObjectChangedTool.objectsChanged(node,
					ApiObjectChanged.CHANGED,
					new IDfPersistentObject[] { obj });
		} catch (DfException e) {
			e.printStackTrace();
			return;
		}
		
	}
	
	public boolean valueChangedEvent(String name, int nr, IDfValue oldValue,
			IDfValue newValue) {
		if (nr < 0) {
			try {

				try {
					obj = con.getPersistentObject(obj.getObjectId());
					ObjectChangedTool.objectsChanged(node,
							ApiObjectChanged.CHANGED,
							new IDfPersistentObject[] { obj });
				} catch (DfException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}

				obj.setValue(name, newValue);
				obj.save();

			} catch (DfException e) {
				System.out.println( "*** " + name + " " + nr + " " + newValue );
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, "Can't change value: " + e );
				return false;
			}
		} else {
			try {
				obj.setRepeatingValue(name, nr, newValue);
				obj.save();
			} catch (DfException e) {
				System.out.println( "*** " + name + " " + nr + " " + newValue );
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, "Can't change value: " + e );
				return false;
			}
		}

		try {
			obj = con.getPersistentObject(obj.getObjectId());
			ObjectChangedTool.objectsChanged(node, ApiObjectChanged.CHANGED,
					new IDfPersistentObject[] { obj });
		} catch (DfException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		return true;

	}

	private class MyTableCellEditor extends AbstractCellEditor implements
			TableCellEditor {

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
			if ( ! (value instanceof IDfValue ) )
				this.value = new DfValue(value.toString());
			else
				this.value = (IDfValue) value;
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

	public void apiObjectDepricated0() {
		table.getLabel().setIcon(LUF.DOT_RED);
	}

	public void newRepeatingEntry(String name, int i) {
		try {
			if ( i < 0 )
				obj.appendString(name, "");
			else
				obj.insertString(name, i, "");
			obj.save();
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}

	public void removeRepeatingEntry(String name, int i) {
		try {
			obj.remove(name, i);
			obj.save();
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}

	public ApiObjectView[] getAttributeViews() {
		return (ApiObjectView[])node.getApi(ApiObjectView.class);
	}

}
