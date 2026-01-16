//package com.merchant.leetcode.dp;
//
//import lombok.extern.slf4j.Slf4j;
////import org.apache.commons.lang3.math.NumberUtils;
//
///**
// * Description:
// * <p>
// *
// * @author lzm
// * @date 2025/12/6
// */
//@Slf4j
//public class DpTrainV1 {
//
//
//	public static void main(String[] args) {
//		int[] days = new int[]{1, 4, 6, 7, 28, 29};
//		int[] costs = new int[]{4, 10, 25};
//		System.out.println("====" + costV4(days, costs));
//	}
//
//	public static int costV4(int[] days, int[] costs) {
//		// dp(n) = min(dp(n-4) + cost[1], dp(n-10) + cost2, dp(n-25) + cost3)
//
//		int maxDay = days[days.length - 1];
//		// arr[i] 代办截止 i 天的最小花费
//		int[] arr = new int[maxDay + 1];
//
//		// base case
//		arr[0] = 0;
//
//		// 有效日期标志
//		boolean[] effectFlag = new boolean[maxDay + 1];
//		for (int day : days) {
//			effectFlag[day] = true;
//		}
//		// 递推
//		for (int i = 1; i < arr.length; i++) {
//			if (effectFlag[i]) {
//				int case1 = arr[Math.max(0, i - 1)] + costs[0];
//				int case2 = arr[Math.max(0, i - 7)] + costs[1];
//				int case3 = arr[Math.max(0, i - 30)] + costs[2];
////				arr[i] = arr[i - 1] + NumberUtils.min(case1, case2, case3);
//
//				arr[i] = Math.min(case1, case2, case3);
//				arr[i] = Math.min(case1, case2, case3);
//			} else {
//				arr[i] = arr[i - 1];
//			}
//		}
//		return arr[maxDay];
//	}
//
//}
