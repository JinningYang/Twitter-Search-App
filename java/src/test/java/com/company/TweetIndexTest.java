package com.company;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.*;

public class TweetIndexTest {
    Set<String> testTokens;

    TweetIndex getTweetIndex(String tweetCsv, SearchAdaptor.SearchAlgorithm algorithm) {
        return getTweetIndex(tweetCsv, algorithm, false);
    }

    TweetIndex getTweetIndex(String tweetCsv, SearchAdaptor.SearchAlgorithm algorithm, boolean useMultiThreadFindAlgo) {
        testTokens = new HashSet<>();
        TweetIndex ret = new TweetIndex(algorithm, useMultiThreadFindAlgo);
        String line;
        ArrayList<Tweet> tweets = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(tweetCsv))) {
            br.readLine();
            while ((line = br.readLine()) != null) {
                tweets.add(new Tweet(line));
                testTokens.addAll(List.of(line.split(",")[1].split(" ")));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        ret.process_tweets(tweets);
        return ret;
    }

    @Test
    public void simpleTest() {
        String DATA_PATH = "../data";
        String DATA_SET = "small.csv";
        var TEST_K1 = List.of(
                "hello",
                "hello me",
                "hello bye",
                "hello this bob",
                "notinanytweets",
                "neeva"
        );
        for (var searchAlgorithm : SearchAdaptor.SearchAlgorithm.values()) {
            var index = getTweetIndex(DATA_PATH + '/' + DATA_SET, searchAlgorithm);
            for (var sortAlgorithm : Recommender.Algorithm.values()) {
                index.setRecommendationAlgorithm(sortAlgorithm);
                System.out.println("==================================");
                System.out.println("| Current Search Algorithm: " + searchAlgorithm.toString());
                System.out.println("| Current Sorting Algorithm: " + sortAlgorithm.toString());
                System.out.println("| Current Dataset: " + DATA_SET);
                System.out.println("==================================");
                for (var tk : TEST_K1) {
                    System.out.println("Search for " + tk);
                    index.search(tk).stream().limit(10).forEach(re -> System.out.println("\t" + re.getTimestamp() + " " + re.getContent()));
                }
            }
        }
    }

    @Test
    public void performanceTest() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("performance.csv", false));
            writer.write("Tokens,Algorithm,Use Multi Thread,Fastest,Median,90% tile, 99% tile\n");
            for (int i = 1; i < 11; i++) {
                System.out.println("====================");
                System.out.println("Running performance test with " + i + " token(s):");
                singlePerformanceTest(i, writer);
                System.out.println("====================");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void singlePerformanceTest(int nTokens, BufferedWriter log) throws IOException {
        String DATA_PATH = "../data";
        String DATA_SET = "tweets.csv";
        for (var algo : SearchAdaptor.SearchAlgorithm.values()) {
            var index = getTweetIndex(DATA_PATH + '/' + DATA_SET, algo);
            for (boolean multiThread : List.of(false, true)) {
                index.setUseMultiThread(multiThread);
                ArrayList<Long> times = new ArrayList<>();
                List<String> testQueries = new ArrayList<>();
                List<String> currentTestQuery = new LinkedList<>();
                for (var testToken : testTokens) {
                    currentTestQuery.add(testToken);
                    if (currentTestQuery.size() > nTokens) {
                        currentTestQuery.remove(0);
                    }
                    if (currentTestQuery.size() == nTokens) {
                        StringBuilder tmp = new StringBuilder();
                        for (var query : currentTestQuery) {
                            if (tmp.length() > 0) {
                                tmp.append(" ");
                            }
                            tmp.append(query);
                        }
                        testQueries.add(tmp.toString());
                    }
                }
                for (int i = 0; i < 1000; i++) {
                    var start = System.nanoTime();
                    for (var testQuery : testQueries) {
                        index.search(testQuery);
                    }
                    var end = System.nanoTime();
                    times.add(end - start);
                }
                Collections.sort(times);
                var fastest = 1000000000 / times.get(0);
                var median = 1000000000 / times.get(times.size() / 2);
                var _90tile = 1000000000 / times.get(times.size() * 9 / 10);
                var _99tile = 1000000000 / times.get(times.size() * 99 / 100);

                System.out.println("Performance using " + algo + " algorithm, using Multi Thread: " + multiThread);
                System.out.println("\tFastest: " + fastest + " Searches/Sec.");
                System.out.println("\tMedian: " + median + " Searches/Sec.");
                System.out.println("\t90 %ile: " + _90tile + " Searches/Sec.");
                System.out.println("\t99 %ile: " + _99tile + " Searches/Sec.");
                if (log != null) {
                    log.write(nTokens + "," + algo + "," + multiThread + "," + fastest + "," + median + "," + _90tile + "," + _99tile + "\n");
                }
            }
        }
    }
}
