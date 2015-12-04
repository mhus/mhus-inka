package de.mhu.com.morse.channel;

import de.mhu.lib.log.AL;
import de.mhu.com.morse.cmd.ifc.Weak;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.utils.AttributeUtil;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.net.Client;
import de.mhu.com.morse.net.IMessage;
import de.mhu.com.morse.obj.ITableRead;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.usr.IQueue;

public class QueryResultQueue implements IQueue {

	private static AL log = new AL( QueryResultQueue.class );
	private static final long MIN_MSG_SIZE = 1024 * 20; // current 20k
	private IQueryResult res;
	private String[] cols;

	public QueryResultQueue( IQueryResult pRes) throws MorseException {
		// weak = pWeak;
		res = pRes;
		cols = res.getColumns();
	}

	public void close() {
		if ( res != null ) res.close();
		res = null;
	}

	public IMessage next( IMessage m, Weak weak ) {
		if ( res == null )
			return null;
	
		try {
			
			
			IMessage msg = weak.getClient().createMessage();
				
			boolean hasNext = appendToMsg( msg );
			
			if ( hasNext )
				msg.append( 0 );
			else {
				msg.append( -1 );
				close();
			}
			
			return msg;
			
			
		} catch ( Exception e ) {
			log.error( e );
		}
			return null;
			
	}

	public boolean appendToMsg( IMessage msg ) throws MorseException {
		
		do {
			boolean hasNext = res.next();
			if ( ! hasNext )
				return false;
			
			msg.append( cols.length );
			
			for ( int i = 0; i < cols.length; i++ ) {
				
				IAttribute attr = res.getAttribute( i );
				if ( attr.isTable() ) {
					/*
					int id = weak.getSession().createQueue( 
							new QueryResultQueue( 
									new TableWrapper( res.getTable( i )
								) 
							) );
					msg.append( id );
					*/
					ITableRead table = res.getTable( i );
					String[] tcols = table.getColumns();
					if ( tcols.length != 0 ) {
						while ( table.next() ) {
							msg.append( tcols.length );
							for ( int j = 0; j < tcols.length; j++ ) {
								AttributeUtil.appendToMsg( table, j,  msg );
							}
						}
					}
					table.close();
					msg.append( 0 );
					
				} else {
					AttributeUtil.appendToMsg( res, i, msg );
				}
			}
		} while ( msg.getCalculatedByteCount() < MIN_MSG_SIZE );
		
		return true;
		
	}
	
	public boolean reset() {
		if ( res == null ) return false;
		try {
			return res.reset();
		} catch (MorseException e) {
			return false;
		}
	}

}
