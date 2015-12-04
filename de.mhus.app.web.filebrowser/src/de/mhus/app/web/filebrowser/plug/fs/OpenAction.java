package de.mhus.app.web.filebrowser.plug.fs;

import java.io.DataInputStream;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import de.mhus.app.web.filebrowser.api.FileBrowserAction;
import de.mhus.app.web.filebrowser.api.FileBrowserNode;
import de.mhus.app.web.filebrowser.api.ProvideInputStream;

public class OpenAction implements FileBrowserAction {

	@Override
	public boolean canExecute(FileBrowserNode node) {
		return (node instanceof ProvideInputStream) && node.isFile();
	}

	@Override
	public boolean doExecute(FileBrowserNode node, ServletContext context, HttpServletResponse response)
			throws ServletException, IOException {
		
		int length   = 0;
        ServletOutputStream outStream = response.getOutputStream();
        String mimetype = context.getMimeType(node.getName());
        
        // sets response content type
        if (mimetype == null) {
            mimetype = "application/octet-stream";
        }
        response.setContentType(mimetype);
        response.setContentLength((int)node.getLength());

        // sets HTTP header
        response.setHeader("Content-Disposition", "attachment; filename=\"" + node.getName() + "\"");
        
        int BUFSIZE = 4096;
        byte[] byteBuffer = new byte[BUFSIZE];
        DataInputStream in = new DataInputStream(((ProvideInputStream)node).getInputStream());
        
        // reads the file's bytes and writes them to the response stream
        while ((in != null) && ((length = in.read(byteBuffer)) != -1))
        {
            outStream.write(byteBuffer,0,length);
        }
        
        in.close();
        outStream.close();
        
		return false;
	}

	@Override
	public String getTitle() {
		return "Open";
	}

	public String toString() {
		return "fs.open";
	}
}
