# termInst 初始化与拉账调度任务

``` 
start():
    - 幂等
    - 创建 batch(INIT)
    - 创建 termInst(INIT)
    - 拉取保证金账户余额
    - return

advance(batchId):
    - pullBill & （未来扩展为 拉账 + 冻结账单）
    - 修改 batch:billing_data_status: => READY 状态
    - return
    
advance(batchId):
    - 找到拉账状态已经 ready 的 batch
    - 计算每个费用项的欠费金额
    - 查询到自动抵扣规则（以前是人工抵扣，自动化之后必然需要有自动抵扣的规则）
    - 根据自动抵扣规则，填写每个费用项的抵扣金额
    - 变更 status，batchStatus => SUBMITED
    - check 账单快照跟上游系统账单是否一致
        - 一致：
        - 不一致：变更 status，batchStatus => FAILD；这次清算的 trigger 放入失败队列，等待重试
    - return
    
advance(batchId):
    - 找到 SUBMITED 的 batch
    - 根据资金指令的生成规则，生成资金指令
    - 变更 batch 状态，到 AUDITING
    
advance(batchId):
    - 等待人工审核，审核完成后，batch 状态变更为 AUDITED
    
advance(batchId):
    - 找到状态为 AUDITED 的 batch
    - 然后为每条指令，到资金系统生成对应的资金单子
    - 修改 batch 状态为 DONE


```