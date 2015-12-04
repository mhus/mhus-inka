package de.mhu.com.morse.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;

import de.mhu.lib.log.AL;
import de.mhu.com.morse.channel.AbstractSelectResult;
import de.mhu.com.morse.channel.IChannel;
import de.mhu.com.morse.channel.IChannelProvider;
import de.mhu.com.morse.channel.IConnection;
import de.mhu.com.morse.channel.IServer;
import de.mhu.com.morse.channel.Server;
import de.mhu.com.morse.mql.CompilledQueryMessage;
import de.mhu.com.morse.mql.ICompiledQuery;
import de.mhu.com.morse.mql.IQueryDefinition;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.mql.Query;
import de.mhu.com.morse.utils.AttributeUtil;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.mql.QueryParser;
import de.mhu.com.morse.net.IMessage;
import de.mhu.com.morse.obj.ITableRead;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.types.ITypes;
import de.mhu.lib.plugin.AfPluginException;

public class MChannelProvider implements IChannelProvider {

	private static AL log = new AL( MChannelProvider.class );
	private MSession session;
	private MyConnection defaultCon;
	private Server server = new Server();

	public MChannelProvider(MSession pSession) {
		session = pSession;
		defaultCon = new MyConnection( 0 );
	}
		
	public IConnection createConnection() throws MorseException {
		return null;
		// return MyConnection( 0 );
	}


	public IConnection getDefaultConnection() {
		return defaultCon;
	}
	
	class MyConnection implements IConnection {

		private Hashtable<String,IChannel> dbcons = new Hashtable<String, IChannel>();
		private int dbId;
		private boolean autoCommit = true;
		
		public MyConnection(int i) {
			dbId = i;
		}

		public void close() {
		}

		public synchronized IChannel getChannel(String srcName) {
			if ( srcName == null )
				srcName = "*";
			
			IChannel db = dbcons.get( srcName );
			if ( db != null ) return db;
			
			try {
				IMessage msg = session.getConnection().createMessage();
				msg.append( "db.def" );
				msg.append( srcName );
				IMessage res = session.sendAndWait( msg, 0 );
				
				db = new MyChannel( srcName, res.getStream( 0 ), dbId, this );
				dbcons.put( srcName, db );
				return db;
			} catch ( Exception e ) {
				log.error( e );
			}
			return null;
		}

		public void commit() {
			try {
				IMessage msg = session.getConnection().createMessage();
				msg.append( "ch.c" );
				msg.append( (byte)dbId );
				session.sendAndWait( msg, 0 );
				return;
			} catch ( Exception e ) {
				log.error( e );
			}
		}

		public boolean isAutoCommit() {
			return autoCommit ;
		}

		public void rollback() {
			try {
				IMessage msg = session.getConnection().createMessage();
				msg.append( "ch.r" );
				msg.append( (byte)dbId );
				session.sendAndWait( msg, 0 );
				return;
			} catch ( Exception e ) {
				log.error( e );
			}
		}

		public void setAutoCommit(boolean in) {
			try {
				IMessage msg = session.getConnection().createMessage();
				msg.append( "ch.sac" );
				msg.append( (byte)dbId );
				msg.append( (byte)(in ? 1 : 0) );
				IMessage ret = session.sendAndWait( msg, 0 );
				autoCommit = ret.getByte( 0 ) == 1;
				return;
			} catch ( Exception e ) {
				log.error( e );
			}
		}

		public IServer getServer() {
			return server;
		}
		
	}
	
	class MyChannel implements IChannel, IQueryDefinition {

		private String name;
		private QueryParser parser;
		private Properties properties;
		private int dbId;
		private IConnection connection;

		public MyChannel(String srcName, InputStream in, int id, IConnection pConnection ) throws IOException {
			name = srcName;
			connection = pConnection;
			dbId = id;
			properties = new Properties();
			properties.load( in );
			parser = new QueryParser( this );
			in.close();
		}

		public String getName() {
			return name;
		}

		public QueryParser getParser() {
			return parser;
		}

		public int getConstantId(String in) {
			return Integer.parseInt( properties.getProperty( in.toUpperCase(), "0" ) );
		}

