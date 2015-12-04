package de.mhu.com.morse.pack.lucene;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;

import de.mhu.com.morse.aaa.IAclManager;
import de.mhu.com.morse.channel.IConnectionServer;
import de.mhu.com.morse.channel.IObjectListener;
import de.mhu.com.morse.channel.idx.IIdx;
import de.mhu.com.morse.channel.idx.IdxDriver;
import de.mhu.com.morse.mql.ErrorResult;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.mql.Query;
import de.mhu.com.morse.obj.ITableRead;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.usr.UserInformation;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.utils.ObjectUtil;
import de.mhu.lib.log.AL;

public class LuceneIdx implements IIdx, IObjectListener {

	private static AL log = new AL( LuceneIdx.class );
	private Map<String, Index> idxByName = new Hashtable<String, Index>();
	private Map<String, LinkedList<Index>> idxByType = new Hashtable<String, LinkedList<Index>>();
	private IAclManager aclManager;

	public void closeIndex() {
		// TODO Auto-generated method stub
		
	}

	public void initIndex(IdxDriver idxDriver, Map<String, String> features)
			throws MorseException {

		aclManager = idxDriver.getAclManager();
		
		IQueryResult res = new Query( idxDriver.getChannelProvider().getDefaultConnection(), 
				"SELECT ** FROM lucene_idx @sys" ).execute();
		while ( res.next() ) {
			
			Index idx = new Index( res );
			idx.validate();
			idxByName.put( idx.name, idx );
			for ( Object[] t : idx.types ) {
				LinkedList<Index> list = idxByType.get( t );
				if ( list == null ) {
					list = new LinkedList<Index>();
					idxByType.put( (String)t[0], list );
				}
				list.add( idx );
			}
			
		}
		res.close();
		
		idxDriver.getObjectManager().registerObjectListener( this );
	}

	public IQueryResult rebuild(IConnectionServer connection,
			UserInformation user, LinkedList<String> names)
			throws MorseException {
		
		if ( ! aclManager.isAdministrator( user ) )
			throw new MorseException( MorseException.ACCESS_DENIED );
		
		if ( log.t6() ) log.debug( "REBUILD: " + names );
		
		for ( Index idx : idxByName.values() ) {
			try {
				
				rebuild( connection, idx );
				
			} catch ( Exception e ) {
				if ( log.t4() ) log.error( e );
			}
		}
		
		return new ErrorResult( 0, 0, "" );
	}
	
	private void rebuild( IConnectionServer connection, Index idx ) throws MorseException {
		
		if ( log.t4() ) log.info( "Start Rebuild: " + idx.name );
		String type = idx.baseType;
		
		idx.createWriter( true );
		
		IQueryResult channels = new Query( connection, "SELECT channel FROM m_channel @sys" ).execute();
		while ( channels.next() ) {
			String name = channels.getString( "channel" );
			if ( ! name.equals( "*" ) ) {
				try {
					IQueryResult res = new Query( connection, "SELECT m_id,m_type FROM " + type + " @" + name ).execute();
					while ( res.next() ) {
						idx.insertObject( connection, res.getString( "m_id" ), res.getString( "m_type" ) );
					}
					res.close();
				} catch ( Throwable e ) {
					if ( log.t10() ) log.warn( "Rebuild from Channel " + name + ": " + e );
				}
			}
		}
		try {
			idx.optimize();
		} catch ( Throwable e ) {
			if ( log.t10() ) log.warn( "Rebuild Optimize " + idx.name + ": " + e );
		}
		channels.close();
		
		
	}
	
	public IQueryResult select(IConnectionServer connection,
			UserInformation user, LinkedList<String> names,
			LinkedList<String> attributes, LinkedList<String[]> where)
			throws MorseException {
		
		if ( names.size() != 2 )
			throw new MorseException( MorseException.ERROR, "Wrong list of names" );
		
		if ( where.size() == 0 )
			throw new MorseException( MorseException.ERROR, "Empty where clause" );
		
		if ( attributes.size() == 0 )
			throw new MorseException( MorseException.NO_ATTRIBUTES );

		Index idx = idxByName.get( names.get( 1 ) );
		
		String query = null;
		for ( String[] w : where ) {
			if ( w[0].equals( "query" ) )
				query = w[1];
		}
		
		if ( query == null )
			throw new MorseException( MorseException.ERROR, "query not defined" );
		
		try {
			return new LuceneIdxResult( idx.execute( query, user ), attributes, aclManager, user );
		} catch (Exception e) {
			throw new MorseException( MorseException.ERROR, e );
		}
		
	}

	private class Index {

		private String name;
		private String path;
		private Object[][] types;
		private Object[][] attributes;
		private File targetDir;
		private IndexWriter writer;
		// private IndexReader reader;
		private String baseType;

		public Index(IQueryResult res) throws MorseException {
			name = res.getString( "name" );
			path = res.getString( "path" );
			LinkedList<Object[]> oa = ObjectUtil.tableToList( res.getTable( "types" ), new String[] { "name", "parser_schema", "subtypes" } );
			types = oa.toArray( new Object[ oa.size() ][] );
			/*
			types = new String[ oa.size() ];
			for ( int i = 0; i < oa.size(); i++ ) types[i] = oa.get( i )[0].toString();
			*/
			oa = ObjectUtil.tableToList( res.getTable( "attributes" ), new String[] { "name", "store_type", "idx_type" } );
			attributes = oa.toArray( new Object[ oa.size() ][] );
			/*
			attributes = new String[ oa.size() ];
			for ( int i = 0; i < oa.size(); i++ ) attributes[i] = oa.get( i )[0].toString();
			*/
			baseType = res.getString( "base_type" );
			
			targetDir = new File( path );
		}

