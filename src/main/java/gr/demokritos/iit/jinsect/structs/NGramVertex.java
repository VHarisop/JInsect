package gr.demokritos.iit.jinsect.structs;

import java.util.Objects;

/**
 * Simple class implementing {@link Vertex} for n-grams.
 * The label is the ngram itself, which is provided by the
 * default constructor.
 *
 * @author VHarisop
 */
public class NGramVertex implements JVertex {

	// vertex label
	private String label;

	/**
	 * Creates a <tt>NGramVertex</tt> object, to represent
	 * an n-gram provided in text format.
	 * @param ngram the ngram to be represented
	 * @return a new <tt>NGramVertex</tt> object.
	 */
	public NGramVertex(String ngram) {
		label = ngram;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String newLabel) {
		label = newLabel;
	}

	/**
	 * Gets the string representation of the vertex,
	 * which is simply the vertex's label.
	 * @return the vertex's string representation 
	 */
	public String toString() {
		return label;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof NGramVertex)) 
			return false;
		
		//If other object is an instance of NGramVertex,
		//their labels are used for comparison
		 
		return this.getLabel().equals(((NGramVertex) o).getLabel());

	}

	@Override
	public int hashCode() {
		return Objects.hash(this.getLabel());
	}

}
