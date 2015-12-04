package de.mhu.com.morse.client;

import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import de.mhu.lib.log.AL;
import de.mhu.com.morse.aco.IAco;
import de.mhu.com.morse.channel.IChannelDriver;
import de.mhu.com.morse.net.IMessage;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.types.IAttributeDefault;
import de.mhu.com.morse.types.IType;
import de.mhu.com.morse.types.ITypes;
import de.mhu.com.morse.utils.AcoUtil;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.utils.TypesUtil;
import de.mhu.lib.plugin.AfPluginException;

public class MTypes implements ITypes {

	private static AL log = new AL( MTypes.class );
	private MConnection con;
	private Hashtable types = null;

	public MTypes( MConnection pCon ) throws IOException, AfPluginException {
		con = pCon;
		reset();
	}
	
	private void reset() throws IOException, AfPluginException {
		types = new Hashtable();
		IMessage msg = con.createMessage();
		msg.append( "a.a" );
		IMessage res = con.sendAndWait(msg, 60000 );
		for ( int i = 0; i < res.getCount(); i++ ) {
			types.put( res.getString(i), new MyType( res.getString(i) ) );
		}
	}
	
	public IType get(String name) {
		return (IType)types.get( name );
	}

	public Iterator getTypes() {
		return types.values().iterator();
	}

	public class MyType implements IType {

		private String name;
		private String superName;
		private LinkedList<IAttribute> attr = new LinkedList<IAttribute>();
		private Hashtable<String,IAttribute> attrByName = new Hashtable<String,IAttribute>();
		private LinkedList<String> channels = new LinkedList<String>();
		private String accessAcl;
		private HashSet<String> instances = new HashSet<String>();
		
		public MyType(String pName) {
			name = pName;
		}

		public IAttribute getAttribute(String name) {
			init();
			return (IAttribute)attrByName.get( name );
		}

		private void init() {
			
			if ( superName != null ) return;
			
			attr.clear();
			attrByName.clear();
			channels.clear();
			
			IMessage msg = null;
			try {
				msg = con.createMessage();
			} catch (MorseException e1) {
				log.error( e1 );
				return;
			}
			msg.append( "a.n" );
			msg.append( name );
			try {
				IMessage res = con.sendAndWait(msg, 60000 );
				superName = res.shiftString();
				accessAcl = res.shiftString();
				while ( res.shiftInteger() == 1 ) {
					MyAttr a = new MyAttr( this, res );
					
					attr.add( a );
					attrByName.put( a.getName(), a );
				}
				
				while ( res.shiftInteger() == 1 ) {
					channels.add( res.shiftString() );
				}
				
				int cnt = res.shiftInteger();
				for ( int i = 0; i < cnt; i++ )
					instances.add( res.shiftString() );
				
			} catch (Exception e) {
				log.error( e );
			}
		}

		public Iterator<IAttribute> getAttributes() {
			init();
			return attr.iterator();
		}

		public String getName() {
			return name;
		}

		public String getSuperName() {
			init();
			return superName;
		}

		public IType getSuperType() {
			init();
			return get( getSuperName() );
		}

		public boolean isInChannel(IChannelDriver driver) {
			return TypesUtil.isInChannel( channels, driver );
		}

		public Iterator<String> getChannelDefinition() {
			return channels.iterator();
		}

		public String getAccessAcl() {
			return accessAcl;
		}

		public boolean isInstanceOf(String type) {
			return instances.contains( type );
		}
		
		public String[] getSuperTypes() {
			return (String[])instances.toArray( new String[ instances.size() ] );
		}
		
	}
	
	class MyAttr implements IAttribute {

		private String name;
		private int type;
		private int size;
		private String cannonical;
		private String sourceTypeName;
		private LinkedList<IAttribute> attr = null;
		private Hashtable<String,IAttribute>  attrByName = null;
		private MyAttr masterAttr;
		private MyType masterType;
		private String acoName;
		private IAco aco;
		private int index;
		private boolean isNull;
		private String defaultValue;
		private String extraValue;
		private String accessAcl;

		public MyAttr( MyType type2, IMessage res) throws MorseException {
			name = res.shiftString();
			type = res.shiftInteger();
			size = res.shiftInteger();
			cannonical = res.shiftString();
			acoName = res.shiftString();
			index = res.shiftInteger();
			isNull = res.shiftInteger() != 0;
			defaultValue = res.shiftString();
			extraValue = res.shiftString();
			accessAcl = res.shiftString();
			sourceTypeName = res.shiftString();
			
			if ( sourceTypeName.equals( type2.getName() ) )
				masterType = type2;
			else
				masterType = (MyType) get( sourceTypeName );
			
			aco = AcoUtil.getAco( con.getSession().getDbProvider().getDefaultConnection(), type, acoName);
			while ( res.shiftInteger() == 1 ) {
				if ( attr == null ) {
					attr = new LinkedList<IAttribute>();
					attrByName = new Hashtable<String,IAttribute>();
				}
				MyAttr a = new MyAttr( this, res );
				attr.add( a );
				attrByName.put( a.getName(), a );
			}
			aco.init( this );

		}

		public MyAttr(MyAttr attr2, IMessage res) throws MorseException {
			
			name = res.shiftString();
			type = res.shiftInteger();
			size = res.shiftInteger();
			cannonical = res.shiftString();
			
			acoName = res.shiftString();
			index = res.shiftInteger();
			isNull = res.shiftInteger() != 0;
			defaultValue = res.shiftString();
			extraValue = res.shiftString();
			accessAcl = res.shiftString();
			
			aco = AcoUtil.getAco( con.getSession().getDbProvider().getDefaultConnection(), type, acoName);
			masterAttr = attr2;
			masterType = masterAttr.masterType;
			aco.init( this );
		}

		public IAttribute getAttribute(String name) {
			return (IAttribute)attrByName.get( name );
		}

		public Iterator<IAttribute> getAttributes() {
			return attr.iterator();
		}

		public String getCanonicalName() {
			return cannonical;
		}

		public String getName() {
			return name;
		}

		public int getSize() {
			return size;
		}

		public IType getSourceType() {
			return masterType;
		}

		public int getType() {
			return type;
		}

		public boolean isTable() {
			return attr != null;
		}

		public String getAccessAcl() {
			return accessAcl;
		}

		public int getIndexType() {
			return index;
		}

		public boolean isNotNull() {
			return isNull;
		}

		public String getDefaultValue() {
			return defaultValue;
		}

		public String getExtraValue() {
			return extraValue;
		}

		public IAttribute getSourceAttribute() {
			return masterAttr;
		}

		public IAco getAco() {
			return aco;
		}

		public String getAcoName() {
			return acoName;
		}
		
	}

	public IAttribute getAttributeByCanonicalName(String canonical) {
		int pos = canonical.indexOf( '.' );
		if ( pos < 0 ) {
			if ( IAttribute.M_POS.equals( canonical ) ) return IAttributeDefault.ATTR_OBJ_M_POS;
			return null;
		}
		if ( pos == 0 ) {
			return IAttributeDefault.getAttribute( canonical );
		}
		IType type = get( canonical.substring( 0, pos ) );
		if ( type == null ) return null;
		canonical = canonical.substring( pos+1 );
		pos = canonical.indexOf( '.' );
		if ( pos < 0 )
			return type.getAttribute( canonical );
		IAttribute attr = type.getAttribute( canonical.substring( 0, pos ) );
		if ( attr == null || !attr.isTable() ) return null;
		return attr.getAttribute( canonical.substring( pos + 1 ) );
	}
}
