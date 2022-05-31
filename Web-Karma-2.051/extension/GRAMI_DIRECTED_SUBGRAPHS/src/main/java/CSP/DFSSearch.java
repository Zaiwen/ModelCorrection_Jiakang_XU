/**
 * Copyright 2014 Mohammed Elseidy, Ehab Abdelhamid

This file is part of Grami.

Grami is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 2 of the License, or
(at your option) any later version.

Grami is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Grami.  If not, see <http://www.gnu.org/licenses/>.
 */

package CSP;

import automorphism.Automorphism;
import dataStructures.*;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.Node;
import pruning.SPpruner;
import statistics.TimedOutSearchStats;
import utilities.MyPair;
import utilities.Settings;
import utilities.Util;

import java.awt.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;

//import java.math.BigDecimal;
//import java.math.BigInteger;

public class DFSSearch
{
	
    class StopTask extends TimerTask {
        public void run() {
        	if(Settings.LimitedTime)
        	{
        		System.out.format("Time's up!%n");
        		stopSearching();
        	}
        }
    }
	
    
	private Variable[] variables;
	private Variable[] result;
	private int resultCounter=0;
	private HashSet<Integer> visitedVariables;
	private SearchOrder sOrder;
	private int minFreqThreshold;
	private Timer timer;
	private Query qry;
	private BigInteger numberOfIterations;
	private BigDecimal worst;
	private final BigDecimal weight = new BigDecimal(Settings.approxEpsilon);
	private BigInteger finalWeight;
	private List<Map<Node, Node>> isomorphismList = new ArrayList<>();//To save all the correspondences between the node in the pattern and the node in the big graph. Added on 22 Jan 2019
    //private Set<Integer> specialNodeLabelSet;//label of anchors or limit nodes in pattern graph. Added on 22 Feb 2019
    private Set<Integer> anchorLabelSet = null;//labels of anchors in pattern graph. Added on 26 Feb 2019
    private Set<Integer> limitNodeLabelSet = null;//labels of limit nodes in pattern graph. Added on 26 Feb 2019

	public static int COSTTHRESHOLD=1;
	
	private volatile boolean isStopped=false;
	
	private HashMap<Integer, HashSet<Integer>> nonCandidates;
	
	public HashMap<Integer, HashSet<Integer>> getNonCandidates() {
		return nonCandidates;
	}

	
	public DFSSearch(ConstraintGraph cg,int minFreqThreshold,HashMap<Integer, HashSet<Integer>> nonCands) 
	{
		if(!Settings.CACHING)
			nonCandidates=(HashMap<Integer, HashSet<Integer>>) nonCands.clone();
		else
			nonCandidates=nonCands;
		this.minFreqThreshold=minFreqThreshold;
		variables=cg.getVariables();
		qry = cg.getQuery();
		result= new Variable[variables.length];
		for (int i = 0; i < variables.length; i++) 
		{
			HashMap<Integer, myNode> list = new HashMap<Integer, myNode>();
			result[i]= new Variable(variables[i].getID(), variables[i].getLabel(),list,variables[i].getDistanceConstrainedWith(),variables[i].getDistanceConstrainedBy()); 
		}
		visitedVariables= new HashSet<Integer>();
		sOrder= new SearchOrder(variables.length);
		limitNodeLabelSet = new HashSet<>();//added on 22 Feb 2019
        anchorLabelSet = new HashSet<>();//added on 26 Feb 2019
	}
	
	//for automorphisms and non-cached search
	public DFSSearch(SPpruner sp,Query qry,int minFreqThreshold) 
	{
		this.minFreqThreshold=minFreqThreshold;
		nonCandidates= new HashMap<Integer, HashSet<Integer>>();
		variables=sp.getVariables();
		this.qry = qry;
		result= new Variable[variables.length];
		for (int i = 0; i < variables.length; i++) 
		{
			HashMap<Integer, myNode> list = new HashMap<Integer, myNode>();
			result[i]= new Variable(variables[i].getID(), variables[i].getLabel(),list,variables[i].getDistanceConstrainedWith(),variables[i].getDistanceConstrainedBy()); 
		}
		visitedVariables= new HashSet<Integer>();
		sOrder= new SearchOrder(variables.length);
        limitNodeLabelSet = new HashSet<>();//added on 22 Feb 2019
        anchorLabelSet = new HashSet<>();//added on 26 Feb 2019
	}

	/**check node consistency and arc consistency. 6 Nov 2018.**/
	private void AC_3_New(Variable[] input)
	{
		LinkedList<VariablePair> Q= new LinkedList<VariablePair>();
		HashSet<String> contains = new HashSet<String> ();
		VariablePair vp;
		//initialize...
		for (int i = 0; i < input.length; i++) 
		{
			Variable currentVar= input[i];
			ArrayList<MyPair<Integer, Double>> list=currentVar.getDistanceConstrainedWith();
			for (int j = 0; j < list.size(); j++) 
			{
				Variable consVar=variables[list.get(j).getA()];
				vp =new VariablePair(currentVar,consVar,list.get(j).getB());
				Q.add(vp);
				contains.add(vp.getString());
				
			}
		}
		
		while(!Q.isEmpty())
		{			
			vp = Q.poll();
			contains.remove(vp.getString());
			Variable v1 = vp.v1;
			Variable v2 = vp.v2;

			//if(v1.getListSize()<freqThreshold || v2.getListSize()<freqThreshold) return;
			int oldV1Size = v1.getListSize();
			int oldV2Size = v2.getListSize();
			refine_Newest(v1, v2, vp.edgeLabel);
			if(oldV1Size!=v1.getListSize())
			{
				//if(v1.getListSize()<freqThreshold) return;
				//add to queue
				ArrayList<MyPair<Integer, Double>> list=v1.getDistanceConstrainedBy();
				for (int j = 0; j < list.size(); j++) 
				{
					Integer tempMP = list.get(j).getA(); 
					Variable consVar=variables[tempMP];
					vp =new VariablePair(consVar,v1,list.get(j).getB());
					if(!contains.contains(vp.getString()))
					{
						insertInOrder(Q, vp);
						//add new variables at the begining
						contains.add(vp.getString());
					}
				}
			}
			if(oldV2Size!=v2.getListSize())
			{
				//if(v2.getListSize()<freqThreshold) return;
				//add to queue
				ArrayList<MyPair<Integer, Double>> list=v2.getDistanceConstrainedBy();
				for (int j = 0; j < list.size(); j++) 
				{
					Variable consVar=variables[list.get(j).getA()];
					vp =new VariablePair(consVar,v2,list.get(j).getB());
					if(!contains.contains(vp.getString()))
					{
						insertInOrder(Q, vp);
						contains.add(vp.getString());
					}
				}
			}
		}
	}