		public String getQueryDefinition(String in) {
			if ( in == null ) in = "null";
			return properties.getProperty( in.toLowerCase() );
		}

		public void close() {
		}

		public IQueryResult query(Query in) throws MorseException {
			ICompiledQuery code = in.getCode();
			IMessage msg = session.getConnection().createMessage();
			msg.append( "qry" );
			msg.append( dbId );
			msg.append( in.getDbName() );
			CompilledQueryMessage.toMessage(code, msg);
			try {
				IMessage res = session.sendAndWait(msg, 0 );
				return new MyQueryResult( res, this );
				
			} catch ( MorseException e ) {
				throw e;
			} catch (Throwable e) {
				throw new MorseException( MorseException.ERROR, e );
			}
		}

		public IConnection getConnection() {
			return connection;
		}		
	}
		
	class MyQueryResult extends AbstractSelectResult {

		private int queueId = -1;
		// private MyDb db;
		private String[] colsNames;
		private String[] colsOrigin;
		private IAttribute[] colsAttr;
		private int[] colsType;
		private Hashtable<String,Object> current = null;
		private Hashtable<String, Integer> colsIndex;
		private int type;
		private LinkedList<Hashtable<String,Object>> cache = new LinkedList<Hashtable<String,Object>>();
		private OutputStream queueOutput = null;
		private InputStream queueInput = null;
		
		
		public MyQueryResult(IMessage res, MyChannel db) {
			// this.db = db;
			returnCode = res.shiftLong();
			if ( returnCode < 0 ) {
				errorCode = res.shiftInteger();
				errorInfo  = res.shiftString();
				return;
			}
			
			type = res.shiftInteger();
			
			int size   = res.shiftInteger();
			colsNames  = new String[ size ];
			colsOrigin = new String[ size ];
			colsType   = new int[ size ];
			colsAttr   = new IAttribute[ size ];
			colsIndex = new Hashtable<String, Integer>();
			ITypes typeModel = session.getConnection().getTypeModel();
			for ( int i = 0; i < size; i++ ) {
				colsNames[i] = res.shiftString();
				colsOrigin[i] = res.shiftString();
				colsType[i] = res.shiftInteger();
				colsAttr[i] = typeModel.getAttributeByCanonicalName( colsOrigin[ i ] );
				colsIndex.put( colsNames[ i ].toLowerCase(), i );
			}
			
			if ( type != IQueryResult.QUEUE_ONE_PACKAGE )
				queueId  = res.shiftInteger();
			else
				queueId = -1;
			
			try {
				loadIntoCache( res );
			} catch (MorseException e) {
				log.error( e );
			}
			
			if ( type == IQueryResult.QUEUE_STREAM_IN )
				queueOutput = new MyOutputStream( session, queueId );
			else
			if ( type == IQueryResult.QUEUE_STREAM_OUT )
				queueInput = new MyInputStream( session, queueId );
			
		}

		public ITableRead getTable(String name) throws MorseException {
			Object obj = current.get( name );
			if ( ! ( obj instanceof ITableRead ) ) return null;
			return (ITableRead)obj;
		}

		public ITableRead getTable(int index) throws MorseException {
			return getTable( colsNames[ index ] );
		}

		public void close() {
			if ( queueId < 0 ) return;
			try {
				IMessage msg = session.getConnection().createMessage();
				msg.append( "q.c" );
				msg.append( queueId );
				queueId = -1;
				session.getConnection().sendMessage(msg );
			} catch (Exception e) {
				log.error( e );
			}
		}

		public IAttribute getAttribute(String name ) throws MorseException {
			Integer i = colsIndex.get( name.toLowerCase() );
			if ( i == null )
				throw new MorseException( MorseException.ATTR_NOT_FOUND, name );
			return getAttribute( i );
		}
		
		public IAttribute getAttribute(int i) throws MorseException {
			return colsAttr[ i ];
		}

		public String[] getColumns() throws MorseException {
			return colsNames;
		}

		public String getRawString(String name) throws MorseException {
			Object obj = current.get( name );
			if ( ! ( obj instanceof String ) ) return null;
			return (String)obj;
		}

		public String getRawString(int i) throws MorseException {
			return getString( colsNames[ i ] );
		}

