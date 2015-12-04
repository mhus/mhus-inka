package de.mhus.aqua.mod.uiapp;

import java.io.PrintWriter;

import de.mhus.aqua.api.AquaRequest;
import de.mhus.aqua.mod.uiapp.wui.WDialog;
import de.mhus.lib.MException;

public abstract class DialogCloseAction extends AjaxAction {

	private UiContainer container;
	private WDialog dialog;

	public DialogCloseAction(UiContainer container, UiBox box, WDialog dialog) {
		super(box);
		this.container = container;
		this.dialog = dialog;
	}

	@Override
	public void processAjax(AquaRequest request, PrintWriter writer)
	throws MException {
		container.removeDialog(dialog);
		super.processAjax(request, writer);
	}

}
