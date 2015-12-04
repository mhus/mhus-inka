package de.mhus.cap.core;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.StatusLineContributionItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.statushandlers.StatusManager;

import de.mhus.cap.core.dnd.CapDropListener.LOCATION;
import de.mhus.cap.core.dnd.CapDropListener.OPERATION;
import de.mhus.cap.core.dnd.ICaoExchange;
import de.mhus.cap.core.ui.ICaoImageProvider;
import de.mhus.lib.MEventHandler;
import de.mhus.lib.MException;
import de.mhus.lib.MSingleton;
import de.mhus.lib.cao.CaoAction;
import de.mhus.lib.cao.CaoActionList;
import de.mhus.lib.cao.CaoDriver;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoFactory;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoMonitor;
import de.mhus.lib.cao.CaoOperation;
import de.mhus.lib.cao.util.LinkedCaoList;
import de.mhus.lib.config.IConfig;
import de.mhus.lib.form.FormException;
import de.mhus.lib.form.MForm;
import de.mhus.lib.logging.ConsoleFactory;
import de.mhus.lib.logging.Log;
import de.mhus.lib.logging.YLogger;
import de.mhus.lib.swt.form.MFormSwtWizard;

public class CapCore {
	
	public static final String FILTER_ACTION_SELECT = "select";
	
	public static final String PROPERTY_EDITABLE = "editable";

	public static final String PROPERTY_UNDO = "undo";

	public static final String EXTENSION_IMAGE_PROVIDER = "de.mhus.cap.core.image_provider";
	public static final String EXTENSION_APPLICATION = "de.mhus.cap.core.application";

	public static final String DEFAULT_IMAGE_PROVIDER = "de.mhus.cap.core.file_extension";

	private static final String CAP_CONFIG_APPLICATION_PATH = "/application";

	private static final String CAP_CONFIG_SERVICE = "xml";
	
	public static final String APP_CAP_GUI = "hair_gui";
	public static final String LIST_LIST_HEADERS = "list_headers";

	public static final String ACTION_OPEN_WITH_SELECT = "openwithselect";

	
	private static CapCore instance;
	private StatusLineContributionItem appStatus;
//	private File configDir;
//	private CaoApplication config;
	private CaoList hotSelected;
	private HairEventHandler hotSelectHandler = new HairEventHandler();
	private CaoActionList hotSelectActionList;
	//private CaoFactory factory = new CaoFactory();
	
	private Map<String,Object> properties = new HashMap<String, Object>();
	
	private LinkedList<ConnectionDefinition> connections = new LinkedList<ConnectionDefinition>();

	private MessageConsole messageConsole;
	
	private static de.mhus.lib.logging.Log log = de.mhus.lib.logging.Log.getLog(CapCore.class);

	private static ICapApplication application;

	public static synchronized CapCore getInstance() {
		if (instance==null)
			instance = new CapCore();
		return instance;
	}
	
	private CapCore() {		
		// configDir = new File(System.getProperty(PROPERTY_CAP_CONFIG_DIR,CAP_CONFIG_DEFAULT_PATH));
//		configDir = application.getConfigurationBaseDir();
//		try {
//			IConfig config = MConfigFactory.getInstance().createConfigFor(configDir);
//			IConfig config = MSingleton.instance().
//			
//			//config = (CaoApplication) MSingleton.instance().getCaoFactory().createConnection(CAP_CONFIG_SERVICE, "path=" + configDir.getCanonicalPath()+CAP_CONFIG_APPLICATION_PATH).getApplication(null,CaoDriver.APP_CONTENT);
//		} catch (Exception e) {
//			log.debug(e);
//		}
		
		try {
			for ( IConfig item : getConfiguration().getConfig("connections").getConfigBundle("connection") ) {
				addConnection(item);
			}
		} catch (Exception e) {}
		
	}

