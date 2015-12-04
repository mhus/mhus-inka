package de.mhu.com.morse.channel.sql;

import java.lang.reflect.Method;
import java.util.LinkedList;

import de.mhu.com.morse.channel.IQueryFunction;
import de.mhu.com.morse.types.IAttribute;

public class Attr {

	public String attrName;
	public Table table;
	public IAttribute attr;
	public String alias;
	public String name;
	public int updateType;
	public int updateIndex;
	public String orgName;
	public LinkedList<String> function = null;
	public IQueryFunction functionObject;
	public Method functionMethod;
	public String[] functionInit;
	
}
