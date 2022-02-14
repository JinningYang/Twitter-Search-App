package com.company;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TweetIndex {
    private final SearchAdaptor.SearchAlgorithm searchAlgorithm;
    private boolean useMultiThread;
    private List<Tweet> tweets;
    private SearchAdaptor tweetSearch;
    private Recommender recommender;

    //enum: SearchAdaptor.SearchAlgorithm.MultiPartitionSuffixTree
    public TweetIndex() {
        this(SearchAdaptor.SearchAlgorithm.MultiPartitionSuffixTree);
    }

    public TweetIndex(SearchAdaptor.SearchAlgorithm algorithm) {
        this(algorithm, false);
    }

    public TweetIndex(SearchAdaptor.SearchAlgorithm algorithm, boolean useMultiThread) {
        this.useMultiThread = useMultiThread;
        searchAlgorithm = algorithm;
    }

    public void process_tweets(List<Tweet> tweets) {
        this.tweets = Collections.unmodifiableList(tweets);
        //tweetDocuments->copy the tweets content
        List<String> tweetDocuments = new ArrayList<>();
        for (Tweet tweet : this.tweets) {
            String content = tweet.getContent();
            tweetDocuments.add(content);
        }
        //find the search algorithm
        tweetSearch = new SearchAdaptor(tweetDocuments, searchAlgorithm);
        //ranking system->find which tweet should be returned
        recommender = new Recommender(this.tweets);
    }

    public void setRecommendationAlgorithm(Recommender.Algorithm algorithm) {
        recommender.setAlgorithm(algorithm);
    }

    public void setUseMultiThread(boolean useMultiThread) {
        this.useMultiThread = useMultiThread;
        tweetSearch.setUseMultiThread(useMultiThread);
    }

    public List<Tweet> search(String query) {
        Set<Integer> resultIdx = tweetSearch.searchFor(query);
        //if not find the resultIdx set, we need to split the query, and then search again
        if (resultIdx.size() == 0) {
            resultIdx = tweetSearch.searchFor(List.of(query.split(" ")));
        }
        List<Tweet> results = new ArrayList<>();
        for (var i : resultIdx) {
            results.add(tweets.get(i));
        }
        //use the recommendation system to sort
        recommender.sort(results);
        return results;
    }
}