	public void setHotSelectedItems(final CaoList selection) {
		synchronized (this) {
			hotSelected = selection;
			hotSelectActionList = null;
		}
//		Job job = new Job("Selection"){
//			@Override
//			protected IStatus run(final IProgressMonitor monitor) {
//				hotSelectHandler.fireHotSelected(selection);
//				return Status.OK_STATUS;
//			}
//		};
//		
//		job.schedule();
		
		
//				if (selection != hotSelected) return;
//				hotSelectHandler.fireHotSelected(selection);
				Job job = new Job("Selection"){
				@Override
				protected IStatus run(final IProgressMonitor monitor) {
					if (selection != hotSelected) return Status.OK_STATUS;
					hotSelectHandler.fireHotSelected(selection);
					return Status.OK_STATUS;
				}
			};
			
			job.schedule(200);

	}
	
	public void setProperty(String name, Object value) {
		synchronized (this) {
			Object curValue = properties.get(name);
			if (value != null && value.equals(curValue)) return;
			properties.put(name, value);
		}
		hotSelectHandler.firePropertyChanged(name, value);
	}
	
	public Object getProperty(String name) {
		return properties.get(name);
	}
	
	public MEventHandler<ICapEventListener> getEventHandler() {
		return hotSelectHandler;
	}
	
	public void setAppStatusItem(StatusLineContributionItem appstatus) {
		this.appStatus=appstatus;
	}
	
	public void setStatus(final String text) {
		if (this.appStatus == null)
			return;

		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				CapCore.this.appStatus.setText(text);
			}
		});
	}

