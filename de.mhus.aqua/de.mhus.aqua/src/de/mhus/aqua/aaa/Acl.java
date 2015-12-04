package de.mhus.aqua.aaa;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import de.mhus.aqua.Activator;
import de.mhus.aqua.api.AquaSession;
import de.mhus.aqua.api.IAcl;
import de.mhus.aqua.api.IUser;
import de.mhus.aqua.cao.AquaConnection;
import de.mhus.aqua.cao.AquaDriver;
import de.mhus.lib.MException;
import de.mhus.lib.cao.CaoApplication;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.logging.Log;
import de.mhus.lib.sql.DbConnection;
import de.mhus.lib.sql.DbStatement;

/**
 * The class represent a set of right definitions for one resource.
 * Rights could be read,write,new version,delete,execute etc. The set of rights are not
 * defined by this class. It's free!
 * 
 * @author mikehummel
 *
 */
public class Acl implements IAcl {

	private static Log log = Log.getLog(Acl.class);
	
	private Map<String, Rule> rights = new HashMap<String, Rule>();
	private int policy;
	private String aclId;
	private AquaConnection con;
	private HashMap<String, Boolean> cache = new HashMap<String, Boolean>();

	public Acl(String aclId) throws Exception {
		this.con = Activator.getAqua().getCaoConnection();
		this.aclId = aclId;
		reload();
	}
	
	public int getPolicy() {
		return policy;
	}

	/**
	 * Check if a user has the right in this acl. Use the session to validate admin rights.
	 * 
	 * @param session If possible give the session to allow admin rights. Admin rights are only given if the
	 * admin is in the session is activated.
	 * @param user The user object.
	 * @param right The asked right (lower case needed!).
	 * @return True if the right is granted.
	 */
	@Override
	public boolean hasRight(AquaSession session, String right) {
		
		//TODO cache !!!!!
		IUser user = session.getUser();
		if (user.isAdmin() && session.isAdminActive() ) return true;
		
		return internalHasRight(user, right);
	}

	/**
	 * Check if a user has the right in this acl. If the user is admin the right is granted.
	 * 
	 * @param user The user object.
	 * @param right The asked right (lower case needed!).
	 * @return True if the right is granted.
	 */
	@Override
	public boolean hasRight(User user, String right) {
		
		if (user.isAdmin()) return true;
		
		return internalHasRight(user, right);
	}
	
	protected boolean internalHasRight(IUser user, String right) {

		synchronized (cache) {
			Boolean ret = cache.get(user.getId() + " " + right);
			if (ret != null ) return ret;			
		}
		
		Rule rule = rights.get(right);
		if (rule == null) return policy == UserRights.ALLOW;
		
		Boolean ret = rule.getRule().validate(user.getRights()) == UserRights.ALLOW;
		synchronized (cache) {
			cache.put(user.getId() + " " + right, ret);
		}
		return ret;
	}

	@Override
	public String getId() {
		return aclId;
	}
	
		
		public void reload() throws Exception {
			CaoApplication<AquaSession> app = con.getApplication(Activator.getAqua().getRootSession(),AquaDriver.APP_AAA);
			CaoElement<AquaSession> element = app.queryTree(AquaConnection.TREE_ACL, Activator.getAqua().getRootSession(), aclId);
			if (element == null) {
				log.t("acl not found", aclId);
				throw new AclNotFoundException();
			}
			policy = element.getInt("policy",0);
			
			CaoList<AquaSession> list = element.getList(AquaConnection.LIST_RULES,Activator.getAqua().getRootSession());
			for (CaoElement<AquaSession> res : list.getElements()) {
				String rights = res.getString("right_name");
				int sort = res.getInt("sort",0);
				int rg = res.getInt("rg",0);
				String rgName = res.getString("rg_name");
				int rgPolicy = res.getInt("rg_policy",0);
				
				appendRuleData(rights,rg,rgName,rgPolicy);
				
			}
			
		}

		/**
		 * Append a rule to the acl.
		 * 
		 * @param rights a comma separated list of rights where to add the new rule. Rights identifier are lower case !!
		 * @param rg The kind of rule @see UserRights for details
		 * @param rgName Name of the role/group which is affected
		 * @param rgPolicy If the test is positive the returned policy (DENY/ALLOW) @see UserRights
		 */
		protected void appendRuleData(String rights, int rg, String rgName,
				int rgPolicy) {
			
			String[] rightsArray = rights.split(",");
			for (String right : rightsArray) {
				right = right.trim().toLowerCase();
				Rule rule = this.rights.get(right);
				if (rule == null) {
					rule = new Rule(right, this);
					this.rights.put(right, rule);
				}
				rule.append(rg,rgName,rgPolicy);
			}
		}
		
		public String toString() {
			return getId() + " " + rights.toString();
		}
		
	
	
}
