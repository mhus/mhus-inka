<%@page language="java" %><%
response.setDateHeader( "Expires", new java.util.Date().getTime() );
response.setDateHeader( "Last-modified", new java.util.Date().getTime() );
response.setHeader( "Cache-Control","no-store, no-cache, must-revalidate" );
response.addHeader( "Cache-Control","post-check=0, pre-check=0" );
response.addHeader( "Pragma","no-cache" );
%><!-- File: tree.jsp --><!-- Date: Tue Aug 05 15:02:55 CEST 2003 -->
<html><%
// try {

de.mhu.test.web.ObjectPage shoreVariable2 = new de.mhu.test.web.ObjectPage();
de.mhu.shore.ifc.PageResult pr = shoreVariable2.init( request, response );

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


StringBuffer shoreVariable2Sb = new StringBuffer();
for ( java.util.Enumeration shoreVariable2Keys = request.getParameterNames(); shoreVariable2Keys.hasMoreElements(); ) {


String k = (String)shoreVariable2Keys.nextElement();

if ( ! k.startsWith( "sp_shoreVariable" ) ) {

shoreVariable2Sb.append( '&' );
shoreVariable2Sb.append( de.mhu.lib.Rfc1738.encode( k ) );
shoreVariable2Sb.append( '=' );
shoreVariable2Sb.append( de.mhu.lib.Rfc1738.encode( request.getParameter( k ) ) );

}

}

String shoreVariable2ParamAdd = shoreVariable2Sb.toString();

if ( shoreVariable2Sb.length() != 0 )
shoreVariable2Sb.setCharAt( 0, '?' );

String shoreVariable2Parameter = shoreVariable2Sb.toString();

%>
<body bgcolor=#ffffff>
<center>
<h1>Objects</h1>
<iframe src="tree.jsp1.jsp" height="400" width="1000" name="tree1">
</iframe>
<p>
<%
Object shoreVariable3 = shoreVariable2.get( "selected" );
if (   shoreVariable3 == null ||
( shoreVariable3 instanceof java.util.Collection && ((java.util.Collection)shoreVariable3).size() == 0 ) ||
( shoreVariable3 instanceof java.util.Map && ((java.util.Map)shoreVariable3).size() == 0 )
) {
%>
Nothing selected
<%
}
%>
<%
Object shoreVariable4 = shoreVariable2.get( "selected" );
boolean shoreVariable4Ok = (shoreVariable4 != null);

if (   shoreVariable4Ok ) {
if ( shoreVariable4 instanceof java.util.Collection && ((java.util.Collection)shoreVariable4).size() == 0 )
shoreVariable4Ok = false;
else
if ( shoreVariable4 instanceof java.util.Map && ((java.util.Map)shoreVariable4).size() == 0 )
shoreVariable4Ok = false;
}

if (   shoreVariable4Ok ) {
%>
  <h1>Selected</h1>
  <table>
  <%

Object shoreVariable5Obj = shoreVariable2.get( "selected" );
int    shoreVariable5Max = -1;
int    shoreVariable5Count = 0;
if ( shoreVariable5Obj != null && shoreVariable5Obj instanceof java.util.Collection )
for ( java.util.Iterator shoreVariable5Iter = ((java.util.Collection)shoreVariable5Obj).iterator(); shoreVariable5Iter.hasNext(); ) {
Object shoreVariable5ObjMap = shoreVariable5Iter.next();
if ( shoreVariable5ObjMap != null && shoreVariable5ObjMap instanceof java.util.Map ) {
java.util.Map shoreVariable5 = (java.util.Map)shoreVariable5ObjMap;

%>
  <tr><td><%=shoreVariable5.get( "name" )%></td></tr>
  <%
shoreVariable5Count++;
if ( shoreVariable5Max != -1 && shoreVariable5Count >= shoreVariable5Max ) break;

}
}
%>
  </table>
<%
}
%>

</body>
</html><%
shoreVariable2.finish();
/*
} catch ( Exception eee ) {
System.err.println( "Page Exception: " + eee );
eee.printStackTrace();
}
*/
%>
