import json
import networkx as nx
import pandas as pd
import os

DS = "museum"


def load_dict():
    with open(DS+r"_node_dict.json", 'r')as f:
        node_dict = json.load(f)
    with open(DS+r"_edge_dict.json", 'r')as f:
        edge_dict = json.load(f)
    return node_dict, edge_dict


def load_lg_graph(filename):
    graph = nx.DiGraph()
    try:
        with open(filename) as file_object:
            for line in file_object:
                if line.__eq__(""):
                    continue
                elif line.startswith("v"):
                    line_split = line.split(" ")
                    node_id = int(line_split[1])
                    node_label = int(line_split[2])
                    graph.add_node(node_id, label=node_label)
                elif line.startswith("e"):
                    line_split = line.split(" ")
                    in_node = int(line_split[1])
                    out_node = int(line_split[2])
                    edge_label = int(line_split[3])
                    graph.add_edge(in_node, out_node, label=edge_label)
        return graph
    except FileNotFoundError:
        print("the file", filename, "does not exist!")


def load_graph_from_csv(filename):
    graph = nx.DiGraph()
    try:
        with open(filename)as graph_file:
            for line in graph_file:
                if "source" not in line:
                    line_split = line.split(",")
                    source = line_split[0]
                    source_label = line_split[0][:-1]
                    target = line_split[1]
                    target_label = line_split[1][:-1]
                    edge_label = line_split[2].rstrip("\n")
                    graph.add_node(source, label=source_label)
                    graph.add_node(target, label=target_label)
                    graph.add_edge(source, target, label=edge_label)
        return graph
    except FileNotFoundError:
        print("the file", filename, "does not exist!")


def load_graph_from_csv1(filename):
    graph = nx.DiGraph()
    try:
        df = pd.read_csv(filename)
        for _,row in df.iterrows():
            source = row["source"]
            target = row["target"]
            edge_label = row["edge_label"]
            target_node_type = row["target_node_type"]
            graph.add_node(source, label=source[:-1], nodeType="InternalNode")
            if target_node_type == "InternalNode":
                graph.add_node(target, label=target[:-1], nodeType=target_node_type)
            else:
                graph.add_node(target, nodeType=target_node_type, learnedTypes=row["learned_types"])
            graph.add_edge(source, target, label=edge_label)
        return graph
    except Exception:
        print("the file", filename, "does not exist!")


def get_mcs(g1, g2) -> nx.DiGraph:
    matching_graph = nx.Graph()

    for (n1, n2) in g2.edges():
        for (m1, m2) in g1.edges():
            if g1.nodes[m1]['label'] == g2.nodes[n1]['label'] and g1.nodes[m2]['label'] == g2.nodes[n2]['label']:
                if g2.edges[n1, n2]['label'] == g1.edges[m1, m2]['label']:
                    matching_graph.add_edge(n1, n2)
    components = nx.connected_components(matching_graph)
    largest_component = max(components, key=len)
    return nx.induced_subgraph(g2, largest_component)


def merge_graph(g1: nx.DiGraph, g2: nx.DiGraph, s1: str, s2: str) -> nx.DiGraph:
    merged_graph = nx.DiGraph()

    try:
        g1.remove_nodes_from([node for node in g1.nodes if g1.nodes[node]["nodeType"] == "columnNode"])
        g2.remove_nodes_from([node for node in g2.nodes if g2.nodes[node]["nodeType"] == "columnNode"])
    except:
        pass

    for node in g1.nodes.data():
        merged_graph.add_node(node[0] + s1, label=node[1]['label'])

    for edge in g1.edges.data():
        merged_graph.add_edge(edge[0] + s1, edge[1] + s1, label=edge[2]['label'])

    mcs_g1 = get_mcs(g2, g1)
    mcs_g2 = get_mcs(g1, g2)

    for node in g2.nodes.data():
        if node not in mcs_g2.nodes.data():
            merged_graph.add_node(node[0] + s2, label=node[1]['label'])

    for edge in g2.edges.data():
        if edge not in mcs_g2.edges.data():
            s_node = edge[0]
            t_node = edge[1]
            if s_node in mcs_g2.nodes or t_node in mcs_g2.nodes:
                i_node = s_node if s_node in mcs_g2.nodes else t_node
                n_node = s_node if s_node not in mcs_g2.nodes else t_node
                for node in mcs_g1.nodes:
                    if i_node[:-1] == node[:-1] and mcs_g2.degree(i_node) == mcs_g1.degree(node):
                        if i_node == s_node:
                            merged_graph.add_edge(node+s1, n_node+s2, label=edge[2]['label'])
                        else:
                            merged_graph.add_edge(n_node+s2,  node+s1, label=edge[2]['label'])
            else:
                merged_graph.add_edge(s_node+s2, t_node+s2, label=edge[2]['label'])

    return merged_graph


