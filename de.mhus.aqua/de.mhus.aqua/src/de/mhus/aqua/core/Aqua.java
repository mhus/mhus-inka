package de.mhus.aqua.core;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Observer;
import java.util.TimerTask;
import java.util.WeakHashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;

import de.mhus.aqua.Activator;
import de.mhus.aqua.aaa.Acl;
import de.mhus.aqua.aaa.User;
import de.mhus.aqua.aaa.UserRights;
import de.mhus.aqua.api.AquaApplication;
import de.mhus.aqua.api.AquaRequest;
import de.mhus.aqua.api.AquaSession;
import de.mhus.aqua.api.CAqua;
import de.mhus.aqua.api.IUser;
import de.mhus.aqua.cao.AquaConnection;
import de.mhus.aqua.cao.AquaContentApplication;
import de.mhus.aqua.cao.AquaElement;
import de.mhus.aqua.caosql.ASConnection;
import de.mhus.aqua.mod.Publisher;
import de.mhus.aqua.res.AquaRes;
import de.mhus.aqua.tpl.Engine;
import de.mhus.lib.MEventHandler;
import de.mhus.lib.MHousekeeper;
import de.mhus.lib.MSingleton;
import de.mhus.lib.MStopWatch;
import de.mhus.lib.MXml;
import de.mhus.lib.cao.CaoApplication;
import de.mhus.lib.cao.CaoDriver;
import de.mhus.lib.cao.CaoFactory;
import de.mhus.lib.config.HashConfig;
import de.mhus.lib.config.IConfig;
import de.mhus.lib.config.XmlConfig;
import de.mhus.lib.jmx.JmxWeakMap;
import de.mhus.lib.jmx.MRemoteManager;
import de.mhus.lib.logging.Log;
import de.mhus.lib.sql.DbConnection;
import de.mhus.lib.sql.DbPool;
import de.mhus.lib.sql.DbStructureUtil;

public class Aqua {
	
	private static final String MODULE_REFERENCES = "/modules";
	private static final String AQUA_CONFIG = "/aqua.xml";
	public static final String KEY_SESSION = "aquasession";
	public static final String DEFAULT_USER = "everyone";
	private static final String DEFAULT_ACL = "default";
	
	private static Log log = Log.getLog(Aqua.class);
//	private ServletContext context;
	private AquaConnection caoConnection;
	private AquaContentApplication caoApplication;
//	private AquaModuleManager bundleManager;
	
	private HashMap<String, AquaApplication> appCache = new HashMap<String, AquaApplication>();
	private String baseDir;
	private long configModTime;
	protected IConfig config = null;
	protected DbPool dbPool = new DbPool();
	protected MEventHandler<Observer> configChangeObserver = new MEventHandler<Observer>();
	protected CaoFactory caoFactory = new CaoFactory();
	protected MHousekeeper housekeeper = new MHousekeeper();
	protected MRemoteManager remoteManager = null;
	protected Map<String, Object> objects = new HashMap<String, Object>();

	private WeakHashMap<String, Acl> aclRegister = new WeakHashMap<String, Acl>();
	private WeakHashMap<String, User> userRegister = new WeakHashMap<String, User>();

	private Engine engine;
	private AquaRootSession rootSession;

    /**
     * Default constructor. 
     */
    public Aqua() {
    }

	public void processHeadRequest(AquaRequest request) throws Exception {
		//processRequest(request); //TODO check if a full request is needed (res)
	}
	
	public void processRequest(AquaRequest request) throws Exception {

		AquaElement node = (AquaElement) caoApplication.queryTree(AquaConnection.TREE_NODE, request.getSession(), request.getPath(), request.getExtPath());
		request.setNode(node);
		
		// check existence or access
		if (node == null) {
			request.sendErrorForbidden();
			return;
		}
	
		AquaApplication app = node.getApplication();
					
		app.process(request);
		
	}
	
