package gr.demokritos.iit.jinsect.structs;

import java.util.Objects;
import org.jgrapht.*;
import org.jgrapht.graph.*;

/**
 * Simple alias class for DefaultWeightedEdge in order 
 * to keep compatibility unbroken.
 */
public class Edge extends DefaultWeightedEdge
{
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new <tt>Edge</tt> object, which is created by
	 * directly calling the superclass' constructor. This is a
	 * separate constructor mainly to avoid "EdgeFactory failed"
	 * errors/warnings.
	 *
	 * @return a new <tt>Edge</tt> object
	 *  
	 */
	public Edge() {
		super();
	}
	
	/**
	 * Simple getter for the label of the source object.
	 * @return a <tt>String</tt> representing the source object.
	 */
	public String getSourceLabel() {
		return String.valueOf(getSource());
	}
	
	/**
	 * Simple getter for the label of the target object.
	 * @return a <tt>String</tt> representing the target object.
	 */
	public String getTargetLabel() {
		return String.valueOf(getTarget());
	}

	/**
	 * Returns a label for the whole edge that consists of the
	 * source and target labels connected via an arrow showing
	 * the edge's direction. 
	 * 
	 * @return a string with the edge labels
	 */
	public String getLabels() {
		return getSourceLabel() + "->" + getTargetLabel();
	}

	/**
	 * Simple getter for the weight of the edge.
	 * @return a double containing the edge weight.
	 */
	public double edgeWeight() {
		return super.getWeight();
	}

	@Override 
	public String toString() {
		return super.toString() + "[" + String.valueOf(getWeight()) + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(getSource(), getTarget());
	}

	@Override
	public boolean equals(Object oOther) {
		if (!(oOther instanceof Edge)) 
			return false;

		Edge eOther = (Edge) oOther;
		return this.getLabels().equals(eOther.getLabels());
	}
}
