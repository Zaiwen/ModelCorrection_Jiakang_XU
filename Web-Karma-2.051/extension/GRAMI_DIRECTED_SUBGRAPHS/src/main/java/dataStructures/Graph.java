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

package dataStructures;


import Dijkstra.DijkstraEngine;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.Node;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.awt.*;
import java.io.*;
import java.text.ParseException;
import java.util.*;
import java.util.Map.Entry;

//import utilities.CombinationGenerator;
//import Temp.SubsetReference;


public class Graph 
{

	public final static int NO_EDGE = 0;
	private HPListGraph<Integer, Double> m_matrix;
	private int nodeCount=0;
	private ArrayList<myNode> nodes;
	private HashMap<Integer, HashMap<Integer,myNode>> freqNodesByLabel;
	private HashMap<Integer, HashMap<Integer,myNode>> nodesByLabel;
	private ArrayList<Integer> sortedFreqLabels; //sorted by frequency !!! Descending......
	
	private ArrayList<Point> sortedFreqLabelsWithFreq;
	
	private HashMap<Double, Integer> edgeLabelsWithFreq;
	private ArrayList<Double> freqEdgeLabels;
	
	private int freqThreshold;
	public int getFreqThreshold() {
		return freqThreshold;
	}

	private int m_id;
	
	
	public Graph(int ID, int freqThresh) 
	{
		
		sortedFreqLabels= new ArrayList<Integer>();
		sortedFreqLabelsWithFreq = new ArrayList<Point>();
		
		m_matrix= new HPListGraph<Integer, Double>();
		m_id=ID;
		nodesByLabel= new HashMap<Integer, HashMap<Integer,myNode>>();
		
		freqNodesByLabel= new HashMap<Integer, HashMap<Integer,myNode>>();
		nodes= new ArrayList<myNode>();
		
		edgeLabelsWithFreq = new HashMap<Double, Integer>();
		freqEdgeLabels = new ArrayList<Double>();
		
		freqThreshold=freqThresh;
		
		if(StaticData.hashedEdges!=null)
		{
//			StaticData.hashedEdges = null;
			System.out.println(StaticData.hashedEdges.hashCode());//throw exception if more than one graph was created
		}
		StaticData.hashedEdges = new HashMap<String, HashMap<Integer, Integer>[]>();
	}


	public static Graph DFSCodeToGraph(DFSCode code){
//		try {
//			FileWriter fw = new FileWriter("C:\\D_Drive\\ASM\\DataMatchingMaster\\tmp.lg");
//			fw.write("t # 1\n");
//			fw.write(DFScodeSerializer.serialize(code.getHPlistGraph()));
//			fw.close();
//			Graph graph = new Graph(1, 0);
//			graph.loadFromFile_Ehab("C:\\D_Drive\\ASM\\DataMatchingMaster\\tmp.lg");
//
//			return graph;
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		try {
			Graph graph = new Graph(1, 0);
			graph.loadFromDFSCode(code);
			return graph;
		}catch (Exception e){
			e.printStackTrace();
		}


		return null;
	}


	
	public ArrayList<Integer> getSortedFreqLabels() {
		return sortedFreqLabels;
	}
	
	public ArrayList<Double> getFreqEdgeLabels() {
		return this.freqEdgeLabels;
	}

	public HashMap<Integer, HashMap<Integer,myNode>> getFreqNodesByLabel()
	{
		return freqNodesByLabel;
	}
	
