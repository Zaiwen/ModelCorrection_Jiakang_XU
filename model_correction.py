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


if __name__ == '__main__':
    trains = [1, 6, 12]
    kg = utils.load_lg_graph(r"D:\ASM\DataSets\museum-crm\museum_kg_20210906.lg")
    with open(r"museum_node_dict.json", 'r') as f:
        node_dict = json.load(f)
    with open(r"museum_edge_dict.json", 'r') as f:
        edge_dict = json.load(f)
    for i in range(1, 30):
        try:
            os.chdir(
                rf"D:\ASM\experiment\model_correction_20220220\train_{trains[0]}_{trains[1]}_{trains[2]}\newSource_{i}\cytoscape")
            c_model = utils.load_graph_from_csv("correct_model.csv")
            k_model = utils.load_graph_from_csv("model_0.csv")
            mg = utils.merge_graph(c_model, k_model, "__cm", "__km")
            model = remove_incorrect_edge(kg, k_model, node_dict, edge_dict)
            utils.save_csv_graph(model, "model.csv")
            utils.save_csv_graph(mg, "result.csv")
        except Exception as e:
            pass
