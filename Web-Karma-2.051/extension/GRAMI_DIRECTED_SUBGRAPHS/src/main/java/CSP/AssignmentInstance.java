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


 @Adapted by Zaiwen Feng @University of South Australia
 */

package CSP;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import dataStructures.myNode;
import edu.isi.karma.rep.alignment.Node;

public class AssignmentInstance 
{

	private Integer minCOST;
	public int getMinCOST() {
		return minCOST;
	}

	public void setMinCOST(int minCOST) {
		this.minCOST = minCOST;
	}
	private myNode[] assignments;
	private Integer[] assignmentOrder;
	private int order=0;
	/**record an isomorphism (including some node mappings) from the pattern graph to the data graph*
     * @from 22 Jan 2019
     * @revised 26 Jan 2019
     * Key: Node in a pattern graph. Value: the corresponding node in the data graph
     * */
	private Map<Node, Node> isomorphisms = new HashMap<>();
	
	public AssignmentInstance(int size) 
	{
		assignments= new myNode[size];
		assignmentOrder= new Integer[size];
		for (int i = 0; i < assignments.length; i++) 
		{
			assignments[i]=null;
		}
		
	}
	
	public int getAssignnedValuesSize()
	{
		int counter=0;
		for (int i = 0; i < assignments.length; i++) 
		{
			if(assignments[i]!=null)
				counter++;
		}
		return counter;
	}
	
	
	public int getAssignmentSize()
	{
		return assignments.length;
	}
	
	public void assign(int index, myNode node)
	{
		assignments[index]=node;
		assignmentOrder[index]=order;
		order++;
	}
	
	public void deAssign(int index)
	{
		assignments[index]=null;
		assignmentOrder[index]=null;
		order--;
	}

	/**clear an instance*
     * @revised 22 Jan 2019
     * */
	public void clear()
	{
		for (int i = 0; i < assignments.length; i++) 
		{
			deAssign(i);
		}
		order=0;
		minCOST=null;
		isomorphisms.clear();//added on 22 Jan 2019
	}
	
	public myNode getAssignment(int index)
	{
		return assignments[index];
	}

	/**print a legal assignment*
     * @revised 22 Jan 2019
     * */
	public void printInstance()
	{
		System.out.print("Assignment: ");
		for (int i = 0; i < assignments.length; i++) 
		{
			myNode node= assignments[i];
			System.out.print(node.getID()+",("+node.getLabel()+")   ");
		}
		System.out.println("The subgraph isomorphism is: ");
        Iterator iterator = isomorphisms.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Node, Node> pair = (Map.Entry<Node,Node>) iterator.next();
            Node node1 = pair.getKey();
            Node node2 = pair.getValue();
            System.out.println("node: " + node1.getId() + " in pattern ----- node : " + node2.getId() + " in data graph" );
        }

	}
	
	public static boolean ensureIDValidty(AssignmentInstance ass)
	{
		HashSet<Integer> container= new HashSet<Integer>();
		for (int i = 0; i < ass.getAssignmentSize(); i++) 
		{
			myNode n= ass.getAssignment(i);
			if(n==null)
				continue;
			int ID = n.getID();
			if(container.contains(ID))
				return false;
			container.add(ID);
		}
		return true;
	}
	@Override
	public String toString() 
	{
		String rep="";
		
		for (int i = 0; i < assignments.length; i++) 
		{
			myNode node=assignments[i];
			if(node==null)
				rep+=" _";
			else
				rep+=" "+node.getID()+"("+assignmentOrder[i]+")";
		}
		
		return rep;
	}

	/**add a correspondence into isomorphism*
     * @from 22 Jan 2019
     * @revised 26 Jan 2019
     * @param nodeInPattern node in the pattern graph
     * @param nodeInData node in the data graph
     * */
	public void addIsomorphism (Node nodeInPattern, Node nodeInData) {
	    isomorphisms.put(nodeInPattern, nodeInData);
    }

    /**Get all of the isomorphisms with regard to this assignment instance*
     * @from 25 Jan 2019
     * @revised 26 Jan 2019
     * @return all of the isomorphisms with regard to this assignment instance
     * */
    public Map<Node, Node> getIsomorphisms () {return isomorphisms;}

}
