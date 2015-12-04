package de.mhu.com.morse.channel.init;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import de.mhu.lib.config.Config;
import de.mhu.lib.config.ConfigManager;
import de.mhu.lib.log.AL;
import de.mhu.lib.utils.Properties;

public class InitStoreManager {
		
	private static Config config = ConfigManager.getConfig( "server" );
	private static AL log = new AL( InitStoreManager.class );
	
	private Hashtable<String, LinkedList<String>> mapping = new Hashtable<String, LinkedList<String>>();
	private Hashtable<String, Properties> store = new Hashtable<String, Properties>();
	
	public InitStoreManager() {
		
		try {
			String[] schemas = config.getProperty( "init.schemas" ).split( "," );
			for ( String schema : schemas ) {
				String clazz = config.getProperty( "init.schema." + schema + ".class");
				if ( log.t5() ) log.info( "Schema " + schema + ' ' + clazz );
				ILoader loader = (ILoader)Class.forName( clazz ).newInstance();
				loader.init( schema );
				
				LinkedList<String> types = loader.getTypes();
				for ( String type : types ) {
					
					LinkedList<String> typeMapping = mapping.get( type );
					if ( typeMapping == null ) {
						typeMapping = new LinkedList<String>();
						mapping.put( type, typeMapping );
					}
					
					LinkedList<String> ids = loader.getIds( type );
					for ( String id : ids ) {
						if ( log.t5() ) log.debug( "Load " + schema + ' ' + type + ' ' + id );
						Properties value = store.get( id );
						if ( value == null ) {
							value = new Properties();
							store.put( id, value );
						}
						loader.fill( type, id, value );
						
						if ( ! typeMapping.contains( id ) )
							typeMapping.add( id );
					}
					
				}
				
			}
		} catch ( Throwable t ) {
			log.error( t );
		}
	}
	
	public List<String> get(String table) {
		
		return mapping.get( table );
	}

	public Properties fetch(String id) {
		
		return store.get( id );
	}

}
