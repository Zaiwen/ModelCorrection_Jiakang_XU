import itertools
import operator
import sys
import time

sys.path.append("")

import utils
import os
import json
from itertools import product
import networkx as nx
import pandas as pd
import joblib


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


class candidate_type:
    def __init__(self, cType, property, score, freq, isNewType):
        self.cType = cType
        self.property = property
        self.score = float(score)
        self.freq = freq
        self.isNewType = isNewType

    def __repr__(self):
        return ",".join([self.cType, self.property, str(self.score)])


def parse_learned_types(learnedTypes, node_dic=None, freq_dic=None, nodes_in_model=None):
    if not isinstance(learnedTypes, str):
        return []

    ls = []
    learned_types = learnedTypes.rstrip("\t").split("\t")
    for t in learned_types:
        cType = t.split()[0]
        property = t.split()[1]
        score = t.split()[2]
        freq = 0
        isNewNode = 0
        if freq_dic:
            freq = freq_dic[node_dic[cType]]
        if nodes_in_model:
            isNewNode = 1 if cType not in nodes_in_model else 0
        ct = candidate_type(cType, property, score, freq, isNewNode)
        ls.append(ct)
    return ls


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

    for node in model.copy().nodes:
        if model.nodes[node]["nodeType"] == "InternalNode":
            flag = True
            for succ in model.successors(node):
                if model.nodes[succ]["nodeType"] == "columnNode":
                    flag = False
            if flag:
                model.remove_node(node)

    components = nx.weakly_connected_components(model)
    largest_component = max(components, key=len)
    isolate_column_dic = {}
    incomplete_model = nx.DiGraph(nx.induced_subgraph(model, largest_component))
    # print(incomplete_model.nodes)

    nm = nx.algorithms.isomorphism.categorical_node_match("label", None)
    em = nx.algorithms.isomorphism.categorical_node_match("label", None)
    g = nx.DiGraph()
    dgm = nx.isomorphism.DiGraphMatcher(kg, utils.csv_to_lg(incomplete_model.copy()), nm, em)

    if not dgm.subgraph_is_isomorphic():
        for e in nx.edge_dfs(incomplete_model):
            if g.nodes and e[0] not in g.nodes and e[1] not in g.nodes:
                continue
            new_node = e[1] if e[1] not in g.nodes else e[0]
            g.add_node(e[0], label=e[0][:-1])
            g.add_node(e[1], label=e[1][:-1])
            g.add_edge(*e, label=model.edges[e]["label"])
            dgm = nx.isomorphism.DiGraphMatcher(kg, utils.csv_to_lg(g), nm, em)
            if not dgm.subgraph_is_isomorphic():
                g.remove_node(new_node)
        incomplete_model = nx.DiGraph(nx.induced_subgraph(incomplete_model, g.nodes))

    for cNode in [node for node in model.nodes if model.nodes[node]["nodeType"] == "columnNode"]:
        eNode = list(model.predecessors(cNode))[0]
        if eNode not in incomplete_model:
            learn_types = utils.parse_learned_types(model, cNode)
            isolate_column_dic[cNode] = learn_types

    return isolate_column_dic, incomplete_model


def gen_candidate_nodes_combination(model: nx.DiGraph, iso_col_dic: dict, node_dic: dict, freq_dic: dict):
    nodes_in_model = [model.nodes[node]["label"] for node in model.nodes
                      if node not in iso_col_dic.values()
                      and model.nodes[node]["nodeType"] == "InternalNode"]
    ls = []
    for k in iso_col_dic:
        learned_types = model.nodes[k]["learnedTypes"]
        tmp = parse_learned_types(learned_types, node_dic, freq_dic, nodes_in_model)
        ls.append(tmp)

    pros = product(*ls)
    for e in ls:
        print(e)

    for c in pros:
        print(",".join([str(node_dic[tp.cType]) for tp in c]))


def check_candidate_types(iso_columns_dic, node_dic, kg, im):

    im_lg = utils.csv_to_lg(im)
    nodes = [im_lg.nodes[node]["label"] for node in im_lg.nodes]
    kg_edges = set()
    for edge in kg.edges.data():
        s = edge[0]
        t = edge[1]
        s_node = kg.nodes[s]['label']
        t_node = kg.nodes[t]['label']
        kg_edges.add((s_node, t_node, edge[2]['label']))

    for col, cTypes in iso_columns_dic.items():
        for ct in cTypes.copy():
            entity = ct[0]
            newNode = node_dic[entity]
            edge_paths = search_edge_paths(newNode, kg_edges, nodes)
            if not check_edge_paths(edge_paths, im_lg, kg):
                cTypes.remove(ct)


