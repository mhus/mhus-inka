<%
{v:formClass} {v:name}_form = null;

Object {v:name}_returnForm = request.getAttribute( "SHORE_FORM_OBJECT" );
if ( {v:name}_returnForm != null && {v:name}_returnForm instanceof {v:formClass} && ! {v:formReload} ) {
  {v:name}_form = ({v:formClass}){v:name}_returnForm;
  {v:name}_form.setPage( {v:pageVariable} );
} else {
  {v:name}_form = new {v:formClass}();
  if ( {v:name}_form instanceof de.mhu.shore.ifc.FormIfc ) {
    {v:name}_form.setFormId( (String){v:id_c} );
    {v:name}_form.setPage( {v:pageVariable} );
    {v:name}_form.initForm( request, response );
  }
}

out.print( "<input type=\"hidden\" name=\"saf_id\" value=\"" + de.mhu.shore.ShoreUtil.text2web( (String){v:id_c}, false ) + "\">" );
out.print( "<input type=\"hidden\" name=\"saf_ac\" value=\"" + de.mhu.shore.ShoreUtil.text2web( {v:action_c}, false ) + "\">" );

for ( java.util.Enumeration {v:name}_form_e = request.getParameterNames();{v:name}_form_e.hasMoreElements();) {
  String {v:name}_key = (String){v:name}_form_e.nextElement();
  if ( ! {v:name}_key.startsWith( "safp_" ) )
    out.print( "<input type=\"hidden\" name=\"safp_" + {v:name}_key + "\" value=\"" + de.mhu.shore.ShoreUtil.text2web( request.getParameter( {v:name}_key ), false ) + "\">" );
}


%>