	public void loadFromFile(String fileName) throws Exception
	{
		String text = "";
		final BufferedReader bin = new BufferedReader(new FileReader(new File(fileName)));
		File f = new File(fileName);
		FileInputStream fis = new FileInputStream(f);
		byte[] b = new byte[(int)f.length()];
		int read = 0;
		while (read < b.length) {
		  read += fis.read(b, read, b.length - read);
		}
		text = new String(b);
		final String[] rows = text.split("\n");
		
		// read graph from rows
		// nodes
		int i = 0;
		int numberOfNodes=0;
		for (i = 1; (i < rows.length) && (rows[i].charAt(0) == 'v'); i++) {
			final String[] parts = rows[i].split("\\s+");
			final int index = Integer.parseInt(parts[1]);
			final int label = Integer.parseInt(parts[2]);
			if (index != i - 1) {
				throw new ParseException("The node list is not sorted", i);
			}

			addNode(label);
			myNode n = new myNode(numberOfNodes, label);
			nodes.add(n);
			HashMap<Integer,myNode> tmp = nodesByLabel.get(label);
			if(tmp==null)
			{
				tmp = new HashMap<Integer,myNode>();
				nodesByLabel.put(label, tmp);
			}
			tmp.put(n.getID(), n);
			numberOfNodes++;
		}
		nodeCount=numberOfNodes;
		// edges
		for (; (i < rows.length) && (rows[i].charAt(0) == 'e'); i++) {
			final String[] parts = rows[i].split("\\s+");
			final int index1 = Integer.parseInt(parts[1]);
			final int index2 = Integer.parseInt(parts[2]);
			final int label = Integer.parseInt(parts[3]);
			addEdge(index1, index2, label);
		}
		
		//now prune the infrequent nodes

		for (Iterator<  Entry< Integer, HashMap<Integer,myNode> > >  it= nodesByLabel.entrySet().iterator(); it.hasNext();)
		{
			Entry< Integer, HashMap<Integer,myNode> > ar =  it.next();
			if(ar.getValue().size()>=freqThreshold)
			{
				sortedFreqLabelsWithFreq.add(new Point(ar.getKey(),ar.getValue().size()));
				freqNodesByLabel.put(ar.getKey(), ar.getValue());
			}
		}
		
		Collections.sort(sortedFreqLabelsWithFreq, new freqComparator());
		
		for (int j = 0; j < sortedFreqLabelsWithFreq.size(); j++) 
		{
			sortedFreqLabels.add(sortedFreqLabelsWithFreq.get(j).x);
			
		}
		
		bin.close();		
	}


	public void loadFromDFSCode(DFSCode code) throws Exception{
		String codeStr = "t # 1\n";
		codeStr += DFScodeSerializer.serialize(code.getHPlistGraph());
		String text = "";
		final BufferedReader rows = new BufferedReader(new StringReader(codeStr));

		// read graph from rows
		// nodes
		int counter = 0;
		int numberOfNodes=0;
		String line;
		String tempLine;
		rows.readLine();
		while ((line = rows.readLine()) !=null && (line.charAt(0) == 'v')) {
			final String[] parts = line.split("\\s+");
			final int index = Integer.parseInt(parts[1]);
			final int label = Integer.parseInt(parts[2]);
			if (index != counter) {
				System.out.println(index+" "+counter);
				throw new ParseException("The node list is not sorted", counter);
			}

			addNode(label);
			myNode n = new myNode(numberOfNodes, label);
			nodes.add(n);
			HashMap<Integer,myNode> tmp = nodesByLabel.get(label);
			if(tmp==null)
			{
				tmp = new HashMap<Integer,myNode>();
				nodesByLabel.put(label, tmp);
			}

			tmp.put(n.getID(), n);
			numberOfNodes++;
			counter++;
		}
		nodeCount=numberOfNodes;
		tempLine = line;

		// edges

		//use the first edge line
		if(tempLine.charAt(0)=='e')
			line = tempLine;
		else
			line = rows.readLine();

		if(line!=null)
		{
			do
			{
				final String[] parts = line.split("\\s+");
				final int index1 = Integer.parseInt(parts[1]);
				final int index2 = Integer.parseInt(parts[2]);
				final int label = Integer.parseInt(parts[3]);
				addEdge(index1, index2, label);
			} while((line = rows.readLine()) !=null && (line.charAt(0) == 'e'));
		}

		//prune infrequent edge labels
		for (Iterator<  Entry< Double,Integer> >  it= this.edgeLabelsWithFreq.entrySet().iterator(); it.hasNext();)
		{
			Entry< Double,Integer > ar =  it.next();
			if(ar.getValue().doubleValue()>=freqThreshold)
			{
				this.freqEdgeLabels.add(ar.getKey());
			}
		}

		//now prune the infrequent nodes
		for (Iterator<  Entry< Integer, HashMap<Integer,myNode> > >  it= nodesByLabel.entrySet().iterator(); it.hasNext();)
		{
			Entry< Integer, HashMap<Integer,myNode> > ar =  it.next();
			if(ar.getValue().size()>=freqThreshold)
			{
				sortedFreqLabelsWithFreq.add(new Point(ar.getKey(),ar.getValue().size()));
				freqNodesByLabel.put(ar.getKey(), ar.getValue());
			}
		}

		Collections.sort(sortedFreqLabelsWithFreq, new freqComparator());

		for (int j = 0; j < sortedFreqLabelsWithFreq.size(); j++)
		{
			sortedFreqLabels.add(sortedFreqLabelsWithFreq.get(j).x);
		}

		//prune frequent hashedEdges
		Vector toBeDeleted = new Vector();
		Set<String> s = StaticData.hashedEdges.keySet();
		for (Iterator<String>  it= s.iterator(); it.hasNext();)
		{
			String sig =  it.next();
			HashMap[] hm = StaticData.hashedEdges.get(sig);
			if(hm[0].size()<freqThreshold || hm[1].size()<freqThreshold)
			{
				toBeDeleted.addElement(sig);
			}
			else
				;
		}
		Enumeration<String> enum1 = toBeDeleted.elements();
		while(enum1.hasMoreElements())
		{
			String sig = enum1.nextElement();
			StaticData.hashedEdges.remove(sig);
		}

		rows.close();
	}
	
