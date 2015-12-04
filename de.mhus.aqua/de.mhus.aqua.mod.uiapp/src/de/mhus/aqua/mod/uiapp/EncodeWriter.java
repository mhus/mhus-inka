package de.mhus.aqua.mod.uiapp;

import java.io.IOException;
import java.io.Writer;

public class EncodeWriter extends Writer {

	private Writer parent;
	private char[] quot = new char[] { '\\', '"' };

	public EncodeWriter(Writer parent) {
		this.parent = parent;
	}
	
	@Override
	public void close() throws IOException {
		parent.close();
	}

	@Override
	public void flush() throws IOException {
		parent.flush();
	}

	@Override
	public void write(char[] buffer, int offset, int len) throws IOException {
		int nextoff = offset;
		int nextlen = len;
		for (int i = 0; i <len; i++) {
			if (buffer[offset+i] == '"') {
				if (i != 0)
					parent.write(buffer,nextoff,i - (len-nextlen));
					parent.write(quot,0,2);
					nextoff = offset+i+1;
					nextlen = len-i-1;
			}
		}
		parent.write(buffer,nextoff,nextlen);
	}

}
