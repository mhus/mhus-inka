<%
if ( true ) {
  String selected = {v:name}_form.{v:getter}();
  if ( selected != null && selected.equals( "{v:value_p}" ) )
    out.print( "selected" );
  else
    out.print( "not_selected" );
}
%>