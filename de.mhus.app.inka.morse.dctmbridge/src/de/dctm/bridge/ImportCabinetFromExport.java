package de.dctm.bridge;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;

import de.mhu.lib.ACast;
import de.mhu.lib.ASql;
import de.mhu.lib.ArgsParser;
import de.mhu.lib.config.Config;
import de.mhu.lib.config.ConfigManager;
import de.mhu.lib.log.AL;
import de.mhu.lib.log.ALUtilities;
import de.mhu.lib.log.SwingAppender;
import de.mhu.com.morse.channel.IConnection;
import de.mhu.com.morse.client.AuthPassword;
import de.mhu.com.morse.client.MConnection;
import de.mhu.com.morse.client.MConnectionTcp;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.mql.Query;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.lib.utils.Properties;
import de.mhu.com.morse.pack.mc.McDocument;
import de.mhu.com.morse.pack.mc.McFolder;
import de.mhu.com.morse.pack.mc.McObject;
import de.mhu.com.morse.pack.mc.McRoot;

public class ImportCabinetFromExport {

	private static MConnectionTcp mcCon;
	private static IQueryResult rExist;

	public static void main(String[] args) throws Exception {
		
		ArgsParser argp = new ArgsParser( args );
		ConfigManager.initialize( argp );
		ALUtilities.configure( argp );
		
		Config mcConfig = ConfigManager.getConfig( "morse" );
		
		
		mcCon = new MConnectionTcp( mcConfig.getProperty("host"), 6666 );
		mcCon.setService( mcConfig.getProperty( "service" ) );
		mcCon.setAuth( mcConfig.getProperty( "user" ), new AuthPassword( mcConfig.getProperty( "pass" ) ) );
		mcCon.connect();
		//IConnection mcDb = mcCon.getSession().getDbProvider().getDefaultConnection();
		
		String path = mcConfig.getProperty( "import_path" );
		if ( path == null ) {
			JFileChooser fc = new JFileChooser();
			fc.setMultiSelectionEnabled( false );
			fc.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
			fc.setDialogTitle( "Select Root Directory for Import" );
			if ( fc.showOpenDialog( null ) != JFileChooser.APPROVE_OPTION )
				return;
			File f = fc.getSelectedFile();
			if ( f == null || !f.exists() || !f.isDirectory() )
				return;
			path = f.getAbsolutePath();
		}
		
		File dir = new File( path );
		
		
		insert( null, dir );
		System.out.println( "FINISH" );
		
	}

