package de.mhu.com.morse.channel.exec;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import de.mhu.lib.log.AL;
import de.mhu.com.morse.mql.ErrorResult;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.utils.MorseException;

public class Exec extends AbstractExec {

	private static AL log = new AL( Exec.class );
	private HashSet<String> locks = new HashSet<String>();
	
	public void useLock( String id ) throws MorseException {
		synchronized ( locks ) {
			if ( locks.contains( id ) ) return;
			connection.getObjectManager().lock( id, "exec", 60000 );
			locks.add( id );
		}
	}
	
	@Override
	public IQueryResult exec(LinkedList<Object> attr, boolean async) throws MorseException {
		
		String[] usage = getProperty( "usage", "**" ).split( "\\|" );
		boolean ok = false;
		for ( int i = 0; i < usage.length; i++ ) {
			String[] u = usage[i].split( "," );
			for ( int j = 0; j < u.length; j++ ) {
				int pos = u[j].trim().indexOf( ' ' );
				if ( pos >= 0 )
					u[ j ] = u[ j ].substring( 0, pos );
				u[ j ] = u[ j ].trim();
				if ( "**".equals( u[ j ] ) ) {
					ok = true;
					continue;
				}
				if ( j >= attr.size() ) continue;
				Object val = attr.get( j );
				if ( 	"*".equals( u[ j ] ) ||
						"string".equals( u[ j ] ) && val instanceof String ||
						"int".equals( u[ j ] ) && val instanceof Integer ||
						"long".equals( u[ j ] ) && val instanceof Long ||
						"double".equals( u[ j ] ) && val instanceof Double ||
						"boolean".equals( u[ j ] ) && val instanceof Boolean 
						) {
				} else {
					// not correct - next check
					continue;
				}

			}
			if ( ok ) continue;
		}
		
		if ( !ok )
			throw new MorseException( MorseException.USAGE, getProperty( "usage", "**" ) );
		
		String lock = getProperty( "lock" );
		if ( lock != null ) {
			useLock( lock );
		}
		
		try {
			String type = getProperty( "type", "class" );
			if ( "class".equals( type ) ) {
				String clazz = getProperty( "class" );
				try {
					IExec exec = (IExec)getClass().getClassLoader().loadClass( clazz ).newInstance();
					Object[] ret = null;
					try {
						ret = exec.execute( this, attr );
					} catch ( ExecException e ) {
						return new ErrorResult( 0, e.getErrorCode(), e.toString() );
					}
					if ( ret == null )
						return new ErrorResult( 0, 0, null );
					
					return new ExecResult( ret );
					
				} catch ( Throwable e ) {
					log.info( clazz, e );
					throw new MorseException( MorseException.ERROR, e );
				}
			}
		} finally {
			synchronized ( locks ) {
				for ( Iterator<String> i = locks.iterator(); i.hasNext(); )
					connection.getObjectManager().unlock( i.next(), "exec" );
			}
		}
		return null;
	}

}
