package de.mhu.com.morse.channel;

public interface IObjectListener {

	public void eventObjectCreated( String channel, String id, String type );
	public void eventObjectUpdated( String channel, String id, String type, String[] attributes );
	public void eventObjectDeleted( String channel, String id, String type );
	public void eventContentSaved  ( String channel, String id, String parentId, String parentType );
	public void eventContentRemoved( String channel, String id, String parentId, String parentType );
	
}
