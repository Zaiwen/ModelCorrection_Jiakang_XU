import networkx as nx
import utils
import json
import pandas as pd
from itertools import permutations
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


def add_columnNodes(result_model, cNodes_dic):
    for k, v in cNodes_dic.items():
        col = k
        entity = v[0]
        data_property = v[1]
        result_model.add_node(col, nodeType="columnNode")
        result_model.add_edge(entity, col, label=data_property)


def add_isoCols(result_model, isoCols):
    E52_cols = [isoCol for isoCol in isoCols if isoCol[0] == "4"]
    ls = []
    for isoCol in isoCols:
        col = isoCol[1]
        entity = isoCol[2]
        data_property = isoCol[3]
        result_model.add_node(col, nodeType="columNode")
        if isoCol not in E52_cols:
            result_model.add_node(entity+"1")
            result_model.add_edge(entity+"1", col, label=data_property)

    n = len(E52_cols)
    if n < 4:
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


def re_label(model):
    map_dic = {}
    removeFlag = False
    for node in model.copy().nodes:
        if not node.startswith("E"):
            removeFlag = True
            break
    if removeFlag:
        for node in model.copy().nodes:
            if "nodeType" not in model.nodes[node] or model.nodes[node]["nodeType"] == "InternalNode":
                if not [cNode for cNode in model.successors(node) if not cNode.startswith("E")]:
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

    return nx.relabel_nodes(model, map_dic, True)


if __name__ == '__main__':

    train = [7, 19, 23]

    dir_path = rf"C:\D_Drive\ASM\experiment\exp_20220627\train_{train[0]}_{train[1]}_{train[2]}___1"


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
    res_df = pd.read_csv(rf"{dir_path}\({train[0]},{train[1]},{train[2]})result.csv")
    res_df["precision"] = [0 for _ in range(len(res_df))]
    res_df["recall"] = [0 for _ in range(len(res_df))]

    for i in range(1, 30):

        # if i != 9:
        #     continue

        try:
            c_model = utils.load_graph_from_csv1\
                (rf"{dir_path}\newSource_{i}\cytoscape\correct_model.csv")

            with open(rf"{dir_path}\newSource_{i}\cNode.json", 'r')as jf:
                dic = json.load(jf)
            print(i)
            newNode = True
            result_model = utils.load_graph_from_csv\
                (rf"{dir_path}\newSource_{i}\s{i}_result.csv")
            if not result_model:
                result_model = utils.load_graph_from_csv\
                    (rf"{dir_path}\newSource_{i}\seed.csv")
                newNode = False
            c_model = re_label(c_model)
            result_model = re_label(result_model)
            add_columnNodes(result_model, dic)
            if newNode:
                with open(rf"{dir_path}\newSource_{i}\isoColsTypes.txt", 'r', encoding="utf-8")as f:
                    isoCols = eval(f.read())
                    ls = add_isoCols(result_model, isoCols)
                    tmp = [model_evaluate(c_model, r_model) for r_model in ls]
                    precision, recall = max(tmp)
                    model = ls[list.index(tmp, (precision, recall))]
                    utils.save_csv_graph(model, rf"{dir_path}\s{i}_result_CNodes.csv")
                    # precision, recall = max[model_evaluate(c_model, r_model) for r_model in ls]
                    print(precision, recall)
                    idx = list(res_df["data source"]).index(rf"s{i}.csv")
                    res_df["precision"][idx] = precision
                    res_df["recall"][idx] = recall
            else:
                precision, recall = model_evaluate(c_model, result_model)
                utils.save_csv_graph(result_model, rf"{dir_path}\s{i}_result_CNodes.csv")
                idx = list(res_df["data source"]).index(rf"s{i}.csv")
                res_df["precision"][idx] = precision
                res_df["recall"][idx] = recall
                print(precision, recall)
        except Exception as e:
            print(e)

    res_df.to_csv(rf"{dir_path}\result.csv", index=False)
    # E52_cols = [isoCol for isoCol in isoCols if isoCol[0] == "4"]
    # ls = []
    # for isoCol in isoCols:
    #     col = isoCol[1]
    #     entity = isoCol[2]
    #     data_property = isoCol[3]
    #     result_model.add_node(col)
    #     if isoCol not in E52_cols:
    #         result_model.add_node(entity+"1")
    #         result_model.add_edge(entity+"1", col, label=data_property)
    #
    # for per in permutations(E52_cols, 3):
    #     r_model = result_model.copy()
    #     for i, isoCol in enumerate(per):
    #         col = isoCol[1]
    #         entity = isoCol[2]
    #         data_property = isoCol[3]
    #         r_model.add_node(col)
    #         r_model.add_node(entity+str(i+1))
    #         r_model.add_edge(entity+str(i+1), col, label=data_property)
    #     ls.append(re_label(r_model))

    # utils.save_csv_graph(result_model, r"C:\D_Drive\ASM\experiment\exp_20220613\train_1_4_21___1\newSource_7\rs.csv")
    # print(*model_evaluate(c_model, result_model))


