<%

Object {v:label}Obj = {v:localLabel}.get( "{v:name}" );
int    {v:label}PageItems = {v:pageItems};
int    {v:label}Count = 0;
int    {v:label}Offset = de.mhu.lib.MhuCast.toint( request.getParameter( "sp_{v:label}_off" ), 0 );

if ( {v:label}Offset < 0 )
  {v:label}Offset = 0;

if ( {v:label}Obj != null && {v:label}Obj instanceof java.util.Collection ) {

  java.util.Iterator {v:label}Iter = ((java.util.Collection){v:label}Obj).iterator();
  try {
  	for ( int i = 0; i < {v:label}Offset; i++ )
  		{v:label}Iter.next();
  } catch ( Exception e ) {
  	{v:label}Offset = 0;
  	{v:label}Iter = ((java.util.Collection){v:label}Obj).iterator();
  }

  out.print( "<table cellpadding=\"0\" cellspacing=\"0\">" );





  out.print( "<tr><td align=\"center\" valign=\"top\">" );
  
  out.print( "<table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" >" );

  // Page / Of
  out.print( "<tr><td><nobr><div style=\"{v:indexStyle}\">" );
  out.print( ({v:label}Offset / {v:label}PageItems + 1) + " / " + ((((java.util.Collection){v:label}Obj).size()-1) / {v:label}PageItems + 1) );
  
  // Back
  out.print( "</div></nobr></td><td width=\"100%\">&nbsp;" );
  out.print( "</td><td>" );
  
  if (   {v:label}Offset != 0 )
  	out.print( "<a href=\"{v:file}?sp_{v:label}_off=" + ({v:label}Offset - {v:label}PageItems) + {v:mainLabel}ParamAdd + "\" >{v:goBack_p}</a>" );
  else
  	out.print( "{v:noBack_p}" );
  
  // Next
  out.print( "</td><td>&nbsp;" );
  out.print( "</td><td>" );
    
  if (   ({v:label}Offset / {v:label}PageItems) <  ((((java.util.Collection){v:label}Obj).size()-1) / {v:label}PageItems) )
  	out.print( "<a href=\"{v:file}?sp_{v:label}_off=" + ({v:label}Offset + {v:label}PageItems) + {v:mainLabel}ParamAdd + "\" >{v:goNext_p}</a>" );
  else
  	out.print( "{v:noNext_p}" );
  
  out.print( "</td></tr>" );
  
  out.print( "</table>" );
  
  out.print( "</td></tr>" );





  out.print( "<tr><td align=\"center\" valign=\"top\">" );

  
%>