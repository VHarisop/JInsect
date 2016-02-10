package gr.demokritos.iit.jinsect.structs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;


/**
 * Utility class for performing edge retrieval using caching.
 * Three separate caches are maintained:
 *    - <tt>Cache</tt> is a string - to - map cache that maps
 *      vertex labels to their edge mappings
 *    - <tt>CacheAccess</tt> keeps a time - to - string mapping
 *      that maps access times to vertex labels
 *    - <tt>ElementAccessTime</tt> is a mapping from string to
 *      time that maps vertex labels to access times.
 *
 * Slightly modified by:
 * @author VHarisop
 *
 * Originally by:
 * @author ggianna
 */
public class EdgeCachedJLocator {
	protected int CacheMaxSize;

	/**
	 * A string - to - map cache that maps vertex labels to
	 * their edge mappings.
	 */
	protected HashMap<String, TreeMap<String, Edge>> Cache;

	/**
	 * A time - to - string mapping that maps access time to vertex labels.
	 */
	protected TreeMap<Long, String> CacheAccess;

	/**
	 * A string - to - time mapping that maps vertex labels to access times.
	 */
	protected HashMap<String, Long> ElementAccessTime;

	protected long TimeCnt = Long.MIN_VALUE;
	protected long lHits = 0, lMisses = 0;

	/** 
	 * Creates a new instance of EdgeCachedLocator, concerning a specific graph.
	 * @param iCacheMaxSize The maximum number of edges to cache.
	 */
	public EdgeCachedJLocator(int iCacheMaxSize) {
		CacheMaxSize = iCacheMaxSize;

		Cache = new HashMap<String, TreeMap<String, Edge>>();
		CacheAccess = new TreeMap<Long, String>();
		ElementAccessTime = new HashMap<String, Long>();
	}

	/** 
	 * Looks up a given directed edge in a selected graph. 
	 * The edge is described based on the label of its
	 * vertices.
	 * @param gGraph The graph to use.
	 * @param vHead A vertex with the desired label for the head of the edge.
	 * @param vTail A vertex with the desired label for the tail of the edge.
	 * @return The edge, if found, otherwise null.
	 */
	public final Edge locateDirectedEdgeInGraph(
			UniqueJVertexGraph gGraph,
			JVertex vHead,
			JVertex vTail) 
	{
		Edge eRes = null;

		/* get the vertices */
		vHead = gGraph.locateVertex(vHead);
		vTail = gGraph.locateVertex(vTail);
		if ((vHead == null) || (vTail == null))
			return null;

		/* get their labels */
		String sHead = vHead.getLabel();
		String sTail = vTail.getLabel();

		// Check cache for source vertex
		TreeMap<String, Edge> hOutVertices = Cache.get(sHead);
		Set<Edge> sEdges;

		if (hOutVertices == null) { // if not found
			lMisses++;

			// get all outgoing edges from source vertex
			sEdges = gGraph.outgoingEdgesOf(vHead); 

			// Check if time has reached max value
			if (TimeCnt == Long.MAX_VALUE) {
				// if so, cache must be reset
				resetCache();
			}

			hOutVertices = new TreeMap<String, Edge>();
			// cache all outgoing edges and vertices
			for (Edge e : sEdges) {
				hOutVertices.put(e.getTargetLabel(), e);
				if (gGraph.getEdgeTarget(e).getLabel().equals(sTail)) 
					eRes = e;
			}

			// Update cache and access time
			Cache.put(sHead, hOutVertices);
			ElementAccessTime.put(sHead, ++TimeCnt);
		}

		else {
			lHits++;
			ElementAccessTime.put(vHead.getLabel(), ++TimeCnt);
		}

		// Update Access time
		CacheAccess.put(TimeCnt, sHead);

		/* Remove oldest element to keep size in limits. 
		 * This if block can only be triggered if some new elements were
		 * added, in which case there is no need to have another call
		 * at hOutVertices.get(sTail). 
		 */
		if (Cache.size() > CacheMaxSize) {
			// Keep doing the following
			while (true) {
				// Check if the oldest element has been reused
				String sVertexLabel = CacheAccess.get(CacheAccess.firstKey());
				if (ElementAccessTime.get(sVertexLabel) > CacheAccess.firstKey())
				{
					// If it has, remove the older time reference
					CacheAccess.remove(CacheAccess.firstKey());
				}
				else
				{
					// else remove the object from cache
					Cache.remove(sVertexLabel);
					CacheAccess.remove(CacheAccess.firstKey());
					ElementAccessTime.remove(sVertexLabel);

					// stop iterating
					break;
				}
			}
		}
		else // If already cached
		{
			return hOutVertices.get(sTail);
		}

		return eRes;
	}

	/** 
	 * Looks up a vertex in a given graph.
	 * @param gGraph The graph to use.
	 * @param vToFind The vertex to locate.
	 * @return The vertex, if found, otherwise null.
	 */
	public final JVertex locateVertexInGraph(UniqueJVertexGraph gGraph, JVertex vToFind) {
		return gGraph.locateVertex(vToFind);
	}

	/** 
	 * Looks up a given (undirected) edge in a selected graph. 
	 * The edge is described based on the label of its
	 * vertices.
	 * @param gGraph The graph to use.
	 * @param vHead A vertex with the desired label for the head or tail of the edge.
	 * @param vTail A vertex with the desired label for the tail or tail of the edge.
	 * @return The edge, if found, otherwise null.
	 */
	public final Edge locateEdgeInGraph(UniqueJVertexGraph gGraph, JVertex vHead, JVertex vTail) {
		Edge eRes = locateDirectedEdgeInGraph(gGraph, vHead, vTail);
		return eRes == null ? locateDirectedEdgeInGraph(gGraph, vTail, vHead) : eRes;
	}

	/** 
	 * Gets the outgoing edges of a given vertex in a directed graph.
	 * @param gGraph The graph to use.
	 * @param vHead A vertex with the desired label for the head of the edge.
	 * @return A list of outgoing edges from <code>vHead</code>. If no such edges exist returns an
	 * empty list.
	 */
	public final List<Edge> getOutgoingEdges(UniqueJVertexGraph gGraph, JVertex vHead) {
		JVertex vNode = gGraph.locateVertex(vHead.getLabel());
		ArrayList<Edge> lRes = new ArrayList<Edge>();
		if (vNode != null) {

			List<JVertex> neighbours = gGraph.getAdjacentVertices(vNode);
			for (JVertex v: neighbours) {
				// Add only child neighbours
				Edge eCur = locateDirectedEdgeInGraph(gGraph, vNode, v);
				if (eCur != null)
					lRes.add(eCur);
			}
		}
		return lRes;
	}    

	/** 
	 * Clears the cache of the locator.
	 */
	public void resetCache() {
		Cache.clear();
		ElementAccessTime.clear();
		CacheAccess.clear();

		TimeCnt = Long.MIN_VALUE;
	}

	/**
	 * Updates the cache if the edges of any vertex already contained
	 * within the cache are changed.
	 *
	 * @param e the new edge
	 */
	public void addedEdge(Edge e) {
		// Check cache
		TreeMap<String, Edge> hOutVertices = Cache.get(e.getSourceLabel());
		if (hOutVertices == null)
			return; // Not cached
		else
			hOutVertices.put(e.getTargetLabel(), e); // Update cache
	}

	/** 
	 * Returns the success ratio of the cache, which is the ratio of the number
	 * of hits over the number of cache accesses.
	 * @return the ratio of hits.
	 */
	public double getSuccessRatio() {
		return (double)lHits / (lHits + lMisses);
	}
}
