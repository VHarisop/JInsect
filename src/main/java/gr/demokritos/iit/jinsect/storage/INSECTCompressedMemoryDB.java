/* Under the terms of LGPL
*/

package gr.demokritos.iit.jinsect.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import gr.demokritos.iit.jinsect.Logging;

/**
 * A class that uses memory for storage, while compressing the data of stored
 * objects to optimize memory use.
 *
 * @author ggianna
 */
public class INSECTCompressedMemoryDB<TObjectType extends Serializable>
	extends INSECTMemoryDB<TObjectType> implements Serializable
{
	static final long serialVersionUID = 1L;

	private static final Logger logger =
			Logging.getLogger(INSECTCompressedMemoryDB.class.getName());

	@Override
	public void saveObject(Serializable oObj, String sObjectName, String sObjectCategory) {
		final ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		try (
			GZIPOutputStream gzOut = new GZIPOutputStream(bOut);
			ObjectOutputStream oOut = new ObjectOutputStream(gzOut);
		) {
			oOut.writeObject(oObj);
			oOut.flush();
			gzOut.flush();
			bOut.close();
		} catch (final IOException e) {
			logger.severe("Cannot save object to memory. Reason:");
			logger.severe(e.getMessage());
			return;
		}

		ObjectMap.put(
				getObjectName(sObjectName, sObjectCategory),
				bOut.toByteArray());
	}

	@Override
	@SuppressWarnings("unchecked")
	public TObjectType loadObject(String sObjectName, String sObjectCategory) {
		final ByteArrayInputStream bIn =
			new ByteArrayInputStream((byte[])ObjectMap.get(
					getObjectName(sObjectName, sObjectCategory)));
		TObjectType tObj = null;
		try {
			final GZIPInputStream gzIn = new GZIPInputStream(bIn);
			final ObjectInputStream oIn = new ObjectInputStream(gzIn);
			tObj = (TObjectType) oIn.readObject();
			oIn.close();
			gzIn.close();
			bIn.close();
		} catch (final IOException | ClassNotFoundException ex) {
			logger.severe("Cannot load object from memory. Reason:");
			logger.severe(ex.getMessage());
		}

		return tObj;
	}

	@Override
	protected String getObjectName(String sObjectName, String sObjectCategory) {
		return String.valueOf(super.getObjectName(sObjectName, sObjectCategory).hashCode());
	}

	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream in)
		throws IOException, ClassNotFoundException {
		ObjectMap = (HashMap<String, Serializable>) in.readObject();
	}

	private void writeObject(ObjectOutputStream out)
		throws IOException {
		out.writeObject(ObjectMap);
	}
}
