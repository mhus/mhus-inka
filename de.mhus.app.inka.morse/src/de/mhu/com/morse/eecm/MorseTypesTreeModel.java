package de.mhu.com.morse.eecm;

import java.util.Iterator;
import java.util.LinkedList;

import de.mhu.com.morse.client.MConnection;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.types.IType;
import de.mhu.com.morse.types.ITypes;
import de.mhu.lib.eecm.model.ITreeModel;
import de.mhu.lib.eecm.model.ObjectInfo;
import de.mhu.lib.form.AForm;
import de.mhu.lib.form.IConfigurable;
import de.mhu.lib.log.AL;

public class MorseTypesTreeModel implements IMorseTreeModel {
	
	private static AL log = new AL(MorseTypesTreeModel.class);
	private ITypes types;
	
	public void setConnection(MorseConnection morseConnection) {
		types = morseConnection.getConnecion().getTypeModel();
	}
	
	public LinkedList<ObjectInfo> getChildFolders(ObjectInfo parent)
			throws Exception {

		if ( parent.getId() == null || parent.getId().length() == 0 ) return getRoots();
		
		IType type = types.get( parent.getId() );
		if ( type == null ) 
			return null;
		
		LinkedList<ObjectInfo> out = new LinkedList<ObjectInfo>();
		for ( Iterator<IType> i = types.getTypes(); i.hasNext(); ) {
			IType t = i.next();
			if ( parent.getId().equals( t.getSuperName() ) )
				out.add( new ObjectInfo( t.getName(), t.getName() ) );
		}
		
		return out;
	}

	public LinkedList<ObjectInfo> getRoots() throws Exception {
		LinkedList<ObjectInfo> out = new LinkedList<ObjectInfo>();
		
		out.add( new ObjectInfo( "m_object", "m_object" ) );
		
		return out;
	}

	public AForm getConfigurationForm( Object source ) {
		// TODO Auto-generated method stub
		return null;
	}

	public IConfigurable getConfigurableObject( ObjectInfo folder ) {
		return null;
	}

	public ObjectInfo getObjectByPath(String path) throws Exception {
		if ( path == null || path.length() == 0 ) return null;
		if ( !path.startsWith( "/" ) ) return null;
		if ( path.equals( "/" ) ) return new ObjectInfo( "", "/" );
		
		String canonical = path.substring( 1 );
		if ( canonical.lastIndexOf( '/' ) > 0 ) {
			canonical = canonical.substring( canonical.lastIndexOf( '/' ) + 1 );
		}
//			IType t = types.get( canonical );
			return new ObjectInfo( canonical, canonical );
	}
	
}
