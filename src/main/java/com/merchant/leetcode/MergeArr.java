package com.merchant.leetcode;

import java.util.Arrays;

/**
 * Description:
 * <p>
 *
 * @author lzm
 * @date 2025/12/2
 */
public class MergeArr {

	public static void main(String[] args) {
//		int[] arr1 = new int[]{1, 5, 7, 0, 0, 0};
//		int[] arr2 = new int[]{2, 3, 8};
		int[] arr1 = new int[]{1, 4, 7};
		int[] arr2 = new int[]{2, 5, 6};
		int[] result = merge(arr1, arr2);
		System.out.println(Arrays.toString(result));
	}

	public static int[] merge(int[] arr1, int[] arr2) {
		int[] newArr = new int[arr1.length + arr2.length];
		int i = arr1.length - 1;
		int j = arr2.length - 1;

		for (int n = newArr.length - 1; n >= 0; n--) {
			if (i < 0) {
				newArr[n] = arr2[j];
				j--;
				continue;
			}
			if (j < 0) {
				newArr[n] = arr1[i];
				i--;
				continue;
			}
			int x = arr1[i];
			int y = arr2[j];
			if (x > y) {
				newArr[n] = x;
				i--;
			} else {
				newArr[n] = y;
				j--;
			}
		}
		return newArr;
	}


}
