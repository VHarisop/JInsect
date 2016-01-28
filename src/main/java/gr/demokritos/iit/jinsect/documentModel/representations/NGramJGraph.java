package gr.demokritos.iit.jinsect.documentModel.representations;

import gr.demokritos.iit.jinsect.structs.IMergeable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import gr.demokritos.iit.jinsect.structs.EdgeCachedJLocator;
import gr.demokritos.iit.jinsect.structs.JVertex;
import gr.demokritos.iit.jinsect.structs.NGramVertex;
import gr.demokritos.iit.jinsect.structs.Edge;
import gr.demokritos.iit.jinsect.structs.UniqueJVertexGraph;

import gr.demokritos.iit.jinsect.io.LineReader;

import gr.demokritos.iit.jinsect.utils;
import gr.demokritos.iit.jinsect.jutils;

import org.jgrapht.*;
import org.jgrapht.graph.*;

/** Represents the graph of a document, with vertices n-grams of the document and edges the number
 * of the n-grams' co-occurences within a given window.
 *
 * @author PCKid
 */
public class NGramJGraph implements Serializable, Cloneable, IMergeable<NGramJGraph> {
	/** The minimum and maximum n-gram size, and the cooccurence window.
	 * Default values are 3, 3, 3 correspondingly.
	 */
	protected int MinSize = 3, MaxSize = 3, CorrelationWindow = 3;
	protected String DataString = "";
	protected HashMap DegradedEdges;

