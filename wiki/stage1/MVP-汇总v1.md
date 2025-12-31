汇总MVP（完整版）


use case：

用户：城市CA
1.发起清算：
a.用户打开清算入口页面，看到几个选项：城市下拉框、主体类型下拉框、主体检索框
b.分别选中这几个选项之后，点击下方的按钮：确认发起清算
c.系统接收到清算发起指令，生成清算批次号给前端页面
i.此时系统同时应当完成当前批次清算的初始化: mvp版本从拉取配置表中的全部费用项进行初始化，不设置默认金额
1.初始化清算批次实例
2.初始化清算主体实例
3.初始化清算费用项实例
d.前端页面拿到返回的批次号之后，跳转到对应的清算页面
i.页面模块一：展示清算主体信息
ii.页面模块二：保证金信息
1.保证金账户余额（可编辑）
2.保证金可抵扣余额（不可编辑）
iii.页面模块三：展示等待清算的费用项
1.每个费用项占一行
a.字段一：左侧是费用项名称
b.字段二：右侧是欠付金额 input 框（可编辑）
c.字段三：是否可抵扣
d.字段四：抵扣金额
2.一系列等待清算的欠付费用项
iv.页面模块四：页面的下方有清算总结，展示
1.仍需支付金额（不可编辑）
2.从保证金抵扣金额（不可编辑）
3.保证金余额（不可编辑）
2.录入金额
a.录入保证金账户余额
i.前端校验余额合法性
ii.提交全部表单的数据到后端，后端计算出 保证金可抵扣余额、清算总结，同时为防止计算错误，使用 redis 锁进行单步锁定
iii.录入金额阶段，系统接口只计算汇总结果，不落库
b.录入欠费项金额
i.前端校验余额合法性
ii.选择是是否可抵扣为  是，右侧出现抵扣金额 input 框，录入抵扣金额
iii.操作过程中，任何字段变更，前端都提交全部表单的数据到后端，后端计算出 保证金可抵扣余额、清算总结，返回前端，同时为防止计算错误，使用 redis 锁进行单步锁定
iv.录入金额阶段，系统接口只计算汇总结果，不落库
c.保存编辑结果，保存后数据落库
3.提交清算表单，确认清算完成
a.接收到前端提交后，再次对全部数据进行校验，确认无误后校验通过
b.变更费用项状态为：汇总完成
c.变更清算批次状态为：清算完成




# 费用清算 · 汇总 MVP 设计文档（单文件完整版：DB + 接口）

============================================================
一、数据库设计（DB Schema）
============================================================

1. fee_cal_batch — 清算批次表
------------------------------------------------------------
CREATE TABLE fee_cal_batch (
id BIGINT NOT NULL AUTO_INCREMENT,
batch_no VARCHAR(64) NOT NULL COMMENT '清算批次号',
status VARCHAR(32) NOT NULL DEFAULT 'EDITING' COMMENT '状态: EDITING / DONE',

deposit_balance_total  DECIMAL(18,2) DEFAULT 0 COMMENT '初始保证金余额（用户输入）',
deposit_amount_deduct  DECIMAL(18,2) DEFAULT 0 COMMENT '本次抵扣金额总和',
deposit_balance_remain DECIMAL(18,2) DEFAULT 0 COMMENT '清算后剩余保证金（计算得出）',

unpaid_amount_remain   DECIMAL(18,2) DEFAULT 0 COMMENT '抵扣后剩余欠付金额（仍需支付）',

ctime DATETIME DEFAULT CURRENT_TIMESTAMP,
mtime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

PRIMARY KEY (id),
UNIQUE KEY uk_batch_no (batch_no)
);


2. fee_cal_merchant — 清算主体表
------------------------------------------------------------
CREATE TABLE fee_cal_merchant (
id BIGINT NOT NULL AUTO_INCREMENT,
batch_no VARCHAR(64) NOT NULL COMMENT '清算批次号',

merchant_type VARCHAR(32) NOT NULL COMMENT '主体类型',
merchant_code VARCHAR(64) NOT NULL COMMENT '主体编码',

deposit_account_id VARCHAR(64) DEFAULT NULL COMMENT '保证金账户ID（预留）',

ctime DATETIME DEFAULT CURRENT_TIMESTAMP,
mtime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

PRIMARY KEY (id),
KEY idx_batch_no (batch_no)
);


3. fee_cal_term_inst — 费用项实例表
------------------------------------------------------------
CREATE TABLE fee_cal_term_inst (
id BIGINT NOT NULL AUTO_INCREMENT,
batch_no VARCHAR(64) NOT NULL COMMENT '清算批次号',
merchant_id BIGINT NOT NULL COMMENT '清算主体ID',

term_code VARCHAR(64) NOT NULL COMMENT '费用项 code',
status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING / DRAFT / DONE',

unpaid_amount DECIMAL(18,2) DEFAULT 0 COMMENT '欠付金额',
deduct_flag TINYINT(1) DEFAULT 0 COMMENT '是否抵扣: 0 否 / 1 是',
deduct_amount DECIMAL(18,2) DEFAULT 0 COMMENT '抵扣金额',

ctime DATETIME DEFAULT CURRENT_TIMESTAMP,
mtime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

PRIMARY KEY (id),
KEY idx_batch_no_merchant (batch_no, merchant_id),
KEY idx_term_code (term_code)
);


