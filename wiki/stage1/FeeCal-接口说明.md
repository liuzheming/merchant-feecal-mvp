# FeeCal Phase3 接口说明

> 面向前端、联调同学的接口速查。所有接口均返回 `ResponseResult`，业务数据在 `data` 字段，失败时 `errno/errmsg` 体现错误原因。

---

## 1. 清算批次列表

- **GET** `/web/feeCal/batch/list`
- **Query 参数**
  | 参数 | 说明 |
  |------|------|
  | `merchantCode` | 主体编码，可选 |
  | `merchantType` | 主体类型（MERCHANT/STORE/...），可选 |
  | `batchStatus` | `EDITING` / `DONE`，可选 |
  | `billingDataStatus` | `PENDING/LOADING/READY/FAILED`，可选 |
  | `pageNo` | 默认 1（<1 时按 1） |
  | `pageSize` | 默认 20，最大 50 |

- **响应** `PageResult<BatchItem>`

```json
{
  "errno": "0",
  "errmsg": "",
  "data": {
    "pageNo": 1,
    "pageSize": 20,
    "total": 35,
    "list": [
      {
        "batchNo": "FC202501010001",
        "batchStatus": "EDITING",
        "billingDataStatus": "READY",
        "merchantType": "STORE",
        "merchantCode": "ST123456",
        "depositAmountDeduct": 1234.56,
        "depositBalanceRemain": 765.44,
        "unpaidAmountRemain": 0,
        "createdAt": "2025-01-10T12:01:03"
      }
    ]
  }
}
```

---

## 2. 清算流程

| 功能 | 方法 | 路径 | 请求体 | 说明 |
|------|------|------|--------|------|
| 发起批次 | POST | `/web/feeCal/summary/start` | `FeeCalStartRequest`（merchantType / merchantCode 等） | 返回 `batchNo` |
| 页面渲染 | GET | `/web/feeCal/summary/page?batchNo=` | - | 返回 `FeeCalPageDTO`（批次 + term 列表） |
| 实时计算 | POST | `/web/feeCal/summary/calculate` | `FeeCalCalculateRequest` | 不落库，用于草稿预览 |
| 保存草稿 | POST | `/web/feeCal/summary/saveDraft` | `FeeCalSaveRequest` | 落库草稿 |
| 提交清算 | POST | `/web/feeCal/summary/submit` | `FeeCalSubmitRequest` | 状态置 `DONE` 并触发资金指令生成 |

`FeeCalPageDTO` 要点：
- `merchantInfo`：主体类型/编码
- `depositInfo`：抵扣金额、余额、欠费等
- `termCards[]`：
  - 基础字段：`code/name/status/autoLoad/unpaidAmount/deductFlag/deductAmount`
  - `supportBillAlloc`：是否支持账单级分配（如保底费）
  - `billItems[]`（仅 `supportBillAlloc=true`）：数组元素结构如下

```json
{
  "allocId": 30001,
  "billingSnapshotId": 50001,
  "billingKey": "MIN_GUAR_2024Q1#001",
  "billingDesc": "2024Q1 保底 #001",
  "unpaidAmount": 800.00,
  "deductFlag": true,
  "deductAmount": 300.00
}
```

> 前端在 `calculate/saveDraft/submit` 请求中需把最新的 `billItems` 原样回传，否则后端无法校验/保存账单级抵扣。若取消勾选，可将 `deductFlag=false`（`deductAmount` 会被置 0）。

> **请求侧约束**：对于 `supportBillAlloc=true` 的费用项，`termCards[].billItems[]` 为必填字段，且单条 `deductAmount ≤ unpaidAmount`，所有账单抵扣之和必须等于该 term 的 `deductAmount`。

---

## 3. 资金指令模块

### 3.1 指令生成 / 查询

- **POST** `/web/feeCal/fund/batch/{batchNo}/generate`
  - 手工重跑指令（幂等）。提交清算时系统会自动调用；仅在需要“重新生成”时触发。

- **GET** `/web/feeCal/fund/batch/{batchNo}/list`
  - **响应** `FundInstructionDTO[]`

```json
[
  {
    "id": 1001,
    "batchNo": "FC202501010001",
    "termInstId": 2001,
    "settleSubjectType": "MERCHANT",
    "settleSubjectNo": "ST123456",
    "payerType": "MERCHANT",
    "payerNo": "ST123456",
    "payeeType": "PLATFORM",
    "payeeNo": "PLATFORM",
    "fundDirection": "DEBIT",
    "fundBizType": "DEPOSIT_DEDUCT",
    "accountType": "DEPOSIT",
    "shouldAmount": 500.00,
    "actualAmount": 500.00,
    "fundStatus": "SUCCESS",
    "callbackStatus": "SUCCESS",
    "fundOrderId": "MOCK-1001-1736521000000",
    "fundChannel": "MOCK_CHANNEL",
    "fundOrderInfo": "{\"status\":\"SUCCESS\"}",
    "attachmentIds": "attA,attB",
    "createdAt": "2025-01-10T12:05:00",
    "updatedAt": "2025-01-10T12:06:30"
  }
]
```

### 3.2 手工操作接口

| 功能 | 方法 | 路径 | 请求体 |
|------|------|------|--------|
| 执行指令 | POST | `/web/feeCal/fund/{instructionId}/execute` | `{ "operator": "u123", "operatorName": "张三", "remark": "手工扣款", "attachmentIds": ["attA","attB"] }` |
| 重试指令 | POST | `/web/feeCal/fund/{instructionId}/retry` | 同 execute |
| 计费回调 | POST | `/web/feeCal/fund/{instructionId}/callback` | `{ "operator": "u123", "operatorName": "张三", "remark": "账务确认" }` |

约束：
- `execute` 仅 `fundStatus=PENDING`
- `retry` 仅 `fundStatus=FAIL`
- `callback` 仅 `fundStatus=SUCCESS`（执行成功才可回填）

---

## 4. 状态/枚举参考

| 枚举 | 取值 |
|------|------|
| `batchStatus` | `EDITING`, `DONE` |
| `billingDataStatus` | `PENDING`, `LOADING`, `READY`, `FAILED` |
| `fundStatus` | `PENDING`, `EXECUTING`, `SUCCESS`, `FAIL` |
| `callbackStatus` | `NOT_STARTED`, `DOING`, `SUCCESS`, `FAIL` |
| `fundBizType` | `DEPOSIT_DEDUCT`, `DEPOSIT_REFUND` |
| `fundDirection` | `DEBIT`（扣款） / `CREDIT`（退款） |

---

## 5. 响应包装

所有接口统一返回：

```json
{
  "errno": "0",
  "errmsg": "",
  "tip": null,
  "traceId": "xxx",
  "data": {...}
}
```

- `errno="0"` 表示成功。
- `tip` 用于需提示的场景（例如余额不足时在 `summary/page` 的 `ResponseResult` 会附带 `"tip": "保证金余额不足"`）。
- `traceId` 来自 SkyWalking，可用于排查链路。

---

如需示例请求体/Mock 数据，可在 DTO 定义处参考字段说明，或找后端同学获取测试脚本。*** End Patch*** End Patch
