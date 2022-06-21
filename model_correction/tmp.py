import os
import sys
import re
import networkx as nx
import random

sys.path.append("")
import utils

if __name__ == '__main__':

    g = utils.load_lg_graph(r"C:\D_Drive\rdf.lg")
    utils.save_csv_graph(utils.lg_to_csv(g), r"C:\D_Drive\rdf.csv")