package com.merchant.common.leetcode;

import java.util.Arrays;

/**
 * Description:
 * <p>
 *
 * @author lzm
 * @date 2025/12/2
 */
public class MergeArr2 {

	public static void main(String[] args) {
		int[] arr1 = new int[]{1, 5, 7, 9, 0, 0, 0};
		int m = 4;
		int[] arr2 = new int[]{2, 3, 8};
		int n = 3;
		int[] result = merge(arr1, m, arr2, n);
		System.out.println(Arrays.toString(result));
	}

	public static int[] merge(int[] arr1, int m, int[] arr2, int n) {
		int i = m - 1;
		int j = n - 1;

		for (int k = arr1.length - 1; k >= 0; k--) {
			if (i < 0) {
				arr1[k] = arr2[j];
				j--;
				continue;
			}
			if (j < 0) {
				arr1[k] = arr1[i];
				i--;
				continue;
			}
			int x = arr1[i];
			int y = arr2[j];
			if (x > y) {
				arr1[k] = x;
				i--;
			} else {
				arr1[k] = y;
				j--;
			}
		}
		return arr1;
	}


}
