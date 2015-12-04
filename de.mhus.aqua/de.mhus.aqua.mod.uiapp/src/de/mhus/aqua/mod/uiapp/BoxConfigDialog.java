package de.mhus.aqua.mod.uiapp;

import de.mhus.aqua.api.AquaRequest;
import de.mhus.aqua.mod.uiapp.wui.ActionCascade;
import de.mhus.aqua.mod.uiapp.wui.WDialog;
import de.mhus.aqua.mod.uiapp.wui.WForm;
import de.mhus.lib.MException;
import de.mhus.lib.form.MForm;

public class BoxConfigDialog extends WDialog {

	private DialogCloseAction closeButton;
	private AjaxAction submitButton;
	private MForm form;
	private UiContainer container;

	public BoxConfigDialog(AquaRequest request,UiContainer container, UiBox box) throws Exception {
		initWElement(request, null, config);
		setNls(box.getNls());
		this.container = container;
		closeButton = new DialogCloseAction(container,box,this) {

			@Override
			public AjaxActionDefinition[] doRequest(AquaRequest request)
					throws Exception {
				BoxConfigDialog.this.close();
				return new AjaxActionDefinition[0];
			}
		};
		setCloseAction(closeButton);
		
		submitButton = new AjaxAction(box) {
			
			@Override
			public AjaxActionDefinition[] doRequest(AquaRequest request)
					throws Exception {
				
				return new AjaxActionDefinition[] {
					box.ajaxActionDefinitionRefresh()
				};
			}
		};
		
		form = box.getConfigForm();
		WForm wform = new WForm();
		wform.initWElement(request, null, null);
		
		wform.setWidth("100%");
		wform.setHeight("100%");
		wform.setTitle(nls == null ? box.getTitle() : nls.find("config_dialog_title=Configure $0$", box.getTitle() ) );
		
		try {
			FormSource source = new FormSource(box, form);
			wform.setSource(source);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// wform.setAutorender(false);
		// onCompleteAction(new WActionCascade(wform.getRenderAction(),wform.getLoadAction()) );
		//wform.onCompleteAction(wform.getLoadAction());
		
		addButton("save=Save", wform.getSaveAction(new ActionCascade(submitButton, getCloseDialogAction() ), null ) );
		//addButton("reset=Reset", wform.getLoadAction());
		//onCompleteAction(wform.getLoadAction() );
		addContent(wform);
		
	}
	
	public void show() {
		container.addDialog(this);
	}
	
	public void close() {
		container.removeDialog(this);
		closeButton.close();
		submitButton.close();
	}

}
