<%
if (true) {
   String value = {v:name}_form.{v:getter}();
   if ( value != null )
     out.print( de.mhu.shore.ShoreUtil.text2web( value, true ) );
}
%>