//	public File getConfigDirectory() {
//		return configDir;
//	}
//	
	public IConfig getConfiguration() {
		return MSingleton.instance().getConfig().getConfig("cao").getConfig("application");
	}
	
	private class HairEventHandler extends MEventHandler<ICapEventListener> {
		
		private void fireHotSelected(CaoList selection) {
			for ( Object listener : getListenersArray() ) {
				try {
					
					((ICapEventListener)listener).hotSelected(selection);
				} catch (Throwable t) {
					log.debug(t);
				}
			}
		}
		
		private void firePropertyChanged(String property, Object value) {
			for ( Object listener : getListenersArray() ) {
				try {
					((ICapEventListener)listener).propertyChanged(property, value);
				} catch (Throwable t) {
					log.debug(t);
				}
			}
		}
			
		private void fireConnectionAdd(ConnectionDefinition newConnection) {
			for ( Object listener : getListenersArray() ) {
				try {
					((ICapEventListener)listener).connectionAdd(newConnection);
				} catch (Throwable t) {
					log.debug(t);
				}
			}
		}
		
		private void fireConnectionRemoved(ConnectionDefinition oldConnection) {
			for ( Object listener : getListenersArray() ) {
				try {
					((ICapEventListener)listener).connectionRemoved(oldConnection);
				} catch (Throwable t) {
					log.debug(t);
				}
			}
		}

		public void fireConnectionChanged(ConnectionDefinition con) {
			for ( Object listener : getListenersArray() ) {
				try {
					((ICapEventListener)listener).connectionChanged(con);
				} catch (Throwable t) {
					log.debug(t);
				}
			}
		}
		
	}

	public CaoList getHotSelected() {
		return hotSelected;
	}

	public CaoActionList getHotSelectActionList() {
		synchronized (this) {
			if (hotSelectActionList==null) {
				if(hotSelected==null) return null;
				hotSelectActionList = new CaoActionList();
				MSingleton.instance().getCaoFactory().fillWithActions(null,hotSelected, hotSelectActionList);
			}
			return hotSelectActionList;
		}
	}

	public boolean executeAction(CaoAction action, CaoList list, Object...initConfig) throws FormException, CaoException {

		if (action instanceof ICapRcpAction)
			((ICapRcpAction)action).setRcpHandles(PlatformUI.getWorkbench().getActiveWorkbenchWindow());

		if ( ! action.canExecute(list,initConfig) ) {
			return false;
		}
		
		MForm configuration = action.createConfiguration(list,initConfig);
		final Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();			
		if (configuration != null) {
					
			// connect wizard
			MFormSwtWizard wizard = new MFormSwtWizard();
			wizard.setWindowTitle( "Execute " + action.getName() ); //TODO use title
			
			wizard.appendPages(configuration);
	
			if ( wizard.show( shell ) != MFormSwtWizard.OK ) {
				wizard.dispose();
				return false;
			}

			wizard.dispose();


		}
		
		final CaoOperation operation = action.execute(list, configuration);
		
		if (operation != null) {
			executeOperation(operation, action.getName());
		}

		return true;
	}
	
	public void executeOperation(final CaoOperation operation, final String name) {
		
		if (operation instanceof ICapRcpAction)
			((ICapRcpAction)operation).setRcpHandles(PlatformUI.getWorkbench().getActiveWorkbenchWindow());

		Job job = new Job(name){
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
					try {
						CaoMonitor mon = new CaoMonitor() {
							private de.mhus.lib.logging.Log log = null;
							private de.mhus.lib.logging.Log log1 = null;
							private de.mhus.lib.logging.Log log2 = de.mhus.lib.logging.Log.getLog(CaoOperation.class);
							private int worked = 0;
							private MessageConsoleStream msgConsoleStream;
							
							{
								messageConsole = getMessageConsole();  
							    msgConsoleStream = messageConsole  
							      .newMessageStream();  
							  
							    ConsolePlugin.getDefault().getConsoleManager().addConsoles(  
							      new IConsole[] { messageConsole });
							    
							    log1 = new ConsoleFactory(name, new PrintStream(msgConsoleStream) );
							    log  = new YLogger(log2, log1);
							    	
							}
							@Override
							public boolean isCanceled() {
								return monitor.isCanceled();
							}

							@Override
							public Log log() {
								return log;
							}

							@Override
							public void subTask(String name) {
								monitor.subTask(name);
							}

							@Override
							public void worked(int work) {
								monitor.worked(work);
								worked = work;
							}

							@Override
							public void beginTask(String name, int totalWork) {
								monitor.beginTask(name, totalWork);
								worked = 0;
							}

							@Override
							public int alreadyWorked() {
								return worked;
							}
							
						};
						// if (operation instance)
						operation.setMonitor(mon);
						operation.initialize();
						try {
							operation.execute();
							final CaoOperation next = operation.getNextOperation();
							if (next != null) {
								Display.getDefault().asyncExec(new Runnable() {
						               public void run() {
						           		if (next instanceof MForm) {
											try {
							        			// connect wizard
							        			MFormSwtWizard wizard = new MFormSwtWizard();
							        			wizard.setWindowTitle( "Execute"); //TODO use title
							        			
							        			wizard.appendPages((MForm)next);
							        			Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();			
	
							        			if ( wizard.show( shell ) != MFormSwtWizard.OK ) {
							        				wizard.dispose();
							        				return;
							        			}
	
							        			wizard.dispose();
											} catch (Exception e) {
												log.e(e);
											}

						        		}
						            	   CapCore.getInstance().executeOperation(next, name);
						               }
								});
							}
						} finally {
							operation.dispose();
						}
					} catch (CaoException e) {
						log.debug(e);
					}
//					Display.getDefault().asyncExec(new Runnable() {
//			               public void run() {
//			            	   MessageDialog.openInformation(HandlerUtil.getActiveShell(event), "Info", "Info for you");
//			               }
//					});
//					

				
				monitor.done();
				return Status.OK_STATUS;
			}
			
		}; 
		
		job.schedule();
	}
		
	

	public void firePropertyChanged(String name, Object value) {
		hotSelectHandler.firePropertyChanged(name, value);		
	}

	public boolean doDrop(LOCATION loc, OPERATION oper, ICaoExchange[] providers, ICaoExchange target) {

		try {
			String actionName = null;
			switch (oper) {
			case COPY: actionName = CaoDriver.ACTION_CAP_COPY_TO;break;
			case MOVE: actionName = CaoDriver.ACTION_CAP_MOVE_TO;break;
			case LINK: actionName = CaoDriver.ACTION_CAP_LINK_TO;break;
			}
			
			if (actionName==null) return false;

			CaoElement targetElement = target.getElement();
			if (loc == LOCATION.AFTER || loc == LOCATION.BEFORE) {
				targetElement = targetElement.getParent();
			}
			if (targetElement == null) return false;
			
			LinkedList<CaoElement> caoData = new LinkedList<CaoElement>();
			for (ICaoExchange item : providers) {
				CaoElement sourceElement = item.getElement();
				if (	   targetElement.getId()!=null 
						&& sourceElement.getParent() != null 
						&& targetElement.getId().equals(sourceElement.getParent().getId()) ) return false;
				caoData.add(sourceElement);
			}
			
			LinkedCaoList caoList = new LinkedCaoList(targetElement, caoData);
			
			CaoActionList actionList = new CaoActionList();
			MSingleton.instance().getCaoFactory().fillWithActions(targetElement,caoList, actionList, targetElement);

			CaoAction action = actionList.getAction(actionName);
			
			if (action==null) return false;
			
			return executeAction(action, caoList, targetElement );
			
		} catch (Throwable t) {
			log.debug(t);
		}
		return false;
	}
	
	public ICaoImageProvider getImageProvider(String name) {
		if (name==null) return null;
		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_IMAGE_PROVIDER);
		for (IConfigurationElement e : config) {
			try {
				if (name.equals(e.getAttribute("name")))
					return (ICaoImageProvider)e.createExecutableExtension("class");
			} catch (CoreException e1) {
				log.debug(e1);
			}
		}
		return null;
	}

	public ICapApplication getApplication() {
		return application;
	}
	
	public static void initialize(ICapApplication app) {
		if (application != null) {
			log.warn("already initialized");
			return;
		}
		application = app;
		application.doStart();
	}

	public CaoFactory getFactory() {
		return MSingleton.instance().getCaoFactory();
	}
	
	protected MessageConsole getMessageConsole() {  
	  if (messageConsole == null) {  
	   messageConsole = new MessageConsole("Messages", null);  
	   ConsolePlugin.getDefault().getConsoleManager().addConsoles( new IConsole[] { messageConsole });  
	  }
	  return messageConsole;
	}
	
	public void addConnection(IConfig config) throws MException {
		ConnectionDefinition newCon = null;
		newCon = new ConnectionDefinition(config);
		addConnection(newCon);
	}

	public void addConnection(ConnectionDefinition newCon) {
		synchronized (connections) {
			connections.add(newCon);
		}
		hotSelectHandler.fireConnectionAdd(newCon);
	}
	
	public void removeConnection(ConnectionDefinition con) {
		synchronized (connections) {
			if (!connections.remove(con) )
				return;
		}
		hotSelectHandler.fireConnectionRemoved(con);
	}
	
	public void fireConnectionChanged(ConnectionDefinition con) {
		hotSelectHandler.fireConnectionChanged(con);
	}

	public List<ConnectionDefinition> getConnections() {
		return connections;
	}

	public boolean isNoSecretMode() {
		return getConfiguration().isProperty("no_more_secrets");
	}

	public void saveConnections() throws MException {
		IConfig ccon = getConfiguration().getConfig("connections");
		for (IConfig child : ccon.getConfigBundle("connection") )
			ccon.removeConfig(child);
		
		synchronized (connections) {
			for (ConnectionDefinition con : connections) {
				IConfig newCon = ccon.createConfig("connection");
				con.fill(newCon);
			}
		}
		
		ccon.save();
		
	}

	public void showError(String msg) {
		System.out.println(msg);
		StatusManager.getManager().handle(new CapStatus(null, msg), StatusManager.SHOW);
	}
	
}
