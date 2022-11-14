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
import edu.isi.karma.modeling.research.Params;

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

	private HPListGraph<Integer, Double> listGraph;

	private ArrayList<Integer> graphNodes;

	private ArrayList<Double> graphEdges;

	private ArrayList<Integer> newNodes;

	private ArrayList<Double> newEdges;

	private ArrayList<Integer> constraintNodes = new ArrayList<>();

	private ArrayList<Double> constraintEdges = new ArrayList<>();

	private Graph initGraph;

	private HashMap<Integer, Integer> constraintNodesMap = new HashMap<>();

	private HashMap<Double, Integer> constraintEdgesMap = new HashMap<>();

	private int[] nodesCounts = new int[50];

	private int[] edgeCounts = new int[50];

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

	public RecursiveStrategy(Graph initGraph, ArrayList<Integer> newNodes, HashMap<Integer, Integer> constraintNodesMap) {
		this.initGraph = initGraph;
		this.initGraph.setShortestPaths_1hop();
		listGraph = initGraph.getListGraph();
		graphNodes = listGraph.getNodeLabels();
		this.newNodes = new ArrayList<>();
		this.newNodes.addAll(newNodes);
		this.constraintNodesMap = constraintNodesMap;
		this.constraintNodes = new ArrayList<>(graphNodes);
		this.constraintNodes.addAll(newNodes);
		this.nodeLabels = initGraph.getListGraph().getNodeLabels();
		timer = new Timer(true);
		this.timer.schedule(new StopTask(), 1000*60*100);
	}


	public RecursiveStrategy(Graph initGraph, ArrayList<Integer> newNodes, ArrayList<Double> newEdges,
							 HashMap<Integer, Integer> constraintNodesMap, HashMap<Double, Integer> constraintEdgesMap){
		this.initGraph = initGraph;
		this.initGraph.setShortestPaths_1hop();
		listGraph = initGraph.getListGraph();
		graphNodes = listGraph.getNodeLabels();
		graphEdges = listGraph.getEdgeLabels();
		this.newNodes = new ArrayList<>();
		this.newNodes.addAll(newNodes);
		this.newEdges = new ArrayList<>();
		this.newEdges.addAll(newEdges);
		this.constraintNodes = new ArrayList<>(graphNodes);
		this.constraintNodes.addAll(newNodes);
		this.constraintEdges = new ArrayList<>(graphEdges);
		this.constraintEdges.addAll(newEdges);
		this.constraintNodesMap = constraintNodesMap;
		this.constraintEdgesMap = constraintEdgesMap;
		this.nodeLabels = initGraph.getListGraph().getNodeLabels();
		timer = new Timer(true);
		this.timer.schedule(new StopTask(), 1000*60*1);
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

		if(node.stopExtend()){
			return;
		}


		if (ret.size() == 1){
			return;
		}


		boolean extendFlag = stopExtend((DFSCode<NodeType, EdgeType>) node);

		System.out.println("check node:");
		System.out.println(node);
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
//			ignored.printStackTrace();
		}

		if (((DFSCode<NodeType, EdgeType>) node).getFrequency() == 0){
			node.store(false);
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


		Arrays.fill(nodesCounts, 0);
		Arrays.fill(edgeCounts, 0);

		IntIterator nit =  codeGraph.nodeIndexIterator();

		int t = code.getLast().getNodeB() - code.getLast().getNodeA();
//		System.out.println(code.getLast());
		assert code.getLast().getNodeB() > code.getLast().getNodeA();

		while (nit.hasNext()){
			int nodeIdx = nit.next();
			int label = codeGraph.getNodeLabel(nodeIdx);
			nodesCounts[label]++;
			int inDegree = codeGraph.getInDegree(nodeIdx);
			int outDegree = codeGraph.getOutDegree(nodeIdx);
			if (Math.abs(t) > 1 && (inDegree == 0 || outDegree == 0) && !constraintNodes.contains(label)){
				return true;
			}
		}

		if (!Objects.equals(Params.DATASET_NAME, "museum-crm")) {
			IntIterator eit = codeGraph.edgeIndexIterator();
			while (eit.hasNext()) {
				int edgeIdx = eit.next();
				int nodeA = codeGraph.getNodeA(edgeIdx);
				int nodeB = codeGraph.getNodeB(edgeIdx);
				int labelA = codeGraph.getNodeLabel(nodeA);
				int labelB = codeGraph.getNodeLabel(nodeB);
				String edgeLabelStr = String.valueOf(codeGraph.getEdgeLabel(edgeIdx));
				double edgeLabel = Double.parseDouble(edgeLabelStr);
				edgeCounts[(int) edgeLabel]++;
				if ((labelA == 0 || labelB == 0) && !constraintEdges.contains(edgeLabel)) {
					return true;
				}
			}
		}

//		IntIterator lgEit = listGraph.edgeIndexIterator();
//		while (lgEit.hasNext()){
//			int edgeIdx = lgEit.next();
//			int nodeA = listGraph.getNodeA(edgeIdx);
//			int nodeB = listGraph.getNodeB(edgeIdx);
//			int labelA = listGraph.getNodeLabel(nodeA);
//			int labelB = listGraph.getNodeLabel(nodeB);
//			boolean a = constraintNodes.lastIndexOf(new Integer(labelA)) == constraintNodes.indexOf(new Integer(labelA));
//			boolean b = constraintNodes.lastIndexOf(new Integer(labelB)) == constraintNodes.indexOf(new Integer(labelB));
//			if (codeNodeLabels.contains(labelA)
//					&& codeNodeLabels.contains(labelB) && a && b
//					&& !codeGraph.hasEdge(labelA, labelB)){
//				return true;
//			}
//		}

		for (int i = 0; i < nodesCounts.length; i++) {
			if (nodesCounts[i] > constraintNodesMap.getOrDefault(i, Integer.MAX_VALUE)){
				return true;
			}
		}

		for (int i = 0; i < edgeCounts.length; i++) {
			if (edgeCounts[i] > constraintEdgesMap.getOrDefault((double)i, Integer.MAX_VALUE)){
				return true;
			}
		}

		if (code.getHPlistGraph().getNodeCount() - code.getHPlistGraph().getEdgeCount() == 0){
			return true;
		}

		if (code.getHPlistGraph().getNodeCount() > constraintNodes.size()+1){
			return true;
		}


		if (Objects.equals(Params.DATASET_NAME, "museum-crm")) {
			TreeSet<Integer> tmp = new TreeSet<>();
			int count4 = 0;
			IntIterator eit1 = codeGraph.edgeIndexIterator();
			while (eit1.hasNext()) {
				int edgeIdx = eit1.next();
				int nodeA = codeGraph.getNodeA(edgeIdx);
				int nodeB = codeGraph.getNodeB(edgeIdx);
				int labelA = codeGraph.getNodeLabel(nodeA);
				int labelB = codeGraph.getNodeLabel(nodeB);
				if (labelA == 4) {
					count4++;
					tmp.add(labelB);
				} else if (labelB == 4) {
					count4++;
					tmp.add(labelA);
				}
			}
			if (tmp.size() < count4) {
				return true;
			}
		}

		return false;

	}


	private boolean isCandidateResult(DFSCode<NodeType, EdgeType> code)  {

		ArrayList<Integer> codeNodes = (ArrayList<Integer>) code.getHPlistGraph().getNodeLabels();
		ArrayList codeEdges = code.getHPlistGraph().getEdgeLabels();
		System.err.println("codeNodes: "+codeNodes);

		// code n_nodes > initGraph
		if (codeNodes.size() < constraintNodes.size()){
			System.err.println(1);
			return false;
		}

		for (Integer i : constraintNodes) {
			if (!codeNodes.remove(new Integer(i))){
				System.err.println(2);
				return false;
			}
		}

		for (Double d : constraintEdges) {
			int i = (int)d.doubleValue();
			if (!codeEdges.remove(String.valueOf(i))){
				System.err.println(3);
				return false;
			}
		}

		if (!Objects.equals(Params.DATASET_NAME, "museum-crm")) {
			IntIterator nit = code.getHPlistGraph().nodeIndexIterator();
			while (nit.hasNext()) {
				int nodeIdx = nit.next();
				Integer nodeLabel = (Integer) code.getHPlistGraph().getNodeLabel(nodeIdx);
				boolean notCandidateResult = true;
				if (nodeLabel != 0) {
					IntIterator eit = code.getHPlistGraph().edgeIndexIterator();
					while (eit.hasNext()) {
						int edgeIdx = eit.next();
						if (code.getHPlistGraph().getNodeA(edgeIdx) == nodeIdx
								|| code.getHPlistGraph().getNodeB(edgeIdx) == nodeIdx) {
							int otherNodeIdx = code.getHPlistGraph().getOtherNode(edgeIdx, nodeIdx);

							Integer otherNodeLabel = (Integer) code.getHPlistGraph().getNodeLabel(otherNodeIdx);
							if (otherNodeLabel == 0) {
								notCandidateResult = false;
								break;
							}
						}
					}
					if (notCandidateResult) {
						System.err.println(4);
						return false;
					}
				}
			}
		}

		//if initGraph is a subgraph of code
		GramiMatcher gm = new GramiMatcher();
		Graph codeGraph = Graph.DFSCodeToGraph(code);

		codeGraph.setShortestPaths_1hop();
		gm.setGraph(codeGraph);
		gm.setQry(new Query(initGraph.getListGraph()));
		int freq = gm.getFrequency(new HashMap<>(), 1);

		System.err.println(5);
		return freq > 0;

//		return true;

	}

	public static void main(String[] args) {

	}



}