4. fee_cal_term_def — 费用项定义表
------------------------------------------------------------
CREATE TABLE fee_cal_term_def (
code VARCHAR(64) NOT NULL COMMENT '费用项 code（主键）',
name VARCHAR(128) NOT NULL COMMENT '费用项名称',

sort_no INT DEFAULT 0 COMMENT '展示顺序',
enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用: 1 启用 / 0 停用',

ctime DATETIME DEFAULT CURRENT_TIMESTAMP,
mtime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

PRIMARY KEY (code)
);


============================================================
二、统一页面数据结构（所有渲染/计算/保存/提交的出参完全一致）
============================================================

{
"batchNo": "xxxxx",
"batchStatus": "EDITING",

"merchantInfo": {
"merchantType": "franchise",
"merchantCode": "FRAN23129898"
},

"depositInfo": {
"depositBalanceTotal": 0.00,
"depositBalanceRemain": 0.00,
"depositAmountDeduct": 0.00,
"unpaidAmountRemain": 0.00
},

"termCards": [
{
"code": "baodifei",
"name": "保底费",
"status": "PENDING",
"unpaidAmount": null,
"deductFlag": null,
"deductAmount": null
}
]
}


============================================================
三、接口设计（全部 5 个接口）
============================================================

说明：
- 汇总阶段接口统一挂载在 `/web/feeCal/summary` 路径下，例如 `/web/feeCal/summary/start`。
- 全部响应封装在 `ResponseResult` 中，字段为 `errno` / `errmsg` / `traceId` / `tip` / `data`，其中成功时 `errno="0"`、`errmsg=""`。`traceId` 由后台自动写入；当保证金余额不足以覆盖抵扣时，后端会直接返回错误 `errmsg="保证金余额不足，无法完成抵扣"`（不再返回成功态 + tip）。
- 为便于阅读，下方 Response 示例均只展开 `data` 字段的内容。


------------------------------------------------------------
1）发起清算（创建批次）
------------------------------------------------------------
POST /web/feeCal/summary/start

Request:
{
"merchantType": "franchise",
"merchantCode": "FRAN23129898"
}

Response（data）:
{
"batchNo": "FC202511240001"
}


------------------------------------------------------------
2）渲染清算页面（根据 batchNo 查询）
------------------------------------------------------------
GET /web/feeCal/summary/page?batchNo=xxxxx

Response（data）:
{
"batchNo": "xxxxx",
"batchStatus": "EDITING",

    "merchantInfo": {
      "merchantType": "franchise",
      "merchantCode": "FRAN23129898"
    },

    "depositInfo": {
      "depositBalanceTotal": 0.00,
      "depositBalanceRemain": 0.00,
      "depositAmountDeduct": 0.00,
      "unpaidAmountRemain": 0.00
    },

    "termCards": [
      {
        "code": "baodifei",
        "name": "保底费",
        "status": "PENDING",
        "unpaidAmount": null,
        "deductFlag": null,
        "deductAmount": null
      },
      {
        "code": "lianwangfei",
        "name": "联网费",
        "status": "PENDING",
        "unpaidAmount": null,
        "deductFlag": null,
        "deductAmount": null
      }
    ]
}


------------------------------------------------------------
3）实时计算接口（不落库）
------------------------------------------------------------
POST /web/feeCal/summary/calculate

Request:
{
"batchNo": "xxxxx",

"depositInfo": {
"depositBalanceTotal": 10000.00
},

"termCards": [
{
"code": "baodifei",
"unpaidAmount": 2000.00,
"deductFlag": true,
"deductAmount": 1700.00
},
{
"code": "lianwangfei",
"unpaidAmount": 300.00,
"deductFlag": false,
"deductAmount": 0.00
}
]
}

Response（data，与渲染接口结构一致）:
{
"batchNo": "xxxxx",
"batchStatus": "EDITING",
"merchantInfo": {
"merchantType": "franchise",
"merchantCode": "FRAN23129898"
},
"depositInfo": {
"depositBalanceTotal": 10000.00,
"depositBalanceRemain": 8300.00,
"depositAmountDeduct": 1700.00,
"unpaidAmountRemain": 0.00
},
"termCards": [
{
"code": "baodifei",
"name": "保底费",
"status": "DRAFT",
"unpaidAmount": 2000.00,
"deductFlag": true,
"deductAmount": 1700.00
},
{
"code": "lianwangfei",
"name": "联网费",
"status": "DRAFT",
"unpaidAmount": 300.00,
"deductFlag": false,
"deductAmount": 0.00
}
]
}


