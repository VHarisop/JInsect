package gr.demokritos.iit.jinsect.documentModel.representations;

import gr.demokritos.iit.jinsect.structs.UniqueJVertexGraph;
import gr.demokritos.iit.jinsect.structs.Edge;

import java.io.IOException;
import java.io.FileNotFoundException;

import java.util.HashSet;

/**
 * The interface implemented by all n-gram graph representations.
 *
 * @author VHarisop
 */
public interface NGramGraph {

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
	 * @return the {@link UniqueJVertexGraph} of the corresponding level.
	 */
	public UniqueJVertexGraph getGraphLevel(int index);

	/**
	 * Returns the graph level corresponding to the given n-gram size.
	 *
	 * @param ngramSize the n-gram size of the level
	 * @return the {@link UniqueJVertexGraph} of the corresponding level.
	 */
	public UniqueJVertexGraph getGraphLevelByNGramSize(int ngramSize);

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
}