	public void loadFromFile_Ehab(String fileName) throws Exception
	{
		String text = "";
		final BufferedReader rows = new BufferedReader(new FileReader(new File(fileName)));
		
		// read graph from rows
		// nodes
		int counter = 0;
		int numberOfNodes=0;
		String line;
		String tempLine;
		rows.readLine();
		while ((line = rows.readLine()) !=null && (line.charAt(0) == 'v')) {
			final String[] parts = line.split("\\s+");
			final int index = Integer.parseInt(parts[1]);
			final int label = Integer.parseInt(parts[2]);
			if (index != counter) {
				System.out.println(index+" "+counter);
				throw new ParseException("The node list is not sorted", counter);
			}
			
			addNode(label);
			myNode n = new myNode(numberOfNodes, label);
			nodes.add(n);
			HashMap<Integer,myNode> tmp = nodesByLabel.get(label);
			if(tmp==null)
			{
				tmp = new HashMap<Integer,myNode>();
				nodesByLabel.put(label, tmp);
			}

			tmp.put(n.getID(), n);
			numberOfNodes++;
			counter++;
		}
		nodeCount=numberOfNodes;
		tempLine = line;

		if (tempLine == null){
			return;
		}
		// edges
		
		//use the first edge line
		if(tempLine.charAt(0)=='e')
			line = tempLine;
		else
			line = rows.readLine();
		
		if(line!=null)
		{
			do
			{
				final String[] parts = line.split("\\s+");
				final int index1 = Integer.parseInt(parts[1]);
				final int index2 = Integer.parseInt(parts[2]);
				final int label = Integer.parseInt(parts[3]);
				addEdge(index1, index2, label);
			} while((line = rows.readLine()) !=null && (line.charAt(0) == 'e'));
		}
		
		//prune infrequent edge labels
		for (Iterator<  Entry< Double,Integer> >  it= this.edgeLabelsWithFreq.entrySet().iterator(); it.hasNext();)
		{
			Entry< Double,Integer > ar =  it.next();
			if(ar.getValue().doubleValue()>=freqThreshold)
			{
				this.freqEdgeLabels.add(ar.getKey());
			}
		}
		
		//now prune the infrequent nodes
		for (Iterator<  Entry< Integer, HashMap<Integer,myNode> > >  it= nodesByLabel.entrySet().iterator(); it.hasNext();)
		{
			Entry< Integer, HashMap<Integer,myNode> > ar =  it.next();
			if(ar.getValue().size()>=freqThreshold)
			{
				sortedFreqLabelsWithFreq.add(new Point(ar.getKey(),ar.getValue().size()));
				freqNodesByLabel.put(ar.getKey(), ar.getValue());
			}
		}
		
		Collections.sort(sortedFreqLabelsWithFreq, new freqComparator());
		
		for (int j = 0; j < sortedFreqLabelsWithFreq.size(); j++) 
		{
			sortedFreqLabels.add(sortedFreqLabelsWithFreq.get(j).x);
		}
		
		//prune frequent hashedEdges
		Vector toBeDeleted = new Vector();
		Set<String> s = StaticData.hashedEdges.keySet();
		for (Iterator<String>  it= s.iterator(); it.hasNext();) 
		{
			String sig =  it.next();
			HashMap[] hm = StaticData.hashedEdges.get(sig);
			if(hm[0].size()<freqThreshold || hm[1].size()<freqThreshold)
			{
				toBeDeleted.addElement(sig);
			}
			else
				;
		}
		Enumeration<String> enum1 = toBeDeleted.elements();
		while(enum1.hasMoreElements())
		{
			String sig = enum1.nextElement();
			StaticData.hashedEdges.remove(sig);
		}
		
		rows.close();		
	}


	
	public void printFreqNodes()
	{
		for (Iterator<  Entry< Integer, HashMap<Integer,myNode> > >  it= freqNodesByLabel.entrySet().iterator(); it.hasNext();)
		{
			Entry< Integer, HashMap<Integer,myNode> > ar =  it.next();
			
			System.out.println("Freq Label: "+ar.getKey()+" with size: "+ar.getValue().size());
		}
	}
	
