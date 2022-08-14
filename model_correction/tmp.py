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
import json

sys.path.append("")


if __name__ == '__main__':

    dir_path = rf"C:\D_Drive\ASM\DataSets\museum-crm\rdf_20220428"
    # print(len(set(ls)))
    # rdf_file = open(rf"C:\D_Drive\ASM\DataSets\museum-crm\KG\museum_kg_20220805.ttl", "w", encoding="utf-8")
    # for file in os.listdir(dir_path):
    #     print(file)
    #     rdf_file = open(rf"C:\D_Drive\ASM\DataSets\museum-crm\KG\rdf\museum_crm_kg_{file}", "w", encoding="utf-8")
    #     for file1 in os.listdir(dir_path):
    #         if file != file1:
    #             with open(rf"{dir_path}\{file1}", "r", encoding="utf-8")as f:
    #                 rdf_file.write(f.read())
    #     rdf_file.close()

    # kg = utils.load_lg_graph(rf"C:\D_Drive\ASM\DataSets\museum-edm\kg_20220802\lg\museum_edm_kg_s9.lg")
    # kg_edges = set()
    # for edge in kg.edges.data():
    #     s = edge[0]
    #     t = edge[1]
    #     s_node = kg.nodes[s]['label']
    #     t_node = kg.nodes[t]['label']
    #     kg_edges.add((s_node, t_node, edge[2]['label']))
    # for e in kg_edges:
    #     print(rf"validEdges.add(new ValidEdge({e[0]}, {e[2]}, {e[1]}));")

    s = "seller=0, expires=1, contactPoint=2, postalCode=3, description=4, mainEntityOfPage=5, manufacturer=6, relatedTo=7, phoneAreaCode=8, phoneSubscriberNumber=9, price=10, copyrightYear=11, familyName=12, acceptedPaymentMethod=13, datePosted=14, addressRegion=15, email=16, itemCondition=17, identifier=18, availableAtOrFrom=19, address=20, givenName=21, telephone=22, itemOffered=23, streetAddress=24, name=25, unidentifiedNumber=26, faxNumber=27, category=28"
    for e in s.split(","):
        entity = e.strip().split("=")[0]
        label = e.strip().split("=")[1]
        print(rf'"{entity}":{label},')
