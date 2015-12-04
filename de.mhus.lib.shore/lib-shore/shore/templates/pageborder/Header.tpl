<%@page language="java" %><%
  response.setDateHeader( "Expires", new java.util.Date().getTime() );
  response.setDateHeader( "Last-modified", new java.util.Date().getTime() );
  response.setHeader( "Cache-Control","no-store, no-cache, must-revalidate" );
  response.addHeader( "Cache-Control","post-check=0, pre-check=0" );
  response.addHeader( "Pragma","no-cache" );
%><!-- File: {v:file_w} --><!-- Date: {v:date_w} -->
