/*
 *
 * Date: 11:29:37 29.07.2003
 * Author hummel
 * 
 * Copyright: Virtueller Campus Bayern GmbH (c) 2003
 * 
 * 
 * 
 */
package de.mhu.shore.plugins;

import java.util.Enumeration;
import java.util.Vector;

import org.javaby.jbyte.Template;

import de.mhu.lib.MhuFile;
import de.mhu.shore.Plugin;
import de.mhu.shore.PluginHelper;
import de.mhu.shore.ShoreUtil;

/**
 * @author hummel
 * 

 * 
 */
public class ShoreInclude implements Plugin {

	private static final String INCLUDE_NAME ="name";
	private static final String INCLUDE_TAG = "include";
	private static final String INCLUDE_TAG_END = "/include";
	private static final String HEADER = "_header.tpl";
	private static final String FOOTER = "_footer.tpl";
	private static final String ERROR_TEXT = "<table border=1><tr><td bgcolor=black><font color=red>Include Error</font></td></tr></table>";
	
	private static final String SHORE_PARAM_BASE = "include.base";
	private static final String PARAM_BASE = "base";

	private MhuFile base = null;
	private Vector footerCache = new Vector(); 

	public void execute(PluginHelper _helper) throws Exception {

		String b = _helper.getParameter( PARAM_BASE );
		if ( b == null ) b = _helper.getShoreParameter( SHORE_PARAM_BASE );
		if ( b == null ) {
			System.err.println(  "Include: base not set, use current dir");
			base = new MhuFile( _helper.getSourceDir() );
		} else
			base = new MhuFile( b );

		boolean worked = false; 
		do {
			worked = false;
			_helper.resetTag();
			
			while ( _helper.nextTag() ) {
				
				String name = _helper.getTagName();
				
				if ( name.equals( INCLUDE_TAG )) {
					parseIncludeInit( _helper );
					worked = true;
				} else
				if ( name.equals( INCLUDE_TAG_END )) {
					parseIncludeFinish( _helper );
					worked = true;
				}
				
			}
			
			if ( footerCache.size() != 0 ) {
				System.err.println( "Include: <" + INCLUDE_TAG + "> without <" + INCLUDE_TAG_END + ">" );
				footerCache.removeAllElements();
			}
			
		} while ( worked == true );
		
	}

	private void parseIncludeFinish(PluginHelper _helper) {

		if ( footerCache.size() == 0 ) {
			System.err.println( "Include: <" + INCLUDE_TAG_END + "> without <" + INCLUDE_TAG + ">" );
			_helper.replaceTag( ERROR_TEXT );
			return;
		}
		
		_helper.replaceTag( (String)footerCache.elementAt( 0 ) );
		footerCache.removeElementAt( 0 ); 
		
	}

	private void parseIncludeInit(PluginHelper _helper) {
		
		String name = (String)_helper.getTagParameter().get( INCLUDE_NAME );
		if ( name == null ) {
			System.err.println( "Include: without name" );
			_helper.replaceTag( ERROR_TEXT );
			return;
		}
		
		MhuFile headerFile = new MhuFile( base, name + HEADER);
		MhuFile footerFile = new MhuFile( base, name + FOOTER );
		
		if ( ! headerFile.exists() ) {
			System.err.println( "Include: File " + headerFile + " not found!" );
			_helper.replaceTag( ERROR_TEXT );
			return;
		}
		if ( ! footerFile.exists() ) {
			System.err.println( "Include: File " + footerFile + " not found!" );
			_helper.replaceTag( ERROR_TEXT );
			return;
		}
		
		System.out.println( "Include: " + name );
		
		try {
			Template header = new Template( headerFile.getAbsolutePath() );
			Template footer   = new Template( footerFile.getAbsolutePath() );
			
			for ( Enumeration i = _helper.getTagParameter().keys(); i.hasMoreElements(); ) {
				String key = (String)i.nextElement(); 
				ShoreUtil.addTemplateParameter( header, key, (String)_helper.getTagParameter().get( key ) );
				ShoreUtil.addTemplateParameter( footer, key, (String)_helper.getTagParameter().get( key ) );
			}
			for ( Enumeration i = _helper.getParameterKeys(); i.hasMoreElements() ; ) {
				String key = (String)i.nextElement(); 
				ShoreUtil.addTemplateParameter( header, key, (String)_helper.getParameter( key ) );
				ShoreUtil.addTemplateParameter( footer, key, (String)_helper.getParameter( key ) );
			}
			
			_helper.replaceTag( header.toString() );
			footerCache.insertElementAt( footer.toString(), 0 );
			
		} catch ( Exception e ) {
			System.err.println( "Include: " + e );
			_helper.replaceTag( ERROR_TEXT );
			return;
		}
		
	}

}
