import sys
sys.path.append("")

import os
import utils


def classNodes():
    os.chdir("cytoscape")
    c_model = utils.load_graph_from_csv("correct_model.csv")
    k_model = utils.load_graph_from_csv("candidate_model.csv")
    m_model = utils.load_graph_from_csv("model.csv")
    mg = utils.merge_graph(c_model, k_model, "__cm", "__km")
    mcs = utils.get_mcs(k_model, c_model)

    grayNodes = [node+"__km" for node in k_model.nodes if node not in m_model]
    yellowNodes = [node+"__cm" for node in mcs.nodes]
    blueNodes = [node for node in mg.nodes if (node.endswith("km") and node not in grayNodes)]
    orangeNodes = [node for node in mg.nodes if (node.endswith("cm") and node not in yellowNodes)]
    os.chdir(os.pardir)
    return grayNodes, yellowNodes, blueNodes, orangeNodes


def markColorCmd():
    mc_cmd = ""
    grayNodes, yellowNodes, blueNodes, orangeNodes = classNodes()

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


def importNetWorkCmd():
    graphs = [os.getcwd() + '\\cytoscape\\' + graph for graph in os.listdir("cytoscape")]
    start = f'network import file file="'
    end = f'" indexColumnSourceInteraction=1 indexColumnTargetInteraction=2 indexColumnTypeInteraction=3 '
    end += f'startLoadRow=1 firstRowAsColumnNames=true rootNetworkList="-- Create new network collection --"\n'
    cmd = ""
    for graph in graphs:
        cmd += start + graph + end

    return cmd.replace('\\', '/')


def saveSessionCmd():
    sf = os.getcwd() + r"\result.cys"
    return f'''session save file="{sf}"\n'''.replace('\\', '/')


def gen_cmd_file(dir):
    os.chdir(dir)
    files = [file for file in os.listdir() if file.startswith('newSource')]
    cmd = ''
    for file in files:
        os.chdir(file)
        try:
            cmd += "session new\n"
            cmd += importNetWorkCmd()
            if "result.csv" in os.listdir("cytoscape"):
                cmd += '''network set current network="result.csv"\n'''
                cmd += markColorCmd()
            cmd += saveSessionCmd()
            cmd += "command sleep duration=0.5\n"
            cmd += "\n"
            print(cmd)
        except Exception as e:
            pass
        os.chdir(os.pardir)
    with open('cmd.txt', 'w')as f:
        f.write(cmd)


if __name__ == '__main__':

    trains = [2, 5, 6]

    dir = rf"C:\D_Drive\ASM\experiment\exp_20220322\train_{trains[0]}_{trains[1]}_{trains[2]}"

    gen_cmd_file(dir)
