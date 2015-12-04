package de.mhus.cao.model.fs;

import java.io.File;

import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoInvalidException;
import de.mhus.lib.cao.CaoMetaDefinition;
import de.mhus.lib.cao.util.WritableElement;

public class IoWritable extends WritableElement {

	public IoWritable(CaoElement master) throws CaoException {
		super(master);
	}

	@Override
	public void save() throws CaoException {
		if (!isValid()) throw new CaoInvalidException();
		File file = new File(master.getId());
		String newName = null;
		for (String name : data.keySet()) {
			CaoMetaDefinition def = master.getMetadata().getDefinition(name);
			if (isWritable(name) && def != null ) {
				if ("writable".equals(name))
					file.setWritable(getBoolean(name,false));
				else
				if ("readable".equals(name))
					file.setReadable(getBoolean(name,true));
				else
				if ("executable".equals(name))
					file.setExecutable(getBoolean(name,false));
				else
				if ("name".equals(name)) {
					newName = getString(name);
				} else
				if ("modified".equals(name))
					file.setLastModified(getLong(name,0));

			}
		}

		reload();
		
		if (newName!=null && !file.getName().equals(newName)) {
			getConnection().fireElementDeleted(getId());
			file.renameTo( new File(file.getParentFile(),newName ) );
			getConnection().fireElementCreated(file.getAbsolutePath());
			((IoElement)master).setInvalid();
		} else {
			master.reload();
			getConnection().fireElementUpdated(getId());
		}
	}

	public boolean isWritable(String name) {
		if (name.equals("directory"))
			return false;
		if (name.equals("hidden"))
			return false;
		if (name.equals("path"))
			return false;
		return true;
	}

	@Override
	public boolean isValid() {
		return master.isValid();
	}

	@Override
	public CaoElement getParent() {
		return master.getParent();
	}

}
