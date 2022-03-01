package VF2.graph;

import java.util.ArrayList;

public class Node {


    public int id ; //节点的编号
    public int label;

    public ArrayList<Node> preds = new ArrayList<>();
    public ArrayList<Node> succs = new ArrayList<>();
    public ArrayList<Edge> inEdges = new ArrayList<>();
    public ArrayList<Edge> outEdges = new ArrayList<>();

    //创建新节点
    public Node(int id, int label) {
        this.id = id;
        this.label = label;
    }

    public Node copy(){
        Node node = new Node(this.id, this.label);
        return node;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return id == node.id && label == node.label;
    }

}
