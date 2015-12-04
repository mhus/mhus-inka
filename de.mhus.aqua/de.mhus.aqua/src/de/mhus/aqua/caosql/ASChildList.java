package de.mhus.aqua.caosql;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import de.mhus.aqua.Activator;
import de.mhus.aqua.aaa.Acl;
import de.mhus.aqua.aaa.UserRights;
import de.mhus.aqua.api.AquaSession;
import de.mhus.aqua.cao.AquaChildList;
import de.mhus.aqua.cao.AquaConnection;
import de.mhus.aqua.cao.AquaElement;
import de.mhus.aqua.core.Aqua;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoListIterator;
import de.mhus.lib.cao.CaoMetadata;
import de.mhus.lib.sql.DbConnection;

public class ASChildList extends AquaChildList {

	private static de.mhus.lib.logging.Log log = de.mhus.lib.logging.Log
			.getLog(ASChildList.class);

	public ASChildList(AquaElement parent) throws CaoException {
		super(parent);
	}
	
	@Override
	public CaoListIterator getElements() {
		try {
			return new MyIterator();
		} catch (CaoException e) {
			log.d(e);
			return new CaoListIterator(getConnection()) {
				
				@Override
				public void reset() throws CaoException {}
				
				@Override
				protected CaoElement nextElement() throws CaoException {
					return null;
				}
				
				@Override
				protected boolean hasNextElement() throws CaoException {
					return false;
				}
			};
		}
	}

	@Override
	public CaoMetadata getMetadata() {
		return parent.getMetadata();
	}

	@Override
	public CaoElement getParent() {
		return parent;
	}

	@Override
	public int size() {
		return 0;
	}

	private class MyIterator extends CaoListIterator {

		private Iterator<Integer> iter;

		protected MyIterator() throws CaoException {
			super(parent.getConnection());
			reset();
		}

		@Override
		protected boolean hasNextElement() throws CaoException {
			return iter.hasNext();
		}

		@Override
		protected CaoElement nextElement() throws CaoException {
			return new ASNode((AquaConnection) getConnection(), iter.next() );
		}

		@Override
		public void reset() throws CaoException {
			try {
				Aqua aqua = Activator.getAqua();
				
				DbConnection db = ((ASConnection)getConnection()).getPool().getConnection(ASConnection.DB_NAME);
				HashMap<String, Object> attributes = new HashMap<String, Object>();
				attributes.put("id", parent.getId());
				ResultSet res = db.getStatement(ASConnection.QUERY_NODE_CHILDREN).executeQuery(attributes);
				LinkedList<Integer> values = new LinkedList<Integer>();
				while (res.next()) {
					if (session != null) {
						Acl acl = aqua.getAcl(res.getString("acl"));
						if (acl.hasRight(session, right ))
							values.add(res.getInt("id"));
					} else
						values.add(res.getInt("id"));
				}
				iter = values.iterator();
				res.close();
				db.close();
			} catch (Exception e) {
				throw new CaoException(e);
			}
		}
		
	}
}
