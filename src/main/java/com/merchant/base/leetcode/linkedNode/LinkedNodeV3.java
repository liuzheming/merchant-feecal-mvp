package com.merchant.base.leetcode.linkedNode;

import static com.merchant.base.leetcode.linkedNode.LinkedNodeV2.print;

import com.merchant.base.leetcode.linkedNode.LinkedNodeV2.Node;

/**
 * Description:
 * <p>
 *
 * @author lzm
 * @date 2026/1/15
 */
public class LinkedNodeV3 {

  public static void main(String[] args) {
    Node dummy = new Node(-1);
    Node head = new Node(0);
    dummy.next = head;
    head.next = new Node(1);
    head.next.next = new Node(2);
    head.next.next.next = new Node(3);
    head.next.next.next.next = new Node(4);
    head.next.next.next.next.next = new Node(5);
    delete(dummy, 2);
    print(dummy);
  }

  public static void delete(Node node, int target) {
    Node cur = node;
    while (cur.next != null) {
      if (cur.next.val == target) {
        cur.next = cur.next.next;
        break;
      }
      cur = cur.next;
    }
  }

}
