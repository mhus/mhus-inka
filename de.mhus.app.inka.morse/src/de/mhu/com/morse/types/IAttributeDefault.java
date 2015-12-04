package de.mhu.com.morse.types;

import java.util.Iterator;

import de.mhu.lib.log.AL;
import de.mhu.com.morse.aco.AcoBoolean;
import de.mhu.com.morse.aco.AcoDouble;
import de.mhu.com.morse.aco.AcoInt;
import de.mhu.com.morse.aco.AcoLong;
import de.mhu.com.morse.aco.AcoMId;
import de.mhu.com.morse.aco.AcoMPos;
import de.mhu.com.morse.aco.AcoString;
import de.mhu.com.morse.aco.IAco;

public class IAttributeDefault {

private static AL log = new AL( IAttributeDefault.class );
	
public static final IAttribute ATTR_OBJ_M_POS = new IAttribute() {
		
		private IAco aco = new AcoMPos();
	
		{
			try {
				aco.init( this );
			} catch ( Exception e ) {
				log.error( e );
			}
		}
		
		public IAttribute getAttribute(String name) {
			return null;
		}

		public Iterator getAttributes() {
			return null;
		}

		public String getCanonicalName() {
			return M_POS;
		}

		public String getName() {
			return M_POS;
		}

		public int getSize() {
			return 0;
		}

		public IType getSourceType() {
			return null; // no source type
		}

		public int getType() {
			return AT_INT;
		}

		public boolean isTable() {
			return false;
		}

		public String getAccessAcl() {
			return null;
		}

		public int getIndexType() {
			return 0;
		}

		public boolean isNotNull() {
			return true;
		}

		public String getDefaultValue() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getExtraValue() {
			// TODO Auto-generated method stub
			return null;
		}

		public IAttribute getSourceAttribute() {
			return null;
		}

		public IAco getAco() {
			return aco;
		}

		public String getAcoName() {
			return IAttribute.ACO_M_POS;
		}
		
	};
	
	public static final IAttribute ATTR_OBJ_M_ID = new IAttribute() {
		
		private IAco aco = new AcoMId();
		
		{
			try {
				aco.init( this );
			} catch ( Exception e ) {
				log.error( e );
			}
		}

		public IAttribute getAttribute(String name) {
			return null;
		}

		public Iterator getAttributes() {
			return null;
		}

		public String getCanonicalName() {
			return IType.TYPE_OBJECT + '.' + M_ID;
		}

		public String getName() {
			return M_ID;
		}

		public int getSize() {
			return 32;
		}

		public IType getSourceType() {
			return null; // no source type
		}

		public int getType() {
			return AT_ID;
		}

		public boolean isTable() {
			return false;
		}

		public String getAccessAcl() {
			return null;
		}

		public int getIndexType() {
			return 0;
		}

		public boolean isNotNull() {
			return true;
		}

		public String getDefaultValue() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getExtraValue() {
			// TODO Auto-generated method stub
			return null;
		}

		public IAttribute getSourceAttribute() {
			return null;
		}

		public IAco getAco() {
			return aco;
		}

		public String getAcoName() {
			return IAttribute.ACO_M_ID;
		}
		
	};

	public static final IAttribute ATTR_OBJ_STRING = new IAttribute() {
		
		private IAco aco = new AcoString();
		private String name = ".str";
	
		{
			try {
				aco.init( this );
			} catch ( Exception e ) {
				log.error( e );
			}
		}
		
		public IAttribute getAttribute(String name) {
			return null;
		}

		public Iterator getAttributes() {
			return null;
		}

		public String getCanonicalName() {
			return name;
		}

		public String getName() {
			return name;
		}

		public int getSize() {
			return IAttribute.STRING_MAX_SIZE;
		}

		public IType getSourceType() {
			return null; // no source type
		}

		public int getType() {
			return AT_STRING;
		}

		public boolean isTable() {
			return false;
		}

		public String getAccessAcl() {
			return null;
		}

		public int getIndexType() {
			return 0;
		}

		public boolean isNotNull() {
			return false;
		}

		public String getDefaultValue() {
			return "";
		}

		public String getExtraValue() {
			return null;
		}

		public IAttribute getSourceAttribute() {
			return null;
		}

		public IAco getAco() {
			return aco;
		}

		public String getAcoName() {
			return IAttribute.ACO_STRING;
		}
		
	};

