package gr.demokritos.iit.jinsect.representations;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import gr.demokritos.iit.jinsect.structs.*;


/**
 * Implementation of a class that extends {@link NGramGaussJGraph} 
 * with a symmetric window function.
 * @author ggianna 
 */
public class NGramGaussSymJGraph extends NGramGaussJGraph {

	static final long serialVersionUID = 1L;

	/**
	 * The width of the actual window, as a factor over the given window
	 */
    static final double Visibility = 3.0;

    /** 
	 * Creates a new instance of NGramGaussSymJGraph 
	 * using the default parameters.
	 *
	 * @return a new NGramGaussSymJGraph object
	 */
    public NGramGaussSymJGraph() {
        InitGraphs();
    }

	/**
	 * Creates a new NGramGaussSymJGraph object using the default 
	 * parameters to represent a given data string.
	 *
	 * @param dataString the string to be represented 
	 * @return a new NGramGaussSymJGraph object
	 */
	public NGramGaussSymJGraph(String dataString) {
		setDataString(dataString);
	}
    
    /**
     * Creates a new instance of NGramGaussSymJGraph using custom parameters
	 *
     * @param iMinSize The minimum n-gram size
     * @param iMaxSize The maximum n-gram size
     * @param iCorrelationWindow The standard deviation of the Gaussian 
	 * scaling function to use when determining neighbouring weights.
     */
    public NGramGaussSymJGraph(int iMinSize, int iMaxSize, int iCorrelationWindow) {
        MinSize = iMinSize;
        MaxSize = iMaxSize;
        CorrelationWindow = iCorrelationWindow;
        
        InitGraphs();
    }

	/**
	 * Creates a new NGramGaussSymJGraph object using custom parameters to
	 * represent a given data string.
	 * 
	 * @param dataString the string to be represented
	 * @param iMinSize the minimum n-gram size
	 * @param iMaxSize the maximum n-gram size
	 * @param iCorrelationWindow the standard deviation of the Gaussian
	 * scaling function to use when determining neighbouring weights.
	 * @return a new NGramGaussSymJGraph object
	 */
	public NGramGaussSymJGraph
	(String dataString, int iMinSize, int iMaxSize, int iCorrelationWindow) {
		MinSize = iMinSize;
		MaxSize = iMaxSize;
		CorrelationWindow = iCorrelationWindow;
		setDataString(dataString);
	}
    
    public void createGraphs() {       
        String sUsableString = new StringBuilder().append(DataString).toString();
        
        int iLen = DataString.length();
        // Create token histogram.
        HashMap<String, Double> hTokenAppearence =
			new HashMap<String, Double>();
        // 1st pass. Populate histogram.
        ///////////////////////////////
        // For all sizes create corresponding levels
        for (int iNGramSize = MinSize; iNGramSize <= MaxSize; iNGramSize++)
        {
            // If n-gram bigger than text
            if (iLen < iNGramSize)
                // then Ignore
                continue;
            
            // The String has a size of at least [iNGramSize]
            String sCurNGram = null;
            LinkedList<String> lNGramSequence = new LinkedList<String>();
            UniqueJVertexGraph gGraph = getGraphLevelByNGramSize(iNGramSize);
            
            for (int iCurStart = 0; iCurStart < iLen; iCurStart++)
            {
                // If reached end
                if (iLen < iCurStart + iNGramSize)
                    // then break
                    break;
                
                // Get n-gram                
                sCurNGram = 
					sUsableString.substring(iCurStart, iCurStart + iNGramSize);
                
                // Update Histogram
                if (hTokenAppearence.containsKey(sCurNGram))
                    hTokenAppearence.put(
						sCurNGram,
						(hTokenAppearence.get(sCurNGram)).doubleValue() + 1.0);
                else
                    hTokenAppearence.put(sCurNGram, 1.0);
                
                // Update list of n-grams
                lNGramSequence.add(sCurNGram);
                // Update graph
                int iListSize = lNGramSequence.size();
                int iTo = (iListSize - 1) >= 0 ? iListSize - 1 : 0;
				int iTemp = iListSize - (int)(CorrelationWindow * Visibility);
                int iFrom = iTemp - 1 >= 0 ? iTemp - 1 : 0;

				List<String> revList = 
					new LinkedList<String>(lNGramSequence.subList(iFrom, iTo));
				Collections.reverse(revList);

                createSymEdgesConnecting(
						gGraph,
						sCurNGram,
                        revList,
						hTokenAppearence);
            }
        }
    }
    
