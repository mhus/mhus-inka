package de.mhus.aqua.api;

public interface IUserRights {

	public static final int ROLE  = 0;
	public static final int GROUP = 1;
	public static final int USER = 2;
	public static final int POLICY = 3;
	public static final int ROLE_PATTERN  = 10;
	public static final int GROUP_PATTERN = 11;
	public static final int USER_PATTERN = 12;
		
	public static final int DENY  = 0;
	public static final int ALLOW = 1;

	public static final String READ  = "read";
	public static final String WRITE = "write";
	public static final String SHOW = "show";
	public static final String EDIT = "edit";
	
	
	boolean containsRole(String role);


	boolean containsGroup(String group);


	boolean contains(int rg, String rgName);

	
}
