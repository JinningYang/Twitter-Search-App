package com.company;

import java.util.*;

public class MultiPartitionSuffixTree<VALUE_TYPE> implements SuffixTree<VALUE_TYPE> {
    private ArrayList<SimpleSuffixTree<VALUE_TYPE>> trees;

    public MultiPartitionSuffixTree(List<String> Ks, List<VALUE_TYPE> Vs) {
        this(Ks, Vs, 0);
    }

    public MultiPartitionSuffixTree(List<String> Ks, List<VALUE_TYPE> Vs, int nPartition) {
        if (Ks == null || Vs == null) {
            assert (Ks == Vs);
            return;
        }
        assert (Ks.size() == Vs.size());
        assert (nPartition >= 0);
        if (nPartition == 0) {
            int ksPerPartition = 20000;
            if (Ks.size() > 60000) {
                ksPerPartition = 10000;
            }
            if (Ks.size() > 120000) {
                ksPerPartition = 5000;
            }
            //round
            nPartition = Math.max((Ks.size() + ksPerPartition / 2) / ksPerPartition, 1);
        }
        List<List<String>> KSegs = new ArrayList<>();
        List<List<VALUE_TYPE>> VSegs = new ArrayList<>();
        trees = new ArrayList<>();
        int now = 0;
        int add = Ks.size() / nPartition;
        for (int i = 0; i < nPartition - 1; i++) {
            KSegs.add(Ks.subList(now, now + add));
            VSegs.add(Vs.subList(now, now + add));
            now += add;
        }
        KSegs.add(Ks.subList(now, Ks.size()));
        VSegs.add(Vs.subList(now, Ks.size()));
        for (int i = 0; i < nPartition; i++) {
            trees.add(new SimpleSuffixTree<>(KSegs.get(i), VSegs.get(i)));
        }
    }

    @Override
    public Set<VALUE_TYPE> searchFor(String K) {
        Set<VALUE_TYPE> ans = new HashSet<>();
        for (var tree : trees) {
            ans.addAll(tree.searchFor(K));
        }
        return Collections.unmodifiableSet(ans);
    }
}
