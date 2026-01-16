//package com.merchant.leetcode.dp;
//
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.math.NumberUtils;
//
///**
// * Description:
// * <p>
// *
// * @author lzm
// * @date 2025/12/6
// */
//@Slf4j
//public class DpTrainV2 {
//
//	public static void main(String[] args) {
//		int[] days = new int[]{1, 2, 3, 4, 10, 20, 21};
//		int[] costs = new int[]{3, 7, 20};   // 1日包=3, 3日包=7, 10日包=20
//
//		LOGGER.info("result:{}", minFlowCost(days, costs));
//	}
//
//	public static int minFlowCost(int[] days, int[] costs) {
//		// 目标日
//		int maxDay = days[days.length - 1];
//		// dp数组
//		int[] dp = new int[maxDay + 1];
//		dp[0] = 0;
//
//		// 跳过无票日
//		boolean[] flags = new boolean[maxDay + 1];
//		for (int day : days) {
//			flags[day] = true;
//		}
//		for (int i = 1; i <= maxDay; i++) {
//			// 枚举票
//			int case1 = dp[Math.max(0, i - 1)] + costs[0];
//			int case2 = dp[Math.max(0, i - 3)] + costs[1];
//			int case3 = dp[Math.max(0, i - 10)] + costs[2];
//			if (flags[i]) {
//				// 最优解
//				dp[i] = NumberUtils.min(case1, case2, case3);
//			} else {
//				dp[i] = dp[i - 1];
//			}
//		}
//		return dp[maxDay];
//	}
//
//
//}
