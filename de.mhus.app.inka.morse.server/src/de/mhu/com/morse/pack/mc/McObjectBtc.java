package de.mhu.com.morse.pack.mc;

import java.util.Date;
import java.util.List;
import java.util.Set;

import de.mhu.com.morse.btc.ObjectBtc;
import de.mhu.com.morse.channel.sql.SqlDriver;
import de.mhu.com.morse.channel.sql.Table;
import de.mhu.com.morse.mql.ErrorResult;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.mql.Query;
import de.mhu.com.morse.mql.ServerQuery;
import de.mhu.com.morse.obj.ITable;
import de.mhu.com.morse.obj.ITableRead;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.types.IType;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.utils.ObjectUtil;

public class McObjectBtc extends ObjectBtc {

	@Override
	public void doInsert(String newId) {
		try {
			if ( getType().isInstanceOf( CMc.MC_DOCUMENT ) ) {
				String chronicle = getString( CMc.V_CHRONICLE_ID );
				if ( ! ObjectUtil.validateId( chronicle ) )
					setString( CMc.V_CHRONICLE_ID, newId );
			}
		} catch ( MorseException e ) {}			
	}

	@Override
	public void doInsertCheck() throws MorseException {
		
		if ( ! getType().isInstanceOf( CMc.MC_ROOT ) ) {
			String parent = getString( CMc.PARENT );
			ObjectUtil.assetId( parent );
			IQueryResult res = connection.fetch( parent, user, false );
			if ( ! res.next() ) {
				res.close();
				throw new MorseException( MorseException.PARENT_FOLDER_NOT_FOUND, parent );
			}
			if ( ! types.get( res.getString( IAttribute.M_TYPE ) ).isInstanceOf( CMc.MC_OBJECT ) ) {
				res.close();
				throw new MorseException( MorseException.PARENT_NOT_A_FOLDER, parent );				
			}
			res.close();
		}
		
		if ( getType().isInstanceOf( CMc.MC_DOCUMENT ) ) {
			
			String chronicle = getString( CMc.V_CHRONICLE_ID );
			if ( ObjectUtil.validateId( chronicle ) ) {
				IQueryResult current = new Query( connection, "SELECT "+CMc.V_VERSION+" FROM "+CMc.MC_DOCUMENT+" WHERE "+CMc.V_CHRONICLE_ID+"='" + chronicle + "'" )
					.execute();
				if ( current.next() ) {
					double curVersion = current.getDouble( 0 );
					if ( curVersion >= getDouble( CMc.V_VERSION ) )
						setDouble( CMc.V_VERSION, curVersion + 0.01 );
				} else {
					if ( getDouble( CMc.V_VERSION ) <= 0 )
						setDouble( CMc.V_VERSION, 1 );
				}
				current.close();
				new Query( connection, "UPDATE "+CMc.MC_DOCUMENT+" SET "+CMc.V_CURRENT+"=0 WHERE "+CMc.V_CHRONICLE_ID+"='" + chronicle + "' " +
						"`enable:-btc,-commit`" )
					.execute().close();
				new Query( connection, "UPDATE "+CMc.MC_DOCUMENT+" (wip) SET v_wip=0 WHERE "+CMc.V_CHRONICLE_ID+"='" + chronicle + "' " +
						"`enable:-btc,-commit`" )
				.execute().close();
				
			} else {
				if ( getDouble( CMc.V_VERSION ) <= 0 )
					setDouble( CMc.V_VERSION, 1 );
			}
			
			setBoolean( CMc.V_CURRENT, true );
			setBoolean( "v_wip", true );
			
		}
		
		Date now = new Date();
		setDate( CMc.CREATED, now );
		setDate( CMc.MODIFIED, now );
		if ( user != null ) {
			setString( CMc.CREATED_BY, user.getUserId() );
			setString( CMc.MODIFIED_BY, user.getUserId() );
		}
		
		/*
		ITable folder = getTable( "folder" );
		folder.reset();
		while ( folder.next() ) {
			folder.removeRow();
			folder.reset();
		}
		
		String parent = getString( "parent" );
		if ( ObjectUtil.validateId( parent ) ) {
			IQueryResult res = new Query( connection,
					"FETCH " + parent ).execute();
			res.next();
			ITableRead parentFolder = res.getTable( "folder" );
			while ( parentFolder.next() ) {
				folder.createRow();
				folder.setString( "path", 
						parentFolder.getString( "path" ) + 
						res.getString( "name" ) + '/');
				folder.setBoolean( "link", parentFolder.getBoolean( "link" ) );
				folder.appendRow();
			}
			parentFolder.close();
			res.close();
		} else {
			folder.createRow();
			folder.setString( "path", "/" );
			folder.appendRow();
		}
		
		ITable links = getTable( "links" );
		links.reset();
		while ( links.next() ) {
			IQueryResult res = new Query( connection,
					"FETCH " + links.getString( "id" ) ).execute();
			res.next();
			ITableRead parentFolder = res.getTable( "folder" );
			while ( parentFolder.next() ) {
				folder.createRow();
				folder.setString( "path", 
						parentFolder.getString( "path" ) + 
						res.getString( "name" ) + '/' );
				folder.setBoolean( "link", true );
				folder.appendRow();
			}
			parentFolder.close();
			res.close();
		}
		
		*/
		
		super.doInsertCheck();
	}