def search_edge_paths(newNode, kg_edges, nodes):
    res = []

    def search_adj_nodes(newNode, kg_edges):
        return [edge for edge in kg_edges if edge[1] == newNode], [edge for edge in kg_edges if edge[0] == newNode]

    def dfs(newNode, kg_edges, nodes, visited_nodes, path, res):
        if newNode in nodes:
            res.append(path[:])
            return
        in_edges, out_edges = search_adj_nodes(newNode, kg_edges)
        for in_edge in in_edges:
            adj_node = in_edge[0]
            if adj_node in visited_nodes:
                continue
            path.append(in_edge)
            visited_nodes.append(adj_node)
            dfs(adj_node, kg_edges, nodes, visited_nodes[:], path[:], res)
            path.pop(-1)
        for out_edge in out_edges:
            adj_node = out_edge[1]
            if adj_node in visited_nodes:
                continue
            path.append(out_edge)
            visited_nodes.append(adj_node)
            dfs(adj_node, kg_edges, nodes, visited_nodes[:], path[:], res)
            path.pop(-1)

    dfs(newNode, kg_edges, nodes, [], [], res)
    return res


def check_edge_paths(edge_paths, model, kg):
    nm = nx.algorithms.isomorphism.categorical_node_match("label", None)
    em = nx.algorithms.isomorphism.categorical_node_match("label", None)
    for edge_path in edge_paths:
        new_model = model.copy()
        new_model_nodes = [new_model.nodes[node]["label"] for node in sorted(new_model.nodes)]
        print(new_model.nodes.data())
        i = new_model.number_of_nodes()
        for edge in edge_path[::-1]:
            in_node, out_node, edge_label = edge[0], edge[1], edge[2]
            print(in_node, out_node, edge_label)
            print(new_model_nodes)
            if in_node in new_model_nodes:
                new_model.add_node(i, label=out_node)
                new_model.add_edge(new_model_nodes.index(in_node), i, label=edge_label)
                new_model_nodes.append(out_node)
                i += 1
            elif out_node in new_model_nodes:
                new_model.add_node(i, label=in_node)
                idx = new_model_nodes.index(out_node)
                new_model.add_edge(i, idx, label=edge_label)
                new_model_nodes.append(in_node)
                i += 1
            dgm = nx.isomorphism.DiGraphMatcher(kg, new_model, nm, em)
            subgraph_is_isomorphic = dgm.subgraph_is_isomorphic()
            if not subgraph_is_isomorphic:
                break
        else:
            return True

    return False


