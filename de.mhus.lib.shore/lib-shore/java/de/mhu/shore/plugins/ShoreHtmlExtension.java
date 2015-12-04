/*
 *  mhu-shore JSP creation Framework
 *    shore help you to create JSP files using MVC (Model-View-Controll) design.
 *    mhu-shore is a ant task and generate JSP files from - nearly - simple
 *    HTML files. A special Servlet or active server component is not needed.
 * 
 *  Copyright (C) 2003  Mike Hummel
 *  
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *  
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *  
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  WWW: http://code.mikehummel.de/
 *  E-mail: code@mikehummel.de
 */

package de.mhu.shore.plugins;

import java.util.Hashtable;

import de.mhu.shore.*;

/**
 * @author hummel
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ShoreHtmlExtension implements Plugin {

	public static final String IMG_TAG    = "img";
	public static final String BUTTON_TAG = "button";
	public static final String HREF       = "href";
	public static final String TARGET     = "target";
	public static final String ON_CLICK   = "onclick";
	public static final String ON_CLICK_IMG   = "onclickimg";
	public static final String ANCHOR_TAG = "a";
	public static final String ANCHOR_TAG_END = "/a";
	public static final String INIT_SWAP_COLOR_TAG = "html.swapcolors.init";
	public static final String SWAP_COLOR_TAG = "html.swapcolors";
	public static final String FLUSH_TAG = "html.flush";


	private PluginHelper helper      = null;
	private int          colorSwapId = -1;
	
	/* (non-Javadoc)
	 * @see de.mhu.shore.Plugin#execute(java.lang.String, java.util.Hashtable)
	 */
	public void execute( PluginHelper _helper ) throws Exception {
		
		helper = _helper;
		
		helper.resetTag();
		while ( helper.nextTag() ) {
			
			String name = helper.getTagName();
			
			if ( name.equals( IMG_TAG ))
				parseHref();
			else
			if ( name.equals( BUTTON_TAG ))
				parseButton();
			else
			if ( name.equals( FLUSH_TAG ))
				parseFlush();
			else
			if ( name.equals( SWAP_COLOR_TAG ))
				parseSwapColor();
			else
			if ( name.equals( INIT_SWAP_COLOR_TAG ))
				parseInitSwapColor();
		}
		
	}


	private void parseInitSwapColor() {
		int id = helper.getUniqueId();
		colorSwapId = id;
		
		helper.replaceTag( "<% boolean shore_html_extension_swap_color_state_" + id + "=true;" +			"String shore_html_extension_swap_color_1_" + id + "=\"" + helper.getTagParameter().get( "color1") + "\";" +			"String shore_html_extension_swap_color_2_" + id + "=\"" + helper.getTagParameter().get( "color2") + "\";\n" ); 
	}
	
	private void parseSwapColor() {
		helper.replaceTag( "<% if ( shore_html_extension_swap_color_state_" + colorSwapId + ") out.print( shore_html_extension_swap_color_1_" + colorSwapId + ");" +			"else out.print( shore_html_extension_swap_color_2_" + colorSwapId + " );\n" +			"shore_html_extension_swap_color_state_" + colorSwapId + "= !shore_html_extension_swap_color_state_" + colorSwapId+";\n%>" );
	}
	
	private void parseFlush() {
		helper.replaceTag( "<%out.flush();%>" );
	}
	
	/**
	 * 
	 */
	private boolean parseButton() {

		String href = (String)helper.getTagParameter().get( HREF );
		
		if ( href == null )
			return false;
			
		// remove parameter
		helper.getTagParameter().remove( HREF );

		// add javascript
		helper.getTagParameter().put( ON_CLICK, "window.location='" + href + "';" );
		
		helper.replaceTag();
		
		return true;
	}
	

	/**
	 * 
	 */
	private boolean parseHref() {
		
		String href = (String)helper.getTagParameter().get( HREF );
		String onClickImg = (String)helper.getTagParameter().get( ON_CLICK_IMG );
		
		if ( href == null )
			return false;
			
		String target = (String)helper.getTagParameter().get( TARGET );
		
		// remove parameter
		helper.getTagParameter().remove( HREF );
		helper.getTagParameter().remove( TARGET );
		helper.getTagParameter().remove( ON_CLICK_IMG );
		
		// new tag ....
		Hashtable h = new Hashtable();
		h.put( HREF, href );
		if ( target != null ) h.put( TARGET, target );
		if ( onClickImg != null ) h.put( ON_CLICK, "this.src='" + onClickImg + "';" );
		
		
		String newTag = ShoreUtil.createHtmlTag( ANCHOR_TAG, h );
		
		newTag+= ShoreUtil.createHtmlTag( helper.getTagName(), helper.getTagParameter() );
		
		newTag+= ShoreUtil.createHtmlTag( ANCHOR_TAG_END, new Hashtable() );
		
		helper.replaceTag( newTag );
		
		return true;
	}
	
}
