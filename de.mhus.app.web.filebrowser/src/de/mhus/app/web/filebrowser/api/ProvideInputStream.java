package de.mhus.app.web.filebrowser.api;

import java.io.IOException;
import java.io.InputStream;

public interface ProvideInputStream {

	InputStream getInputStream() throws IOException;

}
