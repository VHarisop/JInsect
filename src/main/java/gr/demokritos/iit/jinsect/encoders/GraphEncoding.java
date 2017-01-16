package gr.demokritos.iit.jinsect.encoders;

import gr.demokritos.iit.jinsect.structs.JVertex;
import gr.demokritos.iit.jinsect.structs.UniqueVertexGraph;

public interface GraphEncoding {

	/**
	 * Retuns a string representation of a UniqueJVertexGraph that
	 * is a result of a type of encoding (e.g. DFS Encoding)
	 *
	 * @param uvg the {@link UniqueVertexGraph} to encode
	 */
	public String getEncoding(UniqueVertexGraph uvg);

	/**
	 * Retuns a string representation of a UniqueJVertexGraph that
	 * is a result of a type of encoding starting on a given node.
	 *
	 * @param uvg the {@link UniqueVertexGraph} to encode
	 * @param vStart the JVertex to start encoding from
	 */
	public String getEncoding(UniqueVertexGraph uvg, JVertex vStart);
}
