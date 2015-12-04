package de.mhu.com.morse.types;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import de.mhu.lib.config.Config;
import de.mhu.lib.config.ConfigManager;
import de.mhu.lib.log.AL;
import de.mhu.com.morse.aco.IAco;
import de.mhu.com.morse.channel.IChannel;
import de.mhu.com.morse.channel.IChannelDriver;
import de.mhu.com.morse.channel.IChannelProvider;
import de.mhu.com.morse.channel.IConnection;
import de.mhu.com.morse.channel.InitialChannelDriver;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.mql.Query;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.obj.ITableRead;
import de.mhu.com.morse.utils.AcoUtil;
import de.mhu.com.morse.utils.DummyConnection;
import de.mhu.com.morse.utils.ServerTypesUtil;
import de.mhu.com.morse.utils.TypesUtil;
import de.mhu.lib.plugin.AfPlugin;
import de.mhu.lib.plugin.AfPluginException;
import de.mhu.lib.plugin.AfPluginNode;

public class Types extends AfPlugin implements ITypes {

	private static Config config = ConfigManager.getConfig( "server" );
	
	private static AL log = new AL( Types.class );
	private Hashtable types;
	public IConnection connection;

	protected void apDestroy() throws Exception {
		// TODO Auto-generated method stub

	}

	protected void apDisable() throws AfPluginException {
		// TODO Auto-generated method stub

	}

	protected void apEnable() throws AfPluginException {

		try {
			IChannelProvider provider = (IChannelProvider)getSinglePpi( IChannelProvider.class );
			connection = provider.getDefaultConnection();
			
			Query query = new Query( connection, "SELECT ** FROM m_type @sys" );
			IQueryResult res = query.execute();
			loadTypes( res );
			
			if ( log().t4() ) {
				for ( Iterator i = getTypes(); i.hasNext(); )
					((Type)i.next()).dump();
			}
			
			res.close();
			
		} catch ( Throwable th ) {
			throw new AfPluginException( 1, th );
		}
	}
	
	private void loadTypes( IQueryResult res ) throws AfPluginException {
		
		types = new Hashtable();
		
		try {
			
			while ( res.next() ) {
				String name = res.getString( "name" );
				types.put( name, new Type( res ) );
			}
			
			// connect together 
			for ( Iterator i = types.values().iterator(); i.hasNext(); )
				((Type)i.next()).connectSuper();
			
			// act. attributes
			((Type)types.get( IType.TYPE_OBJECT )).connectAttr();
			
		} catch (MorseException e) {
			throw new AfPluginException( 0, e );
		}
		
	}

	protected void apInit() throws Exception {
		
		IChannelDriver sysDb = (IChannelDriver)((AfPluginNode)getApParent()
				.getPluginByPath( config.getProperty( "core.module.path" ) ))
				.getNodeSinglePpi( IChannelDriver.class );
		// InitialDbDriver sysDb = new InitialDbDriver();
		// sysDb.init( "store_sys" ); // TODO from config
		IChannel sysChannel = sysDb.createChannel( null );
		IQueryResult res;
		try {
			res = sysChannel.query( new Query( new DummyConnection( sysChannel ), "SELECT * FROM m_type" ) );
		} catch (MorseException e) {
			log.error( e );
			throw new AfPluginException( 1, e );
		}
		connection = new DummyConnection( sysChannel );
		loadTypes( res );
		
		appendPpi( ITypes.class , this );
		
		sysChannel.close();
	}

	public IType get( String name ) {
		return (IType)types.get( name );
	}
	
	class Type implements IType {

		private String superType;
		private LinkedList<Attribute> attributes = new LinkedList<Attribute>();
		private String name;
		private Type superTypeObj;
		private LinkedList<Type> inheritance = new LinkedList<Type>();
		private Hashtable<String,Attribute> attributeByName = new Hashtable<String,Attribute>();
		private LinkedList<String> channels = new LinkedList<String>();
		private String acl = null;
		private HashSet<String> instances = new HashSet<String>();
		
		public Type(IQueryResult res) throws MorseException {
			
			superType = res.getString( "SUPER_TYPE" );
			if ( superType != null ) superType = superType.toLowerCase();
			name      = res.getString( "NAME" ).toLowerCase();
			acl = res.getString( "access_acl" );
			
			if ( log().t3() ) log().info( "Load Type: " + name );
			ITableRead attr = res.getTable( "attribute" );
			Attribute currentAttr = null;
			while ( attr.next() ) {
				Attribute attrx = new Attribute( this, attr );
				
				if ( currentAttr != null && currentAttr.type == IAttribute.AT_TABLE && attrx.name.startsWith( currentAttr.name + '.' ) ) {
					// append sub-table attribute
					currentAttr.appendTableAttr( attrx );
				} else
				{
					// append as new attribute
					if ( attrx.type == IAttribute.AT_TABLE )
						currentAttr = attrx;
				
					attributes.add( attrx );
					attributeByName.put( attrx.name, attrx );
				}
			}
			attr.close();
			
			ITableRead channels = res.getTable( "channel" );
			while ( channels.next() ) {
				this.channels.add( channels.getString( "name" ) );
			}
			channels.close();
		}

		public void dump() {
			log().info( "TYPE " + name + " extends " + superType + " {" );
			for ( Iterator<Attribute> i = getAttributes(); i.hasNext(); )
				i.next().dump( name );
			log().info( "}" );
		}

