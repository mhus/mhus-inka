package de.mhu.com.morse.eecm;

import java.util.Iterator;
import java.util.LinkedList;

import de.mhu.com.morse.utils.MorseException;
import de.mhu.lib.eecm.model.ITreeModel;
import de.mhu.lib.eecm.model.ObjectInfo;
import de.mhu.lib.form.AForm;
import de.mhu.lib.form.IConfigurable;
import de.mhu.lib.log.AL;

public class ModelsTreeModel implements IMorseTreeModel {
	
	private static AL log = new AL(ModelsTreeModel.class);
	private MorseConnection con;

	public void setConnection(MorseConnection morseConnection) {
		con = morseConnection;
	}

	public LinkedList<ObjectInfo> getChildFolders(ObjectInfo parent)
			throws Exception {
		
		if ( parent instanceof RootFolder ) {
			((RootFolder)parent).init();
			return ((RootFolder)parent).getRootChilds();
		}
		
		if ( parent.getId() == null || parent.getId().length() == 0 ) return getRoots();
		
		WrappedFolder wrapper = (WrappedFolder)parent;
		
		return wrapper.getChilds();
	}

	public LinkedList<ObjectInfo> getRoots() throws Exception {
		LinkedList<String> list = con.getTreeModelIndex();
		LinkedList<ObjectInfo> out = new LinkedList<ObjectInfo>();
		for ( Iterator<String> i = list.iterator(); i.hasNext(); ) {
			out.add( new RootFolder( i.next() ) );
		}
		return out;
	}

	class RootFolder extends ObjectInfo {

		private IMorseTreeModel model;

		public RootFolder(String next) {
			super( next, next );
		}

		public RootFolder(IMorseTreeModel model, String next ) {
			super( next, next );
			this.model = model;
			model.setConnection( con );
		}
		
		public LinkedList<ObjectInfo> getRootChilds() throws Exception {
			
			LinkedList<ObjectInfo> list = model.getRoots();
			LinkedList<ObjectInfo> out = new LinkedList<ObjectInfo>();
			for ( Iterator<ObjectInfo> i = list.iterator(); i.hasNext(); )
				out.add( new WrappedFolder( this, i.next() ) );
			
			return out;
		}

		public synchronized void init() throws MorseException {
			if ( model != null ) return;
			model = (IMorseTreeModel)con.getTreeModel( getId() );
			model.setConnection( con );
		}
		
	}
	
	class WrappedFolder extends ObjectInfo {

		private RootFolder root;
		private ObjectInfo node;

		public WrappedFolder(RootFolder rootFolder, ObjectInfo next) {
			super( next.getId(), next.getName() );
			root = rootFolder;
			node = next;
		}

		public LinkedList<ObjectInfo> getChilds() throws Exception {
			LinkedList<ObjectInfo> list = root.model.getChildFolders( node );
			LinkedList<ObjectInfo> out = new LinkedList<ObjectInfo>();
			for ( Iterator<ObjectInfo> i = list.iterator(); i.hasNext(); )
				out.add( new WrappedFolder( root, i.next() ) );
			
			return out;

		}
		
	}

	public IConfigurable getConfigurableObject( ObjectInfo folder ) {
		
		if ( folder instanceof RootFolder ) {
			try {
				((RootFolder)folder).init();
				return ((RootFolder)folder).model.getConfigurableObject( folder );
			} catch (MorseException e) {
				// TODO Auto-generated catch block
				log.error( e );
			}
		}
		
		return null;
	}

	public ObjectInfo getObjectByPath(String path) throws Exception {
		
		if ( path == null || path.length() == 0 ) return null;
		if ( !path.startsWith( "/" ) ) return null;
		if ( path.equals( "/" ) ) return new ObjectInfo( "", "/" );
		
		int pos = path.indexOf( '/', 1 );
		
		if ( pos < 0 ) {
			return new RootFolder( path.substring( 1 ) );
		} else {
			ITreeModel tree = con.getTreeModel( path.substring( 1, pos ) );
			return new WrappedFolder( new RootFolder( (IMorseTreeModel) tree, path.substring( 1, pos ) ), tree.getObjectByPath( path.substring( pos ) ) );
		}
		
	}
	
}
