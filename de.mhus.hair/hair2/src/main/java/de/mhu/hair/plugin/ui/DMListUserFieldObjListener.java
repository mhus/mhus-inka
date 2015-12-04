package de.mhu.hair.plugin.ui;

import java.awt.event.MouseEvent;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JComponent;
import javax.swing.event.ListSelectionEvent;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfId;

import de.mhu.hair.api.ApiObjectChanged;
import de.mhu.hair.api.ApiObjectSelect;
import de.mhu.hair.api.ApiSystem;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.tools.ObjectChangedTool;
import de.mhu.hair.tools.ObjectWorkerTool;
import de.mhu.lib.Rfc1738;

public class DMListUserFieldObjListener implements DMList.Listener {


	private PluginNode node;
	private DMConnection con;
	private Timer timer;
	private boolean editable;

	public DMListUserFieldObjListener(PluginNode pNode) {
		this( pNode, false );
	}
	public DMListUserFieldObjListener(PluginNode pNode, boolean pEditable ) {
		node = pNode;
		con = (DMConnection) pNode.getSingleApi(DMConnection.class);
		timer = ((ApiSystem) pNode.getSingleApi(ApiSystem.class)).getTimer();
		editable = pEditable;
	}
	
	public boolean isCellEditable(DMList list, int row, int col) {
		return editable;
	}

	public void mouseClickedEvent(DMList list, String[] ids, MouseEvent me) {
		
		int[] rows = list.getTable().getSelectedRows();
		
		DfId[] dfids = new DfId[rows.length];
		for ( int i = 0; i < ids.length; i++ )
			dfids[i] = new DfId(list.getUserObject(rows[i]));
		
		try {
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
			IDfPersistentObject object = con.getPersistentObject( list.getUserObject(row) );
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

	protected void actionSelect(IDfId[] id) {
		try {

			final IDfPersistentObject[] obj = new IDfPersistentObject[id.length];
			for ( int i = 0; i < obj.length; i++ )
				obj[i] = con.getPersistentObject(id[i]);

			final ApiObjectSelect[] list = (ApiObjectSelect[]) node
					.getApi(ApiObjectSelect.class);

			timer.schedule(new TimerTask() {

				public void run() {
					for (int i = 0; i < list.length; i++)
						try {
							list[i].apiObjectSelected(con,
									obj);
						} catch (Throwable t) {
							t.printStackTrace();
						}
				}

			}, 1);

		} catch (DfException e) {
			e.printStackTrace();
		}
	}
	
	protected void actionMenu(DfId[] id, MouseEvent me) throws DfException {
		IDfPersistentObject[] obj = new IDfPersistentObject[id.length];
		for ( int i = 0; i < obj.length; i++)
			obj[i] = con.getPersistentObject(id[i]);
		ObjectWorkerTool.showMenu(node, con, obj,
				(JComponent) me.getSource(), me.getX(), me.getY());
	}
	public boolean isEditable(DMList list) {
		return editable;
	}
	public boolean selectionValueChanged(ListSelectionEvent e,String[] ids) {
		return false;
	}
	
}
