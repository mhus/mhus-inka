<%
if ( true ) {
  String selected = {v:name}_form.{v:getter}();
  if ( selected != null && selected.equals( "{v:value_p}" ) )
    out.print( "checked" );
  else
    out.print( "not_checked" );
}
%>