	/* array of UniqueJVertexGraphs for various N-gram sizes */
	protected UniqueJVertexGraph[] NGramGraphArray;
	protected EdgeCachedJLocator eclLocator = null;

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
	public NGramJGraph(String dataString) {
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
	public NGramJGraph
	(String dataString, int iMinSize, int iMaxSize, int iCorrelationWindow) 
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
	public NGramJGraph(int iMinSize, int iMaxSize, int iCorrelationWindow) {
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
	public static NGramJGraph[] fromFileLines(String path) 
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
	public static NGramJGraph[] fromFileLines(File path) 
	throws IOException, FileNotFoundException
	{
		/* read lines and allocate array */
		String[] lines = new LineReader().getLines(path);
		NGramJGraph[] nGraphs = new NGramJGraph[lines.length]; 
		
		/* build the array of n-gram graphs */
		for (int i = 0; i < lines.length; i++) {
			nGraphs[i] = new NGramJGraph(lines[i]);
		} 

		return nGraphs;
	}

	protected void InitGraphs() {
		// Create array of graphs
		NGramGraphArray = new UniqueJVertexGraph[MaxSize - MinSize + 1];
		// Init array
		for (int iCnt = MinSize; iCnt <= MaxSize; iCnt++)
			NGramGraphArray[iCnt - MinSize] = new UniqueJVertexGraph();
		// Create degraded edge list
		DegradedEdges = new HashMap();        
	}

	/** 
	 * Measures an indication of the size of a document n-gram graph based on 
	 * the edge count of its contained graphs.
	 * 
	 * @return The sum of the count of the edges of the various level graphs in
	 * the document n-gram graph.
	 */
	public int length() {
		Iterator<UniqueJVertexGraph> iIter = 
			Arrays.asList(NGramGraphArray).iterator();

		int iCnt = 0;
		while (iIter.hasNext())
			iCnt += (iIter.next()).getEdgeCount();
		return iCnt;
	}

	public boolean isEmpty() {
		return NGramGraphArray[0].getEdgeCount() == 0;
	}

	/** Creates the graph based on a data string loaded from a given file.
	 *@param sFilename The filename of the file containing the data string.
	 */
	public void loadDataStringFromFile(String sFilename) throws IOException,
		   FileNotFoundException{        
			   String sDataString = utils.loadFileToStringWithNewlines(sFilename);
			   setDataString(sDataString); // Actually update
		   }

	/**
	 * Returns graph with M-based index
	 * @param iIndex The index of the graph. Zero (0) equals to the graph for 
	 * level MinSize n-grams.
	 * @return The {@link UniqueJVertexGraph} of the corresponding level.
	 */
	public UniqueJVertexGraph getGraphLevel(int iIndex) {
		return NGramGraphArray[iIndex];
	}

	/**
	 * Returns graph with n-gram-size-based index
	 * @param iNGramSize The n-gram size of the graph. 
	 * @return The {@link UniqueJVertexGraph} of the corresponding level.
	 */
	public UniqueJVertexGraph getGraphLevelByNGramSize(int iNGramSize) {
		// Check bounds
		if ((iNGramSize < MinSize) || (iNGramSize > MaxSize))
			return null;

		return NGramGraphArray[iNGramSize - MinSize];
	}

	/**
	 * Returns a HashSet with all the edges of the graph.
	 * 
	 * @return a hashset containing the edges of the graph
	 */
	public HashSet getAllNodes() {
		HashSet hRes = new HashSet(length() / (MaxSize - MinSize)); // Init set
		for (int iCurLvl = MinSize; iCurLvl <= MaxSize; iCurLvl++)
		{
			for (Edge e: NGramGraphArray[iCurLvl - MinSize].edgeSet()) {
				hRes.add(e);
			}
		}

		return hRes;
	}

	/**
	 * Set a locator to optimize the edge lookup.
	 * @param eNewLocator The locator to use.
	 */
	public void setLocator(EdgeCachedJLocator eNewLocator) {
		eclLocator = eNewLocator;
	}

	/***
	 * Creates an edge in [gGraph] connecting [sBaseNode] to each node in the
	 *[lOtherNodes] list of nodes. If an edge exists, its weight is increased by [iIncreaseWeight],
	 *else its weight is set to [iStartWeight]
	 *@param gGraph The graph to use
	 *@param sStartNode The node from which all edges begin
	 *@param lOtherNodes The list of nodes to which sBaseNode is connected
	 *@param hAppearenceHistogram The histogram of appearences of the terms
	 ***/
	public void createEdgesConnecting(UniqueJVertexGraph gGraph, String sStartNode, List lOtherNodes,
			HashMap hAppearenceHistogram) 
	{
		double dStartWeight = 0;
		double dIncreaseWeight = 0;

		JVertex vProbe = new NGramVertex(sStartNode);

		// If no neightbours
		if (lOtherNodes != null)
			if (lOtherNodes.size() == 0)
			{
				// Attempt to add solitary node [sStartNode]
				JVertex v = new NGramVertex(sStartNode);
				try {
					gGraph.add(v);    
				}
				catch (Exception e) {
					// Probably exists already
					e.printStackTrace();
				}
				return;
			}

		// Otherwise for every neighbour add edge
		Iterator iIter = lOtherNodes.iterator();

		// Locate source node
		JVertex vOldA = gGraph.locateVertex(vProbe);
		JVertex vA;
		if (vOldA != null)
			vA = vOldA;
		else {
			// else create it
			vA = vProbe;
			// Add to graph
			try {
				gGraph.add(vA);
			}
			catch (Exception e) {
				// Not added. Ignore.
			}
		}

		// create a new cached locator
		EdgeCachedJLocator ecl = new EdgeCachedJLocator(100);
		

		// For every edge
		while (iIter.hasNext())
		{
			JVertex vB = new NGramVertex((String)iIter.next());
			double dOldWeight = 0;
			double dNewWeight = 0;

			dStartWeight = 1.0;
			dIncreaseWeight = dStartWeight;

			Edge weCorrectEdge = ecl.locateDirectedEdgeInGraph(gGraph, vA, vB);

			if (weCorrectEdge == null)
				// Not found. Using Start weight
				dNewWeight = dStartWeight;
			else {
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
			catch (Exception e)
			{
				// Unknown error
				e.printStackTrace();
			}
		}

	}

	/***
	 * Creates an edge in [gGraph] connecting [sBaseNode] to each node in the
	 *[lOtherNodes] list of nodes. If an edge exists, its weight is increased by [iIncreaseWeight],
	 *else its weight is set to [iStartWeight]
	 *@param gGraph The graph to use
	 *@param sStartNode The node from which all edges begin
	 *@param lOtherNodes The list of nodes to which sBaseNode is connected
	 *@param dStartWeight The initial weight for first-occuring nodes
	 *@param dNewWeight The new weight
	 *@param dDataImportance The tendency towards the new value. 0.0 means no change
	 *to the current value. 1.0 means the old value is completely replaced by the
	 *new. 0.5 means the final value is the average of the old and the new.
	 ***/
	public void createWeightedEdgesConnecting(UniqueJVertexGraph gGraph,
			String sStartNode, List lOtherNodes,
			double dStartWeight, double dNewWeight, double dDataImportance) {

		// If no neightbours
		if (lOtherNodes != null)
			if (lOtherNodes.size() == 0)
			{
				// Attempt to add solitary node [sStartNode]
				NGramVertex v = new NGramVertex(sStartNode);
				try {
					gGraph.add(v);    
				}
				catch (Exception e) {
					// Probably exists already
					e.printStackTrace();
				}

			}

		// Locate or create source node
		JVertex vA = gGraph.locateVertex(sStartNode);
		if (vA == null) {
			vA = new NGramVertex(sStartNode);
			try {
				gGraph.add(vA);
			}
			catch (Exception e) {
				// Add failed. Ignore
			}
		}

		EdgeCachedJLocator ecl;
		if (eclLocator == null)
			ecl = new EdgeCachedJLocator(100);
		else
			ecl = eclLocator;

		// Otherwise for every neighbour add edge
		Iterator iIter = lOtherNodes.iterator();
		// For every edge
		while (iIter.hasNext())
		{
			JVertex vB = new NGramVertex((String)iIter.next());

			double dOldWeight = 0;
			double dFinalWeight = 0;
			Edge weCorrectEdge = null;

			// Get old weight
			Edge weEdge = null;
			// Look for SAME ORIENTATION OF EDGE
			boolean bFound = (weEdge =
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
				catch (Exception e) {
					// Insert failed. Ignoring...
					// TODO: Check if it needs to be removed
					e.printStackTrace();
				}
			}
		}
	}

	/***
	 * Creates the graph of n-grams, for all the levels specified in the MinSize, MaxSize range.
	 ***/

	public void createGraphs() {       
		String sUsableString = new StringBuilder().append(DataString).toString();

		int iLen = DataString.length();
		// Create token histogram.
		HashMap hTokenAppearence = new HashMap();
		
		/* 1st pass. Populate histogram.
		 * For all sizes create corresponding levels
		 */
		for (int iNGramSize = MinSize; iNGramSize <= MaxSize; iNGramSize++)
		{
			// If n-gram bigger than text
			if (iLen < iNGramSize)
				// then Ignore
				continue;

			// The String has a size of at least [iNGramSize]
			String sCurNGram = null;
			for (int iCurStart = 0; iCurStart < iLen; iCurStart++)
			{
				// If reached end
				if (iLen < iCurStart + iNGramSize)
					// then break
					break;

				// Get n-gram                
				sCurNGram = sUsableString.substring(iCurStart, iCurStart + iNGramSize);
				// Update Histogram
				if (hTokenAppearence.containsKey(sCurNGram))
					hTokenAppearence.put(sCurNGram, ((Double)hTokenAppearence.get(sCurNGram)).doubleValue() + 1.0);
				else
					hTokenAppearence.put(sCurNGram, 1.0);

			}
		}

		/* 2nd pass. Create graph.
		 * For all sizes create corresponding levels
		 */
		for (int iNGramSize = MinSize; iNGramSize <= MaxSize; iNGramSize++)
		{
			// If n-gram bigger than text, ignore
			if (iLen < iNGramSize)
				continue;

			Vector<String> PrecedingNeighbours = new Vector<String>();
			UniqueJVertexGraph gGraph = getGraphLevelByNGramSize(iNGramSize);

			// The String has a size of at least [iNGramSize]
			String sCurNGram = "";
			for (int iCurStart = 0; iCurStart < iLen; iCurStart++)
			{
				// If reached end, break
				if (iLen < iCurStart + iNGramSize)
					break;

				// Get n-gram                
				sCurNGram = sUsableString.substring(iCurStart, iCurStart + iNGramSize);
				/* put all preceding neighbours to an array */
				String[] aFinalNeighbours = new String[PrecedingNeighbours.size()];
				PrecedingNeighbours.toArray(aFinalNeighbours);
				
				/* create edges to connect preceding neighbours to current ngram */
				createEdgesConnecting(gGraph, sCurNGram, Arrays.asList(aFinalNeighbours), 
						hTokenAppearence);

				/* add the currently examined N-Gram to the preceding neighbours */
				PrecedingNeighbours.add(sCurNGram);

				/* has preceding neighbours exceeded window length? */
				if (PrecedingNeighbours.size() > CorrelationWindow)
					PrecedingNeighbours.removeElementAt(0);
					// Remove first element
			}
		}        
	}

	/***
	 *Merges the data of [dgOtherGraph] document graph to the data of this graph, 
	 *by adding all existing edges and moving the values of those existing in both graphs
	 *towards the new graph values based on a tendency modifier. 
	 *The convergence tendency towards the starting value or the new value is determined 
	 *by [fWeightPercent]. 
	 *@param dgOtherGraph The second graph used for the merging
	 *@param fWeightPercent The convergence tendency parameter. A value of 0.0 
	 * means no change to existing value, 1.0 means new value is the same as 
	 * that of the new graph. A value of 0.5 means new value is exactly between 
	 * the old and new value (average).
	 ***/
	public void mergeGraph(NGramJGraph dgOtherGraph, double fWeightPercent) {
		// If both graphs are the same, ignore merging.
		if (dgOtherGraph == this)
			return;

		for (int iCurLvl = MinSize; iCurLvl <= MaxSize; iCurLvl++) {
			UniqueJVertexGraph gGraph = getGraphLevelByNGramSize(iCurLvl);
			UniqueJVertexGraph gOtherGraph = dgOtherGraph.getGraphLevelByNGramSize(iCurLvl);
			// Check if other graph has corresponding level
			if (gOtherGraph == null)
				// If not, ignore level
				continue;

			// For every edge on other graph
			ArrayList<String> lOtherNodes = new ArrayList<String>();
			for (Edge weCurItem: gOtherGraph.edgeSet())
			{
				String sHead = weCurItem.getSourceLabel();
				String sTail = weCurItem.getTargetLabel();
				double dWeight = weCurItem.edgeWeight();
				
				lOtherNodes.clear();
				lOtherNodes.add(sTail);
				
				// TODO: Check this
				createWeightedEdgesConnecting(gGraph, sHead,
						lOtherNodes, dWeight, dWeight, fWeightPercent);
			}
		}
	}


	public NGramJGraph intersectGraph(NGramJGraph dgOtherGraph) {
		// Init res graph
		NGramJGraph gRes = new NGramJGraph(MinSize, MaxSize, CorrelationWindow);

		// Use cached edge locator
		EdgeCachedJLocator ecl = new EdgeCachedJLocator(1000);

		for (int iCurLvl = MinSize; iCurLvl <= MaxSize; iCurLvl++) {
			UniqueJVertexGraph gGraph = getGraphLevelByNGramSize(iCurLvl);
			UniqueJVertexGraph gOtherGraph = dgOtherGraph.getGraphLevelByNGramSize(iCurLvl);
			UniqueJVertexGraph gNewGraph = gRes.getGraphLevelByNGramSize(iCurLvl);

			// Check if other graph has corresponding level
			if (gOtherGraph == null)
				// If not, ignore level
				continue;

			// For every edge on other graph
			for (Edge e: gOtherGraph.edgeSet()) {
				JVertex vHead = gOtherGraph.getEdgeSource(e);
				JVertex vTail = gOtherGraph.getEdgeTarget(e);
				double curWeight = e.edgeWeight();

				Edge eEdge = ecl.locateEdgeInGraph(gGraph, vHead, vTail);
				if (eEdge != null) {
					try {
						List l = new ArrayList();
						l.add(vTail.getLabel());

						double dTargetWeight = 0.5 * (curWeight + eEdge.edgeWeight());
						createWeightedEdgesConnecting(gNewGraph, 
										vHead.getLabel(), l, 
										dTargetWeight, dTargetWeight, 1.0);
					}
					catch (Exception ex) {
						ex.printStackTrace();
					} 
				}
			}
		}                
		return gRes;
	}

	/** Returns the difference (inverse of the intersection) graph between the current graph 
	 * and a given graph.
	 *@param dgOtherGraph The graph to compare to.
	 *@return A NGramJGraph that is the difference between the current graph and the given graph.
	 */
	public NGramJGraph inverseIntersectGraph(NGramJGraph dgOtherGraph) {

		// Get the union (merged) graph
		NGramJGraph dgUnion = (NGramJGraph)clone();
		dgUnion.mergeGraph(dgOtherGraph, 0);

		// Get the intersection graph
		NGramJGraph dgIntersection = intersectGraph(dgOtherGraph);

		// For every level
		for (int iCurLvl = MinSize; iCurLvl <= MaxSize; iCurLvl++) {
			UniqueJVertexGraph gUnion = dgUnion.getGraphLevelByNGramSize(iCurLvl);
			UniqueJVertexGraph gIntersection = dgIntersection.getGraphLevelByNGramSize(iCurLvl);
			// TODO: Order by edge count for optimization
			EdgeCachedJLocator eclLocator = new EdgeCachedJLocator(10);

			// Check if other graph has corresponding level
			if (gIntersection == null)
				// If not, ignore level
				continue;            

			// For every edge of intersection
			for (Edge weCurItem: gIntersection.edgeSet())
			{
				// If the edge is contained in the merged graph
				Edge eEdge = eclLocator.locateDirectedEdgeInGraph(gUnion,
									gIntersection.getEdgeSource(weCurItem),
									gIntersection.getEdgeTarget(weCurItem));
									
				
				if (eEdge != null) {
					try {
						gUnion.removeEdge(eEdge);
					} 
					catch (Exception ex) {
						// Non-lethal exception. Continue.
						ex.printStackTrace();
					}
				}
			}
		}

		return dgUnion;

	}

	/** Returns both the intersection and the difference (inverse of the intersection)
	 * graph between the current graph and a given graph.
	 *@param dgOtherGraph The graph to use for intersection and difference.
	 *@return A DocumentNGramDistroGraph array of two elements. The first is the intersection between
	 * the current graph and the given graph and the second is the difference of the graphs.
	 * The edge distributions are kept from the original graphs.
	 */
	public NGramJGraph[] intersectAndDeltaGraph(NGramJGraph dgOtherGraph) {

		NGramJGraph dgUnion = null;
		// Initialize union using the biggest graph
		// and get the union (merged) graph
		if (dgOtherGraph.length() > length()) {
			dgUnion = (NGramJGraph)dgOtherGraph.clone();
			dgUnion.merge(this, 0);
		}
		else {
			dgUnion = (NGramJGraph)clone();
			dgUnion.merge(dgOtherGraph, 0);
		}



		NGramJGraph[] res = new NGramJGraph[2];

		// Get the intersection graph
		NGramJGraph dgIntersection = intersectGraph(dgOtherGraph);
		res[0] = dgIntersection;

		// For every level
		for (int iCurLvl = MinSize; iCurLvl <= MaxSize; iCurLvl++) {
			UniqueJVertexGraph gUnion = dgUnion.getGraphLevelByNGramSize(iCurLvl);
			UniqueJVertexGraph gIntersection =
				dgIntersection.getGraphLevelByNGramSize(iCurLvl);
			// TODO: Order by edge count for optimization
			EdgeCachedJLocator eclLocator = new EdgeCachedJLocator(100);

			// Check if other graph has corresponding level
			if (gIntersection == null)
				// If not, ignore level
				continue;

			for (Edge weCurItem: gIntersection.edgeSet()) {
				
				// If the edge is contained in the merged graph
				Edge eEdge = eclLocator.locateDirectedEdgeInGraph(gUnion,
										gIntersection.getEdgeSource(weCurItem),
										gIntersection.getEdgeTarget(weCurItem));
				if (eEdge != null) {
					try {
						gUnion.removeEdge(eEdge);
					} 
					catch (Exception ex) {
						// Non-lethal exception. Continue.
						ex.printStackTrace();
					}
				}
			}
		}

		res[1] = dgUnion;
		return res;
	}

	public int getMinSize() {
		return MinSize;
	}

	public int getMaxSize() {
		return MaxSize;
	}

	public int getWindowSize() {
		return CorrelationWindow;
	}

	/***
	 * Returns a functions of [element graph edges max],[number of neighbours], where
	 * [element graph edges max] refers to the maximum weight of the edges including [sNode],
	 * and [number of neightbours] is its number of neighbours in the graph.
	 *@param sNode The node object the Coexistence Importance of which we calculate
	 ***/
	public double calcCoexistenceImportance(String sNode) {
		NGramVertex v = new NGramVertex(sNode);

		return calcCoexistenceImportance(v);
	}

	public double calcCoexistenceImportance(JVertex vNode) {
		double dRes = 0.0;

		int iNoOfNeighbours = 0;
		double dMaxEdgeWeight = 0;
		// Search all levels
		for (int iNGramSize=MinSize; iNGramSize <= MaxSize; iNGramSize++) {
			UniqueJVertexGraph gCurLevel = getGraphLevelByNGramSize(iNGramSize);
			if (gCurLevel.containsVertex(vNode))                
			{
				// Keep max neighbours number
				Set<Edge> lEdges = gCurLevel.edgesOf(vNode);
				int iTempNeighbours = lEdges.size();
				iNoOfNeighbours = utils.max(iTempNeighbours, iNoOfNeighbours);
				
				for (Edge weEdge: lEdges) {
					double dWeight = weEdge.edgeWeight();
					dMaxEdgeWeight = utils.max(dWeight, dMaxEdgeWeight);
				}
			}
		}

		// Final calculation
		dRes = -200000.0; // Very low value
		if (dMaxEdgeWeight > 0) {
			if (iNoOfNeighbours > 0)
				dRes = Math.log10(Math.pow(2 * dMaxEdgeWeight, 2.5) / Math.max(1.0, Math.pow(iNoOfNeighbours / 2, 2)));
			else
				dRes = Math.log10(Math.pow(2 * dMaxEdgeWeight, 2.5));                
		}

		return dRes;
	}

	public void prune(double dMinCoexistenceImportance) {
		for (int iNGramSize=MinSize; iNGramSize <= MaxSize; iNGramSize++) {
			UniqueJVertexGraph gCurLevel = getGraphLevelByNGramSize(iNGramSize);
			Vector<JVertex> vToRemove = new Vector<JVertex>();

			for (JVertex vCur: gCurLevel.vertexSet()) {
				if (calcCoexistenceImportance(vCur) < dMinCoexistenceImportance) {
					vToRemove.add(vCur);
				}
			}
			// Actually remove
			for (JVertex vCur: vToRemove) { 
				try {
					gCurLevel.removeVertex(vCur);
				}
				catch (Exception e) {
				// Ignore
				}
			}
		}        
	}

	/***
	 *Removes an item (node) from all graphs.
	 *@param sItem The item to remove.
	 ***/
	public void deleteItem(String sItem) {
		// From all levels
		for (int iNGramSize=MinSize; iNGramSize <= MaxSize; iNGramSize++) {
			UniqueJVertexGraph gCurLevel = getGraphLevelByNGramSize(iNGramSize);
			// Vertex v = utils.locateVertexInGraph(gCurLevel, sItem);
			JVertex v = null;
			v = gCurLevel.locateVertex(v);
			if (v == null)
				return;
			try {
				gCurLevel.removeVertex(v);
			}
			catch (Exception e) {
				e.printStackTrace(); // Probably node did not exist
			}
		}        
	}

	/**
	 * Sets all edge weights for all the graphs to 0.
	 */
	public void nullify() {
		// From all levels
		for (int iNGramSize=MinSize; iNGramSize <= MaxSize; iNGramSize++) {
			UniqueJVertexGraph gCurLevel = getGraphLevelByNGramSize(iNGramSize);
			// For all edges, set weight to zero
			for (Edge e: gCurLevel.edgeSet()) {
				gCurLevel.setEdgeWeight(e, 0.0);
			}
		}                
	}

	/**
	 * Sets the data string for this graph, clears all the graphs
	 * and then creates them anew.
	 * @param sDataString the new data string to be represented
	 */
	public void setDataString(String sDataString) {
		DataString = new StringBuilder().append(sDataString).toString();
		InitGraphs();   // Clear graphs
		createGraphs(); // Update graphs        
	}

	public String getDataString() {
		return DataString;
	}

	// Serialization
	private void writeObject(ObjectOutputStream out)
		throws IOException {
			
			// Write Fields
			out.writeInt(MinSize);
			out.writeInt(MaxSize);
			out.writeInt(CorrelationWindow);
			out.writeObject(DataString);

			// Save all graphs on all levels
			// For each graph
			for (int iCnt=MinSize; iCnt <= MaxSize; iCnt++) {
				out.writeObject(getGraphLevelByNGramSize(iCnt));
			}
			// Update degradationn
			out.writeObject(DegradedEdges);
		}

	private void readObject(ObjectInputStream in)
		throws IOException, ClassNotFoundException {
			try {
				// Read Fields
				MinSize = in.readInt();
				MaxSize = in.readInt();
				CorrelationWindow = in.readInt();
				DataString = (String)in.readObject();

				// Create array of graphs
				NGramGraphArray = new UniqueJVertexGraph[MaxSize - MinSize + 1];
				// For each graph
				for (int iCnt=MinSize; iCnt <= MaxSize; iCnt++) {
					UniqueJVertexGraph g = (UniqueJVertexGraph)in.readObject();
					this.NGramGraphArray[iCnt - MinSize] = 
						(UniqueJVertexGraph)in.readObject();
				}
				// Load degradation
				DegradedEdges = (HashMap)in.readObject();

			} catch (Exception e) {
				throw new IOException(e.getMessage());
			}
		}

	public void degrade(NGramJGraph dgOtherGraph) {
		for (int iCurLvl = MinSize; iCurLvl <= MaxSize; iCurLvl++) {
			UniqueJVertexGraph gGraph = getGraphLevelByNGramSize(iCurLvl);
			UniqueJVertexGraph gOtherGraph = dgOtherGraph.getGraphLevelByNGramSize(iCurLvl);
			// Check if other graph has corresponding level
			if (gOtherGraph == null)
				// If not, ignore level
				continue;

			// For every edge on other graph
			for (Edge weCurItem: gOtherGraph.edgeSet()) {
				JVertex vHead = gOtherGraph.getEdgeSource(weCurItem);
				JVertex vTail = gOtherGraph.getEdgeTarget(weCurItem);
				Edge eEdge = gGraph.getEdge(vHead, vTail);
				
				if (eEdge != null)
					try
					{
						if (DegradedEdges.containsKey(eEdge))
							DegradedEdges.put(eEdge,
									((Double)DegradedEdges.get(eEdge)).doubleValue() + 1);
						else
							DegradedEdges.put(eEdge, (double)1.0);
					}
				catch (Exception e)
				{
					// Non fatal error occured. Continue.
					e.printStackTrace();                        
				}
			}
		}        
	}

	public double degredationDegree(Edge e) {
		if (DegradedEdges.containsKey(e))
			return ((Double)DegradedEdges.get(e)).doubleValue();
		else
			return 0;
	}

	public String toCooccurenceText(Map mCooccurenceMap) {
		StringBuffer sb = new StringBuffer();
		// For every graph level
		for (int iCnt=MinSize; iCnt <= MaxSize; iCnt++) {
			UniqueJVertexGraph g = getGraphLevelByNGramSize(iCnt);
			// For all edges
			for (Edge eCur: g.edgeSet()) {
				String sCooccurenceID;
				// If the edge is already in the map
				if (mCooccurenceMap.containsKey(eCur.toString()))
					// Get its ID
					sCooccurenceID = (String)mCooccurenceMap.get(((Edge)eCur).toString());
				else {
					// else create a new ID based on current time and put it in the map.
					sCooccurenceID = String.valueOf(mCooccurenceMap.size() + 1);
					mCooccurenceMap.put(((Edge)eCur).toString(), sCooccurenceID);
				}

				// Add the ID as many times as the co-occurences
				int iStop = (int) g.getEdgeWeight(eCur);
				for (int iTimes=0; iTimes < iStop; iTimes++) {
					sb.append(sCooccurenceID + " ");
				}
			}
		}

		return sb.toString();
	}

	public static void main(String args[]) {
		NGramJGraph ngs = new NGramJGraph(3,3,2);
		ngs.setDataString("abcdef");
		//ngs.setDataString("This is");

		System.out.println(jutils.graphToDot(ngs.getGraphLevel(0), true));
	}

	@Override
	public Object clone() {
		NGramJGraph gRes = new NGramJGraph(MinSize, MaxSize, CorrelationWindow);
		gRes.DataString = DataString;
		gRes.DegradedEdges = new HashMap(this.DegradedEdges);
		gRes.NGramGraphArray = new UniqueJVertexGraph[this.NGramGraphArray.length];
		int iCnt=0;
		for (UniqueJVertexGraph uCur : this.NGramGraphArray)
			gRes.NGramGraphArray[iCnt++] = (UniqueJVertexGraph)uCur.clone();

		return gRes;
	}

	/** See the <i>mergeGraph</i> member for details. Implements the merge interface. */
	public void merge(NGramJGraph dgOtherObject, double fWeightPercent) {
		mergeGraph(dgOtherObject, fWeightPercent);
	}

	/** Returns all edges not existent in another graph. 
	 *@param dgOtherGraph The graph to use for intersection and difference.
	 *@return A NGramJGraph containing all edges from this graph not existing in the
	 * other given graph (edge distros are not used).
	 * The edge distributions are kept from this graphs.
	 */
	public NGramJGraph allNotIn(NGramJGraph dgOtherGraph) {
		// TODO: Order by edge count for optimization
		EdgeCachedJLocator eclLocator = new EdgeCachedJLocator(Math.max(length(),
					dgOtherGraph.length()));
		// Clone this graph
		NGramJGraph dgClone = (NGramJGraph)clone();
		for (int iCurLvl = MinSize; iCurLvl <= MaxSize; iCurLvl++) {
			UniqueJVertexGraph gCloneLevel = dgClone.getGraphLevelByNGramSize(iCurLvl);
			UniqueJVertexGraph gOtherGraphLevel = dgOtherGraph.getGraphLevelByNGramSize(iCurLvl);
			// If this level does not exist in other graph, then keep it and continue.
			if (gOtherGraphLevel == null)
				continue;

			// For every edge of the cloned graph (using a new list of edges)
			for (Edge weCurItem: gCloneLevel.edgeSet()) {
				// If the edge is contained in the merged graph
				Edge eEdge = 
					eclLocator.locateDirectedEdgeInGraph(gOtherGraphLevel,
							gCloneLevel.getEdgeSource(weCurItem),
							gCloneLevel.getEdgeTarget(weCurItem));
				
				if (eEdge != null)
					try {
						gCloneLevel.removeEdge(weCurItem);
						eclLocator.resetCache();
						// Refresh edge iterator
						// iIter = gCloneLevel.edgeSet().iterator();
					} catch (Exception ex) {
						// Non-lethal exception. Continue.
						ex.printStackTrace();
					}
			}
		}

		// DEBUG LINES
		//System.err.println(String.format("(%s) Cache success: %4.3f", 
		//        this.getClass().getName(), eclLocator.getSuccessRatio()));
		//////////////
		return dgClone;
	}

}