	public static final IAttribute ATTR_OBJ_INT = new IAttribute() {
		
		private IAco aco = new AcoInt();
		private String name = ".int";
	
		{
			try {
				aco.init( this );
			} catch ( Exception e ) {
				log.error( e );
			}
		}
		
		public IAttribute getAttribute(String name) {
			return null;
		}

		public Iterator getAttributes() {
			return null;
		}

		public String getCanonicalName() {
			return name;
		}

		public String getName() {
			return name;
		}

		public int getSize() {
			return 0;
		}

		public IType getSourceType() {
			return null; // no source type
		}

		public int getType() {
			return AT_INT;
		}

		public boolean isTable() {
			return false;
		}

		public String getAccessAcl() {
			return null;
		}

		public int getIndexType() {
			return 0;
		}

		public boolean isNotNull() {
			return false;
		}

		public String getDefaultValue() {
			return "0";
		}

		public String getExtraValue() {
			return null;
		}

		public IAttribute getSourceAttribute() {
			return null;
		}

		public IAco getAco() {
			return aco;
		}

		public String getAcoName() {
			return IAttribute.ACO_INT;
		}
		
	};

	public static final IAttribute ATTR_OBJ_LONG = new IAttribute() {
		
		private IAco aco = new AcoLong();
		private String name = ".long";
	
		{
			try {
				aco.init( this );
			} catch ( Exception e ) {
				log.error( e );
			}
		}
		
		public IAttribute getAttribute(String name) {
			return null;
		}

		public Iterator getAttributes() {
			return null;
		}

		public String getCanonicalName() {
			return name;
		}

		public String getName() {
			return name;
		}

		public int getSize() {
			return 0;
		}

		public IType getSourceType() {
			return null; // no source type
		}

		public int getType() {
			return AT_LONG;
		}

		public boolean isTable() {
			return false;
		}

		public String getAccessAcl() {
			return null;
		}

		public int getIndexType() {
			return 0;
		}

		public boolean isNotNull() {
			return false;
		}

		public String getDefaultValue() {
			return "0";
		}

		public String getExtraValue() {
			return null;
		}

		public IAttribute getSourceAttribute() {
			return null;
		}

		public IAco getAco() {
			return aco;
		}

		public String getAcoName() {
			return IAttribute.ACO_LONG;
		}
		
	};

	public static final IAttribute ATTR_OBJ_DOUBLE = new IAttribute() {
		
		private IAco aco = new AcoDouble();
		private String name = ".double";
	
		{
			try {
				aco.init( this );
			} catch ( Exception e ) {
				log.error( e );
			}
		}
		
		public IAttribute getAttribute(String name) {
			return null;
		}

		public Iterator getAttributes() {
			return null;
		}

		public String getCanonicalName() {
			return name;
		}

		public String getName() {
			return name;
		}

		public int getSize() {
			return 0;
		}

		public IType getSourceType() {
			return null; // no source type
		}

		public int getType() {
			return AT_DOUBLE;
		}

		public boolean isTable() {
			return false;
		}

		public String getAccessAcl() {
			return null;
		}

		public int getIndexType() {
			return 0;
		}

		public boolean isNotNull() {
			return false;
		}

		public String getDefaultValue() {
			return "0";
		}

		public String getExtraValue() {
			return null;
		}

		public IAttribute getSourceAttribute() {
			return null;
		}

		public IAco getAco() {
			return aco;
		}

		public String getAcoName() {
			return IAttribute.ACO_DOUBLE;
		}
		
	};
	
	public static final IAttribute ATTR_OBJ_BOOLEAN = new IAttribute() {
		
		private IAco aco = new AcoBoolean();
		private String name = ".boolean";
	
		{
			try {
				aco.init( this );
			} catch ( Exception e ) {
				log.error( e );
			}
		}
		
		public IAttribute getAttribute(String name) {
			return null;
		}

		public Iterator getAttributes() {
			return null;
		}

		public String getCanonicalName() {
			return name;
		}

		public String getName() {
			return name;
		}

		public int getSize() {
			return 0;
		}

		public IType getSourceType() {
			return null; // no source type
		}

		public int getType() {
			return AT_BOOLEAN;
		}

		public boolean isTable() {
			return false;
		}

		public String getAccessAcl() {
			return null;
		}

		public int getIndexType() {
			return 0;
		}

		public boolean isNotNull() {
			return false;
		}

		public String getDefaultValue() {
			return "0";
		}

		public String getExtraValue() {
			return null;
		}

		public IAttribute getSourceAttribute() {
			return null;
		}

		public IAco getAco() {
			return aco;
		}

		public String getAcoName() {
			return IAttribute.ACO_BOOLEAN;
		}
		
	};

	public static IAttribute getAttribute(String name) {
		
		if ( ATTR_OBJ_STRING.getName().equals( name ) )
			return ATTR_OBJ_STRING;
		if ( ATTR_OBJ_INT.getName().equals( name ) )
			return ATTR_OBJ_INT;
		if ( ATTR_OBJ_LONG.getName().equals( name ) )
			return ATTR_OBJ_LONG;
		if ( ATTR_OBJ_DOUBLE.getName().equals( name ) )
			return ATTR_OBJ_DOUBLE;
		if ( ATTR_OBJ_BOOLEAN.getName().equals( name ) )
			return ATTR_OBJ_BOOLEAN;
		
		return null;
	}
	
}
