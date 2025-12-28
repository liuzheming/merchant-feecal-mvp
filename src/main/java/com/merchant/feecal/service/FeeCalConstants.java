package com.merchant.feecal.service;

/**
 * FeeCal 领域常量。
 * <p>显式区分状态枚举与业务配置常量，便于复用与调用方理解。</p>
 */
public final class FeeCalConstants {

	private FeeCalConstants() {
	}

	public static final class Status {
		private Status() {
		}

		public static final class Batch {
			public static final String PENDING = "PENDING";
			public static final String EDITING = "EDITING";
			public static final String DONE = "DONE";

			private Batch() {
			}
		}

		public static final class BillingData {
			public static final String PENDING = "PENDING";
			public static final String LOADING = "LOADING";
			public static final String READY = "READY";
			public static final String FAILED = "FAILED";

			private BillingData() {
			}
		}

		public static final class Term {
			public static final String PENDING = "PENDING";
			public static final String DRAFT = "DRAFT";
			public static final String DONE = "DONE";

			private Term() {
			}
		}

		public static final class FundInstruction {
			public static final String PENDING = "PENDING";
			public static final String EXECUTING = "EXECUTING";
			public static final String SUCCESS = "SUCCESS";
			public static final String FAIL = "FAIL";

			private FundInstruction() {
			}
		}

		public static final class FundCallback {
			public static final String NOT_STARTED = "NOT_STARTED";
			public static final String DOING = "DOING";
			public static final String SUCCESS = "SUCCESS";
			public static final String FAIL = "FAIL";

			private FundCallback() {
			}
		}
	}

	public static final class TermCode {
		public static final String ELECTRONIC_SEAL = "TERM_ELECTRONIC_SEAL";
		public static final String MINIMUM_GUARANTEE = "TERM_MINIMUM_GUARANTEE";
		public static final String CALL_400 = "TERM_400_CALL";

		private TermCode() {
		}
	}

	public static final class Fund {
		private Fund() {
		}

		public static final class Direction {
			public static final String DEBIT = "DEBIT";
			public static final String CREDIT = "CREDIT";

			private Direction() {
			}
		}

		public static final class BizType {
			public static final String DEPOSIT_DEDUCT = "DEPOSIT_DEDUCT";
			public static final String DEPOSIT_REFUND = "DEPOSIT_REFUND";

			private BizType() {
			}
		}

		public static final class Account {
			public static final String DEPOSIT = "DEPOSIT";

			private Account() {
			}
		}

		public static final class PartyType {
			public static final String MERCHANT = "MERCHANT";
			public static final String PLATFORM = "PLATFORM";

			private PartyType() {
			}
		}
	}
}
