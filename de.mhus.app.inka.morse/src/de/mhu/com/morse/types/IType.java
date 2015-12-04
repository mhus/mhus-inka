package de.mhu.com.morse.types;

import java.util.Iterator;

import de.mhu.com.morse.channel.IChannelDriver;

public interface IType {

	public static final String TYPE_OBJECT = "m_object";
	public static final String TYPE_ACL = "m_acl";
	public static final String TYPE_C_OBJECT = "mc_object";
	public static final String TYPE_MC_CONTENT = "mc_content";
	public static final String TYPE_C_DOCUMENT = "mc_document";
	public static final String TYPE_C_FOLDER = "mc_folder";
	public static final String TYPE_TYPE = "m_type";
	public static final String TYPE_MC_OBJECT = "mc_object";

	public Iterator<IAttribute> getAttributes();

	public IAttribute getAttribute(String name);

	public String getName();

	public IType getSuperType();

	public String getSuperName();

	public boolean isInChannel(IChannelDriver driver);

	public Iterator<String> getChannelDefinition();

	public String getAccessAcl();

	public boolean isInstanceOf(String type);

	public String[] getSuperTypes();
	
}
