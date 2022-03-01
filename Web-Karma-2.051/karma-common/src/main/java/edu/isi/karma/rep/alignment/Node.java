/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/
package edu.isi.karma.rep.alignment;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rits.cloning.Cloner;

import edu.isi.karma.modeling.Uris;
import edu.isi.karma.util.RandomGUID;

public abstract class Node implements Comparable<Node>, Cloneable {

	static Logger logger = LoggerFactory.getLogger(Node.class);

	private String id;
	private Label label;
	private NodeType type;
	private Set<String> modelIds;
	protected boolean isForced;
	private Map<String, Integer> outgoingLinks;//added on 27 Aug 2018. The original outgoing 'Stub's table of this node. For knowledge graph generation.
    private Map<String, Integer> incomingLinks;//added on 9 Sep 2018. The original incoming 'Stub's table of this node. For knowledge graph generation.
    private Map<String, Integer> currentOutStubs;//added on 22 Oct 2018. Save the current outgoing 'Stub's table of this node.
    private Map<String, Integer> currentInStubs;//added on 22 Oct 2018. Save the current incoming 'Stub's table of this node.
    private Map<String, Boolean> stubStatus;//added on 16 Nov 2018. indicating if a stub type has been used or not.


    private List<Node> shortestPath = new LinkedList<>();//used for Dijkstra Algorithm. Added on 16 Oct 2018.
    private Integer distance = Integer.MAX_VALUE;//used for Dijkstra Algorithm. Added on 16 Oct 2018
    private Map<Node, Integer> adjacentNodes = new HashMap<>();//used for Dijkstra Algorithm. Added on 16 Oct 2018

    private Map<String, Object> attributeValueMap = new HashMap<>();//attribute value of the entity. Added on 10 Dec 2018
    private String entityType = null;//for generating museum knowledge graph. Added on 8 April 2019
	
	public Node(String id, Label label, NodeType type) {
		
		this.init();
		if (id != null && id.trim().length() > 0) this.id = id;
		if (label != null) this.label = label;
		if (type != null) this.type = type;
	}
	
	public Node(Node v) {
		if (v == null) this.init();
		else {
			this.id = v.id;
			this.label = v.label;
			this.type = v.type;
		}
	}
	
	private void init() {
		this.id = new RandomGUID().toString();
		Label l = null;
		this.label = new Label(l);
		this.type = NodeType.None;
		this.modelIds = new HashSet<>();
		this.outgoingLinks = new HashMap<>();//added on 9 Sep 2018. For knowledge graph generation.
        this.incomingLinks=new HashMap<>();//added on 8 Sep 2018. For knowledge graph generation.
        this.currentInStubs=new HashMap<>();//added on 22 Oct 2018.
        this.currentOutStubs=new HashMap<>();//added on 22 Oct 2018
        this.stubStatus=new HashMap<>();//added on 16 Nov 2018
        this.entityType = new String("");//added on 8 April 2019
	}
	
	public String getId() {
		return this.id;
	}

	/**added on 7 Aug 2018. for generating random big graph.**/
	public void setId(String id) {this.id = id;}
	
	public Label getLabel() {
		return this.label;
	}
	
	public String getUri() {
		if (this.label != null)
			return this.getLabel().getUri();
		return Uris.DEFAULT_NODE_URI;
	}
	
	public String getLocalId() {
		
		String s = this.id;

		if (this.label.getNs() != null)
			s = s.replaceAll(this.label.getNs(), "");
		
		return s;
	}
	
	public String getDisplayId() {
		
		if (this.label.getPrefix() == null)
			return this.getLocalId();
		
		return this.label.getPrefix() + ":" + this.getLocalId();
	}
	
	public NodeType getType() {
		return type;
	}
	
	public Set<String> getModelIds() {
		if (this.modelIds == null)
			return new HashSet<>();
		return modelIds;
	}

	public void setModelIds(Set<String> patternIds) {
		this.modelIds = patternIds;
	}

