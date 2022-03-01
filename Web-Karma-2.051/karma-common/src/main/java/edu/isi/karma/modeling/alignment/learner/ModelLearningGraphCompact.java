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

package edu.isi.karma.modeling.alignment.learner;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.ModelingParams;
import edu.isi.karma.modeling.alignment.GraphUtil;
import edu.isi.karma.modeling.alignment.GraphVizLabelType;
import edu.isi.karma.modeling.alignment.GraphVizUtil;
import edu.isi.karma.modeling.alignment.LinkIdFactory;
import edu.isi.karma.modeling.alignment.SemanticModel;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.research.Params;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.DefaultLink;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.LinkStatus;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.NodeSupportingModelsComparator;
import edu.isi.karma.util.RandomGUID;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;

public class ModelLearningGraphCompact extends ModelLearningGraph {

	private static Logger logger = LoggerFactory.getLogger(ModelLearningGraphCompact.class);
	
	public ModelLearningGraphCompact(OntologyManager ontologyManager) throws IOException {
		super(ontologyManager, ModelLearningGraphType.Compact);
	}
	
	public ModelLearningGraphCompact(OntologyManager ontologyManager, boolean emptyInstance) {
		super(ontologyManager, emptyInstance, ModelLearningGraphType.Compact);
	}
	
	private void addInternalNodes(SemanticModel model, Set<InternalNode> addedNodes) {
	
		if (model == null || model.getGraph() == null) 
			return;
		
		if (addedNodes == null) addedNodes = new HashSet<>();

		HashMap<String, Integer> uriCount = new HashMap<>();
		for (Node n : model.getGraph().vertexSet()) {
			if (n instanceof InternalNode) {
				Integer count = uriCount.get(n.getUri());
				if (count == null) uriCount.put(n.getUri(), 1);
				else uriCount.put(n.getUri(), count.intValue() + 1);
			}
		}
		
		for (Map.Entry<String, Integer> stringIntegerEntry : uriCount.entrySet()) {
			int modelNodeCount = stringIntegerEntry.getValue();
			Set<Node> matchedNodes = this.graphBuilder.getUriToNodesMap().get(stringIntegerEntry.getKey());
			int graphNodeCount = matchedNodes == null ? 0 : matchedNodes.size();
			
			for (int i = 0; i < modelNodeCount - graphNodeCount; i++) {
				String id = this.nodeIdFactory.getNodeId(stringIntegerEntry.getKey());
				Node n = new InternalNode(id, new Label(stringIntegerEntry.getKey()));
				if (this.graphBuilder.addNode(n))
					addedNodes.add((InternalNode)n);
			}
		}		
	}
	
	private HashMap<Node,Node> getInternalNodeMapping(SemanticModel model) {
		
		HashMap<Node,Node> internalNodeMapping =
				new HashMap<>();
		
		HashMap<String, List<Node>> uriMatchedNodes =
				new HashMap<>();
		
		String uri;
		List<Node> sortedNodes = new ArrayList<>();
		for (Node n : model.getGraph().vertexSet()) {
			sortedNodes.add(n);
		}
		Collections.sort(sortedNodes);
		for (Node n : sortedNodes) {
			if (n instanceof InternalNode) {
				uri = n.getUri();
				List<Node> sortedMatchedNodes = uriMatchedNodes.get(uri);
				if (sortedMatchedNodes == null) {
					sortedMatchedNodes = new ArrayList<>();
					Set<Node> matchedNodes = this.graphBuilder.getUriToNodesMap().get(uri);
					if (matchedNodes != null) sortedMatchedNodes.addAll(matchedNodes);
					Collections.sort(sortedMatchedNodes, new NodeSupportingModelsComparator());
					uriMatchedNodes.put(uri, sortedMatchedNodes);
				}
			}
		}

		for (Node n : sortedNodes) {
			if (n instanceof InternalNode) {
				List<Node> sortedMatchedNodes = uriMatchedNodes.get(n.getUri());
				internalNodeMapping.put(n, sortedMatchedNodes.get(0));
				sortedMatchedNodes.remove(0);
			}
		}

		return internalNodeMapping;
	}
	
