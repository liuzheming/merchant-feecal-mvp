ALTER TABLE fee_cal_batch
    ADD COLUMN request_id VARCHAR(64) DEFAULT NULL COMMENT '幂等请求ID',
    ADD UNIQUE KEY uk_request_id (request_id);
