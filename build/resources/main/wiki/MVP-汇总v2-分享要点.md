# Phase2 BillingAdapter 分享讲解要点

## 1. 架构主线（先讲故事，再落细节）
1. **业务目标**
   - Phase2 要把上游账单接进来，汇总页不再靠手工录入；重点是“拉账 → 快照 → 聚合 → 回填”这条链路。
2. **状态机驱动**
   - Batch 上新增 `billingDataStatus = {PENDING, LOADING, READY, FAILED}`，所有页面操作只在 READY 状态放行；其它状态直接报错，避免读到脏数据。
3. **职责拆分**
   - `IFeeCalBillingManageService`：只处理拉账流程（状态判断、调 BillingAdapter、写快照）；
   - `IBillingViewService`：封装 `fee_cal_billing_snapshot` 的纯数据操作（查询/覆盖/聚合）；
   - `IBillingAggregationService`：只负责从快照里“算金额”，不写 TermInst；
   - `IFeeCalCoreService`：最终落库的地方。它在 `start()` 里先触发拉账，再拿聚合结果回填 autoLoad 的 TermInst，确保所有写操作集中在 Core。

## 2. 流程细节（按调用顺序拆解）
1. **start**
   - 创建批次 & TermInst（记录 `autoLoad` 配置） → 调 BillingManage 切换状态到 LOADING 并调用 BillingAdapter 拉账 → SnapshotService 覆盖快照 → 核心服务读取聚合 map，回填 autoLoad TermInst。
   - 若拉账失败，状态回到 FAILED，后端/前端都会提示“账单拉取失败，请稍后再试”。
2. **汇总页 / 计算 / 保存 / 提交**
   - 入口处统一 `ensureBillingDataReady`（READY 才继续，其他状态直接抛错）；
   - 读取 TermInst + TermDef 装配成页面 DTO，autoLoad 的卡片在前端展示“账单填充”，禁止编辑。

## 3. 设计取舍 & 常见追问
1. **为什么要拆 Manage / Snapshot / Aggregation / Core？**
   - 目的是让写操作集中在 Core，便于做事务、审计、策略控制；
   - Snapshot & Aggregation 都是纯读服务，可复用在后续“重拉/定时校验”场景；
   - BillingManage 只关注状态机和落快照，逻辑清晰。
2. **如何保证幂等/一致性？**
   - 状态机层面：只有 PENDING/FAILED 才能触发拉账；LOADING 阻塞重复调用；READY 视为成功态；
   - 数据层面：快照是“先删后写”，聚合结果来自最新快照；所有 TermInst 回填在一个事务里完成。
3. **扩展点**
   - 如果未来要支持手工重拉，只需在 READY/FAILED 场景暴露一个拉账入口，复用同样的 Manage 服务；
   - 回填策略可以在 Core 层做增强，比如只覆盖“未被手工修改”的 autoLoad 项或记录审计日志。

## 4. 快速总结（30 秒说清版本）
> “Phase2 我通过 `billingDataStatus` 管控账单生命周期，拉账流程拆成 Manage（状态机 + Adapter）→ Snapshot（快照落库）→ Aggregation（纯聚合）→ Core（唯一写入者）。所有页面仅在 READY 状态工作，其余状态直接报错。这样既保证了数据一致性，也为后续重拉、策略扩展留了空间。”
