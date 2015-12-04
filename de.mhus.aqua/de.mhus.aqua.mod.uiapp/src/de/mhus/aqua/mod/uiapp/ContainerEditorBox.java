package de.mhus.aqua.mod.uiapp;

import java.io.PrintWriter;
import java.util.Map;

import de.mhus.aqua.api.AquaRequest;
import de.mhus.aqua.api.IUserRights;
import de.mhus.aqua.mod.uiapp.tst.ContGaga;
import de.mhus.aqua.mod.uiapp.wui.ITreeNode;
import de.mhus.aqua.mod.uiapp.wui.IWComponent;
import de.mhus.aqua.mod.uiapp.wui.WColumnSelector;
import de.mhus.aqua.mod.uiapp.wui.WKit;
import de.mhus.aqua.mod.uiapp.wui.WLayoutLCR;
import de.mhus.aqua.mod.uiapp.wui.WLayoutTopLR;
import de.mhus.aqua.mod.uiapp.wui.WTabPane;
import de.mhus.aqua.mod.uiapp.wui.IWTplInclude;
import de.mhus.aqua.mod.uiapp.wui.WTree;
import de.mhus.aqua.mod.uiapp.wui.WUtil.DIRECTION;
import de.mhus.lib.MCast;
import de.mhus.lib.MException;
import de.mhus.lib.MString;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.config.IConfig;
import de.mhus.lib.config.JsonConfig;

/**
 * This class implements the editor utility of the Ui application. 
 * 
 * @author mikehummel
 *
 */
public class ContainerEditorBox extends UiBox {
	
	private boolean adminMode = false;
	private WKit kit;
	private AquaRequest request;
	
	public ContainerEditorBox() throws MException {
		super();
	}

	private static de.mhus.lib.logging.Log log = de.mhus.lib.logging.Log
			.getLog(ContainerEditorBox.class);

	@Override
	public void initWElement(AquaRequest request, String id, IConfig config) throws MException {
		this.request = request;
		super.initWElement(request, id, config);
	}
	
	@Override
	public void doInit() throws MException {
		setTplName(Activator.instance().getId() + "/admin_box");
		kit = new WKit(request,Activator.instance(),"page_editor");
		setNls(kit.getNls());
		WTabPane tab = kit.createTabPane();
		tab.addTab("widgets", "Widgets");
		tab.addTab("layout",  "Layout");
		
		WColumnSelector widgetsTree = (WColumnSelector) kit.create(WColumnSelector.class);
		widgetsTree.setWidth("100%");
		AjaxTreeSource treeSource = new AjaxTreeSource(this) {

			@Override
			public ITreeNode[] getRoots() {
				
				return new ITreeNode[] {
					new GroupNode(".g","General",true)
				};

			}

			@Override
			public ITreeNode[] getChildren(String id) {
				if (id.equals(".g")) {
					ComponentInfo[] a;
					try {
						a = ((UiApplication)request.getNode().getApplication()).getPossibleUIComponents(request.getSession());
					} catch (CaoException e) {
						e.printStackTrace();
						return null;
					}
					ITreeNode[] b = new ITreeNode[a.length];
					for (int i = 0; i < a.length; i++) {
						b[i] = new WidgetNode(a[i]);
					}
					return b;					
				}
				return null;
			}

		};
		widgetsTree.setSource(treeSource);
		
		AjaxAction selectAction = new AjaxAction(this) {
						
			{
				setExtra("+ \"&selection=\" + id");
			}
			@Override
			public AjaxActionDefinition[] doRequest(AquaRequest request) throws Exception {


				String id = request.getParameter("selection");
				log.t("selected", id);
				UiBox newBox = ((UiApplication)request.getNode().getApplication()).createBox(request, container, id );
				
				IWComponent ret = ((UiEditableContainer)container).appendBox(newBox);
				int defaultContainer = ((UiEditableContainer)container).getLayout().getDefaultContainer();
				
				BoxConfigDialog dialog = new BoxConfigDialog(request, container, newBox);
				dialog.show();

				return new AjaxActionDefinition[] {
						new AjaxActionDefinition(ACTION.FIRST,"#list" + defaultContainer,ret), //XXX
						new AjaxActionDefinition(ACTION.CONTENT,"#dialog_pane",dialog)
				};

			}
			
		};
		widgetsTree.setSelectAction(selectAction);
		
		tab.addChild("widgets", widgetsTree);
		
		
		// ---------------------------------------------------------------
		
		WColumnSelector layoutTree = (WColumnSelector) kit.create(WColumnSelector.class);
		layoutTree.setWidth("100%");

		AjaxTreeSource lts = new AjaxTreeSource(this) {
			
			@Override
			public ITreeNode[] getRoots() {
				
				return new ITreeNode[] {
						new GroupNode(".g","General",true)
					};
			}
			
			@Override
			public ITreeNode[] getChildren(String id) {
				if (id.equals(".g")) {
					ITreeNode[] out = new ITreeNode[] {
							new LayoutNode("LCR", WLayoutLCR.class.getCanonicalName()),
							new LayoutNode("TopLR", WLayoutTopLR.class.getCanonicalName())
					};
					return out;

				}
				return null;
			}
		};
		layoutTree.setSource(lts);
		
		AjaxAction lsa = new AjaxAction(this) {
			
			{
				setExtra("+ \"&selection=\" + id");
			}
			@Override
			public AjaxActionDefinition[] doRequest(AquaRequest request) throws Exception {
				String id = request.getParameter("selection");
				log.t("selected", id);
				container.getConfig().getConfig("layout").setProperty("type", id);
				container.createLayout();
				container.refreshLayout();
				return new AjaxActionDefinition[] {
						new AjaxActionDefinition(ACTION.CONTENT,"#dialog_pane",new IWComponent() {
							public void paint(AquaRequest data, PrintWriter stream) throws MException {
								Map<String, Object> attr = engine.createAttributes(null);
								engine.execute(ContainerEditorBox.this, "reload", attr, stream);
							}
						})
				};
			}
		};
		layoutTree.setSelectAction(lsa);
		
		tab.addChild("layout", layoutTree);
				
		addChild("elements", tab);
		
		
	}
	
