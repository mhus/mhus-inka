package de.mhu.com.morse.channel;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import de.mhu.lib.ASql;
import de.mhu.lib.log.AL;
import de.mhu.com.morse.cache.ContentCache;
import de.mhu.com.morse.cache.MemoryCache;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.mql.Query;
import de.mhu.com.morse.obj.IFunctionConfig;
import de.mhu.com.morse.obj.ITableRead;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.utils.ObjectUtil;
import de.mhu.com.morse.pack.mc.CMc;

public class Server implements IServer {
	
	private static AL log = new AL(Server.class);
	private static MemoryCache<String, Object[]> functionCache = new MemoryCache<String, Object[]>( "functions", 60*60*1000, true );

	public synchronized Object loadFunction( IConnection con, String name ) throws MorseException {
		
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		String clazz = null;
		Map<String,String> config = null;
		String accessAcl = null;
		
		Object[] obj = functionCache.get( name );
		
		if ( obj == null ) {
		
			IQueryResult rFunction = null;
			String jar = null;
			try {
				Query qFunction = new Query( con, "SELECT ** FROM m_function WHERE name='" + ASql.escape( name ) + "' @sys" );
				rFunction = qFunction.execute();
				if ( ! rFunction.next() ) {
					rFunction.close();
					// throw new MorseException( MorseException.FUNCTION_NOT_FOUND, name );
					functionCache.put( name, new Object[0] );
					return null;
				}
				jar = rFunction.getString( "jar" );
				clazz = rFunction.getString( "entry_class" );
				config = ObjectUtil.tableToMap( rFunction.getTable( "features" ), "k", "v" );
				accessAcl = rFunction.getString( "access_acl" );
			} finally {
				if ( rFunction != null )
					rFunction.close();
			}
			
			if ( ObjectUtil.validateId( jar ) ) {
				LinkedList<File> files = new LinkedList<File>();
				loadJar( con, name, jar, files );
				URL[] urls = new URL[ files.size() ];
				int cnt = 0;
				for ( Iterator<File> i = files.iterator(); i.hasNext(); )
					try {
						urls[ cnt++ ] = new URL( "file:" + i.next().getAbsolutePath() );
					} catch (MalformedURLException e) {
						log.error( e );
						throw new MorseException( MorseException.FUNCTION_NOT_FOUND, name );
					}
				loader = new URLClassLoader( urls );
			}
		
			obj = new Object[] { loader, clazz, config, accessAcl };
			functionCache.put( name, obj );
			
		} else {
			if ( obj.length == 0 )
				return null;
			
			loader = (ClassLoader)obj[0];
			clazz  = (String)obj[1];
			config = (Map<String,String>)obj[2];
			accessAcl = (String)obj[3];
		}
		
		try {
			Object o = loader.loadClass( clazz ).newInstance();
			if ( o instanceof IFunctionConfig )
				((IFunctionConfig)o).initFunction( config, accessAcl, name );
			
			return o;
			
		} catch (Exception e) {
			log.error( e );
			throw new MorseException( MorseException.FUNCTION_NOT_FOUND, name );
		}
		
	}
	
	private static void loadJar( IConnection con, String name, String jar, LinkedList<File> files ) throws MorseException {
		
		Query qJar = new Query( con, "FETCH " + jar );
		IQueryResult rJar = qJar.execute();
		if ( ! rJar.next() ) {
			rJar.close();
			throw new MorseException( MorseException.JAR_NOT_FOUND, new String[] { jar, name } );
		}
		
		File f = ContentCache.getInstance().getFile( con, rJar.getString( IAttribute.MC_CONTENT ), CMc.DEFAULT_RENDITION );
		if ( files.contains( f ) ) {
			rJar.close();
			return;
		}
		
		files.add( f );
		
		ITableRead links = rJar.getTable( "links" );
		while ( links.next() ) {
			loadJar( con, name, links.getString( "id" ), files );
		}
		
		rJar.close();
		
	}
	
}
