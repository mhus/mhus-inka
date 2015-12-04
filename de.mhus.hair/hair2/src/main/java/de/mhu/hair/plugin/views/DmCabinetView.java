package de.mhu.hair.plugin.views;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;

import com.documentum.fc.client.DfFolder;
import com.documentum.fc.client.DfSysObject;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.common.DfException;

import de.mhu.hair.api.ApiObjectView;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.Plugin;
import de.mhu.hair.plugin.PluginConfig;
import de.mhu.hair.plugin.PluginNode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.GroupLayout;
import javax.swing.JButton;

/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
public class DmCabinetView extends JPanel implements Plugin,ApiObjectView {
	private JLabel lName;
	private JTextField tName;
	private JButton bSave;
	private JTextField tCreated;
	private JLabel lCreated;
	private IDfPersistentObject obj;

	public void destroyPlugin() throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void initPlugin(PluginNode node, PluginConfig config)
			throws Exception {
		initGUI();
		node.addApi(ApiObjectView.class, this);
	}

	public boolean canWorkOn(DMConnection con, IDfPersistentObject obj) {
		try {
			return obj.getType().getName().equals("dm_cabinet");
		} catch (DfException e) {
			e.printStackTrace();
			return false;
		}
	}

	public JComponent getComponent() {
		return this;
	}

	public JMenuItem[] getMenuItems() {
		return null;
	}

	public String getTitle() {
		return "dm_cabinet";
	}

	public void setEditable(boolean editable) {
		tName.setEditable(editable);
		tCreated.setEditable(false);
		bSave.setEnabled(editable);
	}

	public void show(DMConnection con, IDfPersistentObject obj)
			throws DfException {
		this.obj = obj;
		tName.setText( ((IDfFolder)obj).getObjectName());
		tCreated.setText(((IDfFolder)obj).getCreationDate().toString());
	}
	
	private void initGUI() {
		try {
			{
				GroupLayout thisLayout = new GroupLayout((JComponent)this);
				this.setLayout(thisLayout);
				this.setPreferredSize(new java.awt.Dimension(479, 265));
				{
					lName = new JLabel();
					lName.setText("Name");
				}
				{
					bSave = new JButton();
					bSave.setText("Save Changes");
					bSave.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent evt) {
							bSaveActionPerformed(evt);
						}
					});
				}
				{
					tName = new JTextField();
					tName.setText("---");
				}
				{
					lCreated = new JLabel();
					lCreated.setText("Created");
				}
				{
					tCreated = new JTextField();
					tCreated.setText("---");
				}
				thisLayout.setVerticalGroup(thisLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(thisLayout.createParallelGroup()
					    .addGroup(GroupLayout.Alignment.LEADING, thisLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					        .addComponent(tName, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					        .addComponent(lName, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
					    .addGroup(thisLayout.createSequentialGroup()
					        .addGap(19)
					        .addGroup(thisLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					            .addComponent(tCreated, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					            .addComponent(lCreated, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))))
					.addGap(0, 179, Short.MAX_VALUE)
					.addComponent(bSave, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
					.addContainerGap());
				thisLayout.setHorizontalGroup(thisLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(thisLayout.createParallelGroup()
					    .addComponent(lCreated, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 155, GroupLayout.PREFERRED_SIZE)
					    .addComponent(lName, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 155, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
					.addGroup(thisLayout.createParallelGroup()
					    .addComponent(tCreated, GroupLayout.Alignment.LEADING, 0, 288, Short.MAX_VALUE)
					    .addComponent(tName, GroupLayout.Alignment.LEADING, 0, 288, Short.MAX_VALUE)
					    .addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
					        .addGap(0, 164, Short.MAX_VALUE)
					        .addComponent(bSave, GroupLayout.PREFERRED_SIZE, 124, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap());
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void bSaveActionPerformed(ActionEvent evt) {
		try {
			((IDfFolder)obj).setObjectName(tName.getText());
			obj.save();
		} catch ( Exception e) {
			e.printStackTrace();
		}
	}

}