	/**back up on 6 Nov 2018 because I try to modify it. below is the original code of GraMi**/
    private void AC_3_New(Variable[] input, int freqThreshold)
    {
        LinkedList<VariablePair> Q= new LinkedList<VariablePair>();
        HashSet<String> contains = new HashSet<String> ();
        VariablePair vp;
        //initialize...
        for (int i = 0; i < input.length; i++)
        {
            Variable currentVar= input[i];
            ArrayList<MyPair<Integer, Double>> list=currentVar.getDistanceConstrainedWith();
            for (int j = 0; j < list.size(); j++)
            {
                Variable consVar=variables[list.get(j).getA()];
                vp =new VariablePair(currentVar,consVar,list.get(j).getB());
                Q.add(vp);
                contains.add(vp.getString());

            }
        }

        while(!Q.isEmpty())
        {
            vp = Q.poll();
            contains.remove(vp.getString());
            Variable v1 = vp.v1;
            Variable v2 = vp.v2;

            if(v1.getListSize()<freqThreshold || v2.getListSize()<freqThreshold) return;
            int oldV1Size = v1.getListSize();
            int oldV2Size = v2.getListSize();
            refine_Newest(v1, v2, vp.edgeLabel, freqThreshold);
            if(oldV1Size!=v1.getListSize())
            {
                if(v1.getListSize()<freqThreshold) return;
                //add to queue
                ArrayList<MyPair<Integer, Double>> list=v1.getDistanceConstrainedBy();
                for (int j = 0; j < list.size(); j++)
                {
                    Integer tempMP = list.get(j).getA();
                    Variable consVar=variables[tempMP];
                    vp =new VariablePair(consVar,v1,list.get(j).getB());
                    if(!contains.contains(vp.getString()))
                    {
                        insertInOrder(Q, vp);
                        //add new variables at the begining
                        contains.add(vp.getString());
                    }
                }
            }
            if(oldV2Size!=v2.getListSize())
            {
                if(v2.getListSize()<freqThreshold) return;
                //add to queue
                ArrayList<MyPair<Integer, Double>> list=v2.getDistanceConstrainedBy();
                for (int j = 0; j < list.size(); j++)
                {
                    Variable consVar=variables[list.get(j).getA()];
                    vp =new VariablePair(consVar,v2,list.get(j).getB());
                    if(!contains.contains(vp.getString()))
                    {
                        insertInOrder(Q, vp);
                        contains.add(vp.getString());
                    }
                }
            }
        }
    }
    /**
     //	 * fast refine code
     //	 */
	private void refine_Newest(Variable v1, Variable v2, double edgeLabel)
	{
		HashMap<Integer,myNode> listA,listB;

		int labelB=v2.getLabel();//lebel of my neighbor
		listA=v1.getList();//the first column
		listB=v2.getList();//the second column
		HashMap<Integer,myNode> newList= new HashMap<Integer,myNode>();//the newly assigned first column
		HashMap<Integer, myNode> newReachableListB = new HashMap<Integer, myNode>();//the newly asigned second column

		//go over the first column
		for (Iterator<myNode> iterator = listA.values().iterator(); iterator.hasNext();)
		{
			myNode n1= iterator.next();//get the current node
			if(n1.hasReachableNodes()==false)//prune a node without reachable nodes
				continue;

			ArrayList<MyPair<Integer, Double>> neighbors = n1.getRechableWithNodeIDs(labelB, edgeLabel);//get a list of current node's neighbors
			if(neighbors==null)
				continue;

			for (Iterator<MyPair<Integer, Double>> iterator2 = neighbors.iterator(); iterator2.hasNext();)//go over each neighbor
			{
				MyPair<Integer, Double> mp = iterator2.next();//get current neighbor details
				//check the second column if it contains the current neighbor node
				if(listB.containsKey(mp.getA()))
				{
					//if true, put the current node in the first column, and the neighbor node in the second column
					newList.put(n1.getID(),n1);
					newReachableListB.put(mp.getA(), listB.get(mp.getA()));
				}
			}
		}

		//set the newly assigned columns
		v1.setList(newList);
		v2.setList(newReachableListB);
	}



	/**
	 * fast refine code
    back up on 6 Nov 2018. Below is the original code of GraMi (not modified)
	 */
	private void refine_Newest(Variable v1, Variable v2, double edgeLabel, int freqThreshold)
	{
		HashMap<Integer,myNode> listA,listB;

		int labelB=v2.getLabel();//lebel of my neighbor
		listA=v1.getList();//the first column
		listB=v2.getList();//the second column
		HashMap<Integer,myNode> newList= new HashMap<Integer,myNode>();//the newly assigned first column
		HashMap<Integer, myNode> newReachableListB = new HashMap<Integer, myNode>();//the newly asigned second column

		//go over the first column
		for (Iterator<myNode> iterator = listA.values().iterator(); iterator.hasNext();)
		{
			myNode n1= iterator.next();//get the current node
			if(n1.hasReachableNodes()==false)//prune a node without reachable nodes
				continue;

			ArrayList<MyPair<Integer, Double>> neighbors = n1.getRechableWithNodeIDs(labelB, edgeLabel);//get a list of current node's neighbors
			if(neighbors==null)
				continue;

			for (Iterator<MyPair<Integer, Double>> iterator2 = neighbors.iterator(); iterator2.hasNext();)//go over each neighbor
			{
				MyPair<Integer, Double> mp = iterator2.next();//get current neighbor details
				//check the second column if it contains the current neighbor node
				if(listB.containsKey(mp.getA()))
				{
					//if true, put the current node in the first column, and the neighbor node in the second column
					newList.put(n1.getID(),n1);
					newReachableListB.put(mp.getA(), listB.get(mp.getA()));
				}
			}
		}

		//set the newly assigned columns
		v1.setList(newList);
		v2.setList(newReachableListB);
	}
	
	public int hasBeenPrecomputed(Variable[] autos,int[] preComputed,int index) //if returns same index should search in it!!
	{
		HashMap<Integer, myNode> list = autos[index].getList();
		
		for (Iterator<Integer> iterator = list.keySet().iterator(); iterator.hasNext();) 
		{
			int nodeIndex = iterator.next();
			if(preComputed[nodeIndex]==1)
				return nodeIndex;
		}		
		return index; //else return the same index
	}
	
