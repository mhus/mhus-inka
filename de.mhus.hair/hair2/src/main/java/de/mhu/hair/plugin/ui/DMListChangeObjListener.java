package de.mhu.hair.plugin.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableCellEditor;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfValue;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfValue;

import de.mhu.hair.api.ApiObjectChanged;
import de.mhu.hair.api.ApiObjectSelect;
import de.mhu.hair.api.ApiSystem;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.tools.ObjectChangedTool;
import de.mhu.hair.tools.ObjectWorkerTool;
import de.mhu.lib.Rfc1738;

public class DMListChangeObjListener implements DMList.Listener {

	private PluginNode node;
	private DMConnection con;
	private Timer timer;
	private int objectIdCellIndex;

	public int getObjectIdCellIndex() {
		return objectIdCellIndex;
	}
	public void setObjectIdCellIndex(int objectIdCellIndex) {
		this.objectIdCellIndex = objectIdCellIndex;
	}
	public DMListChangeObjListener(PluginNode pNode) {
		this( pNode, -1 );
	}
	public DMListChangeObjListener(PluginNode pNode, int pObjectIdCellIndex ) {
		objectIdCellIndex = pObjectIdCellIndex;
		node = pNode;
		con = (DMConnection) pNode.getSingleApi(DMConnection.class);
		timer = ((ApiSystem) pNode.getSingleApi(ApiSystem.class)).getTimer();
	}
	
	public boolean isCellEditable(DMList list, int row, int col) {
		return objectIdCellIndex >= 0 && col != objectIdCellIndex;
	}

	public void mouseClickedEvent(DMList list, String[] ids, MouseEvent me) {
		try {
			DfId[] dfids = new DfId[ids.length];
			for ( int i = 0; i < ids.length; i++ )
				dfids[i] = new DfId(ids[i]);
			if (me.getButton() == MouseEvent.BUTTON1
					&& me.getClickCount() > 1)
				actionSelect(dfids);
			else if (me.getButton() == MouseEvent.BUTTON3)
				actionMenu(dfids, me);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void selectedEvent(DMList list, String id) {
		
	}

	public boolean valueChangedEvent(DMList list, int row, int col, Object oldValue,
			Object newValue) {
		if ( ! isCellEditable(list, row, col) ) return false;
		DMConnection con = list.getConnection();
		try {
			IDfPersistentObject object = con.getPersistentObject( list.getTable().getValueAt(row, objectIdCellIndex ).toString() );
			String colName = list.getTable().getColumnName(col);
			if ( object.isAttrRepeating(colName) ) {
				String[] parts = newValue.toString().split("\\&");
				object.truncate(colName, 0);
				for ( String part : parts )
					object.appendString(colName, Rfc1738.decode(part));
			} else {
				object.setString(colName, newValue.toString());
			}
			object.save();
			
			ObjectChangedTool.objectsChanged(node, ApiObjectChanged.CHANGED,
					new IDfPersistentObject[] { object });

		} catch (DfException e) {
			e.printStackTrace();
			return false;
		}
				
		return true;
	}

	protected void actionSelect(IDfId[] ids) {
		try {

			final IDfPersistentObject[] objs = new IDfPersistentObject[ids.length];
			for ( int i = 0; i < ids.length; i++ )
				objs[i] = con.getPersistentObject(ids[i]);

			final ApiObjectSelect[] list = (ApiObjectSelect[]) node
					.getApi(ApiObjectSelect.class);

			timer.schedule(new TimerTask() {

				public void run() {
					for (int i = 0; i < list.length; i++)
						try {
							list[i].apiObjectSelected(con,
									objs);
						} catch (Throwable t) {
							t.printStackTrace();
						}
				}

			}, 1);

		} catch (DfException e) {
			e.printStackTrace();
		}
	}
	
	protected void actionMenu(DfId[] ids, MouseEvent me) throws DfException {
		IDfPersistentObject[] objs = new IDfPersistentObject[ids.length];
		for ( int i = 0; i < ids.length; i++ )
			objs[i] = con.getPersistentObject(ids[i]);
		ObjectWorkerTool.showMenu(node, con, objs,
				(JComponent) me.getSource(), me.getX(), me.getY());
	}
	public boolean isEditable(DMList list) {
		return objectIdCellIndex >= 0;
	}
	public boolean selectionValueChanged(ListSelectionEvent e, String[] ids) {
		return false;
	}
	
}