		public boolean next() throws MorseException {
			current = null;
			if ( cache.size() == 0 && type == IQueryResult.QUEUE_FETCH ) {
				if ( queueId < 0 ) return false;
				IMessage msg = session.getConnection().createMessage();
				msg.append( "q.n" );
				msg.append( queueId );
				try {
					IMessage res = session.sendAndWait(msg, 0 );
					loadIntoCache( res );
				} catch ( MorseException e ) {
					if ( e.getMessageId() != MorseException.RX_RETURN_CODE )
						log.error( e );
					queueId = -1;
					return false;		
				} catch (Exception e) {
					log.error( e );
					queueId = -1;
					return false;
				}
			}
			
			if ( cache.size() != 0 ) {
				current = cache.removeFirst();
				return true;
			}
			
			return false;
			
		}
		
		private void loadIntoCache( IMessage msg ) throws MorseException {
			int rc = 0;
			while ( ( rc = msg.shiftInteger() ) > 0 ) {
				Hashtable<String,Object> current = new Hashtable<String,Object>();
				for ( int i = 0; i < colsNames.length; i++ ) {
					// System.out.println( "COL: " + i + ' ' + colsNames[i] + ' ' + colsOrigin[ i ] );
					if ( colsType[i] != IAttribute.AT_TABLE )
						current.put( colsNames[i], AttributeUtil.readFromMsg( msg, colsAttr[ i ] ) );
					else
						current.put( colsNames[i], new MyTableQueryResult( this, i, msg ) );
					
				}
				cache.addLast( current );
			}
			/*
			if ( rc == -1 ) {
				close(); // close the queue
			}
			*/
		}

		public int getPreferedQuereType() {
			return QUEUE_FETCH;
		}

		public int getAttributeCount() {
			return colsNames.length;
		}

		public boolean reset() throws MorseException {
			IMessage msg = session.getConnection().createMessage();
			msg.append( "q.r" );
			msg.append( queueId );
			try {
				IMessage res = session.sendAndWait(msg, 0 );
				return ( res.getByte( 0 ) == 1 );
			} catch ( Exception e ) {
				log.error( e );
				queueId = -1;
				return false;
			}
		}
		
		public InputStream getInputStream() throws MorseException {
			if ( queueInput == null )
				throw new MorseException( MorseException.NOT_SUPPORTED );
			return queueInput;
		}

		public OutputStream getOutputStream() throws MorseException {
			if ( queueOutput == null )
				throw new MorseException( MorseException.NOT_SUPPORTED );
			return queueOutput;
		}
		
	}
	
	class MyTableQueryResult extends AbstractSelectResult {

		private MyQueryResult result;
		private int column;
		private String[] colsNames;
		//private String[] colsOrigin;
		private int[] colsType;
		private String[] current;
		private LinkedList values = new LinkedList();
		private Hashtable<String, Integer> colsIndex;
		private int pos = 0;

		public MyTableQueryResult(MyQueryResult result, int colNr, IMessage res) throws MorseException {
			this.result = result;
			this.column = colNr;
			IAttribute a = result.getAttribute( colNr );
			int cnt = 0;
			for ( Iterator i = a.getAttributes(); i.hasNext(); ) {
				i.next();
				cnt++;
			}
			// cnt+=2; // INCL. ID AND POS
			colsNames = new String[ cnt ];
			//colsOrigin = new String[ cnt ];
			colsType = new int[ cnt ];
			colsIndex = new Hashtable<String, Integer>();
			cnt = 0;
			for ( Iterator i = a.getAttributes(); i.hasNext(); ) {
				IAttribute ta = (IAttribute)i.next();
				colsNames[ cnt ] = ta.getName();
				//colsOrigin[ cnt ] = ta.getCanonicalName();
				colsType[ cnt ] = ta.getType();
				colsIndex.put( colsNames[ cnt ].toLowerCase(), cnt );
				cnt++;
			}
			/*
			colsNames[ cnt ] = IAttributeDefault.ATTR_OBJ_M_ID.getName();
			colsType [ cnt ] = IAttributeDefault.ATTR_OBJ_M_ID.getType();
			colsIndex.put( colsNames[ cnt ], cnt );
			cnt++;
			colsNames[ cnt ] = IAttributeDefault.ATTR_OBJ_M_POS.getName();
			colsType [ cnt ] = IAttributeDefault.ATTR_OBJ_M_POS.getType();
			colsIndex.put( colsNames[ cnt ], cnt );
			cnt++;
			*/
			// read records
			while ( res.shiftInteger() != 0 ) {
				String[] va = new String[ colsNames.length ];
				for ( int i = 0; i < colsNames.length; i++ )
					va[i] = AttributeUtil.readFromMsg( res , getAttribute( i ) );
				values.add( va );
			}
		}