	private boolean areAllLabelsDistinct(HPListGraph<Integer, Double> me)
	{
		for (int i = 0; i < me.getNodeCount(); i++) 
		{
			int labelChecker=((Integer)me.getNodeLabel(i));
			for (int j = i+1; j < me.getNodeCount(); j++) 
			{
				int label= (Integer) me.getNodeLabel(j);
				if(labelChecker==label)
				{
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * given the graph, check whther it is Acyclic or not (we assume the graph is connected)
	 * @param me
	 * @return
	 */
	public static boolean isItAcyclic(HPListGraph<Integer, Double> me)
	{
		HashSet<Integer> visited = new HashSet<Integer>();
		Vector<Integer> toBeVisited = new Vector<Integer>();
		int currentNodeID;
		toBeVisited.add(0);
		while(visited.size()<me.getNodeCount())
		{
			if(toBeVisited.size()==0)
				break;
			
			currentNodeID = toBeVisited.get(0);
			toBeVisited.remove(0);
			visited.add(currentNodeID);
			//get all neighbor nodes (incoming and outgoing)
			int alreadyVisitedNeighbors = 0;//this should not be more than 1
			
			//all edges!
			for (final IntIterator eit = me.getEdgeIndices(currentNodeID); eit.hasNext();)
			{
				int edgeID = eit.next();
				int nID = me.getNodeA(edgeID);
				if(nID==currentNodeID) nID=me.getNodeB(edgeID);
				
				if(visited.contains(nID))
				{
					alreadyVisitedNeighbors++;
					if(alreadyVisitedNeighbors>1)
					{
						//System.out.println("It is CYCLIC!");
						return false;
					}
				}
				else
				{
					toBeVisited.add(nID);
				}
			}
		}
		
		return true;
	}


	/**this function is used to compute the valid assignment for every variable.
     * If the pattern graph has a tree-like structure and unique node labels, then the following optimization can be applied.
     * Let G be a graph with a single label per node, S(V_s,E_s,L_s) be a patten of G, S's underlying undirected graph is a tree,
     * and all of its node labels are unique. To calculate s_G(S) directly, it is sufficient to consider to consider the S to G CSP
     * and refine the domains of variables by enforcing node and arc consistency
     * @from 4 Nov 2018*
	 * @revised 8 Jan 2019
     * */
	public void searchExistances2() throws Exception
	{
        //Unique labels
		if(Settings.DISTINCTLABELS && areAllLabelsDistinct(qry.getListGraph()) && DFSSearch.isItAcyclic(qry.getListGraph()))//Line 5 - Line 7
		{
			AC_3_New(variables);//Here we don't consider threshold. 6 Nov 2018.
			result=cloneDomian(variables);
			return;
		}else {
		    throw new Exception("There exist some nodes with the same labels in the pattern graph!");
        }
	}


    /**this function is used to compute the valid assignment for every variable. 4 Nov 2018*
 back up @ 6 Nov 2018
 */
//    public void searchExistances()
//    {
//        if(Settings.isApproximate)
//            numberOfIterations=new BigInteger("0");
//        ArrayList<Integer> X= new ArrayList<Integer>();
//
//        //fast check for the min size of all the candidates, if any of them is below the minimum threshold break !!!
//        int min=variables[0].getListSize();
//        for (int i = 0; i < variables.length; i++)
//        {
//            if(min>variables[i].getListSize())
//                min=variables[i].getListSize();
//            X.add(variables[i].getID());
//        }
//        if(min<minFreqThreshold)
//            return;
//        if(variables.length==2 && variables[0].getLabel()!=variables[1].getLabel())
//        {
//            int nodeAIdx = qry.getListGraph().getNodeA(0);
//            int nodeBIdx = qry.getListGraph().getNodeB(0);
//            int nodeALabel = (Integer) qry.getListGraph().getNodeLabel(nodeAIdx);
//            int nodeBLabel = (Integer) qry.getListGraph().getNodeLabel(nodeBIdx);
//            int edgeLabel = qry.getListGraph().getEdge(nodeAIdx, nodeBIdx);
//            String sig = nodeALabel+"_"+edgeLabel+"_"+nodeBLabel;
//            if(StaticData.getHashedEdgesFreq(sig)>=minFreqThreshold)
//            {
//                result=cloneDomian(variables);
//                return;
//            }
//        }
//        if(Settings.DISTINCTLABELS && areAllLabelsDistinct(qry.getListGraph()) && DFSSearch.isItAcyclic(qry.getListGraph()))
//        {
//            AC_3_New(variables, minFreqThreshold);
//            result=cloneDomian(variables);
//            return;
//        }
//
//        //Now automorphisms
//        Variable[] autos=null;
//        Automorphism<Integer, Double> atm=null;
//        int[] preComputed=null;
//        if(Settings.isAutomorphismOn)
//        {
//            HPListGraph<Integer, Double> listGraph=qry.getListGraph();
//            preComputed=new int[variables.length];
//            for (int i = 0; i < preComputed.length; i++)
//            {
//                preComputed[i]=0;
//            }
//            atm=new Automorphism<Integer, Double>(listGraph);
//            autos= atm.getResult();
//        }
//
//
//
//        //SEARCH
//        ArrayList<myNode> tmp= new ArrayList<myNode>();
//        ArrayList<Integer> costs= new ArrayList<Integer>();
//        for (int i = variables.length-1; i >=0 ; i--) //Line 9 in Algorithm ISFREQUENT
//        {
//            TimedOutSearchStats.numberOfDomains++;
//            boolean search=true;
//            if(Settings.isAutomorphismOn && atm.hasAutomorphisms())
//            {
//
//                int preIndex= hasBeenPrecomputed(autos, preComputed, i);
//                if(i!=preIndex)
//                {
//                    search=false;
//                    if(Settings.PRINT)
//                        System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$it has automorphisms");
//
//                    variables[i].setList((HashMap<Integer, myNode>) variables[preIndex].getList().clone());
//                    result[i].setList((HashMap<Integer, myNode>) result[preIndex].getList().clone());
//                }
//            }
//
//            AC_3_New(variables, minFreqThreshold);//Line 13. a new arc consistency checker is set up. 5 Nov 2018
//
//
//            if(search==true)
//            {
//                //fast check
//                min=variables[0].getListSize();
//                for (int l = 0; l < variables.length; l++)
//                {
//                    if(min>variables[l].getListSize())
//                        min=variables[l].getListSize();
//                }
//                if(min<minFreqThreshold)
//                    return;
//
//                setVariableVisitingOrder(i);
//                int index=-1;
//                index = sOrder.getNext();
//                Variable firstVB = variables[index];
//                HashMap<Integer,myNode> firstList= firstVB.getList();
//                AssignmentInstance instance = new AssignmentInstance(variables.length);
//                if(tmp.size()>TimedOutSearchStats.maximum)
//                    TimedOutSearchStats.maximum = tmp.size();
//                tmp.clear();
//
//                for (Iterator<myNode> iterator = firstList.values().iterator(); iterator.hasNext();)//Line 15 in Algorithm ISFREQUENT
//                {
//                    myNode firstNode= iterator.next();
//                    //if already marked don't search it
//                    if(result[index].getList().containsKey(firstNode.getID()))
//                    {
//                        if(Settings.PRINT)
//                            System.out.println("ALready searched before !!");
//                        continue;
//                    }
//                    sOrder.reset();
//                    instance.assign(firstVB.getID(), firstNode);
//                    if(Settings.PRINT)
//                        System.out.println(instance);
//
//
//                    timer = new Timer(true);
//                    timer.schedule(new StopTask(), 5*1000);
//
//                    int value=-1;
//
//                    if(Settings.isApproximate)
//                    {
//                        numberOfIterations=new BigInteger("0");
//                        worst=new BigDecimal("1");
//                        for (int k = 0; k < variables.length; k++)
//                        {
//                            int listSize = (int)(variables[k].getList().size()*weight.doubleValue());
//                            worst= worst.multiply(new BigDecimal(listSize));
//                        }
//                        finalWeight= new BigInteger("1");
//
//                        finalWeight=finalWeight.multiply(worst.toBigInteger());
//                        finalWeight=finalWeight.multiply(new BigInteger((variables.length*2)+""));
//                        finalWeight=finalWeight.add(new BigDecimal(Settings.approxConstant).toBigInteger());
//                    }
//                    value=searchExistances(instance);//TODO
//
//                    //reset number of iterations!!
//                    numberOfIterations=new BigInteger("0");
//
//                    if(value==-3)
//                    {
//                        tmp.add(firstNode);
//                        TimedOutSearchStats.totalNumber++;
//
//                        if(Settings.PRINT)
//                            System.out.println("passed the time threshold!!");
//                        isStopped=false;
//                    }
//                    timer.cancel();
//
//                    if(value==-2) //not Found!!!
//                    {
//                        //remove element !!
//                        iterator.remove();
//                        if(Settings.isAutomorphismOn && atm.hasAutomorphisms())
//                        {
//                            HashMap<Integer, myNode> list= autos[firstVB.getID()].getList();
//                            for (Iterator<Integer>  iterator2= list.keySet().iterator(); iterator2.hasNext();)
//                            {
//                                int nodeIndex= iterator2.next();
//                                HashSet<Integer> nonCan=nonCandidates.get(nodeIndex);
//                                if(nonCan==null)
//                                {
//                                    nonCan= new HashSet<Integer>();
//                                    nonCan.add(firstNode.getID());
//                                    nonCandidates.put(nodeIndex, nonCan);
//                                }
//                                else
//                                {
//                                    if(!nonCan.contains(firstNode.getID()))
//                                        nonCan.add(firstNode.getID());
//                                }
//
//                            }
//                        }
//                        else
//                        {
//                            HashSet<Integer> nonCan=nonCandidates.get(firstVB.getID());
//                            if(nonCan==null)
//                            {
//                                nonCan= new HashSet<Integer>();
//                                nonCan.add(firstNode.getID());
//                                nonCandidates.put(firstVB.getID(), nonCan);
//                            }
//                            else
//                            {
//                                if(!nonCan.contains(firstNode.getID()))
//                                    nonCan.add(firstNode.getID());
//                            }
//                        }
//                        if(Settings.PRINT)
//                            System.out.println("ERRRRRRRRRRRRRRRRRRR........................................Not Found: ");
//                    }
//                    if(value==-1)
//                    {
//                        //instance Found
//                        for (int j = 0; j < variables.length; j++)
//                        {
//                            myNode assignedNode=instance.getAssignment(j);
//                            if(Settings.isAutomorphismOn && atm.hasAutomorphisms())
//                            {
//                                HashMap<Integer, myNode> list= autos[j].getList();
//                                for (Iterator<Integer>  iterator2= list.keySet().iterator(); iterator2.hasNext();)
//                                {
//                                    int nodeIndex= iterator2.next();
//                                    if(!result[nodeIndex].getList().containsKey(assignedNode.getID()))
//                                    {
//                                        result[nodeIndex].getList().put(assignedNode.getID(), assignedNode);
//                                    }
//                                }
//                            }
//                            else
//                            {
//                                if(!result[j].getList().containsKey(assignedNode.getID()))
//                                {
//                                    result[j].getList().put(assignedNode.getID(), assignedNode);
//                                }
//                            }
//                        }
//                        //check if the size of the list has passed already the minFreqThreshold!!
//                        if(result[index].getList().size()>=minFreqThreshold)// line 24 in the algorithm isfrequent
//                            break;
//                    }
//                    else if(value>=0)
//                        if(Settings.PRINT)
//                            System.out.println("ERRRRRRRRRRRRRRRRRRR........................................Value: "+value);
//
//                    instance.clear();
//                }
//
//                //Timedout search. Line 25.
//                if(Settings.isApproximate==false)
//                    if(result[index].getList().size()<minFreqThreshold)
//                    {
//                        System.out.println("into TMP Part 1");
//                        //fast check
//                        if(result[index].getList().size()+tmp.size()<minFreqThreshold)
//                            return;
//                        if(Settings.isApproximate==true)
//                        {
//                        }
//                        else //TRULY SEARCH INTO IT!!
//                        {
//                            System.out.println("into TMP Part 2");
//                            for (int j = 0; j < tmp.size(); j++)
//                            {
//                                System.out.println("found: "+result[index].getList().size()+" tmp: "+tmp.size()+" j: "+j);
//                                if((result[index].getList().size()+(tmp.size()-j))<minFreqThreshold)
//                                    return;
//
//                                boolean isExistant=true;
//
//                                if(Settings.isDecomposeOn==true) //decomposition is ON !!!
//                                {
//
//                                    HPListGraph<Integer, Double> actualPatternGraph = qry.getListGraph();
//                                    Decomposer<Integer, Double> com= new Decomposer<Integer, Double>(actualPatternGraph);
//                                    com.decompose();
//                                    ArrayList<HashMap<HPListGraph<Integer, Double>, ArrayList<Integer>>> maps=com.getMappings();
//                                    int counter=0;
//                                    for (int k = 0; k < maps.size(); k++) //iterate over edges removed!!
//                                    {
//                                        HashMap<HPListGraph<Integer, Double>, ArrayList<Integer>> edgeRemoved= maps.get(k);
//
//                                        for (Iterator<Entry<HPListGraph<Integer, Double>, ArrayList<Integer>>> iterator = edgeRemoved.entrySet().iterator(); iterator.hasNext();)
//                                        {
//                                            System.out.println(counter++);
//                                            Entry<HPListGraph<Integer, Double>, ArrayList<Integer>> removedEdgeEntry = iterator.next();
//                                            HPListGraph<Integer, Double> listGraph= removedEdgeEntry.getKey();	// ---------------------------->each graph candidate
//                                            String key=listGraph.toString();
//                                            myNode firstNode=tmp.get(j);
//
//
//                                            ArrayList<Integer> graphMappings=removedEdgeEntry.getValue();	//pattern nodeID ~ original ID
//                                            int correspondingINdex = searchMappings(graphMappings, i); //check if i==index
//                                            if(correspondingINdex==-1)
//                                                continue;
//
//                                            instance.assign(correspondingINdex, firstNode);
//
//                                            Query qry = new Query((HPListGraph<Integer, Double>)listGraph);
//                                            SPpruner sp = new SPpruner();
//                                            ArrayList<HashMap<Integer,myNode>> candidatesByNodeID = new ArrayList<HashMap<Integer,myNode>> ();
//                                            for (int l = 0; l < listGraph.getNodeCount(); l++)
//                                            {
//                                                candidatesByNodeID.add((HashMap<Integer, myNode>) variables[graphMappings.get(l)].getList().clone());
//                                            }
//                                            sp.getPrunedLists(candidatesByNodeID, qry);
//                                            DFSSearch df = new DFSSearch(sp,qry,-1);
//
//                                            isExistant=df.searchParticularExistance(instance, correspondingINdex);
//                                            if(isExistant==false)
//                                            {
//                                                firstList.remove(firstNode.getID());
//                                                HashSet<Integer> nonCan=nonCandidates.get(firstVB.getID());
//                                                if(nonCan==null)
//                                                {
//                                                    nonCan= new HashSet<Integer>();
//                                                    nonCan.add(firstNode.getID());
//                                                    nonCandidates.put(firstVB.getID(), nonCan);
//                                                }
//                                                else
//                                                {
//                                                    if(!nonCan.contains(firstNode.getID()))
//                                                        nonCan.add(firstNode.getID());
//                                                }
//                                                break;
//                                            }
//                                            instance.clear();
//                                        }
//                                        if(isExistant==false)
//                                            break;
//
//                                    }
//                                }
//
//                                if(isExistant==false)
//                                {
//                                    continue;
//                                }
//
//                                myNode firstNode=tmp.get(j);
//                                sOrder.reset();
//                                instance.assign(firstVB.getID(), firstNode);
//                                if(Settings.PRINT)
//                                    System.out.println(instance);
//                                //TODO
//                                int value;
//                                value=searchExistances(instance);
//
//                                if(value==-2)
//                                {
//                                    firstList.remove(firstNode.getID());
//
//                                    if(Settings.isAutomorphismOn && atm.hasAutomorphisms())
//                                    {
//                                        HashMap<Integer, myNode> list= autos[firstVB.getID()].getList();
//                                        for (Iterator<Integer>  iterator2= list.keySet().iterator(); iterator2.hasNext();)
//                                        {
//                                            int nodeIndex= iterator2.next();
//                                            HashSet<Integer> nonCan=nonCandidates.get(nodeIndex);
//                                            if(nonCan==null)
//                                            {
//                                                nonCan= new HashSet<Integer>();
//                                                nonCan.add(firstNode.getID());
//                                                nonCandidates.put(nodeIndex, nonCan);
//                                            }
//                                            else
//                                            {
//                                                if(!nonCan.contains(firstNode.getID()))
//                                                    nonCan.add(firstNode.getID());
//                                            }
//
//                                        }
//                                    }
//                                    else
//                                    {
//                                        HashSet<Integer> nonCan=nonCandidates.get(firstVB.getID());
//                                        if(nonCan==null)
//                                        {
//                                            nonCan= new HashSet<Integer>();
//                                            nonCan.add(firstNode.getID());
//                                            nonCandidates.put(firstVB.getID(), nonCan);
//                                        }
//                                        else
//                                        {
//                                            if(!nonCan.contains(firstNode.getID()))
//                                                nonCan.add(firstNode.getID());
//                                        }
//                                    }
//                                    if(Settings.PRINT)
//                                        System.out.println("ERRRRRRRRRRRRRRRRRRR........................................Not Found: ");
//
//                                }
//                                if(value==-1)
//                                {
//                                    System.out.println("Found...");
//                                    //instance Found
//                                    for (int k = 0; k < variables.length; k++)
//                                    {
//                                        myNode assignedNode=instance.getAssignment(k);
//                                        if(Settings.isAutomorphismOn && atm.hasAutomorphisms())
//                                        {
//                                            HashMap<Integer, myNode> list= autos[k].getList();
//                                            for (Iterator<Integer>  iterator2= list.keySet().iterator(); iterator2.hasNext();)
//                                            {
//                                                int nodeIndex= iterator2.next();
//                                                if(!result[nodeIndex].getList().containsKey(assignedNode.getID()))
//                                                {
//                                                    result[nodeIndex].getList().put(assignedNode.getID(), assignedNode);
//                                                }
//                                            }
//                                        }
//                                        else
//                                        {
//                                            if(!result[k].getList().containsKey(assignedNode.getID()))
//                                            {
//                                                result[k].getList().put(assignedNode.getID(), assignedNode);
//                                            }
//                                        }
//                                    }
//                                    //check if the size of the list has passed already the minFreqThreshold!!
//                                    if(result[index].getList().size()>=minFreqThreshold)
//                                        break;
//                                }
//                                instance.clear();
//                            }
//                        }
//                    }
//                //end of Search
//                if(result[index].getList().size()<minFreqThreshold)
//                    return;
//            }
//
//
//            resetVariableVisitingOrder();
//
//            //AC_3_New(variables, minFreqThreshold);//a new arc consistency checker is set up. 5 Nov 2018
//            if(Settings.isAutomorphismOn)
//                preComputed[i]=1;
//        }
//    }

    /**Add limit nodes*
     *@param limitNodeLabelSet a special node label set
     *@created 22 Feb 2019
     * */
    public void setLimitNodeLabel (Set<Integer> limitNodeLabelSet) {
        for (Integer i : limitNodeLabelSet) {
            this.limitNodeLabelSet.add(i);
        }
    }


    /**Add anchors*
     *@param anchorLabelSet an anchor label set
     *@created 22 Feb 2019
     * */
    public void setAnchorLabel (Set<Integer> anchorLabelSet) {
        for (Integer i : anchorLabelSet) {
            this.anchorLabelSet.add(i);
        }
    }

	/**begin to adapt GraMi to output all of the graph embeddings which follows Minimum image based metrics
     * It is not appropriate to adapt GrmMi into an isomorphism sub-graph matcher,
     * However, we decide to adapt it into a sub-graph matcher which follows Minimum image based metric,
     * and return the frequency as well as graph embeddings
     * *
	 * @from 8 Feb 2019
     * @revised 22 Feb 2019
     * @comment if the parameter 'special' is not empty, the algorithm will search for a solution for all of the variables of this special domain.
	 * */
	public void searchExistances()
	{
	    /**Limit node is the node contained in the pattern graph, but has least mapping nodes (embedded nodes) in the data graph*
         * @added on 13 Feb 2019
         * */

		if(Settings.isApproximate)
			numberOfIterations=new BigInteger("0");
		ArrayList<Integer> X= new ArrayList<>();

        int minimalImageNodeID =variables[0].getID();//ID of the minimal image node of the pattern graph. added on 13 Feb 2019

		//fast check for the min size of all the candidates, if any of them is below the minimum threshold break !!!
		int min=variables[0].getListSize();
		for (int i = 0; i < variables.length; i++)
		{
			if(min>variables[i].getListSize()) {
                min=variables[i].getListSize();
                minimalImageNodeID = variables[i].getID();
            }
			X.add(variables[i].getID());
		}

        minFreqThreshold = min;//update the minimum freq threshold, which is now the min size of all the candidates. added on 13 Feb 2019

//		if(min<minFreqThreshold)//maybe do not need it, because we need to count all of the solutions. 20 Dec 2018
//			return;


		if(variables.length==2 && variables[0].getLabel()!=variables[1].getLabel())
		{
			int nodeAIdx = qry.getListGraph().getNodeA(0);
			int nodeBIdx = qry.getListGraph().getNodeB(0);
			int nodeALabel = (Integer) qry.getListGraph().getNodeLabel(nodeAIdx);
			int nodeBLabel = (Integer) qry.getListGraph().getNodeLabel(nodeBIdx);
			int edgeLabel = qry.getListGraph().getEdge(nodeAIdx, nodeBIdx);
			String sig = nodeALabel+"_"+edgeLabel+"_"+nodeBLabel;
			if(StaticData.getHashedEdgesFreq(sig)>=minFreqThreshold)
			{
				result=cloneDomian(variables);
				return;
			}
		}

		//Now automorphisms. Compute the automorphisms of S
		Variable[] autos=null;
		Automorphism<Integer, Double> atm=null;
		int[] preComputed=null;
		if(Settings.isAutomorphismOn)
		{
			HPListGraph<Integer, Double> listGraph=qry.getListGraph();
			preComputed=new int[variables.length];
			for (int i = 0; i < preComputed.length; i++)
			{
				preComputed[i]=0;
			}
			atm=new Automorphism<Integer, Double>(listGraph);
			autos= atm.getResult();
		}



		//SEARCH
		ArrayList<myNode> tmp= new ArrayList<myNode>();
		ArrayList<Integer> costs= new ArrayList<Integer>();
		for (int i = variables.length-1; i >=0 ; i--) //Line 9 in Algorithm ISFREQUENT
		{
//			System.out.println("\n");
//			System.out.println("now begin to run the No. " + i + " variable (counting until 0). There are totally " + variables.length + " variables.");//added on 22 Jan 2019

            /**Check if the current variable represents a special nodes (anchors or limit nodes) or not. Added on 22 Feb 2019**/
            int label = variables[i].getLabel();
            /**Check if the current variable represents a special nodes (anchors or limit nodes) or not**/

			TimedOutSearchStats.numberOfDomains++;
			boolean search=true;
			if(Settings.isAutomorphismOn && atm.hasAutomorphisms())
			{

				int preIndex= hasBeenPrecomputed(autos, preComputed, i);
				if(i!=preIndex)
				{
					search=false;
					if(Settings.PRINT)
						System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$it has automorphisms");

					variables[i].setList((HashMap<Integer, myNode>) variables[preIndex].getList().clone());
					result[i].setList((HashMap<Integer, myNode>) result[preIndex].getList().clone());
				}
			}

			AC_3_New(variables, minFreqThreshold);//Line 13. a new arc consistency checker is set up. 5 Nov 2018


			if(search)
			{
				//fast check
				min=variables[0].getListSize();//'min' is the # variables in a domain that has the least variables. 22 Jan 2019
				for (int l = 0; l < variables.length; l++)
				{
					if(min>variables[l].getListSize())
						min=variables[l].getListSize();
				}

//				if(min<minFreqThreshold)// line 14. If the size of a domain is less than the threshold, then return false
//					return;

				setVariableVisitingOrder(i);
				int index=-1;
				index = sOrder.getNext();
				Variable firstVB = variables[index];//'firstVB' should be recorded. 22 Jan 2019
				HashMap<Integer,myNode> firstList= firstVB.getList();//get all of the variables in this domain
				AssignmentInstance instance = new AssignmentInstance(variables.length);
				if(tmp.size()>TimedOutSearchStats.maximum)
					TimedOutSearchStats.maximum = tmp.size();
				tmp.clear();


				for (Iterator<myNode> iterator = firstList.values().iterator(); iterator.hasNext();)//Line 15 in Algorithm ISFREQUENT
				{

				    /**check if current variable is special node or not. If it isn't a special node, then we will check *
                     * if the size of the result list has already passed the minFreqThreshold or not.
                     * However, if the current variable is a special node (anchors or limit node), we will try to search a solution for each assignment of this variable
                     * */
				    if (!limitNodeLabelSet.contains(label))
				        if (!anchorLabelSet.contains(label)) {
                            //check if the size of the result list has passed already the minFreqThreshold!!
                            if(result[index].getList().size()>=minFreqThreshold)// line 24 in the algorithm isfrequent. Move from the end of this for loop to here. 2019.02.13
                                break;
                        }

					myNode firstNode= iterator.next();
					//if already marked don't search it ---line 16
					if(result[index].getList().containsKey(firstNode.getID()))
					{
						if(Settings.PRINT)
							System.out.println("Already searched before !!");
						continue;
					}
					sOrder.reset();
					instance.assign(firstVB.getID(), firstNode);
					Node nodeInPattern = new InternalNode(Integer.toString(firstVB.getID()),new Label(Integer.toString(firstVB.getLabel())));//added on 27 Jan 2019
					Node nodeInData = new InternalNode(Integer.toString(firstNode.getID()),new Label(Integer.toString(firstNode.getLabel())));//added on 27 Jan 2019
					instance.addIsomorphism(nodeInPattern, nodeInData);//record the correspondence...important!

					if(Settings.PRINT)
						System.out.println(instance);


					timer = new Timer(true);
					timer.schedule(new StopTask(), 500);

					int value=-1;

					if(Settings.isApproximate)
					{
						numberOfIterations=new BigInteger("0");
						worst=new BigDecimal("1");
						for (int k = 0; k < variables.length; k++)
						{
							int listSize = (int)(variables[k].getList().size()*weight.doubleValue());
							worst= worst.multiply(new BigDecimal(listSize));
						}
						finalWeight= new BigInteger("1");

						finalWeight=finalWeight.multiply(worst.toBigInteger());
						finalWeight=finalWeight.multiply(new BigInteger((variables.length*2)+""));
						finalWeight=finalWeight.add(new BigDecimal(Settings.approxConstant).toBigInteger());
					}

					value=searchExistances(instance);//TODO

					//reset number of iterations!!
					numberOfIterations=new BigInteger("0");

					if(value==-3)
					{
						tmp.add(firstNode);
						TimedOutSearchStats.totalNumber++;

						if(Settings.PRINT)
							System.out.println("passed the time threshold!!");
						isStopped=false;
						timer.cancel();
						instance.clear();
						break;
					}
					timer.cancel();

					if(value==-2) //not Found!!!
					{
						//remove element !!
						iterator.remove();
						if(Settings.isAutomorphismOn && atm.hasAutomorphisms())
						{
							HashMap<Integer, myNode> list= autos[firstVB.getID()].getList();
							for (Iterator<Integer>  iterator2= list.keySet().iterator(); iterator2.hasNext();)
							{
								int nodeIndex= iterator2.next();
								HashSet<Integer> nonCan=nonCandidates.get(nodeIndex);
								if(nonCan==null)
								{
									nonCan= new HashSet<Integer>();
									nonCan.add(firstNode.getID());
									nonCandidates.put(nodeIndex, nonCan);
								}
								else
								{
									if(!nonCan.contains(firstNode.getID()))
										nonCan.add(firstNode.getID());
								}

							}
						}
						else
						{
							HashSet<Integer> nonCan=nonCandidates.get(firstVB.getID());
							if(nonCan==null)
							{
								nonCan= new HashSet<Integer>();
								nonCan.add(firstNode.getID());
								nonCandidates.put(firstVB.getID(), nonCan);
							}
							else
							{
								if(!nonCan.contains(firstNode.getID()))
									nonCan.add(firstNode.getID());
							}
						}
						if(Settings.PRINT)
							System.out.println("ERRRRRRRRRRRRRRRRRRR........................................Not Found: ");
					}
					if(value==-1)
					{
						//instance Found
						for (int j = 0; j < variables.length; j++)
						{
							myNode assignedNode=instance.getAssignment(j);
							if(Settings.isAutomorphismOn && atm.hasAutomorphisms())
							{
								HashMap<Integer, myNode> list= autos[j].getList();
								for (Iterator<Integer>  iterator2= list.keySet().iterator(); iterator2.hasNext();)
								{
									int nodeIndex= iterator2.next();
									if(!result[nodeIndex].getList().containsKey(assignedNode.getID()))
									{
										result[nodeIndex].getList().put(assignedNode.getID(), assignedNode);
									}
								}
							}
							else
							{
								if(!result[j].getList().containsKey(assignedNode.getID()))
								{
									result[j].getList().put(assignedNode.getID(), assignedNode);
								}
							}
						}

//						instance.printInstance();//added on 22 Jan 2019. Print this solution (each variable has a legal assignment)
						Map<Node,Node> isomorphism = instance.getIsomorphisms();//get the sub-graph isomorphism with regard to this assignment instance. Added on 25 Jan 2019.
						/**deep clone hash map above**/
						Map<Node,Node> clonedIsomorphisms = new HashMap<>();
						clonedIsomorphisms.putAll(isomorphism);
						isomorphismList.add(clonedIsomorphisms);//added on 22 Jan 2019. important!

//						//check if the size of the result list has passed already the minFreqThreshold!!
//						if(result[index].getList().size()>=minFreqThreshold)// line 24 in the algorithm isfrequent
//							break;//move these two rows of code to the beginning of the for loop. 2019.02.13
					}
					else if(value>=0)
						if(Settings.PRINT)
							System.out.println("ERRRRRRRRRRRRRRRRRRR........................................Value: "+value);
					instance.clear();
				}
				//end of Search
//				if(result[index].getList().size()<minFreqThreshold)
//					return;  //Line 34 of 'ISFREQUENT' Algorithm
			}


			resetVariableVisitingOrder();

			AC_3_New(variables, minFreqThreshold);//a new arc consistency checker is set up. 5 Nov 2018 maybe line 13
			if(Settings.isAutomorphismOn)
				preComputed[i]=1;

//			System.out.println("now finish running the No. " + i + " variable.");//added on 22 Jan 2019

		}
//
//		GramiMatcher.printMatchedNode(isomorphismList);

//		System.out.println("Generate Isomorphism Matrix!");
//        Node[][] matrix = getIsomorphismMatrix(isomorphismList);
//        /**If there is not any special node (anchor or limit node) in the pattern graph, we will remove some solutions whose assignments
//         * in the column of minimum image are duplicated.
//         * **/
//        if (specialNodeLabelSet.isEmpty()) //added on 22 Feb 2019
//        {
//            List<Map<Node,Node>> updatedIsomorphism = updateIsomorphismMatrix(matrix, minimalImageNodeID);
//            this.isomorphismList = updatedIsomorphism;
//            GramiMatcher.printMatchedNode(isomorphismList);
//        }
	}

	/**Generate sub-graph isomorphism matrix*
     * @from 13 Feb 2019
     * @param isomorphismList list of all the isomorphisms
     * @return isomorphism matrix, like:
     * 0 1  ---> pattern graph, where 0 is the limit node id, for instance
     * 3 0  ---> the 1st embedded graph
     * 3 2  ---> the 2nd embedded graph
     * 3 1  ---> the 3rd embedded graph
     * 8 9  ---> the 4th embedded graph
     * 5 6  ---> the 5th embedded graph
     * */
    private Node[][] getIsomorphismMatrix (List<Map<Node,Node>> isomorphismList) {
        Node[][] matrix = new Node [isomorphismList.size()+1][isomorphismList.get(0).size()];


        for (int i = 0; i < isomorphismList.size(); i++) {

            Map<Node,Node> embeddedGraphs = isomorphismList.get(i);// get sub-graph isomorphism map
            int index = 0;//index for the matrix
            Iterator it = embeddedGraphs.entrySet().iterator();
            while (it.hasNext()) {

                Map.Entry pair = (Map.Entry)it.next();
                Node nodeInPattern = (Node) pair.getKey();//node in the pattern
                Node nodeInData = (Node) pair.getValue();//node in the data graph

                if (i == 0) {
                    matrix[i][index] = nodeInPattern;
                    matrix[i+1][index] = nodeInData;
                } else {

                    matrix[i+1][index] = nodeInData;

                }
                index++;
            }
        }

        return matrix;
    }

    /**Filter out embedded graphs. For each limit node, the number of legal assignments is equal to the number of variables*
     * @from 14 Feb 2019
     *@param matrix matrix to be updated
     * @param limitNodeID For each limit node, we filter out duplicated legal assignments
     * @return updated isomorphism list
     * */
    public List<Map<Node,Node>> updateIsomorphismMatrix (Node[][] matrix, int limitNodeID) {

        /**index is the column ID where the limit node is**/
        int index = 0;
        for (int i = 0; i < matrix[0].length; i++) {
            int number = Integer.parseInt(matrix[0][i].getId().trim());//convert string to int
            if (number == limitNodeID) {
                index = i;
            }
        }

        /**Find the duplicates in the column where the limit node is**/
        HashSet<Integer> set = new HashSet<>();

        /**create a new 2D array**/
        Node[][] updatedMatrix = new Node[matrix.length][matrix[0].length];

        /**Copy the pattern graph (the first row of the pattern graph) into the first row of the updated matrix**/
        for (int m = 0; m < matrix[0].length; m++) {

            updatedMatrix[0][m] = matrix[0][m];
        }

        int k = 1;

        /**Get each row of matrix except the first row (because the first row is pattern graph)**/
        for (int j = 1; j < matrix.length; j++) {

            Node element = matrix[j][index];
            if (set.add(Integer.parseInt(element.getId().trim()))) {//it means that the element is NOT duplicate

                /**copy this row from the original 2D array to the updated one**/
                updatedMatrix[k] = matrix[j];
                k++;
            }
        }
        k = k-1; //k- the efficient rows of the updated matrix

        List<Map<Node,Node>> updatedIsomorphismList = new ArrayList<>();
        for (int n = 1; n <= k; n++) {
            Map<Node,Node> map = new HashMap<>();
            for (int p = 0; p < updatedMatrix[0].length; p++) {

                map.put(updatedMatrix[0][p],updatedMatrix[n][p]);
            }

            updatedIsomorphismList.add(map);
        }


        return updatedIsomorphismList;
    }
	
	
	private void printVariablesSize(Variable[] vars)
	{
		for (int i = 0; i < vars.length; i++) 
		{
			System.out.println("Var["+i+"]"+vars[i].getList().size());
		}
	}
	
	private int calculateCost(AssignmentInstance instance)
	{
		int cost=0;
		for (int k = 0; k < variables.length; k++) 
		{
			myNode assignedNode=instance.getAssignment(k);
			if(assignedNode==null)
			{
				//cost of the missing Node
				cost+=1;
				//aggregate the cost of the missing edges!!
				int in=variables[k].getDistanceConstrainedBy().size();
				int out=variables[k].getDistanceConstrainedWith().size();
				cost=cost+in+out;
			}
		}
		return cost;
	}
	
	
	public boolean searchParticularExistance(AssignmentInstance instance,int orderINdex)
	{
		sOrder.reset();
		setVariableVisitingOrder(orderINdex);
		timer = new Timer(true);
        timer.schedule(new StopTask(), 7*1000);
		int value=searchExistances(instance);
		isStopped=false;
		timer.cancel();
		if(value==-2) return false;
		else return true;
		
	}
	
	private int searchMappings(ArrayList<Integer> mappings, int variableINdex)
	{
		for (int i = 0; i < mappings.size(); i++) 
		{
			if(mappings.get(i)==variableINdex)
				return i;
		}
		return -1;
	}
	/**Search for a solution that assigns u to x for a given time. Line 18.
     *
     * @comment 20 Dec 2018**/
	private int searchExistances(AssignmentInstance instance)
	{
		if(Settings.isApproximate)
		{
			if(finalWeight.compareTo(numberOfIterations)<1)
			{
				return -3;
			}
		}
		else
		{
			if(isStopped)//remove on 20 Dec 2018
			{
				return -3;
			}
		}
		
		int index = sOrder.getNext();

		if(index!=-1)
		{
			Variable currentVB=variables[index];
			ArrayList<MyPair<Integer, Double>> constrainingVariables=currentVB.getDistanceConstrainedWith();
			
			ArrayList<ArrayList<MyPair<Integer, Double>>> candidates= new ArrayList<ArrayList<MyPair<Integer, Double>>>();
			ArrayList<VariableCandidates> variableCandidates= new ArrayList<VariableCandidates>();
			
			//check Validity with constraintVariables
			for (int i = 0; i < constrainingVariables.size(); i++) 
			{
				Variable cnVariable=variables[constrainingVariables.get(i).getA()];
				Double edgeLabel = constrainingVariables.get(i).getB();
				int cnVariableIndex=cnVariable.getID();
				myNode cnVariableInstance = instance.getAssignment(cnVariableIndex);
				if(cnVariableInstance!=null)
				{
					candidates.add(cnVariableInstance.getRechableByNodeIDs(currentVB.getLabel(), edgeLabel));
					variableCandidates.add(new VariableCandidates(cnVariableIndex, cnVariableInstance.getRechableByNodeIDs(currentVB.getLabel(), edgeLabel)));
				}
			}
			
			ArrayList<MyPair<Integer, Double>> constrainingBYVariables=currentVB.getDistanceConstrainedBy();
			for (int i = 0; i < constrainingBYVariables.size(); i++) 
			{
				Variable cnVariable=variables[constrainingBYVariables.get(i).getA()];
				Double edgeLabel = constrainingBYVariables.get(i).getB();
				int cnVariableIndex=cnVariable.getID();
				myNode cnVariableInstance = instance.getAssignment(cnVariableIndex);
				if(cnVariableInstance!=null)
				{
					candidates.add(cnVariableInstance.getRechableWithNodeIDs(currentVB.getLabel(), edgeLabel));
					variableCandidates.add(new VariableCandidates(cnVariableIndex, cnVariableInstance.getRechableWithNodeIDs(currentVB.getLabel(), edgeLabel)));
				}
			}
			
			
			
			ArrayList<MyPair<Integer, Double>> finalCandidates= Util.getIntersection(candidates);
			
			if(finalCandidates.size()==0)
			{
				//learn the new constraints !!!
				ArrayList<Point> constrainedVariableIndices=Util.getZerosIntersectionIndices(variableCandidates);
				if(constrainedVariableIndices.size()!=0)
				{
					Point p =constrainedVariableIndices.get(0);
					int minValue=sOrder.getSecondOrderValue(p.x, p.y);
					for (int i = 1; i < constrainedVariableIndices.size(); i++) 
					{
						p = constrainedVariableIndices.get(i);
						int value=sOrder.getSecondOrderValue(p.x, p.y);
						if(minValue>value)
							minValue=value;
					}
						int jumpToIndex=sOrder.getVariableIndex(minValue);
						sOrder.stepBack();
						if(Settings.isApproximate)
							numberOfIterations=numberOfIterations.add(new BigInteger("1"));
						
						instance.deAssign(currentVB.getID());
						return jumpToIndex;
				}
				
			}

						
			int hasResult=0;
			
			for (int i = 0; i < finalCandidates.size(); i++) 
			{
				int candidateIndex=finalCandidates.get(i).getA();
				myNode candidateNode = currentVB.getList().get(candidateIndex);
								
				if(candidateNode!=null)
				{
					instance.assign(currentVB.getID(), candidateNode);
					Node nodeInPattern = new InternalNode(Integer.toString(currentVB.getID()),new Label(Integer.toString(currentVB.getLabel())));
                    Node nodeInData = new InternalNode(Integer.toString(candidateNode.getID()),new Label(Integer.toString(candidateNode.getLabel())));
					instance.addIsomorphism(nodeInPattern, nodeInData);//add a correspondence to the isomorphism. Added on 22 Jan 2019
					//check identity Validity
					if(AssignmentInstance.ensureIDValidty(instance))
					{
						hasResult = searchExistances(instance);
						if(hasResult==-3)
							return -3;
						if(hasResult==-1)
							return -1;
						else if(hasResult>=0)
						{
							if (currentVB.getID()!=hasResult)
							{
								sOrder.stepBack();
								if(Settings.isApproximate)
									numberOfIterations=numberOfIterations.add(new BigInteger("1"));
								instance.deAssign(currentVB.getID());
								return hasResult;
							}
						}
						else
							;
					}
					else
					{
						if(Settings.isApproximate)
							numberOfIterations=numberOfIterations.add(new BigInteger("1"));
						instance.deAssign(currentVB.getID());
						
					}	
				}
				//End ID Validity
			}
			//after finishing... step back to before state
			sOrder.stepBack();
			if(Settings.isApproximate)
				numberOfIterations=numberOfIterations.add(new BigInteger("1"));
			instance.deAssign(currentVB.getID());
			
		}
		else// index ==-1 means that I reached the point where the assignment is legal
		{
			return -1; //return True
		}
		return -2; //return False
	}



	public void stopSearching()
	{
		isStopped=true;
	}
	
	
	
	//***************************************
	public int getResultCounter()
	{
		return resultCounter;
	}
	
	private void resetVariableVisitingOrder()
	{
		sOrder= new SearchOrder(variables.length);
		visitedVariables.clear();
	}
	
	private void setVariableVisitingOrder(int begin)
	{
		sOrder.addNext(begin);
		visitedVariables.add(begin);
		searchOrder(variables[begin]);
	}
	
	private void searchOrder(Variable vb)
	{
		HashMap<Integer, myNode> list = vb.getList();
		ArrayList<MyPair<Integer, Double>> constrains= vb.getDistanceConstrainedWith();
		for (int i = 0; i < constrains.size(); i++) 
		{
			Variable currentVB= variables[constrains.get(i).getA()];
			if(!visitedVariables.contains(currentVB.getID()))
			{
				visitedVariables.add(currentVB.getID());
				sOrder.addNext(currentVB.getID());
				searchOrder(currentVB);
			}
		}
		ArrayList<MyPair<Integer, Double>> constrainsBY= vb.getDistanceConstrainedBy();
		for (int i = 0; i < constrainsBY.size(); i++) 
		{
			Variable currentVB= variables[constrainsBY.get(i).getA()];
			if(!visitedVariables.contains(currentVB.getID()))
			{
				visitedVariables.add(currentVB.getID());
				sOrder.addNext(currentVB.getID());
				searchOrder(currentVB);
			}
		}
		
	}
	/**
     * @comment 4 Nov 2018.*Get the frequency of a pattern according to the size of variables.
     * @Checked 20 Dec 2018
     * @comment 9 Jan 2019 * implementation of Proposition 2 of Grami paper
     * */
	public int getFrequencyOfPattern()
	{
		
		int min= result[0].getListSize();
		for (int i = 1; i < result.length; i++) 
		{
			if(min>result[i].getListSize())
				min= result[i].getListSize();
		}
		return min;
	}
	
	public Variable[] getResultVariables() {
		return result;
	}


	public void printListFrequencies()
	{
		for (int i = 0; i < result.length; i++) 
		{
			System.out.println("Result["+result[i].getID()+"] (Label:"+result[i].getLabel()+")=  "+result[i].getListSize());
			HashMap<Integer, myNode> list = result[i].getList();
		}
	}
	
	
	private int getMaxDegreeVariableIndex()
	{
		Variable[] vs= variables;
		int index=0;
		int max=vs[0].getConstraintDegree();
		for (int i = 1; i < vs.length; i++) 
		{
			int degree=vs[i].getConstraintDegree();
			if(max< degree)
			{
				max=degree;
				index=i;
			}
		}	
		return index;
	}
	
	private int getMinListVariableIndex()
	{
		Variable[] vs= variables;
		int index=0;
		int min=vs[0].getListSize();
		for (int i = 1; i < vs.length; i++) 
		{
			int listSize=vs[i].getListSize();
			if(min>listSize)
			{
				index=i;
				min=listSize;
			}
		}
		return index;
	}

	private boolean isAnyEmpty(Variable[] domain)
	{
		boolean isAnyEmpty=false;
		for (int i = 0; i < domain.length; i++) 
		{
			if(domain[i].getList().size()==0)
				{isAnyEmpty=true;break;}
		}
		return isAnyEmpty;
	}
	
	private Variable[] cloneDomian(Variable[] domain)
	{
		Variable[] cloneDomian= new Variable[domain.length];
		for (int i = 0; i < cloneDomian.length; i++) 
		{
			Variable currentDomain=domain[i];
			cloneDomian[i]=new Variable(currentDomain.getID(), currentDomain.getLabel(), (HashMap<Integer, myNode>) currentDomain.getList().clone(), null,null);
		}
		
		//add constraints !!
		for (int i = 0; i < cloneDomian.length; i++) 
		{
			cloneDomian[i].setDistanceConstrainedBy(domain[i].getDistanceConstrainedBy());
			cloneDomian[i].setDistanceConstrainedWith(domain[i].getDistanceConstrainedWith());
		}
		return cloneDomian;
	}
	
	private Variable[] look_ahead(int index, myNode node ,Variable[] currentDomain)
	{
		Variable[] result=cloneDomian(currentDomain);
		
		//assert!!
		if(!result[index].getList().containsKey(node.getID()))
			;
		
		//assign this node !!
		result[index].getList().clear();
		result[index].getList().put(node.getID(), node);
		{
		HashSet<Integer> asserter= new HashSet<Integer>();
		boolean collision=false;
		//assert that all assigned nodes are distinct
		for (int i = 0; i < result.length; i++) 
		{
			if(result[i].getList().size()==1)
			{
				int nodeID=result[i].getList().keySet().iterator().next();
				if(asserter.contains(nodeID))
				{
					collision=true;
					break;
				}
				else
					asserter.add(nodeID);
			}
		}
		if(collision==true)
		{
			return null;
		} //assignment not valid !!
		}
		
		//now refine..
		AC_3_New(result, minFreqThreshold);
		return result;
	}
	
	private int solve(ArrayList<Integer> X,Variable[] currentDomain,AssignmentInstance instance)
	{
		int index = sOrder.getNext();
		if(index!=-1)
		{
			Variable[] D=currentDomain;
			Variable[] D_dash=D;
			//
			boolean isAnyEmptyD_dash=isAnyEmpty(D_dash);
			Variable currentVariable = D_dash[index];
						
			if(!isAnyEmptyD_dash)
			{
				//iterate over them !!
				for (Iterator<Entry<Integer, myNode>> iterator = currentVariable.getList().entrySet().iterator();iterator.hasNext();) 
				{
					Entry<Integer, myNode> nodeEntry = iterator.next();
					Variable[] D_dash_dash=look_ahead(index, nodeEntry.getValue(), D_dash);
					if(D_dash_dash!=null && !isAnyEmpty(D_dash_dash))
					{
						X.remove((Integer)currentVariable.getID());
						instance.assign(currentVariable.getID(), nodeEntry.getValue());
						int hasSoln=solve(X, D_dash_dash,instance);
						if(hasSoln==-1)
							return -1;
					}
					else
						;
				}
			}
			else //if any domain is Empty!!
				;
			sOrder.stepBack();
			instance.deAssign(currentVariable.getID());
			X.add(index);
		}
		else  //found solution!!
		{
			return -1;  //Found !!
		}
		
		return -2; //not found
	}
	
	private Variable[] forwardCheck(Variable[] domain)
	{
		
		for (int i = 0; i < domain.length; i++) 
		{
			Variable currentDomain=domain[i];
			if(currentDomain.getList().size()==1)
			{
				myNode node=currentDomain.getList().values().iterator().next();
				
				
				ArrayList<MyPair<Integer, Double>> consBY =domain[i].getDistanceConstrainedBy();
				HashMap<Integer, ArrayList<MyPair<Integer, Double>>> nodereachBy= node.getReachableByNodes(); //Label ~ NodeIDs
				
				for (int j = 0; j < consBY.size(); j++) 
				{
					int variableIndex = consBY.get(j).getA();
					Variable vb = domain[variableIndex];
					HashMap<Integer, myNode> vbList = vb.getList();
					ArrayList<MyPair<Integer, Double>> candNodes= nodereachBy.get(vb.getLabel());
					HashMap<Integer, myNode> newList = new HashMap<Integer, myNode>();
					for (int k = 0; k < candNodes.size(); k++) 
					{
						int candID=candNodes.get(k).getA();
						myNode candNode =vbList.get(candID);
						if(candNode!=null)
							newList.put(candNode.getID(),candNode);
					}
					domain[variableIndex].setList(newList);
				}
				
				ArrayList<MyPair<Integer, Double>> consWith =domain[i].getDistanceConstrainedWith();
				HashMap<Integer, ArrayList<MyPair<Integer, Double>>> nodereachWith= node.getReachableWithNodes(); //Label ~ NodeIDs
				
				for (int j = 0; j < consWith.size(); j++) 
				{
					int variableIndex = consWith.get(j).getA();
					Variable vb = domain[variableIndex];
					HashMap<Integer, myNode> vbList = vb.getList();
					ArrayList<MyPair<Integer, Double>> candNodes= nodereachWith.get(vb.getLabel());
					HashMap<Integer, myNode> newList = new HashMap<Integer, myNode>();
					for (int k = 0; k < candNodes.size(); k++) 
					{
						int candID=candNodes.get(k).getA();
						myNode candNode =vbList.get(candID);
						if(candNode!=null)
							newList.put(candNode.getID(),candNode);
					}
					domain[variableIndex].setList(newList);
				}
			}
		}
		
		
		return domain;
	}
	
	private void search(AssignmentInstance instance)
	{
		int index = sOrder.getNext();
		if(index!=-1)
		{
			Variable currentVB=variables[index];
			ArrayList<MyPair<Integer, Double>> constrainingVariables=currentVB.getDistanceConstrainedWith();
			
			ArrayList<ArrayList<MyPair<Integer, Double>>> candidates= new ArrayList<ArrayList<MyPair<Integer, Double>>>();
			ArrayList<VariableCandidates> variableCandidates= new ArrayList<VariableCandidates>();
			
			//check Validty with constraintVariables
			for (int i = 0; i < constrainingVariables.size(); i++) 
			{
				Variable cnVariable=variables[constrainingVariables.get(i).getA()];
				double edgeLabel = constrainingVariables.get(i).getB();
				int cnVariableIndex=cnVariable.getID();
				myNode cnVariableInstance = instance.getAssignment(cnVariableIndex);
				if(cnVariableInstance!=null)
				{
					candidates.add(cnVariableInstance.getRechableByNodeIDs(currentVB.getLabel(), edgeLabel));
					variableCandidates.add(new VariableCandidates(cnVariableIndex, cnVariableInstance.getRechableByNodeIDs(currentVB.getLabel(), edgeLabel)));
				}
			}
			
			ArrayList<MyPair<Integer, Double>> constrainingBYVariables=currentVB.getDistanceConstrainedBy();
			for (int i = 0; i < constrainingBYVariables.size(); i++) 
			{
				Variable cnVariable=variables[constrainingBYVariables.get(i).getA()];
				double edgeLabel = constrainingBYVariables.get(i).getB();
				int cnVariableIndex=cnVariable.getID();
				myNode cnVariableInstance = instance.getAssignment(cnVariableIndex);
				if(cnVariableInstance!=null)
				{
					candidates.add(cnVariableInstance.getRechableWithNodeIDs(currentVB.getLabel(), edgeLabel));
					variableCandidates.add(new VariableCandidates(cnVariableIndex, cnVariableInstance.getRechableWithNodeIDs(currentVB.getLabel(), edgeLabel)));
				}
			}
			
			
			
			ArrayList<MyPair<Integer, Double>> finalCandidates= Util.getIntersection(candidates);						
			int hasResult=0;
			
			//end check Validty with constraintVariables
			for (int i = 0; i < finalCandidates.size(); i++) 
			{
				int candidateIndex=finalCandidates.get(i).getA();
				myNode candidateNode = currentVB.getList().get(candidateIndex);
				if(candidateNode!=null)
				{
					instance.assign(currentVB.getID(), candidateNode);
					//check identity Validity
					if(AssignmentInstance.ensureIDValidty(instance))
					{
						//proceed with next
						search(instance);
					}
					else
					{
						instance.deAssign(currentVB.getID());
					}
				}
				//End ID Validity
			}
			//after finishing... step back to before state
			sOrder.stepBack();
			instance.deAssign(currentVB.getID());
			
		}
		else// index ==-1 means that I reached the point where the assignment is legal
		{
			//ADD element
			resultCounter++;
			for (int i = 0; i < instance.getAssignmentSize(); i++) 
			{
				myNode nodeInstance=instance.getAssignment(i);
				if(!result[i].getList().containsKey(nodeInstance.getID()))
					result[i].getList().put(nodeInstance.getID(), nodeInstance);
			}
		}
	}
	
	public void searchAll()
	{
		setVariableVisitingOrder(getMaxDegreeVariableIndex()); //set variable visit order
		int index=-1;
		
		index = sOrder.getNext();
		Variable firstVB = variables[index];
		HashMap<Integer,myNode> firstList= firstVB.getList();
		
		int tempCounter=0;
		
		AssignmentInstance instance = new AssignmentInstance(variables.length);
		
		for (Iterator<myNode> iterator = firstList.values().iterator(); iterator.hasNext();)
		{
			myNode firstNode= iterator.next();
			instance.assign(firstVB.getID(), firstNode);
			System.out.println(tempCounter++);
			search(instance);
		}
	}

	//insert vp into order according to their variable values length
	private void insertInOrder(LinkedList<VariablePair> Q, VariablePair vp)
	{
		int i = 0;
		Iterator itr = Q.iterator();
	    while(itr.hasNext())
	    {
	    	VariablePair tempVP = (VariablePair)itr.next();
	    	if(tempVP.getMinValuesLength()>vp.getMinValuesLength())
			{
				Q.add(i, vp);
				return;
			}
	    	i++;
	    }
	    Q.add(i, vp);
	}

	/**Get all of the sub-graph isomorphisms detected*
     * @return all of the isomorphisms detected
     * @from 25 Jan 2019
     * */
    public List<Map<Node, Node>> getIsomorphismList() {
        return isomorphismList;
    }

}