	@Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        Node node = (Node) obj;
        return this.id.equals(node.getId());
    }
    
    @Override
    public int hashCode() {
    	return this.getId().hashCode();
    }

    @Override
    public int compareTo(Node node) {       
        //compare id
        return this.id.compareTo(node.getId());
    }

    public boolean isForced() {
    	return isForced;
    }
    
    public void setForced(boolean value) {
    	isForced = value;
    }
    
    public Node clone() {

    	Cloner cloner = new Cloner();
    	return cloner.deepClone(this);

//    	switch (this.type) {
//			case None: return new SimpleNode(this.getId(), this.getLabel()); 
//			case ColumnNode: return new ColumnNode(this.getId(), ((ColumnNode)this).getHNodeId(), ((ColumnNode)this).getColumnName()); 
//			case LiteralNode: return new LiteralNode(this.getId(), ((LiteralNode)this).getValue(), ((LiteralNode)this).getDatatype()); 
//			case InternalNode: return new InternalNode(this.getId(), this.getLabel());
//		}
//
//		logger.error("Cloning the node has been failed. Cannot identify the type of the node.");
//		return null;
    }

    /**added on 27 Aug 2018**/
    public Map<String,Integer> getIncomingLinks(){
        return this.incomingLinks;
    }

    /**added on 27 Aug 2018**/
    public void setIncomingLinks(Map<String,Integer> incomingLinks){
        this.incomingLinks = incomingLinks;
    }

    /**added on 22 Oct 2018**/
    public void addIncomingLinks(String relationship, Integer number){
        this.incomingLinks.put(relationship,number);
    }

    /**added on 8 Sep 2018**/
    public Map<String,Integer> getOutgoingLinks(){
        return this.outgoingLinks;
    }

    /**added on 8 Sep 2018**/
    public void setOutgoingLinks(Map<String,Integer> outgoingLinks){
        this.outgoingLinks=outgoingLinks;
    }

    /**added on 22 Oct 2018**/
    public void addOutgoingLinks(String relationship, Integer number){
        this.outgoingLinks.put(relationship,number);
    }

    /**added on 22 Oct 2018**/
    public Map<String, Integer> getCurrentInStubs() {return this.currentInStubs;}

    /**added on 8 April 2019**/
    public void setEntityType (String entityType) {this.entityType = entityType;}

    /**added on 8 April 2019**/
    public String getEntityType () {return this.entityType;}

    public void setCurrentInStubs(Map<String,Integer> currentInStubs) {this.currentInStubs=currentInStubs;}

    public void addToCurrentInStubs(String relationship, Integer number){ this.currentInStubs.put(relationship, number); }

    public Map<String, Integer> getCurrentOutStubs() {return this.currentOutStubs;}

    public void setCurrentOutStubs(Map<String, Integer> currentOutStubs) {this.currentOutStubs=currentOutStubs;}

    public void addToCurrentOutStubs(String relationship, Integer number){ this.currentOutStubs.put(relationship,number); }

    /**see if a stub type has been used or not so far*
     * @From 16 Nov 2018
     *@param stubType a certain stub type of the source. E.g. there are a couple of stub types for a person, such as lives in, employs...
     * */
    public boolean getStubStatus (String stubType) {
        boolean isUsed ;
        isUsed = stubStatus.get(stubType);
        return isUsed;
    }

    /**alter the using status of a stub *
     * @From 16 Nov 2018
     *@param stub a certain stub type
     *@param isUsed if this stub type has been used (or partially used) or not
     * */
    public void addStubStatus (String stub, boolean isUsed) {
        this.stubStatus.put(stub,isUsed);
    }

    /**Decrease the # of in-stubs 1 for a specified relationship type*
     * @param relationship a certain in-stub type
     *                     @revised 16 Nov 2018
     * */
    public Map<String, Integer> decreaseInStubs(String relationship) throws Exception{
        if(this.currentInStubs.get(relationship) > 0){
            this.currentInStubs.put(relationship, this.currentInStubs.get(relationship)-1);
            if (!this.getStubStatus(relationship)) {//if this in-stub has not been used before decreasing it
                this.addStubStatus(relationship,true);//true-meaning the in-stub is used now
            }else {//if this stub has been used
                //do nothing
            }
            return this.currentInStubs;
        }else{
            throw new Exception("There are not enough in-stubs to be run out!");
        }
    }

    /**Decrease # of out-stubs 1 for a specified relationship type*
     * @param relationship a certain out-stub type
     * @revised on 3 Dec 2018
     * */
    public Map<String, Integer> decreaseOutStubs(String relationship) throws Exception{
        if(this.currentOutStubs.get(relationship) > 0){
            this.currentOutStubs.put(relationship, this.currentOutStubs.get(relationship)-1);
            if (!this.getStubStatus(relationship)) {//if this out-stub has not been used before decreasing it
                this.addStubStatus(relationship,true);//true - means this out-stub is used now
            }else {//if this stub has been used
                //do nothing
            }
            return this.currentOutStubs;
        }else {
            throw new Exception("There are not enough in-stubs to be run out!");
        }
    }

    /**return all of the current stubs of an entity.
     * @From 19 Oct 2018*
     * @revised on 22 Oct 2018
     * */
    public Map<String,Integer> getAllCurrentStubs(){
        Map<String,Integer> allLinks = new HashMap<>();
        allLinks.putAll(currentInStubs);
        allLinks.putAll(currentOutStubs);
        return allLinks;
    }

    /**return all of the original stubs of an entity.*
     * @From 16 Nov 2018
     * */
    public Map<String, Integer> getAllOriginalStubs(){
        Map<String, Integer> allLinks = new HashMap<>();
        allLinks.putAll(incomingLinks);
        allLinks.putAll(outgoingLinks);
        return allLinks;
    }


    /**added on 16 Oct 2018**/
    public void addDestination(Node destination, int distance){
        adjacentNodes.put(destination, distance);
    }

    /**added on 16 Oct 2018**/
    public void setDistance(int distance){this.distance = distance;}

    /**added on 16 Oct 2018**/
    public Integer getDistance(){return this.distance;}

    /**added on 16 Oct 2018**/
    public void setShortestPath(List<Node> shortestPath){this.shortestPath = shortestPath;}

    /**added on 16 Oct 2018**/
    public List<Node> getShortestPath(){return this.shortestPath;}

    /**added on 16 Oct 2018**/
    public void setAdjacentNodes(Map<Node, Integer> adjacentNodes){this.adjacentNodes = adjacentNodes;}

    /**added on 16 Oct 2018**/
    public Map<Node, Integer> getAdjacentNodes(){return this.adjacentNodes;}

    /**add attribute value to an attribute*
     * @param attributeType the type of the attribute
     * @param attributeValue the value of the attribute
     * @from 10 Dec 2018
     * */
    public void addAttributeValue (String attributeType, Object attributeValue) {
        this.attributeValueMap.put(attributeType,attributeValue);
    }

    /**get attribute value with regard to an attribute*
     * @param attributeType type of attribute
     * @return value of this attribute
     * @from 10 Dec 2018
     * */
    public Object getAttributeValue (String attributeType) {
        return this.attributeValueMap.get(attributeType);
    }

    /**get all the attributes with regard to an entity*
     * @return attribute value map
     * @from 12 April 2019
     * */
    public Map<String, Object> getAttributeValueMap () {
        return this.attributeValueMap;
    }

    /**SET label of each node*
     * @from 1 May 2019
     * @param uri updated uri of node
     * */
    public void setLabel (String uri) {

        label.setUri(uri);
    }

}
