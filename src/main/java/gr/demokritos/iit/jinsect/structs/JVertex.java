package gr.demokritos.iit.jinsect.structs;

/**
 * Interface for Vertex objects, which are used internally
 * by N-gram graphs to represent N-grams, words, or other tokens.
 * 
 * @author VHarisop
 */
public interface JVertex {
	
	/**
	 * Get the vertex's label, which is the string representation
	 * of the object represented by the vertex.
	 * @return the label of the vertex 
	 */
	public String getLabel();

	/**
	 * Set the label on the vertex. 
	 * @param label the new label of the vertex
	 */
	public void setLabel(String label);
}