	public void init() throws Exception {
			
			MStopWatch stopwatch = new MStopWatch();
			stopwatch.start();
			log.i("init AQUA");

			baseDir = System.getProperty("de.mhus.aqua.dir");
	
			// laod config
			loadConfig();
			
			rootSession = new AquaRootSession();
			
			// init Activator values
			log.i("init activator");
			for ( IConfig map : config.getConfig("activator").getConfigBundle("map")) {
				Activator.getMActivator().addMap(map.getExtracted("alias"), map.getExtracted("class"));				
			}
		
			// init Databases
			log.i("init Databases");
			dbPool.setConfig(getConfig().getConfig("dbpool"));
			
			// init cao
			log.i("init CAO");
			caoFactory.registerDriver("aqua", "Aqua", Activator.getMActivator());
			caoConnection  = (AquaConnection) caoFactory.createConnection("aqua", "<config database='"+ASConnection.DB_NAME+"'/>");
			caoApplication = (AquaContentApplication) caoConnection.getApplication(null,CaoDriver.APP_CONTENT); //TODO root session ???

			// init database
			log.i("init database");
			DbConnection db = getDbPool().getConnection(ASConnection.DB_NAME);
			DbStructureUtil.createStructure(config.getConfig("sql"),db);
			db.close();
		
			// listen the config
			log.i("listen for config file");
			housekeeper.register(new TimerTask() {
				
				@Override
				public void run() {
					File f = getConfigFile();
					long time = f.lastModified();
					if (time != configModTime) {
						try {
							loadConfig();
							fireConfigChanged();
						} catch (Exception e) {
							log.w("can't reload config",f,e);
						}
					}
				}
			}, 1000 * 60, true);
			
			// open jmx
			log.i("open jmx manager");
			getJmxManager().open();
			getJmxManager().register(new JmxWeakMap(this, "userCache", userRegister));
			getJmxManager().register(new JmxWeakMap(this, "aclCache", aclRegister  ));
			getJmxManager().register(new JmxAqua(this));
						
			// template engine
			engine = new Engine();
			
			stopwatch.stop();
			log.i("Starting AQUA in " + stopwatch.getCurrentTimeAsString(false));

	}

	private File getConfigFile() {
		return new File(baseDir + AQUA_CONFIG);		
	}
	
	private void loadConfig() throws Exception {
		File f = getConfigFile();
		log.i("load config",f);
		configModTime = f.lastModified();
		FileInputStream is = new FileInputStream(f);
		config = new XmlConfig( MXml.loadXml(is).getDocumentElement() );
		is.close();
		config.setProperty("_basedir", baseDir);
		new MSingleton() {
			{
				config = Aqua.this.config;
				setInstance(this);
				fireConfigChanged();
			}
		};
	}
	
	public AquaApplication getAquaApplication(String name) throws Exception {
		synchronized (appCache) {
			AquaApplication app = appCache.get(name);
			if (app == null) {
				IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(CAqua.AQUA_APPLICATION);
				try {
					for (IConfigurationElement e : config) {
						// final String name2 = e.getDeclaringExtension().getUniqueIdentifier();
						final String name2 = e.getAttribute("id");
						if (name.equals(name2)) {
							log.t("create application",name);
							final Object o = e.createExecutableExtension("class");
							if (o instanceof AquaApplication) {
								ISafeRunnable runnable = new ISafeRunnable() {
									@Override
									public void handleException(Throwable exception) {
										log.i("Exception in client",name2);
									}	

									@Override
									public void run() throws Exception {
										((AquaApplication)o).initialize();
									}
								};
								SafeRunner.run(runnable);
								app = (AquaApplication)o;
							}
						}
					}
				} catch (CoreException ex) {
					System.out.println(ex.getMessage());
				}
				
				if (app!=null)
					appCache.put(name, app);
			}
			return app;
		}
	}

