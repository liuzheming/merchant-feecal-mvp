-- 幂等表（用于流程级幂等控制）
CREATE TABLE `fee_cal_idempotent`
(
    `id`         BIGINT      NOT NULL AUTO_INCREMENT,
    `biz_type`   VARCHAR(64) NOT NULL COMMENT '业务类型',
    `request_id` VARCHAR(64) NOT NULL COMMENT '幂等请求ID',
    `status`     VARCHAR(32) NOT NULL COMMENT '状态: PROCESSING / SUCCESS / FAIL',
    `batch_no`   VARCHAR(64) DEFAULT NULL COMMENT '关联批次号',
    `result_msg` VARCHAR(256) DEFAULT NULL COMMENT '处理结果/错误信息',
    `ctime`      DATETIME    DEFAULT CURRENT_TIMESTAMP,
    `mtime`      DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_biz_request` (`biz_type`, `request_id`),
    KEY `idx_batch_no` (`batch_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='清算流程幂等表';
