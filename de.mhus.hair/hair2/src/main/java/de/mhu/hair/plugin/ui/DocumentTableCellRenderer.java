package de.mhu.hair.plugin.ui;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.documentum.fc.client.IDfSysObject;

import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.tools.DctmTool;
import de.mhu.hair.tools.ObjectTool;
import de.mhu.lib.resources.ImageProvider;
import de.mhu.lib.swing.table.AlternatingCellRenderer;

public class DocumentTableCellRenderer extends AlternatingCellRenderer {

	private int colName = -1;
	private int colLang = -1;
	private int colContentType = -1;
	private int colType = -1;
	private int colVersion = -1;
	
	private DMList list;
	
	public int getColName() {
		return colName;
	}

	public void setColName(int colName) {
		this.colName = colName;
	}

	public int getColLang() {
		return colLang;
	}

	public void setColLang(int colLang) {
		this.colLang = colLang;
	}
	
	public int getColContentType() {
		return colContentType;
	}

	public void setColContentType(int colContentType) {
		this.colContentType = colContentType;
	}

	public int getColType() {
		return colType;
	}

	public void setColType(int colType) {
		this.colType = colType;
	}

	public int getColVersion() {
		return colVersion;
	}

	public void setColVersion(int colVersion) {
		this.colVersion = colVersion;
	}

	public DocumentTableCellRenderer(DMList list) {
		this.list = list;
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		DefaultTableCellRenderer c = (DefaultTableCellRenderer)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		
		try {
			DMConnection con = list.getConnection();
			c.setIcon(null);
			if ( column == colName ) {
				String id = list.getUserObject(row);
				//IDfSysObject uo = con.getExistingObject(id);
				//Icon icon = ObjectTool.getIcon(uo.getType().toString(), uo
				//		.getContentType(),DctmTool.isFolder(uo.getObjectId().getId()) );
				Icon icon = ObjectTool.getIcon((String)table.getValueAt(row, colType), (String)table.getValueAt(row, colContentType),DctmTool.isFolder(id) );
				c.setIcon(icon);
			} else
			if ( column == colLang ) {
				String lc = (String)value;
				String flag = null;
				
				//TODO generic mapping lang -> flag
				if ( lc.equals( "de_DE") )
					flag = "germany";
				else
				if ( lc.equals( "en_US" ) )
					flag = "usa";
				
				if ( flag != null ) {
					Icon icon = ImageProvider.getInstance().getIcon("mhu:flag:" + flag);
					c.setIcon(icon);
				}
			} else
			if ( column == colVersion ) {
				String v = (String)value;
				Icon icon = null;
				if ( v.indexOf( "Active" ) >= 0 )
					icon = ImageProvider.getInstance().getIcon("mhu:mono:notes_accept:color red");
				else
				if ( v.indexOf( "Approved" ) >= 0 )
					icon = ImageProvider.getInstance().getIcon("mhu:mono:notes_add:color red");
				else
				if ( v.indexOf( "Staging" ) >= 0 )
					icon = ImageProvider.getInstance().getIcon("mhu:mono:notes_up:color yellow");
				else
				if ( v.indexOf( "_NEW_" ) >= 0 )
					icon = ImageProvider.getInstance().getIcon("mhu:mono:notes_settings:color green");
				else
				if ( v.indexOf( "WIP" ) >= 0 )
					icon = ImageProvider.getInstance().getIcon("mhu:mono:notes_add:color green");
				else
				if ( v.indexOf( "Expired" ) >= 0 )
					icon = ImageProvider.getInstance().getIcon("mhu:mono:notes_cancel:color grey");
				
				c.setIcon(icon);
					
			}
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		return c;
	}
}