	//1 hop distance for the shortest paths
	public void setShortestPaths_1hop()
	{
		for (Iterator<  Entry< Integer, HashMap<Integer,myNode> > >  it= freqNodesByLabel.entrySet().iterator(); it.hasNext();)
		{
			Entry< Integer, HashMap<Integer,myNode> > ar =  it.next();
			
			HashMap<Integer,myNode> freqNodes= ar.getValue();
			int counter=0;
			for (Iterator<myNode> iterator = freqNodes.values().iterator(); iterator.hasNext();) 
			{
				myNode node =  iterator.next();
				System.out.println(counter++);
				node.setReachableNodes_1hop(this, freqNodesByLabel);
			}
		}
	}
	
	public void setShortestPaths(DijkstraEngine dj)
	{
		for (Iterator<  Entry< Integer, HashMap<Integer,myNode> > >  it= freqNodesByLabel.entrySet().iterator(); it.hasNext();)
		{
			Entry< Integer, HashMap<Integer,myNode> > ar =  it.next();
			
			HashMap<Integer,myNode> freqNodes= ar.getValue();
			int counter=0;
			for (Iterator<myNode> iterator = freqNodes.values().iterator(); iterator.hasNext();) 
			{
				myNode node =  iterator.next();
				dj.execute(node.getID(),null);
				System.out.println(counter++);
				node.setReachableNodes(dj, freqNodesByLabel, this.getListGraph());
			}
		}
	}
	
	public myNode getNode(int ID)
	{
		return nodes.get(ID);
	}
	
	public HPListGraph<Integer, Double> getListGraph()
	{
		return m_matrix;
	}
	public int getID() {
		return m_id;
	}
	
	public int getDegree(int node) {

		return m_matrix.getDegree(node);
	}
		
	public int getNumberOfNodes()
	{
		return nodeCount;
	}
	
	 
	public int addNode(int nodeLabel) {
		return m_matrix.addNodeIndex(nodeLabel);
	}

	/**add myNodes. added on 17 August 2018.**/
	public void addNode(myNode node){

		nodes.add(node);
	}

	/**set frequent nodes by label. added on 17 Aug 2018.**/
	public void setFreqNodesByLabel(HashMap<Integer, HashMap<Integer, myNode>> freqNodesByLabel) {

		this.freqNodesByLabel = freqNodesByLabel;
	}

	public int addEdge(int nodeA, int nodeB, double edgeLabel)
	{
		Integer I = edgeLabelsWithFreq.get(edgeLabel);
		if(I==null)
			edgeLabelsWithFreq.put(edgeLabel, 1);
		else
			edgeLabelsWithFreq.put(edgeLabel, I.intValue()+1);

		//add edge frequency
		int labelA = nodes.get(nodeA).getLabel();
		int labelB = nodes.get(nodeB).getLabel();

		String hn;

		hn = labelA+"_"+edgeLabel+"_"+labelB;

		HashMap<Integer,Integer>[] hm = StaticData.hashedEdges.get(hn);
		if(hm==null)
		{
			hm = new HashMap[2];
			hm[0] = new HashMap();
			hm[1] = new HashMap();

			StaticData.hashedEdges.put(hn, hm);
		}
		else
		{}
		hm[0].put(nodeA, nodeA);
		hm[1].put(nodeB, nodeB);

		Double d = new Double(edgeLabel);
		return m_matrix.addEdgeIndex(nodeA, nodeB, d, 1);
	}

