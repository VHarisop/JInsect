# JInsect
The JINSECT toolkit is a Java-based toolkit and library that supports and demonstrates the use of n-gram graphs within Natural Language Processing applications, ranging from summarization and summary evaluation to text classiﬁcation and indexing.

## Main concepts

# Code Snippets
* Create an n-gram graph from a string:

```java

import gr.demokritos.iit.jinsect.documentModel.representations.DocumentNGramGraph;

...

// The string we want to represent
String sTmp = "Hello graph!";

// The default document n-gram graph with min n-gram size 
// and max n-gram size set to 3, and dist parameter set to 3
DocumentNGramGraph dngGraph = new DocumentNGramGraph();

// Create the graph
dngGraph.setDataString(sTmp);

```

* Create an n-gram graph from a file 

```java

...

import gr.demokritos.iit.jinsect.documentModel.comparators.NGramCachedGraphComparator;
import gr.demokritos.iit.jinsect.documentModel.representations.DocumentNGramGraph;
import gr.demokritos.iit.jinsect.structs.GraphSimilarity;
import gr.demokritos.iit.jinsect.utils;
import java.io.IOException;

...
        
	// The filename of the file the contents of which will form the graph
	String sFilename = "file.txt";
	DocumentNGramGraph dngGraph = new DocumentNGramGraph(); 
	// Load the data string from the file, also dealing with exceptions
	try {
		dngGraph.loadDataStringFromFile(sFilename);
	} catch (IOException ex) {
		ex.printStackTrace();
	}

```

* Compare two graphs, extracting their similarity 

```java

...

import gr.demokritos.iit.jinsect.documentModel.comparators.NGramCachedGraphComparator;
import gr.demokritos.iit.jinsect.documentModel.representations.DocumentNGramGraph;
import gr.demokritos.iit.jinsect.structs.GraphSimilarity;
import gr.demokritos.iit.jinsect.utils;
import java.io.IOException;

...

    String sTmp = "Hello graph!";
    DocumentNGramGraph dngGraph = new DocumentNGramGraph(); 
    dngGraph.setDataString(sTmp);
    String sTmp2 = "Hello other graph!";
    DocumentNGramGraph dngGraph2 = new DocumentNGramGraph(); 
    dngGraph2.setDataString(sTmp2);

    // Create a comparator object
    NGramCachedGraphComparator ngc = new NGramCachedGraphComparator();
    // Extract similarity
    GraphSimilarity gs = ngc.getSimilarityBetween(dngGraph, dngGraph2);
    // Output similarity (all three components: containment, value and size)
	System.out.println(gs.toString());

```

* Merge two graphs 

```java

import gr.demokritos.iit.jinsect.documentModel.representations.DocumentNGramGraph;

...

	// create the two graphs
	String sTmpA = "Hello graph A!";
	String sTmpB = "Hello graph B!";
	DocumentNGramGraph dngGraphA = new DocumentNGramGraph();
	DocumentNGramGraph dngGraphB = new DocumentNGramGraph();
	dngGraphA.setDataString(sTmpA);
	dngGraphB.setDataString(sTmpB);

	// perform merging with weight factor 0.5 (averaging)
	// result is on dngGraphA
	dngGraphA.mergeGraph(dngGraphB, 0.5);

```

* Load and save a graph to a file

```java

import gr.demokritos.iit.jinsect.documentModel.representations.DocumentNGramGraph;
import gr.demokritos.iit.jinsect.storage.INSECTFileDB;

...

		// string to be represented
		String sTmp = "Hello there, I am an example string!";
		DocumentNGramGraph dngGraph = new DocumentNGramGraph();
		INSECTFileDB<DocumentNGramGraph> db = new INSECTFileDB<DocumentNGramGraph>();
		
		// if the file already exists
		if (db.existsObject("test", "graph")) { 
			dngGraph = db.loadObject("test", "graph");
		}
		else {
			// Create the graph
			dngGraph.setDataString(sTmp);

			// save object to file
			db.saveObject(dngGraph, "test", "graph");
		}

```



## Versions
V1.0
The first version of the JInsect library was born through a strenuous [PhD effort](http://www.iit.demokritos.gr/~ggianna), which means that a lot of small projects were attached to the code. Thus, the 1st version includes:
- The n-gram graphs (NGG) representations. See my [thesis, Chapter 3](http://www.iit.demokritos.gr/~ggianna/thesis.pdf) for more info.
- The NGG operators update/merge, intersect, allNotIn, etc. See my [thesis, Chapter 4](http://www.iit.demokritos.gr/~ggianna/thesis.pdf) for more info.
- The AutoSummENG summary evaluation family of methods.
- INSECTDB storage abstraction for object serialization.
- A very rich (and useful!) utils class which one *must* consult before trying to work with the graphs.
- Tools for the estimation of optimal parameters of n-gram graphs
- Support for [DOT](http://www.graphviz.org/doc/info/lang.html) language representation of NGGs.
...and many many side-projects that are hidden including a chunker based on something similar to a language model, a semantic index that builds upon string subsumption to determine meaning and many others. Most of these are, sadly, not documented or published.

I should stress that V1.0:
* supports efficient multi-threaded execution
* contains examples of application for classification
* contains examples of application for clustering
* contains command-line application for language-neutral summarization

**TODO for V1.0:** 
* Clean problematic classes that have dependencies from Web services.

V2.0
The second version of n-gram graphs is hoping to be started. The aim is to remove problematic dependencies, due to subprojects and keep the clean, core part of the project. I am also aiming to convert it into a maven project to improve integration into current solutions.
## License
JInsect is under [LGPL license](https://www.gnu.org/licenses/lgpl.html).
