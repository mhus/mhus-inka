<table cellpadding="0" cellspacing="0" width="{v:width_p}" height="{v:height_p}"><tr><td valign=top align=left>
<table border="0" cellpadding="0" cellspacing="0"><%
if (true) {
		de.mhu.shore.ifc.Tree shoreTree = (de.mhu.shore.ifc.Tree)new {v:class}();
		shoreTree.setPage( {v:pageVariable} );
		shoreTree.setName( "{v:name_p}" );
		shoreTree.setImagePath( "{v:images_p}" );
		de.mhu.shore.ifc.tree.Document shoreTreeDoc = shoreTree.getDocument( request, response );
		if ( shoreTreeDoc != null ) {
  
          // events
  		  String p = request.getParameter( "shoreTree_{v:name_p}_exp" );
  		  if ( p != null )
  		    shoreTreeDoc.expandEvent( p );
  		  p = request.getParameter( "shoreTree_{v:name_p}_click" );
  		  if ( p != null )
  		    shoreTreeDoc.clickEvent( p );
  		  p = request.getParameter( "shoreTree_{v:name_p}_imgclick" );
  		  if ( p != null )
  		    shoreTreeDoc.imgClickEvent( p );
  		    
  		  String r = "";
  		  String oldR = request.getParameter( "shoreTree_rnd" );
  		  while  ( ( r = "" + Math.random() ).equals( oldR ) ) {};
  		  r = "&shoreTree_rnd=" + r;
  		      
  		  // reload option
  		  if ( shoreTreeDoc.getReloadPage() ) {
  		  	out.println( "<script language=\"JavaScript\">" );
  		  	out.println( "window.location= \"{v:file_url}\";" );
  		  	out.println( "</script>" );
  		  	return;
  		  }

  		  // show titles
  		  out.print( "<tr>" );
  		  out.print( "<td><div style=\"{v:title.style_pp}\">&nbsp;&nbsp;</td>" );
  		  out.print( "<td width=\"{v:tree.width_pp}\" nowrap=\"yes\"><nobr><div style=\"{v:title.style_pp}\">" );
  		  out.print( "{v:title_pw}" );
  		  out.print( "</div></nobr></td>" );
  		  
  		  <t:table.titles>
  		  out.print( "<td><div style=\"{v:title.style_pp}\">&nbsp;&nbsp;</td>" );
  		  out.print( "<td width=\"{v:width_pp}\" nowrap=\"yes\"><nobr><div style=\"{v:title.style_pp}\">" );
  		  out.print( "{v:title_pw}" );
  		  out.print( "</div></nobr></td>" );
          </t:table.titles>
          out.print( "<td><div style=\"{v:title.style_pp}\">&nbsp;&nbsp;</td>" );
          
          out.println( "</tr>" );
  		      
  		  // show tree
  		  boolean odd = false;
		  while ( shoreTreeDoc.next() ) {
			de.mhu.shore.ifc.tree.Node shoreNode = shoreTreeDoc.current();
			odd = (! odd );
			String shoreColor = null;
			if ( odd )
			  shoreColor = "bgcolor={v:bgcolor1_p}";
			else
			  shoreColor = "bgcolor={v:bgcolor2_p}";
            
            out.print( "<tr>" );
            
            out.print( "<td " + shoreColor + "><div style=\"{v:style_pp}\">&nbsp;&nbsp;</td>" );
            
			out.print( "<td " + shoreColor + " valign=center align=left noWrap=yes>" );
			out.print( "<a name=\"shoreTree_{v:name_pp}_" + shoreTreeDoc.getNodeReference() + "\">" );

			if ( shoreNode.isSelected() )
			  out.print( "<div style=\"{v:selected.style_pp}\">" );
			else
			  out.print( "<div style=\"{v:style_pp}\">" );
			  
			out.print( "<nobr>" );
      
			for ( int i = 0; i < shoreTreeDoc.getLevels(); i++ ) {
				
				if ( i == shoreTreeDoc.getLevels()-1 && ! shoreNode.isLeaf() ) {
					out.print( "<a href=\"{v:file_url}?shoreTree_{v:name_url}_exp=" + shoreTreeDoc.getNodeReference() + r + "#shoreTree_{v:name_url}_" + shoreTreeDoc.getNodeReference() + "\">" );
				}
				
				
				String l = shoreTreeDoc.getLevelAt( i );
				out.print( "<img src=\"" + shoreNode.getImage( l ) + "\" border=0 vspace=0 hspace=0 >" );
				
				
				if ( i == shoreTreeDoc.getLevels()-1 && ! shoreNode.isLeaf() ) {
					out.print( "</a>" );
				}

			}
      
      		if ( shoreNode.isLeaf() ) {
				out.print( "<img src=\"" + shoreNode.getImage( de.mhu.shore.ifc.tree.Document.LEAF ) + "\" border=0 vspace=0 hspace=0 >" );
      		} else {
				out.print( "<a href=\"{v:file_url}?shoreTree_{v:name_url}_exp=" + shoreTreeDoc.getNodeReference() + r + "#shoreTree_{v:name_url}_" + shoreTreeDoc.getNodeReference() + "\">" );
				if ( shoreNode.isOpen() )
					out.print( "<img src=\"" + shoreNode.getImage( de.mhu.shore.ifc.tree.Document.FOLDER_OPEN ) + "\" border=0 vspace=0 hspace=0 >" );
				else
					out.print( "<img src=\"" + shoreNode.getImage( de.mhu.shore.ifc.tree.Document.FOLDER_CLOSED ) + "\" border=0 vspace=0 hspace=0 >" );
				out.print( "</a>" );
      		}
      				
			String extraImage = shoreNode.getImage( de.mhu.shore.ifc.tree.Document.EXTRA );
			if ( extraImage != null ) {
  			  if ( shoreNode.isClickableImage() )
				out.print( "<a href=\"{v:file_url}?shoreTree_{v:name_url}_imgclick=" + shoreTreeDoc.getNodeReference() + r + "#shoreTree_{v:name_url}_" + shoreTreeDoc.getNodeReference() + "\">" );
			  out.print( "<img src=\"" + extraImage + "\" border=0 vspace=0 hspace=0 > " );
			  if ( shoreNode.isClickableImage() )
			    out.print( "</a>" );
			}
			  
			if ( shoreNode.isClickable() )
				out.print( "<a href=\"{v:file_url}?shoreTree_{v:name_url}_click=" + shoreTreeDoc.getNodeReference() + r + "#shoreTree_{v:name_url}_" + shoreTreeDoc.getNodeReference() + "\">" );

			out.print( shoreNode.getTitle() );
			if ( shoreNode.isClickable() )
				out.print( "</a>" );
			
			out.print( "</nobr></div></td>" );

			<t:table.values>
			out.print( "<td " + shoreColor + "><div style=\"{v:style_pp}\">&nbsp;&nbsp;</td>" );
			
			out.print( "<td " + shoreColor + " nowrap=\"yes\" ><nobr>" );
			
			if ( shoreNode.isSelected() )
			  out.print( "<div style=\"{v:selected.style_pp}\">" );
			else
			  out.print( "<div style=\"{v:style_pp}\">" );
			
			out.print( shoreNode.getValue( "{v:name_pw}" ) );
			out.print( "</div></nobr></td>" );
			</t:table.values>
			
			out.print( "<td " + shoreColor + "><div style=\"{v:style_pp}\">&nbsp;&nbsp;</td>" );
			
			out.print( "</tr>\n" );
        
		  }
     
		}
}
%></table></td></tr></table>