------------------------------------------------------------
4）保存草稿（落库）
------------------------------------------------------------
POST /web/feeCal/summary/saveDraft

Request:
{
"batchNo": "xxxxx",
"depositInfo": {
"depositBalanceTotal": 10000.00
},
"termCards": [
{
"code": "baodifei",
"unpaidAmount": 2000.00,
"deductFlag": true,
"deductAmount": 1700.00
},
{
"code": "lianwangfei",
"unpaidAmount": 300.00,
"deductFlag": false,
"deductAmount": 0.00
}
]
}

Response（data，结构与渲染一致）:
{
"batchNo": "xxxxx",
"batchStatus": "EDITING",
"merchantInfo": {
"merchantType": "franchise",
"merchantCode": "FRAN23129898"
},
"depositInfo": {
"depositBalanceTotal": 10000.00,
"depositBalanceRemain": 8300.00,
"depositAmountDeduct": 1700.00,
"unpaidAmountRemain": 0.00
},
"termCards": [
{
"code": "baodifei",
"name": "保底费",
"status": "DRAFT",
"unpaidAmount": 2000.00,
"deductFlag": true,
"deductAmount": 1700.00
},
{
"code": "lianwangfei",
"name": "联网费",
"status": "DRAFT",
"unpaidAmount": 300.00,
"deductFlag": false,
"deductAmount": 0.00
}
]
}


------------------------------------------------------------
5）提交清算（落库 + DONE）
------------------------------------------------------------
POST /web/feeCal/summary/submit

Request:
{
"batchNo": "xxxxx",
"depositInfo": {
"depositBalanceTotal": 10000.00
},
"termCards": [
{
"code": "baodifei",
"unpaidAmount": 2000.00,
"deductFlag": true,
"deductAmount": 1700.00
},
{
"code": "lianwangfei",
"unpaidAmount": 300.00,
"deductFlag": false,
"deductAmount": 0.00
}
]
}

Response（data，状态 DONE）:
{
"batchNo": "xxxxx",
"batchStatus": "DONE",
"merchantInfo": {
"merchantType": "franchise",
"merchantCode": "FRAN23129898"
},
"depositInfo": {
"depositBalanceTotal": 10000.00,
"depositBalanceRemain": 8300.00,
"depositAmountDeduct": 1700.00,
"unpaidAmountRemain": 0.00
},
"termCards": [
{
"code": "baodifei",
"name": "保底费",
"status": "DONE",
"unpaidAmount": 2000.00,
"deductFlag": true,
"deductAmount": 1700.00
},
{
"code": "lianwangfei",
"name": "联网费",
"status": "DONE",
"unpaidAmount": 300.00,
"deductFlag": false,
"deductAmount": 0.00
}
]
}


============================================================
四、代码结构（与当前实现保持一致）
============================================================

- **接口入口**：`FeeCalSummaryController` 暴露 `/web/feeCal/summary` 下的 5 个接口，统一返回 `ResponseResult`。若抵扣导致保证金余额不足，会直接抛出业务异常（`errmsg="保证金余额不足，无法完成抵扣"`），需要前端捕获并提示。
- **流程编排**：`IFeeCalFacade` / `FeeCalFacadeImpl` 面向流程模块（BPM/任务分发），`start` 调用核心服务，`summaryPage/Calculate/SaveDraft/Submit` 调用汇总服务，未来的盘点（prepare）接口也会通过 Facade 统一编排。
- **服务分层**：
  - `IFeeCalCoreService` / `FeeCalCoreServiceImpl`（core）：负责批次/主体初始化、加载上下文、查询/更新 `fee_cal_batch` 与 `fee_cal_term_inst`、批量更新费用项状态，并对外提供统一方法（prepare/summary 共用）。
  - `IFeeCalSummaryService` / `FeeCalSummaryServiceImpl`（summary）：聚焦汇总业务，依赖 CoreService 获取上下文，执行金额计算与校验，保存/提交时落库并在提交成功后调用 `FundAdapter` 发送 `FundInstruction`。
- **支撑层**：
  - 费用项定义：`TermDefService` / `DefaultTermDefService` 负责读取 `fee_cal_term_def`，由 CoreService 统一消费。
  - 上游/下游适配：`BillingAdapter`（当前为 Noop，占位，后续盘点阶段用于拉取上游待清算项）、`FundAdapter`（提交汇总后发起资金指令）。
- **数据库**：使用 `db/migration/commerce-merchant-config-dao/V4.0__FEE_CAL.sql` 的四张表（批次/主体/费用项实例/费用项定义），字段、索引与文档第一部分完全一致，DTO 与返回结构也严格复用同名字段。

说明：
- 目前仅实现“汇总页”流程；`service/prepare` 目录已预留，后续上线盘点时可直接依托 CoreService/Fascade 层复用。
- 字段命名、JSON 结构及状态枚举需严格遵循本文档，避免前后端契约不一致。
