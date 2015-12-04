package de.mhus.aqua.caosql;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

import de.mhus.aqua.Activator;
import de.mhus.aqua.aaa.Acl;
import de.mhus.aqua.api.AquaApplication;
import de.mhus.aqua.api.AquaRequest;
import de.mhus.aqua.api.AquaSession;
import de.mhus.aqua.api.IAcl;
import de.mhus.aqua.api.IUser;
import de.mhus.aqua.cao.AquaConnection;
import de.mhus.aqua.cao.AquaElement;
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
import de.mhus.lib.cao.util.LinkedCaoList;
import de.mhus.lib.cao.CaoMetadata;
import de.mhus.lib.cao.CaoWritableElement;
import de.mhus.lib.config.IConfig;
import de.mhus.lib.config.XmlConfig;
import de.mhus.lib.logging.Log;
import de.mhus.lib.sql.DbConnection;
import de.mhus.lib.sql.DbStatement;

public class ASElement extends AquaElement {

//	private static Log log = Log.getLog(ASElement.class);
	private CaoMetadata meta;
	private String id;
	private HashMap<String, Object> data = new HashMap<String, Object>();
	
	public ASElement(AquaConnection connection, ResultSet res)  throws Exception {
		super(connection);
		load(res);
	}

	public ASElement(AquaConnection connection) throws CaoException {
		super(connection);
	}

	protected void load(final ResultSet res) throws Exception {
		meta = new CaoMetadata(getConnection().getDriver()) {
			{
				ResultSetMetaData dbMeta = res.getMetaData();
				for (int i = 1; i <= dbMeta.getColumnCount(); i++) {
					CaoMetaDefinition.TYPE type = TYPE.STRING;
					switch(dbMeta.getColumnType(i)) {
					case Types.TIME:
					case Types.DATE:
						type = TYPE.DATETIME;
						break;
					case Types.INTEGER:
					case Types.BIGINT:
						type = TYPE.LONG;
						break;
					case Types.FLOAT:
					case Types.DOUBLE:
						type = TYPE.DOUBLE;
						break;
					case Types.BOOLEAN:
						type = TYPE.BOOLEAN;
						break;
					}
					this.definition.add(new CaoMetaDefinition(this, dbMeta.getColumnName(i), type, dbMeta.getColumnName(i), dbMeta.getColumnDisplaySize(i), new String[0]));
				}
			}
		};
		ResultSetMetaData dbMeta = res.getMetaData();
		for (int i = 1; i <= dbMeta.getColumnCount(); i++) {
			data.put(dbMeta.getColumnName(i), res.getObject(i));
		}
		if (meta.getDefinition("id") != null)
			id = res.getString("id");
	}
	
	@Override
	public boolean getBoolean(String arg0, boolean def) {
		Object val = data.get(arg0);
		if (val == null) return def;
		if (val instanceof Boolean)
			return (Boolean)val;
		
		return MCast.toboolean(String.valueOf(val),false);
	}

	@Override
	public CaoList getChildren(AquaSession access) throws CaoException {
		return new ASChildList(this);
	}

	@Override
	public Calendar getCalendar(String arg0) {
		Calendar c = Calendar.getInstance();
		c.setTime((Date)data.get(arg0));
		return c;
	}

	@Override
	public MDate getDate(String arg0) {
		return new MDate( (Date)data.get(arg0));
	}
	
	@Override
	public double getDouble(String arg0, double def) {
		try {
			return (Double)data.get(arg0);
		} catch (Throwable e) {
			return def;
		}
	}

	@Override
	public String getId() throws CaoException {
		return id;
	}

	@Override
	public CaoList getList(String arg0, AquaSession access, String... arg1) throws CaoException {
		try {
			if (arg0.equals(AquaConnection.LIST_RULES)) {
				HashMap<String, Object> attr = new HashMap<String, Object>();
				attr.put("id", getString("id"));
				DbConnection db = ((ASConnection)getConnection()).getPool().getConnection(ASConnection.DB_NAME);
				DbStatement sth = db.getStatement(ASConnection.QUERY_ACL_RULE);
				ResultSet res = sth.executeQuery(attr);
				LinkedList<CaoElement> data = new LinkedList<CaoElement>();
				while (res.next()) {
					data.add(new ASElement((AquaConnection) getConnection(),res));
				}
				res.close();
				db.close();
				
				return new LinkedCaoList(getConnection(), data);
			}
			return null;
		} catch (Exception e) {
			throw new CaoException(e);
		}
	}

	@Override
	public long getLong(String arg0, long def) {
		Object val = data.get(arg0);
		if (val == null) return def;
		if (val instanceof Number)
			return ((Number)val).longValue();
		return MCast.tolong(String.valueOf(val), def);
	}

	@Override
	public CaoMetadata getMetadata() {
		return meta;
	}

	@Override
	public String getName() throws CaoException {
		return (String)data.get("name");
	}

	public AquaApplication getApplication() throws CaoException {
		try {
			return Activator.getAqua().getAquaApplication(getString("application"));
		} catch (Exception e) {
			throw new CaoException(e);
		}
	}

	@Override
	public Object getObject(String name, String... args) throws CaoException {
		return data.get(name);
	}
	
	
	@Override
	public CaoElement getParent() {
		return null;
	}

	@Override
	public String getString(String arg0) throws CaoException {
		Object out = data.get(arg0);
		if (out == null) return null;
		return String.valueOf(out);
	}


	@Override
	public void reload() throws CaoException {
	}

	@Override
	public AquaElement getChild(String name, AquaSession access) throws CaoException {
		return null;
	}

	@Override
	public IConfig getApplicationConfig(IUser user) throws MException {
		return null;
	}

	@Override
	public AquaElement getExtendedNode(String ext) {
		return null;
	}

	@Override
	public void setApplicationConfig(IUser user, IConfig config)
			throws CaoException {
	}

	@Override
	public void setApplicationConfig(IConfig config) throws CaoException {
	}
	
}
