import utils
import networkx as nx
import os
import json


if __name__ == '__main__':
    os.chdir(r"E:\exp_20210830\train_2_5_6\newSource_1\cytoscape")
    c_model = utils.load_graph_from_csv("correct_model.csv")
    k_model = utils.load_graph_from_csv("model_0.csv")
    m_model = utils.load_graph_from_csv("model.csv")

    mg = utils.merge_graph(c_model, k_model, "__cm", "__km")
    mcs = utils.get_mcs(k_model, c_model)

    blueNodes = []
    yellowNodes = []
    orangeNodes = []
    grayNodes = []

    for node in k_model.nodes:
        if node not in m_model:
            grayNodes.append(node+"__km")

    yellowNodes = [node +"__cm" for node in utils.get_mcs(k_model, c_model).nodes]

    for node in mg.nodes:
        if node.endswith("km") and node not in grayNodes:
            blueNodes.append(node)
        elif node.endswith("cm") and node not in yellowNodes:
            orangeNodes.append(node)

    print(blueNodes)
    print(yellowNodes)
    print(orangeNodes)
    print(grayNodes)


