package com.merchant.base;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Description:
 * <p>
 *
 * @author lzm
 * @date 2025/12/2
 */
@Slf4j
public class Solution {


	public static void main(String[] args) {
		Solution solution = new Solution();
		ListNode head = new ListNode(1);
		head.next = new ListNode(2);
		head.next.next = new ListNode(3);
		head.next.next.next = new ListNode(4);
		head.next.next.next.next = new ListNode(5);

		solution.reverseList(head);
	}

	public ListNode reverseList(ListNode head) {

		ListNode pre = null;
		ListNode current = head;
		ListNode next = null;

		while (current != null) {
			next = current.next;
			current.next = pre;
			pre = current;
			current = next;
		}
		// 打印链表
		print(pre);
		return current;
	}

	public ListNode reverseList2(ListNode head) {

		// 反转
		if (head == null || head.next == null) {
			return head;
		} else {
			ListNode newHead = reverseList2(head.next);
			head.next.next = head;
			head.next = null;
			// 打印链表
			print(newHead);
			return newHead;
		}

	}


	private static void print(ListNode pre) {
		ListNode temp = pre;
		List<Integer> vals = new java.util.ArrayList<>();
		while (temp != null) {
			vals.add(temp.val);
			temp = temp.next;
		}
		LOGGER.info(JsonUtils.obj2Str(vals));
	}

	public static class ListNode {
		int val;
		ListNode next;

		ListNode() {
		}

		ListNode(int val) {
			this.val = val;
		}

		ListNode(int val, ListNode next) {
			this.val = val;
			this.next = next;
		}
	}

}
