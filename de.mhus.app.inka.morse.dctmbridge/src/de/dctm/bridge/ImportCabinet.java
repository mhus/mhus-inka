package de.dctm.bridge;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JFrame;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;

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
import de.mhu.com.morse.pack.mc.McDocument;
import de.mhu.com.morse.pack.mc.McFolder;
import de.mhu.com.morse.pack.mc.McObject;
import de.mhu.com.morse.pack.mc.McRoot;

public class ImportCabinet {

	private static DMConnection dmCon;
	private static MConnectionTcp mcCon;
	private static IQueryResult rExist;

	public static void main(String[] args) throws Exception {
		
		ArgsParser arg = new ArgsParser( args );
		ConfigManager.initialize( arg );
		ALUtilities.configure( arg );
		
		Config dmConfig = ConfigManager.getConfig( "dctm" );
		Config mcConfig = ConfigManager.getConfig( "morse" );
		
		if ( dmConfig.isProperty( "docbroker" ) ) {
			DMConnection.setDocbroker( dmConfig.getProperty( "docbroker" ), 1489 );
		}
		
		dmCon = new DMConnection( dmConfig.getProperty( "user" ), dmConfig.getProperty( "pass" ), dmConfig.getProperty( "docbase" ) );
		
		mcCon = new MConnectionTcp( mcConfig.getProperty("host"), 6666 );
		mcCon.setService( mcConfig.getProperty( "service" ) );
		mcCon.setAuth( mcConfig.getProperty( "user" ), new AuthPassword( mcConfig.getProperty( "pass" ) ) );
		mcCon.connect();
		IConnection mcDb = mcCon.getSession().getDbProvider().getDefaultConnection();
		
		String cabinet = dmConfig.getProperty( "cabinet", "/WCM_Sandbox" );
		
		insert( null, dmCon.getExistingObject( cabinet, " " ) );
		
	}