	public void processAjax(AquaRequest request, PrintWriter writer) throws MException {

		if (!request.getNode().getAcl().hasRight(request.getSession(), IUserRights.EDIT))
			return;
		
		String action = request.getParameter("action");
	

		if ("on".equals(action)) {
//			AjaxAction ret = new AjaxAction(this) {
//				
//				@Override
//				public AjaxActionDefinition[] doRequest(AquaRequest request) throws Exception {
//					return new AjaxActionDefinition[] {
//							new AjaxActionDefinition(ACTION.CONTENT, "", component)
//					};
//				}
//			};			
			writer.print("{\"rc\":\"ok\",\"results\":[{\"a\":\"content\", \"n\":\"#admin_container\", \"v\":\"");
			PrintWriter w = new PrintWriter( new EncodeWriter(writer) );
	
			Map<String, Object> attr = engine.createAttributes(request);
			attr.put("admin", getId() );
			nls.initTpl(attr);
			adminMode = true;
			engine.execute(this, "on", attr, w);
			
			w.flush();
			writer.print("\"}]}");
			return;
		} else
		if ("cancel".equals(action)) {

			writer.print("{\"rc\":\"ok\",\"results\":[{\"a\":\"content\", \"n\":\"#admin_container\", \"v\":\"");
			PrintWriter w = new PrintWriter( new EncodeWriter(writer) );
			
			Map<String, Object> attr = engine.createAttributes(request);
			attr.put("admin", getId() );
			attr.put("active", request.getSession().isAdminActive());
			nls.initTpl(attr);
			adminMode = false;
			engine.execute(this, "reload", attr, w);
			
			w.flush();
			writer.print("\"}]}");
			
			try {
				container.loadConfig();
				container.refreshLayout();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			return;
		} else
		if ("save".equals(action)) {
			writer.print("{\"rc\":\"ok\",\"results\":[{\"a\":\"content\", \"n\":\"#admin_container\", \"v\":\"");
			PrintWriter w = new PrintWriter( new EncodeWriter(writer) );
			
			Map<String, Object> attr = engine.createAttributes(request);
			attr.put("admin", getId() );
			attr.put("active", request.getSession().isAdminActive());
			nls.initTpl(attr);
			adminMode = false;
			engine.execute(this, "reload", attr, w);
			
			w.flush();
			writer.print("\"}]}");
			
			container.getNode().setApplicationConfig(container.getSession().getUser(), container.getConfig());
			
			return;
		} else
		if ("savedefault".equals(action)) {
			writer.print("{\"rc\":\"ok\",\"results\":[{\"a\":\"content\", \"n\":\"#admin_container\", \"v\":\"");
			PrintWriter w = new PrintWriter( new EncodeWriter(writer) );
			
			Map<String, Object> attr = engine.createAttributes(request);
			attr.put("admin", getId() );
			attr.put("active", request.getSession().isAdminActive());
			nls.initTpl(attr);
			adminMode = false;
			engine.execute(this, "reload", attr, w);
			
			w.flush();
			writer.print("\"}]}");
			
			container.getNode().setApplicationConfig(container.getSession().getUser(), null);
			container.getNode().setApplicationConfig(container.getConfig());
			try {
				container.loadConfig();
				container.refreshLayout();
			} catch (Exception e) {
				log.i(e);
			}
			
			return;
		} else
		if ("move".equals(action)) {
			writer.print("{\"rc\":\"ok\",\"results\":[]}");
			String boxId = MString.afterIndex(request.getParameter("node"),'_');
			int list = MCast.toint(request.getParameter("list").substring("list".length()),-1);
			int pos  = MCast.toint(request.getParameter("pos"),-1);
			if (boxId == null || !boxId.startsWith("i") || list < 0 || pos < 0) return; 
			((UiEditableContainer)container).moveBox(boxId,list,pos);
		} else
		if ("setup".equals(action)) {
			String boxId = MString.afterIndex(request.getParameter("node"),'_');
			UiBox box = container.getBox(boxId);
			request.setNode(container.getNode());
			BoxConfigDialog dialog = null;
			try {
				dialog = new BoxConfigDialog(request, container, box);
			} catch (Exception e) {
				log.e(e);
				return;
			}
			dialog.show();

			writer.print("{\"rc\":\"ok\",\"results\":[{\"a\":\"content\", \"n\":\"#dialog_pane\", \"v\":\"");
			PrintWriter w = new PrintWriter( new EncodeWriter(writer) );
			dialog.paint(request, w);
			w.flush();
			writer.print("\"}]}");
		} else
		if ("close".equals(action)) {
			String boxId = MString.afterIndex(request.getParameter("node"),'_');
			((UiEditableContainer)container).removeBox(boxId);
			writer.print("{\"rc\":\"ok\",\"results\":[{\"a\":\"remove\", \"n\":\"#box_"+boxId+"\", \"v\":\"");
			writer.print("\"}]}");
		}
		if ("resize".equals(action)) {
			String boxId = MString.afterIndex(request.getParameter("node"),'_');
			int height = MCast.toint(request.getParameter("height"),-1);
			UiBox box = container.getBox(boxId);
			if (box.canChangeHeight())
				box.setHeight(height);
			writer.print("{\"rc\":\"ok\",\"results\":[]}");
		}
		
		super.processAjax(request, writer);
		
	}

	@Override
	public String getTitle() {
		return kit.getNls().find("box_title=Admin");
	}

	@Override
	protected void doFillAttributes(AquaRequest data, Map<String, Object> attr) {

		try {
			if (request.getNode().getAcl().hasRight(request.getSession(), IUserRights.EDIT)) {
				if (adminMode) {
					attr.put(SECTION, "on");
				} else {
					attr.put(SECTION, "off");
				}
				attr.put("active", true );
			} else {
				attr.put(SECTION, "off");
				attr.put("active", false );			
			}
		} catch (CaoException e) {
			log.i(e);
		}
	}

	static class GroupNode implements ITreeNode {
		
		private String title;
		private String id;
		private boolean group;

		public GroupNode(String id, String title, boolean isGroup) {
			this.title = title;
			this.id = id;
			this.group = isGroup;
		}
		@Override
		public String getTitle() {
			return title;
		}

		@Override
		public String getIconClass() {
			return "";
		}

		@Override
		public boolean hasChildren() {
			return group;
		}

		@Override
		public boolean isExpanded() {
			return false;
		}

		@Override
		public String getId() {
			return id;
		}		
	}
	
	static class LayoutNode implements ITreeNode {

		private String title;
		private String id;

		public LayoutNode(String title, String id) {
			this.title = title;
			this.id = id;
		}
		@Override
		public String getTitle() {
			return title;
		}

		@Override
		public String getIconClass() {
			return "";
		}

		@Override
		public boolean hasChildren() {
			return false;
		}

		@Override
		public boolean isExpanded() {
			return false;
		}

		@Override
		public String getId() {
			return id;
		}
		
	}
	
	static class WidgetNode implements ITreeNode {

		private ComponentInfo info;

		public WidgetNode(ComponentInfo info) {
			this.info = info;
		}

		@Override
		public String getTitle() {
			return info.getTitle();
		}

		@Override
		public String getIconClass() {
			return "";
		}

		@Override
		public boolean hasChildren() {
			return false;
		}

		@Override
		public boolean isExpanded() {
			return false;
		}

		@Override
		public String getId() {
			return info.getId();
		}
		
	}

	@Override
	public boolean canChangeHeight() {
		return false;
	}

	@Override
	public void setHeight(int height) {
		
	}

	@Override
	public int getHeight() {
		return -1;
	}
	
	@Override
	public boolean isVisible() {
		return false;
	}

}