		public void optimize() throws CorruptIndexException, IOException {
			writer.optimize();
			writer.flush();
			writer.close();
			writer = null;
		}

		public Hits execute(String line, UserInformation user ) throws Exception {

			IndexReader reader = null;
			try {
				reader = IndexReader.open( targetDir );
			} catch (Exception e) {
				throw new MorseException( MorseException.ERROR, e );
			}
			
			Searcher searcher = new IndexSearcher(reader);
			Analyzer analyzer = new StandardAnalyzer();
			QueryParser parser = new QueryParser("_rendition", analyzer);
			org.apache.lucene.search.Query query = parser.parse(line);
			Hits hits = searcher.search(query, new AclFilter( aclManager, user ));
		
			return hits;
		}

		public void insertObject(IConnectionServer connection, String id,
				String type) throws MorseException {
			
			if ( writer == null ) {
				createWriter( false );
			}
			
			boolean ok = false;
			for ( int i = 0; i < types.length; i++ )
				if ( types[i][0].equals( type ) ) { ok = true; break; }
			if ( ! ok ) return;
			
			IQueryResult res = connection.fetch( id, null,  false );
			if ( ! res.next() )
				throw new MorseException( MorseException.OBJECT_NOT_FOUND, id );
			
			Document doc = new Document();
			
			int cnt = 0;
			IQueryResult r = null;
			InputStreamReader is = null;
			for ( Object[] at : attributes ) {
				
				try {
					if ( "_rendition".equals( at[0] ) ) {
						r = new Query( connection, "RENDITION " + id + " LOAD DEFAULT" ).execute();
						is = new InputStreamReader( r.getInputStream() );
						doc.add( new Field( "_rendition", is ) );
						cnt++;
					} else {
						String value = res.getString( (String)at[0] );
						Field.Store store = Field.Store.YES;
						if ( "no".equals( at[1] ) )
							store = Field.Store.NO;
						else
						if ( "compress".equals( at[1] ) )
							store = Field.Store.COMPRESS;
						Field.Index index = Field.Index.TOKENIZED;
						if ( "no".equals( at[2] ) )
							index = Field.Index.NO;
						else
						if ( "no_norms".equals( at[2] ) )
							index = Field.Index.NO_NORMS;
						else
						if ( "un_tokenized".equals( at[2] ) )
							index = Field.Index.UN_TOKENIZED;
						
						doc.add( new Field( (String)at[0], value, store, index ) );
						cnt++;
					}
				} catch ( Exception e ) {
					if ( log.t6() ) log.warn( "Indexing: " + name + "Attribute: " + (String)at[0] + " ID: " + id , e );
					// throw new MorseException( MorseException.ERROR, e ); // TODO REMOVE LINE !!!
				}
			}
			
			if ( cnt != 0 )
				try {
					doc.add( new Field( "m_id", id, Field.Store.YES, Field.Index.UN_TOKENIZED ) );
					doc.add( new Field( "m_acl", res.getString( IAttribute.M_ACL ), Field.Store.YES, Field.Index.UN_TOKENIZED ) );
					writer.addDocument( doc );
				} catch (Exception e) {
					if ( log.t6() ) log.warn( "Indexing: " + name + " ID: " + id , e );
				}
			
			if ( is != null )
				try {
					is.close();
				} catch (IOException e) {
					if ( log.t6() ) log.warn( "Indexing Close Stream: " + name + " ID: " + id , e );
				}
			if ( r != null ) r.close();
			res.close();
		}

		private void createWriter( boolean create ) throws MorseException {
			
			if ( writer != null ) {
				try {
					writer.close();
				} catch (Exception e) {
					if ( log.t6() ) log.warn( "Close writer: " + name , e );
				}
				writer = null;
			}
			
			try {
				Directory dir = FSDirectory.getDirectory( targetDir );
				writer = new IndexWriter( dir, true, new StandardAnalyzer(), create );
				
				writer.setInfoStream( System.out );
				
			} catch (Exception e) {
				try {
					writer = new IndexWriter( targetDir, new StandardAnalyzer(), true );
				} catch (Exception e1) {
					throw new MorseException( MorseException.ERROR, e1 );
				}
			}
		}

		public void validate() throws MorseException {

			if ( ! targetDir.exists() && ! targetDir.mkdirs() )
				throw new MorseException( MorseException.ERROR, "Can't create " + path );
			
			if ( ! targetDir.isDirectory() )
				throw new MorseException( MorseException.ERROR, "Not a directory " + path );
			
			createWriter( false );
			/*
			try {
				reader = IndexReader.open( targetDir );
			} catch (Exception e) {
				throw new MorseException( MorseException.ERROR, e );
			}
			*/
		}
		
	}

	public void eventContentRemoved(String channel, String id, String parentId,
			String parentType) {
		// TODO Auto-generated method stub
		
	}

	public void eventContentSaved(String channel, String id, String parentId,
			String parentType) {
		// TODO Auto-generated method stub
		
	}

	public void eventObjectCreated(String channel, String id, String type) {
		// TODO Auto-generated method stub
		
	}

	public void eventObjectDeleted(String channel, String id, String type) {
		// TODO Auto-generated method stub
		
	}

	public void eventObjectUpdated(String channel, String id, String type,
			String[] attributes) {
		// TODO Auto-generated method stub
		
	}
}