	public AquaRes createAquaRes(String name) throws Exception {
		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(CAqua.AQUA_RES);
		try {
			for (IConfigurationElement e : config) {
				final String name2 = e.getAttribute("id");
				if (name.equals(name2)) {
					log.t("create resource",name);
					final Object o = e.createExecutableExtension("class");
					if (o instanceof AquaRes) {
						return (AquaRes) o;
					}
				}
			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		log.t("create resource null",name);
		return null;
	}

	public Publisher createPublisher(String name) throws Exception {
		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(CAqua.AQUA_PUBLISHER);
		try {
			for (IConfigurationElement e : config) {
				final String name2 = e.getAttribute("id");
				if (name.equals(name2)) {
					log.t("create publisher",name);
					final Object o = e.createExecutableExtension("class");
					if (o instanceof Publisher) {
						return (Publisher) o;
					}
				}
			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		log.t("create publisher null",name);
		return null;
	}
	
	public AquaConnection getCaoConnection() {
		return caoConnection;
	}

	public AquaContentApplication getCaoApplication() {
		return caoApplication;
	}

	public void destroy() {
		log.i("destroy AQUA");
		for (Object obj : objects.values() )
			if (obj instanceof Closeable) {
				try {
					((Closeable)obj).close();
				} catch (Throwable t) {}
			}
		objects = null;

		System.gc();
	}
	
	public IUser getDefaultUser() throws Exception {
		return getUser(DEFAULT_USER);
	}
	
	public Acl getAcl(String aclId) throws Exception {
		if (aclId==null) aclId = DEFAULT_ACL;
		synchronized (aclRegister) {
			Acl acl = aclRegister.get(aclId);
			if (acl == null) {
				acl = new Acl(aclId);
				aclRegister.put(aclId, acl);
			}
			return acl;
		}
	}
 
	public IUser getUser(AquaRequest request) throws Exception {
		//TODO use factory !!!! to validate, evtl. return new request formular for PIN / TAN etc. or redirect ...
		String user = request.getParameter("user");
		// String pw = request.getRequest().getParameter("pw");
		return getUser(user);
	}
	
	public void fireAclChanged() {
		synchronized (aclRegister) {
			for (Acl acl : aclRegister.values()) {
				try {
					log.t("reload",acl.getId());
					acl.reload();
				} catch (Exception e) {
					log.t(acl.getId(),e);
				}
			}
		}
	}

	public IUser getUserForSession(AquaSession aquaSession) throws Exception {
		//TODO check if another user via SSO should be used ....
		//Use a factory ...
		// XXX !!!
		if (aquaSession instanceof AquaRootSession) {
			return new AquaRootUser();
		}
		return getUser(DEFAULT_USER);
	}
	
	public Engine getTplEngine() {
		return engine;
	}
	
	protected IUser getUser(String id) throws Exception {
		//TODO use factory
		synchronized (userRegister) {
			User user = userRegister.get(id);
			if (user == null) {
				user = new User(id);
				userRegister.put(id, user);
			}
			return user.getUser();
		}
	}

	public UserRights getUserRights(String userId) throws Exception {
		// cache ... ?
		return new UserRights(caoConnection, userId);
	}
	
	/**
	 * Return the central configuration.
	 * @return
	 */
	public synchronized IConfig getConfig() {
		if (config == null ) config = new HashConfig();
		return config;
	}
	
	/**
	 * Register a listener to get events if the config is changed. The reference in the list is weak. This
	 * means the listener must be hold on another variable too.
	 * 
	 * @param listener
	 */
	public void registerConfigListener(Observer listener) {
		configChangeObserver.registerWeak(listener);
	}
	
	/**
	 * Remove a registered config listener.
	 * 
	 * @param listener
	 */
	public void unregisterConfigListener(Observer listener) {
		configChangeObserver.unregister(listener);
	}
	
	/**
	 * Fire this if the config is changed.
	 */
	protected void fireConfigChanged() {
		for (Object listener : configChangeObserver.getListenersArray()) {
			((Observer)listener).update(null, config);
		}
	}
	
	public synchronized MRemoteManager getJmxManager() {
		if (remoteManager == null)
			remoteManager = new MRemoteManager();
		return remoteManager;
	}
	
	/**
	 * Return a central database pool.
	 * 
	 * @return
	 */
	public DbPool getDbPool() {
		return dbPool;
	}

	public String getBaseDir() {
		return baseDir;
	}
	
	public Object setObject(String key, Object object) {
		if (key == null) return null;
		if (object == null) {
			return objects.remove(key);
		}
		return objects.put(key, object);
	}
	
	/**
	 * Get a user object by unique key.
	 * 
	 * @param key
	 * @return
	 */
	public Object getObject(String key) {
		return objects.get(key);
	}

	public AquaSession getRootSession() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
