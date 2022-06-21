package VF2.algorithm;

import VF2.graph.Edge;
import VF2.graph.LGGraph;
import VF2.graph.Node;

import java.util.ArrayList;
import java.util.Arrays;


public class VF2 {
    //输入查询图和目标图，返回子图集合
    public ArrayList<int[]> matchGraphSetWithQuery(LGGraph targetGraph, LGGraph queryGraph) {
        ArrayList<int[]> stateSet = new ArrayList<int[]>();
        State state = new State(targetGraph, queryGraph);
        matchRecursive(state, stateSet, targetGraph, queryGraph);
        return stateSet;
    }

    //匹配
    private void matchRecursive(State state, ArrayList<int[]> stateSet, LGGraph targetGraph, LGGraph queryGraph) {

        if (state.depth == queryGraph.nodes.size()) {    // 找到一个匹配子图
            state.matched = true;
            int[] arr = Arrays.copyOf(state.core_2,state.core_2.length);
//            for(int[] arr2 : stateSet){
//                for (int q = 0 ; q < arr2.length ; q++) {
//                    System.out.print("(" + arr2[q] + "-" + q + ") ");
//                }
//                System.out.println();
//            }
            stateSet.add(arr);
        } else {    // 继续寻找子图
            ArrayList<Pair<Integer, Integer>> candidatePairs = genCandidatePairs(state, targetGraph, queryGraph);
            for (Pair<Integer, Integer> entry : candidatePairs) {
                if (checkFeasibility(state, entry.getKey(), entry.getValue())){
                    state.extendMatch(entry.getKey(), entry.getValue()); // 深入下一个可能节点
                    matchRecursive(state, stateSet, targetGraph, queryGraph);
                    state.backtrack(entry.getKey(), entry.getValue()); // 返回前一个状态
                }
            }
        }
    }


    //返回待筛选的匹配对
    private ArrayList<Pair<Integer, Integer>> genCandidatePairs(State state, LGGraph targetGraph, LGGraph queryGraph) {
        ArrayList<Pair<Integer, Integer>> pairList = new ArrayList<Pair<Integer, Integer>>();

        if (!state.T1out.isEmpty() && !state.T2out.isEmpty()) { //判断是否有未配对且作为外边的另一端节点
            int G2id = -1;
            for (int i : state.T2out) {
                G2id = Math.max(i, G2id);
            }
            for (int i : state.T1out) {
                pairList.add(new Pair<Integer,Integer>(i, G2id));
            }
            return pairList;
        } else if (!state.T1in.isEmpty() && !state.T2in.isEmpty()) { //判断是否有未配对且作为内边的另一端节点
            int G2id = -1;
            for (int i : state.T2in) {
                G2id = Math.max(i, G2id);
            }
            for (int i : state.T1in) {
                pairList.add(new Pair<Integer,Integer>(i, G2id));
            }
            return pairList;
        } else { //无起点，无终点，从未配对节点集合中配对
            int G2id = -1;
            for (int i : state.unmapped2) {
                G2id = Math.max(i, G2id);
            }
            for (int i : state.unmapped1) {
                pairList.add(new Pair<Integer,Integer>(i, G2id));
            }
            return pairList;
        }
    }


    private Boolean checkFeasibility(State state , int G1id , int G2id) {

        Node G1Node = state.targetGraph.nodes.get(G1id);
        Node G2Node = state.queryGraph.nodes.get(G2id);

        return  checkPredAndSucc(state, G1Node, G2Node)&&
                checkInAndOut(state, G1Node, G2Node)&&
                checkNew(state, G1Node, G2Node);

    }

    private Boolean checkPredAndSucc(State state, Node G1Node , Node G2Node) {

        if (G1Node.label != G2Node.label){
            return false;
        }

        Node G1NodeCopy = G1Node.copy();
        Node G2NodeCopy = G2Node.copy();

        for (Edge edge : G1Node.inEdges) {
            Node G1pred = edge.source;
            if (state.inM1(G1pred.id)){
                Node G2pred = new Node(state.core_1[G1pred.id],G1pred.label);
                Edge e = new Edge(G2pred,G2NodeCopy,edge.label);
                if (!G2Node.inEdges.contains(e)){
                    return false;
                }
            }
        }

        for(Edge edge : G1Node.outEdges){
            Node G1succ = edge.target;
            if (state.inM1(G1succ.id)){
                Node G2succ = new Node(state.core_1[G1succ.id],G1succ.label);
                Edge e = new Edge(G2NodeCopy,G2succ,edge.label);
                if (!G2Node.outEdges.contains(e)){
                    return false;
                }
            }
        }

        for(Edge edge : G2Node.inEdges){
            Node G2pred = edge.source;
            if (state.inM2(G2pred.id)){
                Node G1pred = new Node(state.core_2[G2pred.id], G2pred.label);
                Edge e = new Edge(G1pred,G1NodeCopy,edge.label);
                if (!G1Node.inEdges.contains(e)){
                    return false;
                }
            }
        }

        for (Edge edge : G2Node.outEdges){
            Node G2succ = edge.target;
            if (state.inM2(G2succ.id)){
                Node G1succ = new Node(state.core_2[G2succ.id],G2succ.label);
                Edge e = new Edge(G1NodeCopy,G1succ,edge.label);
                if (!G1Node.outEdges.contains(e)){
                    return false;
                }
            }
        }

        return true;
    }

    private boolean checkInAndOut(State state, Node G1Node , Node G2Node) {


        int G1PredCnt = 0, G1SucCnt = 0;
        int G2PredCnt = 0, G2SucCnt = 0;

        for (Node node : G1Node.preds){
            if (state.inT1in(node.id)){
                G1PredCnt++;
            }
        }
        for (Node node : G1Node.succs){
            if (state.inT1in(node.id)){
                G1SucCnt++;
            }
        }
        for (Node node : G2Node.preds){
            if (state.inT2in(node.id)){
                G2PredCnt++;
            }
        }
        for (Node node : G2Node.succs){
            if (state.inT2in(node.id)){
                G2SucCnt++;
            }
        }

        if (G1PredCnt < G2PredCnt || G1SucCnt < G2SucCnt){
            return false;
        }

        for (Node node : G1Node.preds){
            if (state.inT1out(node.id)){
                G1PredCnt++;
            }
        }
        for (Node node : G1Node.succs){
            if (state.inT1out(node.id)){
                G1SucCnt++;
            }
        }
        for (Node node : G2Node.preds){
            if (state.inT2out(node.id)){
                G2PredCnt++;
            }
        }
        for (Node node : G2Node.succs){
            if (state.inT2out(node.id)){
                G2SucCnt++;
            }
        }

        if (G1PredCnt < G2PredCnt || G1SucCnt < G2SucCnt){
            return false;
        }

        return true;
    }

    private boolean checkNew(State state, Node G1Node , Node G2Node){



        int G1PredCnt = 0, G1SucCnt = 0;
        int G2PredCnt = 0, G2SucCnt = 0;
        for (Node node : G1Node.preds){
            if (state.inN1Tilde(node.id)){
                G1PredCnt++;
            }
        }
        for (Node node : G1Node.succs){
            if (state.inN1Tilde(node.id)){
                G1SucCnt++;
            }
        }
        for (Node node : G2Node.preds){
            if (state.inN2Tilde(node.id)){
                G2PredCnt++;
            }
        }
        for (Node node : G2Node.succs){
            if (state.inN2Tilde(node.id)){
                G2SucCnt++;
            }
        }

        if (G1PredCnt < G2PredCnt || G1SucCnt < G2SucCnt){
            return false;
        }

        return true;
    }
}




