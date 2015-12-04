package de.mhu.hair.api;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.common.DfException;

import de.mhu.hair.dctm.DMConnection;

public interface ApiObjectView extends Api {

	public boolean canWorkOn(DMConnection con, IDfPersistentObject obj);

	public void show(DMConnection con, IDfPersistentObject obj) throws DfException;

	public JComponent getComponent();

	public JMenuItem[] getMenuItems();

	public void setEditable(boolean editable);

	public String getTitle();
	
}
