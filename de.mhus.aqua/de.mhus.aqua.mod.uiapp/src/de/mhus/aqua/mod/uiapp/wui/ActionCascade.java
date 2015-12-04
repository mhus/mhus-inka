package de.mhus.aqua.mod.uiapp.wui;

public class ActionCascade implements Action {

	private Action[] actions;

	public ActionCascade(Action ...actions) {
		this.actions = actions;
	}
	
	@Override
	public String paint() {
		StringBuffer sb = new StringBuffer();
		for (Action action : actions)
			sb.append(action.paint());
		return sb.toString();
	}

}