	@Override
	public void doDelete() throws MorseException {
		if ( getType().isInstanceOf( CMc.MC_DOCUMENT ) ) {
			if ( getBoolean( CMc.V_CURRENT ) ) {
				String chronicle = getString( CMc.V_CHRONICLE_ID );
				if ( ObjectUtil.validateId( chronicle ) ) {
					new Query( connection, "UPDATE " + CMc.MC_DOCUMENT + " (all) " +
							"SET " + CMc.V_CURRENT + "=1 " +
							"WHERE " + CMc.V_CHRONICLE_ID + "='" + chronicle + "' " +
							"ORDER BY " + CMc.V_VERSION + 
							" DESC LIMIT 1 " +
							"`enable:-btc,-commit`" )
					.execute().close();
				}
			}
		}
		super.doDelete();
	}
	
	@Override
	public void doUpdate() throws MorseException {
		super.doUpdate();
		
		if ( ! getType().isInstanceOf( CMc.MC_ROOT ) && isDirty( CMc.PARENT ) ) {
			String parent = getString( CMc.PARENT );
			ObjectUtil.assetId( parent );
			IQueryResult res = connection.fetch( parent, user, false );
			if ( ! res.next() ) {
				res.close();
				throw new MorseException( MorseException.PARENT_FOLDER_NOT_FOUND, parent );
			}
			if ( ! types.get( res.getString( IAttribute.M_TYPE ) ).isInstanceOf( CMc.MC_OBJECT ) ) {
				res.close();
				throw new MorseException( MorseException.PARENT_NOT_A_FOLDER, parent );				
			}
			res.close();
		}
		
		setDate( CMc.MODIFIED, new Date() );
		if ( user != null ) {
			setString( CMc.MODIFIED_BY, user.getUserId() );
		}
	}
	
	@Override
	public IQueryResult createRendition(int index, String format) throws MorseException {
		super.createRendition(index, format);
		String mql = "SAVE INTO "+CMc.MC_CONTENT+" FOR " + getObjectId() + " FORMAT " + format;
		ServerQuery q = new ServerQuery( connection, mql );
		IQueryResult res = q.execute( user );
		
		return res;
	}

	@Override
	public void insertRendition( int index, String format, String contentId, long size ) throws MorseException {
		super.insertRendition(index, format, contentId, size);
		
		if ( index == -1 ) {
			setString( CMc.CONTENT, contentId );
			setString( CMc.FORMAT, format );
			setLong( CMc.SIZE, size );
		} else {
			 ITable table = getTable( CMc.RENDITIONS );
			 if ( index < -1 )
				 table.createRow();
			 else
				 table.setCursor( index );
			 table.setString( CMc.R_CONTENT, contentId );
			 table.setString( CMc.R_FORMAT, format );
			 table.setLong( CMc.R_SIZE, size );
			 
			 if ( index < -1 )
				 table.appendRow();
			 
		}
		
		setDate( CMc.MODIFIED, new Date() );
		if ( user != null ) {
			setString( CMc.MODIFIED_BY, user.getUserId() );
		}

	}
	
	@Override
	public String deleteRendition(int index) throws MorseException {
		
		if ( index == -1 ) {
			setString( CMc.CONTENT, "" );
		} else
		if ( index >= 0 ) {
			ITable table = getTable( CMc.RENDITIONS );
			table.removeRow( index );
		}
		super.deleteRendition( index );
		setDate( CMc.MODIFIED, new Date() );
		if ( user != null ) {
			setString( CMc.MODIFIED_BY, user.getUserId() );
		}
		return null;
	}

	@Override
	public IQueryResult loadRendition(int index, Set<String> sharedChannels ) throws MorseException {
		String id = "";
		if ( index == -1 ) {
			id = getString( CMc.CONTENT );
		} else
		if ( index >= 0 ) {
			ITable table = getTable( CMc.RENDITIONS );
			table.setCursor( index );
			id = table.getString( CMc.R_CONTENT );
		}
		if ( ! ObjectUtil.validateId( id ) )
			return new ErrorResult( 1, 1, "no id" );
		String channel = connection.getObjectManager().findObject( id );
		if ( channel == null )
			return new ErrorResult( 1, 1, "no channel" );
		
		String shared = "";
		if ( sharedChannels != null && sharedChannels.contains( channel ) )
			shared = " SHARED";
		
		String mql = "LOAD " + id + shared + " @" + channel;
		ServerQuery q = new ServerQuery( connection, mql );
		return q.execute( user );
	
	}

	public boolean needSqlHint(int hintSize, String[] hints, Table table, SqlDriver driver ) {
		if ( ! table.type.isInstanceOf( CMc.MC_DOCUMENT ) )
			return false;
		if ( hints == null || hintSize == 0 ) return true;
		if ( hintSize == 1 && CMc.ALL.equals( hints[0] ) ) return false;
		return true;
	}

	public String getSqlHint(int hintSize, String[] hints, Table table, SqlDriver driver ) {
		if ( ! table.type.isInstanceOf( CMc.MC_DOCUMENT ) )
			return null;
		if ( hints == null || hintSize == 0 ) return table.alias + "." + driver.getColumnName( CMc.V_CURRENT ) + "=1";
		if ( hintSize == 1 && CMc.ALL.equals( hints[0] ) ) return null;
		StringBuffer sb = new StringBuffer();
		for ( int i = 0; i < hintSize; i++ ) {
			
			if ( i != 0 )
				sb.append( " AND " );
			
			sb.append( table.alias )
				.append( '.' )
				.append( driver.getColumnName( "v_" + hints[i] ) )
				.append( "=1");
		
		}
		return sb.toString();
	}

}