	/**Fill in a graph with elements from a directed multiple weighed graph.
     * @created 17 Aug 2018
     * @revised 17 Feb 2019.*
	 * @param directedWeightedMultigraph received graph normally it might be an entity linking graph
	 * @param  graph graph to be enriched
     * @return    a hash map to save the the correspondences. Key: the node index in GraMi; Value: the corresponding id in the original 'directedWeighedMultiGraph'
	 * */
	public static Map<Integer,Integer> loadDirectedWeightedMultigraph(DirectedWeightedMultigraph<edu.isi.karma.rep.alignment.Node, LabeledLink> directedWeightedMultigraph, Graph graph){

		/**get all of the nodes from the directed multiple weighted graph**/
		Set<edu.isi.karma.rep.alignment.Node> nodeSet = directedWeightedMultigraph.vertexSet();
		/**creates a hash map to save index and label of all the nodes in the GraMi graph. **/
		Map<Integer,String> allNodesInGramiGraphMap = new HashMap<Integer, String>();//<Key, Value> Key: the index of the nodes in the GraMi graph. Value: label of the nodes in the GraMi graph
		/**creates a hash map to save the initial index in the 'directedWeightedMultiGraph' and the current index in the GraMi graph**/
		Map<Integer,Integer> indexCorrespondence = new HashMap<Integer, Integer>();//<Key,Value> Key: the initial index, value: the current index
		/**creates a hash map to save all the new nodes in the GraMi graph and their indices**/
		Map<Integer,myNode> myNodeMap = new HashMap<Integer,myNode>();//<Key,Value> Key: the current index(id) of the myNode, value: myNode
		/**Create a hash map to save the the correspondences. Key: the node index in GraMi; Value: the corresponding id in the original 'directedWeighedMultiGraph'**/
		Map<Integer,Integer> reverseIndexCorrespondence = new HashMap<>();

		/**add nodes to the GraMi graph**/
		Iterator<edu.isi.karma.rep.alignment.Node> iterator = nodeSet.iterator();
		while (iterator.hasNext()){
			edu.isi.karma.rep.alignment.Node node = iterator.next();
			String label = node.getUri();//get label of node
			String initialIndex = node.getId();//get the initial label of a node
			int index = graph.addNode(Integer.valueOf(label));//add a node into the GraMi graph, and output an index
			allNodesInGramiGraphMap.put(index,label);//current index and table
			indexCorrespondence.put(Integer.valueOf(initialIndex),index);
			reverseIndexCorrespondence.put(index,Integer.valueOf(initialIndex));//added on 17 Feb 2019
			myNode u = new myNode(index,Integer.valueOf(label));//creates new GraMi node
			myNodeMap.put(index,u);
			graph.addNode(u);//add this myNode into the graph
		}

		/**get all of the edges from the directed multiple weighted graph, and add them to the GraMi graph**/
		Set<LabeledLink> edgeSet = directedWeightedMultigraph.edgeSet();
		Iterator<LabeledLink> iterator1 = edgeSet.iterator();
		while (iterator1.hasNext()){
			LabeledLink labeledLink = iterator1.next();
			String edgeLabel = labeledLink.getUri();//get edge label
			edu.isi.karma.rep.alignment.Node initialStart = labeledLink.getSource();//get the initial start of an edge
			Node initialTarget = labeledLink.getTarget();//get the initial target of an edge
			Integer startId = indexCorrespondence.get(Integer.valueOf(initialStart.getId()));//get the current start id of the edge
			Integer targetId = indexCorrespondence.get(Integer.valueOf(initialTarget.getId()));//get the current target id of the edge
			myNode start = myNodeMap.get(startId);//get the current start node of the edge
			myNode target = myNodeMap.get(targetId);//get the current target node of the edge
			start.addreachableNode(target,Integer.valueOf(edgeLabel));//link the source node and the target node in the GraMi graph. This line is different from approximate subgraph matching. 17 Aug 2018.
			graph.addEdge(startId,targetId,Integer.valueOf(edgeLabel));//add this edge into the GraMi graph.
		}

		/**get all of node labels of GraMi graph**/
		Collection<String> c = allNodesInGramiGraphMap.values();//extract all of labels in the GraMi graph
		Set<String> labelSet = new HashSet<String>();//remove duplicated
		Iterator<String> iterator2 = c.iterator();
		while (iterator2.hasNext()){
			String label = iterator2.next();
			labelSet.add(label);
		}
		/**compute frequent nodes by label**/
		HashMap<Integer,HashMap<Integer,myNode>> freqNodesByLabel = new HashMap<Integer, HashMap<Integer, myNode>>();//creates an new empty map

		Set set = allNodesInGramiGraphMap.entrySet();
		Iterator iterator3 = set.iterator();
		while (iterator3.hasNext()){
			Map.Entry m = (Map.Entry)iterator3.next();
			Integer index = (Integer) m.getKey();//get index(id) of a node
			String label = (String)m.getValue();//get label of a node
			HashMap<Integer,myNode> value = freqNodesByLabel.get(Integer.valueOf(label));//check if this label exists in 'freqNodesByLabel'
			if(value != null){
				//Key is present
				value.put(index,myNodeMap.get(index));
			}else {
				//No such Key
				HashMap<Integer,myNode> nodeHashMap = new HashMap<Integer, myNode>();
				nodeHashMap.put(index,myNodeMap.get(index));
				freqNodesByLabel.put(Integer.valueOf(label),nodeHashMap);
			}
		}
		graph.setFreqNodesByLabel(freqNodesByLabel);//add 'freqNodesByLabel' to the GraMi graph
        return reverseIndexCorrespondence;
	}



}
