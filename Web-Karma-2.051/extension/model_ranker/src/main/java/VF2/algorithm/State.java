package VF2.algorithm;

import VF2.graph.LGGraph;
import VF2.graph.Node;

import java.util.HashSet;

public class State {
    public int[] core_1; // 存储目标图节点的配对情况，'-1'表示未配对
    public int[] core_2; // 存储查询图节点的配对情况，'-1'表示未配对

    public int[] in_1;    // 存储目标图节点是否作为内边另一端的节点，'-1'表示不是
    public int[] in_2;    // 存储查询图节点是否作为内边另一端的节点，'-1'表示不是
    public int[] out_1; // 存储目标图节点是否作为外边另一端的节点，'-1'表示不是
    public int[] out_2; // 存储查询图节点是否作为外边另一端的节点，'-1'表示不是

    public HashSet<Integer> T1in;    // 只存储目标图中未配对且作为内边另一端的节点的节点集合
    public HashSet<Integer> T1out;    // 只存储目标图中未配对且作为外边另一端的节点的节点集合
    public HashSet<Integer> T2in;    // 只存储查询图中未配对且作为内边另一端的节点的节点集合
    public HashSet<Integer> T2out;    // 只存储查询图中未配对且作为外边另一端的节点的节点集合

    public HashSet<Integer> unmapped1;    // 目标图中未配对的节点集合
    public HashSet<Integer> unmapped2;    // 查询图中未配对的节点集合

    public int depth = 0; // 子图匹配的深度

    public boolean matched = false;

    public LGGraph targetGraph;
    public LGGraph queryGraph;

    //初始化状态
    public State(LGGraph targetGraph, LGGraph queryGraph) {

        this.targetGraph = targetGraph;
        this.queryGraph = queryGraph;

        int targetSize = targetGraph.nodes.size();
        int querySize = queryGraph.nodes.size();

        T1in = new HashSet<Integer>(targetSize * 2);
        T1out = new HashSet<Integer>(targetSize * 2);
        T2in = new HashSet<Integer>(querySize * 2);
        T2out = new HashSet<Integer>(querySize * 2);

        unmapped1 = new HashSet<Integer>(targetSize * 2);
        unmapped2 = new HashSet<Integer>(querySize * 2);

        core_1 = new int[targetSize];
        core_2 = new int[querySize];

        in_1 = new int[targetSize];
        in_2 = new int[querySize];
        out_1 = new int[targetSize];
        out_2 = new int[querySize];

        //全部设为初始状态
        for (int i = 0; i < targetSize; i++) {
            core_1[i] = -1;
            in_1[i] = -1;
            out_1[i] = -1;
            unmapped1.add(i);
        }
        for (int i = 0; i < querySize; i++) {
            core_2[i] = -1;
            in_2[i] = -1;
            out_2[i] = -1;
            unmapped2.add(i);
        }
    }

    //判断当前目标图节点的配对情况
    public Boolean inM1(int nodeId) { //目标图节点是否映射
        return (core_1[nodeId] > -1);
    }

    //判断当前查询图节点的配对情况
    public Boolean inM2(int nodeId) { //查询图节点是否映射
        return (core_2[nodeId] > -1);
    }

    //判断当前目标图节点是否未配对且作为内边另一端的节点
    public Boolean inT1in(int nodeId) { //目标图节点是否未映射且属于终点
        return ((core_1[nodeId] == -1) && (in_1[nodeId] > -1));
    }

    //判断当前查询图节点是否未配对且作为内边另一端的节点
    public Boolean inT2in(int nodeId) { //查询图节点是否未映射且属于终点
        return ((core_2[nodeId] == -1) && (in_2[nodeId] > -1));
    }

    //判断当前目标图节点是否未配对且作为外边另一端的节点
    public Boolean inT1out(int nodeId) { //目标图节点是否未映射且属于起点
        return ((core_1[nodeId] == -1) && (out_1[nodeId] > -1));
    }

