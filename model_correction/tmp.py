import os
import sys
import re
import networkx as nx
import random

sys.path.append("")
import utils

if __name__ == '__main__':

    random.seed(10)
    ls = []
    for _ in range(10):
        c = "#"
        for i in range(6):
            c += random.choice("0123456789ABCDEF")
        ls.append(c)
        # print(c)
    print(ls)