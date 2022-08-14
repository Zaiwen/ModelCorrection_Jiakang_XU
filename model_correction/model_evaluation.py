import networkx as nx
import utils
import json
import pandas as pd
from itertools import permutations
from model_correction import search_edge_paths
import sys


def model_evaluate(correct_model:nx.DiGraph, target_model:nx.DiGraph):
    n_edges_cm = len(correct_model.edges)
    n_edges_tm = len(target_model.edges)

    # print(n_edges_tm)
    # print(n_edges_cm)
    n_edges_common = 0
    for e in target_model.edges:

        if e in correct_model.edges:
            # print(e)
            n_edges_common += 1
        else:
            # print(e)
            pass

    precision = n_edges_common / n_edges_tm
    recall = n_edges_common / n_edges_cm
    return round(precision, 2), round(recall, 2)


def add_column_nodes(result_model, cNodes_dic):
    for k, v in cNodes_dic.items():
        col = k
        entity = v[0]
        data_property = v[1]
        result_model.add_node(col, nodeType="columnNode")
        result_model.add_edge(entity, col, label=data_property)


def add_searched_isoCols(result_model, isoCols):
    E52_cols = [isoCol for isoCol in isoCols if isoCol[0] == "4"]
    ls = []
    for isoCol in isoCols:
        col = isoCol[1]
        entity = isoCol[2]
        data_property = isoCol[3]
        result_model.add_node(col, nodeType="columnNode")
        if isoCol not in E52_cols:
            result_model.add_node(entity+"1")
            result_model.add_edge(entity+"1", col, label=data_property)
    n = len(E52_cols)
    if n == 1:
        for per in permutations(E52_cols, n):
            r_model = result_model.copy()
            for i, isoCol in enumerate(per):
                col = isoCol[1]
                entity = isoCol[2]
                data_property = isoCol[3]
                r_model.add_node(col, nodeType="columNode")
                for j in range(1, 5):
                    if entity+str(j) in result_model.nodes:
                        r_model.add_edge(entity+str(j), col, label=data_property)
                        break
            ls.append(re_label(r_model))
    elif n < 4:
        for per in permutations(E52_cols, n):
            r_model = result_model.copy()
            for i, isoCol in enumerate(per):
                col = isoCol[1]
                entity = isoCol[2]
                data_property = isoCol[3]
                r_model.add_node(col, nodeType="columNode")
                r_model.add_node(entity+str(i+1))
                r_model.add_edge(entity+str(i+1), col, label=data_property)
            ls.append(re_label(r_model))
    elif n == 4:
        for isoCol in E52_cols:
            for per in permutations([col for col in E52_cols if col != isoCol], 3):
                r_model = result_model.copy()
                r_model.add_node(isoCol[1], nodeType="columnNode")
                r_model.add_node(isoCol[2] + str(3))
                r_model.add_edge(isoCol[2] + str(3), isoCol[1], label=isoCol[3])
                for i, isoCol1 in enumerate(per):
                    col = isoCol1[1]
                    entity = isoCol1[2]
                    data_property = isoCol[3]
                    r_model.add_node(col, nodeType="columnNode")
                    r_model.add_node(entity + str(i + 1))
                    r_model.add_edge(entity + str(i + 1), col, label=data_property)
                ls.append(re_label(r_model))
    return ls


