/**
 * created May 25, 2006
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

import CSP.Variable;
import dataStructures.*;
import edu.isi.karma.modeling.research.Params;
import utilities.MyPair;

import java.awt.*;
import java.util.*;


/**
 * Represents the right most extension of gSpan.
 * <p>
 * For gSpan just backward edges from the last inserted node, or forward edges
 * staring in nodes of the right most path (path of forward edges between the
 * "root" node to the last inserted node) are relevant.
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
public class RightMostExtension<NodeType, EdgeType> extends
		GenerationPartialStep<NodeType, EdgeType> {

	public static int counter = 0;
	
	private final Map<GSpanEdge<NodeType, EdgeType>, GSpanExtension<NodeType, EdgeType>> children;

	private TreeSet<Frequency> freqs;

	private ArrayList<ValidEdge> validEdges;



	/**
	 * creates a new pruning
	 * 
	 * @param next
	 *            the next step of the generation chain
	 */
	public RightMostExtension(
			final GenerationPartialStep<NodeType, EdgeType> next) {
		super(next);
		this.children = new TreeMap<GSpanEdge<NodeType, EdgeType>, GSpanExtension<NodeType, EdgeType>>();
		freqs = new TreeSet<>();
		freqs.add(new IntFrequency(0));

		validEdges = new ArrayList<>();

		if (Objects.equals(Params.DATASET_NAME, "museum-crm")){
			validEdges.add(new ValidEdge(0, 2, 4));
			validEdges.add(new ValidEdge(0, 0, 1));
			validEdges.add(new ValidEdge(0, 1, 6));
			validEdges.add(new ValidEdge(17, 18, 2));
			validEdges.add(new ValidEdge(2, 6, 3));
			validEdges.add(new ValidEdge(2, 8, 5));
			validEdges.add(new ValidEdge(2, 7, 0));
			validEdges.add(new ValidEdge(2, 15, 15));
			validEdges.add(new ValidEdge(14, 17, 16));
			validEdges.add(new ValidEdge(1, 3, 8));
			validEdges.add(new ValidEdge(1, 12, 12));
			validEdges.add(new ValidEdge(1, 5, 7));
			validEdges.add(new ValidEdge(1, 4, 9));
			validEdges.add(new ValidEdge(7, 2, 4));
			validEdges.add(new ValidEdge(2, 11, 6));
			validEdges.add(new ValidEdge(13, 13, 1));
			validEdges.add(new ValidEdge(13, 11, 6));
			validEdges.add(new ValidEdge(2, 22, 22));
			validEdges.add(new ValidEdge(2, 23, 20));
			validEdges.add(new ValidEdge(2, 21, 21));
			validEdges.add(new ValidEdge(19, 4, 9));
			validEdges.add(new ValidEdge(0, 0, 19));
			validEdges.add(new ValidEdge(19, 5, 7));
			validEdges.add(new ValidEdge(19, 3, 8));
			validEdges.add(new ValidEdge(15, 2, 4));
			validEdges.add(new ValidEdge(2, 9, 10));
			validEdges.add(new ValidEdge(2, 10, 11));
			validEdges.add(new ValidEdge(8, 2, 4));
			validEdges.add(new ValidEdge(2, 26, 17));
			validEdges.add(new ValidEdge(13, 13, 2));
			validEdges.add(new ValidEdge(2, 20, 1));
			validEdges.add(new ValidEdge(23, 28, 2));
			validEdges.add(new ValidEdge(23, 11, 6));
			validEdges.add(new ValidEdge(7, 16, 14));
			validEdges.add(new ValidEdge(1, 19, 9));
			validEdges.add(new ValidEdge(15, 24, 11));
			validEdges.add(new ValidEdge(1, 29, 14));
			validEdges.add(new ValidEdge(2, 14, 14));
			validEdges.add(new ValidEdge(0, 16, 14));
			validEdges.add(new ValidEdge(0, 7, 1));
			validEdges.add(new ValidEdge(14, 19, 18));
			validEdges.add(new ValidEdge(6, 25, 6));
		}
		else if (Objects.equals(Params.DATASET_NAME, "weapon-lod")){
			validEdges.add(new ValidEdge(1, 11, 0));
			validEdges.add(new ValidEdge(2, 2, 0));
			validEdges.add(new ValidEdge(2, 0, 1));
			validEdges.add(new ValidEdge(2, 8, 0));
			validEdges.add(new ValidEdge(2, 5, 2));
			validEdges.add(new ValidEdge(5, 7, 0));
			validEdges.add(new ValidEdge(4, 13, 0));
			validEdges.add(new ValidEdge(5, 13, 0));
			validEdges.add(new ValidEdge(2, 12, 3));
			validEdges.add(new ValidEdge(1, 13, 0));
			validEdges.add(new ValidEdge(2, 1, 0));
			validEdges.add(new ValidEdge(2, 13, 0));
			validEdges.add(new ValidEdge(2, 3, 5));
			validEdges.add(new ValidEdge(3, 4, 1));
			validEdges.add(new ValidEdge(2, 10, 4));
			validEdges.add(new ValidEdge(1, 9, 0));
			validEdges.add(new ValidEdge(3, 14, 0));
			validEdges.add(new ValidEdge(2, 9, 0));
			validEdges.add(new ValidEdge(2, 6, 0));

		}
		else if (Objects.equals(Params.DATASET_NAME, "museum-edm")){
			validEdges.add(new ValidEdge(5, 17, 4));
			validEdges.add(new ValidEdge(4, 16, 0));
			validEdges.add(new ValidEdge(5, 1, 0));
			validEdges.add(new ValidEdge(2, 11, 0));
			validEdges.add(new ValidEdge(3, 18, 5));
			validEdges.add(new ValidEdge(5, 20, 6));
			validEdges.add(new ValidEdge(5, 10, 0));
			validEdges.add(new ValidEdge(4, 13, 0));
			validEdges.add(new ValidEdge(1, 7, 0));
			validEdges.add(new ValidEdge(4, 9, 0));
			validEdges.add(new ValidEdge(5, 0, 0));
			validEdges.add(new ValidEdge(6, 11, 0));
			validEdges.add(new ValidEdge(5, 6, 0));
			validEdges.add(new ValidEdge(5, 12, 0));
			validEdges.add(new ValidEdge(4, 21, 0));
			validEdges.add(new ValidEdge(7, 11, 0));
			validEdges.add(new ValidEdge(3, 15, 2));
			validEdges.add(new ValidEdge(5, 3, 4));
			validEdges.add(new ValidEdge(3, 11, 0));
			validEdges.add(new ValidEdge(4, 2, 0));
			validEdges.add(new ValidEdge(4, 5, 0));
			validEdges.add(new ValidEdge(5, 22, 1));
			validEdges.add(new ValidEdge(5, 8, 0));
			validEdges.add(new ValidEdge(5, 14, 0));
			validEdges.add(new ValidEdge(5, 4, 7));
			validEdges.add(new ValidEdge(5, 19, 5));
		}

		// TODO: evtl schnellere vergleich der gEdges, aber das macht nicht viel
		// aus
	}

	/**
	 * includes the found extension to the corresponding fragment
	 * 
	 * @param gEdge
	 * @param code
	 */
	protected void add(final GSpanEdge<NodeType, EdgeType> gEdge,
			final DFSCode<NodeType, EdgeType> code,int type) {
		// search corresponding extension
		GSpanExtension<NodeType, EdgeType> ext = children.get(gEdge);

		//~

		if (ext == null) {
			// create new extension
			 HPMutableGraph<NodeType, EdgeType> ng = (HPMutableGraph<NodeType, EdgeType>) code.getHPlistGraph().clone();
			// TODO: avoid clone??
			gEdge.addTo(ng);  //reformulate the form of the new extended fragment!!
			ext = new GSpanExtension<NodeType, EdgeType>();
			ext.edge=gEdge;


			ext.frag = new DFSCode<NodeType, EdgeType>(code.getSortedFreqLabels(), code.getSingleGraph(),utilities.Util.clone(code.getNonCandidates())).set((HPListGraph<NodeType, EdgeType>)ng,code.getFirst() , code.getLast(),code.getParents());
			ext.frag = (DFSCode<NodeType, EdgeType>) code.extend(ext); //PUT THE STRING HERE

			if (code.isExtending()){
				Frequency freq = ext.frag.frequency();
				System.err.println(freqs);
				if (freqs.first().compareTo(freq) < 0){
					System.err.println(freqs);
					children.put(gEdge, ext);
					freqs.add(freq);
					if (freqs.size() > 3){
						freqs.remove(freqs.first());
					}
				}
			}else {
				children.put(gEdge, ext);
			}

//			children.put(gEdge, ext);   //TODO push into Children !!!
		} else {
			gEdge.release();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.MiningStep#call(de.parsemis.miner.SearchLatticeNode,
	 *      java.util.Collection)
	 */
	@Override
	public void call(final SearchLatticeNode<NodeType, EdgeType> node,
			final Collection<Extension<NodeType, EdgeType>> extensions) {
		// just give YOUR extensions to the next step
		extensions.clear();
		extensions.addAll(children.values());
		callNext(node, extensions);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.GenerationPartialStep#call(de.parsemis.miner.SearchLatticeNode,
	 *      de.parsemis.graph.Embedding)
	 */
	@Override
	public void call(final SearchLatticeNode<NodeType, EdgeType> node) 
	{
		counter++;
		extend((DFSCode<NodeType, EdgeType>) node);
		callNext(node); //malhash aii lazma
	}

	protected final void extend(final DFSCode<NodeType, EdgeType> code) 
	{
		System.out.println("Extending code:\n"+code);
		
		final HPGraph<NodeType, EdgeType> subGraph = code.getHPlistGraph();

		final int lastNode = subGraph.getNodeCount() - 1;
		
		Graph singleGraph= code.getSingleGraph();
		
		ArrayList<Double> freqEdgeLabels = singleGraph.getFreqEdgeLabels();
		
		ArrayList<Integer> sortedFreqLabels  =singleGraph.getSortedFreqLabels();
		
		Variable[] vrs = code.getCurrentVariables();



		if(vrs==null)
			System.out.println("aloooooooo");
		// find extensions of the last node;
		{
			Variable lastVariable=vrs[lastNode];
			//assertion!!
			
			HashSet<Integer> labelDC = lastVariable.getLabelsDistanceConstrainedWith();
			
			ArrayList<Point> pairs= new ArrayList<Point>();
			for (Iterator<Integer> iterator = labelDC.iterator(); iterator.hasNext();)
			{
				int theLabelB= iterator.next();
				int index=sortedFreqLabels.indexOf(theLabelB);
				pairs.add(new Point(theLabelB,index));				
			} 
			Collections.sort(pairs,new freqComparator());
			
			//Forward Edges
			int theLabelA=lastVariable.getLabel();
			for (int i = 0; i < pairs.size(); i++) 
			{
				Point currentPoint=pairs.get(i);
				int label=currentPoint.x;
				int index=currentPoint.y;
				
				for(int j=0;j<freqEdgeLabels.size();j++)
				{
					final GSpanEdge<NodeType, EdgeType> gEdge = new GSpanEdge<NodeType, EdgeType>().set(lastNode, lastNode+1 ,
							sortedFreqLabels.indexOf(theLabelA), freqEdgeLabels.get(j).intValue(), index, 1, theLabelA, label);
					if ((code.getLast().compareTo(gEdge) < 0) && isValidEdge(gEdge))
					{
						add(gEdge, code,0);
					}
					else
						gEdge.release();
				}
			}
			//NOw the other ONe
			labelDC = lastVariable.getLabelsDistanceConstrainedBy();
			
			pairs= new ArrayList<Point>();
			for (Iterator<Integer> iterator = labelDC.iterator(); iterator.hasNext();)
			{
				int theLabelB= iterator.next();
				int index=sortedFreqLabels.indexOf(theLabelB);
				pairs.add(new Point(theLabelB,index));				
			} 
			Collections.sort(pairs,new freqComparator());
			
			//Forward Edges
			theLabelA=lastVariable.getLabel();
			for (int i = 0; i < pairs.size(); i++) 
			{
				Point currentPoint=pairs.get(i);
				int label=currentPoint.x;
				int index=currentPoint.y;
				for(int j=0;j<freqEdgeLabels.size();j++)
				{
					final GSpanEdge<NodeType, EdgeType> gEdge = new GSpanEdge<NodeType, EdgeType>().set(lastNode, lastNode+1 , sortedFreqLabels.indexOf(theLabelA), freqEdgeLabels.get(j).intValue(), index, -1, theLabelA, label); 											
					if ((code.getLast().compareTo(gEdge) < 0) && isValidEdge(gEdge))
					{

						add(gEdge, code,0);
					}
					else
						gEdge.release();
				}
			}
			
			//Backward Edges!! 
			//now pass by each variable and check if we could add an edge!
			ArrayList<MyPair<Integer, Double>> connected= lastVariable.getDistanceConstrainedWith();
			labelDC = lastVariable.getLabelsDistanceConstrainedWith();
			for (int i = 0; i < vrs.length; i++) 
			{
				Variable candidateVB=vrs[i];
				if(candidateVB.getID()==lastVariable.getID())
					continue;
				
				if(labelDC.contains(candidateVB.getLabel()))
				{
					boolean isConstrainedWith=false;
					for (int j = 0; j < connected.size(); j++) 
					{
						Variable connectVB=vrs[connected.get(j).getA()];
						if(connectVB.getID()==candidateVB.getID())
							{isConstrainedWith=true;break;}
					}
					
					if(isConstrainedWith==true)
					{
						continue;
					}
					
					//else create Gedge
					int theLabelB=candidateVB.getLabel();
					for(int j=0;j<freqEdgeLabels.size();j++)
					{
						final GSpanEdge<NodeType, EdgeType> gEdge = new GSpanEdge<NodeType, EdgeType>().set(lastNode, candidateVB.getID() , sortedFreqLabels.indexOf(theLabelA), freqEdgeLabels.get(j).intValue(), sortedFreqLabels.indexOf(theLabelB), 1, theLabelA, theLabelB); 											
						if ((code.getLast().compareTo(gEdge) < 0) && isValidEdge(gEdge))
						{
							add(gEdge, code,1);
						}
						else
							gEdge.release();
					}
				}
			}
			//Now the Other ONe:
			
			connected= lastVariable.getDistanceConstrainedBy();
			labelDC = lastVariable.getLabelsDistanceConstrainedBy();
			for (int i = 0; i < vrs.length; i++) 
			{
				Variable candidateVB=vrs[i];
				if(candidateVB.getID()==lastVariable.getID())
					continue;
				
				if(labelDC.contains(candidateVB.getLabel()))
				{
					
					boolean isConstrainedWith=false;
					
					for (int j = 0; j < connected.size(); j++) 
					{
						Variable connectVB=vrs[connected.get(j).getA()];
						if(connectVB.getID()==candidateVB.getID())
							{isConstrainedWith=true;break;}
					}
					
					if(isConstrainedWith==true)
					{
						continue;
					}
					
					//else create Gedge
					int theLabelB=candidateVB.getLabel();
					for(int j=0;j<freqEdgeLabels.size();j++)
					{
						final GSpanEdge<NodeType, EdgeType> gEdge = new GSpanEdge<NodeType, EdgeType>().set(lastNode, candidateVB.getID() , sortedFreqLabels.indexOf(theLabelA), freqEdgeLabels.get(j).intValue(), sortedFreqLabels.indexOf(theLabelB), -1, theLabelA, theLabelB); 											
						if ((code.getLast().compareTo(gEdge) < 0) && isValidEdge(gEdge))
						{
							add(gEdge, code,1);
						}
						else
							gEdge.release();
					}
				}
			}
			
		}
		
		// find extensions of the rightmost Path;
		//**********************************
		// if findPathsOnly then only extensions at node 0 are necessary
		int ackNode = lastNode;
		do {
			// find extension of the right most path
			final GSpanEdge<NodeType, EdgeType> ack = code.getParent(ackNode);
			ackNode = ack.getNodeA();//patternID
			
			Variable currentVariable= vrs[ackNode];
			HashSet<Integer> labelDC = currentVariable.getLabelsDistanceConstrainedWith();
			ArrayList<Point> pairs= new ArrayList<Point>();
			for (Iterator<Integer> iterator = labelDC.iterator(); iterator.hasNext();)
			{
				int theLabelB= iterator.next();
				int index=sortedFreqLabels.indexOf(theLabelB);
				pairs.add(new Point(theLabelB,index));				
			} 
			Collections.sort(pairs,new freqComparator());
			//now create the forward edges!!!
			int theLabelA=currentVariable.getLabel();
			
			for (int i = 0; i < pairs.size(); i++) 
			{
				Point currentPoint=pairs.get(i);
				int label=currentPoint.x;
				int index=currentPoint.y;
				for(int j=0;j<freqEdgeLabels.size();j++)
				{
					final GSpanEdge<NodeType, EdgeType> gEdge = new GSpanEdge<NodeType, EdgeType>().set(ackNode, lastNode+1 , sortedFreqLabels.indexOf(theLabelA), freqEdgeLabels.get(j).intValue(), index, 1, theLabelA, label);
					if (isValidEdge(gEdge)) {
						add(gEdge, code, 0);
					}
				}
			}
		} while (ackNode > 0);
		/////////////////////////////////The other ONe
		
		ackNode = lastNode;
		do {
			// find extension of the right most path
			final GSpanEdge<NodeType, EdgeType> ack = code.getParent(ackNode);
			ackNode = ack.getNodeA();//patternID
			
			Variable currentVariable= vrs[ackNode];
			HashSet<Integer> labelDC = currentVariable.getLabelsDistanceConstrainedBy();
			ArrayList<Point> pairs= new ArrayList<Point>();
			for (Iterator<Integer> iterator = labelDC.iterator(); iterator.hasNext();)
			{
				int theLabelB= iterator.next();
				int index=sortedFreqLabels.indexOf(theLabelB);
				pairs.add(new Point(theLabelB,index));				
			} 
			Collections.sort(pairs,new freqComparator());
			//now create the forward edges!!!
			int theLabelA=currentVariable.getLabel();
			
			for (int i = 0; i < pairs.size(); i++) 
			{
				Point currentPoint=pairs.get(i);
				int label=currentPoint.x;
				int index=currentPoint.y;
				for(int j=0;j<freqEdgeLabels.size();j++)
				{
					final GSpanEdge<NodeType, EdgeType> gEdge = new GSpanEdge<NodeType, EdgeType>().set(ackNode, lastNode+1 , sortedFreqLabels.indexOf(theLabelA), freqEdgeLabels.get(j).intValue(), index, -1, theLabelA, label);
					if (isValidEdge(gEdge)) {
						add(gEdge, code, 0);
					}
				}
				
			}
		} while (ackNode > 0);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.GenerationPartialStep#reset()
	 */
	@Override
	public void reset() {
		children.clear();
		freqs.clear();
		freqs.add(new IntFrequency(0));
		resetNext();
	}


	public boolean isValidEdge(GSpanEdge<NodeType, EdgeType> gEdge){
		if (gEdge.getDirection() == 1){
			return validEdges.contains(new ValidEdge(gEdge.getThelabelA(), gEdge.getEdgeLabel(), gEdge.getThelabelB()));
		}
		return validEdges.contains(new ValidEdge(gEdge.getThelabelB(), gEdge.getEdgeLabel(), gEdge.getThelabelA()));
//		return true;
	}


	class ValidEdge{
		int label_a;
		int label_b;
		int edge_label;

		public ValidEdge(int label_a, int edge_label, int label_b) {
			this.label_a = label_a;
			this.edge_label = edge_label;
			this.label_b = label_b;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			ValidEdge validEdge = (ValidEdge) o;
			return Objects.equals(label_a, validEdge.label_a) && Objects.equals(label_b, validEdge.label_b) && Objects.equals(edge_label, validEdge.edge_label);
		}

		@Override
		public int hashCode() {
			return Objects.hash(label_a, label_b, edge_label);
		}

	}



}
