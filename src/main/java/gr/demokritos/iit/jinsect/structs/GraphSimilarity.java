package gr.demokritos.iit.jinsect.structs;

/**
 * A class that holds info about the similarity between two graphs.
 * Contains separate fields for the graph's:
 *     - ValueSimilarity
 *     - ContainmentSimilarity
 *     - SizeSimilarity
 *     - StructuralSimilarity 
 *
 * @author ggianna 
 * @author VHarisop
 */
public class GraphSimilarity {
	/**
	 * Specifies the similarity by means of co-existing nodes, as well
	 * as their values. Range in [0, 1] - 0 means no node of A is in B,
	 * 1 means all nodes of A exist in B and their weights are identical.
	 */
	public double ValueSimilarity;

	/**
	 * Specifies the similarity by means of co-existing nodes only.
	 * Range of possible values is in [0, 1] - 0 means no node of A is in B,
	 * 1 means all nodes of A are in B.
	 */
	public double ContainmentSimilarity;

	/**
	 * Specifies the similarity by means of size. Range of possible values 
	 * is in [0, 1] - 1 means that A has the same number of edges as B.
	 */
	public double SizeSimilarity;

	/**
	 * Specifies the graph structural similarity. 
	 */
	public double StructuralSimilarity;

	/**
	 * Returns a blank GraphSimilarity object with all similarity
	 * values initialized to 0.
	 */
	public GraphSimilarity() {
		ValueSimilarity = 0;
		ContainmentSimilarity = 0;
		SizeSimilarity = 0;
		StructuralSimilarity = 0;
	}

	/**
	 * Calculates the overall similarity this object describes. The default method
	 * is to return the product size, value and containment similarities. 
	 * It is recommended to override this method, should another type of 
	 * calculation be required.
	 *
	 * @return the overall similarity 
	 */
	public double getOverallSimilarity() {
		return ValueSimilarity * ContainmentSimilarity * SizeSimilarity;
	}

	/**
	 * Calculates an overall distance as a function of the overall similarity.
	 * The distance is the inverse of the overall similarity. In case overall 
	 * similarity is equal to 0, POSITIVE_INFINITY is returned.
	 *
	 * @return the overall distance, ranging from 0 to positive infinity
	 */
	public double asDistance() {
		double overall = getOverallSimilarity();
		if (overall == 0) 
			return Double.POSITIVE_INFINITY;
		else
			return 1.0 / overall;
	}
}
