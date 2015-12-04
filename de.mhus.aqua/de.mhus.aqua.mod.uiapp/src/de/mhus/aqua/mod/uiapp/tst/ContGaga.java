package de.mhus.aqua.mod.uiapp.tst;

import java.io.PrintWriter;
import java.util.Map;

import de.mhus.aqua.api.AquaRequest;
import de.mhus.aqua.mod.uiapp.AjaxTreeSource;
import de.mhus.aqua.mod.uiapp.XmlTreeSource;
import de.mhus.aqua.mod.uiapp.UiBox;
import de.mhus.aqua.mod.uiapp.wui.ITreeNode;
import de.mhus.aqua.mod.uiapp.wui.ITreeSource;
import de.mhus.aqua.mod.uiapp.wui.WColumnSelector;
import de.mhus.aqua.mod.uiapp.wui.WKit;
import de.mhus.lib.MException;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.form.MForm;

public class ContGaga extends UiBox {

//	private WColumnSelector obj;

	public ContGaga() throws MException {
		super();
	}

	@Override
	public String getTitle() {
		return "GaGa";
	}


	@Override
	protected void doInit() {
//		WKit kit = new WKit(this.request,nls);
//		try {
//			obj = (WColumnSelector) kit.create(WColumnSelector.class);
//			obj.setSource(new AjaxTreeSource(this) {
//
//				@Override
//				public ITreeNode[] getRoots() {
//					return new ITreeNode[] {
//							new MyTreeNode("aa","Nr A"),
//							new MyTreeNode("ab","Nr B"),
//					};
//				}
//
//				@Override
//				public ITreeNode[] getChildren(String id) {
//					if (id == null) return null;
//					if (id.startsWith("a")) {
//						return new ITreeNode[] {
//								new MyTreeNode("x1","Nr 1x"),
//								new MyTreeNode("x2","Nr 2x"),
//						};
//					}
//					if (id.startsWith("x")) {
//						return new ITreeNode[] {
//								new MyTreeNode("y1","Nr 1y"),
//								new MyTreeNode("y2","Nr 2y"),
//						};
//					}
//					if (id.startsWith("y")) {
//						return new ITreeNode[] {
//								new MyTreeNode("g1","Nr 1g"),
//								new MyTreeNode("g2","Nr 2g"),
//						};
//					}
//					if (id.startsWith("g")) {
//						return new ITreeNode[] {
//								new MyTreeNode("h1","Nr 1h"),
//								new MyTreeNode("h2","Nr 2h"),
//						};
//					}
//
//					return null;
//				}
//				
//			});
//			addChild(null, obj);
//		} catch (MException e) {
//			e.printStackTrace();
//		}
	}

	@Override
	public void paint(AquaRequest data, PrintWriter stream) throws CaoException {
				
		stream.print("<div class='container'");
		stream.print("><p>");
		for ( int i = 0; i < 10; i++ )
			stream.println( "GAGA ");
		stream.print("</p><p>");
		try {
			String list = DEFAULT;
			paint(data, list, stream);
		} catch (Exception e) {
			e.printStackTrace();
		}
		stream.print("</p><p>");
		stream.print(config.getProperty("text"));
		stream.print("</p></div>");
	}

	@Override
	protected void doFillAttributes(AquaRequest data, Map<String, Object> attr) {
		
	}

	@Override
	public MForm getConfigForm() {
		return new ContGagaForm(config);
	}

	@Override
	public boolean canChangeHeight() {
		return true;
	}
	
//	private class MyTreeNode implements ITreeNode {
//
//		private String id;
//		private String title;
//
//		MyTreeNode(String id, String title) {
//			this.id = id;
//			this.title=title;
//		}
//		
//		@Override
//		public String getTitle() {
//			return title;
//		}
//
//		@Override
//		public String getIconClass() {
//			return null;
//		}
//
//		@Override
//		public boolean hasChildren() {
//			return ((ITreeSource)obj.getSource()).getChildren(id) != null;
//		}
//
//		@Override
//		public boolean isExpanded() {
//			return false;
//		}
//
//		@Override
//		public String getId() {
//			return id;
//		}
//		
//	}
	
}