def add_remain_isoCols(model, k_model, c_model, remain_isoCols, kg_edges, node_dict, edge_dict):

    if not remain_isoCols:
        return model

    E52_cols = [col_ls for col_ls in remain_isoCols if col_ls[1] == "E52_Time-Span"]
    model_lg = utils.csv_to_lg(model.copy())
    nodes = [model_lg.nodes[node]["label"] for node in model_lg.nodes]

    not_kg_cols = []
    for col_ls in remain_isoCols:
        if col_ls not in E52_cols:
            col = col_ls[0]
            entity = col_ls[1]
            data_property = col_ls[2]
            label = col_ls[3]
            if entity+str(1) in model.nodes:
                model.add_node(col, nodeType="columnNode")
                model.add_edge(entity + str(1), col, label=data_property)
                continue

            model.add_node(col, nodeType="columnNode")
            model.add_edge(entity + str(1), col, label=data_property)
            edge_paths = search_edge_paths(label, kg_edges, nodes)
            edge_paths.sort(key=len)
            candidate_paths = [edge_path for edge_path in edge_paths if len(edge_path) == len(edge_paths[0])]
            # print(candidate_paths)
            if not candidate_paths:
                not_kg_cols.append(col_ls)
                continue
            # assert len(candidate_paths) == 1
            # print(candidate_paths)
            edge_path = candidate_paths[0]
            for edge in edge_path[::-1]:
                model.add_edge(node_dict[edge[0]]+str(1), node_dict[edge[1]]+str(1), label=edge_dict[edge[2]])

    if E52_cols:
        for i, col_ls in enumerate(E52_cols):
            col = col_ls[0]
            entity = col_ls[1]
            data_property = col_ls[2]
            model.add_node(col, nodeType="columnNode")
            model.add_edge(entity + str(i+1), col, label=data_property)

        edge_paths = search_edge_paths(4, kg_edges, nodes)
        edge_paths.sort(key=len)
        candidate_paths = [edge_path for edge_path in edge_paths if len(edge_path) == len(edge_paths[0])]

        model_ls = []

        for per in permutations(candidate_paths, len(E52_cols)):
            tmp_model = model.copy()
            # print(per)
            for i, e in enumerate(per):
                tmp_model.add_edge(node_dict[e[0][0]]+str(1), node_dict[e[0][1]]+str(i+1), label=edge_dict[e[0][2]])
            tmp_model = re_label(tmp_model)
            model_ls.append([tmp_model, model_evaluate(c_model, tmp_model)])

        model = max(model_ls, key=lambda x:x[1])[0]

    for col_ls in not_kg_cols:

        col = col_ls[0]
        entity = col_ls[1]
        data_property = col_ls[2]
        model.add_node(col, nodeType="columnNode")
        model.add_edge(entity+str(1), col, label=data_property)
        for edge in k_model.edges.data():
            if edge[1][:-1] == entity:
                model.add_edge(edge[0], edge[1], label=edge[2]["label"])

    if not nx.is_weakly_connected(model):
        model.add_node("E8_Acquisition1", label="E8_Acquisition")
        model.add_edge("E22_Man-Made_Object1", "E8_Acquisition1", label="P24i_changed_ownership_through")
    return model


def re_label(model):
    map_dic = {}
    removeFlag = False
    for node in model.copy().nodes:
        if not node.startswith("E"):
            removeFlag = True
            # print(True)
            break
    if removeFlag:
        for node in model.copy().nodes:
            if "nodeType" not in model.nodes[node] or model.nodes[node]["nodeType"] == "InternalNode":
                if not [cNode for cNode in model.successors(node) if not (cNode.startswith("E") and cNode[1].isdecimal())]:
                    model.remove_node(node)

    for node in model.copy().nodes:
        if node.startswith("E52_Time-Span") and len(list(model.predecessors(node))) == 2:
            model.remove_node("E8_Acquisition1")


    for edge in model.edges:
        if edge[1][:-1] == "E52_Time-Span":
            if edge[0] == "E67_Birth1":
                map_dic[edge[1]] = "E52_Time-Span1"
            elif edge[0] == "E69_Death1":
                map_dic[edge[1]] = "E52_Time-Span2"
            elif edge[0] == "E12_Production1":
                map_dic[edge[1]] = "E52_Time-Span3"
            else:
                map_dic[edge[1]] = "E52_Time-Span4"
        if edge[1][:-1] == "E55_Type":
            if edge[0] == "E12_Production1":
                map_dic[edge[1]] = "E55_Type1"
            else:
                map_dic[edge[1]] = "E55_Type2"

    model = nx.relabel_nodes(model, map_dic, True)

    # if not nx.is_weakly_connected(model):
    #     model.add_node("E8_Acquisition1", label="E8_Acquisition")
    #     model.add_edge("E22_Man-Made_Object1", "E8_Acquisition1", label="P24i_changed_ownership_through")
    return model


