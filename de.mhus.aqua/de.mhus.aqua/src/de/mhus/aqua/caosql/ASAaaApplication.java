package de.mhus.aqua.caosql;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.LinkedList;

import de.mhus.aqua.api.AquaSession;
import de.mhus.aqua.cao.AquaConnection;
import de.mhus.lib.MException;
import de.mhus.lib.cao.CaoApplication;
import de.mhus.lib.cao.CaoConnection;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.util.LinkedCaoList;
import de.mhus.lib.sql.DbConnection;
import de.mhus.lib.sql.DbStatement;

public class ASAaaApplication extends CaoApplication<AquaSession> {

	protected ASAaaApplication(AquaConnection connection) throws CaoException {
		super(connection);
	}

	@Override
	public CaoElement<AquaSession> queryTree(String name, AquaSession access, String... attributes)
			throws CaoException {
		try {
			if (AquaConnection.TREE_ACL.equals(name)) {
				String aclId = attributes[0];
				DbConnection db = ((ASConnection)getConnection()).getPool().getConnection(ASConnection.DB_NAME);
				DbStatement sth = db.getStatement(ASConnection.QUERY_ACL);
				HashMap<String, Object> attr = new HashMap<String, Object>();
				attr.put("id", aclId);
				ResultSet res = sth.executeQuery(attr);
				if (res.next()) {
					ASElement element = new ASElement((AquaConnection) getConnection(),res);
					res.close();
					db.close();
					return element;
				} else {
					res.close();
					db.close();
					throw new MException("acl not found",aclId);
				}
			}
			if (AquaConnection.TREE_USER.equals(name)) {
				String userId = attributes[0];
				DbConnection db = ((ASConnection)getConnection()).getPool().getConnection(ASConnection.DB_NAME);
				DbStatement sth = db.getStatement(ASConnection.QUERY_USER);
				HashMap<String, Object> attr = new HashMap<String, Object>();
				attr.put("id", userId);
				ResultSet res = sth.executeQuery(attr);
				if (res.next()) {
					ASElement element = new ASElement((AquaConnection) getConnection(),res);
					res.close();
					db.close();
					return element;
				} else {
					res.close();
					db.close();
					throw new MException("user not found",userId);
				}
			}
		} catch (Exception e) {
			throw new CaoException(e);
		}
		return null;
	}

	@Override
	public CaoList<AquaSession> queryList(String name, AquaSession access, String... attributes)
			throws CaoException {
		try {
			if (AquaConnection.LIST_RIGHTS.equals(name)) {
				String userId = attributes[0];
				DbConnection db = ((ASConnection)getConnection()).getPool().getConnection(ASConnection.DB_NAME);
				DbStatement sth = db.getStatement(ASConnection.QUERY_USER_RIGHTS);
				HashMap<String, Object> attr = new HashMap<String, Object>();
				attr.put("user", userId);
				ResultSet res = sth.executeQuery(attr);
				LinkedList<CaoElement<AquaSession>> data = new LinkedList<CaoElement<AquaSession>>();
				while (res.next()) {
					data.add(new ASElement((AquaConnection) getConnection(),res));
				}
				res.close();
				db.close();
				
				return new LinkedCaoList<AquaSession>(getConnection(), data);
				
			}
		} catch (Exception e) {
			throw new CaoException(e);
		}
			return null;
	}

}
