package gr.demokritos.iit.jinsect;

import gr.demokritos.iit.jinsect.representations.NGramJGraph;

class Main {
	
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		if (args.length < 2) {
			return;
		}
		// graph database and query graph
		try {
			NGramJGraph[] nGraphs = NGramJGraph.fromFileLines(args[0]);
			NGramJGraph nggQuery = new NGramJGraph(args[1]);

			for (int i = 0; i < nGraphs.length; ++i) {
				double similarity = 
					jutils.graphStructuralSimilarity(nGraphs[i].getGraphLevel(0),
													 nggQuery.getGraphLevel(0));

				System.out.printf("%s - Sim: %.3f\n", nGraphs[i].getDataString(),
													 similarity);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