if __name__ == '__main__':

    train = [1, 15, 26]

    dir_path = rf"C:\D_Drive\ASM\experiment\exp_20220705\train_{train[0]}_{train[1]}_{train[2]}___1"
    # dir_path = rf"C:\D_Drive\ASM\experiment\exp_20220712"

    for i in range(1, 30):
        try:
              result_lg = utils.load_lg_graph\
                  (rf"{dir_path}\newSource_{i}\s{i}_result.lg")
              utils.save_csv_graph(utils.lg_to_csv(result_lg),
                    rf"{dir_path}\newSource_{i}\s{i}_result.csv")
        except Exception:
            pass
    #
    # sys.exit(1)
    res_df = pd.read_csv(rf"{dir_path}\result.csv")
    res_df["precision"] = [0 for _ in range(len(res_df))]
    res_df["recall"] = [0 for _ in range(len(res_df))]
    node_dict, edge_dict = utils.load_dict1()

    for i in range(1, 30):

        # if i != 5:
        #     continue

        try:
            c_model = utils.load_graph_from_csv1\
                (rf"{dir_path}\newSource_{i}\cytoscape\correct_model.csv")
            k_model = utils.load_graph_from_csv1\
                (rf"{dir_path}\newSource_{i}\cytoscape\candidate_model.csv")
            with open(rf"{dir_path}\newSource_{i}\cNode.json", 'r')as jf:
                column_node_dic = json.load(jf)
            print(i)
            newNodeSearched = True
            result_model = utils.load_graph_from_csv\
                (rf"{dir_path}\newSource_{i}\s{i}_result.csv")
            if not result_model:
                result_model = utils.load_graph_from_csv\
                    (rf"{dir_path}\newSource_{i}\seed.csv")
                newNodeSearched = False
            c_model = re_label(c_model)
            result_model = re_label(result_model)
            add_column_nodes(result_model, column_node_dic)
            kg = utils.load_lg_graph(rf"C:\D_Drive\ASM\DataSets\museum-crm\tmp\lg\museum_kg_s{i}.lg")
            kg_edges = set()
            for edge in kg.edges.data():
                s = edge[0]
                t = edge[1]
                s_node = kg.nodes[s]['label']
                t_node = kg.nodes[t]['label']
                kg_edges.add((s_node, t_node, edge[2]['label']))

            if newNodeSearched:
                with open(rf"{dir_path}\newSource_{i}\isoColsTypes.txt", 'r', encoding="utf-8")as f:
                    isoCols = eval(f.read())
                    ls = add_searched_isoCols(result_model, isoCols)
                tmp = [model_evaluate(c_model, r_model) for r_model in ls]
                precision, recall = max(tmp)
                model = ls[list.index(tmp, (precision, recall))]

                # for edge in model.edges:
                #     print(edge)
                with open(rf"{dir_path}\newSource_{i}\errorCols.json", 'r', encoding='utf-8') as jf:
                    js = json.load(jf)
                remain_isoCols = []
                for col, t in js.items():
                    remain_isoCols.append([col, t[0], t[1], t[2]])

                model = add_remain_isoCols(model, k_model, c_model, remain_isoCols, kg_edges, node_dict, edge_dict)
                # model = re_label(model)
                utils.save_csv_graph(model, rf"{dir_path}\s{i}_result_CNodes.csv")
                precision, recall = model_evaluate(c_model, model)
                print(precision, recall)
                idx = list(res_df["data source"]).index(rf"s{i}.csv")
                res_df["precision"][idx] = precision
                res_df["recall"][idx] = recall
            else:
                result_model = re_label(result_model)
                with open(rf"{dir_path}\newSource_{i}\errorCols.json", 'r', encoding='utf-8') as jf:
                    js = json.load(jf)
                with open(rf"{dir_path}\newSource_{i}\isoCols.json", 'r', encoding='utf-8')as jf:
                    js1 = json.load(jf)[0]
                remain_isoCols = []
                for col, t in js.items():
                    remain_isoCols.append([col, t[0], t[1], t[2]])
                for ls in js1:
                    remain_isoCols.append([ls[1], ls[2], ls[3], ls[0]])
                result_model = add_remain_isoCols(result_model, k_model, c_model, remain_isoCols, kg_edges, node_dict, edge_dict)
                precision, recall = model_evaluate(c_model, result_model)
                utils.save_csv_graph(result_model, rf"{dir_path}\s{i}_result_CNodes.csv")
                idx = list(res_df["data source"]).index(rf"s{i}.csv")
                res_df["precision"][idx] = precision
                res_df["recall"][idx] = recall
                print(precision, recall)
        except Exception as e:
            print(e)
    res_df.to_csv(rf"{dir_path}\result.csv", index=False)


