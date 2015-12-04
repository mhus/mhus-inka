/*
 *
 * Date: 16:45:54 12.05.2003
 * Author hummel
 * 
 * Copyright: Virtueller Campus Bayern GmbH (c) 2003
 * 
 * 
 * 
 */
package de.mhu.test.testtree;

import java.io.PrintWriter;
import java.io.StringWriter;

import de.mhu.lib.MhuFile;
import de.mhu.shore.ifc.tree.Document;

/**
 * @author hummel
 * 

 * 
 */
public class test {

	public static void main(String[] args) {
		
		MhuFile file = new MhuFile( "out.html" );

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter( writer );  

		out.println( "<table border=0 cellpadding=0 cellspacing=0>" );
		//*************************************************************************

		de.mhu.shore.ifc.Tree shoreTree = (de.mhu.shore.ifc.Tree)new PageTree();
		shoreTree.setName( "test" );
		shoreTree.setImagePath( "images" );
		de.mhu.shore.ifc.tree.Document shoreTreeDoc = shoreTree.getDocument( null, null );
		if ( shoreTreeDoc != null ) {
  
		  while ( shoreTreeDoc.next() ) {
			de.mhu.shore.ifc.tree.Node shoreNode = shoreTreeDoc.current();
      
			out.print( "<tr><td valign=center align=left><nobr>" );
      
			for ( int i = 0; i < shoreTreeDoc.getLevels(); i++ ) {
				
				if ( i == shoreTreeDoc.getLevels()-1 && ! shoreNode.isLeaf() ) {
					out.print( "<a href=\"?exp=" + shoreTreeDoc.getNodeReference() + "\">" );
				}
				
				
				String l = shoreTreeDoc.getLevelAt( i );
				out.print( "<img src=\"" + shoreNode.getImage( l ) + "\" border=0 vspace=0 hspace=0 >" );
				
				
				if ( i == shoreTreeDoc.getLevels()-1 && ! shoreNode.isLeaf() ) {
					out.print( "</a>" );
				}

			}
      
      		if ( shoreNode.isLeaf() ) {
				out.print( "<img src=\"" + shoreNode.getImage( Document.LEAF ) + "\" border=0 vspace=0 hspace=0 >" );
      		} else {
				out.print( "<a href=\"?exp=" + shoreTreeDoc.getNodeReference() + "\">" );
				if ( shoreNode.isOpen() )
					out.print( "<img src=\"" + shoreNode.getImage( Document.FOLDER_OPEN ) + "\" border=0 vspace=0 hspace=0 >" );
				else
					out.print( "<img src=\"" + shoreNode.getImage( Document.FOLDER_CLOSED ) + "\" border=0 vspace=0 hspace=0 >" );
				out.print( "</a>" );
      		}
      
			out.print( "<font size=1> " );
			if ( shoreNode.isClickable() )
				out.print( "<a href=\"?click=" + shoreTreeDoc.getNodeReference() + "\">" );
			out.print( shoreNode.getTitle() );
			if ( shoreNode.isClickable() )
				out.print( "</a>" );
			out.print( "</font>" );
			
			out.print( "</nobr></td></tr>" );
        
		  }
     
		}
		
		//*********************************************************************
		out.println( "</table>" );
		
		try {
		
			file.write( writer.toString() );
			
		} catch ( Exception e) {
			e.printStackTrace();
		}
			
	}
}
