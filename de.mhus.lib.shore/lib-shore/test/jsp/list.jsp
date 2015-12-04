<%@page language="java" %><%
response.setDateHeader( "Expires", new java.util.Date().getTime() );
response.setDateHeader( "Last-modified", new java.util.Date().getTime() );
response.setHeader( "Cache-Control","no-store, no-cache, must-revalidate" );
response.addHeader( "Cache-Control","post-check=0, pre-check=0" );
response.addHeader( "Pragma","no-cache" );
%><!-- File: list.jsp --><!-- Date: Tue Aug 05 15:02:55 CEST 2003 -->


<html><%
// try {

de.mhu.test.PageTest shoreVariable6 = new de.mhu.test.PageTest();
de.mhu.shore.ifc.PageResult pr = shoreVariable6.init( request, response );

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


StringBuffer shoreVariable6Sb = new StringBuffer();
for ( java.util.Enumeration shoreVariable6Keys = request.getParameterNames(); shoreVariable6Keys.hasMoreElements(); ) {


String k = (String)shoreVariable6Keys.nextElement();

if ( ! k.startsWith( "sp_shoreVariable" ) ) {

shoreVariable6Sb.append( '&' );
shoreVariable6Sb.append( de.mhu.lib.Rfc1738.encode( k ) );
shoreVariable6Sb.append( '=' );
shoreVariable6Sb.append( de.mhu.lib.Rfc1738.encode( request.getParameter( k ) ) );

}

}

String shoreVariable6ParamAdd = shoreVariable6Sb.toString();

if ( shoreVariable6Sb.length() != 0 )
shoreVariable6Sb.setCharAt( 0, '?' );

String shoreVariable6Parameter = shoreVariable6Sb.toString();

%>
<body>

<%
Object shoreVariable7 = shoreVariable6.get( "list" );
if (   shoreVariable7 == null ||
( shoreVariable7 instanceof java.util.Collection && ((java.util.Collection)shoreVariable7).size() == 0 ) ||
( shoreVariable7 instanceof java.util.Map && ((java.util.Map)shoreVariable7).size() == 0 )
) {
%>
Nix da
<%
}
%>

<%

Object shoreVariable8Obj = shoreVariable6.get( "list" );
int    shoreVariable8Max = -1;
int    shoreVariable8Count = 0;
if ( shoreVariable8Obj != null && shoreVariable8Obj instanceof java.util.Collection )
for ( java.util.Iterator shoreVariable8Iter = ((java.util.Collection)shoreVariable8Obj).iterator(); shoreVariable8Iter.hasNext(); ) {
Object shoreVariable8ObjMap = shoreVariable8Iter.next();
if ( shoreVariable8ObjMap != null && shoreVariable8ObjMap instanceof java.util.Map ) {
java.util.Map shoreVariable8 = (java.util.Map)shoreVariable8ObjMap;

%>
Item: <%=shoreVariable8.get( "id" )%> - <%=shoreVariable8.get( "name" )%> - <%=shoreVariable6.get( "name" )%>
<%
shoreVariable8Count++;
if ( shoreVariable8Max != -1 && shoreVariable8Count >= shoreVariable8Max ) break;

}
}
%>

</body>
</html><%
shoreVariable6.finish();
/*
} catch ( Exception eee ) {
System.err.println( "Page Exception: " + eee );
eee.printStackTrace();
}
*/
%>
