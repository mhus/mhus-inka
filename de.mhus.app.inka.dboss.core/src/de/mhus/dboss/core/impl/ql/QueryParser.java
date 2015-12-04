package de.mhus.dboss.core.impl.ql;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import de.mhus.dboss.core.DBossException;

public class QueryParser {

	private boolean DEBUG = false;
	
	private static Logger log = Logger.getLogger( QueryParser.class );
	private QDNode node;
	public Hashtable globalDefinitions = new Hashtable();
	private IQueryDefinition qd;

	public QueryParser( IQueryDefinition pQd ) {
		
		qd = pQd;
		
		node =  new QDNode();
		
		node.parseDefinition( qd.getQueryDefinition( null ).split( " " ), 0 );
				
	}

	public ICompiledQuery compile(String[] parts, int offset, int end) throws DBossException {

		CompiledQuery code = new CompiledQuery();
		
		if ( parts[ end-1 ].startsWith( "enable:" ) ) {
			code.setFeatures( parts[ end-1 ].substring( 7 ).split( "," ) );
			end--;
			if ( code.isFeature( "debug_parser" ) )
				DEBUG = true;
			if ( DEBUG ) {
				String[] f = code.getFeatures();
				if ( log.isDebugEnabled() )
					for ( int i = 0; i < f.length; i++ )
						log.info( "FEATURE: " + f[ i ] );
			}
		}
		
		int start = offset;
		offset = node.compile( parts, offset, end, code );
		
		if ( offset != end ) throw new DBossException( "PARSE_ERROR " + toErrorMessage( parts, start, end, offset ) ); 
		
		if ( DEBUG )
			log.info( "CODE" + toString() );
		return code;
	}
	
	private String toErrorMessage(String[] parts, int start, int end, int offset) {
		StringBuffer out = new StringBuffer( "ERROR: " );
		for ( int i = start; i < end; i++ ) {
			if ( i == offset )
				out.append( "--> " );
			out.append( parts[ i ] );
			out.append( ' ' );
		}
		out.append( "<--" );
		return out.toString();
	}

	private class QDNode {
	
		LinkedList definition = new LinkedList();
		
		int parseDefinition( String[] def, int offset ) {
			
			for ( int i = offset; i < def.length; i++ ) {
				
				String part = def[i];
				
				if ( part.startsWith("<" ) && part.endsWith( ">" ) ) {
					// is other definition
					definition.add( new QDRef( part ) );
				} else
				if ( part.endsWith( "[" ) ) {
					// start of choice
					QDChoice choice = new QDChoice( part );
					i = choice.parse( def, i );
					definition.add( choice );
				} else
				if ( "]".equals( part ) ) {
					return -i;
				} else
				if ( "|".equals( part ) ) {
					return i;
				} else
				if ( part.startsWith( "?" ) ) {
					// attribute
					definition.add( new QDAttr( part ) );
				} else
					definition.add( new QDConst( part ) );
					
			}
			
			return -def.length;
			
		}
		
		public String toString() {
			StringBuffer sb = new StringBuffer();
			for ( Iterator i = definition.iterator(); i.hasNext(); ) {
				QDObj obj = (QDObj)i.next();
				sb.append( obj.toString() );
				sb.append( ' ' );
			}
			return sb.toString();
		}

		public int compile(String[] parts, int offset, int end, CompiledQuery code) {
			
			for ( Iterator i = definition.iterator(); i.hasNext(); ) {
				
				QDObj obj = (QDObj)i.next();
				if ( DEBUG && offset < end ) log.info( space( offset ) + ">>> NODE " + offset  + ' ' + parts[ offset ] + " -------------------");
				int ret = obj.compile( parts, offset, end, code );
				if ( DEBUG && offset < end ) log.info( space( offset ) + "<<< NODE " + offset  + ' ' + parts[ offset ] + " -------------------");
				
				if ( ret < 0 ) return ret;
				
				offset = ret;
				
			}
			
			return offset;
		}

	}
	
	private String space( int l ) {
		StringBuffer sb = new StringBuffer();
		for ( int i =0; i < l ; i++ )
			sb.append( "  " );
		return sb.toString();
	}
	
	private interface QDObj {
		
		public int compile(String[] parts, int offset, int end, CompiledQuery code);
	}
	
	private class QDConst implements QDObj {

		private String value;
		private int id;

