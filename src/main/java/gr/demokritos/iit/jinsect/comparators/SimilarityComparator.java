package gr.demokritos.iit.jinsect.comparators; 

import java.util.Comparator;
import gr.demokritos.iit.jinsect.structs.*;
import gr.demokritos.iit.jinsect.jutils;

/**
 * A custom similarity comparator to be used for comparing
 * {@link gr.demokritos.iit.jinsect.structs.UniqueJVertexGraph} objects
 * based on their s-similarity.
 *
 * @author VHarisop
 */
public class SimilarityComparator
implements Comparator<UniqueJVertexGraph>
{
	@Override
	public int compare(UniqueJVertexGraph uvgA,
					   UniqueJVertexGraph uvgB)
	{
		/* get s-similarity using jutils */
		double sSim = jutils.graphStructuralSimilarity(uvgA, uvgB);

		return Double.compare(sSim, 0.0);
	}
}
