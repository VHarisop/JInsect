package gr.demokritos.iit.jinsect.representations;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import gr.demokritos.iit.jinsect.Logging;
import gr.demokritos.iit.jinsect.Utils;
import gr.demokritos.iit.jinsect.io.LineReader;
import gr.demokritos.iit.jinsect.structs.Edge;
import gr.demokritos.iit.jinsect.structs.EdgeCachedLocator;
import gr.demokritos.iit.jinsect.structs.JVertex;
import gr.demokritos.iit.jinsect.structs.NGramVertex;
import gr.demokritos.iit.jinsect.structs.UniqueVertexGraph;

/**
 * Represents the graph of a document, with the n-grams of the document as
 * vertices and the number of the n-grams' co-occurences within a given window
 * as edges.
 *
 * @author PCKid
 */
public class NGramJGraph
implements Serializable, NGramGraph
{
	static final long serialVersionUID = 1L;

	private static final Logger logger =
		Logging.getLogger(NGramJGraph.class.getName());

	/** The minimum and maximum n-gram size, and the cooccurence window.
	 * Default values are 3, 3, 3 correspondingly.
	 */
	protected int MinSize = 3, MaxSize = 3, CorrelationWindow = 3;
	protected String DataString = "";
	protected HashMap<Edge, Double> DegradedEdges;

	/* array of UniqueJVertexGraphs for various N-gram sizes */
	protected UniqueVertexGraph[] NGramGraphArray;
	protected EdgeCachedLocator eclLocator = null;

	/**
	 * The label of the NGramGraph.
	 */
	protected String label = null;

	/**
	 * Creates a new instance of NGramJGraph with default
	 * parameters (MinSize = MaxSize = 3, CorrelationWindow = 3)
	 */
	public NGramJGraph() {
		InitGraphs();
	}

	/**
	 * Creates a new instance of NGramJGraph with default
	 * parameters (MinSize = MaxSize = CorrelationWindow = 3)
	 * with an initial data string.
	 *
	 * @param dataString the data string to represent
	 * @return a new NGramJGraph object with default parameters
	 */
	public NGramJGraph(final String dataString) {
		setDataString(dataString);
	}

	/**
	 * Creates a new instance of NGramJGraph with an initial
	 * data string to be represented and custom parameters.
	 *
	 * @param dataString the data string to represent
	 * @param iMinSize the minimum n-gram size
	 * @param iMaxSize the maximum n-gram size
	 * @param iCorrelationWindow the correlation window length
	 * @return a new NGramJGraph with the given parameters
	 */
	public NGramJGraph(
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
	 * Creates a new instance of NGramJGraph
	 * @param iMinSize The minimum n-gram size
	 * @param iMaxSize The maximum n-gram size
	 * @param iCorrelationWindow The maximum distance of terms to be considered
	 * as correlated.
	 */
	public NGramJGraph(
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
	 * This method is a proxy to {@link #fromFileLines(File)}
	 * @param path the string containing the file path
	 * @return an array of {@link NGramJGraph} objects
	 */
	public static NGramJGraph[] fromFileLines(final String path)
		throws IOException, FileNotFoundException
	{
		return fromFileLines(new File(path));
	}

	/**
	 * Creates an array of NGramJGraph objects, each of which is built
	 * using a line from a given file as a data string.
	 *
	 * @param path the {@link java.io.File} from which to read the lines
	 * @return an array of {@link NGramJGraph} objects
	 */
	public static NGramJGraph[] fromFileLines(final File path)
		throws IOException, FileNotFoundException
	{
		/* read lines and allocate array */
		final String[] lines = new LineReader().getLines(path);
		final NGramJGraph[] nGraphs = new NGramJGraph[lines.length];

		/* build the array of n-gram graphs */
		for (int i = 0; i < lines.length; i++) {
			nGraphs[i] = new NGramJGraph(lines[i]);
		}

		return nGraphs;
	}

	protected void InitGraphs() {
		// Create array of graphs
		NGramGraphArray = new UniqueVertexGraph[MaxSize - MinSize + 1];
		// Init array
		for (int iCnt = MinSize; iCnt <= MaxSize; iCnt++) {
			NGramGraphArray[iCnt - MinSize] = new UniqueVertexGraph();
		}
		// Create degraded edge list
		DegradedEdges = new HashMap<>();
	}

	/**
	 * Returns this NGramGraph's label.
	 *
	 * @return the graph's label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the graph's label.
	 *
	 * @param label the new label of the graph
	 */
	public void setLabel(final String label) {
		this.label = label;
	}

	/**
	 * Measures an indication of the size of a document n-gram graph based on
	 * the edge count of its contained graphs.
	 *
	 * @return The sum of the count of the edges of the various level graphs in
	 * the document n-gram graph.
	 */
	public int length() {
		return Arrays.stream(NGramGraphArray)
			.mapToInt(g -> g.getEdgeCount())
			.sum();
	}

	/**
	 * Checks if the graph is empty by checking if the edge count
	 * of the graph of the minimum ngram size is equal to 0.
	 *
	 * @return true if the graph is empty, else false
	 */
	public boolean isEmpty() {
		return NGramGraphArray[0].getEdgeCount() == 0;
	}

	/** Creates the graph based on a data string loaded from a given file.
	 *@param sFilename The filename of the file containing the data string.
	 */
	public void loadDataStringFromFile(final String sFilename)
	throws IOException, FileNotFoundException
	{
		final String sDataString =
			Utils.loadFileToStringWithNewlines(sFilename);
		setDataString(sDataString); // Actually update
	}

	/**
	 * Returns graph with M-based index
	 * @param iIndex The index of the graph. Zero (0) equals to the graph for
	 * level MinSize n-grams.
	 * @return The {@link UniqueVertexGraph} of the corresponding level.
	 */
	public final UniqueVertexGraph getGraphLevel(final int iIndex) {
		return NGramGraphArray[iIndex];
	}

	/**
	 * Returns graph with n-gram-size-based index
	 * @param iNGramSize The n-gram size of the graph.
	 * @return The {@link UniqueVertexGraph} of the corresponding level.
	 */
	public final UniqueVertexGraph
	getGraphLevelByNGramSize(final int iNGramSize) {
		// Check bounds
		if ((iNGramSize < MinSize) || (iNGramSize > MaxSize)) {
			return null;
		}
		return NGramGraphArray[iNGramSize - MinSize];
	}

	/**
	 * Returns a HashSet with all the edges of the graph.
	 *
	 * @return a hashset containing the edges of the graph
	 */
	public final HashSet<Edge> getAllNodes() {
		final HashSet<Edge> hRes =
			new HashSet<>(length() / (MaxSize - MinSize)); // Init set
		for (int iCurLvl = MinSize; iCurLvl <= MaxSize; iCurLvl++)
		{
			NGramGraphArray[iCurLvl - MinSize].edgeSet()
				.forEach(e -> hRes.add(e));
		}

		return hRes;
	}

	/**
	 * Set a locator to optimize the edge lookup.
	 * @param eNewLocator The locator to use.
	 */
	public void setLocator(final EdgeCachedLocator eNewLocator) {
		eclLocator = eNewLocator;
	}

	/**
	 * Creates an edge in [gGraph] connecting [sBaseNode] to each node in the
	 * [lOtherNodes] list of nodes. If an edge exists, its weight is increased
	 * by [iIncreaseWeight], else its weight is set to [iStartWeight].
	 * @param gGraph The graph to use
	 * @param sStartNode The node from which all edges begin
	 * @param lOtherNodes The list of nodes to which sBaseNode is connected
	 * @param hAppearenceHistogram The histogram of appearences of the terms
	 **/
	private void createEdgesConnecting(final UniqueVertexGraph gGraph,
			final String sStartNode,
			final List<String> lOtherNodes,
			final HashMap<String, Double> hAppearenceHistogram)
	{
		double dStartWeight = 0;
		double dIncreaseWeight = 0;

		final JVertex vProbe = new NGramVertex(sStartNode);

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
					// Probably exists already
					logger.warning(e.getMessage());
				}
				return;
			}
		}

		// Otherwise for every neighbour add edge
		final Iterator<String> iIter = lOtherNodes.iterator();

		// Locate source node
		final JVertex vOldA = gGraph.locateVertex(vProbe);
		JVertex vA;
		if (vOldA != null) {
			vA = vOldA;
		} else {
			// else create it
			vA = vProbe;
			// Add to graph
			try {
				gGraph.add(vA);
			}
			catch (final Exception e) {
				// Not added. Ignore.
			}
		}
		// create a new cached locator
		final EdgeCachedLocator ecl = new EdgeCachedLocator(100);

		// For every edge
		while (iIter.hasNext())
		{
			final JVertex vB = new NGramVertex(iIter.next());
			double dOldWeight = 0;
			double dNewWeight = 0;

			dStartWeight = 1.0;
			dIncreaseWeight = dStartWeight;

			final Edge weCorrectEdge =
				ecl.locateDirectedEdgeInGraph(gGraph, vA, vB);

			if (weCorrectEdge == null) {
				// Not found. Using Start weight
				dNewWeight = dStartWeight;
			} else {
				dOldWeight = weCorrectEdge.edgeWeight();
				dNewWeight = dOldWeight + dIncreaseWeight; // Increase as required
			}

			try
			{
				/* if edge was not present, add to graph */
				if (weCorrectEdge == null) {
					ecl.addedEdge(gGraph.addEdge(vA, vB, dNewWeight));
				}
				else {
					gGraph.setEdgeWeight(weCorrectEdge, dNewWeight);
				}
			}
			catch (final Exception e)
			{
				// Unknown error
				logger.severe(e.getMessage());
			}
		}

	}

	/**
	 * Creates an edge in [gGraph] connecting [sBaseNode] to each node in the
	 * [lOtherNodes] list of nodes. If an edge exists, its weight is increased
	 * by [iIncreaseWeight],
	 * else its weight is set to [iStartWeight]
	 * @param gGraph The graph to use
	 * @param sStartNode The node from which all edges begin
	 * @param lOtherNodes The list of nodes to which sBaseNode is connected
	 * @param dStartWeight The initial weight for first-occuring nodes
	 * @param dNewWeight The new weight
	 * @param dDataImportance The tendency towards the new value. 0.0 means no change
	 * to the current value. 1.0 means the old value is completely replaced by the
	 * new. 0.5 means the final value is the average of the old and the new.
	 **/
	public void createWeightedEdgesConnecting(
			final UniqueVertexGraph gGraph,
			final String sStartNode, final List<String> lOtherNodes,
			final double dStartWeight, final double dNewWeight,
			final double dDataImportance)
	{
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

			}
		}

		// Locate or create source node
		JVertex vA = gGraph.locateVertex(sStartNode);
		if (vA == null) {
			vA = new NGramVertex(sStartNode);
			try {
				gGraph.add(vA);
			}
			catch (final Exception e) {
				// Add failed. Ignore
			}
		}

		EdgeCachedLocator ecl;
		if (eclLocator == null) {
			ecl = new EdgeCachedLocator(100);
		} else {
			ecl = eclLocator;
		}

		// Otherwise for every neighbour add edge
		final Iterator<String> iIter = lOtherNodes.iterator();
		// For every edge
		while (iIter.hasNext())
		{
			final JVertex vB = new NGramVertex(iIter.next());

			double dOldWeight = 0;
			double dFinalWeight = 0;
			Edge weCorrectEdge = null;

			// Get old weight
			Edge weEdge = null;
			// Look for SAME ORIENTATION OF EDGE
			final boolean bFound = (weEdge =
					gGraph.getEdge(vA, vB))
				!= null;
			if (bFound)
			{
				dOldWeight = weEdge.edgeWeight();
				// Found edge should break to avoid redundancy
				weCorrectEdge = weEdge;
				dFinalWeight = dOldWeight + (dNewWeight - dOldWeight)
					* dDataImportance; // Increase as required

				gGraph.setEdgeWeight(weCorrectEdge, dFinalWeight);
			}
			else
			{
				// Not found. New edge.
				dFinalWeight = dStartWeight;
				try {
					gGraph.addEdge(vA, vB, dFinalWeight);
					ecl.resetCache();
				}
				catch (final Exception e) {
					// Insert failed. Ignoring...
					// TODO: Check if it needs to be removed
					logger.warning(e.getMessage());
				}
			}
		}
	}

	/**
	 * Creates the graph of n-grams, for all the levels
	 * specified in the MinSize, MaxSize range.
	 */
	protected void createGraphs() {
		final String sUsableString = DataString;

		final int iLen = DataString.length();
		// Create token histogram.
		final HashMap<String, Double> hTokenAppearance =
			new HashMap<>();

		/* 1st pass. Populate histogram.
		 * For all sizes create corresponding levels
		 */
		for (int iNGramSize = MinSize; iNGramSize <= MaxSize; iNGramSize++)
		{
			// If n-gram bigger than text
			if (iLen < iNGramSize) {
				// then Ignore
				continue;
			}

			for (int iCurStart = 0; iCurStart < iLen; iCurStart++)
			{
				// If reached end
				if (iLen < iCurStart + iNGramSize) {
					// then break
					break;
				}

				// Get n-gram
				final String sCurNGram =
					sUsableString.substring(iCurStart, iCurStart + iNGramSize);
				// Update Histogram
				if (hTokenAppearance.containsKey(sCurNGram)) {
					hTokenAppearance.put(
						sCurNGram,
						(hTokenAppearance.get(sCurNGram)).doubleValue() + 1.0);
				} else {
					hTokenAppearance.put(sCurNGram, 1.0);
				}
			}
		}

		/* 2nd pass. Create graph.
		 * For all sizes create corresponding levels
		 */
		for (int iNGramSize = MinSize; iNGramSize <= MaxSize; iNGramSize++)
		{
			// If n-gram bigger than text, ignore
			if (iLen < iNGramSize) {
				continue;
			}

			final Vector<String> PrecedingNeighbours = new Vector<>();
			final UniqueVertexGraph gGraph = getGraphLevelByNGramSize(iNGramSize);

			for (int iStart = 0; iStart < iLen; iStart++)
			{
				// If reached end, break
				if (iLen < iStart + iNGramSize) {
					break;
				}

				// Get n-gram
				final String sCurNGram =
					sUsableString.substring(iStart, iStart + iNGramSize);
				/* put all preceding neighbours to an array */
				final String[] aFinalNeighbours =
						new String[PrecedingNeighbours.size()];
				PrecedingNeighbours.toArray(aFinalNeighbours);

				/* create edges to connect preceding neighbours to current ngram */
				createEdgesConnecting(
					gGraph,
					sCurNGram,
					Arrays.asList(aFinalNeighbours),
					hTokenAppearance);

				/* add the currently examined N-Gram to the preceding neighbours */
				PrecedingNeighbours.add(sCurNGram);

				/* has preceding neighbours exceeded window length? */
				if (PrecedingNeighbours.size() > CorrelationWindow) {
					// Remove first element
					PrecedingNeighbours.removeElementAt(0);
				}
			}
		}
	}

	/**
	 * Merges the data of [dgOtherGraph] document graph to the data of this
	 * graph by adding all existing edges and moving the values of those
	 * existing in both graphs towards the new graph values based on a tendency
	 * modifier.
	 * The convergence tendency towards the starting value or the new value
	 * is determined by [fWeightPercent].
	 * @param dgOtherGraph The second graph used for the merging
	 * @param fWeightPercent The convergence tendency parameter. A value of 0.0
	 * means no change to existing value, 1.0 means new value is the same as
	 * that of the new graph. A value of 0.5 means new value is exactly between
	 * the old and new value (average).
	 */
	public void mergeGraph(
		final NGramGraph dgOtherGraph, final double fWeightPercent)
	{
		// If both graphs are the same, ignore merging.
		if (dgOtherGraph == this) {
			return;
		}

		for (int iCurLvl = MinSize; iCurLvl <= MaxSize; iCurLvl++) {
			final UniqueVertexGraph gGraph =
				getGraphLevelByNGramSize(iCurLvl);
			final UniqueVertexGraph gOtherGraph =
				dgOtherGraph.getGraphLevelByNGramSize(iCurLvl);

			// Check if other graph has corresponding level
			if (gOtherGraph == null) {
				// If not, ignore level
				continue;
			}

			// For every edge on other graph
			final ArrayList<String> lOtherNodes = new ArrayList<>();
			for (final Edge weCurItem: gOtherGraph.edgeSet())
			{
				final String sHead = weCurItem.getSourceLabel();
				final String sTail = weCurItem.getTargetLabel();
				final double dWeight = weCurItem.edgeWeight();

				lOtherNodes.clear();
				lOtherNodes.add(sTail);

				// TODO: Check this
				createWeightedEdgesConnecting(gGraph, sHead,
						lOtherNodes, dWeight, dWeight, fWeightPercent);
			}
		}
	}

	/**
	 * Computes the intersection of the graph with another graph.
	 *
	 * @param dgOtherGraph the graph to be intersected with
	 * @return the graph resulting from the intersection of the
	 * two graphs
	 */
	public NGramGraph intersectGraph(final NGramGraph dgOtherGraph) {
		// Init res graph
		final NGramJGraph gRes = new NGramJGraph(MinSize, MaxSize, CorrelationWindow);

		// Use cached edge locator
		final EdgeCachedLocator ecl = new EdgeCachedLocator(1000);

		for (int iCurLvl = MinSize; iCurLvl <= MaxSize; iCurLvl++) {
			final UniqueVertexGraph gGraph = getGraphLevelByNGramSize(iCurLvl);
			final UniqueVertexGraph gOtherGraph = dgOtherGraph.getGraphLevelByNGramSize(iCurLvl);
			final UniqueVertexGraph gNewGraph = gRes.getGraphLevelByNGramSize(iCurLvl);

			// Check if other graph has corresponding level
			if (gOtherGraph == null) {
				// If not, ignore level
				continue;
			}

			// For every edge on other graph
			for (final Edge e: gOtherGraph.edgeSet()) {
				final JVertex vHead = gOtherGraph.getEdgeSource(e);
				final JVertex vTail = gOtherGraph.getEdgeTarget(e);
				final double curWeight = e.edgeWeight();

				final Edge eEdge = ecl.locateEdgeInGraph(gGraph, vHead, vTail);
				if (eEdge != null) {
					try {
						final List<String> l = new ArrayList<>();
						l.add(vTail.getLabel());

						final double dTargetWeight =
							0.5 * (curWeight + eEdge.edgeWeight());

						createWeightedEdgesConnecting(gNewGraph,
								vHead.getLabel(), l,
								dTargetWeight, dTargetWeight, 1.0);
					}
					catch (final Exception ex) {
						logger.severe(ex.getMessage());
					}
				}
			}
		}
		return gRes;
	}

	/**
	 * Returns the difference (inverse of the intersection) graph between
	 * the current graph and a given graph.
	 * @param dgOtherGraph The graph to compare to.
	 * @return A NGramJGraph that is the difference between the current graph and the given graph.
	 */
	public NGramGraph inverseIntersectGraph(final NGramGraph dgOtherGraph) {

		// Get the union (merged) graph
		final NGramJGraph dgUnion = (NGramJGraph)clone();
		dgUnion.mergeGraph(dgOtherGraph, 0);

		// Get the intersection graph
		final NGramGraph dgIntersection = intersectGraph(dgOtherGraph);

		// For every level
		for (int iCurLvl = MinSize; iCurLvl <= MaxSize; iCurLvl++) {
			/* get union and intersection graphs for the
			 * current n-gram level */
			final UniqueVertexGraph gUnion =
				dgUnion.getGraphLevelByNGramSize(iCurLvl);
			final UniqueVertexGraph gIntersection =
				dgIntersection.getGraphLevelByNGramSize(iCurLvl);

			// TODO: Order by edge count for optimization
			final EdgeCachedLocator eclLocator = new EdgeCachedLocator(10);

			// Check if other graph has corresponding level
			if (gIntersection == null) {
				// If not, ignore level
				continue;
			}

			// For every edge of intersection
			for (final Edge weCurItem: gIntersection.edgeSet())
			{
				// If the edge is contained in the merged graph
				final Edge eEdge = eclLocator.locateDirectedEdgeInGraph(gUnion,
						gIntersection.getEdgeSource(weCurItem),
						gIntersection.getEdgeTarget(weCurItem));


				if (eEdge != null) {
					try {
						gUnion.removeEdge(eEdge);
					}
					catch (final Exception ex) {
						// Non-lethal exception. Continue.
						logger.info(ex.getMessage());
					}
				}
			}
		}

		return dgUnion;

	}

	/**
	 * Returns both the intersection and the difference (inverse of the
	 * intersection) graph between the current graph and a given graph.
	 * @param dgOtherGraph The graph to use for intersection and difference.
	 * @return An array of two elements. The first is the intersection between
	 * the current graph and the given graph and the second is the difference
	 * of the graphs. The edge distributions are kept from the original graphs.
	 */
	public NGramGraph[] intersectAndDeltaGraph(final NGramGraph dgOtherGraph) {
		NGramGraph dgUnion = null;
		// Initialize union using the biggest graph and get the merged one
		if (dgOtherGraph.length() > length()) {
			dgUnion = dgOtherGraph.clone();
			dgUnion.merge(this, 0);
		}
		else {
			dgUnion = clone();
			dgUnion.merge(dgOtherGraph, 0);
		}

		final NGramGraph[] res = new NGramGraph[2];

		// Get the intersection graph
		final NGramGraph dgIntersection = intersectGraph(dgOtherGraph);
		res[0] = dgIntersection;

		// For every level
		for (int iCurLvl = MinSize; iCurLvl <= MaxSize; iCurLvl++) {
			/* get union and intersection graph for each n-gram level */
			final UniqueVertexGraph gUnion =
				dgUnion.getGraphLevelByNGramSize(iCurLvl);
			final UniqueVertexGraph gIntersection =
				dgIntersection.getGraphLevelByNGramSize(iCurLvl);

			// TODO: Order by edge count for optimization
			final EdgeCachedLocator eclLocator = new EdgeCachedLocator(100);

			// Check if other graph has corresponding level
			if (gIntersection == null) {
				// If not, ignore level
				continue;
			}

			for (final Edge weCurItem: gIntersection.edgeSet()) {

				// If the edge is contained in the merged graph
				final Edge eEdge = eclLocator.locateDirectedEdgeInGraph(gUnion,
						gIntersection.getEdgeSource(weCurItem),
						gIntersection.getEdgeTarget(weCurItem));
				if (eEdge != null) {
					try {
						gUnion.removeEdge(eEdge);
					}
					catch (final Exception ex) {
						// Non-lethal exception. Continue.
						logger.info(ex.getMessage());
					}
				}
			}
		}
		res[1] = dgUnion;
		return res;
	}

	/**
	 * @see NGramGraph#getMinSize getMinsize
	 */
	public final int getMinSize() {
		return MinSize;
	}

	/**
	 * @see NGramGraph#getMaxSize getMaxSize
	 */
	public final int getMaxSize() {
		return MaxSize;
	}

	/**
	 * @see NGramGraph#getWindowSize getWindowSize
	 */
	public final int getWindowSize() {
		return CorrelationWindow;
	}

	/**
	 * Returns a functions of [element graph edges max],[number of neighbours], where
	 * [element graph edges max] refers to the maximum weight of the edges including [sNode],
	 * and [number of neightbours] is its number of neighbours in the graph.
	 *
	 * @param sNode The node object the Coexistence Importance of which we calculate
	 */
	public double calcCoexistenceImportance(final String sNode) {
		return calcCoexistenceImportance(new NGramVertex(sNode));
	}

	/**
	 * Calculates the coexistence importance of a vertex in the graph.
	 *
	 * @param vNode the vertex to calculate coexistence importance for
	 * @return the vertex' coexistence importance
	 */
	public double calcCoexistenceImportance(final JVertex vNode) {
		double dRes = 0.0;

		int iNoOfNeighbours = 0;
		double dMaxEdgeWeight = 0;
		// Search all levels
		for (int iNGramSize=MinSize; iNGramSize <= MaxSize; iNGramSize++) {
			final UniqueVertexGraph gCurLevel =
					getGraphLevelByNGramSize(iNGramSize);
			if (gCurLevel.containsVertex(vNode))
			{
				// Keep max neighbours number
				final Set<Edge> lEdges = gCurLevel.edgesOf(vNode);
				final int iTempNeighbours = lEdges.size();
				iNoOfNeighbours = Math.max(iTempNeighbours, iNoOfNeighbours);

				for (final Edge weEdge: lEdges) {
					final double dWeight = weEdge.edgeWeight();
					dMaxEdgeWeight = Math.max(dWeight, dMaxEdgeWeight);
				}
			}
		}

		// Final calculation
		dRes = -200000.0; // Very low value
		if (dMaxEdgeWeight > 0) {
			if (iNoOfNeighbours > 0) {
				dRes = Math.log10(Math.pow(2 * dMaxEdgeWeight, 2.5) /
					Math.max(1.0, Math.pow(iNoOfNeighbours / 2, 2)));
			} else {
				dRes = Math.log10(Math.pow(2 * dMaxEdgeWeight, 2.5));
			}
		}
		return dRes;
	}

	/**
	 * Prunes vertices from the graph based on a coexistence importance
	 * threshold - vertices with coexistence importance below that threshold
	 * are removed.
	 *
	 * @param minImportance the minimum coexistence importance
	 */
	public void prune(final double minImportance) {
		for (int iNGramSize=MinSize; iNGramSize <= MaxSize; iNGramSize++) {
			final UniqueVertexGraph gCurLevel =
					getGraphLevelByNGramSize(iNGramSize);
			final Vector<JVertex> vToRemove = new Vector<>();

			for (final JVertex vCur: gCurLevel.vertexSet()) {
				if (calcCoexistenceImportance(vCur) < minImportance) {
					vToRemove.add(vCur);
				}
			}
			// Actually remove
			for (final JVertex vCur: vToRemove) {
				try {
					gCurLevel.removeVertex(vCur);
				}
				catch (final Exception e) {
					// nonfatal, continue
					logger.info(e.getMessage());
				}
			}
		}
	}

	/**
	 * Removes an item (node) from all graphs.
	 *
	 * @param sItem The label of the node to remove.
	 */
	public void deleteItem(final String sItem) {
		// From all levels
		for (int iNGramSize = MinSize; iNGramSize <= MaxSize; iNGramSize++) {
			final UniqueVertexGraph gCurLevel =
					getGraphLevelByNGramSize(iNGramSize);
			// Vertex v = Utils.locateVertexInGraph(gCurLevel, sItem);
			final JVertex v = gCurLevel.locateVertex(new NGramVertex(sItem));
			if (v == null) {
				return;
			}
			try {
				gCurLevel.removeVertex(v);
			}
			catch (final Exception e) {
				// Most probable cause: Node did not exist
				logger.warning(e.getMessage());
			}
		}
	}

	/**
	 * Sets all edge weights for all the graphs to 0.
	 */
	public void nullify() {
		// From all levels
		for (int size = MinSize; size <= MaxSize; size++) {
			final UniqueVertexGraph curr = getGraphLevelByNGramSize(size);
			// For all edges, set weight to zero
			for (final Edge e: curr.edgeSet()) {
				curr.setEdgeWeight(e, 0.0);
			}
		}
	}

	/**
	 * Sets the data string for this graph, clears all the graphs
	 * and then creates them anew.
	 * @param sDataString the new data string to be represented
	 */
	public void setDataString(final String sDataString) {
		DataString = sDataString;
		InitGraphs();   // Clear graphs
		createGraphs(); // Update graphs
	}

	/**
	 * Simple getter for the graph's data string.
	 *
	 * @return the data string represented by the graph
	 */
	public final String getDataString() {
		return DataString;
	}

	/**
	 * Serializes the object.
	 */
	private void writeObject(final ObjectOutputStream out)
		throws IOException {

		// Write Fields
		out.writeInt(MinSize);
		out.writeInt(MaxSize);
		out.writeInt(CorrelationWindow);
		out.writeObject(DataString);

		// Save all graphs on all levels
		// For each graph
		for (int iCnt = MinSize; iCnt <= MaxSize; iCnt++) {
			out.writeObject(getGraphLevelByNGramSize(iCnt));
		}
		// Update degradationn
		out.writeObject(DegradedEdges);
	}

	/**
	 * Reads a serialized object.
	 */
	@SuppressWarnings("unchecked")
	private void readObject(final ObjectInputStream in)
	throws IOException, ClassNotFoundException {
		try {
			// Read Fields
			MinSize = in.readInt();
			MaxSize = in.readInt();
			CorrelationWindow = in.readInt();
			DataString = (String)in.readObject();

			// Create array of graphs
			NGramGraphArray = new UniqueVertexGraph[MaxSize - MinSize + 1];
			// For each graph
			for (int iCnt=MinSize; iCnt <= MaxSize; iCnt++) {
				this.NGramGraphArray[iCnt - MinSize] =
					(UniqueVertexGraph)in.readObject();
			}
			// Load degradation
			DegradedEdges = (HashMap<Edge, Double>)in.readObject();

		} catch (final Exception e) {
			throw new IOException(e.getMessage());
		}
	}

	public void degrade(final NGramJGraph dgOtherGraph) {
		for (int iCurLvl = MinSize; iCurLvl <= MaxSize; iCurLvl++) {
			final UniqueVertexGraph gGraph =
				getGraphLevelByNGramSize(iCurLvl);
			final UniqueVertexGraph gOtherGraph =
				dgOtherGraph.getGraphLevelByNGramSize(iCurLvl);
			// Check if other graph has corresponding level
			if (gOtherGraph == null) {
				// If not, ignore level
				continue;
			}

			// For every edge on other graph
			for (final Edge weCurItem: gOtherGraph.edgeSet()) {
				final JVertex vHead = gOtherGraph.getEdgeSource(weCurItem);
				final JVertex vTail = gOtherGraph.getEdgeTarget(weCurItem);
				final Edge eEdge = gGraph.getEdge(vHead, vTail);

				if (eEdge != null) {
					try {
						if (DegradedEdges.containsKey(eEdge)) {
							DegradedEdges.put(eEdge,
								(DegradedEdges.get(eEdge)).doubleValue() + 1);
						} else {
							DegradedEdges.put(eEdge, 1.0);
						}
					} catch (final Exception e)
					{
						// Non fatal error occured. Continue.
						logger.info(e.getMessage());
					}
				}
			}
		}
	}

	/**
	 * Returns the degradation degree of a specified edge.
	 *
	 * @param e the edge whose degradation degree is requested
	 * @return the degradation degree of the edge, or 0 if it
	 * is not degraded
	 */
	public double degradationDegree(final Edge e) {
		if (DegradedEdges.containsKey(e)) {
			return (DegradedEdges.get(e)).doubleValue();
		} else {
			return 0;
		}
	}

	public String toCooccurenceText(final Map<String, String> mCooccurenceMap) {
		final StringBuilder sb = new StringBuilder();
		// For every graph level
		for (int iCnt=MinSize; iCnt <= MaxSize; iCnt++) {
			final UniqueVertexGraph g = getGraphLevelByNGramSize(iCnt);
			// For all edges
			for (final Edge eCur: g.edgeSet()) {
				String sCooccurenceID;
				// If the edge is already in the map
				if (mCooccurenceMap.containsKey(eCur.toString())) {
					// Get its ID
					sCooccurenceID = mCooccurenceMap.get(eCur.toString());
				} else {
					// else create a new ID based on current time and put it in the map.
					sCooccurenceID = String.valueOf(mCooccurenceMap.size() + 1);
					mCooccurenceMap.put(eCur.toString(), sCooccurenceID);
				}

				// Add the ID as many times as the co-occurences
				final int iStop = (int) g.getEdgeWeight(eCur);
				for (int iTimes=0; iTimes < iStop; iTimes++) {
					sb.append(sCooccurenceID + " ");
				}
			}
		}

		return sb.toString();
	}

	@Override
	public NGramGraph clone() {
		final NGramJGraph gRes =
			new NGramJGraph(MinSize, MaxSize, CorrelationWindow);
		gRes.DataString = DataString;
		gRes.DegradedEdges = new HashMap<>(this.DegradedEdges);
		gRes.NGramGraphArray =
			new UniqueVertexGraph[this.NGramGraphArray.length];
		int iCnt=0;
		for (final UniqueVertexGraph uCur : this.NGramGraphArray) {
			gRes.NGramGraphArray[iCnt++] = (UniqueVertexGraph)uCur.clone();
		}

		return gRes;
	}

	/**
	 * See the <i>mergeGraph</i> member for details.
	 * Implements the merge interface.
	 */
	public void merge(final NGramGraph dgOtherObject, final double fWeightPercent) {
		mergeGraph(dgOtherObject, fWeightPercent);
	}

	/**
	 * Returns all edges not existent in another graph.
	 * @param dgOtherGraph The graph to use for intersection and difference.
	 * @return A NGramJGraph containing all edges from this graph
	 * not existing in the other given graph (edge distros are not used).
	 * The edge distributions are kept from this graph.
	 */
	public NGramGraph allNotIn(final NGramGraph dgOtherGraph) {
		// TODO: Order by edge count for optimization
		final EdgeCachedLocator eclLocator = new EdgeCachedLocator(
				Math.max(length(), dgOtherGraph.length()));
		// Clone this graph
		final NGramGraph dgClone = clone();
		for (int iCurLvl = MinSize; iCurLvl <= MaxSize; iCurLvl++) {
			final UniqueVertexGraph gCloneLevel =
				dgClone.getGraphLevelByNGramSize(iCurLvl);
			final UniqueVertexGraph gOtherGraphLevel =
				dgOtherGraph.getGraphLevelByNGramSize(iCurLvl);

			// If this level does not exist in other graph, then keep
			// it and continue.
			if (gOtherGraphLevel == null) {
				continue;
			}

			/*
			 * create a copy of the edge set to avoid
			/* ConcurrentModificationException
			 */
			final Set<Edge> eSet = new HashSet<>(gCloneLevel.edgeSet());

			// For every edge of the cloned graph (using a new list of edges)
			for (final Edge weCurItem: eSet) {
				// Edge weCurItem = eIter.next();
				// If the edge is contained in the merged graph
				final Edge eEdge =
					eclLocator.locateDirectedEdgeInGraph(gOtherGraphLevel,
							gCloneLevel.getEdgeSource(weCurItem),
							gCloneLevel.getEdgeTarget(weCurItem));

				if (eEdge != null) {
					try {
						gCloneLevel.removeEdge(weCurItem);
						eclLocator.resetCache();
					} catch (final Exception ex) {
						// Non-lethal exception. Continue.
						logger.info(ex.getMessage());
					}
				}
			}
		}

		return dgClone;
	}

}

