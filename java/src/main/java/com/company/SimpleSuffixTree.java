package com.company;

import java.util.*;


public class SimpleSuffixTree<VALUE_TYPE> implements SuffixTree<VALUE_TYPE> {
    private final Node root;

    // This is a modified version of suffix tree. Basically in this version we will put all key strings into one suffix
    // tree, and then add a list of values in the tree's node.
    public SimpleSuffixTree(List<String> Ks, List<VALUE_TYPE> Vs) {
        // Key at index i has value of V at index i.
        // I know Java can do pair, but that's in JavaFX. Who wants to import JavaFX just for Pair?
        root = new Node();
        if (Ks == null || Vs == null) {
            assert (Ks == Vs);
            return;
        }
        assert (Ks.size() == Vs.size());
        buildTree(Ks, Vs);
        trimTree();
    }

    @Override
    public Set<VALUE_TYPE> searchFor(String K) {
        HashSet<VALUE_TYPE> result = new HashSet<>();
        var nowNode = root;
        var nowCharIdx = -1;
        var failed = false;
        for (int i = 0; i < K.length(); i++) {
            char c = K.charAt(i);
            if (nowNode.chars.size() != nowCharIdx + 1) {
                nowCharIdx++;
                if (c != nowNode.chars.get(nowCharIdx)) {
                    failed = true;
                    break;
                }
            } else {
                if (!nowNode.edges.containsKey(c)) {
                    failed = true;
                    break;
                }
                nowNode = nowNode.edges.get(c);
                nowCharIdx = -1;
            }
        }
        if (!failed) {
            result.addAll(nowNode.values);
        }
        return result;
    }

    private void buildTree(List<String> Ks, List<VALUE_TYPE> Vs) {
        for (int i = 0; i < Ks.size(); i++) {
            addSingle(Ks.get(i), Vs.get(i));
        }
    }

    private void addSingle(String K, VALUE_TYPE V) {
        root.values.add(V); // An empty string will match another empty string.
        for (int i = 0; i < K.length(); i++) {
            var now = root;
            for (int j = i; j < K.length(); j++) {
                now.values.add(V);
                char c = K.charAt(j);
                if (!now.edges.containsKey(c)) {
                    now.edges.put(c, new Node());
                }
                now = now.edges.get(c);
                now.values.add(V);
            }
        }
    }

    private void trimTree() {
        // Trim the trie tree in `root` so that it turns from something like:
        //     A
        //      \
        //       B
        //      / \
        //     C   F
        //    /     \
        //   D       G
        //  /       / \
        // E       H   I
        //
        // Into this less deep tree:
        //     A
        //      \
        //       B
        //      / \
        //   CDE   FG
        //        /  \
        //       H    I
        //
        // To reduce time complexity at search.
        Stack<Node> st = new Stack<>();
        st.push(root);
        while (!st.empty()) {
            var now = st.pop();
            while (now.edges.size() == 1) {
                Character c = null;
                for (var k : now.edges.keySet()) {
                    c = k;
                    break;
                }
                assert (c != null);
                now.chars.add(c);
                now.values.addAll(now.edges.get(c).values);
                now.edges = now.edges.get(c).edges;
            }
            for (var e : now.edges.values()) {
                st.push(e);
            }
        }
    }

    private class Node {
        public Set<VALUE_TYPE> values = new HashSet<>();
        public ArrayList<Character> chars = new ArrayList<>();//->缩点
        public HashMap<Character, Node> edges = new HashMap<>();
    }
}
