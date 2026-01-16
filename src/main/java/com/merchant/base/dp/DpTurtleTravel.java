//package com.merchant.leetcode.dp;
//
//import lombok.extern.slf4j.Slf4j;
//
//import static java.lang.Math.max;
//import static org.apache.commons.lang3.math.NumberUtils.min;
//
///**
// * Description:
// * <p>
// *
// * @author lzm
// * @date 2025/12/5
// */
//@Slf4j
//public class DpTurtleTravel {
//
//
//	public static void main(String[] args) {
//
//		LOGGER.info("cost:{}", cost(3));
//
//	}
//
//	public static long cost(int days) {
//		// dp(days) = min(dp(days - 1) + cost1, dp(days - 2) + cost2)
//
//		// 终止条件
//		if (days <= 0) {
//			return 0;
//		}
//
//		int cost1 = 10;
//		int cost2 = 20;
//
//		// 子问题拆分
//		long case1 = cost(days - 1) + cost1;
//		long case2 = cost(days - 2) + cost2;
//
//		// 合并子问题结果
//		return Math.min(case1, case2);
//	}
//
//	public static long costV2(int days) {
//		// dp(days) = min(dp(days - 1) + cost1, dp(days - 2) + cost2)
//
//		// 状态定义 costArr[i] 代表每天的最小花费
//		long[] costArr = new long[days + 1];
//		int cost1 = 10;
//		int cost2 = 20;
//
//		// base case
//		costArr[0] = 0;
//
//		// 递推
//		for (int i = 1; i < days + 1; i++) {
//			long case1 = costArr[max(0, i - 1)] + cost1;
//			long case2 = costArr[max(0, i - 2)] + cost2;
//			costArr[i] = Math.min(case1, case2);
//		}
//
//		// 返回值
//		// 合并子问题结果
//		return costArr[days];
//	}
//
//
//	public static long costV3(int[] days) {
//		// dp(days) = min(dp(days - 1) + cost1, dp(days - 2) + cost2)
//
//		// costArr[i] 代表每天的最小花费
//		int maxDay = days[days.length - 1];
//		// 状态定义
//		long[] costArr = new long[maxDay + 1];
//		int cost1 = 10;
//		int cost3 = 20;
//		int cost7 = 30;
//
//		// base case
//		costArr[0] = 0;
//
//		// 标记出行日
//		boolean[] travel = new boolean[maxDay + 1];
//		for (int d : days) {
//			travel[d] = true;
//		}
//		// 递推
//		for (int i = 1; i < maxDay + 1; i++) {
//			if (travel[i]) {
//				long case1 = costArr[max(0, i - 1)] + cost1;
//				long case2 = costArr[max(0, i - 3)] + cost3;
//				long case3 = costArr[max(0, i - 7)] + cost7;
//				costArr[i] = min(case1, case2, case3);
//			} else {
//				costArr[i] = costArr[i - 1];
//			}
//		}
//
//		// 返回值
//		return costArr[maxDay];
//	}
//
//
//}
