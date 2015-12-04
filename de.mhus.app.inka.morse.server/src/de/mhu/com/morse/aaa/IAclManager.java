package de.mhu.com.morse.aaa;

import de.mhu.com.morse.types.IType;
import de.mhu.com.morse.usr.UserInformation;
import de.mhu.lib.plugin.IAfPpi;

public interface IAclManager extends IAfPpi {

	public static final String ADMINISTRATOR = "administrator";
	
	public boolean hasRead( UserInformation user, String acl );
	public boolean hasWrite( UserInformation user, String acl );
	public boolean hasCreate( UserInformation user, String acl );
	public boolean hasDelete( UserInformation user, String acl );
	public boolean hasGrant( UserInformation user, String acl );
	public boolean hasAdmin( UserInformation user, String acl );
	public boolean hasVersion( UserInformation user, String acl );
	public boolean hasLoad( UserInformation user, String acl );
	public boolean hasSave( UserInformation user, String acl );
	public boolean hasExec( UserInformation user, String acl );
	public boolean isAdministrator( UserInformation user );
	
	
	public String getNewObjectAcl(UserInformation user, IType type);
	public String getNewContentAcl(UserInformation user, IType type);
	
}
