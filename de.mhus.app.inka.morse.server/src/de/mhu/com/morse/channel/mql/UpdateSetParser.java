package de.mhu.com.morse.channel.mql;

import java.util.Iterator;
import java.util.LinkedList;

import de.mhu.com.morse.aaa.IAclManager;
import de.mhu.com.morse.btc.Btc;
import de.mhu.com.morse.channel.IChannelDriverServer;
import de.mhu.com.morse.channel.CMql;
import de.mhu.com.morse.channel.sql.SqlUtils;
import de.mhu.com.morse.mql.ICompiledQuery;
import de.mhu.com.morse.obj.ITable;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.types.IType;
import de.mhu.com.morse.usr.UserInformation;
import de.mhu.com.morse.utils.AttributeUtil;
import de.mhu.com.morse.utils.MorseException;

public class UpdateSetParser {
	
	public static final int CHANGE_SUB_ATTR = 1;
	public static final int CHANGE_TRUNCATE = 2;
	public static final int CHANGE_INSERT = 3;
	public static final int CHANGE_APPEND = 4;
	public static final int CHANGE_ATTR = 5;
	private static final int CHANGE_REMOVE = 6;

	public static int parse(	int off, 
								ICompiledQuery code, 
								IType type, 
								IAclManager aclManager, 
								UserInformation user, 
								IChannelDriverServer driver,
								UpdateSetDescription desc,
								boolean isNoError ) throws MorseException {
		
		// check the attributes and values
		// -------------------------------
		
		while ( off < code.size() && code.getInteger( off ) == CMql.NaN ) {
			String name = code.getString( off );
			int index = -1;
			boolean set = true;

			off++;
			if ( code.getInteger( off ) == CMql.OPEN ) {
				// has index, remember
				off++; // (
				index = Integer.parseInt( code.getString( off ) );
				off++; 
				off++; // )
			}
			if ( code.getInteger( off ) == CMql.EQ ) {
				// has equal, its a SET instruction
				off++;
								
				if ( index != -1 ) {
					// table attribute
					int pos = name.indexOf( '.' );
					if ( pos < 0 )
						throw new MorseException( MorseException.ATTR_NOT_A_TABLE, name );
					IAttribute attr = type.getAttribute( name.substring( 0, pos ) );
					if ( attr == null )
						throw new MorseException( MorseException.ATTR_NOT_FOUND, name );
					if ( ! aclManager.hasWrite( user, attr.getAccessAcl() ) ) {
						if ( isNoError )
							set = false;
						else
							throw new MorseException( MorseException.ACCESS_DENIED_WRITE, new String[] { "attr", name, attr.getAccessAcl() } );
					}
					if ( ! attr.isTable() )
						throw new MorseException( MorseException.ATTR_IS_A_TABLE, name );

					IAttribute subAttr = attr.getAttribute( name.substring( pos+1 ) );
					if ( subAttr == null )
						throw new MorseException( MorseException.ATTR_NOT_FOUND, name );
					if ( ! aclManager.hasWrite( user, subAttr.getAccessAcl() ) ) {
						if ( isNoError )
							set = false;
						else
							throw new MorseException( MorseException.ACCESS_DENIED_WRITE, new String[] { "attr", name, attr.getAccessAcl() } );
					}
					String value = code.getString( off );
					off++;
					if ( ! AttributeUtil.isValue( value ) || ! subAttr.getAco().validate( value ) )
						throw new MorseException( MorseException.ATTR_VALUE_NOT_VALIDE, new String[] { subAttr.getCanonicalName(), value } );
					value = SqlUtils.getValueRaw(driver, attr, value);
					
					if ( set )
						desc.appendSubAttr( index, attr, subAttr, value );
					
				} else {
					// single attribute
					
					IAttribute attr = type.getAttribute( name );
					if ( attr == null )
						throw new MorseException( MorseException.ATTR_NOT_FOUND, name );
					if ( ! aclManager.hasWrite( user, attr.getAccessAcl() ) ) {
						if ( isNoError )
							set = false;
						else
							throw new MorseException( MorseException.ACCESS_DENIED_WRITE, new String[] { "attr", name, attr.getAccessAcl() } );
					}
					if ( attr.isTable() )
						throw new MorseException( MorseException.ATTR_IS_A_TABLE, name );
					String value = code.getString( off );
					off++;
					if ( ! AttributeUtil.isValue( value ) || ! attr.getAco().validate( AttributeUtil.valueExtract(attr, value ) ) )
						throw new MorseException( MorseException.ATTR_VALUE_NOT_VALIDE, new String[] { attr.getCanonicalName(), value } );
					value = SqlUtils.getValueRaw(driver, attr, value);
					
					if ( set )
						desc.appendAttr( attr, value );
				}

			} else
			if ( code.getInteger( off ) == CMql.INSERT || code.getInteger( off ) == CMql.APPEND ) {
				boolean isAppend = code.getInteger( off ) == CMql.APPEND;
				if ( isAppend && index != -1 )
					throw new MorseException( MorseException.INDEX_NOT_ALLOWED, name );
				off++;
				// insert a row into nested-table
				boolean hasNames = code.getInteger( off ) == CMql.OPEN;
				
				IAttribute attr = type.getAttribute( name );
				if ( attr == null )
					throw new MorseException( MorseException.ATTR_NOT_FOUND, name );
				if ( ! aclManager.hasWrite( user, attr.getAccessAcl() ) ) {
					if ( isNoError )
						set = false;
					else
						throw new MorseException( MorseException.ACCESS_DENIED_WRITE, new String[] {"attr", name, attr.getAccessAcl() } );
				}
				if ( ! attr.isTable() )
					throw new MorseException( MorseException.ATTR_NOT_A_TABLE, attr.getCanonicalName() );
				
				int namesCnt = 0;
				IAttribute[] attrs  = null;
				String[] values = null;
				LinkedList<Boolean> canSet = null;
				if ( isNoError ) canSet = new LinkedList<Boolean>();
				
				if ( hasNames ) {
					// collect names
					while ( code.getInteger( off + namesCnt*2 ) != CMql.CLOSE ) {
						namesCnt++;
					}
					attrs = new IAttribute[ namesCnt ];
					namesCnt = 0;
					while ( code.getInteger( off + namesCnt*2 ) != CMql.CLOSE ) {
						boolean set2 = true;
						String subName = code.getString( off + namesCnt * 2 + 1 );
						attrs[ namesCnt ] = attr.getAttribute( subName );
						if ( attrs[ namesCnt ] == null )
							throw new MorseException( MorseException.ATTR_NOT_FOUND, subName );
						if ( ! aclManager.hasWrite( user, attrs[ namesCnt ].getAccessAcl() ) ) {
							if ( isNoError )
								set2 = false;
							else
								throw new MorseException( MorseException.ACCESS_DENIED_WRITE, new String[] { "attr", attrs[ namesCnt ].getCanonicalName(), attrs[ namesCnt ].getAccessAcl() } );
						}
						
						namesCnt++;
						
						if ( isNoError )
							canSet.add( set2 );
					}
					off = off + namesCnt * 2 + 2; // ...) VALUES
				} else {
					// collect all names from attribute
					for ( Iterator<IAttribute> i = attr.getAttributes(); i.hasNext(); ) {
						IAttribute subAttr = i.next();
						if ( aclManager.hasWrite( user, subAttr.getAccessAcl() ) )
							namesCnt++;
					}
					attrs = new IAttribute[ namesCnt ];
					namesCnt = 0;
					for ( Iterator<IAttribute> i = attr.getAttributes(); i.hasNext(); ) {
						IAttribute subAttr = i.next();
						if ( aclManager.hasWrite( user, subAttr.getAccessAcl() ) ) {
							attrs[ namesCnt ] = subAttr;
							namesCnt++;
						}
						
						if ( isNoError )
							canSet.add( true );
					}
					off++;
				}
				
				// collect values
				int cnt = 0;
				values = new String[ attrs.length ];
				off ++;
				while ( code.getInteger( off ) != CMql.CLOSE ) {
					if ( cnt >= values.length )
					throw new MorseException( MorseException.ATTR_VALUE_NOT_VALIDE );
					values[ cnt ] = code.getString( off );
					off++;
					if ( ! AttributeUtil.isValue( values[cnt] ) || ! attrs[ cnt].getAco().validate( values[cnt] ) )
						throw new MorseException( MorseException.ATTR_VALUE_NOT_VALIDE, new String[] { attrs[ cnt].getCanonicalName(), values[cnt] } );
					values[ cnt ] = SqlUtils.getValueRaw(driver, attrs[cnt], values[cnt] );
					if ( code.getInteger( off ) == CMql.COMMA )
						off++;
					cnt++;
				}
				if ( cnt != values.length )
					throw new MorseException( MorseException.ATTR_VALUE_NOT_VALIDE );
				
				off++;
				
				if ( isNoError ) {
					for ( int i = 0; i < attrs.length; i++ )
						if ( ! canSet.get( i ) ) attrs[i] = null;
				}
				
				if ( set ) {
					if ( isAppend )
						desc.appendAppend( attr, attrs, values );
					else
						desc.appendInsert( attr, index, attrs, values );
				}
				
			} else
			if ( code.getInteger( off ) == CMql.TRUNCATE ) {
				if ( index == -1 )
					throw new MorseException( MorseException.INDEX_NOT_SET, name );
				off++;
				IAttribute attr = type.getAttribute( name );
				if ( attr == null )
					throw new MorseException( MorseException.ATTR_NOT_FOUND, name );
				if ( ! aclManager.hasWrite( user, attr.getAccessAcl() ) ) {
					if ( isNoError )
						set = false;
					else
						throw new MorseException( MorseException.ACCESS_DENIED_WRITE, new String[] { "attr", name, attr.getAccessAcl() } );
				}
				if ( ! attr.isTable() )
					throw new MorseException( MorseException.ATTR_NOT_A_TABLE, name );
				
				if ( set )
					desc.appendTruncate( attr, index );
				
			} else
			if ( code.getInteger( off ) == CMql.DELETE ) {
				if ( index == -1 )
					throw new MorseException( MorseException.INDEX_NOT_SET, name );
				off++;
				IAttribute attr = type.getAttribute( name );
				if ( attr == null )
					throw new MorseException( MorseException.ATTR_NOT_FOUND, name );
				if ( ! aclManager.hasWrite( user, attr.getAccessAcl() ) ) {
					if ( isNoError )
						set = false;
					else
						throw new MorseException( MorseException.ACCESS_DENIED_WRITE, new String[] { "attr", name, attr.getAccessAcl() } );
				}
				if ( ! attr.isTable() )
					throw new MorseException( MorseException.ATTR_NOT_A_TABLE, name );
				
				if ( set )
					desc.appendDelete( attr, index );
				
			}
			
			if ( code.getInteger( off ) == CMql.COMMA )
				off++;
		}
		
		return off;
	}
	
