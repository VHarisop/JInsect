package gr.demokritos.iit.jinsect.representations;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import gr.demokritos.iit.jinsect.Logging;
import gr.demokritos.iit.jinsect.io.LineReader;
import gr.demokritos.iit.jinsect.structs.Edge;
import gr.demokritos.iit.jinsect.structs.EdgeCachedLocator;
import gr.demokritos.iit.jinsect.structs.JVertex;
import gr.demokritos.iit.jinsect.structs.NGramVertex;
import gr.demokritos.iit.jinsect.structs.UniqueVertexGraph;

/**
 * A version of the {@link NGramJGraph} class, using a symmetrical correlation
 * window for n-gram cooccurences.
 *
 * @author ggianna
 * @author VHarisop
 */
public class NGramSymJGraph extends NGramJGraph {
	final static long serialVersionUID = 1L;

	private static final Logger logger =
		Logging.getLogger(NGramSymJGraph.class.getName());

	/**
	 * Speeds up edge location in a given graph, by caching last searches.
	 */
	public EdgeCachedLocator eclLocator = null;

	/**
	 * Creates a NGramSymJGraph object with default parameters
	 * (MinSize = MaxSize = CorrelationWindow = 3) and empty
	 * initial data string.
	 *
	 * @return a NGramSymJGraph object with empty data string
	 */
	public NGramSymJGraph() {
		InitGraphs();
	}

	/**
	 * Creates a NGramSymJGraph object with an empty initial data string
	 * and custom size and window parameters.
	 *
	 * @param iMinSize the minimum n-gram size
	 * @param iMaxSize the maximum n-gram size
	 * @param iCorrelationWindow the correlation window length
	 * @return a NGramSymJGraph object with custom parameters
	 */
	public NGramSymJGraph(
		final int iMinSize,
		final int iMaxSize,
		final int iCorrelationWindow)
	{
		MinSize = iMinSize;
		MaxSize = iMaxSize;
		CorrelationWindow = iCorrelationWindow;

		InitGraphs();
	}

	/**
	 * Creates a NGramSymJGraph object to represent a given data string
	 * with default parameters (MinSize = MaxSize = CorrelationWindow = 3).
	 *
	 * @param dataString the data string to be represented
	 * @return a new NGramSymJGraph with default parameters
	 */
	public NGramSymJGraph(final String dataString) {
		setDataString(dataString);
	}

	/**
	 * Creates a NGramSymJGraph object to represent a given data string
	 * with custom size and window parameters.
	 *
	 * @param dataString the data string to be represented
	 * @param iMinSize the minimum n-gram size
	 * @param iMaxSize the maximum n-gram size
	 * @param iCorrelationWindow the correlation window length
	 * @return a NGramSymJGraph object with an initial data string
	 * and custom parameters
	 */
	public NGramSymJGraph(
		final String dataString,
		final int iMinSize,
		final int iMaxSize,
		final int iCorrelationWindow)
	{
		MinSize = iMinSize;
		MaxSize = iMaxSize;
		CorrelationWindow = iCorrelationWindow;
		setDataString(dataString);
	}

	/**
	 * This method is a proxy to {@link #fromFileLines(File)}
	 * @param path the string containing the file path
	 * @return an array of {@link NGramSymJGraph} objects
	 */
	public static NGramSymJGraph[] fromFileLines(final String path)
		throws IOException, FileNotFoundException {
		return fromFileLines(new File(path));
	}

	/**
	 * Creates an array of NGramJGraph objects, each of which is built
	 * using a line from a given file as a data string.
	 *
	 * @param path the {@link java.io.File} from which to read the lines
	 * @return an array of {@link NGramJGraph} objects
	 */
	public static NGramSymJGraph[] fromFileLines(final File path)
		throws IOException, FileNotFoundException {
		/* read lines and allocate array */
		final String[] lines = new LineReader().getLines(path);
		final NGramSymJGraph[] nGraphs = new NGramSymJGraph[lines.length];

		/* build the array of n-gram graphs */
		for (int i = 0; i < lines.length; i++) {
			nGraphs[i] = new NGramSymJGraph(lines[i]);
		}

		return nGraphs;
	}

