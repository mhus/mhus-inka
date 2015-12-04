<%
Object {v:label} = {v:localLabel}.get( {v:name_c} );
boolean {v:label}Ok = ({v:label} != null);

if (   {v:label}Ok ) {
     if ( {v:label} instanceof java.util.Collection && ((java.util.Collection){v:label}).size() == 0 )
       {v:label}Ok = false;
     else
     if ( {v:label} instanceof java.util.Map && ((java.util.Map){v:label}).size() == 0 )
       {v:label}Ok = false;
}

if (   {v:label}Ok ) {
%>