if __name__ == '__main__':
    node_dict, edge_dict = utils.load_dict()
    kg = utils.load_lg_graph(r"C:\D_Drive\ASM\DataSets\museum-crm\museum_kg_20220513.lg")
    freq_dic = {}.fromkeys(range(24), 0)
    node_set = set()
    for node in kg.nodes:
        freq_dic[kg.nodes[node]["label"]] += 1
        if kg.in_degree(node) == 0 or kg.out_degree(node) == 0:
            node_set.add(kg.nodes[node]["label"])
    kg_edges = set()
    for edge in kg.edges.data():
        s = edge[0]
        t = edge[1]
        s_node = kg.nodes[s]['label']
        t_node = kg.nodes[t]['label']
        kg_edges.add((s_node, t_node, edge[2]['label']))

    # s17_lg = utils.load_lg_graph(r"C:\D_Drive\ASM\experiment\correct_models\lg\s17.csv.lg")
    # nm = nx.algorithms.isomorphism.categorical_node_match("label", None)
    # em = nx.algorithms.isomorphism.categorical_node_match("label", None)
    # dgm = nx.isomorphism.DiGraphMatcher(kg, s17_lg, nm, em)
    # print(dgm.subgraph_is_isomorphic())
    # sys.exit(1)
    k_model = utils.load_graph_from_csv1(
        rf"C:\D_Drive\ASM\experiment\exp_20220530\train_1_6_12___1\newSource_17\cytoscape\candidate_model.csv")
    dic, im = find_isolate_columns(kg, k_model, node_dict, edge_dict)
    E52_df = pd.read_csv(r"C:\D_Drive\ASM\experiment\exp_20220530\trains_df\E52.csv", header=[0, 1], index_col=0)
    E55_df = pd.read_csv(r"C:\D_Drive\ASM\experiment\exp_20220530\trains_df\E55.csv", header=[0, 1], index_col=0)

    E52_model = joblib.load(r"C:\D_Drive\ASM\experiment\exp_20220530\tree_model\E52.model")
    E55_model = joblib.load(r"C:\D_Drive\ASM\experiment\exp_20220530\tree_model\E55.model")

    E52_features = joblib.load(r"C:\D_Drive\ASM\experiment\exp_20220530\tree_model\E52.features")
    E55_features = joblib.load(r"C:\D_Drive\ASM\experiment\exp_20220530\tree_model\E55.features")

    E52_df = E52_df.loc[[idx for idx in E52_df.index if idx.startswith("s17")]]
    E55_df = E55_df.loc[[idx for idx in E55_df.index if idx.startswith("s17")]]

    E52_df = E52_df.loc[[idx for idx in E52_df.index if idx.split(":")[1] in im.nodes]]
    E55_df = E55_df.loc[[idx for idx in E55_df.index if idx.split(":")[1] in im.nodes]]

    im.remove_nodes_from([node for node in im.nodes if im.nodes[node]["nodeType"] == "columnNode"])
    im.remove_node("E52_Time-Span1")
    im.remove_node("E55_Type1")

    for pred in E52_model.predict(E52_df["features"][E52_features]):
        if pred == 0:
            im.add_node("E52_Time-Span1", label="E52_Time-Span")
            im.add_edge("E67_Birth1", "E52_Time-Span1", label="P4_has_time-span")
        if pred == 1:
            im.add_node("E52_Time-Span2", label="E52_Time-Span")
            im.add_edge("E69_Death1", "E52_Time-Span2", label="P4_has_time-span")
        if pred == 2:
            im.add_node("E52_Time-Span3", label="E52_Time-Span")
            im.add_edge("E12_Production1", "E52_Time-Span3", label="P4_has_time-span")

    for pred in E55_model.predict(E55_df["features"][E55_features]):
        if pred == 0:
            im.add_node("E55_Type1", label="E55_Type")
            im.add_edge("E12_Production1", "E55_Type1", label="P32_used_general_technique")
        if pred == 1:
            im.add_node("E55_Type2", label="E55_Type")
            im.add_edge("E22_Man-Made_Object1", "E55_Type2", label="P2_has_type")

    im_lg = utils.csv_to_lg(im)
    utils.save_csv_graph(im, r"C:\D_Drive\ASM\experiment\exp_20220530\tmp.csv")
    utils.save_lg_graph(im_lg, r"C:\D_Drive\ASM\experiment\exp_20220530\s17_seed.lg")
    for node in im_lg.nodes.data():
        print(node)
    nodes = [im_lg.nodes[node]["label"] for node in im_lg.nodes]
    print(im_lg.nodes)
    edge_paths = search_edge_paths(17, kg_edges, nodes)
    print(edge_paths)
    print(check_edge_paths(edge_paths, im_lg, kg))
    # check_candidate_types(dic, node_dict, kg, im)
    # for k, v in dic.items():
    #     print(k)
    #     print(v)

    '''
    trains = [2, 5, 6]
    ofile = open(rf"C:\D_Drive\ASM\experiment\exp_20220522\extension\({trains[0]},{trains[1]},{trains[2]})\candidate_check_result.txt", "w", encoding="utf-8")
    try:
        for i in range(1, 30):
            k_model = utils.load_graph_from_csv1(
                rf"C:\D_Drive\ASM\experiment\exp_20220522\train_{trains[0]}_{trains[1]}_{trains[2]}___1\newSource_{i}\cytoscape\candidate_model.csv")
            if not k_model:
                continue
            ofile.write(f"new ds:s{i}\n")
            start = time.time()
            dic, im = find_isolate_columns(kg, k_model, node_dict, edge_dict)
            utils.save_lg_graph(utils.csv_to_lg(im),
                                rf"C:\D_Drive\ASM\experiment\exp_20220522\extension\({trains[0]},{trains[1]},{trains[2]})\seed_models\s{i}_seed.lg")
            check_candidate_types(dic, node_dict, kg, im)
            for k, v in dic.items():
                ofile.write(f"isolate column: {k}\n")
                ofile.write(f"candidate type: {v}\n")
            end = time.time()
            ofile.write(f"running time: {round(end-start, 3)}\n")
            ofile.write("\n")
            ofile.flush()
        ofile.close()
    except FileNotFoundError:
        pass
    '''

    # trains = [1, 6, 12]
    # for i in range(1, 30):
    #     try:
    #         os.chdir(
    #             rf"C:\D_Drive\ASM\experiment\exp_20220530\train_{trains[0]}_{trains[1]}_{trains[2]}___1\newSource_{i}\cytoscape")
    #         c_model = utils.load_graph_from_csv1("correct_model.csv")
    #         k_model = utils.load_graph_from_csv1("candidate_model.csv")
    #         utils.add_learn_types_into_graph(c_model)
    #         utils.add_learn_types_into_graph(k_model)
    #         utils.save_csv_graph1(c_model, "correct_model1.csv")
    #         utils.save_csv_graph1(k_model, "candidate_model1.csv")
    #         mg = utils.merge_graph(c_model, k_model, "__cm", "__km")
    #         # d, im = find_isolate_columns(kg, k_model, node_dict, edge_dict)
    #         im = remove_incorrect_edge(kg, k_model, node_dict, edge_dict)
    #         utils.save_csv_graph(im, "model.csv")
    #         utils.save_csv_graph(mg, "result.csv")
    #     except Exception as e:
    #         print(e)
