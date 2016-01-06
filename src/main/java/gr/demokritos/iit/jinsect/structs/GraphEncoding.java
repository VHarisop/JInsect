package gr.demokritos.iit.jinsect.structs;


public interface GraphEncoding {

	/**
	 * Retuns a string representation of a UniqueJVertexGraph that 
	 * is a result of a type of encoding (e.g. DFS Encoding)
	 *
	 */
	public String getEncoding();

	/**
	 * Retuns a string representation of a UniqueJVertexGraph that 
	 * is a result of a type of encoding starting on a given node.
	 *
	 * @param vStart the JVertex to start encoding from
	 */
	public String getEncoding(JVertex vStart);
}