		public void close() {
			values.clear();
		}

		public IAttribute getAttribute(String name ) throws MorseException {
			Integer i = colsIndex.get( name.toLowerCase() );
			if ( i == null )
				throw new MorseException( MorseException.ATTR_NOT_FOUND, name );
			return getAttribute( i );
		}
		
		public IAttribute getAttribute(int i) throws MorseException {
			// if ( i == colsNames.length - 1 ) return IAttributeDefault.ATTR_OBJ_M_POS;
			// if ( i == colsNames.length - 2 ) return IAttributeDefault.ATTR_OBJ_M_ID;
			
			return result.getAttribute( column ).getAttribute( colsNames[i] );
		}

		public String[] getColumns() throws MorseException {
			return colsNames;
		}

		public String getRawString(String name) throws MorseException {
			Integer i = colsIndex.get( name.toLowerCase() );
			if ( i == null )
				throw new MorseException( MorseException.ATTR_NOT_FOUND, name );
			return getString( i );
		}

		public String getRawString(int i) throws MorseException {
			return current[i];
		}

		public boolean next() throws MorseException {
			if ( pos >= values.size() ) return false;
			current = (String[])values.get( pos );
			pos++;
			return true;
		}

		public int getAttributeCount() {
			return colsNames.length;
		}

		@Override
		public int getPreferedQuereType() {
			return 0;
		}

		@Override
		public ITableRead getTable(String name) throws MorseException {
			return null;
		}

		@Override
		public ITableRead getTable(int index) throws MorseException {
			return null;
		}

		public boolean reset() throws MorseException {
			pos = 0;
			return true;
		}

		public InputStream getInputStream() throws MorseException {
			throw new MorseException( MorseException.NOT_SUPPORTED );
		}

		public OutputStream getOutputStream() throws MorseException {
			throw new MorseException( MorseException.NOT_SUPPORTED );
		}


	}
	
	class MyOutputStream extends OutputStream {

		private byte[] buffer = new byte[ 1024 * 20 ]; // TODO From config and handle minimum with server!!
		private int pos = 0;
		private IConnection connection;
		private int queueId;
		private MSession session;
		
		public MyOutputStream(MSession pSession, int pQueueId) {
			session = pSession;
			queueId = pQueueId;
		}

		@Override
		public void write(int b) throws IOException {
			if ( queueId == -1 ) throw new IOException( "Stream already closed" );
			buffer[ pos ] = (byte)b;
			pos++;
			if ( pos >= buffer.length ) {
				IMessage msg = null;
				try {
					msg = session.getConnection().createMessage();
				} catch (MorseException e1) {
					// TODO Auto-generated catch block
					log.error( e1 );
					throw new IOException( e1.getMessage() );
				}
				msg.append( "q.n" );
				msg.append( queueId );
				msg.append( buffer );
				try {
					IMessage res = session.sendAndWait( msg, 0 );
				} catch (AfPluginException e) {
					log.error( e );
					throw new IOException( "Can't write stream" );
				}
				
				pos = 0;
			}
		}
		