		public QDConst(String part) {
			int pos = part.lastIndexOf( '|' );
			if ( pos < 0 ) {
				value = part.toUpperCase();
				id = qd.getConstantId( part );
			} else {
				value = part.substring( 0, pos );
				id = Integer.parseInt( part.substring( pos+1 ) );
			}
		}

		public String toString() {
			return value + '|' + id;
		}

		public int compile(String[] parts, int offset, int end, CompiledQuery code) {
			if ( DEBUG ) log.info( space( offset ) + "--- CONST " + value );
			
			if ( offset >= end ) return -1;
			
			if ( parts[ offset ].toUpperCase().equals( value ) ) {
				code.add( id );
				return offset+1;
			}
			
			return -1;
			
		}
		
	}
	
	private class QDRef implements QDObj {

	private String name;
		private QDNode myNode;

		public QDRef(String part) {
			name = part.substring( 1, part.length()-1 );
			if ( !globalDefinitions.containsKey( name ) ) {
				globalDefinitions.put( name, "" );
				QDNode newNode = new QDNode();
				newNode.parseDefinition( qd.getQueryDefinition( name ).split( " " ), 0 );
				globalDefinitions.put( name, newNode );
			}
			// myNode = (QDNode)globalDefinitions.get( name );
		}
		
		public String toString() {
			return '<' + name + '>';
		}

		public int compile(String[] parts, int offset, int end, CompiledQuery code) {
			
			if ( DEBUG ) log.info( space( offset ) + "--- REF " + name );
			
			if ( myNode == null )
				myNode = (QDNode)globalDefinitions.get( name );
			
			return myNode.compile( parts, offset, end, code );
		}
	}
	
	private class QDChoice implements QDObj {

		private LinkedList choices = new LinkedList();
		private boolean maybe = false;
		private boolean multi = false;
		
		public QDChoice(String part) {
			maybe = part.equals( "0,1[" );
			if ( part.equals( "*[") ) {
				maybe = true;
				multi = true;
			}
		}

		public int parse(String[] def, int i) {
			while ( true ) {
				QDNode newNode = new QDNode();
				int j = newNode.parseDefinition( def, i+1 );
				choices.add( newNode );
				if ( j < 0 ) {
					i = -j;
					break;
				}
				i = j;
			}
			return i;
		}
		
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append( "[ " );
			boolean first = true;
			for ( Iterator i = choices.iterator(); i.hasNext(); ) {
				if ( !first ) sb.append( "| " );
				first = false;
				QDNode n = (QDNode)i.next();
				sb.append( n.toString() );
			}
			sb.append( ']' );
			return sb.toString();
		}

		public int compile(String[] parts, int offset, int end, CompiledQuery code) {
			
			if ( DEBUG ) log.info(space( offset ) + "--- CHOICE " + choices );
			
			while ( true ) {
			
				if ( offset >= end ) return offset;
				
				for ( Iterator i = choices.iterator(); i.hasNext(); ) {
					
					QDNode n = (QDNode)i.next();
					CompiledQuery code2 = new CompiledQuery();
					int ret = n.compile( parts, offset, end, code2 );
					if ( ret >= 0 ) {
						code.addAll( code2 );
						if ( DEBUG ) log.info(space( offset ) + "+++ CHOICE OK " + ret + ' ' + n.toString() );
						if ( !multi )
							return ret;
						offset = ret;
						continue;
					}
					
				}
				
				if ( choices.size() == 1 || maybe ) {
					if ( DEBUG ) log.info(space( offset ) + "--- CHOICE NONE" );
					return offset;
				}
	
				if ( DEBUG ) log.info(space( offset ) + "*** CHOICE ERROR" );
				return -1;

			}
			
		}
	}

	private class QDAttr implements QDObj {

		private String ext;

		public QDAttr(String part) {
			ext = part.substring( 1 );
		}
		
		public String toString() {
			return "?" + ext;
		}

		public int compile(String[] parts, int offset, int end, CompiledQuery code) {
			if ( DEBUG ) log.info( space( offset ) + "+++ ATTR " + parts[ offset ] );
			code.add( parts[ offset ] );
			return offset+1;
		}
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append( ':' );
		sb.append( node.toString() );
		for ( Iterator i = globalDefinitions.keySet().iterator(); i.hasNext(); ) {
			sb.append( "\n" );
			String name = (String)i.next();
			QDNode n = (QDNode)globalDefinitions.get( name );
			sb.append( name );
			sb.append( ": " );
			sb.append( n.toString() );
		}
		return sb.toString();
	}
	
}
