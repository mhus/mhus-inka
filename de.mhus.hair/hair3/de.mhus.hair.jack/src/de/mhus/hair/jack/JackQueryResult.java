package de.mhus.hair.jack;

import java.util.List;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import de.mhus.hair.jack.JackQueryResult.JackQueryList;
import de.mhus.lib.cao.CaoAccess;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoListIterator;
import de.mhus.lib.cao.CaoMetaDefinition;
import de.mhus.lib.cao.CaoMetaDefinition.TYPE;
import de.mhus.lib.cao.CaoMetadata;
import de.mhus.lib.cao.CaoWritableElement;
import de.mhus.lib.cao.util.MutableElement;
import de.mhus.lib.cao.util.MutableMetadata;

public class JackQueryResult extends CaoElement {

	private String queryString;
	private String language;

	public JackQueryResult(JackApplication jackApplication, String language, String queryString) throws CaoException {
		super(jackApplication);
		this.language = language;
		this.queryString = queryString;
	}

	@Override
	public CaoList getChildren(CaoAccess access) throws CaoException {
				
		try {
			return new JackQueryList();
		} catch (RepositoryException e) {
			throw new CaoException(e);
		}
	}

	@Override
	public boolean isNode() {
		return true;
	}

	@Override
	public String getId() throws CaoException {
		return null;
	}

	@Override
	public String getName() throws CaoException {
		return null;
	}

	@Override
	public CaoMetadata getMetadata() {
		return null;
	}

	@Override
	public String getString(String name) throws CaoException {
		return null;
	}

	@Override
	public CaoList getList(String name, CaoAccess access, String... attributes)
			throws CaoException {
		return null;
	}

	@Override
	public Object getObject(String name, String... attributes)
			throws CaoException {
		return null;
	}

	@Override
	public boolean isWritable() {
		return false;
	}

	@Override
	public CaoWritableElement getWritableNode() throws CaoException {
		return null;
	}

	@Override
	public void reload() throws CaoException {

	}

	@Override
	public boolean lock(int timeout) throws CaoException {
		return false;
	}

	@Override
	public boolean unlock() throws CaoException {
		return false;
	}

	@Override
	public boolean isLocked(boolean owner) throws CaoException {
		return false;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	class JackQueryList extends CaoList {

		private QueryResult result;
		private MutableMetadata meta;

		protected JackQueryList() throws RepositoryException {
			super(JackQueryResult.this, queryString);
						
			Query query;

				query = ((JackConnection)getConnection()).getSession()
					.getWorkspace().getQueryManager().createQuery(queryString.trim(),language);
				result = query.execute();
			

			meta = new MutableMetadata(getDriver());
			List<CaoMetaDefinition> map = meta.getMap();
			
			for (String colName : result.getColumnNames()) {
				map.add(new CaoMetaDefinition(meta, colName, TYPE.STRING, null, Integer.MAX_VALUE));
			}
			
		}

		@Override
		public CaoMetadata getMetadata() {
			return meta;
		}

		@Override
		public int size() {
			return -1;
		}

		@Override
		public CaoListIterator getElements() {
			try {
				return new ResultIterator(this);
			} catch (CaoException e) {
				e.printStackTrace();
				return null;
			}
		}
		
	}
	
	private class ResultIterator extends CaoListIterator {

		private RowIterator rows;
		private JackQueryList list;

		public ResultIterator(JackQueryList list) throws CaoException {
			super(list.getConnection());
			this.list = list;
			reset();
		}

		@Override
		protected CaoElement nextElement() throws CaoException {
			// return new JackElement(list.getApplication(),rows.nextRow(),null);
			MutableElement out = new MutableElement(list.getApplication());
			out.setMetadata(list.meta);
			Row row = rows.nextRow();
			for (CaoMetaDefinition m : list.meta) {
				try {
					out.setString(m.getName(), row.getValue(m.getName()).getString());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return out;
		}

		@Override
		protected boolean hasNextElement() throws CaoException {
			return rows.hasNext();
		}

		@Override
		public void reset() throws CaoException {
			try {
				rows = list.result.getRows();
			} catch (RepositoryException e) {
				throw new CaoException(e);
			}
		}
		
	}
}
