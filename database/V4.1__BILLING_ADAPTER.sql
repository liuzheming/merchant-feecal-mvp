-- billing adapter phase2 schema updates

ALTER TABLE `fee_cal_batch`
    ADD COLUMN `billing_data_status` VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '账单同步状态: PENDING / LOADING / READY / FAILED' AFTER `status`;

ALTER TABLE `fee_cal_term_def`
    ADD COLUMN `auto_load` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否自动拉账填充' AFTER `enabled`;

ALTER TABLE `fee_cal_term_inst`
    ADD COLUMN `auto_load` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否来自自动拉账' AFTER `deduct_amount`;

CREATE TABLE `fee_cal_billing_snapshot`
(
    `id`                    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `batch_no`              VARCHAR(64)  NOT NULL COMMENT '清算批次号',
    `term_code`             VARCHAR(64)  NOT NULL COMMENT '费用项 code',
    `billing_key`           VARCHAR(128)         DEFAULT NULL COMMENT '上游账单可读标识',
    `billing_should_pay_amount` DECIMAL(18, 2)      DEFAULT 0 COMMENT '应付金额',
    `billing_actual_pay_amount` DECIMAL(18, 2)      DEFAULT 0 COMMENT '已付金额',
    `billing_unpaid_amount`     DECIMAL(18, 2)      DEFAULT 0 COMMENT '未付金额（应付-已付）',
    `billing_source_info`    JSON                         NULL COMMENT '上游账单原始JSON',
    `snapshot_time`          DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '快照时间',
    PRIMARY KEY (`id`),
    KEY `idx_billing_batch_term` (`batch_no`, `term_code`),
    KEY `idx_billing_batch_term_key` (`batch_no`, `term_code`, `billing_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='清算账单视图快照';
