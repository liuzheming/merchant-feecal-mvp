package com.merchant.base.leetcode;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class Top49Test {

  @Test
  void groupsStandardExample() {
    Top49 solver = new Top49();
    String[] input = {"eat", "tea", "tan", "ate", "nat", "bat"};

    List<List<String>> actual = solver.groupAnagrams(input);
    List<List<String>> expected = Arrays.asList(
        Arrays.asList("bat"),
        Arrays.asList("nat", "tan"),
        Arrays.asList("ate", "eat", "tea")
    );

    assertEquals(normalize(expected), normalize(actual));
  }

  @Test
  void groupsSingleAndEmpty() {
    Top49 solver = new Top49();
    String[] input = {"", "", "a", "b", "ab", "ba"};

    List<List<String>> actual = solver.groupAnagrams(input);
    List<List<String>> expected = Arrays.asList(
        Arrays.asList("", ""),
        Arrays.asList("a"),
        Arrays.asList("b"),
        Arrays.asList("ab", "ba")
    );

    assertEquals(normalize(expected), normalize(actual));
  }

  @Test
  void groupsAllSameAnagrams() {
    Top49 solver = new Top49();
    String[] input = {"abc", "bca", "cab", "cba"};

    List<List<String>> actual = solver.groupAnagrams(input);
    List<List<String>> expected = Arrays.asList(
        Arrays.asList("abc", "bca", "cab", "cba")
    );

    assertEquals(normalize(expected), normalize(actual));
  }

  private List<List<String>> normalize(List<List<String>> groups) {
    if (groups == null) {
      return null;
    }
    List<List<String>> normalized = new ArrayList<>(groups.size());
    for (List<String> group : groups) {
      List<String> sorted = group.stream().sorted().collect(Collectors.toList());
      normalized.add(sorted);
    }
    normalized.sort(Comparator.comparing(list -> String.join("#", list)));
    return normalized;
  }
}
