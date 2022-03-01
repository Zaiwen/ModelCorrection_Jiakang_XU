package VF2.graph;

public class Edge {

    public Node source; //起点
    public Node target; //终点
    public int label;


    public Edge(Node source, Node target, int label) {
        this.source = source;
        source.succs.add(target);
        source.outEdges.add(this);
        this.target = target;
        target.preds.add(source);
        target.inEdges.add(this);
        this.label = label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return label == edge.label &&
                source.equals(edge.source)&&
                target.equals(edge.target);
    }

}
