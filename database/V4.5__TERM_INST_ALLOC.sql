-- Phase3: bill-level allocation table

ALTER TABLE `fee_cal_term_def`
    ADD COLUMN `bill_alloc_enabled` TINYINT NOT NULL DEFAULT 0 COMMENT '是否开启账单分配' AFTER `auto_load`;

ALTER TABLE `fee_cal_term_inst`
    ADD COLUMN `billing_alloc_enabled` TINYINT NOT NULL DEFAULT 0 COMMENT '是否开启账单分配' AFTER `auto_load`;

CREATE TABLE IF NOT EXISTS `fee_cal_term_inst_alloc` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `batch_no` VARCHAR(64) NOT NULL COMMENT '批次号',
    `term_inst_id` BIGINT NOT NULL COMMENT '费用项实例ID',
    `term_code` VARCHAR(64) NOT NULL COMMENT '费用项编码',
    `billing_snapshot_id` BIGINT NOT NULL COMMENT '快照ID',
    `unpaid_amount` DECIMAL(18,2) NOT NULL COMMENT '账单未付金额',
    `alloc_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否抵扣',
    `alloc_amount` DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '抵扣金额',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_term_snapshot` (`term_inst_id`, `billing_snapshot_id`),
    KEY `idx_batch_term` (`batch_no`, `term_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='费用项账单分配';
