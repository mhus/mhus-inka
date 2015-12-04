package de.mhu.com.morse.types;

import java.util.Iterator;

import de.mhu.com.morse.aco.IAco;

public interface IAttribute {

	public static final int AT_STRING = 0;
	public static final int AT_TABLE = 1;
	public static final int AT_INT = 2;
	public static final int AT_DATE = 3;
	public static final int AT_ID = 4;
	public static final int AT_STRING_RAW = 5;
	public static final int AT_ACL = 6;
	public static final int AT_BOOLEAN = 7;
	public static final int AT_LONG = 8;
	public static final int AT_DOUBLE = 10;
	
	public static final String[] TITLES_AT = new String[] { "string", "table", "int", "date", "id", "raw", "acl", "boolean", "long", "?", "double" };
	
	public static final int STRING_MAX_SIZE = 600;
	
	public static final int INDEX_NONE = 0;
	public static final int INDEX_NORMAL = 1;
	public static final int INDEX_UNIQUE = 2;

	public static final String M_ID="m_id";
	public static final String M_POS = "m_pos";
	public static final String M_ACL = "m_acl";
	public static final String M_TYPE = "m_type";	
	public static final String M_STAMP = "m_stamp";
	
	public static final String ACO_M_ID = "m_id";
	public static final String ACO_M_POS = "m_pos";
	public static final String ACO_STRING = "string";
	public static final String ACO_ENUM = "enum";
	public static final String MC_FORMAT = "format";
	public static final String ACO_INT = "int";
	public static final String ACO_LONG = "long";
	public static final String ACO_DOUBLE = "double";
	public static final String ACO_BOOLEAN = "boolean";
	public static final String MC_CONTENT = "content";
	public static final String M_LOCK = "m_lock";
	
	public String getName();
	
	public boolean isTable();
	public Iterator<IAttribute> getAttributes();
	public IAttribute getAttribute(String name);

	public String getCanonicalName();

	public int getType();
	public IType getSourceType();
	public IAttribute getSourceAttribute();
	
	public int getSize();
	
	public int getIndexType();
	public boolean isNotNull();
	public String getAccessAcl();
	public String getDefaultValue();
	public String getExtraValue();
	public String getAcoName();
	public IAco getAco();
	

}
