# Phase2 复盘（写给未来的自己）

## 1. AI 真正帮到我的地方
- **体力活全部交出去**：账单 Manage/Snapshot/Aggregation/Adapter 等类的骨架、接口 wiring、DTO/POJO 跑腿都交给 AI，尤其是状态机流程和 repo 封装，省了大量重复劳动。
- **测试/文档辅助**：虽然最终测试因内网环境没跑，但自动生成的 wiki 接口图、前端改造要点让我后面只需要微调，不用从零写说明。

## 2. 关键设计必须自己把方向
- **BillingAdapter 不做清算**：始终坚持“拉账 + 快照”与“回填 TermInst”解耦，AI 有意把聚合/写回放一块时，及时把写操作拉回 CoreService。
- **TermInst 保持干净**：坚持 autoLoad 只标记来源，不允许 Adapter 直接写 TermInst；所有落库都在 Core 层，避免 service 层各自写表造成后续审计难题。
- **状态机语义**：把 INIT 改成 PENDING、LOADING/READY/FAILED 锁死行为——这部分全靠自己盯，否则 AI 容易默认“失败了再次调用就重拉”。

## 3. Phase3 的协作 SOP（沿用这个套路）
1. **先人工定领域模型 & 状态机**：写出 Batch/TermInst/快照/状态流转，澄清“谁写库谁读库”。
2. **再让 AI 写 Demo+测试**：骨架、Repo、DTO、控制器、主干流程全部交给它。
3. **人工做边界审核/坏场景补丁**：重点检查状态机、幂等、回滚/错误提示，以及“谁负责写”的问题。

> 结论：Phase2 证明“先人工定方向 → AI 执行 → 人类兜底”的 SOP 可行，Phase3 继续沿用，别忘了把自定义的核心原则随时写进 wiki，方便 AI 和未来的自己同步。
