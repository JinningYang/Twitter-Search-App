package com.company;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConcurrentSearchHelper<VALUE_TYPE> {
    private final SearchAble<VALUE_TYPE> searchEngine;

    public ConcurrentSearchHelper(SearchAble<VALUE_TYPE> searchEngine) {
        this.searchEngine = searchEngine;
    }

    public Set<VALUE_TYPE> searchInSingleThread(List<String> keywords) {
        Set<VALUE_TYPE> result = null;
        for (var k : keywords) {
            var tmp = searchEngine.searchFor(k);
            if (result != null) {
                //the matched value
                result.retainAll(tmp);
            } else {
                result = new HashSet<>(tmp);
            }
            if (result.size() == 0) {
                break;
            }
        }
        return result == null ? Set.of() : Collections.unmodifiableSet(result);
    }

    public Set<VALUE_TYPE> searchInMultiThread(List<String> keywords) {
        //check how many processes in the system
        return searchInMultiThread(keywords, Runtime.getRuntime().availableProcessors());
    }

    public Set<VALUE_TYPE> searchInMultiThread(List<String> keywords, int nproc) {
        assert (nproc > 0);
        //10000 keywords -> 1000 keywords, and 10 searches to process it
        List<List<String>> keywordSegs = new ArrayList<>();
        int now = 0;
        int add = Math.max(keywords.size() / nproc, 1);
        for (int i = 0; i < nproc - 1; i++) {
            if (now + add > keywords.size()) {
                break;
            }
            keywordSegs.add(keywords.subList(now, now + add));
            now += add;
        }
        if (now < keywords.size()) {
            keywordSegs.add(keywords.subList(now, keywords.size()));
        }
        ArrayList<Thread> threads = new ArrayList<>();
        //支持并发的写
        List<Set<VALUE_TYPE>> results = new CopyOnWriteArrayList<>();
        for (var keywordSeg : keywordSegs) {
            Thread t = new Thread(() -> {
                if (keywordSeg.size() != 0) {
                    results.add(searchInSingleThread(keywordSeg));
                }
            });
            t.start();
            threads.add(t);
        }
        for (var thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Set<VALUE_TYPE> ans = null;
        for (var result : results) {
            if (ans != null) {
                ans.retainAll(result);
            } else {
                ans = new HashSet<>(result);
            }
        }

        return ans == null ? Set.of() : Collections.unmodifiableSet(ans);
    }

    public SearchAble<VALUE_TYPE> getSearchEngine() {
        return searchEngine;
    }
}
