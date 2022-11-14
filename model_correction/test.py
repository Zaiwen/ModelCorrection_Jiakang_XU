import networkx as nx
import os
import json
import sys
sys.path.append("")
import utils

import rdflib



if __name__ == '__main__':

    for file in os.listdir(rf"C:\D_Drive\ASM\DataSets\museum-crm\sources-modified-20210828"):
        with open(rf"C:\D_Drive\ASM\DataSets\museum-crm\sources-modified-20210828\{file}", 'r', encoding="utf-8", errors="ignore") as f:
            text = f.read()
            with open(rf"C:\D_Drive\ASM\DataSets\museum-crm\sources-modified-20210828\{file}", 'w', encoding="utf-8", errors="ignore") as f1:
                f1.write(text)
