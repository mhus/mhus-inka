package de.mhus.aqua.mod.uiapp.wui;

import java.io.PrintWriter;

import de.mhus.aqua.api.AquaRequest;
import de.mhus.aqua.api.AquaSession;
import de.mhus.aqua.api.IUserRights;
import de.mhus.aqua.cao.AquaChildList;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;

public class WNavigation extends IWComponent {

	public void paint(AquaRequest data, PrintWriter stream) {
		stream.print("<div class='nextnodes' ");
		stream.print(this.getClass().getCanonicalName());
		paintTagAttributes(stream);
		stream.print("><ul>");
		
		try {
			AquaChildList list = (AquaChildList)data.getNode().getChildren(data.getSession());
			list.setSession(data.getSession());
			list.setUserRight(IUserRights.SHOW);

			stream.print("<li><a href='");
			stream.print(data.getPath());
			stream.print("/");
			stream.print("'>");
			stream.print("Home");
			stream.print("</a>");

			for ( CaoElement<AquaSession> ele : list.getElements() ) {
				// if (!ele.getBoolean("hidden",true)) {
				if (ele != null) {
					stream.print("<li><a href='");
					stream.print(data.getPath());
					stream.print("/");
					stream.print(ele.getString("name"));
					stream.print("'>");
					stream.print(ele.getString("name"));
					stream.print("</a>");
			   }
			}
		} catch (CaoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		stream.print("</ul></div>");
		
	}

}
