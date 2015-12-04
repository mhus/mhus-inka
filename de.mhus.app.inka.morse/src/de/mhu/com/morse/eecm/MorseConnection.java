package de.mhu.com.morse.eecm;

import java.util.LinkedList;

import de.mhu.lib.eecm.model.IEcmConnection;
import de.mhu.lib.eecm.model.IListTableModel;
import de.mhu.lib.eecm.model.ITreeModel;
import de.mhu.com.morse.client.IAuthHandler;
import de.mhu.com.morse.client.MConnection;
import de.mhu.com.morse.client.MConnectionTcp;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.mql.Query;
import de.mhu.com.morse.utils.MorseException;

public class MorseConnection implements IEcmConnection {

	private static final String FUNCTION_TREE_PREFIX = "model.tree.";
	private static final String FUNCTION_LIST_PREFIX = "model.list.";
	private static final String DEFAULT_TREE_MODEL = FUNCTION_TREE_PREFIX + "mc.folders";
	private IAuthHandler authHandler;
	private String host;
	private int port;
	private String service;
	private String loginName;
	private MConnection con;
	private String documentVersion;
	private Object nmode;

	void setAuthHandler(IAuthHandler authHandler) {
		this.authHandler = authHandler;
	}
	
	void setHost(String host) {
		this.host = host;
	}
	
	void setPort(int port) {
		this.port = port;
	}
	
	void setService(String service) {
		this.service = service;
	}

	void setLoginName(String loginName) {
		this.loginName = loginName;
	}
	
	void connect() throws Exception {
		con = new MConnectionTcp( host, port );
		// con.setEntranceAddress( host, port );
		con.setService( service );
		con.setAuth( loginName, authHandler );
		con.connect();
	}
	
	public void disconnect() {
		if ( ! isConnected() ) return;
		con.close();
		con = null;
	}

	/*
	public LinkedList<DocumentInfo> getChildDocuments(FolderInfo parent)  throws Exception {
		IQueryResult res = new Query( con.getDefaultConnection(), "SELECT m_id, name FROM mc_document " + ( documentVersion == null ? "" : "(" + documentVersion + ")" ) + " WHERE mc_parent='"+ parent.getId() +"' ORDER BY name" ).execute();
		LinkedList<DocumentInfo> list = new LinkedList<DocumentInfo>();
		while ( res.next() ) {
			list.add( new DocumentInfo( res.getString( 0 ), res.getString( 1 ) ) );
		}
		res.close();
		return list;
		
	}
	
	*/
	public boolean isConnected() {
		return con != null && con.isConnected();
	}

	public MConnection getConnecion() {
		return con;
	}

	public ITreeModel getDefaultTreeModel() throws MorseException {
		ModelsTreeModel model = new ModelsTreeModel();
		model.setConnection( this );
		return model;
		// return getTreeModel( DEFAULT_MODEL );
	}

	public ITreeModel getTreeModel(String name) throws MorseException {
		IMorseTreeModel model = (IMorseTreeModel)con.getDefaultConnection().getServer().loadFunction( con.getDefaultConnection(), FUNCTION_TREE_PREFIX + name );
		model.setConnection( this );
		return model;
	}

	public LinkedList<String> getTreeModelIndex() throws MorseException {
		IQueryResult res = new Query( con.getDefaultConnection(), "SELECT name FROM m_function WHERE name like '" + FUNCTION_TREE_PREFIX + "%' @sys" ).execute();
		LinkedList<String> list = new LinkedList<String>();
		int len = FUNCTION_TREE_PREFIX.length();
		while ( res.next() ) {
			String name = res.getString( 0 ).substring( len );
			list.add( name );
		}
		res.close();
		return list;
	}

	public IListTableModel getListTable(String name) throws Exception {
		// IMorseListTableModel model = (IMorseListTableModel)con.getDefaultConnection().getServer().loadFunction( con.getDefaultConnection(), FUNCTION_LIST_PREFIX + name );
		IMorseListTableModel model = new MorseDocumentList();
		model.setConnection( this );
		return model;
	}

	public LinkedList<String> getListTableModelIndex() throws Exception {
		IQueryResult res = new Query( con.getDefaultConnection(), 
				"SELECT name FROM m_function WHERE name like '" + FUNCTION_LIST_PREFIX + "%' @sys" ).execute();
		LinkedList<String> list = new LinkedList<String>();
		int len = FUNCTION_LIST_PREFIX.length();
		while ( res.next() ) {
			String name = res.getString( 0 ).substring( len );
			list.add( name );
		}
		res.close();
		return list;
	}

}