    /***
     * Creates an edge in [gGraph] connecting [sStartNode] to each node in the
     *[lOtherNodes] list of nodes, as well as other nodes to [sBaseNode]. 
     *If an edge exists, its weight is increased by [iIncreaseWeight],
     *else its weight is set to [iStartWeight]
     *@param gGraph The graph to use
     *@param sStartNode The node from which all edges begin
     *@param lOtherNodes The list of nodes to which sBaseNode is connected. The list MUST BE ORDERED ASCENDINGLY 
     * based on distance from the <code>sStartNode</code>.
     *@param hAppearenceHistogram The histogram of appearences of the terms
    ***/
    public void createSymEdgesConnecting(
			UniqueJVertexGraph gGraph,
			String sStartNode,
			List<String> lOtherNodes,
            HashMap<String, Double> hAppearenceHistogram) {
        double dStartWeight = 0;
        double dIncreaseWeight = 0;
        
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
                    e.printStackTrace(System.err);
                }
                return;
            }
        
        // Otherwise for every neighbour add edge
        Iterator<String> iIter = lOtherNodes.iterator();
        
        // Locate source node
		JVertex vOldA = gGraph.locateVertex(sStartNode);
        JVertex vA;
        if (vOldA != null)
            vA = vOldA;
        else {
            // else create it
			vA = new NGramVertex(sStartNode);
            // Add to graph
            try {
                gGraph.add(vA);
            }
            catch (Exception e) {
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
            JVertex vB = new NGramVertex((String)iIter.next());
            
            double dOldWeight = 0;
            double dNewWeight = 0;
            //dStartWeight = 2.0 / (((Double)hAppearenceHistogram.get(vA.getLabel())).doubleValue() +
                    //((Double)hAppearenceHistogram.get(vB.getLabel())).doubleValue());
            dStartWeight = ScalingFunction(++iCnt);
            dIncreaseWeight = dStartWeight;
            //WeightedEdge weCorrectEdge = (WeightedEdge)jinsect.utils.locateDirectedEdgeInGraph(gGraph, vA, vB);
            
            if (eclLocator == null)
                eclLocator = new EdgeCachedJLocator(10);
            // Add one-way edge
            Edge weCorrectEdge = eclLocator.locateDirectedEdgeInGraph(gGraph, vA, vB);
            
            if (weCorrectEdge == null)
                // Not found. Using Start weight
                dNewWeight = dStartWeight;
            else {
                dOldWeight = weCorrectEdge.edgeWeight();
                dNewWeight = dOldWeight + dIncreaseWeight; // Increase as required
            }
            
            try
            {
                if (weCorrectEdge == null) {
                    Edge e = gGraph.addEdge(vA, vB, dNewWeight);
                    eclLocator.addedEdge(e);
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
            
            // Add reverse edge
            weCorrectEdge = eclLocator.locateDirectedEdgeInGraph(gGraph, vB, vA);
            
            if (weCorrectEdge == null)
                // Not found. Using Start weight
                dNewWeight = dStartWeight;
            else {
                dOldWeight = weCorrectEdge.edgeWeight();
                dNewWeight = dOldWeight + dIncreaseWeight; // Increase as required
            }
            
            try
            {
                if (weCorrectEdge == null) {
                    Edge e = gGraph.addEdge(vB, vA, dNewWeight);
                    eclLocator.addedEdge(e);
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
     *@param hAppearenceHistogram The histogram of appearences of the terms
    ***/
    public void createEdgesConnecting(
			UniqueJVertexGraph gGraph,
			String sStartNode, 
			List<String> lOtherNodes,
            HashMap<String, Double> hAppearenceHistogram) {
        double dStartWeight = 0;
        double dIncreaseWeight = 0;
        
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
        Iterator<String> iIter = lOtherNodes.iterator();
        
        // Locate source node
        JVertex vOldA = gGraph.locateVertex(sStartNode);
        JVertex vA;
        if (vOldA != null)
            vA = vOldA;
        else {
            // else create it
            vA = new NGramVertex(sStartNode);
            // Add to graph
            try {
                gGraph.add(vA);
            }
            catch (Exception e) {
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
			JVertex vB = new NGramVertex(iIter.next());
            
            double dOldWeight = 0;
            double dNewWeight = 0;

            dStartWeight = ScalingFunction(Math.abs(++iCnt - (lOtherNodes.size() / 2)));
            dIncreaseWeight = dStartWeight;
            
			if (eclLocator == null)
                eclLocator = new EdgeCachedJLocator(10);
            Edge weCorrectEdge = 
				eclLocator.locateDirectedEdgeInGraph(gGraph, vA, vB);
            
            if (weCorrectEdge == null)
                // Not found. Using Start weight
                dNewWeight = dStartWeight;
            else {
                dOldWeight = weCorrectEdge.edgeWeight();
                dNewWeight = dOldWeight + dIncreaseWeight; // Increase as required
            }
            
            try
            {
                if (weCorrectEdge == null) {
                    Edge e = gGraph.addEdge(vA, vB, dNewWeight);
                    eclLocator.addedEdge(e);
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
}
