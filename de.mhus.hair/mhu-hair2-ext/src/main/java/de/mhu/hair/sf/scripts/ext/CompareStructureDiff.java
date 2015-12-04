package de.mhu.hair.sf.scripts.ext;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import de.mhu.lib.ATimekeeper;

public class CompareStructureDiff<E> {
	
	public void compare( TreeMap<String,E> current, TreeMap<String,E> last, Listener<E> listener ) {
		
		listener.start( current, last );
		
		Iterator<Map.Entry<String, E>> cur = current.entrySet().iterator();
		Iterator<Map.Entry<String, E>> old = last.entrySet().iterator();
		
		Map.Entry<String, E> curEntry = null;
		Map.Entry<String, E> oldEntry = null;
		
		String      curKey = null;
		E curVal = null;
		
		String        oldKey = null;
		E  oldVal = null;
		
		if ( cur.hasNext() ) {
			curEntry = cur.next();
			curKey   = curEntry.getKey();
			curVal   = curEntry.getValue();
			// if ( config.isTrace9() ) config.out.println( "CUR: " + curKey );
		}
		
		if ( old.hasNext() ) {
			oldEntry = old.next();
			oldKey   = oldEntry.getKey();
			oldVal   = oldEntry.getValue();
			// if ( config.isTrace9() ) config.out.println( "OLD: " + oldKey );
		}
		
		while ( true ) {
			
			if ( curEntry == null && oldEntry == null )
				break;
			
			
			
			int comp = 0;
			
			if ( curEntry != null && oldEntry != null )
				comp = curKey.compareTo( oldKey );
			else
			if ( curEntry == null )
				comp = 1;
			else
				comp = -1;
				
			if ( comp == 0 ) {
				
				// found same check id and vstamp
				if ( ! listener.equalsMapValues(curVal,oldVal) ) {					
					listener.updateObject( curKey, curVal, oldVal );
				}
				
				
				if ( cur != null && cur.hasNext() ) {
					curEntry = cur.next();
					curKey   = curEntry.getKey();
					curVal   = curEntry.getValue();
					// if ( config.isTrace9() ) config.out.println( "CUR: " + curKey );
				} else
					curEntry = null;
				
				if ( old != null && old.hasNext() ) {
					oldEntry = old.next();
					oldKey   = oldEntry.getKey();
					oldVal   = oldEntry.getValue();
					// if ( config.isTrace9() ) config.out.println( "OLD: " + oldKey );
				} else
					oldEntry = null;
				
				continue;
				
			} else
			
			if ( comp > 0 ) {
				
				// old has new one (deleted in cur), check by listener
				listener.deleteObject( oldKey, oldVal );
				
				if ( old != null && old.hasNext() ) {
					oldEntry = old.next();
					oldKey   = oldEntry.getKey();
					oldVal   = oldEntry.getValue();
					// if ( config.isTrace9() ) config.out.println( "OLD: " + oldKey );
				} else
					oldEntry = null;
				
				
			} else {
				// comp < 0
				
				// cur has new one, check by listener
				listener.createObject( curKey, curVal );
				 
				if ( cur != null && cur.hasNext() ) {
					curEntry = cur.next();
					curKey   = curEntry.getKey();
					curVal   = curEntry.getValue();
					// if ( config.isTrace8() ) config.out.println( "CUR: " + curKey );
				} else
					curEntry = null;
				
			}
			

			
		}
		
		// if ( config.isTrace1() ) config.out.println( "CMP: FINISH");
		listener.finish( current, last );
		
	}
	
	public static interface Listener<E> {

		public boolean equalsMapValues(E curVal, E oldVal);

		public void start( TreeMap<String, E> pCurrent, TreeMap<String, E> pLast );
		
		public void finish( TreeMap<String, E> pCurrent, TreeMap<String, E> pLast );
		
		public void updateObject( String path, E curVal, E lastVal );

		public void createObject( String path, E curVal);

		public void deleteObject( String path, E lastVal);
		
	}
}