	private static void insert( String parentId, File dir ) throws DfException  {
		
		String newId = null;
		
		try {
			if ( dir.isDirectory() ) {
				Properties p = new Properties();
				p.load( new FileInputStream( new File( dir, "attr.txt" ) ) );
				System.out.println( ">>>>>>>>>>>>>>>> " + p.getProperty( "_path" ) + " <<<<<<<<<<<<<<<<<<");
				
				if ( parentId == null ) {
					rExist = new Query( mcCon.getDefaultConnection(), "SELECT m_id FROM mc_root WHERE name='" + ASql.escape( p.getProperty( "object_name" ) ) + "'" ).execute();
					if ( rExist.next() ) {
						newId = rExist.getString( 0 );
						rExist.close();
					} else {
						rExist.close();
						McRoot newObj = (McRoot)mcCon.createObject( "mc_root" );
						newObj.setName( p.getProperty( "object_name" ) );
						
						newObj.setModifyDate( ACast.toDate( p.getProperty( "r_modify_date" ) ) );
						newObj.setCreatedDate( ACast.toDate( p.getProperty( "r_creation_date" ) ) );
						
						newObj.saveAsNew( mcCon.getDefaultConnection() );
						newId = newObj.getObjectId();
					}
	
				} else {
					
	//				 find a existing obj ?
					rExist = new Query( mcCon.getDefaultConnection(), "SELECT m_id FROM mc_folder WHERE mc_parent='" + parentId + "' AND name='" + ASql.escape( p.getProperty( "object_name" ) ) + "'" ).execute();
					if ( rExist.next() ) {
						newId = rExist.getString( 0 );
						rExist.close();
					} else {
						rExist.close();
						try {
							McFolder newObj = (McFolder)mcCon.createObject( "mc_folder" );
							newObj.setName( p.getProperty( "object_name" ) );
							newObj.setParent( parentId );
	
							newObj.setModifyDate( ACast.toDate( p.getProperty( "r_modify_date" ) ) );
							newObj.setCreatedDate( ACast.toDate( p.getProperty( "r_creation_date" ) ) );
							
							newObj.saveAsNew( mcCon.getDefaultConnection() );
							newId = newObj.getObjectId();
						} catch ( Exception dfe ) {
							dfe.printStackTrace();
							return;
						}
					}
				}
				
				int cnt = 0;
				while ( true ) {
					File f = new File( dir, String.valueOf( cnt ) );
					if ( f.exists() ) {
						insert( newId, f );
					} else
						break;
					cnt++;
				}
				
			} else {
				
				String chronicleId = null;
				Properties p = new Properties();
				p.load( new FileInputStream( dir ) );
				int versionCnt = Integer.parseInt( p.getProperty( "cnt" ) );
				for ( int i = 0; i < versionCnt; i++ ) {				
					Properties v = new Properties();
					v.load( new FileInputStream( dir.getAbsolutePath() + "." + i + ".txt" ) );
					System.out.println( ">>>>>>>>>>>>>>>> " + v.getProperty( "_path" ) + ' ' + v.getProperty( "r_version_label.0") + " <<<<<<<<<<<<<<<<<<");
					
					McDocument newObj = (McDocument)mcCon.createObject( "mc_document" );
					newObj.setMqlEnable( "-btc,-event" );
					newObj.setName( v.getProperty( "object_name") );
					newObj.setLanguage( v.getProperty( "language_code" ) );
					newObj.setParent( parentId );
					
					for ( int k = 0; v.getProperty( "r_version_label." + k ) != null; k++ ) {
						String vv = v.getProperty( "r_version_label." + k );
						if ( k == 0 ) {
							try {
								newObj.setDouble( "v_version", Double.parseDouble( vv ) );
							} catch ( Exception e ) {}
						}
						if ( "CURRENT".equals( vv ) )
							newObj.setBoolean( "v_current", true );
						else
						if ( "Active".equals( vv ) )
							newObj.setBoolean( "v_active", true );
						else
						if ( "WIP".equals( vv ) )
							newObj.setBoolean( "v_wip", true );
						else
						if ( "Staging".equals( vv ) )
							newObj.setBoolean( "v_staging", true );
						else
						if ( "Expired".equals( vv ) )
							newObj.setBoolean( "v_expired", true );
						else
						if ( "Approved".equals( vv ) )
							newObj.setBoolean( "v_approved", true );
	
					}
					
					if ( chronicleId != null )
						newObj.setChronicleId( chronicleId );
	
					newObj.setModifyDate( ACast.toDate( v.getProperty( "r_modify_date" ) ) );
					newObj.setCreatedDate( ACast.toDate( v.getProperty( "r_creation_date" ) ) );
					
					newObj.saveAsNew( mcCon.getDefaultConnection() );
					
					if ( chronicleId == null ) {
						chronicleId = newObj.getObjectId();
						new Query( mcCon.getDefaultConnection(), "UPDATE mc_document SET v_chronicle_id='" + chronicleId + "' WHERE m_id='" + chronicleId + "' `enable:-btc,-event`" ).execute().close();
					}
					
					
					try {
						
						String r = p.getProperty( "r." + i,"" );
						if ( r.length() > 0 ) {
							String[] renditions = r.split(",");
							
							for ( int j = 0; j < renditions.length; j++ ) {
								FileInputStream fis = new FileInputStream( dir.getAbsoluteFile() + "." + i + "." + renditions[j] + ".dat" );
								if ( j == 0 )
									newObj.saveRendition( mcCon.getDefaultConnection(), fis, renditions[j] );
								else
									newObj.appendRendition( mcCon.getDefaultConnection(), fis, renditions[j] );
		
								fis.close();
							}
						}			
					} catch ( Exception dfe ) {
						dfe.printStackTrace();
					}
					
				}
				
			}

		} catch ( Exception ex ) {
			ex.printStackTrace();
		}
	}
}
