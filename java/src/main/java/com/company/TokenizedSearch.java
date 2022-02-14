package com.company;

import java.util.*;

public class TokenizedSearch<VALUE_TYPE> implements SearchAble<VALUE_TYPE> {
    private final Map<String, Set<VALUE_TYPE>> dict;

    public TokenizedSearch(List<String> Ks, List<VALUE_TYPE> Vs) {
        dict = new HashMap<>();
        if (Ks == null || Vs == null) {
            assert (Ks == Vs);
            return;
        }
        assert (Ks.size() == Vs.size());
        for (int i = 0; i < Ks.size(); i++) {
            if (!dict.containsKey(Ks.get(i))) {
                dict.put(Ks.get(i), new HashSet<>());
            }
            dict.get(Ks.get(i)).add(Vs.get(i));
        }
    }

    @Override
    public Set<VALUE_TYPE> searchFor(String K) {
        //K+" " -> good for suffix tree
        if (dict.containsKey(K + " ")) {
            return Collections.unmodifiableSet(dict.get(K + " "));
        }
        return Set.of();
    }
}
