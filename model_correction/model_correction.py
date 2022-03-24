import sys

sys.path.append("")

import utils
import os
import json
import networkx as nx


def remove_incorrect_edge(kg, model, node_dict, edge_dict):
    kg_edges = set()
    for edge in kg.edges.data():
        s = edge[0]
        t = edge[1]
        s_node = kg.nodes[s]['label']
        t_node = kg.nodes[t]['label']
        kg_edges.add((s_node, t_node, edge[2]['label']))

    for edge in model.copy().edges.data():
        s_node = edge[0][:-1]
        t_node = edge[1][:-1]
        edge_label = edge[2]['label']
        if (node_dict.get(s_node, -1), node_dict.get(t_node, -1), edge_dict.get(edge_label, -1)) not in kg_edges:
            model.remove_edge(edge[0], edge[1])

    graph = nx.Graph(model)
    components = nx.connected_components(graph)
    largest_component = max(components, key=len)
    return nx.induced_subgraph(model, largest_component)


def find_isolate_columns(kg, model: nx.DiGraph, node_dict, edge_dict):
    kg_edges = set()
    for edge in kg.edges.data():
        s = edge[0]
        t = edge[1]
        s_node = kg.nodes[s]['label']
        t_node = kg.nodes[t]['label']
        kg_edges.add((s_node, t_node, edge[2]['label']))

    for edge in model.copy().edges.data():
        s_node = edge[0]
        t_node = edge[1]
        edge_label = edge[2]['label']
        if model.nodes[t_node]["nodeType"] == "InternalNode" and \
                (node_dict.get(s_node[:-1], -1), node_dict.get(t_node[:-1], -1),
                 edge_dict.get(edge_label, -1)) not in kg_edges:
            model.remove_edge(edge[0], edge[1])

    components = nx.weakly_connected_components(model)
    largest_component = max(components, key=len)
    for node in model.nodes.data():
        if model.nodes[node[0]]["nodeType"] == "columnNode" and \
                node[0] not in nx.induced_subgraph(model, largest_component).nodes:
            print(node)
        else:
            cNodes = [succ for succ in list(model.successors(node[0])) if model.nodes[succ]["nodeType"] == "columnNode"]
            if len(cNodes) > 1:
                print(cNodes)


if __name__ == '__main__':
    trains = [2, 5, 6]
    kg = utils.load_lg_graph(r"C:\D_Drive\ASM\DataSets\museum-crm\museum_kg_20210906.lg")
    with open(r"museum_node_dict.json", 'r') as f:
        node_dict = json.load(f)
    with open(r"museum_edge_dict.json", 'r') as f:
        edge_dict = json.load(f)

    # k_model = utils.load_graph_from_csv1(
    #     r"C:\D_Drive\ASM\experiment\exp_20220322\train_2_5_6\newSource_11\cytoscape\candidate_model.csv")
    # find_isolate_columns(kg, k_model, node_dict, edge_dict)
    for i in range(1, 30):
        try:
            os.chdir(
                rf"C:\D_Drive\ASM\experiment\exp_20220322\train_{trains[0]}_{trains[1]}_{trains[2]}\newSource_{i}\cytoscape")
            c_model = utils.load_graph_from_csv1("correct_model.csv")
            k_model = utils.load_graph_from_csv1("candidate_model.csv")
            mg = utils.merge_graph(c_model, k_model, "__cm", "__km")
            model = remove_incorrect_edge(kg, k_model, node_dict, edge_dict)
            utils.save_csv_graph(model, "model.csv")
            utils.save_csv_graph(mg, "result.csv")
        except Exception as e:
            print(e)