	private HashMap<Node,Node> getColumnNodeMapping(SemanticModel model, 
			HashMap<Node,Node> internalNodeMapping) {
		
		if (model == null || model.getGraph() == null) 
			return null;
		
		if (internalNodeMapping == null || internalNodeMapping.isEmpty()) 
			return null;

		HashMap<String,List<Node>> dataPropertyColumnNodes = new HashMap<>();
		
		HashMap<Node,Node> columnNodeMapping = new HashMap<>();
		
		for (Node n : model.getGraph().vertexSet()) {
			if (n instanceof ColumnNode) {
				ColumnNode c = (ColumnNode)n;
				Set<LabeledLink> domainLinks = GraphUtil.getDomainLinksInLabeledGraph(model.getGraph(), (ColumnNode)n);
				if (domainLinks == null || domainLinks.isEmpty())
					continue;
				for (LabeledLink l : domainLinks) {
					if (l.getSource() == null) continue;
					Node domain = l.getSource();
					Node mappedNode = internalNodeMapping.get(domain);
					String linkUri =  l.getUri(); 
					String key =  mappedNode.getId() + linkUri;
					
					List<Node> sortedMatchedNodes;
					if (dataPropertyColumnNodes.containsKey(key)) { 
						sortedMatchedNodes = dataPropertyColumnNodes.get(key);
					} else {
						Set<Node> matchedColumnNodes = this.graphBuilder.getNodeDataProperties().get(key);
						sortedMatchedNodes = new ArrayList<>();
						if (matchedColumnNodes != null) sortedMatchedNodes.addAll(matchedColumnNodes);
						Collections.sort(sortedMatchedNodes, new NodeSupportingModelsComparator());
						dataPropertyColumnNodes.put(key, sortedMatchedNodes);
					}
					
					if (sortedMatchedNodes.isEmpty()) {
						ColumnNode newNode = new ColumnNode(new RandomGUID().toString(), 
								c.getHNodeId(), c.getColumnName(), c.getRdfLiteralType(),
								c.getLanguage());
						if (this.graphBuilder.addNode(newNode)) {
							columnNodeMapping.put(n, newNode);
						}
					} else {
						columnNodeMapping.put(n, sortedMatchedNodes.get(0));
						sortedMatchedNodes.remove(0);
					}
				}
			}
		}
		return columnNodeMapping;
	}

