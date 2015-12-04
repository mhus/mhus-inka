package de.mhus.dboss.core.impl.ql;

import java.util.Iterator;
import java.util.LinkedList;

import org.apache.log4j.Logger;

public class CompiledQuery implements ICompiledQuery {

	private static Logger log = Logger.getLogger( CompiledQuery.class );
	LinkedList list = new LinkedList();
	protected String[] features;
	
	public void add(String string) {
		list.add( string );
	}

	public void add(int id) {
		list.add( new Integer( id ) );
	}

	public void addAll(CompiledQuery code2) {
		list.addAll( code2.list );
	}

	public void dump() {
		for ( Iterator i = list.iterator(); i.hasNext(); )
			log.info( "> " + i.next().toString() );

	}

	public int getInteger(int index ) {
		if ( index >= list.size() ) return CMql.NaN;
		Object obj = list.get( index );
		if ( ! ( obj instanceof Integer ) ) return CMql.NaN;
		return ((Integer)obj).intValue();
 	}

	public String getString(int index) {
		if ( index >= list.size() ) return CMql.NaS;
		Object obj = list.get( index );
		if ( ! ( obj instanceof String ) ) return CMql.NaS;
		return (String)obj;
	}

	public int size() {
		return list.size();
	}

	public void setFeatures(String[] strings) {
		features = strings;
	}

	public boolean isFeature( String in ) {
		if ( features == null ) return false;
		for ( int i = 0; i < features.length; i++ )
			if ( features[i].equals( in ) ) return true;
		return false;
	}

	public String[] getFeatures() {
		return features;
	}
	
}
