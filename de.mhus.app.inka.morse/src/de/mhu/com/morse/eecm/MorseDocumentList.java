package de.mhu.com.morse.eecm;

import java.util.Arrays;
import java.util.LinkedList;

import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.mql.Query;
import de.mhu.com.morse.obj.BtoObject;
import de.mhu.lib.eecm.model.ObjectInfo;
import de.mhu.lib.form.IConfigurable;
import de.mhu.lib.log.AL;

public class MorseDocumentList implements IMorseListTableModel {
	
	private static AL log = new AL(MorseDocumentList.class);
	private MorseConnection con;
	private ObjectInfo objInfo;
	// private McObject obj;
	private String[] possibleColumns = new String[] { "name", "v_version" };
	private int rowsPerPage = 0;
	private int pageNr = 0;
	private LinkedList<String> columns;
	private IQueryResult res;
	private boolean hasNext;

	public void setConnection(MorseConnection morseConnection) {
		con = morseConnection;
	}

	public void close() {
		if ( res != null ) {
			res.close();
			res = null;
		}
	}

	public Class getColumnType(String name) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public IConfigurable getConfigurableObject() {
		return null;
	}

	public LinkedList<String> getDefaultColumns() throws Exception {
		
		LinkedList<String> out = new LinkedList<String>();
		for ( String e : possibleColumns ) out.add(  e );
		return out;
		
	}

	public int getPageCount() throws Exception {
		return -1;
	}

	public LinkedList<String> getPossibleColumns() throws Exception {
	
		LinkedList<String> out = new LinkedList<String>();
		for ( String e : possibleColumns ) out.add(  e );
		return out;

	}

	public boolean hasNextPage() throws Exception {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean hasNextRow() throws Exception {
		if ( res == null ) return false;
		return hasNext;
	}

	public LinkedList<Object> nextRow() throws Exception {
		LinkedList<Object> out = new LinkedList<Object>();
		BtoObject obj = con.getConnecion().loadObject( con.getConnecion().getDefaultConnection(), res.getString( "m_id" ) );
		for ( String n : columns )
			out.add( obj.getObject( n ) );
		obj.close();
		hasNext = res.next();
		return out;
	}

	public void setCurrentPage(int nr) throws Exception {
		
		if ( rowsPerPage <= 0 ) nr = 0;
		pageNr = nr;
		
		if ( res != null ) {
			res.close();
			res = null;
		}
		
		String mql = "INDEX mc,current SELECT m_id,name WHERE mc_parent='" + objInfo.getId() + "'";
		if ( rowsPerPage > 0 ) {
			mql=mql+",limit=" + ( rowsPerPage ) + ",offset=" + ( pageNr*rowsPerPage );
		}
		
		res = new Query( con.getConnecion().getDefaultConnection(), mql ).execute();
		hasNext = res.next();
	}

	public void setRowsPerPage(int nr) throws Exception {
		rowsPerPage = nr;
	}

	public void setUsedColumns(LinkedList<String> cols) throws Exception {
		columns = cols;
	}

	public void seTarget(ObjectInfo object) throws Exception {
		objInfo = object;
		// obj = (McObject)con.getConnecion().loadObject( con.getConnecion().getDefaultConnection(), objInfo.getId() );
	}

}
