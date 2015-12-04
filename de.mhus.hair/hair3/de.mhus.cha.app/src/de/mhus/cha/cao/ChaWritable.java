package de.mhus.cha.cao;

import java.io.File;
import java.io.FileOutputStream;

import org.w3c.dom.Element;

import de.mhus.cap.core.Access;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoInvalidException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoMetaDefinition;
import de.mhus.lib.cao.util.WritableElement;
import de.mhus.lib.MXml;

public class ChaWritable extends WritableElement<Access> {

	private Element xml;

	public ChaWritable(CaoElement<Access> master, Element data) throws CaoException {
		super(master);
		this.xml = data;
	}

	@Override
	public void save() throws CaoException {
		
		if (!isValid()) throw new CaoInvalidException();
		
		// manipulate data structure
		File file = new File(master.getId());
		for (String name : data.keySet()) {
			CaoMetaDefinition def = master.getMetadata().getDefinition(name);
			if (isWritable(name) && def != null ) {
				Element node = MXml.getElementByPath(xml, name);
				if (node == null) {
					node = xml.getOwnerDocument().createElement(name);
					xml.appendChild(node);
				}
				Object obj = data.get(name);
				if (obj != null) {
					if (obj instanceof CaoList) {
						// TODO handle repeatings
					} else {
						node.setTextContent(obj.toString());
					}
				}
			}
		}

		// save file
		try {
			FileOutputStream fos = new FileOutputStream( new File ( ((ChaElement)master).getFile(), "data.xml" ) );
			MXml.saveXml(xml, fos);
			fos.close();
		} catch (Exception e) {
			throw new CaoException(getId(), e);
		}
		
		// reload
		reload();		
		master.reload();
		getConnection().fireElementUpdated(getId());
		
	}

	public boolean isWritable(String name) {
		return true;
	}

	@Override
	public boolean isValid() {
		return master.isValid();
	}

	@Override
	public CaoElement<Access> getParent() {
		return master.getParent();
	}

}