def csv_to_lg(csv_graph):
    node_dict, edge_dict = load_dict()
    csv_graph.remove_nodes_from(nodes=[node for node in csv_graph.nodes if csv_graph.nodes[node]["nodeType"] == "columnNode"])
    graph = nx.DiGraph()
    i = 0
    for node in csv_graph.nodes:
        csv_graph.nodes[node]["id"] = i
        i += 1
    for edge in csv_graph.edges:
        source_id = csv_graph.nodes[edge[0]]['id']
        source_label = csv_graph.nodes[edge[0]]['label']
        target_id = csv_graph.nodes[edge[1]]['id']
        target_label = csv_graph.nodes[edge[1]]['label']
        edge_label = csv_graph.edges[edge]['label']
        graph.add_node(source_id, label=node_dict.get(source_label, -1))
        graph.add_node(target_id, label=node_dict.get(target_label, -1))
        graph.add_edge(source_id, target_id, label=edge_dict.get(edge_label, -1))
    return graph


def lg_to_csv(lg_graph):
    node_dict, edge_dict = load_dict()
    node_dict = {v: k for k, v in node_dict.items()}
    edge_dict = {v: k for k, v in edge_dict.items()}

    graph = nx.DiGraph()
    nodes = [[node[0], node[1]['label']] for node in lg_graph.nodes.data()]
    nodes.sort(key=lambda x: x[1])
    i = 0
    last_node = nodes[0]
    for node in nodes.copy():
        if node[1] != last_node[1]:
            i = 0
        node.append(node_dict.get(node[1]) + str(i))
        i += 1
        last_node = node

    def get(nodes, id):
        for node in nodes:
            if node[0] == id:
                return node

    for edge in lg_graph.edges.data():
        s_id = edge[0]
        t_id = edge[1]
        label = edge[2]['label']
        s_node = get(nodes, s_id)
        t_node = get(nodes, t_id)
        graph.add_edge(s_node[2], t_node[2], label=edge_dict.get(label))
    return graph


def print_lg_graph(g):
    for node in sorted(g.nodes):
        print('v', node, g.nodes[node]['label'])
    for edge in g.edges:
        print('e', edge[0], edge[1], g.edges[edge]['label'])
    print()


def print_csv_graph(g):
    for edge in g.edges:
        source = edge[0]
        target = edge[1]
        edge_label = g.edges[edge]['label']
        print(source, target, edge_label)
    print()


def save_lg_graph(g, filename):
    with open(filename, 'w') as f:
        f.write('t # 1\n')
        for node in sorted(g.nodes):
            # f.writelines('v ' + str(node) + ' ' + str(g.nodes[node]['label']) + '\n')
            f.write(' '.join(['v', str(node), str(g.nodes[node]['label'])]) + '\n')
        for edge in g.edges:
            # f.writelines('e ' + str(edge[0]) + ' ' + str(edge[1]) + ' ' + str(g.edges[edge]['label']) + '\n')
            f.write(' '.join(['e', str(edge[0]), str(edge[1]), str(g.edges[edge]['label'])]) + '\n')


def save_csv_graph(g, filename):
    with open(file=filename, mode="w")as f:
        f.write(",".join(['source', 'target', 'edge_label']) + "\n")
        for edge in g.edges:
            source = edge[0]
            target = edge[1]
            edge_label = g.edges[edge]['label']
            f.write(",".join([source, target, edge_label]) + "\n")


if __name__ == '__main__':

    graph = load_graph_from_csv1(r"C:\D_Drive\ASM\experiment\tmp\cytoscape\candidate_model.csv")
    # graph.remove_nodes_from(nodes=[node for node in graph.nodes if graph.nodes[node]["nodeType"] == "columnNode"])
    for node in graph.nodes.data():
        print(node)

