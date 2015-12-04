package de.mhus.aqua.cao;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import de.mhus.aqua.Activator;
import de.mhus.aqua.aaa.Acl;
import de.mhus.aqua.api.AquaApplication;
import de.mhus.aqua.api.AquaSession;
import de.mhus.aqua.api.IAcl;
import de.mhus.aqua.api.IUser;
import de.mhus.aqua.caosql.ASElement;
import de.mhus.lib.MCast;
import de.mhus.lib.MDate;
import de.mhus.lib.MException;
import de.mhus.lib.MString;
import de.mhus.lib.MXml;
import de.mhus.lib.cao.CaoConnection;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoMetaDefinition;
import de.mhus.lib.cao.CaoMetaDefinition.TYPE;
import de.mhus.lib.cao.CaoMetadata;
import de.mhus.lib.cao.CaoWritableElement;
import de.mhus.lib.config.IConfig;
import de.mhus.lib.config.XmlConfig;
import de.mhus.lib.logging.Log;
import de.mhus.lib.sql.DbConnection;
import de.mhus.lib.sql.DbStatement;

public abstract class AquaElement extends CaoElement<AquaSession> {

	private Acl acl;

	protected AquaElement(AquaConnection connection) throws CaoException {
		super(connection);
	}

	public AquaApplication getApplication() throws CaoException {
		try {
			return Activator.getAqua().getAquaApplication(getString("application"));
		} catch (Exception e) {
			throw new CaoException(e);
		}
	}

	@Override
	public CaoWritableElement<AquaSession> getWritableNode() throws CaoException {
		return null;
	}

	@Override
	public boolean isLocked(boolean arg0) throws CaoException {
		return false;
	}

	@Override
	public boolean isNode() {
		return true;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public boolean isWritable() {
		return false;
	}

	@Override
	public boolean lock(int arg0) throws CaoException {
		return false;
	}

	@Override
	public boolean unlock() throws CaoException {
		return false;
	}

	public abstract AquaElement getChild(String name, AquaSession access) throws CaoException;

	public abstract IConfig getApplicationConfig(IUser user) throws MException;
	
	public IAcl getAcl() throws CaoException{
		if (acl == null)
			try {
				acl = Activator.getAqua().getAcl(getString("acl"));
			} catch (Exception e) {
				throw new CaoException(e);
			}
		return acl;
	}

	public abstract AquaElement getExtendedNode(String ext);

	/**
	 * Set or remove the application config for a specified user.
	 * 
	 * @param user
	 * @param config the config or null to remove it.
	 * @throws CaoException
	 */
	public abstract void setApplicationConfig(IUser user, IConfig config) throws CaoException;

	public abstract void setApplicationConfig(IConfig config) throws CaoException;
	
}
