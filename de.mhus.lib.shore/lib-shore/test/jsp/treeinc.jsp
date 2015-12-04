<%@page language="java" %><%
response.setDateHeader( "Expires", new java.util.Date().getTime() );
response.setDateHeader( "Last-modified", new java.util.Date().getTime() );
response.setHeader( "Cache-Control","no-store, no-cache, must-revalidate" );
response.addHeader( "Cache-Control","post-check=0, pre-check=0" );
response.addHeader( "Pragma","no-cache" );
%><!-- File: treeinc.jsp --><!-- Date: Tue Aug 05 15:02:55 CEST 2003 -->
<html><%
// try {

de.mhu.test.web.ObjectPage shoreVariable9 = new de.mhu.test.web.ObjectPage();
de.mhu.shore.ifc.PageResult pr = shoreVariable9.init( request, response );

if ( pr != null && ! pr.goAhead() ) {

if ( pr.isError() ) {
response.sendError( 500, pr.getErrorMsg() );
return;
}

if ( pr.goForward() ) {
if ( pr.isRedirect() ) {
response.sendRedirect( pr.getForwardURL().toString() );
} else {
javax.servlet.RequestDispatcher rd = request.getRequestDispatcher( pr.getForwardURL().toString() );
rd.forward( request, response );
}
}


}


StringBuffer shoreVariable9Sb = new StringBuffer();
for ( java.util.Enumeration shoreVariable9Keys = request.getParameterNames(); shoreVariable9Keys.hasMoreElements(); ) {


String k = (String)shoreVariable9Keys.nextElement();

if ( ! k.startsWith( "sp_shoreVariable" ) ) {

shoreVariable9Sb.append( '&' );
shoreVariable9Sb.append( de.mhu.lib.Rfc1738.encode( k ) );
shoreVariable9Sb.append( '=' );
shoreVariable9Sb.append( de.mhu.lib.Rfc1738.encode( request.getParameter( k ) ) );

}

}

String shoreVariable9ParamAdd = shoreVariable9Sb.toString();

if ( shoreVariable9Sb.length() != 0 )
shoreVariable9Sb.setCharAt( 0, '?' );

String shoreVariable9Parameter = shoreVariable9Sb.toString();

%>
<body bgcolor=#ffffff>
<center>
<h1>Objects</h1>
<table cellpadding="0" cellspacing="0" width="1000" height="400"><tr><td valign=top align=left>
<table border="0" cellpadding="0" cellspacing="0"><%
if (true) {
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
out.println( "window.location= \"treeinc.jsp\";" );
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
out.print( "<a href=\"treeinc.jsp?shoreTree_tree1_exp=" + shoreTreeDoc.getNodeReference() + r + "#shoreTree_tree1_" + shoreTreeDoc.getNodeReference() + "\">" );
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
out.print( "<a href=\"treeinc.jsp?shoreTree_tree1_exp=" + shoreTreeDoc.getNodeReference() + r + "#shoreTree_tree1_" + shoreTreeDoc.getNodeReference() + "\">" );
if ( shoreNode.isOpen() )
out.print( "<img src=\"" + shoreNode.getImage( de.mhu.shore.ifc.tree.Document.FOLDER_OPEN ) + "\" border=0 vspace=0 hspace=0 >" );
else
out.print( "<img src=\"" + shoreNode.getImage( de.mhu.shore.ifc.tree.Document.FOLDER_CLOSED ) + "\" border=0 vspace=0 hspace=0 >" );
out.print( "</a>" );
}

String extraImage = shoreNode.getImage( de.mhu.shore.ifc.tree.Document.EXTRA );
if ( extraImage != null ) {
if ( shoreNode.isClickableImage() )
out.print( "<a href=\"treeinc.jsp?shoreTree_tree1_imgclick=" + shoreTreeDoc.getNodeReference() + r + "#shoreTree_tree1_" + shoreTreeDoc.getNodeReference() + "\">" );
out.print( "<img src=\"" + extraImage + "\" border=0 vspace=0 hspace=0 > " );
if ( shoreNode.isClickableImage() )
out.print( "</a>" );
}

if ( shoreNode.isClickable() )
out.print( "<a href=\"treeinc.jsp?shoreTree_tree1_click=" + shoreTreeDoc.getNodeReference() + r + "#shoreTree_tree1_" + shoreTreeDoc.getNodeReference() + "\">" );

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
}
%></table></td></tr></table>
<p>
<%
Object shoreVariable10 = shoreVariable9.get( "selected" );
if (   shoreVariable10 == null ||
( shoreVariable10 instanceof java.util.Collection && ((java.util.Collection)shoreVariable10).size() == 0 ) ||
( shoreVariable10 instanceof java.util.Map && ((java.util.Map)shoreVariable10).size() == 0 )
) {
%>
Nothing selected
<%
}
%>
<%
Object shoreVariable11 = shoreVariable9.get( "selected" );
boolean shoreVariable11Ok = (shoreVariable11 != null);

if (   shoreVariable11Ok ) {
if ( shoreVariable11 instanceof java.util.Collection && ((java.util.Collection)shoreVariable11).size() == 0 )
shoreVariable11Ok = false;
else
if ( shoreVariable11 instanceof java.util.Map && ((java.util.Map)shoreVariable11).size() == 0 )
shoreVariable11Ok = false;
}

if (   shoreVariable11Ok ) {
%>
  <h1>Selected</h1>
  <table>
  <%

Object shoreVariable12Obj = shoreVariable9.get( "selected" );
int    shoreVariable12Max = -1;
int    shoreVariable12Count = 0;
if ( shoreVariable12Obj != null && shoreVariable12Obj instanceof java.util.Collection )
for ( java.util.Iterator shoreVariable12Iter = ((java.util.Collection)shoreVariable12Obj).iterator(); shoreVariable12Iter.hasNext(); ) {
Object shoreVariable12ObjMap = shoreVariable12Iter.next();
if ( shoreVariable12ObjMap != null && shoreVariable12ObjMap instanceof java.util.Map ) {
java.util.Map shoreVariable12 = (java.util.Map)shoreVariable12ObjMap;

%>
  <tr><td><%=shoreVariable12.get( "name" )%></td></tr>
  <%
shoreVariable12Count++;
if ( shoreVariable12Max != -1 && shoreVariable12Count >= shoreVariable12Max ) break;

}
}
%>
  </table>
<%
}
%>
</html><%
shoreVariable9.finish();
/*
} catch ( Exception eee ) {
System.err.println( "Page Exception: " + eee );
eee.printStackTrace();
}
*/
%>
