package de.mhus.aqua.res;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import de.mhus.aqua.Activator;
import de.mhus.aqua.api.AquaRequest;
import de.mhus.lib.MFile;
import de.mhus.lib.logging.Log;

public class ZipRes extends AquaRes {

	private static Log log = Log.getLog(ZipRes.class);
	private String path;
	private ZipFile zipFile;
	private String subPath;
	private long modified;

	@Override
	public void process(AquaRequest request) throws Exception {
		
		if (path == null) {
			path = config.getExtracted("path","");
			File f = new File(path);
			modified = f.lastModified();
			subPath = config.getExtracted("subpath","");
			zipFile = new ZipFile(f);
		}
		
		request.markStaticContent();
		
		if (request.notModifiedSince(modified))
			return;
				
		String file = subPath + request.getExtPath();
		if (file.startsWith("/")) file = file.substring(1);
		
		log.t("deliver",path,file);
		ZipEntry entry = zipFile.getEntry(file);
		if (entry == null) {
			log.d("not found",getName(),path,file);
			request.sendErrorNotFound();
			return;
		}
		InputStream is = zipFile.getInputStream(entry);
		
		OutputStream os = request.getOutputStream();
		MFile.copyFile(is, os);
		is.close();
		
	}

}
