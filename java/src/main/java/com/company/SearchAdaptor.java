package com.company;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SearchAdaptor {

    private final ConcurrentSearchHelper<Integer> searchHelper;
    private final AutoCorrector autoCorrector;
    private boolean useMultiThread;

    public SearchAdaptor(List<String> searchList, SearchAlgorithm searchAlgorithm) {
        //if the algo is NaiveSearch||TokenizedSearch -> then we don't need to use MultiThread
        this(searchList, searchAlgorithm, !(searchAlgorithm == SearchAlgorithm.NaiveSearch ||
                searchAlgorithm == SearchAlgorithm.TokenizedSearch));
    }

    public SearchAdaptor(List<String> searchList, SearchAlgorithm searchAlgorithm, boolean useMultiThread) {

        this.useMultiThread = useMultiThread;
        assert (searchList != null);
        SearchBundle bundle;
        SearchAble<Integer> searchEngine = null;
        if (searchAlgorithm == SearchAlgorithm.SimpleTokenizedSuffixTree ||
                searchAlgorithm == SearchAlgorithm.MultiPartitionTokenizedSuffixTree ||
                searchAlgorithm == SearchAlgorithm.TokenizedSearch) {
            bundle = generateBundle(searchList, BundleGenAlgorithm.BreakIntoTokens);
        } else {
            bundle = generateBundle(searchList, BundleGenAlgorithm.AsIs);
        }
        switch (searchAlgorithm) {
            case NaiveSearch:
                searchEngine = new NaiveSearch<>(bundle.Ks, bundle.Vs);
                break;
            case TokenizedSearch:
                searchEngine = new TokenizedSearch<>(bundle.Ks, bundle.Vs);
                break;
            case SimpleSuffixTree:
            case SimpleTokenizedSuffixTree:
                searchEngine = new SimpleSuffixTree<>(bundle.Ks, bundle.Vs);
                break;
            case MultiPartitionSuffixTree:
            case MultiPartitionTokenizedSuffixTree:
                searchEngine = new MultiPartitionSuffixTree<>(bundle.Ks, bundle.Vs);
                break;
            default:
                assert (false);
        }
        //multi-thread search: 比如有10000个关键词， 把它们break into 1000个关键词的查找任务，并行跑->快
        searchHelper = new ConcurrentSearchHelper<>(searchEngine);
        //not implement->if the user has a type fault, we can correct it
        autoCorrector = new AutoCorrector(searchList);
    }

    public void setUseMultiThread(boolean useMultiThread) {
        this.useMultiThread = useMultiThread;
    }

    public Set<Integer> searchFor(String keyString) {
        return searchHelper.searchInSingleThread(List.of(keyString));
    }

    public Set<Integer> searchFor(List<String> keywords) {
        return searchFor(keywords, 0);
    }

    private Set<Integer> searchFor(List<String> keywords, int depth) {
        // How many rounds of spell correction do we want?
        int MAX_DEPTH = 5;
        if (depth >= MAX_DEPTH) {
            return Set.of();
        }
        assert (keywords != null);
        Set<Integer> result = useMultiThread ? searchHelper.searchInMultiThread(keywords)
                : searchHelper.searchInSingleThread(keywords);
        if (result != null && result.size() == 0) {
            List<String> newSearchKeywords = new ArrayList<>();
            for (var k : keywords) {
                newSearchKeywords.add(autoCorrector.TryCorrectSpelling(k));
            }
            //if the autoCorrector made some corrections, then we research, and depth +1;
            if (!newSearchKeywords.equals(keywords)) {
                return searchFor(newSearchKeywords, depth + 1);
            }
        }
        return result == null ? Set.of() : Collections.unmodifiableSet(result);
    }

    SearchBundle generateBundle(List<String> searchList, BundleGenAlgorithm algorithm) {
        SearchBundle bundle = new SearchBundle();
        switch (algorithm) {
            //Key : String->Context; Value : Index
            case AsIs:
                bundle.Ks = new ArrayList<>();
                for (var k : searchList) {
                    bundle.Ks.add(k.trim() + " ");
                }
                List<Integer> list = new ArrayList<>();
                int bound = searchList.size() - 1;
                for (int i1 = 0; i1 <= bound; i1++) {
                    Integer integer = i1;
                    list.add(integer);
                }
                bundle.Vs = list;
                break;
            case BreakIntoTokens:
                bundle.Ks = new ArrayList<>();
                bundle.Vs = new ArrayList<>();
                for (int i = 0; i < searchList.size(); i++) {
                    for (var k : searchList.get(i).split(" ")) {
                        bundle.Ks.add(k.trim() + " ");
                        bundle.Vs.add(i);
                    }
                }
                break;
            default:
                assert (false);
        }
        return bundle;
    }

    /*
     * NaiveSearch: Naively grep through all tweets.
     * TokenizedSearch: Separate all tweets into tokens, and then store them in a hashmap where its key is the token
     *                  and its value is the idx of tweets that contains this token.
     * SimpleSuffixTree: Put tweets into a suffix tree.
     * MultiPartitionSuffixTree: Same as SimpleSuffixTree, but using a multi-partition impl of suffix tree.
     * TokenizedSuffixTree: Only put tokens in a tweet into a suffix tree, but not the whole tweet.
     * */
    public enum SearchAlgorithm {
        NaiveSearch,
        TokenizedSearch,
        SimpleSuffixTree,
        MultiPartitionSuffixTree,
        SimpleTokenizedSuffixTree,
        MultiPartitionTokenizedSuffixTree
    }

    private enum BundleGenAlgorithm {
        BreakIntoTokens,
        AsIs
    }

    private static class SearchBundle {
        //Key-Value Pair
        public List<String> Ks;
        public List<Integer> Vs;
    }
}