	public static void changeBtc( UpdateSetDescription desc, Btc btc ) throws MorseException {
		
		for ( int i = 0; i < desc.descLen; i++ ) {
			ITable t = null;
			switch ( desc.desc[i].type ) {
			case CHANGE_ATTR:
				btc.setString( desc.desc[i].attr.getName(), desc.desc[i].value );
				break;
			case CHANGE_SUB_ATTR:
				t = btc.getTable( desc.desc[i].attr.getName() );
				t.setCursor( desc.desc[i].index );
				t.setString( desc.desc[i].subAttr.getName(), desc.desc[i].value );
				break;
			case CHANGE_APPEND:
				t = btc.getTable( desc.desc[i].attr.getName() );
				t.createRow();
				for ( int j = 0; j < desc.desc[i].attrs.length; j++ )
					if ( desc.desc[i].attrs[j] != null ) t.setString( desc.desc[i].attrs[j].getName(), desc.desc[i].values[j] );
				t.appendRow();
				break;
			case CHANGE_INSERT:
				t = btc.getTable( desc.desc[i].attr.getName() );
				t.createRow();
				for ( int j = 0; j < desc.desc[i].attrs.length; j++ )
					if ( desc.desc[i].attrs[j] != null ) t.setString( desc.desc[i].attrs[j].getName(), desc.desc[i].values[j] );
				t.insertRow( desc.desc[i].index );
				break;
			case CHANGE_TRUNCATE:
				t = btc.getTable( desc.desc[i].attr.getName() );
				while ( t.getSize() > desc.desc[i].index )
					t.removeRow( desc.desc[i].index );
				break;
			case CHANGE_REMOVE:
				t = btc.getTable( desc.desc[i].attr.getName() );
				t.removeRow( desc.desc[i].index );
				break;
			}
		}
		
	}
	
