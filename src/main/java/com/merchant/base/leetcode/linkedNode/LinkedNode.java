package com.merchant.base.leetcode.linkedNode;

/**
 * Description:
 * <p>
 *
 * @author lzm
 * @date 2026/1/15
 */
public class LinkedNode {

  public static void main(String[] args) {
    Node head = new Node(0);
    head.next = new Node(1);
    head.next.next = new Node(2);
    head.next.next.next = new Node(3);
    print(head);
  }

  public static void print(Node node) {
    Node cur = node;
    int count = 0;
    while (cur != null) {
      System.out.println(cur.val);
      count++;
      cur = cur.next;
    }
    System.out.println(count);
  }


  public static class Node {

    public Node(int val) {
      this.val = val;
    }

    public int val;
    public Node next;
  }

}
