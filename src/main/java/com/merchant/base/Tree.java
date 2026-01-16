package com.merchant.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description:
 * <p>
 *
 * @author lzm
 * @date 2026/1/14
 */
public class Tree {

  public static class Node {

    int val;

    Node left;
    Node right;

    public Node(int val) {
      this.val = val;
    }
  }


  public List<Integer> beforeOrder() {
    List<Integer> result = new ArrayList<>();
    Node root = new Node(1);
    doBeforeOrder(root, result);
    return result;
  }

  private void doBeforeOrder(Node node, List<Integer> result) {
    if (node == null) {
      return;
    }
    doBeforeOrder(node.left, result);
    result.add(node.val);
    doBeforeOrder(node.right, result);
  }


  public static void main(String[] args) {
    int[] nums = new int[]{3,2,4};
    int target = 6;
    twoSum(nums, target);
  }

  public static int[] twoSum(int[] nums, int target) {
    int[] result = new int[2];
    if (nums == null || nums.length == 0) {
      return result;
    }

    Map<Integer, Integer> map = new HashMap();
    for (int i = 0; i < nums.length; i++) {
      map.put(nums[i], i);
    }

    for (int i=0; i< nums.length; i++) {
      int x = nums[i];
      int y = target - x;
      if (map.containsKey(y) && map.get(y) != i) {
        result[0] = i;
        result[1] = map.get(y);
        return result;
      }
    }
    return result;
  }
}
