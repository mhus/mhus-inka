package de.mhu.com.morse.obj;

import java.util.Hashtable;
import java.util.Iterator;

import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.types.IType;
import de.mhu.com.morse.utils.MorseException;

public class MObject extends AbstractObject {

	protected IType type;
	protected IAttribute[] attributes;
	protected String[] colNames;
	protected Object[][] values;
	protected boolean dirty = false;
	protected Hashtable<String,Integer> attrIndex = new Hashtable<String, Integer>();
	private boolean hasTables;
	private String objectId; 
	
	public MObject() {
	}
		
	public String getObjectId() throws MorseException {
		if ( objectId == null )
			objectId = getString( IAttribute.M_ID );
		return objectId;
	}

	public synchronized void setType( IType pType ) throws MorseException {
		if ( type != null )
			throw new MorseException( MorseException.TYPE_ALREADY_SET );
		type = pType;
		
		int cnt = 0;
		for ( Iterator<IAttribute> i = type.getAttributes(); i.hasNext(); ) {
			IAttribute attr = i.next();
			cnt++;
		}
		attributes = new IAttribute[ cnt ];
		colNames   = new String[ cnt ];
		values     = new Object[ cnt ][];
		cnt = 0;
		for ( Iterator<IAttribute> i = type.getAttributes(); i.hasNext(); ) {
			IAttribute attr = i.next();
			attributes[ cnt ] = attr;
			colNames [ cnt ] = attr.getName();
			attrIndex.put( colNames[ cnt ], cnt );
			values[ cnt ] = new Object[ 2 ];
			if ( ! attr.isTable() ) {
				values[ cnt ][0] = attr.getDefaultValue();
			} else {
				hasTables = true;
				values[ cnt ][0] = createRawTable( cnt );
			}
			cnt++;
		}
		
	}
	
	protected MObjectTable createRawTable(int index) throws MorseException {
		return new MObjectTable( type, attributes[ index ] );
	}
	
	protected int getIndexOf( String name ) throws MorseException {
		Integer i = attrIndex.get( name.toLowerCase() );
		if ( i == null )
			throw new MorseException( MorseException.ATTR_NOT_FOUND, name );
		return i;
	}
	
	protected void validateIndex( int index ) throws MorseException {
		if ( index < 0 || index >= attributes.length )
			throw new MorseException( MorseException.ATTR_NOT_FOUND, String.valueOf( index ) );
	}
	
	@Override
	public ITable getTable(String name) throws MorseException {
		return (ITable)values[ getIndexOf( name ) ][0];
	}

	@Override
	public ITable getTable(int index) throws MorseException {
		validateIndex( index );
		return (ITable)values[ index ][0];
	}

	@Override
	protected void setRawString(String name, String value) throws MorseException {
		int index = getIndexOf( name );
		dirty = true;
		if ( values[ index ][1] == null ) values[ index ][1] = values[ index ][0];
		values[ index ][0] = value;
	}

	@Override
	protected void setRawString(int index, String value) throws MorseException {
		validateIndex( index );
		dirty = true;
		if ( values[ index ][1] == null ) values[ index ][1] = values[ index ][0];
		values[ index ][0] = value;
	}

	@Override
	public void close() {
	}

	@Override
	public IAttribute getAttribute(String name) throws MorseException {
		return getAttribute( getIndexOf( name ) );
	}

	@Override
	public IAttribute getAttribute(int index) throws MorseException {
		validateIndex( index );
		return attributes[ index ];
	}

	@Override
	public int getAttributeCount() {
		return attributes.length;
	}

	@Override
	public String[] getColumns() throws MorseException {
		return colNames;
	}

	@Override
	public String getRawString(String name) throws MorseException {
		int index = getIndexOf( name );
		if ( attributes[ index ].isTable() )
			throw new MorseException( MorseException.NOT_SUPPORTED );			
		return (String)values[ index ][0];
	}

	@Override
	public String getRawString(int index) throws MorseException {
		validateIndex( index );
		if ( attributes[ index ].isTable() )
			throw new MorseException( MorseException.NOT_SUPPORTED );
		return (String)values[ index ][0];
	}

	@Override
	public boolean next() throws MorseException {
		return false;
	}

	public IType getType() {
		return type;
	}

	public boolean reset() throws MorseException {
		return false;
	}

	public boolean isDirty() {
		if ( dirty ) return true;
		if ( !hasTables ) return false;
		for ( int i = 0; i < values.length; i++ )
			if ( values[ i ][ 0 ] instanceof ITable && ((ITable)values[ i ][ 0 ]).isDirty() )
				return true;
		return false;
	}

	public boolean isDirty(int index) throws MorseException {
		validateIndex( index );
		if ( values[ index ][ 0 ] instanceof MObject )
			return ((MObject)values[ index ][ 0 ]).isDirty();
		return values[ index ][1] != null;
	}
	
	protected void cleanUp() {
		for ( int i = 0; i < values.length; i++ )
			if ( values[ i ][ 0 ] instanceof MObject )
				((MObject)values[ i ][ 0 ]).cleanUp();
			else
			if ( values[ i ][ 1 ] != null ) {
				//values[ i ][ 0 ] = values[ i ][ 1 ];
				values[ i ][ 1 ] = null;
			}
		dirty = false;
	}

	public boolean isDirty(String name) throws MorseException {
		int index = getIndexOf( name );
		if ( values[ index ][ 0 ] instanceof MObject )
			return ((MObject)values[ index ][ 0 ]).isDirty();
		return values[ index ][ 1 ] != null;
	}

	public boolean isNew() {
		return false;
	}

}
