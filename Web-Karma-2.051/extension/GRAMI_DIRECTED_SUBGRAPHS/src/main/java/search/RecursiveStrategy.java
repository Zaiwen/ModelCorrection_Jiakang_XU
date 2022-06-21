/**
 * created May 16, 2006
 * 
 * @by Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 *
 * Copyright 2006 Marc Woerlein
 * 
 * This file is part of parsemis.
 *
 * Licence: 
 *  LGPL: http://www.gnu.org/licenses/lgpl.html
 *   EPL: http://www.eclipse.org/org/documents/epl-v10.php
 *   See the LICENSE file in the project's top-level directory for details.
 */
package search;


import AlgorithmInterface.Algorithm;
import au.com.d2dcrc.GramiMatcher;
import dataStructures.*;

import java.util.*;


//import de.parsemis.utils.Frequented;

/**
 * This class represents the local recursive strategy.
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * @param <NodeType>
 *            the type of the node labels (will be hashed and checked with
 *            .equals(..))
 * @param <EdgeType>
 *            the type of the edge labels (will be hashed and checked with
 *            .equals(..))
 */
public class RecursiveStrategy<NodeType, EdgeType> implements
		Strategy<NodeType, EdgeType> {

	private Extender<NodeType, EdgeType> extender;

	private Collection<HPListGraph<NodeType, EdgeType>> ret;

	private DFSCode<NodeType, EdgeType> canonicalCode;

	private HPListGraph<Integer, Double> listGraph;

	private ArrayList<Integer> graphNodes;

	private ArrayList<Integer> newNodes;

	private ArrayList<Integer> constraintNodes;

	private Graph initGraph;

	private int graphLevel;

	private HashMap<Integer, Integer> constraintMap;

	private int[] counts = new int[24];

	private HashMap<Integer, Integer[]>nodeAdjs;

	private ArrayList<Integer> nodeLabels;

	private boolean continueExtend = true;

	private boolean stopSearching = false;

	private Timer timer;

	class StopTask extends TimerTask {
		public void run() {
			System.out.println();
			System.out.println("time out! stop searching");
			System.out.println();
			stopSearching = true;
		}
	}


	public RecursiveStrategy() {

	}

	public RecursiveStrategy(Graph initGraph, ArrayList<Integer> newNodes, HashMap<Integer, Integer> constraintMap) {
		this.initGraph = initGraph;
		this.initGraph.setShortestPaths_1hop();
		listGraph = initGraph.getListGraph();
		graphLevel = listGraph.getNodeCount();
		graphNodes = (ArrayList<Integer>) listGraph.getNodeLabels();
		this.newNodes = new ArrayList<Integer>();
		this.newNodes.addAll(newNodes);
		this.constraintMap = constraintMap;
		this.constraintNodes = new ArrayList<>(graphNodes);
		this.constraintNodes.addAll(newNodes);
		this.nodeAdjs = new HashMap<>();
		this.nodeLabels = initGraph.getListGraph().getNodeLabels();
		timer = new Timer(true);
		this.timer.schedule(new StopTask(), 1000*60*5);


//		{
//			this.nodeAdjs.put(0, new Integer[]{1, 2, 4, 6, 14, 19});
//			this.nodeAdjs.put(1, new Integer[]{0, 2, 7, 8, 9, 12, 13, 14});
//			this.nodeAdjs.put(2, new Integer[]{0, 1, 3, 5, 6, 10, 11, 13, 14, 15, 17, 20, 21, 22, 23});
//			this.nodeAdjs.put(3, new Integer[]{2});
//			this.nodeAdjs.put(4, new Integer[]{0, 7, 8, 15});
//			this.nodeAdjs.put(5, new Integer[]{2});
//			this.nodeAdjs.put(6, new Integer[]{0, 2, 6, 23, 13});
//			this.nodeAdjs.put(7, new Integer[]{1, 19, 4, 14});
//			this.nodeAdjs.put(8, new Integer[]{1, 19, 4});
//			this.nodeAdjs.put(9, new Integer[]{1, 19});
//			this.nodeAdjs.put(10, new Integer[]{2});
//			this.nodeAdjs.put(11, new Integer[]{2, 15});
//			this.nodeAdjs.put(12, new Integer[]{1});
//			this.nodeAdjs.put(13, new Integer[]{1, 2, 6});
//			this.nodeAdjs.put(14, new Integer[]{16, 0, 2, 1, 18, 7});
//			this.nodeAdjs.put(15, new Integer[]{2, 4, 11});
//			this.nodeAdjs.put(16, new Integer[]{14});
//			this.nodeAdjs.put(17, new Integer[]{2});
//			this.nodeAdjs.put(18, new Integer[]{14});
//			this.nodeAdjs.put(19, new Integer[]{0, 7, 8, 9});
//			this.nodeAdjs.put(20, new Integer[]{2});
//			this.nodeAdjs.put(21, new Integer[]{2});
//			this.nodeAdjs.put(22, new Integer[]{2});
//			this.nodeAdjs.put(23, new Integer[]{2, 6});
//		}

	}

	public RecursiveStrategy(HPListGraph<Integer, Double> listGraph){
		this.listGraph = listGraph;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.strategy.Strategy#search(de.parsemis.miner.Algorithm,
	 *      int)
	 */
	public Collection<HPListGraph<NodeType, EdgeType>> search(//INITIAL NODES SEARCH
			final Algorithm<NodeType, EdgeType> algo,int freqThresh) {
		ret = new ArrayList<HPListGraph<NodeType, EdgeType>>();

		extender = algo.getExtender(freqThresh);

		for (final Iterator<SearchLatticeNode<NodeType, EdgeType>> it = algo.initialNodes(); it.hasNext();) {
			final SearchLatticeNode<NodeType, EdgeType> code = it.next();
			final long time = System.currentTimeMillis();
//			if (VERBOSE) {
//				out.print("doing seed " + code + " ...");
//			}
//			if (VVERBOSE) {
//				out.println();
//			}
			//System.out.println("Searching into: "+code);
			//System.out.println("*********************************");
			search(code);
			it.remove();

//			if (VERBOSE) {
//				out.println("\tdone (" + (System.currentTimeMillis() - time)
//						+ " ms)");
			//}
		}
		return ret;
	}




	/**
	 *
	 */
	public DFSCode<NodeType, EdgeType> searchDFSCode(Algorithm<NodeType, EdgeType> algo){
		extender = algo.getExtender(1);
		for (final Iterator<SearchLatticeNode<NodeType, EdgeType>> it = algo.initialNodes(); it.hasNext();) {
			final SearchLatticeNode<NodeType, EdgeType> code = it.next();
			searchNode(code);
		}
		return canonicalCode;
	}

	private void searchNode(SearchLatticeNode<NodeType, EdgeType> node){

		if(node.getLevel() == listGraph.getEdgeCount() - 1){
			DFSCode<NodeType, EdgeType> code = (DFSCode<NodeType, EdgeType>) node;
			if (code.getFrequency() == 1 && code.isCanonical()){
				canonicalCode = code;
				return;
			}
		}

		Collection<SearchLatticeNode<NodeType, EdgeType>> tmp = extender.getChildren(node);

		for (final SearchLatticeNode<NodeType, EdgeType> child : tmp) {
			searchNode(child);
		}

	}

	/**
	 *
	 */
	public Collection<HPListGraph<NodeType, EdgeType>> searchFromInitCode(Algorithm<NodeType, EdgeType> algo){
		ret = new ArrayList<HPListGraph<NodeType, EdgeType>>();
		extender = algo.getExtender(1);
		SearchLatticeNode<NodeType, EdgeType> code = algo.getInitialNode();

		search(code);

		return ret;

	}

	@SuppressWarnings("unchecked")
	private void search(final SearchLatticeNode<NodeType, EdgeType> node) {//RECURSIVE NODES SEARCH



		//System.out.println("Getting Children");
		final Collection<SearchLatticeNode<NodeType, EdgeType>> tmp = extender.getChildren(node);

		//System.out.println("finished Getting Children");
		//System.out.println(node.getLevel());
		for (final SearchLatticeNode<NodeType, EdgeType> child : tmp) {

//			if (VVVERBOSE) {
//				out.println("doing " + child);
//			}
			//System.out.println("   branching into: "+child);
			//System.out.println("   ---------------------");
			search(child);

		}
//		if (VVERBOSE) {
//			out.println("node " + node + " done. Store: " + node.store()
//					+ " children " + tmp.size() + " freq "
//					+ ((Frequented) node).frequency());
//		}
		if (node.store()) {
			node.store(ret);
		} else {
			node.release();
		}
//		node.finalizeIt();
	}



	public Collection<HPListGraph<NodeType, EdgeType>> extend(
			final Algorithm<NodeType, EdgeType> algo, int freqThresh) {

		ret = new ArrayList<>();

		extender = algo.getExtender(freqThresh);

//		GramiMatcher gm = new GramiMatcher();
//		gm.setGraph(initGraph);

		for (final Iterator<SearchLatticeNode<NodeType, EdgeType>> it = algo.initialNodes(); it.hasNext();) {
			final SearchLatticeNode<NodeType, EdgeType> code = it.next();

//			if (VERBOSE) {
//				out.print("doing seed " + code + " ...");
//			}
//			if (VVERBOSE) {
//				out.println();
//			}
			//System.out.println("Searching into: "+code);
			//System.out.println("*********************************");

//			try {
//				gm.setQry(new Query((HPListGraph<Integer, Double>) code.getHPlistGraph()));
//				int freq = gm.getFrequency(new HashMap<>(),1);
//				if (freq > 0){
//					extend(code);
//				}
//			}catch (Exception ignored){
//			}

			ArrayList<Integer> codeNodes = (ArrayList<Integer>) code.getHPlistGraph().getNodeLabels();
			boolean search = true;
			for (Integer codeNode : codeNodes) {
				if (!constraintNodes.contains(codeNode)){
					search = false;
					break;
				}
			}
			if (search){
				extend(code);
			}
//			extend(code);
			it.remove();
//			if (VERBOSE) {
//				out.println("\tdone (" + (System.currentTimeMillis() - time)
//						+ " ms)");
			//}
		}

		return ret;

	}


	private void extend(final SearchLatticeNode<NodeType, EdgeType> node) {//RECURSIVE NODES SEARCH

		if (stopSearching){
			return;
		}

		if(!continueExtend){
			return;
		}

		if (ret.size() == 1){
			return;
		}


		boolean extendFlag = stopExtend((DFSCode<NodeType, EdgeType>) node);

		System.out.println("check node:");
		System.out.println(node);
//		for (int count : counts) {
//			System.out.print(count+" ");
//		}
		System.out.println("stop extend this node? "+extendFlag);
		System.out.println();

		node.stopExtend(extendFlag);

		try {

			final Collection<SearchLatticeNode<NodeType, EdgeType>> tmp = extender.getChildren(node);

			for (SearchLatticeNode<NodeType, EdgeType> child : tmp) {
				extend(child);
				if (ret.size() == 1){
					return;
				}
			}

		}catch (Exception ignored){
		}

		if (node.store() && isCandidateResult((DFSCode<NodeType, EdgeType>) node)) {
			System.err.println("find a candidate result");
			node.store(ret);
		} else {
			node.release();
		}

		node.finalizeIt();

	}


	private boolean stopExtend(DFSCode<NodeType, EdgeType> code){

		HPListGraph<Integer, Double> codeGraph = (HPListGraph<Integer, Double>) code.getHPlistGraph();
		ArrayList<Integer> codeNodeLabels = (ArrayList<Integer>) code.getHPlistGraph().getNodeLabels();
		ArrayList<Integer> listGraphNodeLabels = listGraph.getNodeLabels();


		boolean flag = true;
		for (Integer label : codeNodeLabels) {
			if (!listGraphNodeLabels.remove(new Integer(label))){
				flag = false;
				break;
			}
		}
		if (flag){
			GramiMatcher gm = new GramiMatcher();
			gm.setGraph(initGraph);
			gm.setQry(new Query(codeGraph));
			int freq = gm.getFrequency(new HashMap<>(), 1);
			if (freq > 0){
				return false;
			}
		}



		Arrays.fill(counts, 0);

		IntIterator it =  codeGraph.nodeIndexIterator();

		int t = code.getLast().getNodeB() - code.getLast().getNodeA();
		assert code.getLast().getNodeB() > code.getLast().getNodeA();
		while (it.hasNext()){
			int nodeIdx = it.next();
			int label = codeGraph.getNodeLabel(nodeIdx);
			counts[label]++;

			int inDegree = codeGraph.getInDegree(nodeIdx);
			int outDegree = codeGraph.getOutDegree(nodeIdx);
			if (Math.abs(t) > 1 && (inDegree == 0 || outDegree == 0) && !constraintNodes.contains(label)){
				return true;
			}

//			if (!constraintNodes.contains(label)){
//				return true;
//			}

		}


//		int count = 0;
//		for (int label : codeNodeLabels) {
//			if (label == 0){
//				count++;
//			}
//		}

		for (int i = 0; i < counts.length; i++) {
			if (counts[i] > constraintMap.getOrDefault(i, Integer.MAX_VALUE)){
				return true;
			}
		}

		if (code.getHPlistGraph().getNodeCount() - code.getHPlistGraph().getEdgeCount() == 0){
			return true;
		}

		if (code.getHPlistGraph().getNodeCount() > constraintNodes.size()+1){
			return true;
		}

//		IntIterator eit = codeGraph.edgeIndexIterator();
//		if (count >= 2){
//			boolean hasEdge = false;
//			while (eit.hasNext()){
//				int edgeIdx = eit.next();
//				int label = Integer.parseInt((String) codeGraph.getEdgeLabel(edgeIdx));
//				int nodeA = codeGraph.getNodeA(edgeIdx);
//				int nodeB = codeGraph.getNodeB(edgeIdx);
//				int nodeLabelA = (Integer) codeGraph.getNodeLabel(nodeA);
//				int nodeLabelB = (Integer) codeGraph.getNodeLabel(nodeB);
//				if (nodeLabelA==0 && nodeLabelB==0 && label==7){
//					hasEdge = true;
//				}
//			}
//			if (!hasEdge){
//				return true;
//			}
//		}

		IntIterator eit = listGraph.edgeIndexIterator();
		while (eit.hasNext()){
			int edgeIdx = eit.next();
			int nodeA = listGraph.getNodeA(edgeIdx);
			int nodeB = listGraph.getNodeB(edgeIdx);
			int labelA = listGraph.getNodeLabel(nodeA);
			int labelB = listGraph.getNodeLabel(nodeB);
			boolean a = constraintNodes.lastIndexOf(new Integer(labelA)) == constraintNodes.indexOf(new Integer(labelA));
			boolean b = constraintNodes.lastIndexOf(new Integer(labelB)) == constraintNodes.indexOf(new Integer(labelB));
			if (codeNodeLabels.contains(labelA)
					&& codeNodeLabels.contains(labelB) && a && b
					&& !codeGraph.hasEdge(labelA, labelB)){
				return true;
			}

		}

		TreeSet<Integer> tmp = new TreeSet<>();
		int count4 = 0;
		IntIterator eit1 = codeGraph.edgeIndexIterator();
		while (eit1.hasNext()){
			int edgeIdx = eit1.next();
			int nodeA = codeGraph.getNodeA(edgeIdx);
			int nodeB = codeGraph.getNodeB(edgeIdx);
			int labelA = codeGraph.getNodeLabel(nodeA);
			int labelB = codeGraph.getNodeLabel(nodeB);
			if (labelA == 4){
				count4++;
				tmp.add(labelB);
			}else if (labelB == 4){
				count4++;
				tmp.add(labelA);
			}
		}
		if (tmp.size() < count4){
			return true;
		}


		return false;

	}




	private boolean isCandidateResult(DFSCode<NodeType, EdgeType> code)  {

		ArrayList<Integer> codeNodes = (ArrayList<Integer>) code.getHPlistGraph().getNodeLabels();
		System.err.println("codeNodes: "+codeNodes);
//		System.err.println("constraintNodes: "+constraintNodes);


		// code n_nodes > initGraph
		if (codeNodes.size() < constraintNodes.size()){
			return false;
		}

		for (Integer i : constraintNodes) {
			if (!codeNodes.remove(new Integer(i))){
				return false;
			}
		}

		//if initGraph is a subgraph of code
		GramiMatcher gm = new GramiMatcher();
		Graph codeGraph = Graph.DFSCodeToGraph(code);

		codeGraph.setShortestPaths_1hop();
		gm.setGraph(codeGraph);
		gm.setQry(new Query(initGraph.getListGraph()));
		int freq = gm.getFrequency(new HashMap<>(), 1);

		return freq > 0;

//		return true;

	}

	public static void main(String[] args) {

	}



}
