import sys
sys.path.append("")

import os
import utils


def classNodes(path):
    c_model = utils.load_graph_from_csv(path + "\\correct_model.csv")
    k_model = utils.load_graph_from_csv(path + "\\candidate_model.csv")
    m_model = utils.load_graph_from_csv(path + "\\model.csv")
    mg = utils.merge_graph(c_model, k_model, "__cm", "__km")
    mcs = utils.get_mcs(k_model, c_model)

    grayNodes = [node+"__km" for node in k_model.nodes if node not in m_model]
    yellowNodes = [node+"__cm" for node in mcs.nodes]
    blueNodes = [node for node in mg.nodes if (node.endswith("km") and node not in grayNodes)]
    orangeNodes = [node for node in mg.nodes if (node.endswith("cm") and node not in yellowNodes)]

    return grayNodes, yellowNodes, blueNodes, orangeNodes


def markColorCmd(graph_path):
    mc_cmd = ""
    path = graph_path[:graph_path.rfind("/")]
    # print(path)
    grayNodes, yellowNodes, blueNodes, orangeNodes = classNodes(path)

    def _markColor(nodes, color):
        _mc_cmd = f'node set properties network=current nodeList="{",".join(nodes)}" bypass=true ' \
                  f''
        _mc_cmd += f'propertyList="fill color" valueList="{color}"\n'
        return _mc_cmd

    mc_cmd += _markColor(blueNodes, "lightblue")
    mc_cmd += _markColor(yellowNodes, "yellow")
    mc_cmd += _markColor(orangeNodes, "orange")
    mc_cmd += _markColor(grayNodes, "gray")
    return mc_cmd


def setProperties(graph_path):

    graph = utils.load_graph_from_csv1(graph_path)

    cmd = f'''node set properties bypass=true network=current nodeList=all'''
    cmd += f''' propertyList="fill color" valueList="orange"\n'''

    candidate_types = [node for node in graph.nodes if "__ct_" in str(node)]

    cmd += f'''node set properties bypass=true network=current nodeList="{",".join(candidate_types)}"'''
    cmd += f''' propertyList="fill color" valueList="#FCBBA1"\n'''

    column_nodes = [node for node in graph.nodes if graph.nodes[node]["nodeType"] == "columnNode"]
    cmd += f'''node set properties bypass=true network=current nodeList="{",".join(column_nodes)}"'''
    cmd += ''' propertyList="Shape,fill color" valueList="Rectangle,lightblue"\n'''

    data_properties = [" ".join([edge[0], "("+edge[2]["label"]+")", edge[1]]) for edge in graph.edges.data() if edge[1] in column_nodes]
    cmd += f'''edge set properties bypass=true network=current edgeList="{",".join(data_properties)}"'''
    cmd += ''' propertyList="Line Type,Target Arrow Shape" valueList="dot,None"\n'''

    return cmd


def importNetWorkCmd(graph_path):
    cmd = f'network import file file={graph_path}"'
    cmd += f'" indexColumnSourceInteraction=1 indexColumnTargetInteraction=2 indexColumnTypeInteraction=3 '
    cmd += f'startLoadRow=1 firstRowAsColumnNames=true rootNetworkList="-- Create new network collection --"\n'
    return cmd.replace('\\', '/')


def saveSessionCmd(source):
    sf = os.getcwd() + "\\" + source + r"\result.cys"
    return f'''session save file="{sf}"\n'''.replace('\\', '/')


def gen_cmd_file(dir):
    os.chdir(dir)
    sources = [file for file in os.listdir() if file.startswith('newSource')]
    # files = ['candidate_model.csv', 'correct_model.csv', 'model.csv', 'result.csv']
    # print(sources)
    cmd = ''
    for source in sources:
        files = [file for file in os.listdir(f"{os.getcwd()}\\{source}\\cytoscape")]
        # print(files)
        cmd += "session new\n"
        for file in files:
            graph_path = f"{dir}\\{source}\\cytoscape\\{file}".replace("\\", "/")
            # if not (file == "candidate_model.csv" or file == "correct_model.csv"):
            try:
                cmd += importNetWorkCmd(graph_path)
                cmd += f'network set current network="{file}"\n'
                cmd += setProperties(graph_path)
                if file == "result.csv":
                    cmd += markColorCmd(graph_path)
                    pass
            except Exception as e:
                pass
        cmd += saveSessionCmd(source)
        cmd += "command sleep duration=0.5\n"

    print(cmd)
        # os.chdir(os.pardir)
    # os.chdir(os.pardir)
    with open(dir + '\\cmd.txt', 'w')as f:
        f.write(cmd)


if __name__ == '__main__':

    trains = [5, 9, 26]

    dir = rf"C:\D_Drive\ASM\experiment\exp_20220613\train_{trains[0]}_{trains[1]}_{trains[2]}___1"

    gen_cmd_file(dir)
