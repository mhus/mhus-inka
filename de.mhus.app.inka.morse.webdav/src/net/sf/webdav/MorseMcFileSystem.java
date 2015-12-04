package net.sf.webdav;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;

import javax.servlet.http.HttpServletRequest;

import de.mhu.lib.ArgsParser;
import de.mhu.lib.config.Config;
import de.mhu.lib.config.ConfigManager;
import de.mhu.lib.config.FileConfigLoader;
import de.mhu.lib.eecm.model.EcmManager;
import de.mhu.lib.eecm.model.IEcmConnection;
import de.mhu.lib.eecm.model.ITreeModel;
import de.mhu.lib.eecm.model.ObjectInfo;
import de.mhu.lib.jmx.AJmxManager;
import de.mhu.lib.log.ALUtilities;

public class MorseMcFileSystem implements IWebdavStorage {

	private Config config;

	public MorseMcFileSystem() {

		try {
			Class.forName( "de.mhu.com.morse.eecm.MorseDriver" );
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		ArgsParser.initialize( new String[0] );
		//ConfigManager.initialize( new FileConfigLoader( "C:/mhu/morse_100/src/core/morse/config" ), "config.properties" );
		ConfigManager.initialize( );
		ALUtilities.configure();
		AJmxManager.start();
		config = ConfigManager.getConfig( "client" );
	}
	
	public void begin(HttpServletRequest req, Hashtable parameters)
			throws Exception {
		
		/*
		String url = "morse://localhost:6666/service";
		String user = "root";
		String pass = "nein";
		*/
		String url = config.getProperty( "url" );
		String user = config.getProperty( "user" );
		String pass = config.getProperty( "pass" );
		
		synchronized ( req.getSession() ) {
			IEcmConnection con = (IEcmConnection)req.getSession().getAttribute( "morse_con" );
			if ( con == null ) {
				con = EcmManager.connect( url, user, pass );
				req.getSession().setAttribute( "morse_con", con );
			}
			
			ITreeModel model = (ITreeModel)req.getSession().getAttribute( "morse_tree");
			if ( model == null ) {
				model = con.getDefaultTreeModel();
				req.getSession().setAttribute( "morse_tree", model );
			}
		}	
	}

	public void commit(HttpServletRequest req) throws IOException {
		// TODO Auto-generated method stub
		
	}

	public void createFolder(HttpServletRequest req, String folderUri)
			throws IOException {
		// TODO Auto-generated method stub
		
	}

	public void createResource(HttpServletRequest req, String resourceUri)
			throws IOException {
		// TODO Auto-generated method stub
		
	}

	public String[] getChildrenNames(HttpServletRequest req, String folderUri)
			throws Exception {
		
		ITreeModel model = (ITreeModel)req.getSession().getAttribute( "morse_tree");
		ObjectInfo parent = model.getObjectByPath( folderUri );
		LinkedList<ObjectInfo> list = model.getChildFolders( parent );
		String[] out = new String[ list.size() ];
		for ( int i = 0; i < out.length; i++ )
			out[i] = list.get( i ).getName();
		return out;
	}

	public Date getCreationDate(HttpServletRequest req, String uri)
			throws IOException {
		
		return new Date(); //TODO
	}

	public Date getLastModified(HttpServletRequest req, String uri)
			throws IOException {
		return new Date(); // TODO
	}

	public InputStream getResourceContent(HttpServletRequest req,
			String resourceUri) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public long getResourceLength(HttpServletRequest req, String resourceUri)
			throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean isFolder(HttpServletRequest req, String uri)
			throws Exception {
		ITreeModel model = (ITreeModel)req.getSession().getAttribute( "morse_tree");
		try {
			ObjectInfo parent = model.getObjectByPath( uri );
			return parent.isFolder();
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean isResource(HttpServletRequest req, String uri)
			throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean objectExists(HttpServletRequest req, String uri)
			throws IOException {
		// TODO Auto-generated method stub
		return true;
	}

	public void removeObject(HttpServletRequest req, String uri)
			throws IOException {
		// TODO Auto-generated method stub
		
	}

	public void rollback(HttpServletRequest req) throws IOException {
		// TODO Auto-generated method stub
		
	}

	public void setResourceContent(HttpServletRequest req, String resourceUri,
			InputStream content, String contentType, String characterEncoding)
			throws IOException {
		// TODO Auto-generated method stub
		
	}

}
