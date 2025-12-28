-- 费用清算批次表
CREATE TABLE `fee_cal_batch`
(
    `id`                     BIGINT      NOT NULL AUTO_INCREMENT,
    `batch_no`               VARCHAR(64) NOT NULL COMMENT '清算批次号',
    `status`                 VARCHAR(32) NOT NULL DEFAULT 'EDITING' COMMENT '状态: EDITING / DONE',
    `deposit_balance_total`  DECIMAL(18, 2)       DEFAULT 0 COMMENT '初始保证金余额（用户输入）',
    `deposit_amount_deduct`  DECIMAL(18, 2)       DEFAULT 0 COMMENT '本次抵扣金额总和',
    `deposit_balance_remain` DECIMAL(18, 2)       DEFAULT 0 COMMENT '清算后剩余保证金（计算得出）',
    `unpaid_amount_remain`   DECIMAL(18, 2)       DEFAULT 0 COMMENT '抵扣后剩余欠付金额（仍需支付）',
    `ctime`                  DATETIME             DEFAULT CURRENT_TIMESTAMP,
    `mtime`                  DATETIME             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_batch_no` (`batch_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='费用清算批次表';

-- 清算主体表
CREATE TABLE `fee_cal_merchant`
(
    `id`                 BIGINT      NOT NULL AUTO_INCREMENT,
    `batch_no`           VARCHAR(64) NOT NULL COMMENT '清算批次号',
    `merchant_type`      VARCHAR(32) NOT NULL COMMENT '主体类型',
    `merchant_code`      VARCHAR(64) NOT NULL COMMENT '主体编码',
    `deposit_account_id` VARCHAR(64) DEFAULT NULL COMMENT '保证金账户ID（预留）',
    `ctime`              DATETIME    DEFAULT CURRENT_TIMESTAMP,
    `mtime`              DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY                  `idx_batch_no` (`batch_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='清算主体表';

-- 费用项实例表
CREATE TABLE `fee_cal_term_inst`
(
    `id`            BIGINT      NOT NULL AUTO_INCREMENT,
    `batch_no`      VARCHAR(64) NOT NULL COMMENT '清算批次号',
    `merchant_id`   BIGINT      NOT NULL COMMENT '清算主体ID',
    `term_code`     VARCHAR(64) NOT NULL COMMENT '费用项 code',
    `status`        VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING / DRAFT / DONE',
    `unpaid_amount` DECIMAL(18, 2)       DEFAULT 0 COMMENT '欠付金额',
    `deduct_flag`   TINYINT(1) DEFAULT 0 COMMENT '是否抵扣: 0 否 / 1 是',
    `deduct_amount` DECIMAL(18, 2)       DEFAULT 0 COMMENT '抵扣金额',
    `ctime`         DATETIME             DEFAULT CURRENT_TIMESTAMP,
    `mtime`         DATETIME             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY             `idx_batch_no_merchant` (`batch_no`, `merchant_id`),
    KEY             `idx_term_code` (`term_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='费用项实例表';

-- 费用项定义表
CREATE TABLE `fee_cal_term_def`
(
    `code`    VARCHAR(64)  NOT NULL COMMENT '费用项 code（主键）',
    `name`    VARCHAR(128) NOT NULL COMMENT '费用项名称',
    `sort_no` INT      DEFAULT 0 COMMENT '展示顺序',
    `enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用: 1 启用 / 0 停用',
    `ctime`   DATETIME DEFAULT CURRENT_TIMESTAMP,
    `mtime`   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='费用项定义表';