		public void connectAttr() throws AfPluginException {
			
			// create list of super types
			IType t = this;
			while ( t != null ) {
				instances.add( t.getName() );
				t = t.getSuperType();
			}
			
			if ( superTypeObj != null ) {
				int cnt = 0;
				for ( Iterator i = superTypeObj.attributes.iterator(); i.hasNext(); ) {
					Attribute attr = (Attribute)i.next();
					for ( Iterator j = attributes.iterator(); j.hasNext(); )
						if (((Attribute)j.next()).name.equals( attr.name ) ) {
							log().error( "Can't overwrite attribute name in " + name + ": " + attr.name );
							throw new AfPluginException( 0, "Nö" );
						}
					attributes.add( cnt, attr );
					attributeByName.put( attr.name, attr );
					cnt++;
				}
			}
			
			for ( Iterator i = inheritance.iterator(); i.hasNext(); )
				((Type)i.next()).connectAttr();
			
		}

		public void connectSuper() {
			if ( superType == null ) return;
			superTypeObj = (Type)types.get( superType );
			if ( superTypeObj == null ) {
				if ( ! name.equals( IType.TYPE_OBJECT ) )
					log().error( "Supertype not found for " + name + ": " + superType );
				return;
			}
			superTypeObj.inheritance.add( this );
		}

		public Iterator getAttributes() {
			return attributes.iterator();
		}

		public IAttribute getAttribute(String name) {
			return (IAttribute)attributeByName.get( name.toLowerCase() );
		}

		public String getName() {
			return name;
		}

		public IType getSuperType() {
			return superTypeObj;
		}

		public String getSuperName() {
			return superType;
		}

		public boolean isInChannel(IChannelDriver driver) {
			return TypesUtil.isInChannel( channels, driver );
		}

		public Iterator getChannelDefinition() {
			return channels.iterator();
		}

		public String getAccessAcl() {
			return acl;
		}

		public boolean isInstanceOf(String type) {
			return instances.contains( type );
		}
		
		public String[] getSuperTypes() {
			return (String[])instances.toArray( new String[ instances.size() ] );
		}
		
	}
	
	class Attribute implements IAttribute {

		public Attribute masterAttr;
		private int type;
		private String name;
		private String size;
		private String value;
		private String def;
		private Type masterType;
		private Hashtable<String,IAttribute> tableAttrs;
		private LinkedList<IAttribute> tableAttrsSort;
		private int index;
		private String acl;
		private boolean notNull;
		private String acoName;
		private IAco aco;

		public Attribute(Type pMasterType, ITableRead attr) throws MorseException {
			masterType = pMasterType;
			type = attr.getInteger( "TYPE" );
			name = attr.getString( "NAME" ).toLowerCase();
			size = attr.getString( "COL_SIZE" );
			value = attr.getString( "VALUE" );
			def  = attr.getString( "DEF" );
			index = attr.getInteger( "HAS_INDEX" );
			acl  = attr.getString( "ACL" );
			notNull = attr.getBoolean( "NOT_NULL" );
			acoName = attr.getString( "ACO" );
			aco = AcoUtil.getAco( connection, type, acoName );
			
			if ( IAttribute.AT_TABLE == type ) {
				tableAttrs = new Hashtable<String,IAttribute>();
				tableAttrsSort = new LinkedList<IAttribute>();
			}
			aco.init( this );
		}

		public void dump(String name2) {
			log().debug( "  " + name2 + '.' + name + " " + type + '(' + size + ") - " + masterType.getName() + ' ' + acoName + ' ' + aco  );
			if ( isTable() ) {
				log().debug( "  {" );
				for ( Iterator i = getAttributes(); i.hasNext(); )
					((Attribute)i.next()).dump( "  " + name );
				log().debug( "  }" );
			}
		}

		public void prepareTableAttr(Attribute currentAttr) {
			name = name.substring( currentAttr.name.length() + 1 );
			masterAttr = currentAttr;
		}

		public void appendTableAttr(Attribute attrx) {
			
			attrx.prepareTableAttr( this );
			tableAttrs.put( attrx.name, attrx );
			tableAttrsSort.add( attrx );
		}

		public String getName() {
			return name;
		}

		public boolean isTable() {
			return tableAttrs != null;
		}
		
		public Iterator getAttributes() {
			return tableAttrsSort.iterator();
		}

		public IAttribute getAttribute(String name) {
			return (IAttribute)tableAttrs.get( name );
		}

		public String getCanonicalName() {
			if ( masterAttr == null )
				return masterType.name + '.' + name;
			else
				return masterType.name + '.' + masterAttr.name + '.' + name;
		}

		public int getType() {
			return type;
		}

		public IType getSourceType() {
			return masterType;
		}
		
		public IAttribute getSourceAttribute() {
			return masterAttr;
		}
		public int getSize() {
			if ( size == null || size.length() == 0 )
				return 0;
			return Integer.parseInt( size );
		}

		public String getAccessAcl() {
			return acl;
		}

		public int getIndexType() {
			return index;
		}

		public boolean isNotNull() {
			return notNull;
		}

		public String getDefaultValue() {
			return def;
		}

		public String getExtraValue() {
			return value;
		}

		public IAco getAco() {
			return aco;
		}

		public String getAcoName() {
			return acoName;
		}
	}

	public Iterator getTypes() {
		return types.values().iterator();
	}

	public IAttribute getAttributeByCanonicalName(String canonical) {
		int pos = canonical.indexOf( '.' );
		if ( pos < 0 ) {
			if ( IAttribute.M_POS.equals( canonical ) ) return IAttributeDefault.ATTR_OBJ_M_POS;
			return null;
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
