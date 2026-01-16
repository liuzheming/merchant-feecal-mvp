package com.merchant.base;

import com.google.common.collect.Lists;

import java.util.List;
import java.lang.String;

/**
 * Description:
 * <p>
 *
 * @author lzm
 * @date 2025/12/2
 */
public class TwoSum {

	public static void main(String[] args) {
		int[] arr = {1, 2, 3, 4, 5, 6};
		int target = 6;
		System.out.println(twoSum(arr, target));

		String s = new String();
	}

	public static List<Integer> twoSum(int[] arr, int target) {
		for (int i = 0; i <= arr.length - 2; i++) {
			int one = arr[i];
			for (int j = i + 1; j <= arr.length - 1; j++) {
				int two = arr[j];
				if (target == one + two) {
					return Lists.newArrayList(i, j);
				}
			}
		}
		return null;
	}

}
