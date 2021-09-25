import os
import shutil
import json

import utils


def run(kgPath, qryPaths, outPath):
    os.system(f"java -jar E:\\GramiMatcher_jar\\GramiMatcher.jar kgPath={kgPath} qryPaths={','.join(qryPaths)} outPath={outPath}")


if __name__ == '__main__':

    DIR = r"E:\exp_20210830\train_2_5_6"

    os.chdir(r"E:\exp_20210830\2_5_6")
    error_log = open("error.log", "w")


    for i in range(1, 30):
        try:
            if "temp" in os.listdir():
                shutil.rmtree("temp")
            os.mkdir("temp")
            qryPaths = []

            cm = utils.load_graph_from_csv(DIR + rf"\newSource_{i}\cytoscape\correct_model.csv")
            utils.save_lg_graph(utils.csv_to_lg(cm), os.getcwd() + rf"\temp\correct_model.lg")
            qryPaths.append(os.getcwd() + rf"\temp\correct_model.lg")

            model = utils.load_graph_from_csv(DIR + rf"\newSource_{i}\cytoscape\model.csv")
            utils.save_lg_graph(utils.csv_to_lg(model), os.getcwd() + rf"\temp\model.lg")
            qryPaths.append(os.getcwd()+rf"\temp\model.lg")

            removed_edges = {}
            j = 0
            for node in model.nodes:
                if model.degree(node) == 1:
                    model_copy = model.copy()
                    model_copy.remove_node(node)
                    removed_edge = list(model.in_edges(node))[0] if model.in_edges(node) else list(model.out_edges(node))[0]
                    removed_edges[f"e{j}"] = removed_edge
                    modified_model = utils.csv_to_lg(model_copy)
                    utils.save_lg_graph(modified_model, os.getcwd() + rf"\temp\model_e{j}.lg")
                    qryPaths.append(os.getcwd()+rf"\temp\model_e{j}.lg")
                    j += 1
            with open(f"removed_edges_s{i}.json", "w")as f:
                json.dump(removed_edges, f, indent=4)
            run(r"D:\DataMatching\museum_kg_20210830.lg", qryPaths, rf"result_s{i}.csv")
        except Exception as e:
            error_log.write(str(i)+"\n")
            error_log.write(e.__str__()+"\n")
            error_log.flush()

    if "temp" in os.listdir():
        shutil.rmtree("temp")

    error_log.close()
