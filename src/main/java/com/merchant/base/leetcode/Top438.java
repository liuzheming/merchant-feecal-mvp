package com.merchant.base.leetcode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Description:
 * <p>
 *
 * @author lzm
 * @date 2026/1/15
 */
public class Top438 {

  public static void main(String[] args) {

    findAnagrams("cbaebabacd", "abc");
  }


  public static List<Integer> findAnagrams(String s, String p) {

    int left = 0;
    int right = 0;
    Map<Character, Integer> map = new HashMap<>();
    List<Integer> result = new ArrayList();
    Map<Character, Integer> pMap = new HashMap<>();
    for (int i = 0; i < p.length(); i++) {
      char n = p.charAt(i);
      pMap.put(n, pMap.getOrDefault(n, 0) + 1);
    }

    for (right = 0; right < s.length(); right++) {
      char x = s.charAt(right);
      while (right - left >= p.length()) {
        char leftChar = s.charAt(left);
        map.put(leftChar, map.get(leftChar) - 1);
        if (map.get(leftChar) == 0) {
          map.remove(leftChar);
        }
        left++;
      }
      map.put(x, map.getOrDefault(x, 0) + 1);
      if (map.equals(pMap)) {
        result.add(left);
      }
    }
    return result;
  }
}
