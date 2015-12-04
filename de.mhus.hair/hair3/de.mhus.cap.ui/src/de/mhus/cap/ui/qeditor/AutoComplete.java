package de.mhus.cap.ui.qeditor;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * AutoComplete adds auto completion capabilities to any Text 
 * widget.  AutoComplete is very straight-forward and can be 
 * added to any Text widget with very few lines of code.  For 
 * example:
 * <p>
 * <code>
 * Text text = new Text(parent,SWT.BORDER);<BR>
 * <BR>
 * AutoComplete autoComplete = new AutoComplete(text);<BR>
 * autoComplete.setValues(new String[]{"value 1","value 2"});<BR>
 * </code>
 */
public class AutoComplete {
	
	/**
	 * Enables resize on the autocomplete popup.  This is not supported 
	 * by all OSes, therefore this style should only be considered a hint.
	 */
	public static final int RESIZE = 1 << 1;
	
	private Shell shell;
	
	private Text owner;
	private Shell ownerShell;
	
	private ControlListener parentShellControlListener;
	private ShellListener parentShellShellListener;
	private Listener mouseDownFilter;
	
	private boolean canReturnFocusOnMouseDown = true;
	
	private boolean ignoreDeactivate = false;
	
	private int visibleItemCount = 6;
	
	private List list;
	
	private IAutoCompleteProvider provider;
	
	private boolean ignoreModification = false;
	
//	private java.util.List valuesList = new ArrayList();
	
	private boolean resize = false;
	
	private boolean active = false;
		
	/**
	 * Constructs an AutoComplete instance providing auto completion
	 * capabilities to the given Text widget using the completion values
	 * as supplied by the given IAutoCompletionProvider.
	 * 
	 * @param owner Text widget that requires autocompletion
	 * @param style style of the autocomplete
	 * @param provider provides values for autocompletion
	 */
	public AutoComplete(final Text owner,int style, IAutoCompleteProvider providerx){
		this.owner = owner;
		this.ownerShell = owner.getShell();
		this.provider = providerx;
		
		if ((style & RESIZE) == RESIZE)
            resize = true;
		
		owner.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				dispose();
			}		
		});
		
		owner.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