	/**Add new edges into the current model learning graph. At the same time, update the weight of all the existing edges. 5 June 2018.*
     * @param model the new model to be added
     * @param internalNodeMapping the node mapping between the current model learning graph and the new model to be added.
     * @param columnNodeMapping ???
     * @param weightSystem the chosen weight assigning algorithm.
     * */
	private void addLinks(SemanticModel model, 
			HashMap<Node, Node> internalNodeMapping, 
			HashMap<Node, Node> columnNodeMapping,
			PatternWeightSystem weightSystem) {
		
		if (model == null) 
			return;

		/**This set is used to contain all the edges where the weight has been updated. 6 Nov 2017.**/
		Set<String> updatedEdgesId = new TreeSet<>();
		
		String modelId = model.getId();

		Node source, target;
		Node n1, n2;

		HashMap<Node,Node> mapping = new HashMap<>();
		if (internalNodeMapping != null) mapping.putAll(internalNodeMapping);
		if (columnNodeMapping != null) mapping.putAll(columnNodeMapping);
		/**Iterate every edge in the semantic model to be incorporated. 7 June 2018.**/
		for (LabeledLink e : model.getGraph().edgeSet()) {
			
			source = e.getSource();
			target = e.getTarget();
			/**Try to get the corresponding start node of this edge in the existing merged model. 7 June 2018.**/
			n1 = mapping.get(source);
			if (n1 == null) {
//					logger.warn("the mappings does not include the source node " + source.getId());
				continue;
			}
			/**Try to get the corresponding end node of this edge in the existing merged model. 7 June 2018.**/
			n2 = mapping.get(target);
			if (n2 == null) {
//					logger.warn("the mappings does not include the target node " + target.getId());
				continue;
			}
			
			String id = LinkIdFactory.getLinkId(e.getUri(), n1.getId(), n2.getId());
			LabeledLink l = this.graphBuilder.getIdToLinkMap().get(id);/**Try to get the corresponding edge in the existing merged model. 7 June 2018.**/

			/**The edge corresponding to 'e' (i.e. 'l') has already existed in the merged graph. **/
			if (l != null) {
				int numOfPatterns = l.getModelIds().size();/**Get the number of tags on the 'l'**/

				if (weightSystem == PatternWeightSystem.OriginalWeights) {
					double currentW = l.getWeight();
					double newW = model.getGraph().getEdgeWeight(e);
					if (newW < currentW)
						this.graphBuilder.changeLinkWeight(l, newW);
				}

				/**Our prototype uses 'JWSPaperFormula' weight system. **/
				else if (weightSystem == PatternWeightSystem.JWSPaperFormula) {


                    this.graphBuilder.changeLinkWeight(l,
                            ModelingParams.PATTERN_LINK_WEIGHT - ((double) (numOfPatterns + 1) / (double) (this.totalNumberOfKnownModels + 1) ));

                    updatedEdgesId.add(l.getId());/**mark this edge because the weight of this edge has already been updated. 7 June 2018.**/

				}

				else {
					this.graphBuilder.changeLinkWeight(l, ModelingParams.PATTERN_LINK_WEIGHT);
				}
				l.getModelIds().add(modelId);
				n1.getModelIds().add(modelId);
				n2.getModelIds().add(modelId);
			}
			/**The edge 'e' only appears in the new semantic model to be added, and the corresponding edge never appears in the current merged graph. I.e.
             * We incorporate this new edge into the merged graph. 5 June 2018.**/
			else {
//					System.out.println("added links: " + i);
//					i++;

                int numOfPatterns = 1;/**The value is 1 because the edge is new in the merged graph. 6 June 2018.**/
				LabeledLink link = e.copy(id);/**Copy a totally same edge as 'e'. 7 June 2018.**/
				
				if (link == null) {
		    		logger.error("cannot instanciate a link from the type: " + e.getType().toString());
		    		continue;
				}
				link.setStatus(LinkStatus.Normal); // all the links in learning graph are normal

				if (link.getModelIds() != null)
					link.getModelIds().clear();
				link.getModelIds().add(modelId);/**add tag (e.g. ds1, ds2...) on the edge. 6 June 2018.**/

				if (weightSystem == PatternWeightSystem.OriginalWeights) {

					if (!this.graphBuilder.addLink(n1, n2, link, model.getGraph().getEdgeWeight(e)))
                        continue;

				}

				/**Assign weight to each new edge that is newly incorporated to the merged graph. 6 June 2018.**/
				else {
					if (this.graphBuilder.addLink(n1, n2, link, ModelingParams.PATTERN_LINK_WEIGHT -
                            ((double) (numOfPatterns ) / (double) (this.totalNumberOfKnownModels + 1) ))) {

                        updatedEdgesId.add(link.getId());/**Mark this edge because the weight of edge has been already updated. 6 Nov 2017.**/
                        n1.getModelIds().add(modelId);
                        n2.getModelIds().add(modelId);
                        continue;
                    }
				}
			}
		}

        HashMap<String, LabeledLink> labeledLinkHashMap = this.graphBuilder.getIdToLinkMap();

        /**Update the weight of the edges that already existed in the merged graph except the edges that has already updated in this round. 6 Nov 2017.**/
        Iterator iterator = labeledLinkHashMap.entrySet().iterator();
        while (iterator.hasNext()) {

            Map.Entry pair = (Map.Entry)iterator.next();

            String edgeId = (String) pair.getKey();
            LabeledLink l = (LabeledLink) pair.getValue();
            /**if the weight of this edge has already been updated..**/
            if (updatedEdgesId.contains(edgeId)) {

                continue;
            }
            /**If the edge has not been updated yet, update its weight...**/
            else {
                int numOfPatterns = l.getModelIds().size();
                this.graphBuilder.changeLinkWeight(l, ModelingParams.PATTERN_LINK_WEIGHT -
                        ((double) (numOfPatterns ) / (double) (this.totalNumberOfKnownModels + 1) ));
            }

        }

		/**Delete it from V3.0**/
//		DefaultLink[] graphLinks = this.graphBuilder.getGraph().edgeSet().toArray(new DefaultLink[0]);
//		for (DefaultLink e : graphLinks) {
//			source = e.getSource();
//			target = e.getTarget();
//			if (source instanceof InternalNode &&
//					target instanceof InternalNode &&
//					e instanceof LabeledLink) {
//				LabeledLink l = (LabeledLink)e;
//				Set<Node> nodesWithSourceUri = this.graphBuilder.getUriToNodesMap().get(source.getUri());
//				Set<Node> nodesWithTargetUri = this.graphBuilder.getUriToNodesMap().get(target.getUri());
//				if (nodesWithSourceUri == null || nodesWithTargetUri == null) continue;
//				for (Node nn1 : nodesWithSourceUri) {
//					for (Node nn2 : nodesWithTargetUri) {
//						if (nn1.equals(source) && nn2.equals(target)) continue;
//						if (nn1.equals(nn2)) continue;
//						String id = LinkIdFactory.getLinkId(l.getUri(), nn1.getId(), nn2.getId());
//						LabeledLink newLink = l.copy(id);
//						newLink.setModelIds(null);
//						this.graphBuilder.addLink(nn1, nn2, newLink, ModelingParams.PATTERN_LINK_WEIGHT);
//					}
//				}
//			}
//		}

	}
	
