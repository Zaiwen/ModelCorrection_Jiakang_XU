import utils
import os
import networkx as nx
import json


def viz_mcs(train_index, new_source_index):
    os.chdir(f"E:/model_correction_20210705/train_{train_index}/newSource_{new_source_index}/cytoscape")
    graph_files = os.listdir()
    graphs = [utils.load_graph_from_csv(g) for g in graph_files if g.startswith("model")]
    mcs = graphs[0]
    graphs = graphs[1:]
    for graph in graphs:
        mcs = utils.get_mcs(graph, mcs)

    utils.save_csv_graph(mcs, "mcs.csv")
    i = 0
    removed_edges = {}
    for e in mcs.edges:
        mcs_ = mcs.copy()
        mcs_.remove_edge(*e)
        mcs_.remove_node(e[1])
        if nx.is_weakly_connected(mcs_):
            removed_edges["e"+str(i)] = e
            graph_name = "mcs_e"+str(i)+".csv"
            i += 1
            utils.save_csv_graph(mcs_, graph_name)

    f = open("removed_edges.txt", 'w', encoding='utf-8')
    json.dump(removed_edges, f, indent=4)
    f.close()


trains = ['9_10_24', '2_12_14', '18_24_28']


for train_index in trains:
    for i in range(29):
        try:
            viz_mcs(train_index, i+1)
        except Exception:
            pass