		@Override
		public void write( byte[] b, int offset, int len ) throws IOException {
			if ( queueId == -1 ) throw new IOException( "Stream already closed" );

			if (b == null) {
			    throw new NullPointerException();
			} else if ((offset < 0) || (offset > b.length) || (len < 0) ||
				   ((offset + len) > b.length) || ((offset + len) < 0)) {
			    throw new IndexOutOfBoundsException();
			} else if (len == 0) {
			    return;
			}
			
			while ( pos + len > buffer.length ) {
				// split ....
				
				int newLen = buffer.length - pos;
				
				write( b, offset, newLen );
				offset+=newLen;
				len-=newLen;
				
			}
			
			System.arraycopy( b, offset, buffer, pos, len );
			pos+=len;
			
			if ( pos == buffer.length ) {
				// send
				IMessage msg;
				try {
					msg = session.getConnection().createMessage();
				} catch (MorseException e1) {
					log.error( e1 );
					throw new IOException( e1.getMessage() );
				}
				msg.append( "q.n" );
				msg.append( queueId );
				msg.append( buffer );
				try {
					IMessage res = session.sendAndWait( msg, 0 );
				} catch (AfPluginException e) {
					log.error( e );
					throw new IOException( "Can't write stream" );
				}
				
				pos = 0;
			}
			
		}
		
		
		public void close() throws IOException {
			if ( queueId == -1 ) return;
			if ( pos != 0 ) {
				byte[] outBuffer = new byte[ pos ];
				System.arraycopy( buffer, 0, outBuffer, 0, pos );
				IMessage msg;
				try {
					msg = session.getConnection().createMessage();
				} catch (MorseException e1) {
					log.error( e1 );
					throw new IOException( e1.getMessage() );
				}
				msg.append( "q.n" );
				msg.append( queueId );
				msg.append( outBuffer );
				pos = 0;
				try {
					IMessage res = session.sendAndWait( msg, 0 );
				} catch (Exception e) {
					log.error( e );
					throw new IOException( "Can't write stream" );
				}		
				buffer = null;
			}
			IMessage msg;
			try {
				msg = session.getConnection().createMessage();
			} catch (MorseException e1) {
				log.error( e1 );
				throw new IOException( e1.getMessage() );
			}
			msg.append( "q.c" );
			msg.append( queueId );
			try {
				session.getConnection().sendMessage( msg );
			} catch (Exception e) {
				log.error( e );
				queueId = -1;
				throw new IOException( "Can't write stream" );
			}	
			queueId = -1;
		}
		
	}
	
	class MyInputStream extends InputStream {

		private byte[] buffer = null;
		private int queueId;
		private MSession session;
		private int size = 0;
		private int pos = 0;
		
		public MyInputStream(MSession pSession, int pQueueId) {
			session = pSession;
			queueId = pQueueId;
		}
		
		@Override
		public int read() throws IOException {
			
			if ( queueId == -1 ) 
				return -1;
			
			if ( pos >= size ) {
				IMessage msg;
				try {
					msg = session.getConnection().createMessage();
				} catch (MorseException e1) {
					log.error( e1 );
					throw new IOException( e1.getMessage() );
				}
				msg.append( "q.n" );
				msg.append( queueId );
				try {
					IMessage res = session.sendAndWait( msg, 0 );
					size = res.shiftInteger();
					/*
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					AFile.copyFile( res.shiftStream(),baos );
					buffer = baos.toByteArray();
					*/
					buffer = res.shiftByteArray();
						
				} catch ( MorseException me ) {
					if ( me.getMessageId() == MorseException.TX_QUEUE_END ) {
						queueId = -1;
						return -1;
					}
					log.error( me );
					throw new IOException( "Can't read stream" );						
				} catch (Exception e) {
					log.error( e );
					throw new IOException( "Can't read stream" );
				}
				pos = 0;
			}
			byte ret = buffer[ pos ];
			pos++;
			return ret;
		}
		
		/*
		@Override
		public int read( byte[] b, int offset, int len ) {
			
			if ( queueId == -1 ) 
				return -1;
			
			while ( pos + len > size ) {
				int newLen = size - 
			}
			
			
			
			
		}
		*/
		@Override
		public void close() throws IOException {
			if ( queueId == -1 ) return;
			IMessage msg;
			try {
				msg = session.getConnection().createMessage();
			} catch (MorseException e1) {
				log.error( e1 );
				throw new IOException( e1.getMessage() );
			}
			msg.append( "q.c" );
			msg.append( queueId );
			try {
				session.getConnection().sendMessage( msg );
			} catch (Exception e) {
				log.error( e );
				queueId = -1;
				throw new IOException( "Can't write stream" );
			}	
			queueId = -1;
		}
		
		@Override
		public int available() throws IOException {
			if ( queueId == -1 ) return 0;
			return size - pos;
	    }
		
	}
}