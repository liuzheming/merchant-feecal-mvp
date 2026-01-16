package com.merchant.base;

/**
 * Description:
 * <p>
 *
 * @author lzm
 * @date 2025/12/2
 */
public class RevertArr {

	public static void main(String[] args) {
		int[] arr = revertArr2(new int[]{1, 2, 3, 4, 5, 6});
		for (int j : arr) {
			System.out.print(j + " ");
		}
	}

	public static int[] revertArr(int[] arr) {
		int[] arr2 = new int[arr.length];
		int i;
		int j;
		for (i = 0; i < arr.length; i++) {
			j = arr.length - i - 1;
			arr2[j] = arr[i];
		}
		return arr2;
	}

	public static int[] revertArr2(int[] arr) {
		for (int i = 0; i < arr.length; i++) {
			int j = arr.length - i - 1;
			if (i >= j) {
				break;
			}
			int x = arr[i];
			int y = arr[j];
			arr[i] = y;
			arr[j] = x;
		}
		return arr;
	}

}
