# FeeCal 技术综述

## 1. 清算相关表结构

| 表名 | 作用 | 关键字段 |
|------|------|----------|
| `fee_cal_batch` | 清算批次主表，承载批次状态与保证金汇总 | `batch_no`：唯一索引；`status`、`billing_data_status`：核心状态机；`deposit_*` & `unpaid_amount_remain`：金额汇总 |
| `fee_cal_merchant` | 批次关联的清算主体 | `batch_no`、`merchant_type`、`merchant_code` |
| `fee_cal_term_def` | 可配置的费用项定义 | `code`/`name`；`auto_load`（账单自动回填）；`bill_alloc_enabled`（是否支持账单级分配） |
| `fee_cal_term_inst` | 费用项实例（TermInst），是清算聚合根 | `batch_no`、`merchant_id`、`term_code`；`status`（PENDING/DRAFT/DONE）；`unpaid_amount`、`deduct_flag`、`deduct_amount`；`billing_alloc_enabled` |
| `fee_cal_billing_snapshot` | 上游账单快照 | `batch_no`、`term_code`；`billing_key`；`should_pay/actual_pay/unpaid_amount`；`billing_source_info`（JSON 原始数据） |
| `fee_cal_term_inst_alloc` | 账单级抵扣明细，TermInst 对应的账单列表 | `term_inst_id`、`billing_snapshot_id`（UK）；`unpaid_amount`；`alloc_flag`、`alloc_amount` |
| `fee_cal_fund_instruction` | 资金指令，连接清算结果与资金执行 | `fund_direction`、`fund_biz_type`、`account_type`；`should_amount/actual_amount`；`fund_status`（PENDING/EXECUTING/SUCCESS/FAIL）；`callback_status`（NOT_STARTED/DOING/SUCCESS/FAIL）；`payer/payee` 信息 |

> 其他辅助表（如日志表、通用配置）暂与清算解耦，MVP 聚焦以上七张核心表。

## 2. 核心模块与接口

- **FeeCalCoreService (`IFeeCalCoreService`)**  
  - 职责：批次启动、主体落库、TermInst 初始化、批次状态维护、账单状态校验。  
  - 关键接口：`start(FeeCalStartRequest)`、`loadCoreContext(batchNo)`、`ensureBillingDataReady(context)`、`updateBatch/TermInst`。

- **FeeCalSummaryService (`IFeeCalSummaryService`)**  
  - 职责：清算页面渲染、计算、保存草稿与提交，协调 TermInst、Bill Allocation、批次金额。  
  - 接口：`getPage` / `calculate` / `saveDraft` / `submit`。  
  - 说明：提交完成后由 Facade 触发资金指令生成，服务本身仅聚焦清算域。

- **FeeCalBillingManageService (`IFeeCalBillingManageService`)**  
  - 职责：编排 BillingAdapter 拉账 → `IBillingSnapshotService` 落库 → 状态机切换。  
  - 依赖：`BillingAdapter`（聚合多个 Proxy）、`IBillingSnapshotService`、`FeeCalTermInstAllocService`。

- **FeeCalTermInstAllocService**  
  - 职责：基于快照批量重建 `fee_cal_term_inst_alloc`；校验/保存账单级抵扣。  
  - 接口：`rebuildAllocations(FeeCalCoreContext)`、`loadBillItems(batchNo, termInsts)`、`applyAllocations(termInst, billItems, termDeductAmount)`。

- **FundInstructionAppService (`IFundInstructionAppService`)**  
  - 职责：资金指令的生成、列表查询、执行/重试、计费回调。  
  - 接口：`generateForBatch`、`listByBatch`、`execute`、`retry`、`callback`。  
  - 内部组件：`FundInstructionGenerator`、`FundExecutor`（封装 `FundGateway`）、`FundBillingCallbackService`。

- **FeeCalFacadeImpl (`IFeeCalFacade`)**  
  - 职责：统一编排清算 START / SUMMARY / BATCH LIST 等入口；在 `summarySubmit` 后追加调用 `FundInstructionAppService.generateForBatch`，保证清算与资金流程解耦但连贯。

## 3. MVP 过程中的 Trade-off

1. **TermInst 仍做聚合根，账单映射延后拆解**  
   - 选择在 `fee_cal_term_inst_alloc` 中维护 1:1 的 TermInst 与 BillingSnapshot 关系，未引入更复杂的多对多映射。  
   - Trade-off：先保证账单级抵扣能力上线，后续再按需求引入 `fee_cal_fund_instruction_detail` 等关系表，避免前期设计过度。

2. **Billing 拉取与清算耦合在 Core 流程，而非异步**  
   - `FeeCalCoreService.start` 里串联 `FeeCalBillingManageService.refreshBillingData`、聚合、分配重建。  
   - 好处：MVP 阶段确保“创建批次后即可看到欠费数据”；代价是 start 阶段较重，未来可拆成异步任务 + 前端轮询。

3. **资金指令域保持单表 + 同步执行**  
   - 资金执行、回调均采用 Mock 网关 + 同步状态流转，`fund_status` 和 `callback_status` 分成两条状态线但逻辑简单。  
   - 取舍：先验证“清算→资金”闭环，后续真实对接时可以替换 `FundGateway`、补充异步轮询/审计日志。

4. **配置驱动改造逐步推进**  
   - `billing_alloc_enabled`、`auto_load`、TermCode 与 Adapter 的映射仍在少量常量与 TermDef 之间过渡。  
   - 决策：在前端/运营明确配置需求前，允许少量常量存在，等模型稳定再完全配置化。

5. **事件 vs Facade 编排**  
   - 最终选择由 Facade 在 `summarySubmit` 后调用 `FundInstructionAppService.generateForBatch`，而不是让 SummaryService 直接操作资金域。  
   - 这样保持领域职责清晰，同时后续如果要改成事件驱动，只需在 Facade 或 AppService 层调整，不影响清算核心。

> 上述取舍确保 MVP 快速交付的同时，保留了后续拆分与扩展的接口与分层空间。
