package de.mhu.com.morse.cmd;

import java.util.Iterator;

import de.mhu.com.morse.cmd.ifc.Command;
import de.mhu.com.morse.cmd.ifc.IMessageDelegator;
import de.mhu.com.morse.cmd.ifc.Weak;
import de.mhu.com.morse.net.IMessage;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.types.IType;
import de.mhu.com.morse.types.ITypes;
import de.mhu.lib.plugin.AfPlugin;
import de.mhu.lib.plugin.AfPluginException;

public class TypesCmd extends AfPlugin {

	private ITypes types;

	protected void apDestroy() throws Exception {
		// TODO Auto-generated method stub
		
	}

	protected void apDisable() throws AfPluginException {
		// TODO Auto-generated method stub
		
	}

	protected void apEnable() throws AfPluginException {
		types = (ITypes)getSinglePpi( ITypes.class );
	}

	protected void apInit() throws Exception {
		IMessageDelegator sysMd = (IMessageDelegator)getSinglePpi( IMessageDelegator.class );
		
		sysMd.registerCommand( new AttrAllCmd() );
		sysMd.registerCommand( new AttrByNameCmd() );
		
	}

	private class AttrAllCmd extends Command {
		AttrAllCmd() {
			super( "a.a" );
		}

		public void doAction(IMessage msg, Weak weak) throws Exception {
			IMessage ret = msg.getClient().createMessage();
			for ( Iterator i = types.getTypes(); i.hasNext(); )
				ret.append( ((IType)i.next()).getName() );
			replayToUser( weak, ret );
		}
	}
	
	private class AttrByNameCmd extends Command {
		AttrByNameCmd() {
			super( "a.n" );
		}

		public void doAction(IMessage msg, Weak weak) throws Exception {
			
			IType type = types.get( msg.getString( 0 ) );
			IMessage ret = msg.getClient().createMessage();
			if ( type != null ) {
				ret.append( type.getSuperName() );
				ret.append( type.getAccessAcl() );
				for ( Iterator i = type.getAttributes(); i.hasNext(); ) {
					IAttribute attr = (IAttribute)i.next();
					ret.append( 1 );
					ret.append( attr.getName() );
					ret.append( attr.getType() );
					ret.append( attr.getSize() );
					ret.append( attr.getCanonicalName() );
					ret.append( attr.getAcoName() );
					ret.append( attr.getIndexType() );
					ret.append( attr.isNotNull() ? 1 : 0 );
					ret.append( attr.getDefaultValue() );
					ret.append( attr.getExtraValue() );
					ret.append( attr.getAccessAcl() );
					ret.append( attr.getSourceType().getName() );
					if ( attr.isTable() ) {
						for ( Iterator j = attr.getAttributes(); j.hasNext(); ) {
							IAttribute tAttr = (IAttribute)j.next();
							ret.append( 1 );
							ret.append( tAttr.getName() );
							ret.append( tAttr.getType() );
							ret.append( tAttr.getSize() );
							ret.append( tAttr.getCanonicalName() );
							
							ret.append( tAttr.getAcoName() );
							ret.append( tAttr.getIndexType() );
							ret.append( tAttr.isNotNull() ? 1 : 0 );
							ret.append( tAttr.getDefaultValue() );
							ret.append( tAttr.getExtraValue() );
							ret.append( tAttr.getAccessAcl() );
							
						}
					}
					ret.append( 0 );
				}
				ret.append( 0 );
				
				for ( Iterator i = type.getChannelDefinition(); i.hasNext(); ) {
					ret.append( 1 );
					ret.append( (String)i.next() );
				}
				ret.append( 0 );
				
				String[] superTypes = type.getSuperTypes();
				ret.append( superTypes.length );
				for ( int i = 0; i < superTypes.length; i++ )
					ret.append( superTypes[ i ] );
				
			}
			replayToUser( weak, ret );
		}
	}
	
}