	public static class UpdateSetDescription {

		public int descLen = 0;
		public Description[] desc = new Description[ 10 ];
		
		public void appendSubAttr(int index, IAttribute attr, IAttribute subAttr, String value) {
			Description d = new Description();
			d.type = CHANGE_SUB_ATTR;
			d.attr = attr;
			d.subAttr = subAttr;
			d.value = value;
			append( d );
		}

		public void appendDelete(IAttribute attr, int index) {
			Description d = new Description();
			d.type = CHANGE_REMOVE;
			d.attr = attr;
			d.index = index;
			append( d );			
		}

		private synchronized void append(Description d) {
			if ( descLen >= desc.length ) {
				Description[] old = desc;
				desc = new Description[ desc.length + 10 ];
				System.arraycopy( old, 0, desc, 0, old.length );
			}
			desc[ descLen ] = d;
			descLen++;
		}

		public void appendTruncate(IAttribute attr, int index) {
			Description d = new Description();
			d.type = CHANGE_TRUNCATE;
			d.attr = attr;
			d.index = index;
			append( d );
		}

		public void appendInsert(IAttribute attr, int index, IAttribute[] attrs, String[] values) {
			Description d = new Description();
			d.type = CHANGE_INSERT;
			d.attr = attr;
			d.index = index;
			d.attrs = attrs;
			d.values = values;
			append( d );
		}

		public void appendAppend(IAttribute attr, IAttribute[] attrs, String[] values) {
			Description d = new Description();
			d.type = CHANGE_APPEND;
			d.attr = attr;
			d.attrs = attrs;
			d.values = values;
			append( d );	
		}

		public void appendAttr(IAttribute attr, String value) {
			Description d = new Description();
			d.type = CHANGE_ATTR;
			d.attr = attr;
			d.value = value;
			append( d );
		}
		
		
		
	}
		
	public static class Description {
		public String[] values;
		public IAttribute[] attrs;
		public int index;
		public String value;
		public IAttribute subAttr;
		public IAttribute attr;
		public int type = 0;
		
		
		
	}
	
}
