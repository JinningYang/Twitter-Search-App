package com.company;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Recommender {
    private final Map<Algorithm, AlgorithmHandler> algorithmRegistry;
    private Algorithm algorithm;

    public Recommender(List<Tweet> tweets) {
        this(tweets, Algorithm.Latest);
    }

    public Recommender(List<Tweet> tweets, Algorithm algorithm) {
        this.algorithm = algorithm;

        algorithmRegistry = new HashMap<>();
        algorithmRegistry.put(Algorithm.Latest, new AlgorithmHandler() {
            @Override
            public void init(List<Tweet> tweets) {
            }

            @Override
            public void sortTweets(List<Tweet> tweets) {
                tweets.sort((t1, t2) -> t2.getTimestamp() - t1.getTimestamp());
            }
        });
        algorithmRegistry.put(Algorithm.HighestViews, new AlgorithmHandler() {
            @Override
            public void init(List<Tweet> tweets) {
            }

            @Override
            public void sortTweets(List<Tweet> tweets) {
                tweets.sort((t1, t2) -> t2.getViews() - t1.getViews());
            }
        });
        algorithmRegistry.put(Algorithm.HighestLikes, new AlgorithmHandler() {
            @Override
            public void init(List<Tweet> tweets) {
            }

            @Override
            public void sortTweets(List<Tweet> tweets) {
                tweets.sort((t1, t2) -> t2.getLikes() - t1.getLikes());
            }
        });
        algorithmRegistry.put(Algorithm.HighestRetweets, new AlgorithmHandler() {
            @Override
            public void init(List<Tweet> tweets) {
            }

            @Override
            public void sortTweets(List<Tweet> tweets) {
                tweets.sort((t1, t2) -> t2.getRetweets() - t1.getRetweets());
            }
        });
        algorithmRegistry.put(Algorithm.MyAwesomeModel, new AlgorithmHandler() {
            private int maxTime;

            //find the max timestamp -> the recent timestamp
            @Override
            public void init(List<Tweet> tweets) {
                assert (tweets != null);
                maxTime = -1;
                for (var t : tweets) {
                    maxTime = Math.max(maxTime, t.getTimestamp());
                }
            }

            private Double getIndex(Tweet t) {
                return (t.getViews() + t.getLikes() * 50 + t.getRetweets() * 100) *
                        (maxTime + 1.0) / (maxTime + 1.0 - t.getTimestamp()); // Prefer the latest tweets...
            }

            @Override
            public void sortTweets(List<Tweet> tweets) {
                tweets.sort((t1, t2) -> getIndex(t2).compareTo(getIndex(t1)));
            }
        });
        assert (algorithmRegistry.containsKey(algorithm));
        for (var algo : algorithmRegistry.values()) {
            algo.init(tweets); // Init all algos so that we can hot swap!
        }
    }

    public void setAlgorithm(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    public void sort(List<Tweet> results) {
        assert (results != null);
        algorithmRegistry.get(algorithm).sortTweets(results);
    }

    public enum Algorithm {
        Latest,
        HighestViews,
        HighestLikes,
        HighestRetweets,
        MyAwesomeModel
    }

    private interface AlgorithmHandler {
        void init(List<Tweet> tweets);

        void sortTweets(List<Tweet> tweets);
    }
}