	@Override  /**this function is used to merge two models. 5 June 2018.**/
	public Set<InternalNode> addModel(SemanticModel model, PatternWeightSystem weightSystem) {
				
		// adding the patterns to the graph
		
		if (model == null) 
			return null;
		
//		String modelId = model.getId();
//		if (this.graphBuilder.getModelIds().contains(modelId)) {
//			// FIXME	
//			// we need to somehow update the graph, but I don't know how to do that yet.
//			// so, we rebuild the whole graph from scratch.
//			logger.info("the graph already includes the model and needs to be updated, we re-initialize the graph from the repository!");
//			initializeFromJsonRepository();
//			return null;
//		}

		this.totalNumberOfKnownModels ++;

		// add the model  nodes that are not in the graph
		Set<InternalNode> addedInternalNodes = new HashSet<>();
		this.addInternalNodes(model, addedInternalNodes);/**add all of the internal nodes. 1 June 2019**/
		HashMap<Node, Node> internalNodeMapping = this.getInternalNodeMapping(model);/**get all the mapping of internal nodes between the current model learning graph and the model to be added. 1 June 2019**/

		HashMap<Node, Node> columnNodeMapping = this.getColumnNodeMapping(model, internalNodeMapping);/**all of the column nodes mappings**/
		
		this.addLinks(model, internalNodeMapping, columnNodeMapping, weightSystem);/**add all of the links. 1 June 2019.**/

		this.lastUpdateTime = System.currentTimeMillis();
		return addedInternalNodes;
	}

