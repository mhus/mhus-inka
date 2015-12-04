<%

Object {v:label}Obj = {v:localLabel}.get( "{v:name_p}" );
int    {v:label}Max = {v:max};
int    {v:label}Count = 0;
if ( {v:label}Obj != null && {v:label}Obj instanceof java.util.Collection )
  for ( java.util.Iterator {v:label}Iter = ((java.util.Collection){v:label}Obj).iterator(); {v:label}Iter.hasNext(); ) {
    Object {v:label}ObjMap = {v:label}Iter.next();
    if ( {v:label}ObjMap != null && {v:label}ObjMap instanceof java.util.Map ) {
      java.util.Map {v:label} = (java.util.Map){v:label}ObjMap;
  
%>