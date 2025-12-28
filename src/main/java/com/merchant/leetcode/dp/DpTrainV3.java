package com.merchant.leetcode.dp;

import org.apache.commons.lang3.math.NumberUtils;

/**
 * Description:
 * <p>
 *
 * @author lzm
 * @date 2025/12/6
 */
public class DpTrainV3 {

	public static void main(String[] args) {
		int[] days = {1, 2, 3, 10, 11, 12, 40};
		int[] costs = {5, 18, 50};   // 1日券=5, 5日券=18, 15日券=50

		System.out.println("===" + minParkCost(days, costs));
	}

	public static int minParkCost(int[] days, int[] costs) {

		ThreadLocal<String> tl = new ThreadLocal<>();
		tl.set("sss");
		tl.remove();
		// 目标日
		int maxDay = days[days.length - 1];

		// dp 数组
		int[] dp = new int[maxDay + 1];

		// base case
		dp[0] = 0;

		// 跳过无票日
		boolean[] flags = new boolean[maxDay + 1];
		for (int day : days) {
			flags[day] = true;
		}

		for (int i = 1; i <= maxDay; i++) {
			if (flags[i]) {

				// 枚举票
				int case1 = dp[Math.max(0, i - 1)] + costs[0];
				int case2 = dp[Math.max(0, i - 5)] + costs[1];
				int case3 = dp[Math.max(0, i - 15)] + costs[2];

				dp[i] = NumberUtils.min(case1, case2, case3);
			} else {
				dp[i] = dp[i - 1];
			}
		}

		return dp[maxDay];
	}


}