	public Set<InternalNode> addLodPattern(SemanticModel model) {
		
		Set<InternalNode> addedNodes = new HashSet<>();
		
		if (model == null) 
			return addedNodes;
		
		String modelId = model.getId();

		Node source, target;
		Node n1, n2;
		
		String sourceUri, targetUri, linkUri; 
		String id, key;
		
		Set<String> mappedLinks = new HashSet<>();
		Set<String> mappedNodes = new HashSet<>();

		HashMap<Node,Node> mapping = new HashMap<>();
		
		HashMap<String, List<Node>> uriMatchedNodes = new HashMap<>();

		List<LabeledLink> sortedLinks = new ArrayList<>();
		List<LabeledLink> idSortedLinks = new ArrayList<>();
		idSortedLinks.addAll(model.getGraph().edgeSet());
		Collections.sort(idSortedLinks);
		for (LabeledLink l : idSortedLinks) {
			key = l.getSource().getUri() + l.getUri() + l.getTarget().getUri();
			if (this.graphBuilder.getPatternLinks().get(key) != null) {
				sortedLinks.add(l);
			}
		}
		for (LabeledLink l : idSortedLinks) {
			if (!sortedLinks.contains(l)) {
				sortedLinks.add(l);
			}
		}
		
		sortedLinks.addAll(model.getGraph().edgeSet());
//		System.out.println("new pattern ...");
		for (LabeledLink e : sortedLinks) {
			

			source = e.getSource();
			target = e.getTarget();
			
//			if (model.getId().equals("p4-022E14EC-57EC-9F3B-CBCD-F64FDFE95609")) {
//				System.out.println(GraphUtil.labeledGraphToString(model.getGraph()));
//				System.out.println(e.getId());
//			}
				
//			if (source.getId().contains("E42_Identifier") &&
//					target.getId().contains("E55_Type")) {
//				System.out.println(GraphUtil.labeledGraphToString(model.getGraph()));
//				System.out.println("debug");
//			}
			
			sourceUri = source.getUri();
			targetUri = target.getUri();
			linkUri = e.getUri();
			
			n1 = mapping.get(source);
			n2 = mapping.get(target);

			key = sourceUri + linkUri + targetUri;

//			if (target.getId().contains("E52_Time-Span2")) {
//				System.out.println(GraphUtil.labeledGraphToString(model.getGraph()));
//				System.out.println("debug");
//			}

			List<LabeledLink> matchedLinks = null;
			if (n1 == null && n2 == null) {
				matchedLinks = this.graphBuilder.getPatternLinks().get(key); 
				if (matchedLinks != null && !matchedLinks.isEmpty()) {
					Collections.sort(matchedLinks);
					for (LabeledLink l : matchedLinks) {
						if (!mappedLinks.contains(l.getId())) {
							mappedLinks.add(l.getId());
							n1 = l.getSource();
							n2 = l.getTarget();
							mapping.put(source,n1);
							mapping.put(target, n2);
							mappedNodes.add(n1.getId());
							mappedNodes.add(n2.getId());
							break;
						}
					}
				}
			} else if (n1 == null) { // target is already mapped
				matchedLinks = this.graphBuilder.getPatternLinks().get(key); 
				if (matchedLinks != null && !matchedLinks.isEmpty()) {
					for (LabeledLink l : matchedLinks) {
						if (!mappedLinks.contains(l.getId()) && 
								!mappedNodes.contains(l.getSource().getId()) &&
								l.getTarget().getId().equalsIgnoreCase(n2.getId())) {
							mappedLinks.add(l.getId());
							n1 = l.getSource();
							mapping.put(source,n1);
							mappedNodes.add(n1.getId());
							break;
						}
					}
				}
			} else if (n2 == null) {
				matchedLinks = this.graphBuilder.getPatternLinks().get(key); 
				if (matchedLinks != null && !matchedLinks.isEmpty()) {
					for (LabeledLink l : matchedLinks) {
						if (!mappedLinks.contains(l.getId()) && 
								!mappedNodes.contains(l.getTarget().getId()) &&
								l.getSource().getId().equalsIgnoreCase(n1.getId())) {
							mappedLinks.add(l.getId());
							n2 = l.getTarget();
							mapping.put(target,n2);
							mappedNodes.add(n2.getId());
							break;
						}
					}
				}
			}
				
			if (n1 == null) {
				
				List<Node> sortedMatchedNodes = uriMatchedNodes.get(sourceUri);
				if (sortedMatchedNodes == null) {
					sortedMatchedNodes = new LinkedList<>();
					Set<Node> matchedNodes = this.graphBuilder.getUriToNodesMap().get(sourceUri);
					if (matchedNodes != null && !matchedNodes.isEmpty()) {
						sortedMatchedNodes.addAll(matchedNodes);
						Collections.sort(sortedMatchedNodes, new NodeSupportingModelsComparator());
						while (!sortedMatchedNodes.isEmpty()) { 
							if (mappedNodes.contains(sortedMatchedNodes.get(0).getId())) {
								sortedMatchedNodes.remove(0);
								continue;
							}
							Set<DefaultLink> outLinks = this.getGraphBuilder().getGraph().outgoingEdgesOf(sortedMatchedNodes.get(0));
							boolean okLink = true;
							if (outLinks != null) {
								for (DefaultLink dl : outLinks) {
									if (dl instanceof LabeledLink &&
											dl.getUri().equalsIgnoreCase(linkUri)) {
										sortedMatchedNodes.remove(0);
										okLink = false;
										break;
									}
								}
								if (!okLink) continue; 
							}
							break;
						}
						if (!sortedMatchedNodes.isEmpty()) {
							n1 = sortedMatchedNodes.get(0);
							mappedNodes.add(n1.getId());
							mapping.put(source, n1);
							sortedMatchedNodes.remove(0);
						}
						uriMatchedNodes.put(sourceUri, sortedMatchedNodes);
					}
				}
				if (n1 == null) {
					id = this.nodeIdFactory.getNodeId(sourceUri);
					n1 = new InternalNode(id, new Label(sourceUri));
					if (this.graphBuilder.addNode(n1)) {
						mapping.put(source, n1);
						mappedNodes.add(n1.getId());
						addedNodes.add((InternalNode)n1);
					} else {
						System.out.println("Error in adding the node " + id + " to the graph.");
					}
				} 
			}

			if (n2 == null) {
				
//				if(model.getId().equals("p4-06B7640A-8E23-B427-4B46-B1C9C194BDD7"))
//				{
//					System.out.println(GraphUtil.labeledGraphToString(model.getGraph()));	
//				}
				List<Node> sortedMatchedNodes = uriMatchedNodes.get(targetUri);
				if (sortedMatchedNodes == null) {
					sortedMatchedNodes = new LinkedList<>();
					Set<Node> matchedNodes = this.graphBuilder.getUriToNodesMap().get(targetUri);
					if (matchedNodes != null && !matchedNodes.isEmpty()) {
						sortedMatchedNodes.addAll(matchedNodes);
						Collections.sort(sortedMatchedNodes, new NodeSupportingModelsComparator());
						while (!sortedMatchedNodes.isEmpty()) { 
							if (mappedNodes.contains(sortedMatchedNodes.get(0).getId())) {
								sortedMatchedNodes.remove(0);
								continue;
							}
							Set<DefaultLink> inLinks = this.getGraphBuilder().getGraph().incomingEdgesOf(sortedMatchedNodes.get(0));
							boolean okLink = true;
							if (inLinks != null) {
								for (DefaultLink dl : inLinks) {
									if (dl instanceof LabeledLink &&
											dl.getUri().equalsIgnoreCase(linkUri)) {
										sortedMatchedNodes.remove(0);
										okLink = false;
										break;
									}
								}
								if (!okLink) continue; 
							}
							break;
						}
						if (!sortedMatchedNodes.isEmpty()) {
							n2 = sortedMatchedNodes.get(0);
							mappedNodes.add(n2.getId());
							mapping.put(target, n2);
							sortedMatchedNodes.remove(0);
						}
						uriMatchedNodes.put(targetUri, sortedMatchedNodes);
					}
				}
				if (n2 == null) {
					id = this.nodeIdFactory.getNodeId(targetUri);
//					if (id.contains("E55_Type10"))
//						System.out.println("break");
					n2 = new InternalNode(id, new Label(targetUri));
					if (this.graphBuilder.addNode(n2)) {
						mapping.put(target, n2);
						addedNodes.add((InternalNode)n2);
						mappedNodes.add(n2.getId());
					} else {
						System.out.println("Error in adding the node " + id + " to the graph.");
					}
				} 
			}
			
//			if (n1 == null || n2 == null) {
//				System.out.println(GraphUtil.labeledGraphToString(model.getGraph()));
//				System.out.println("debug");
//			}
			id = LinkIdFactory.getLinkId(e.getUri(), n1.getId(), n2.getId());
			LabeledLink l = this.graphBuilder.getIdToLinkMap().get(id);
			if (l != null) {
				this.graphBuilder.changeLinkWeight(l, ModelingParams.PATTERN_LINK_WEIGHT);
				l.getModelIds().add(modelId);
				n1.getModelIds().add(modelId);
				n2.getModelIds().add(modelId);
			} else {

				LabeledLink link = e.copy(id);
				
				if (link == null) {
		    		logger.error("cannot instanciate a link from the type: " + e.getType().toString());
		    		continue;
				}
				link.setStatus(LinkStatus.Normal); // all the links in learning graph are normal

				if (link.getModelIds() != null)
					link.getModelIds().clear();
				link.getModelIds().add(modelId);

				if (!this.graphBuilder.addLink(n1, n2, link, ModelingParams.PATTERN_LINK_WEIGHT)) continue;

				this.graphBuilder.savePatternLink(link);
				n1.getModelIds().add(modelId);
				n2.getModelIds().add(modelId);
			}
		}
		
		return addedNodes;
	}
	
