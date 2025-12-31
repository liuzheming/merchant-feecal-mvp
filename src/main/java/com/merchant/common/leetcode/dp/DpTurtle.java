package com.merchant.common.leetcode.dp;


/**
 * Description:
 * <p>
 *
 * @author lzm
 * @date 2025/12/5
 */
public class DpTurtle {


	public static void main(String[] args) {

		System.out.print(countTurtlePath(100));
	}

	public static long countTurtlePath(int n) {

		int step1 = 1;
		int step2 = 2;

		long[] arr = new long[n + 1];
		arr[1] = step1;
		arr[2] = step2;

		for (int i = 3; i <= n; i++) {
			arr[i] = arr[i - 1] + arr[i - 2];
		}

		return arr[n];
	}


}
