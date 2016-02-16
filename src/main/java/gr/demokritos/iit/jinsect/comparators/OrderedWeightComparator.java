package gr.demokritos.iit.jinsect.comparators;

import gr.demokritos.iit.jinsect.structs.JVertex;
import gr.demokritos.iit.jinsect.structs.Pair;
import gr.demokritos.iit.jinsect.structs.UniqueJVertexGraph;

import java.util.*;

/**
 * This comparator compares {@link UniqueJVertexGraph} objects based on their
 * ordered weight representations. Two pairs of vertex - weight lists, sorted
 * in descending order by the weights, are compared. For every pair of pairs,
 * the label is first compared lexicographically. If the labels are equal, the
 * weights are compared instead.
 *
 * @author VHarisop
 */
public final class OrderedWeightComparator
	implements Comparator<UniqueJVertexGraph> {

	@Override
	public int compare(UniqueJVertexGraph uvgA, UniqueJVertexGraph uvgB) {
		Iterator<Pair<JVertex, Double>> iterA = 
			uvgA.getOrderedWeightPairs().iterator();

		Iterator<Pair<JVertex, Double>> iterB =
			uvgB.getOrderedWeightPairs().iterator();

		do {
			/* handle all cases where either of the iterators has been
			 * depleted. If both are empty, the graphs are equal.
			 */
			if (!(iterA.hasNext() || iterB.hasNext()))
				return 0;
			if (iterA.hasNext() && !iterB.hasNext())
				return -1;
			if (!iterA.hasNext() && iterB.hasNext())
				return 1;

			/* get the two pairs to perform next comparison */
			Pair<JVertex, Double> pA = iterA.next();
			Pair<JVertex, Double> pB = iterB.next();

			String sA = pA.getFirst().getLabel(),
				   sB = pB.getFirst().getLabel();

			/* if labels are equal, compare weights instead.
			 * Otherwise, return the label comparison result.
			 */
			if (sA.equals(sB)) {
				int ret = Double.compare(pA.getSecond(), pB.getSecond());
				if (ret == 0) {
					continue;
				}
				else {
					return ret;
				}
			}
			else {
				return sA.substring(0, 1).compareTo(sB.substring(0, 1));
				/* char cA = sA.charAt(0), cB = sB.charAt(0);
				if (cA < cB) 
					return -1;
				else if (cA == cB)
					return 0;
				else // cA > cB 
					return 1;

				// return sA.compareTo(sB);
				*/
			}
		} while (true);
	}
}
