package gr.demokritos.iit.jinsect.comparators;

import gr.demokritos.iit.jinsect.documentModel.representations.NGramJGraph;
import gr.demokritos.iit.jinsect.structs.*;
import gr.demokritos.iit.jinsect.jutils;
import gr.demokritos.iit.jinsect.utils;

public class NGramGraphComparator {
	
	/** 
	 * Creates a new, empty instance of NGramGraphComparator 
	 */
	public NGramGraphComparator() {}

	/**
	 * Returns the similarity between two NGramJGraph objects.
	 *
	 * @param ngA the first graph
	 * @param ngB the second graph
	 * @return a {@link GraphSimilarity} object, containing all similarity
	 * metrics between the two graphs
	 */
	public GraphSimilarity getSimilarityBetween(NGramJGraph ngA, NGramJGraph ngB) {
		GraphSimilarity gSim = new GraphSimilarity();

		/* calculate overall importance first */
		int overallImportance = 0;
		for (int iCnt = ngA.getMinSize(); iCnt <= ngA.getMaxSize(); ++iCnt) {
			overallImportance += utils.sumFromTo(ngA.getMinSize(), iCnt);
		}

		for (int iCnt = ngA.getMinSize(); iCnt <= ngA.getMaxSize(); ++iCnt) {
			// calculate level weight
			int levelImportance = utils.sumFromTo(ngA.getMinSize(), iCnt);

			GraphSimilarity gSimLevel = new GraphSimilarity();
			UniqueJVertexGraph uvgA = ngA.getGraphLevelByNGramSize(iCnt);
			UniqueJVertexGraph uvgB = ngB.getGraphLevelByNGramSize(iCnt);

			/* if ngB doesn't have the corresponding level, to next iteration */
			if (uvgB == null) {
				continue;
			}
			
			int firstEdges = uvgA.getEdgeCount();
			int secondEdges = uvgB.getEdgeCount();

			if (firstEdges > secondEdges) {
				/* swap the graphs */
				UniqueJVertexGraph vgSwap = uvgB;
				uvgB = uvgA;
				uvgA = vgSwap;
			}

			int minEdges = (firstEdges >= secondEdges) ? secondEdges : firstEdges;
			int maxEdges = (firstEdges <= secondEdges) ? secondEdges : firstEdges;

			for (Edge e: uvgA.edgeSet()) {
				Edge eFound = 
					uvgB.getEdge(uvgA.getEdgeSource(e), uvgA.getEdgeTarget(e));

				/* if no such edge exists, go to next iteration */
				if (eFound == null) {
					continue;
				}

				double degradation = utils.min(
							ngA.degredationDegree(e), 
							ngB.degredationDegree(eFound));

				// update containment similarity
				gSimLevel.ContainmentSimilarity += 
					1.0 / (minEdges * utils.max(1.0, degradation));

				double wA = e.edgeWeight();
				double wB = eFound.edgeWeight();

				// update value similarity
				gSimLevel.ValueSimilarity += 
					(utils.min(wA, wB) / utils.max(wA, wB)) /
					(maxEdges * utils.max(1.0 , ngA.degredationDegree(e) + 
										  ngB.degredationDegree(eFound)));
			}
			// update size similarity 
			gSimLevel.SizeSimilarity += minEdges / utils.max(maxEdges, 1.0);

			// update gSim
			double impRatio = levelImportance / overallImportance;

			gSim.ValueSimilarity += 
				gSimLevel.ValueSimilarity * impRatio;

			gSim.SizeSimilarity += 
				gSimLevel.SizeSimilarity * impRatio;

			gSim.ContainmentSimilarity += 
				gSimLevel.ContainmentSimilarity * impRatio;

			gSim.StructuralSimilarity += 
				jutils.graphStructuralSimilarity(uvgA, uvgB) * impRatio;
		}

		return gSim;
	}
}
