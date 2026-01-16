package com.merchant.base.leetcode;

import java.util.HashSet;
import java.util.Set;

/**
 * Description:
 * <p>
 *
 * @author lzm
 * @date 2026/1/14
 */
public class Top3 {


  public static void main(String[] args) {
    lengthOfLongestSubstring("abcabcbb");
  }


  public static int lengthOfLongestSubstring(String s) {
    int left = 0;
    int right = 0;
    Set<Character> set = new HashSet<>();
    int length = 0;
    for (right = 0; right < s.length(); right++) {
      // 扩张
      char x = s.charAt(right);
      // 判断是否合法
      boolean invalid = set.contains(x);
      // 如果合法，纳入；如果非法，收缩
      if (!invalid) {
        set.add(x);
      } else {
        while (set.contains(x)) {
          set.remove(x);
          left++;
        }
      }
      length = right - left + 1;
    }
    return length;
  }
}
