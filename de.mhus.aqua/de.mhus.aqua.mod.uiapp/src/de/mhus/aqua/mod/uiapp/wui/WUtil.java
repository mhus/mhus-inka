package de.mhus.aqua.mod.uiapp.wui;

public class WUtil {

	public enum DIRECTION {NORTH,EAST,SOUTH,WEST,CENTER};
	
	public static String toSize(String in) {
		if (in.endsWith("%"))
			return "'"+in+"'";
		return in;
	}

	public static String toText(String in) {
		if (in == null) return "";
		return in;
	}

	public static String toText(WNls nls,String in) {
		if (in == null) return "";
		if (nls == null) {
			int pos = in.indexOf('=');
			if (pos < 0)
				return toText(in);
			else
				return toText(in.substring(pos+1));
		}
		return toText(nls.find(in));
	}
	
}
