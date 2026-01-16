package com.merchant.base;

/**
 * Description:
 * <p>
 *
 * @author lzm
 * @date 2025/12/2
 */
public class FindMax {

	public static void main(String[] args) {

		FindMax findMax = new FindMax();
		int[] arr = {1, 3, 5, 7, 9, 2, 4, 6, 8, 0};
		int max = findMax.findMax(arr);
		System.out.println("Max value: " + max);

	}

	public int findMax(int[] arr) {
		Integer max = null;
		for (int j : arr) {
			if (max == null) {
				max = j;
			} else {
				max = max > j ? max : j;
			}
		}
		return max;
	}

}
