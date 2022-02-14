package com.company;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SuffixTreeTest {
    public static String getSaltString(int length) {
        // From: https://stackoverflow.com/questions/20536566/creating-a-random-string-with-a-z-and-0-9-in-java
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < length) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        return salt.toString();
    }

    public static Set<Integer> naiveFind(List<String> strings, List<Integer> values, String toFind) {
        Set<Integer> ret = new HashSet<>();
        for (int i = 0; i < strings.size(); i++) {
            if (strings.get(i).contains(toFind)) {
                ret.add(values.get(i));
            }
        }
        return Collections.unmodifiableSet(ret);
    }

    @Test
    void simpleTestMatch() {
        List<String> key = new ArrayList<>();
        List<Integer> value = new ArrayList<>();
        key.add("Aha a");
        value.add(1);
        key.add("Ahb a");
        value.add(2);
        key.add("Ahc a");
        value.add(3);
        key.add("Ahaa");
        value.add(4);
        key.add("Aha b");
        value.add(5);
        key.add("Aha c");
        value.add(6);
        key.add(" Aha a");
        value.add(7);
        key.add("Aha aha");
        value.add(8);
        key.add("Asha a");
        value.add(9);
        key.add("Aha aAha");
        value.add(10);
        key.add("");
        value.add(11);
        SimpleSuffixTree<Integer> tree = new SimpleSuffixTree<>(key, value);
        List<String> testInput = new ArrayList<>();
        List<Set<Integer>> expectedOutput = new ArrayList<>();
        testInput.add("aha");
        expectedOutput.add(Set.of(8));
        testInput.add("Aha");
        expectedOutput.add(Set.of(1, 4, 5, 6, 7, 8, 10));
        testInput.add("Aha a");
        expectedOutput.add(Set.of(1, 7, 8, 10));
        testInput.add("Ahc");
        expectedOutput.add(Set.of(3));
        testInput.add("c");
        expectedOutput.add(Set.of(3, 6));
        testInput.add("a a");
        expectedOutput.add(Set.of(1, 7, 8, 9, 10));
        testInput.add("aaaaa");
        expectedOutput.add(Set.of());
        testInput.add("");
        expectedOutput.add(Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11));
        for (int i = 0; i < testInput.size(); i++) {
            assertEquals(expectedOutput.get(i), tree.searchFor(testInput.get(i)));
        }
    }

    @Test
    void randomTestMatch() {
        int NUM_STRINGS = 200000;
        int STRING_LENGTH = 80;
        int NUM_WORDS = 100000;
        int WORD_LENGTH = 10;
        List<String> strings = new ArrayList<>();
        for (int i = 0; i < NUM_STRINGS; i++) {
            strings.add(getSaltString(STRING_LENGTH));
        }
        List<String> words = new ArrayList<>();
        for (int i = 0; i < NUM_WORDS; i++) {
            words.add(getSaltString(WORD_LENGTH));
        }
        List<Integer> values = IntStream.rangeClosed(0, NUM_STRINGS - 1).boxed().collect(Collectors.toList());

        List<Set<Integer>> ground_truth = new ArrayList<>();
        List<Set<Integer>> actual = new ArrayList<>();
        var start = System.currentTimeMillis();
        SuffixTree<Integer> stree = new MultiPartitionSuffixTree<>(strings, values);
        var buildEnd = System.currentTimeMillis();
        for (var word : words) {
            ground_truth.add(naiveFind(strings, values, word));
        }
        var naiveFindEnd = System.currentTimeMillis();
        for (var word : words) {
            actual.add(stree.searchFor(word));
        }
        var end = System.currentTimeMillis();
        System.out.println("Time to build: " + (buildEnd - start) + " ms.");
        System.out.println("Time to match: Naive = " + (naiveFindEnd - buildEnd) + " ms, Suffix tree = " + (end - naiveFindEnd) + " ms.");
        assertEquals(ground_truth, actual);

    }
}
