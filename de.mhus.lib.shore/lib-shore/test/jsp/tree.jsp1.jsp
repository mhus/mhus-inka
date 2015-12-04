<html>
<body bgcolor=#ffffff >
<table border="0" cellpadding="0" cellspacing="0">
<%
de.mhu.shore.ifc.Tree shoreTree = (de.mhu.shore.ifc.Tree)new de.mhu.test.web.ObjectTree();
shoreTree.setPage( null );
shoreTree.setName( "tree1" );
shoreTree.setImagePath( "/images/tree/" );


de.mhu.shore.ifc.tree.Document shoreTreeDoc = shoreTree.getDocument( request, response );
if ( shoreTreeDoc != null ) {

// events
String p = request.getParameter( "shoreTree_tree1_exp" );
if ( p != null )
shoreTreeDoc.expandEvent( p );
p = request.getParameter( "shoreTree_tree1_click" );
if ( p != null )
shoreTreeDoc.clickEvent( p );
p = request.getParameter( "shoreTree_tree1_imgclick" );
if ( p != null )
shoreTreeDoc.imgClickEvent( p );

String r = "";
String oldR = request.getParameter( "shoreTree_rnd" );
while  ( ( r = "" + Math.random() ).equals( oldR ) ) {};
r = "&shoreTree_rnd=" + r;

// reload option
if ( shoreTreeDoc.getReloadPage() ) {
out.println( "<script language=\"JavaScript\">" );
// out.println( "window.parent.location.reload();" );
out.println( "window.parent.location=\"tree.jsp\";" );
out.println( "</script>" );
return;
}

// show titles
out.print( "<tr>" );
out.print( "<td><div style=\"font-weight:bold FONT-SIZE: 14px; FONT-FAMILY: Verdana, Arial, Helvetica, sans-serif\">&nbsp;&nbsp;</td>" );
out.print( "<td width=\"400\" nowrap=\"yes\"><nobr><div style=\"font-weight:bold FONT-SIZE: 14px; FONT-FAMILY: Verdana, Arial, Helvetica, sans-serif\">" );
out.print( "Threads" );
out.print( "</div></nobr></td>" );

out.print( "<td><div style=\"font-weight:bold FONT-SIZE: 14px; FONT-FAMILY: Verdana, Arial, Helvetica, sans-serif\">&nbsp;&nbsp;</td>" );
out.print( "<td width=\"200\" nowrap=\"yes\"><nobr><div style=\"font-weight:bold FONT-SIZE: 14px; FONT-FAMILY: Verdana, Arial, Helvetica, sans-serif\">" );
out.print( "Inhalt" );
out.print( "</div></nobr></td>" );
out.print( "<td><div style=\"font-weight:bold FONT-SIZE: 14px; FONT-FAMILY: Verdana, Arial, Helvetica, sans-serif\">&nbsp;&nbsp;</td>" );
out.print( "<td width=\"200\" nowrap=\"yes\"><nobr><div style=\"font-weight:bold FONT-SIZE: 14px; FONT-FAMILY: Verdana, Arial, Helvetica, sans-serif\">" );
out.print( "R&uuml;ckgabe" );
out.print( "</div></nobr></td>" );
out.print( "<td><div style=\"font-weight:bold FONT-SIZE: 14px; FONT-FAMILY: Verdana, Arial, Helvetica, sans-serif\">&nbsp;&nbsp;</td>" );

out.println( "</tr>" );

// show tree
boolean odd = false;
while ( shoreTreeDoc.next() ) {
de.mhu.shore.ifc.tree.Node shoreNode = shoreTreeDoc.current();
odd = (! odd );
String shoreColor = null;
if ( odd )
shoreColor = "bgcolor=#eeeeee";
else
shoreColor = "bgcolor=#ffffff";

out.print( "<tr>" );

out.print( "<td " + shoreColor + "><div style=\"FONT-SIZE: 10px; FONT-FAMILY: Verdana, Arial, Helvetica, sans-serif\">&nbsp;&nbsp;</td>" );

out.print( "<td " + shoreColor + " valign=center align=left noWrap=yes>" );
out.print( "<a name=\"shoreTree_tree1_" + shoreTreeDoc.getNodeReference() + "\">" );

if ( shoreNode.isSelected() )
out.print( "<div style=\"font-weight:bold FONT-SIZE: 10px; FONT-FAMILY: Verdana, Arial, Helvetica, sans-serif\">" );
else
out.print( "<div style=\"FONT-SIZE: 10px; FONT-FAMILY: Verdana, Arial, Helvetica, sans-serif\">" );

out.print( "<nobr>" );

for ( int i = 0; i < shoreTreeDoc.getLevels(); i++ ) {

if ( i == shoreTreeDoc.getLevels()-1 && ! shoreNode.isLeaf() ) {
out.print( "<a style=\"FONT-SIZE: 10px; FONT-FAMILY: Verdana, Arial, Helvetica, sans-serif\" href=\"tree.jsp1.jsp?shoreTree_tree1_exp=" + shoreTreeDoc.getNodeReference() + r + "#shoreTree_tree1_" + shoreTreeDoc.getNodeReference() + "\">" );
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
out.print( "<a style=\"FONT-SIZE: 10px; FONT-FAMILY: Verdana, Arial, Helvetica, sans-serif\" href=\"tree.jsp1.jsp?shoreTree_tree1_exp=" + shoreTreeDoc.getNodeReference() + r + "#shoreTree_tree1_" + shoreTreeDoc.getNodeReference() + "\">" );
if ( shoreNode.isOpen() )
out.print( "<img src=\"" + shoreNode.getImage( de.mhu.shore.ifc.tree.Document.FOLDER_OPEN ) + "\" border=0 vspace=0 hspace=0 >" );
else
out.print( "<img src=\"" + shoreNode.getImage( de.mhu.shore.ifc.tree.Document.FOLDER_CLOSED ) + "\" border=0 vspace=0 hspace=0 >" );
out.print( "</a>" );
}

String extraImage = shoreNode.getImage( de.mhu.shore.ifc.tree.Document.EXTRA );
if ( extraImage != null ) {
if ( shoreNode.isClickableImage() )
out.print( "<a href=\"tree.jsp1.jsp?shoreTree_tree1_imgclick=" + shoreTreeDoc.getNodeReference() + r + "#shoreTree_tree1_" + shoreTreeDoc.getNodeReference() + "\">" );
out.print( "<img src=\"" + extraImage + "\" border=0 vspace=0 hspace=0 >" );
if ( shoreNode.isClickableImage() )
out.print( "</a>" );
}

if ( shoreNode.isClickable() )
out.print( "<a href=\"tree.jsp1.jsp?shoreTree_tree1_click=" + shoreTreeDoc.getNodeReference() + r + "#shoreTree_tree1_" + shoreTreeDoc.getNodeReference() + "\">" );

out.print( shoreNode.getTitle() );
if ( shoreNode.isClickable() )
out.print( "</a>" );

out.print( "</nobr></div></td>" );

out.print( "<td " + shoreColor + "><div style=\"FONT-SIZE: 10px; FONT-FAMILY: Verdana, Arial, Helvetica, sans-serif\">&nbsp;&nbsp;</td>" );

out.print( "<td " + shoreColor + " nowrap=\"yes\" ><nobr>" );

if ( shoreNode.isSelected() )
out.print( "<div style=\"FONT-SIZE: 10px; FONT-FAMILY: Verdana, Arial, Helvetica, sans-serif\">" );
else
out.print( "<div style=\"FONT-SIZE: 10px; FONT-FAMILY: Verdana, Arial, Helvetica, sans-serif\">" );

out.print( shoreNode.getValue( "value" ) );
out.print( "</div></nobr></td>" );
out.print( "<td " + shoreColor + "><div style=\"FONT-SIZE: 10px; FONT-FAMILY: Verdana, Arial, Helvetica, sans-serif\">&nbsp;&nbsp;</td>" );

out.print( "<td " + shoreColor + " nowrap=\"yes\" ><nobr>" );

if ( shoreNode.isSelected() )
out.print( "<div style=\"FONT-SIZE: 10px; FONT-FAMILY: Verdana, Arial, Helvetica, sans-serif\">" );
else
out.print( "<div style=\"FONT-SIZE: 10px; FONT-FAMILY: Verdana, Arial, Helvetica, sans-serif\">" );

out.print( shoreNode.getValue( "return" ) );
out.print( "</div></nobr></td>" );

out.print( "<td " + shoreColor + "><div style=\"FONT-SIZE: 10px; FONT-FAMILY: Verdana, Arial, Helvetica, sans-serif\">&nbsp;&nbsp;</td>" );

out.print( "</tr>\n" );

}

}
%>
</table>

</body>
</html>