	private static void insert( String parentId, IDfSysObject obj) throws DfException  {
		String t = obj.getTypeName();
		System.out.println( ">>>>>>>>>>>>>>>> " + obj.getObjectName() + " <<<<<<<<<<<<<<<<<<");
		String newId = null;
		
		try {
			if ( t.equals( "ebase_wcm_channel" ) ) {
	//			 find a existing obj ?
				rExist = new Query( mcCon.getDefaultConnection(), "SELECT m_id FROM mc_root WHERE name='" + ASql.escape( obj.getObjectName() ) + "'" ).execute();
				if ( rExist.next() ) {
					newId = rExist.getString( 0 );
					rExist.close();
				} else {
					rExist.close();
					McRoot newObj = (McRoot)mcCon.createObject( "mc_root" );
					newObj.setName( obj.getObjectName() );
					
					newObj.setModifyDate( obj.getModifyDate().getDate() );
					newObj.setCreatedDate( obj.getCreationDate().getDate() );
					
					newObj.saveAsNew( mcCon.getDefaultConnection() );
					newId = newObj.getObjectId();
				}
			} else
			if ( obj.getTypeName().equals( "ebase_wcm_folder" ) ) {
	
				// find a existing obj ?
				rExist = new Query( mcCon.getDefaultConnection(), "SELECT m_id FROM mc_folder WHERE mc_parent='" + parentId + "' AND name='" + ASql.escape( obj.getObjectName() ) + "'" ).execute();
				if ( rExist.next() ) {
					newId = rExist.getString( 0 );
					rExist.close();
				} else {
					rExist.close();
					try {
						McFolder newObj = (McFolder)mcCon.createObject( "mc_folder" );
						newObj.setName( obj.getObjectName() );
						newObj.setParent( parentId );
						
						newObj.setModifyDate( obj.getModifyDate().getDate() );
						newObj.setCreatedDate( obj.getCreationDate().getDate() );
	
						newObj.saveAsNew( mcCon.getDefaultConnection() );
						newId = newObj.getObjectId();
					} catch ( Exception dfe ) {
						dfe.printStackTrace();
						return;
					}			
				}
	
			} else {
	//			 find a existing obj ?
				rExist = new Query( mcCon.getDefaultConnection(), "SELECT m_id FROM mc_document WHERE mc_parent='" + parentId + "' AND name='" + ASql.escape( obj.getObjectName() ) + "' AND mc_lang='" + obj.getString( "language_code" ) + "'" ).execute();
				if ( rExist.next() ) {
					newId = rExist.getString( 0 );
					rExist.close();
					return;
				} else {
					rExist.close();
				
					try {
						String dql = "select r_object_id from dm_sysobject (all) where i_chronicle_id='" + obj.getChronicleId() + "'";
						IDfQuery query = dmCon.createQuery( dql );
						IDfCollection res = query.execute( dmCon.getSession(), IDfQuery.READ_QUERY );
						
						LinkedList<String> ll = new LinkedList<String>();
						while ( res.next() ) {
							ll.add( res.getString( "r_object_id" ) );
						}
						res.close();
						
						String chronicleId = null;
						
						for ( Iterator<String> i = ll.iterator(); i.hasNext(); ) {
							IDfSysObject o = dmCon.getExistingObject( i.next() );
							McDocument newObj = (McDocument)mcCon.createObject( "mc_document" );
							newObj.setMqlEnable( "-btc,-event" );
							newObj.setName( o.getObjectName() );
							newObj.setLanguage( o.getString( "language_code" ) );
							newObj.setParent( parentId );
							
							for ( int k = 0; k < o.getValueCount( "r_version_label" ); k++ ) {
								String v = o.getRepeatingString( "r_version_label" , k );
								if ( k == 0 ) {
									try {
										newObj.setDouble( "v_version", Double.parseDouble( v ) );
									} catch ( Exception e ) {}
								}
								if ( "CURRENT".equals( v ) )
									newObj.setBoolean( "v_current", true );
								else
								if ( "Active".equals( v ) )
									newObj.setBoolean( "v_active", true );
								else
								if ( "WIP".equals( v ) )
									newObj.setBoolean( "v_wip", true );
								else
								if ( "Staging".equals( v ) )
									newObj.setBoolean( "v_staging", true );
								else
								if ( "Expired".equals( v ) )
									newObj.setBoolean( "v_expired", true );
								else
								if ( "Approved".equals( v ) )
									newObj.setBoolean( "v_approved", true );
		
							}
							
							if ( chronicleId != null )
								newObj.setChronicleId( chronicleId );
							
							newObj.setModifyDate( o.getModifyDate().getDate() );
							newObj.setCreatedDate( o.getCreationDate().getDate() );
	
							newObj.saveAsNew( mcCon.getDefaultConnection() );
							
							if ( chronicleId == null )
								chronicleId = newObj.getObjectId();
							try {
								ByteArrayInputStream bais = o.getContent();
								
								newObj.saveRendition( mcCon.getDefaultConnection(), bais, o.getContentType() );
								bais.close();
							} catch ( Exception dfe ) {
								dfe.printStackTrace();
							}
						}
						
						return;
					
					} catch ( Exception dfe ) {
						dfe.printStackTrace();
						return;
					}	
				}
			}
			
			String dql = "select r_object_id from dm_sysobject where any i_folder_id='" + obj.getObjectId() + "'";
			IDfQuery query = dmCon.createQuery( dql );
			IDfCollection res = query.execute( dmCon.getSession(), IDfQuery.READ_QUERY );
			
			LinkedList<String> ll = new LinkedList<String>();
			while ( res.next() ) {
				ll.add( res.getString( "r_object_id" ) );
			}
			res.close();
			
			for ( Iterator<String> i = ll.iterator(); i.hasNext(); ) {
				insert( newId, dmCon.getExistingObject( i.next() ) );
			}

		} catch ( Exception ex ) {
			ex.printStackTrace();
		}
	}
}
