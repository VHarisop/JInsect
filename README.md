[![Build Status](https://travis-ci.org/VHarisop/JInsect.svg?branch=maven-core)](https://travis-ci.org/VHarisop/JInsect)

# JInsect
The JINSECT toolkit is a Java-based toolkit and library that supports and demonstrates the use of n-gram graphs within Natural Language Processing applications, ranging from summarization and summary evaluation to text classiﬁcation and indexing. The `jinsect-core` subset of JInsect aims to keep the core utilities and classes for a minimal package to be used in applications.

## Main concepts

# Code Snippets
* Create an n-gram graph from a string:

```java

import gr.demokritos.iit.jinsect.representations.*;

...

// The string we want to represent
String sTmp = "Hello graph!";

// The default document n-gram graph with min n-gram size 
// and max n-gram size set to 3, and dist parameter set to 3
NGramGraph dngGraph = new NGramJGraph(sTmp);

```

* Create an n-gram graph from a file 

```java

...

import gr.demokritos.iit.jinsect.representations.*;
import java.io.IOException;

...
        
	// The filename of the file the contents of which will form the graph
	String sFilename = "file.txt";
	NGramJGraph dngGraph = new NGramJGraph(); 
	// Load the data string from the file, also dealing with exceptions
	try {
		dngGraph.loadDataStringFromFile(sFilename);
	} catch (IOException ex) {
		ex.printStackTrace();
	}

```
* Merge two graphs 

```java

import gr.demokritos.iit.jinsect.representations.*;

...

	// create the two graphs
	String sTmpA = "Hello graph A!";
	String sTmpB = "Hello graph B!";
	NGramGraph dngGraphA = new NGramJGraph(sTmpA);
	NGramGraph dngGraphB = new NGramJGraph(sTmpB);

	// perform merging with weight factor 0.5 (averaging)
	// result is on dngGraphA
	dngGraphA.mergeGraph(dngGraphB, 0.5);

```

* Load and save a graph to a file

```java

import gr.demokritos.iit.jinsect.representations.*;
import gr.demokritos.iit.jinsect.storage.INSECTFileDB;

...

		// string to be represented
		String sTmp = "Hello there, I am an example string!";
		NGramJGraph dngGraph = new NGramJGraph();
		INSECTFileDB<NGramJGraph> db = new INSECTFileDB<NGramJGraph>();
		
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
### v1.0
The first version of the JInsect library was born through a strenuous [PhD effort](http://www.iit.demokritos.gr/~ggianna), which means that a lot of small projects were attached to the code. Thus, the 1st version includes:
- The n-gram graphs (NGG) representations. See my [thesis, Chapter 3](http://www.iit.demokritos.gr/~ggianna/thesis.pdf) for more info.
- The NGG operators update/merge, intersect, allNotIn, etc. See my [thesis, Chapter 4](http://www.iit.demokritos.gr/~ggianna/thesis.pdf) for more info.
- The AutoSummENG summary evaluation family of methods.
- INSECTDB storage abstraction for object serialization.
- A very rich (and useful!) utils class which one *must* consult before trying to work with the graphs.
- Tools for the estimation of optimal parameters of n-gram graphs
- Support for [DOT](http://www.graphviz.org/doc/info/lang.html) language representation of NGGs.
...and many many side-projects that are hidden including a chunker based on something similar to a language model, a semantic index that builds upon string subsumption to determine meaning and many others. Most of these are, sadly, not documented or published.

I should stress that v1.0:
* supports efficient multi-threaded execution
* contains examples of application for classification
* contains examples of application for clustering
* contains command-line application for language-neutral summarization

**TODO for V1.0:** 
* Clean problematic classes that have dependencies from Web services.

### v2.0
The second version of n-gram graphs is hoping to be started. The aim is to remove problematic dependencies, due to subprojects and keep the clean, core part of the project. This version is intended to be released as a Maven project, in order to enable easier integration into other projects.

## License
JInsect is under [LGPL license](https://www.gnu.org/licenses/lgpl.html).
