package de.mhu.com.morse.eecm;

import java.io.IOException;
import java.util.LinkedList;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import de.mhu.lib.eecm.model.ITreeModel;
import de.mhu.lib.eecm.model.ObjectInfo;
import de.mhu.lib.form.AForm;
import de.mhu.lib.form.AFormModelFromXml;
import de.mhu.lib.form.IConfigurable;
import de.mhu.lib.log.AL;
import de.mhu.com.morse.client.MConnection;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.mql.Query;

public class MorseRepositoryTreeModel implements IMorseTreeModel, IConfigurable {
	
	private static AL log = new AL(MorseRepositoryTreeModel.class);
	private MConnection con;
	private String visibleRoot = "";

	public void setConnection(MorseConnection morseConnection) {
		con = morseConnection.getConnecion();		
	}
	
	public LinkedList<ObjectInfo> getChildFolders(ObjectInfo parent)  throws Exception {
		
		if ( parent.getId() == null || parent.getId().length() == 0 ) return getRoots();
		
		// IQueryResult res = new Query( con.getDefaultConnection(), "SELECT m_id, name FROM mc_folder WHERE mc_parent='"+ parent.getId() +"'" ).execute();
		IQueryResult res = new Query( con.getDefaultConnection(), "INDEX mc,folders SELECT m_id, name WHERE mc_parent='"+ parent.getId() +"'" ).execute();
		LinkedList<ObjectInfo> list = new LinkedList<ObjectInfo>();
		while ( res.next() ) {
			list.add( new ObjectInfo( res.getString( 0 ), res.getString( 1 ), true ) );
		}
		res.close();
		
		// TODO: Configure: show documents and version to show
		res = new Query( con.getDefaultConnection(), "INDEX mc,current SELECT m_id, name WHERE mc_parent='"+ parent.getId() +"'" ).execute();
		while ( res.next() ) {
			list.add( new ObjectInfo( res.getString( 0 ), res.getString( 1 ), false ) );
		}
		
		res.close();
		return list;
		
	}

	public LinkedList<ObjectInfo> getRoots() throws Exception {
		// IQueryResult res = new Query( con.getDefaultConnection(), "SELECT m_id,name FROM mc_root" ).execute();
		IQueryResult res = new Query( con.getDefaultConnection(), "INDEX mc,folders SELECT m_id,name WHERE mc_parent='" + visibleRoot + "'" ).execute();
		LinkedList<ObjectInfo> list = new LinkedList<ObjectInfo>();
		while ( res.next() ) {
			list.add( new ObjectInfo( res.getString( 0 ), res.getString( 1 ) ) );
		}
		res.close();
		return list;
	}

	public IConfigurable getConfigurableObject( ObjectInfo folder ) {
		return this;
	}
	
	public AForm getConfigurationForm( Object source ) {
		try {
			return new AFormModelFromXml( MorseRepositoryTreeModel.class );
		} catch (Exception e) {
			log.error( e );
		}
		return null;
	}
	
	public void setRoot( String in ) {
		System.out.println( "SET Root " + in );
		visibleRoot = in;
	}
	
	public String getRoot() {
		return visibleRoot;
	}
	
	public String[] getRootValues() {
		try {
			LinkedList<ObjectInfo> list = getRoots();
			String[] out = new String[ list.size() + 1 ];
			for ( int i = 0; i < out.length-1; i++ )
				out[i+1] = list.get( i ).getId();
			out[0] = "";
			return out;
		} catch (Exception e) {
			log.error( e );
		}
		return null;
	}
	
	public String[] getRootTitles() {
		try {
			LinkedList<ObjectInfo> list = getRoots();
			String[] out = new String[ list.size() + 1 ];
			for ( int i = 0; i < out.length-1; i++ )
				out[i+1] = list.get( i ).getName();
			out[0] = "[All]";
			return out;
		} catch (Exception e) {
			log.error( e );
		}
		return null;
	}
	
	public String[] getRootNls() {
		return null;
	}

	public ObjectInfo getObjectByPath(String path) throws Exception {
		if ( path == null || path.length() == 0 ) return null;
		if ( !path.startsWith( "/" ) ) return null;
		if ( path.equals( "/" ) ) return new ObjectInfo( "", "/" );
		
		IQueryResult res = new Query( con.getDefaultConnection(), 
				"INDEX mc,folders SELECT m_id,name WHERE path='" + path + "'" ).execute();
		ObjectInfo info = null;
		if ( res.next() ) {
			info = new ObjectInfo( res.getString( 0 ), res.getString( 1 ) );
		}
		res.close();
		
		// TODO configuration !
		if ( info == null ) {
			res = new Query( con.getDefaultConnection(), 
					"INDEX mc,current SELECT m_id,name WHERE path='" + path + "'" ).execute();
			if ( res.next() ) {
				info = new ObjectInfo( res.getString( 0 ), res.getString( 1 ) );
			}
			res.close();
		}
		
		return info;
	}
	
}