	public static void main(String[] args) throws Exception {

		ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getDefault();
		OntologyManager ontologyManager = new OntologyManager(contextParameters.getId());
		File ff = new File(Params.ONTOLOGY_DIR);
		File[] files = ff.listFiles();
		for (File f : files) {
			ontologyManager.doImport(f, "UTF-8");
		}
		ontologyManager.updateCache();  
		
		List<SemanticModel> semanticModels = 
				ModelReader.importSemanticModelsFromJsonFiles(Params.MODEL_DIR, Params.MODEL_MAIN_FILE_EXT);

		String graphPath = Params.GRAPHS_DIR;
		String graphName = graphPath + "graph.json";
		String graphVizName = graphPath + "graph.dot";
		

		ModelLearningGraph ml = ModelLearningGraph.getEmptyInstance(ontologyManager, ModelLearningGraphType.Compact);
		int i = 0;
		Set<InternalNode> addedNodes = new HashSet<>();
		Set<InternalNode> temp;
		for (SemanticModel sm : semanticModels) {
			i++;
			if (i == 4) continue;
			System.out.println(sm.getId());
			temp = ml.addModel(sm, PatternWeightSystem.JWSPaperFormula);
			if (temp != null) addedNodes.addAll(temp);
		}
		
		ml.updateGraphUsingOntology(addedNodes);
		try {
			GraphUtil.exportJson(ml.getGraphBuilder().getGraph(), graphName, true, true);
			GraphVizUtil.exportJGraphToGraphviz(ml.getGraphBuilder().getGraph(), 
					"main graph", 
					true, 
					GraphVizLabelType.LocalId,
					GraphVizLabelType.LocalUri,
					false, 
					false, 
					graphVizName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
