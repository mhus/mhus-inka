package de.mhus.cap.core.dnd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.WeakHashMap;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

/**
 * Class for serializing gadgets to/from a byte array
 */
public class CaoTransfer extends ByteArrayTransfer {

	private static CaoTransfer _instance = new CaoTransfer();

	private static final String CF_TEXT = "cao-transfer-format"; //$NON-NLS-1$ 
	private static final int CF_TEXTID = registerType(CF_TEXT);

	private WeakHashMap<String, ICaoExchange> tempCache = new WeakHashMap<String, ICaoExchange>();

	private int nextId;
	
	private CaoTransfer() {
	}

	/**
	 * * Returns the singleton instance of the CaoTransfer class. * * @return
	 * the singleton instance of the CaoTransfer class
	 */
	public static CaoTransfer getInstance() {
		return _instance;
	}

	protected int[] getTypeIds() {
		return new int[] { CF_TEXTID };
	}

	protected String[] getTypeNames() {
		return new String[] { CF_TEXT };
	}

	public void javaToNative(Object object, TransferData transferData) {
		if (object == null || !(object instanceof ICaoExchange[]))
			return;

		if (isSupportedType(transferData)) {
			ICaoExchange[] myTypes = (ICaoExchange[]) object;
			try {

				// write data to a byte array and then ask super to convert to
				// pMedium
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				DataOutputStream writeOut = new DataOutputStream(out);
				writeOut.writeInt(myTypes.length);
				for (int i = 0, length = myTypes.length; i < length; i++) {
					writeOut
							.writeUTF(getTempElementId(myTypes[i]));
				}
				byte[] buffer = out.toByteArray();
				writeOut.close();

				super.javaToNative(buffer, transferData);

			} catch (IOException e) {
			}
		}
	}

	public Object nativeToJava(TransferData transferData) {

		if (isSupportedType(transferData)) {

			byte[] buffer = (byte[]) super.nativeToJava(transferData);
			if (buffer == null)
				return null;

			try {
				ByteArrayInputStream in = new ByteArrayInputStream(buffer);
				DataInputStream readIn = new DataInputStream(in);

				int size = readIn.readInt();
				LinkedList<ICaoExchange> list = new LinkedList<ICaoExchange>();
				for (int i = 0; i < size; i++) {

					ICaoExchange datum = getTempElement(readIn.readUTF());
					if (datum != null)
						list.add(datum);
				}
				readIn.close();

				return list.toArray(new ICaoExchange[list.size()]);

			} catch (IOException ex) {
				return null;
			}
		}

		return null;
	}

	private String getTempElementId(ICaoExchange element) {
		synchronized (tempCache) {
			long myId = nextId++;
			String id = myId +"_" + System.currentTimeMillis();
			
			tempCache.put(id, element);
			
			return id;
		}
	}

	private ICaoExchange getTempElement(String id) {
		synchronized (tempCache) {
			ICaoExchange element = tempCache.get(id);
			return element;
		}
	}
	
}
