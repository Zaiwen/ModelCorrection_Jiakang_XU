import os
import sys
import re
import time
import model_correction
import json
from itertools import permutations
import networkx as nx
import random
from itertools import combinations
import utils
import pandas as pd
import json
from pathlib import Path


sys.path.append("")


if __name__ == '__main__':

    dir_path1 = rf"C:\D_Drive\ASM\DataSets\museum-crm\sources-modified-20210828"
    dir_path2 = rf"C:\D_Drive\ASM\DataSets\museum-crm\sources-json"
    node_dict, edge_dict = utils.load_dict()
    node_dict = {v: k for k, v in node_dict.items()}
    edge_dict = {v: k for k, v in edge_dict.items()}

    # for i, f in enumerate(os.listdir(dir_path1)):
    #     df = pd.read_csv(rf"{dir_path1}\{f}")
    #     ls = []
    #     for _, row in df.iterrows():
    #         # print(dict(row))
    #         ls.append(dict(row))
    #     with open(rf"{dir_path2}\{f[:-3]}json", "w")as jf:
    #         json.dump(ls, jf, indent=4)

    dir_path = rf"C:\D_Drive\ASM\DataSets\weapon-lod\kg_20220720\lg_20220919"
    average_entities = 0
    average_relationships = 0
    for file in os.listdir(dir_path):
        lg = utils.load_lg_graph(rf"{dir_path}\{file}")
        average_entities += len(lg.nodes)
        average_relationships += len(lg.edges)

    average_entities /= len(os.listdir(dir_path))
    average_relationships /= len(os.listdir(dir_path))
    print(average_entities)
    print(average_relationships)