	@Override
	protected void createGraphs() {
		final String sUsableString = DataString;
		final int iLen = DataString.length();
		// Create token histogram.
		final HashMap<String, Double> hTokenAppearence =
			new HashMap<>();
		// 1st pass. Populate histogram.
		///////////////////////////////
		// For all sizes create corresponding levels
		for (int size = MinSize; size <= MaxSize; size++)
		{
			// If n-gram bigger than text
			if (iLen < size) {
				// then Ignore
				continue;
			}

			final LinkedList<String> lNGramSequence = new LinkedList<>();
			final UniqueVertexGraph gGraph = getGraphLevelByNGramSize(size);
			for (int iCurStart = 0; iCurStart < iLen; iCurStart++)
			{
				// If reached end
				if (iLen < iCurStart + size) {
					// then break
					break;
				}

				// Get n-gram
				final String sCurNGram =
					sUsableString.substring(iCurStart, iCurStart + size);

				// Update Histogram
				if (hTokenAppearence.containsKey(sCurNGram)) {
					hTokenAppearence.put(
						sCurNGram,
						hTokenAppearence.get(sCurNGram).doubleValue() + 1.0);
				} else {
					hTokenAppearence.put(sCurNGram, 1.0);
				}

				// Update list of n-grams
				lNGramSequence.add(sCurNGram);
				// Update graph
				final int iListSize = lNGramSequence.size();
				final int iTo = (iListSize - 1) >= 0 ? iListSize - 1 : 0;
				final int iFrom = (iListSize - CorrelationWindow - 1) >= 0 ?
					iListSize - CorrelationWindow - 1 : 0;

				/**
				 * Create a reversed copy of the sublist
				 */
				final List<String> revList =
					new LinkedList<>(lNGramSequence.subList(iFrom, iTo));
				Collections.reverse(revList);

				createSymEdgesConnecting(gGraph,
						sCurNGram, revList, hTokenAppearence);
			}
		}

	}

	/**
	 * Creates an edge in [gGraph] connecting [sStartNode] to each node in the
	 * [lOtherNodes] list of nodes, as well as other nodes to [sBaseNode].
	 * If an edge exists, its weight is increased by [iIncreaseWeight],
	 * else its weight is set to [iStartWeight]
	 * @param gGraph The graph to use
	 * @param sStartNode The node from which all edges begin
	 * @param lOtherNodes The list of nodes to which sBaseNode is connected.
	 * The list MUST BE ORDERED ASCENDINGLY based on distance from the <code>sStartNode</code>.
	 * @param hAppearenceHistogram The histogram of appearences of the terms
	 */
	private void createSymEdgesConnecting(
			final UniqueVertexGraph gGraph,
			final String sStartNode,
			final List<String> lOtherNodes,
			final HashMap<String, Double> hAppearenceHistogram)
	{
		double dStartWeight = 0;
		double dIncreaseWeight = 0;

		// If no neightbours
		if (lOtherNodes != null) {
			if (lOtherNodes.size() == 0)
			{
				// Attempt to add solitary node [sStartNode]
				final NGramVertex v = new NGramVertex(sStartNode);
				try {
					gGraph.add(v);
				}
				catch (final Exception e) {
					// Probably exists already
					logger.warning(e.getMessage());
				}
				return;
			}
		}

		// Otherwise for every neighbour add edge
		final Iterator<String> iIter = lOtherNodes.iterator();

		// Locate source node
		final JVertex vOldA = gGraph.locateVertex(sStartNode);
		JVertex vA;
		if (vOldA != null) {
			vA = vOldA;
		} else {
			// else create it
			vA = new NGramVertex(sStartNode);
			// Add to graph
			try {
				gGraph.add(vA);
			}
			catch (final Exception e) {
				logger.warning(e.getMessage());
			}

		}

		//////////!!!!!!!!!!!!/////////
		// TODO: MAKE SURE the order of neighbouring vertices corresponds to their distance.
		//////////!!!!!!!!!!!!/////////

		// For every edge that touches vA
		while (iIter.hasNext()) {
			final String label = iIter.next();
			final JVertex vB = new NGramVertex(label);

			double dOldWeight = 0;
			double dNewWeight = 0;

			dStartWeight = 1.0;
			dIncreaseWeight = dStartWeight;

			if (eclLocator == null) {
				eclLocator = new EdgeCachedLocator(10);
			}
			// Add one-way edge
			Edge weCorrectEdge =
				eclLocator.locateDirectedEdgeInGraph(gGraph, vA, vB);

			if (weCorrectEdge == null) {
				dNewWeight = dStartWeight;
			} else {
				dOldWeight = weCorrectEdge.edgeWeight();
				dNewWeight = dOldWeight + dIncreaseWeight; // Increase as required
			}

			try
			{
				if (weCorrectEdge == null) {
					final Edge e = gGraph.addEdge(vA, vB, dNewWeight);
					eclLocator.addedEdge(e);
				} else {
					gGraph.setEdgeWeight(weCorrectEdge, dNewWeight);
				}
			}
			catch (final Exception e)
			{
				// Unknown error. Ignore.
				logger.warning(e.getMessage());
			}

			// Add reverse edge
			weCorrectEdge = eclLocator.locateDirectedEdgeInGraph(gGraph, vB, vA);

			if (weCorrectEdge == null) {
				dNewWeight = dStartWeight;
			} else {
				dOldWeight = weCorrectEdge.edgeWeight();
				dNewWeight = dOldWeight + dIncreaseWeight; // Increase as required
			}

			try {
				if (weCorrectEdge == null) {
					final Edge e = gGraph.addEdge(vB, vA, dNewWeight);
					eclLocator.addedEdge(e);
				} else {
					gGraph.setEdgeWeight(weCorrectEdge, dNewWeight);
				}
			}
			catch (final Exception e) {
				// Unknown error. Print trace and continue
				logger.warning(e.getMessage());
			}
		}
	}
}