//				if (owner.getText().equals("")){
//					hide();
//					return;
//				}
//				if (!ignoreModification){
//					String[] displayVals = provider.getValues(values, owner.getText());
//					if (displayVals == null || displayVals.length == 0){
//						hide();
//					} else {
//						if (list == null)
//							create();
//						
//						list.setItems(displayVals);
//						show();
//					}
//				}
			}		
		});
		
        owner.addKeyListener(new KeyListener(){
            public void keyPressed(KeyEvent e) {
            	
            	if (!active) return;
            	
            	if (isOpen()){
	            	if (e.keyCode == SWT.ARROW_DOWN){
	            		e.doit = false;
	            		keyDown();
	            		return;
	            	}
	            	if (e.keyCode == SWT.ARROW_UP){
	            		e.doit = false;
	            		keyUp();
	            		return;
	            	}
	            	if (e.character == '\r' ){
	            		String[] sel = list.getSelection();
	            		if (sel != null && sel.length == 1)
	            			setText(sel[0]);
	            		e.doit = false;
	            		hide();
	            		return;
	            	}
	            	if (e.keyCode == SWT.ESC) {
	            		e.doit = false;
	            		hide();
	            		return;
	            	}
	            	
            	}
            }
            public void keyReleased(KeyEvent e) {
            	
            	if (!active) return;

            	if (e.keyCode == SWT.ARROW_DOWN){
            		return;
            	}
            	if (e.keyCode == SWT.ARROW_UP){
            		return;
            	}
            	if (e.character == '\r' || e.keyCode == SWT.ESC){
            		return;
            	}
            	
            	
				if (owner.getText().equals("")){
					hide();
					return;
				}
				if (!ignoreModification){
					String[] displayVals = provider.getValues(owner);
					if (displayVals == null || displayVals.length == 0){
						hide();
					} else {
						if (list == null)
							create();
						
						list.setItems(displayVals);
						show();
					}
				}
            }}
        );
        
        owner.addListener(SWT.MouseWheel,new Listener(){
			public void handleEvent(Event arg0) {
				
            	if (!active) return;

				if (isOpen()){
					scroll(arg0.count);
				}
			}}
        );
	}
	
	private void create(){
		int style = SWT.ON_TOP;
		if (resize)
			style = style | SWT.RESIZE;
		
		shell = new Shell(ownerShell,style);
        
		shell.addListener(SWT.Traverse, new Listener() {
			public void handleEvent(Event e) {
				if (e.detail == SWT.TRAVERSE_ESCAPE) {
					e.doit = false;
					hide();
				}
			}
		});

		
		parentShellShellListener = new ShellListener(){
            public void shellActivated(ShellEvent arg0) {
            }
            public void shellClosed(ShellEvent arg0) {
            }
            public void shellDeactivated(ShellEvent arg0) {
            	owner.getDisplay().asyncExec(new Runnable(){
					public void run() {
						if (isOpen() && shell != owner.getDisplay().getActiveShell()){		
							ownerShell.setActive();
					    	hide();
					    }
					}}
				);                        
            }
            public void shellDeiconified(ShellEvent arg0) {
            }
            public void shellIconified(ShellEvent arg0) {
            	hide();
            }
        };
		
        ownerShell.addShellListener(parentShellShellListener);
        
		parentShellControlListener = new ControlListener(){
			public void controlMoved(ControlEvent e) {
				hide();
			}
			public void controlResized(ControlEvent e) {
				hide();
			}
		};
		
		ownerShell.addControlListener(parentShellControlListener);
		
		shell.setLayout(new FillLayout());

		list = new List(shell,SWT.V_SCROLL);
		list.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}		
			public void widgetSelected(SelectionEvent e) {
				if (list.getSelection().length > 0)
					setText(list.getSelection()[0]);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
				}
				hide();
			}		
		});
		
		shell.addShellListener(new ShellListener(){
			public void shellActivated(ShellEvent e) {
			}
			public void shellClosed(ShellEvent e) {
			}
			public void shellDeactivated(ShellEvent e) {
				if (!ignoreDeactivate)
					hide();
			}
			public void shellDeiconified(ShellEvent e) {
			}
			public void shellIconified(ShellEvent e) {
			}}
		);

		mouseDownFilter = new Listener(){
			public void handleEvent(Event event) {
				if (isOpen()){
					if (canReturnFocusOnMouseDown){
						owner.getDisplay().asyncExec(new Runnable(){
							public void run() {
								ignoreDeactivate = true;
						    	ownerShell.setActive();
								ignoreDeactivate = false;
							}}
						);					
					}
					if (event.widget instanceof Control){
						Control c = (Control) event.widget;
						if (!shell.getBounds().contains(owner.getDisplay().map(c,null,event.x,event.y))){
							hide();
							event.type = SWT.NONE;
						}					
					}
				}
			}
		};
		
		owner.getDisplay().addFilter(SWT.MouseDown,mouseDownFilter);
	}
	
	
	private void show(){
		if (isOpen()){
			int times = visibleItemCount;
			if (list.getItemCount() < visibleItemCount)
				times = list.getItemCount();
			
			int popupHeight = list.getItemHeight() * times;
			
			popupHeight = shell.computeTrim(0,0,shell.getSize().x,popupHeight).height;
			
			shell.setSize(shell.getSize().x,popupHeight);
			return;
		}
		owner.getDisplay().asyncExec(new Runnable(){
			public void run() {
				
				if (shell == null)
					create();
				
				Point size;

				//comboWidth is really the width of the combo minus trimmings around
				//the dropdown shell,
				//The intention is that if the strategy wants to set its width equal
				//to the combo it would just use this value
				
				//int popupWidth = shell.computeTrim(0,0,1,1).width -1;
				//popupWidth = (owner.getSize().x - owner.getCaretLocation().x) - popupWidth;	
				int popupWidth = 300;
				
				int times = visibleItemCount;
				if (list.getItemCount() < visibleItemCount)
					times = list.getItemCount();
				
				int popupHeight = list.getItemHeight() * times;
				size = new Point(popupWidth,popupHeight);
				
				Rectangle bounds = shell.computeTrim(0,0,size.x,size.y);
				shell.setSize(bounds.width,bounds.height);

				
				Point loc = owner.getDisplay().map(owner.getParent(),null,owner.getLocation());
				Point caret = owner.getCaretLocation();
				loc.x = loc.x + caret.x;
				loc.y = loc.y + caret.y + 16;
				
//				if (size.x < owner.getSize().x){
//					loc.x += owner.getSize().x - shell.getSize().x;
//				}
//				if (loc.x < 0)
//					loc.x = 0;
//				if ((loc.x + size.x) > owner.getDisplay().getClientArea().width)
//					loc.x = owner.getDisplay().getClientArea().width - size.x;
//								
//				loc.y += owner.getSize().y;
//				
//				if ((loc.y + size.y) > owner.getDisplay().getClientArea().height)
//					loc.y -= owner.getSize().y + size.y;
				
				shell.setLocation(loc);
				
				shell.setVisible(true);

				owner.redraw();
			}}
		);
	}
	
	private boolean isOpen(){
		if (shell == null)
			return false;
		return (!shell.isDisposed() && shell.isVisible());
	}
	
	private void hide(){
		active = false;
		if (!isOpen())
			return;
		owner.getDisplay().asyncExec(new Runnable(){
			public void run() {
		    	ownerShell.setActive();
		    	if (!owner.isDisposed())
		    		AutoComplete.this.owner.forceFocus();
		    	if (!shell.isDisposed())
		    		shell.setVisible(false);
		    	if (!owner.isDisposed()){
		    		AutoComplete.this.owner.forceFocus();
		    		owner.redraw();
		    	}
			}}
		);
	}
	
	private void dispose(){
		if (shell != null)
			shell.dispose();
		if (mouseDownFilter != null)
			owner.getDisplay().removeFilter(SWT.MouseDown,mouseDownFilter);
		if (shell != null){
			ownerShell.removeControlListener(parentShellControlListener);
			ownerShell.removeShellListener(parentShellShellListener);
		}
	}

	/**
	 * Sets how many items will be visible in the autocomplete popup.
	 * @param items Items to be visible
	 * 
	 */
	public void setVisibleItemCount(int items){
		visibleItemCount = items;
	}
	
	/**
	 * Returns the number of items that will be visible in the
	 * autocompletion popup.
	 * 
	 * @return Items to be visible
	 */
	public int getVisibleItemCount(){
		return visibleItemCount;
	}
	
	private void keyUp() {
		int sel = list.getSelectionIndex();
		if (sel < 1)
			return;
		list.setSelection(sel -1);
		list.showSelection();
//		if (list.getSelection().length > 0)
//			setText(list.getSelection()[0]);
	}

	private void keyDown() {
		if (list.getItemCount() == 0)
			return;
		int sel = list.getSelectionIndex();
		if (sel == -1){
			list.setSelection(0);
		} else if (sel != list.getItemCount() -1){
			list.setSelection(sel +1);
		} else if (sel == list.getItemCount()){
			return;
		}
		list.showSelection();
//		if (list.getSelection().length > 0)
//			setText(list.getSelection()[0]);
	}
	
	private void scroll(int linesToScroll){
		linesToScroll = -linesToScroll;
		if (list.getItemCount() == 0)
			return;
		
		int current = list.getTopIndex();
		current += linesToScroll;
		if (current < 0){
			current = 0;
		} else if (current > list.getItemCount() -1){
			current = list.getItemCount() -1;
		}
		list.setTopIndex(current);
	}

	private void setText(final String text){
		owner.getDisplay().asyncExec(new Runnable(){public void run() {
			ignoreModification = true;
			provider.doAutoCompleate(owner, text);
			ignoreModification = false;
		}});		
	}

	public void setActive(boolean active ) {
		if (active) {
			show();
		} else {
			hide();
		}
		this.active = active;
	}

	public boolean isActive() {
		return active;
	}
	
	
}