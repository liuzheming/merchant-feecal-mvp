package com.merchant.base.leetcode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description:
 * <p>
 *
 * @author lzm
 * @date 2026/1/15
 */
public class Top49 {

  /**
   * 题目：字母异位词分组
   * <p>
   * 给你一个字符串数组，请将字母异位词组合在一起。
   * 可以按任意顺序返回结果。
   * <p>
   * 示例：
   * 输入：["eat","tea","tan","ate","nat","bat"]
   * 输出：[["bat"],["nat","tan"],["ate","eat","tea"]]
   * <p>
   * 约束：
   * 1 <= strs.length <= 10^4
   * 0 <= strs[i].length <= 100
   * strs[i] 仅包含小写英文字母。
   */
  public List<List<String>> groupAnagrams(String[] strs) {
    Map<Map<Character, Integer>, List<String>> map = new HashMap<>();
    for (String str : strs) {
      Map<Character, Integer> lateMap = new HashMap<>();
      for (int i = 0; i < str.length(); i++) {
        lateMap.put(str.charAt(i), lateMap.getOrDefault(str.charAt(i), 0));
      }

      if (map.containsKey(lateMap)) {
        map.get(lateMap).add(str);
      } else {
        ArrayList<String> strList = new ArrayList<>();
        strList.add(str);
        map.put(lateMap, strList);
      }
    }
    List<List<String>> collect = new ArrayList<>(map.values());
    return collect;
  }
}
