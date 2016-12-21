package gr.demokritos.iit.jinsect.comparators;

import gr.demokritos.iit.jinsect.Utils;
import gr.demokritos.iit.jinsect.representations.NGramGraph;
import gr.demokritos.iit.jinsect.structs.Edge;
import gr.demokritos.iit.jinsect.structs.GraphSimilarity;
import gr.demokritos.iit.jinsect.structs.UniqueVertexGraph;

/**
 * A comparator that computes the similarity between two {@link NGramGraph}
 * objects, using size, value and containment similarity.
 * @author ggianna
 *
 * Modified slightly by:
 * @author vharisop
 *
 */
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
	public GraphSimilarity getSimilarityBetween(
		final NGramGraph ngA, final NGramGraph ngB) {
		final GraphSimilarity gSim = new GraphSimilarity();

		/* calculate overall importance first */
		int overallImportance = 0;
		for (int iCnt = ngA.getMinSize(); iCnt <= ngA.getMaxSize(); ++iCnt) {
			overallImportance += Utils.sumFromTo(ngA.getMinSize(), iCnt);
		}

		for (int iCnt = ngA.getMinSize(); iCnt <= ngA.getMaxSize(); ++iCnt) {
			// calculate level weight
			final int levelImportance = Utils.sumFromTo(ngA.getMinSize(), iCnt);

			final GraphSimilarity gSimLevel = new GraphSimilarity();
			UniqueVertexGraph uvgA = ngA.getGraphLevelByNGramSize(iCnt);
			UniqueVertexGraph uvgB = ngB.getGraphLevelByNGramSize(iCnt);

			/* if ngB doesn't have the corresponding level, to next iteration */
			if (uvgB == null) {
				continue;
			}

			final int firstEdges = uvgA.getEdgeCount();
			final int secondEdges = uvgB.getEdgeCount();

			if (firstEdges > secondEdges) {
				/* swap the graphs */
				final UniqueVertexGraph vgSwap = uvgB;
				uvgB = uvgA;
				uvgA = vgSwap;
			}

			final int minEdges =
					(firstEdges >= secondEdges) ? secondEdges : firstEdges;
			final int maxEdges =
					(firstEdges <= secondEdges) ? secondEdges : firstEdges;

			for (final Edge e: uvgA.edgeSet()) {
				final Edge eFound =
					uvgB.getEdge(uvgA.getEdgeSource(e), uvgA.getEdgeTarget(e));

				/* if no such edge exists, go to next iteration */
				if (eFound == null) {
					continue;
				}

				final double degradation = Math.min(
						ngA.degradationDegree(e),
						ngB.degradationDegree(eFound));

				// update containment similarity
				gSimLevel.ContainmentSimilarity +=
					1.0 / (minEdges * Math.max(1.0, degradation));

				final double wA = e.edgeWeight();
				final double wB = eFound.edgeWeight();

				// update value similarity
				gSimLevel.ValueSimilarity +=
					(Math.min(wA, wB) / Math.max(wA, wB)) /
					(maxEdges * Math.max(1.0, ngA.degradationDegree(e) + ngB.degradationDegree(eFound)));
			}
			// update size similarity
			gSimLevel.SizeSimilarity += minEdges / Math.max(maxEdges, 1.0);

			gSim.ValueSimilarity +=
				(gSimLevel.ValueSimilarity * levelImportance) / overallImportance;

			gSim.SizeSimilarity +=
				(gSimLevel.SizeSimilarity * levelImportance) / overallImportance;

			gSim.ContainmentSimilarity += (gSimLevel.ContainmentSimilarity *
					levelImportance) / overallImportance;
		}
		return gSim;
	}
}
