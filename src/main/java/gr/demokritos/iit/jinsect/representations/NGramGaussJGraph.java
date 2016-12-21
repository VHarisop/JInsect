package gr.demokritos.iit.jinsect.representations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import gr.demokritos.iit.jinsect.Logging;
import gr.demokritos.iit.jinsect.structs.Edge;
import gr.demokritos.iit.jinsect.structs.EdgeCachedLocator;
import gr.demokritos.iit.jinsect.structs.JVertex;
import gr.demokritos.iit.jinsect.structs.NGramVertex;
import gr.demokritos.iit.jinsect.structs.UniqueVertexGraph;

/**
 * A Document N-gram UniqueJVertexGraph that uses a Gaussian bell scaling
 * function to determine weights applied to various distances of neighbouring
 * n-grams. Default parameters for n-gram size are MinSize = MaxSize = 3 and
 * the default correlation window has length equal to 3.
 * @author ggianna
 */
public class NGramGaussJGraph extends NGramJGraph {
	static final long serialVersionUID = 1L;
	public EdgeCachedLocator eclLocator = null;

	private static final Logger logger =
			Logging.getLogger(NGramGaussJGraph.class.getName());

	/**
	 * Creates a new instance of DocumentNGramGaussNormJGraph
	 * using the default parameters and an empty initial
	 * data string.
	 *
	 * @return a new NGramGaussJGraph object
	 */
	public NGramGaussJGraph() {
		InitGraphs();
	}

	/**
	 * Creates a new instance of NGramGaussJGraph using the default
	 * parameters representing a given data string.
	 *
	 * @param dataString the data string to be represented
	 * @return a new NGramGaussJGraph object
	 */
	public NGramGaussJGraph(final String dataString) {
		InitGraphs();
		setDataString(dataString);
	}

	/**
	 * Creates a new instance of NGramGaussJGraph with custom parameters
	 * and an empty initial data string.
	 * @param iMinSize The minimum n-gram size
	 * @param iMaxSize The maximum n-gram size
	 * @param iCorrelationWindow The standard deviation of the Gaussian
	 * scaling function to use when determining neighbouring weights.
	 */
	public NGramGaussJGraph(
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
	 * Creates a new NGramGaussJGraph object with custom parameters to
	 * represent a given data string.
	 *
	 * @param dataString the data string to be represented
	 * @param iMinSize the minimum n-gram size
	 * @param iMaxSize the maximum n-gram size
	 * @param iCorrelationWindow the standard deviation of the Gaussian
	 * scaling function to use when determining neighbouring weights.
	 * @return a new NGramGaussJGraph object
	 */
	public NGramGaussJGraph(
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
	 * Creates the graph of n-grams, for all the levels specified in the
	 * MinSize, MaxSize range.
	 * The whole document is taken into account for neighbouring, even
	 * though the distance affects neighbouring importance, by scaling
	 * the neighbouring weight by a Gaussian function of distance.
	 */
	@Override
	protected void createGraphs() {
		final String sUsableString = DataString;

		final int iLen = DataString.length();
		// Create token histogram.
		final HashMap<String, Double> hTokenAppearence =
			new HashMap<>();
		// 1st pass. Populate histogram.
		// For all sizes create corresponding levels
		for (int size = MinSize; size <= MaxSize; size++)
		{
			// If n-gram bigger than text
			if (iLen < size) {
				// then Ignore
				continue;
			}

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

			}
		}

		// 2nd pass. Create graph.
		// For all sizes create corresponding levels
		for (int iNGramSize = MinSize; iNGramSize <= MaxSize; iNGramSize++)
		{
			// If n-gram bigger than text
			if (iLen < iNGramSize) {
				// then Ignore
				continue;
			}

			final List<String> PrecedingNeighbours = new ArrayList<>();
			final UniqueVertexGraph gGraph = getGraphLevelByNGramSize(iNGramSize);

			String sCurNGram = "";
			for (int iCurStart = 0; iCurStart < iLen; iCurStart++)
			{
				// If reached end
				if (iLen < iCurStart + iNGramSize) {
					// then break
					break;
				}

				// Get n-gram
				sCurNGram = sUsableString.substring(
					iCurStart, iCurStart + iNGramSize);
				String[] aFinalNeighbours;
				aFinalNeighbours = new String[PrecedingNeighbours.size()];
				PrecedingNeighbours.toArray(aFinalNeighbours);
				createEdgesConnecting(gGraph,
						sCurNGram,
						Arrays.asList(aFinalNeighbours),
						hTokenAppearence);

				PrecedingNeighbours.add(sCurNGram);
				// Take neighbours into account up to 3 times the stdev
				if (PrecedingNeighbours.size() > CorrelationWindow * 3)
				 {
					PrecedingNeighbours.remove(0); // remove first element
				}
			}
			final int iNeighboursLen = PrecedingNeighbours.size();
			if (iNeighboursLen > 0) {
				createEdgesConnecting(gGraph,
						sCurNGram,
						PrecedingNeighbours,
						hTokenAppearence);
			}
		}
	}

	/**
	 * Creates an edge in [gGraph] connecting [sBaseNode] to each node in the
	 * [lOtherNodes] list of nodes. If an edge exists, its weight is increased
	 * by [iIncreaseWeight], else its weight is set to [iStartWeight].
	 * @param gGraph The graph to use
	 * @param sStartNode The node from which all edges begin
	 * @param lOtherNodes The list of nodes to which sBaseNode is connected
	 * @param hAppearenceHistogram The histogram of appearences of the terms
	 *
	 */
	protected void createEdgesConnecting(
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
				final JVertex v = new NGramVertex(sStartNode);
				try {
					gGraph.add(v);
				}
				catch (final Exception e) {
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
				// Not added. Ignore.
			}

		}

		//////////!!!!!!!!!!!!/////////
		// TODO: MAKE SURE the order of neighbouring vertices corresponds to their distance.
		//////////!!!!!!!!!!!!/////////

		int iCnt=0;
		// For every edge
		while (iIter.hasNext())
		{
			final JVertex vB = new NGramVertex(iIter.next());

			double dOldWeight = 0;
			double dNewWeight = 0;
			dStartWeight = ScalingFunction(++iCnt);
			dIncreaseWeight = dStartWeight;

			if (eclLocator == null) {
				eclLocator = new EdgeCachedLocator(10);
			}
			final Edge weCorrectEdge =
				eclLocator.locateDirectedEdgeInGraph(gGraph, vA, vB);

			if (weCorrectEdge == null) {
				// Not found. Using Start weight
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
				}
				else {
					gGraph.setEdgeWeight(weCorrectEdge, dNewWeight);
				}
			}
			catch (final Exception e)
			{
				/* Unknown error, output as severe */
				logger.severe(e.getMessage());
			}
		}

	}


	/**
	 * A function providing a scaling factor according to
	 * the distance between any two n-grams.
	 *
	 * @param iDistance The distance between the two n-grams.
	 * @return A double scaling factor.
	 */
	protected double ScalingFunction(final int iDistance) {
		return Math.exp(-Math.pow((iDistance), 2.0) /
				(2.0*Math.pow(CorrelationWindow,2.0)));
	}

	@Override
	protected void InitGraphs() {
		super.InitGraphs();
		if (eclLocator != null) {
			eclLocator.resetCache();
		}
	}
}