    //判断当前查询图节点是否未配对且作为外边另一端的节点
    public Boolean inT2out(int nodeId) { //查询图节点是否未映射且属于起点
        return ((core_2[nodeId] == -1) && (out_2[nodeId] > -1));
    }

    //判断当前目标图节点是否(未配对)且作为(内边或外边另一端的节点)
    public Boolean inT1(int nodeId) { //目标图节点是否属于起点或终点
        return (this.inT1in(nodeId) || this.inT1out(nodeId));
    }

    //判断当前查询图节点是否(未配对)且作为(内边或外边另一端的节点)
    public Boolean inT2(int nodeId) {
        return (this.inT2in(nodeId) || this.inT2out(nodeId));
    }

    public Boolean inN1Tilde(int nodeId) { //判断目标图节点是否未配对且不作为内边另一端的节点且不作为外边另一端的节点
        return ((core_1[nodeId] == -1) && (in_1[nodeId] == -1) && (out_1[nodeId] == -1));
    }

    public Boolean inN2Tilde(int nodeId) { //判断查询图节点是否未配对且不作为内边另一端的节点且不作为外边另一端的节点
        return ((core_2[nodeId] == -1) && (in_2[nodeId] == -1) && (out_2[nodeId] == -1));
    }

    //节点匹配成功，继续深入
    public void extendMatch(int targetIndex, int queryIndex) {

        core_1[targetIndex] = queryIndex;
        core_2[queryIndex] = targetIndex;
        unmapped1.remove(targetIndex);
        unmapped2.remove(queryIndex);
        T1in.remove(targetIndex);
        T1out.remove(targetIndex);
        T2in.remove(queryIndex);
        T2out.remove(queryIndex);

        depth++;

        Node targetNode = targetGraph.nodes.get(targetIndex);
        Node queryNode = queryGraph.nodes.get(queryIndex);

        for (Node node : targetNode.preds) {
            if (in_1[node.id] == -1) { //相邻的节点未处理过
                in_1[node.id] = depth;
                if (!inM1(node.id)) //相邻的节点未配对
                    T1in.add(node.id);
            }
        }

        for (Node node : targetNode.succs) {
            if (out_1[node.id] == -1) {
                out_1[node.id] = depth;
                if (!inM1(node.id))
                    T1out.add(node.id);
            }
        }

        for (Node node : queryNode.preds) {
            if (in_2[node.id] == -1) {
                in_2[node.id] = depth;
                if (!inM2(node.id))
                    T2in.add(node.id);
            }
        }

        for (Node node : queryNode.succs) {
            if (out_2[node.id] == -1) {
                out_2[node.id] = depth;
                if (!inM2(node.id))
                    T2out.add(node.id);
            }
        }

    }

    //回溯，回到深入前的状态
    public void backtrack(int targetNodeIndex, int queryNodeIndex) {

        core_1[targetNodeIndex] = -1;
        core_2[queryNodeIndex] = -1;
        unmapped1.add(targetNodeIndex);
        unmapped2.add(queryNodeIndex);

        for (int i = 0; i < core_1.length; i++) {
            if (in_1[i] == depth) {
                in_1[i] = -1;
                T1in.remove(i);
            }
            if (out_1[i] == depth) {
                out_1[i] = -1;
                T1out.remove(i);
            }
        }
        for (int i = 0; i < core_2.length; i++) {
            if (in_2[i] == depth) {
                in_2[i] = -1;
                T2in.remove(i);
            }
            if (out_2[i] == depth) {
                out_2[i] = -1;
                T2out.remove(i);
            }
        }

        if (inT1in(targetNodeIndex))
            T1in.add(targetNodeIndex);
        if (inT1out(targetNodeIndex))
            T1out.add(targetNodeIndex);
        if (inT2in(queryNodeIndex))
            T2in.add(queryNodeIndex);
        if (inT2out(queryNodeIndex))
            T2out.add(queryNodeIndex);

        depth--;
    }
}
