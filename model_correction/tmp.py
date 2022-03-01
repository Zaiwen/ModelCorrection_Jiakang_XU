import os
import sys
import re
import networkx as nx

sys.path.append("")
import utils

if __name__ == '__main__':
    PATH = r"D:\ASM\experiment\exp_20220218\(2, 5, 6)-16"

    graph = utils.load_lg_graph(f"{PATH}\\result.lg")
    utils.save_csv_graph(utils.lg_to_csv(graph), f"{PATH}\\result.csv")
    # result_graph = utils.load_lg_graph(f"{PATH}\\(9, 11)-10\\result_with_domain_constraint.lg")
    # utils.save_csv_graph(utils.lg_to_csv(result_graph), f"{PATH}\\(9, 11)-10\\result_with_dc.csv")
    # for dir in os.listdir(PATH):
    #     try:
    #         result_graph = utils.load_lg_graph(f"{PATH}\\{dir}\\result.lg")
    #         utils.save_csv_graph(utils.lg_to_csv(result_graph), f"{PATH}\\{dir}\\result.csv")
    #     except Exception:
    #         pass

    # for file in os.listdir(PATH):
    #     model_graph = utils.load_lg_graph(f"{PATH}\\{file}")
    #     utils.save_csv_graph(utils.lg_to_csv(model_graph), f"{PATH}\\{file}.csv")


