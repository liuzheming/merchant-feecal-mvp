package com.merchant.base.leetcode.linkedNode;

/**
 * Description:
 * <p>
 *
 * @author lzm
 * @date 2026/1/15
 */
public class LinkedNodeV2 {

  public static void main(String[] args) {
    Node dummy = new Node(-1);
    Node head = new Node(0);
    dummy.next = head;
    head.next = new Node(1);
    head.next.next = new Node(2);
    head.next.next.next = new Node(3);
    print(dummy);
  }

  public static void print(Node node) {
    Node cur = node.next;
    int count = 0;
    while (cur != null) {
      System.out.println(cur.val);
      count++;
      cur = cur.next;
    }
    System.out.println(count);
  }


  public static class Node {

    public Node() {

    }

    public Node(int val) {
      this.val = val;
    }

    public int val;
    public Node next;
  }

}
