package com.merchant.base.leetcode;

import com.google.common.primitives.Ints;
import java.util.Arrays;

/**
 * Description:
 * <p>
 *
 * @author lzm
 * @date 2026/1/15
 */
public class Top283 {

  public static void main(String[] args) {
    int[] ints = {9, 0, 9, 8, 1};
    moveZeroes(ints);
    System.out.print(Ints.asList(ints));
  }

  public static void moveZeroes(int[] nums) {
    for (int i = 0; i < nums.length; i++) {
      if (nums[i] == 0) {
        for (int j = i + 1; j < nums.length && nums[j] != 0; j++) {
          int tmp = nums[j];
          nums[j] = 0;
          nums[i] = tmp;
        }
      }
    }
  }

}
