package de.mhu.com.morse.obj;

import de.mhu.lib.ASql;
import de.mhu.lib.log.AL;
import de.mhu.com.morse.channel.IConnection;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.mql.Query;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.types.IType;
import de.mhu.com.morse.utils.MorseException;


public class BtoObject extends de.mhu.com.morse.obj.MObject {

	private static AL log = new AL( BtoObject.class );

	private String mqlEnable;

	public void setMqlEnable(String in) {
		if ( in == null )
			mqlEnable = null;
		else
			mqlEnable = " `enable:" + in + '`';
	}

	public void saveAsNew(IConnection con ) throws MorseException {
		saveAsNew( con, null );
	}
	 
	public void saveAsNew(IConnection con, String channel ) throws MorseException {
		
		StringBuffer mql = new StringBuffer().append( "INSERT INTO " ).append( getType().getName() ).append( '(' );
		boolean needComma = false;
		for ( int i = 0; i < colNames.length; i++ ) {
			if ( ! ( values[i][0] instanceof ITableRead ) && 
				 ! getType().getAttribute( colNames[i] ).getSourceType().getName().equals( IType.TYPE_OBJECT ) ) {
				if ( needComma ) mql.append( ',' );
				needComma = true;
				mql.append( colNames[i] );
			}
		}
		mql.append( ") VALUES (" );
		needComma = false;
		for ( int i = 0; i < colNames.length; i++ ) {	
			if ( ! ( values[i][0] instanceof ITable ) && !getType().getAttribute( colNames[i] ).getSourceType().getName().equals( IType.TYPE_OBJECT ) ) {
				if ( needComma ) mql.append( ',' );	
				needComma = true;
				mql.append('\'');
				mql.append( getRawString( i ) );
				mql.append('\'');
			}
		}
		mql.append( ')' );
		
		for ( int i = 0; i < colNames.length; i++ ) {
			if ( values[i][0] instanceof ITable ) {
				MObjectTable table = (MObjectTable)getTable( i );
				table.reset();
				String[] tCols = table.getColumns();
				while ( table.next() ) {
					mql.append( "APPEND VALUES(" );
					for ( int j = 0; j < tCols.length; j++ ) {
						if ( j != 0 ) mql.append( ',' );		
						mql.append('\'');
						mql.append( table.getRawString( j ) );
						mql.append('\'');
					}
					mql.append( ')' );
				}
			}
		}
		
		if ( mqlEnable != null )
			mql.append( mqlEnable );
		
		if ( channel != null )
			mql.append( " @" ).append( channel );
		
		IQueryResult res = new Query( con, mql.toString() ).execute();
		try {
			if ( !res.next() )
				throw new MorseException( MorseException.CANT_CREATE_OBJECT );
			setString( IAttribute.M_ID, res.getString( 0 ) );
		} finally {
			res.close();
		}
		cleanUp();
		// fetch()
	}

	public void save( IConnection con ) throws MorseException {
		save( con, null );
	}
	
	public void save( IConnection con, String channel ) throws MorseException {
		
		if ( ! isDirty() ) return;
		
		StringBuffer mql = new StringBuffer().append( "UPDATE " ).append( getType().getName() ).append( " SET " );
		boolean needComma = false;
		for ( int i = 0; i < colNames.length; i++ ) {
			if ( isDirty( i ) ) {
				if ( needComma ) mql.append( ',' );
				needComma = true;
				mql.append( colNames[i] );
				if ( values[i][0] instanceof ITable ) {
					mql.append( " TRUNCATE" );
					MObjectTable table = (MObjectTable)getTable( i );
					String[] tCols = table.getColumns();
					while ( table.next() ) {
						mql.append(  ',' ).append( colNames[i] ).append( " APPEND VALUES (" );
						for ( int j = 0; j < tCols.length; j++ ) {
							if ( j != 0 ) mql.append( ',' );
							mql.append( '\'' ).append( table.getRawString( j ) ).append( '\'' );
						}
						mql.append( ')' );
					}
				} else {
					mql.append( "='" ).append( ASql.escape( (String)values[i][0] ) ).append('\'' );
				}
				
			}
		}
		
		if ( mqlEnable != null )
			mql.append( mqlEnable );

		if ( channel != null )
			mql.append( " @" ).append( channel );
		
		new Query( con, mql.toString() ).execute().close();
		cleanUp();
		// fetch()
	}

	public final void loadData( IQueryResult obj ) throws MorseException {
		if ( obj != null ) {
			for ( int i = 0; i < obj.getAttributeCount(); i++ ) {
				IAttribute attr = obj.getAttribute( i );
				if ( attr.isTable() ) {
					ITable d = getTable( attr.getName() );
					ITableRead s = obj.getTable( i );
					while ( s.next() ) {
						d.createRow();
						for ( int j = 0; j < s.getAttributeCount(); j++ )
							try {
								d.setString( j, s.getString( j ) );
							} catch ( Exception e ) {
								log.warn( e );
							}
						d.appendRow();
					}
				} else {
					try {
						setString( attr.getName(), obj.getString( attr.getName() ) );
					} catch ( Exception e ) {
						log.warn( e );
					}
				}
			}
			cleanUp();
		}
	}
	
	
}
