package de.mhus.cap.core.dnd;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TransferData;

import de.mhus.cao.model.fs.IoApplication;
import de.mhus.cao.model.fs.IoElement;
import de.mhus.cap.core.CapCore;
import de.mhus.lib.cao.CaoApplication;
import de.mhus.lib.cao.CaoConnection;
import de.mhus.lib.cao.CaoDriver;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoFactory;
import de.mhus.lib.cao.CaoForm;

public class CapDropListener extends ViewerDropAdapter {

	private static de.mhus.lib.logging.Log log = de.mhus.lib.logging.Log.getLog(CapDropListener.class);

	public enum LOCATION {
		AFTER,
		BEFORE,
		INTO,
		NONE
	}
	
	public enum OPERATION {
		DEFAULT, COPY, MOVE, LINK, TARGET_MOVE
		
	}
	
	public CapDropListener(Viewer viewer) {
		super(viewer);
	}

//	@Override
//	public void drop(DropTargetEvent event) {
//		int location = this.determineLocation(event);
//		ICaoProvider target = (ICaoProvider) determineTarget(event);
//		
//		String translatedLocation ="";
//		switch (location){
//		case 1 :
//			translatedLocation = "Dropped before the target ";
//			break;
//		case 2 :
//			translatedLocation = "Dropped after the target ";
//			break;
//		case 3 :
//			translatedLocation = "Dropped on the target ";
//			break;
//		case 4 :
//			translatedLocation = "Dropped into nothing ";
//			break;
//		}
//		System.out.println(translatedLocation);
//		System.out.println("The drop was done on the element: " + target );
//		super.drop(event);
//	}

	// This method performs the actual drop
	// We simply add the String we receive to the model and trigger a refresh of the 
	// viewer by calling its setInput method.
	@Override
	public boolean performDrop(Object data) {
		
		// file transfer
		if (data instanceof String[]) {
			try {

				ICaoExchange[] list = new ICaoExchange[((String[]) data).length];
				
				CaoDriver driver = CapCore.getInstance().getFactory().getDriver("fs");
				CaoForm config = driver.createConfiguration();
				CaoConnection con = driver.createConnection(config);
				CaoApplication app = con.getApplication(CaoDriver.APP_CONTENT);
				
				int i = 0;
				for (String item : (String[]) data ) {
					final IoElement element = new IoElement((IoApplication) app,item);
					list[i] = new ICaoExchange() {
	
						@Override
						public CaoElement getElement() {
							return element;
						}
						
						@Override
						public boolean doDrop(LOCATION loc, OPERATION oper, ICaoExchange[] providers) {
							return false;
						}
					};
					i++;
				}
				data = list;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (data instanceof ICaoExchange[]) {
		
			ICaoExchange[] providers = (ICaoExchange[])data;
			Object targetObject = determineTarget(getCurrentEvent());
			if ( ! (targetObject instanceof ICaoExchange)) return false;
			
			ICaoExchange target = (ICaoExchange) targetObject;
			int location = this.determineLocation(getCurrentEvent());
			
			LOCATION loc = LOCATION.NONE;
			switch(location) {
			case LOCATION_AFTER: loc = LOCATION.AFTER;break;
			case LOCATION_BEFORE: loc = LOCATION.BEFORE;break;
			case LOCATION_ON: loc = LOCATION.INTO;break;
			}
			
			OPERATION oper = OPERATION.DEFAULT;
			switch(getCurrentOperation()) {
			case DND.DROP_COPY: oper = OPERATION.COPY;break;
			case DND.DROP_MOVE: oper = OPERATION.MOVE;break;
			case DND.DROP_LINK: oper = OPERATION.LINK;break;
			case DND.DROP_TARGET_MOVE: oper = OPERATION.TARGET_MOVE;break;
			}
			
			if (target != null) return target.doDrop( loc, oper, providers );
			return false;
		}
				
		return false;
	}

	@Override
	public boolean validateDrop(Object targetObject, int operation,
			TransferData transferType) {
		
		if ( ! (targetObject instanceof ICaoExchange)) return false;
		
		return true;
		
	}


}
