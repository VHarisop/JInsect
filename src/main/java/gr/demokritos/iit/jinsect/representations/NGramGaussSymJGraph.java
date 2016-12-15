package gr.demokritos.iit.jinsect.representations;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import gr.demokritos.iit.jinsect.Logging;
import gr.demokritos.iit.jinsect.structs.Edge;
import gr.demokritos.iit.jinsect.structs.EdgeCachedLocator;
import gr.demokritos.iit.jinsect.structs.JVertex;
import gr.demokritos.iit.jinsect.structs.NGramVertex;
import gr.demokritos.iit.jinsect.structs.UniqueVertexGraph;


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

    private static final Logger logger =
    		Logging.getLogger(NGramGaussSymJGraph.class.getName());

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
        for (int iNGramSize = MinSize; iNGramSize <= MaxSize; iNGramSize++)
        {
            // If n-gram bigger than text
            if (iLen < iNGramSize) {
				// then Ignore
                continue;
			}

            final LinkedList<String> lNGramSequence = new LinkedList<>();
            final UniqueVertexGraph gGraph = getGraphLevelByNGramSize(iNGramSize);
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
                if (hTokenAppearence.containsKey(sCurNGram)) {
					hTokenAppearence.put(
						sCurNGram,
						(hTokenAppearence.get(sCurNGram)).doubleValue() + 1.0);
				} else {
					hTokenAppearence.put(sCurNGram, 1.0);
				}

                // Update list of n-grams
                lNGramSequence.add(sCurNGram);
                // Update graph
                final int iListSize = lNGramSequence.size();
                final int iTo = (iListSize - 1) >= 0 ? iListSize - 1 : 0;
				final int iTemp = iListSize - (int)(CorrelationWindow * Visibility);
                final int iFrom = iTemp - 1 >= 0 ? iTemp - 1 : 0;

				final List<String> revList =
					new LinkedList<>(lNGramSequence.subList(iFrom, iTo));
				Collections.reverse(revList);

                createSymEdgesConnecting(
						gGraph,
						sCurNGram,
                        revList,
						hTokenAppearence);
            }
        }
    }

    /**
     * Creates an edge in [gGraph] connecting [sStartNode] to each node in the
     * [lOtherNodes] list of nodes, as well as other nodes to [sBaseNode].
     * If an edge exists, its weight is increased by [iIncreaseWeight],
     * else its weight is set to [iStartWeight].
     * @param gGraph The graph to use
     * @param sStartNode The node from which all edges begin
     * @param lOtherNodes The list of nodes to which sBaseNode is connected.
     * The list MUST BE ORDERED ASCENDINGLY
     * based on distance from the <code>sStartNode</code>.
     * @param hAppearenceHistogram The histogram of appearences of the terms
     */
    private void createSymEdgesConnecting(
			UniqueVertexGraph gGraph,
			String sStartNode,
			List<String> lOtherNodes,
            HashMap<String, Double> hAppearenceHistogram) {
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
            //dStartWeight = 2.0 / (((Double)hAppearenceHistogram.get(vA.getLabel())).doubleValue() +
                    //((Double)hAppearenceHistogram.get(vB.getLabel())).doubleValue());
            dStartWeight = ScalingFunction(++iCnt);
            dIncreaseWeight = dStartWeight;
            //WeightedEdge weCorrectEdge = (WeightedEdge)jinsect.Utils.locateDirectedEdgeInGraph(gGraph, vA, vB);

            if (eclLocator == null) {
				eclLocator = new EdgeCachedLocator(10);
			}
            // Add one-way edge
            Edge weCorrectEdge = eclLocator.locateDirectedEdgeInGraph(gGraph, vA, vB);

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

            // Add reverse edge
            weCorrectEdge = eclLocator.locateDirectedEdgeInGraph(gGraph, vB, vA);

            if (weCorrectEdge == null) {
				dNewWeight = dStartWeight;
			} else {
                dOldWeight = weCorrectEdge.edgeWeight();
                dNewWeight = dOldWeight + dIncreaseWeight; // Increase as required
            }

            try
            {
                if (weCorrectEdge == null) {
                    final Edge e = gGraph.addEdge(vB, vA, dNewWeight);
                    eclLocator.addedEdge(e);
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
     * Creates an edge in [gGraph] connecting [sBaseNode] to each node in
     * the [lOtherNodes] list of nodes. If an edge exists, its weight is
     * increased by [iIncreaseWeight], else its weight is set to
     * [iStartWeight].
     *
     * @param gGraph the {@link UniqueVertexGraph} to use
     * @param sStartNode the label of the source node
     * @param lOtherNodes a {@link List} of target nodes
     * @param hAppearenceHistogram a {@link HashMap<String, Double>} that
     * represents a histogram of term appearances
     */
    @Override
    protected void createEdgesConnecting(
			UniqueVertexGraph gGraph,
			String sStartNode,
			List<String> lOtherNodes,
            HashMap<String, Double> hAppearenceHistogram) {
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

            dStartWeight = ScalingFunction(Math.abs(++iCnt - (lOtherNodes.size() / 2)));
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
                // Unknown error
            	logger.severe(e.getMessage());
            }
        }

    }
}
