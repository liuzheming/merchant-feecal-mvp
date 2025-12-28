-- Phase3 MVP: create fund instruction table

CREATE TABLE IF NOT EXISTS `fee_cal_fund_instruction` (
    `id`                BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '指令主键ID',
    `batch_no`          VARCHAR(64) NOT NULL COMMENT '清算批次编号',
    `term_inst_id`      BIGINT      DEFAULT NULL COMMENT '来源费用项ID；整体退款可为空',

    `settle_subject_type` VARCHAR(32) NOT NULL COMMENT '清算主体类型',
    `settle_subject_no`   VARCHAR(64) NOT NULL COMMENT '清算主体编码',

    `payer_type` VARCHAR(32) NOT NULL COMMENT '付款方类型',
    `payer_no`   VARCHAR(64) NOT NULL COMMENT '付款方编码',
    `payee_type` VARCHAR(32) NOT NULL COMMENT '收款方类型',
    `payee_no`   VARCHAR(64) NOT NULL COMMENT '收款方编码',

    `fund_direction` VARCHAR(16) NOT NULL COMMENT 'DEBIT/CREDIT/COLLECT',
    `fund_biz_type`  VARCHAR(32) NOT NULL COMMENT 'DEPOSIT_DEDUCT/DEPOSIT_REFUND',
    `account_type`   VARCHAR(16) NOT NULL DEFAULT 'DEPOSIT' COMMENT '账户类型',

    `should_amount` DECIMAL(18,2) NOT NULL COMMENT '应执行金额',
    `actual_amount` DECIMAL(18,2) DEFAULT NULL COMMENT '实际执行金额',

    `fund_status`     VARCHAR(16) NOT NULL COMMENT 'PENDING/EXECUTING/SUCCESS/FAIL',
    `callback_status` VARCHAR(16) NOT NULL COMMENT 'NOT_STARTED/DOING/SUCCESS/FAIL',

    `fund_order_id`   VARCHAR(64)  DEFAULT NULL COMMENT '资金系统单号',
    `fund_channel`    VARCHAR(32)  DEFAULT NULL COMMENT '资金渠道',
    `fund_order_info` JSON         DEFAULT NULL COMMENT '资金单详情JSON',
    `attachment_ids`  VARCHAR(512) DEFAULT NULL COMMENT '附件ID集合',

    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    KEY `idx_batch_no` (`batch_no`),
    KEY `idx_term_inst` (`term_inst_id`),
    KEY `idx_fund_status` (`fund_status`),
    UNIQUE KEY `uk_batch_term_biz` (`batch_no`, `term_inst_id`, `fund_biz_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资金指令表';
