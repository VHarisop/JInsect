package gr.demokritos.iit.jinsect.representations;

import gr.demokritos.iit.jinsect.structs.UniqueVertexGraph;
import gr.demokritos.iit.jinsect.structs.Edge;

import java.io.IOException;
import java.io.FileNotFoundException;

import java.util.HashSet;

/**
 * An interface that must be implemented by all N-Gram Graph representations.
 *
 * @author VHarisop
 */
public interface NGramGraph extends Cloneable {
	/**
	 * Computes an indication of the size of an n-gram graph based on the
	 * edge count of its contained graphs.
	 *
	 * @return The sum of the count of the edges of the graph's various levels
	 */
	public int length();

	/**
	 * @return the minimum n-gram size of the graph
	 */
	public int getMinSize();

	/**
	 * @return the maximum n-gram size of the graph
	 */
	public int getMaxSize();

	/**
	 * @return the size of the graph's correlation window
	 */
	public int getWindowSize();

	/**
	 * Checks if the n-gram graph is empty.
	 *
	 * @return true if the graph is empty, else false
	 */
	public boolean isEmpty();

	/**
	 * Sets all the edge weights for all the graphs to 0.
	 */
	public void nullify();

	/**
	 * Removes an item (node) from all levels' graphs.
	 *
	 * @param sItem the label of the node to remove
	 */
	public void deleteItem(String sItem);

	/**
	 * Returns the degradation degree of a specified edge.
	 *
	 * @param e the edge whose degradation degree is requested
	 * @return the degradation degree of the edge
	 */
	public double degradationDegree(Edge e);

	/**
	 * Returns a graph level specified by a given index.
	 *
	 * @param index the index of the graph. Zero (0) indicates the level that
	 * corresponds to the minimum n-gram size.
	 * @return the {@link UniqueVertexGraph} of the corresponding level.
	 */
	public UniqueVertexGraph getGraphLevel(int index);

	/**
	 * Returns the graph level corresponding to the given n-gram size.
	 *
	 * @param ngramSize the n-gram size of the level
	 * @return the {@link UniqueVertexGraph} of the corresponding level.
	 */
	public UniqueVertexGraph getGraphLevelByNGramSize(int ngramSize);

	/**
	 * Sets the data string for this graph, clears all previous graph levels
	 * and creates them anew.
	 *
	 * @param sDataString the new data string to be represented
	 */
	public void setDataString(String sDataString);

	/**
	 * Returns the data string that is represented by this graph.
	 *
	 * @return the data string this graph represents
	 */
	public String getDataString();

	/**
	 * Returns a hash set containing all the edges of the graph.
	 *
	 * @return a hashset with all of the graph's edges
	 */
	public HashSet<Edge> getAllNodes();
	
	/**
	 * Creates the graph based on a data string loaded from a given file.
	 *
	 * @param sFilename the name of the file containing the data string
	 */
	public void loadDataStringFromFile(String sFilename)
		throws IOException, FileNotFoundException;

	/**
	 * Computes the intersection of the graph with another graph.
	 *
	 * @param otherGraph the graph to intersect with
	 * @return the graph resulting from the intersection
	 */
	public NGramGraph intersectGraph(NGramGraph otherGraph);

	/**
	 * Returns the difference (inverse of the intersection) graph between
	 * the current graph and another given graph.
	 *
	 * @param otherGraph the graph to compare against
	 * @return a NGramGraph that is the difference between the current and
	 * the given graph
	 */
	public NGramGraph inverseIntersectGraph(NGramGraph otherGraph);

	/**
	 * Returns both the intersection and difference (inverse of the intersection)
	 * graph between the current graph and a given graph.
	 *
	 * @param otherGraph the graph to be used for intersection and difference
	 * @return an array of two elements, of which the first is the intersection
	 * graph and the second the difference of the two graphs.
	 */
	public NGramGraph[] intersectAndDeltaGraph(NGramGraph otherGraph);

	/**
	 * Merges the graph with another specified graph using a specified
	 * tendency for weighting the merged edges.
	 *
	 * @param otherGraph the graph to merge with
	 * @param weightPercent the convergence tendency parameter. A value of
	 * 0.0 means no change to the existing object, 1.0 means the updated
	 * object is the same as the new object. A value of 0.5 means the new
	 * object is equally similar to the two source objects (averaging effect).
	 */
	public void merge(NGramGraph otherGraph, double weightPercent);

	/**
	 * Prunes the graph's vertices whose coexistence importance is below a
	 * specified threshold.
	 *
	 * @param minCoexistenceImportance the coexistence importance threshold
	 */
	public void prune(double minCoexistenceImportance);

	/**
	 * Computes the graph that consists of all vertices and edges that are
	 * not in another specified graph.
	 *
	 * @param otherGraph the other graph
	 * @return the graph that consists of vertices and edges not in the other
	 * graph
	 */
	public NGramGraph allNotIn(NGramGraph otherGraph);

	/**
	 * Creates a clone of this object.
	 *
	 * @return a clone of the object
	 */
	public NGramGraph clone();
}
