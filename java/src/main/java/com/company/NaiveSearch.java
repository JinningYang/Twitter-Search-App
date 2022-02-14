package com.company;

import java.util.*;

public class NaiveSearch<VALUE_TYPE> implements SearchAble<VALUE_TYPE> {
    private final List<String> strings;
    private final List<VALUE_TYPE> values;

    public NaiveSearch(List<String> Ks, List<VALUE_TYPE> Vs) {
        strings = new ArrayList<>();
        values = new ArrayList<>();
        if (Ks == null || Vs == null) {
            //要么一起==null
            assert (Ks == Vs);
            return;
        }
        assert (Ks.size() == Vs.size());
        strings.addAll(Ks);
        values.addAll(Vs);
    }

    @Override
    public Set<VALUE_TYPE> searchFor(String K) {
        Set<VALUE_TYPE> ret = new HashSet<>();
        for (int i = 0; i < strings.size(); i++) {
            if (strings.get(i).contains(K)) {
                ret.add(values.get(i));
            }
        }
        return Collections.unmodifiableSet(ret);
    }
}
