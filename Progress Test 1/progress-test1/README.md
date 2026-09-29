## Coverage (JaCoCo)

| Class | Line coverage | Branch coverage |
|---|---|---|
| AccountService | 92% | 87% |
| AccountValidator | 100% | 98% |

*(≥ ngưỡng yêu cầu: Line ≥ 80%, Branch ≥ 70%)*

![JaCoCo coverage](Coverage.png)

## Mutation testing thủ công

| # | Lỗi chèn | Test fail | Đã hoàn tác |
|---|---|---|---|
| M1 | `login()`: đổi `>=` thành `>` ở điều kiện khóa tài khoản | `Login.login_FifthFailedAttempt_LocksAccount` | ✅ |
| M2 | `login()`: xóa nhánh `if (acc.isLocked())` | `Login.login_LockedAccount_CorrectPassword_StillReturnsAccountLocked` | ✅ |
| M6 | `Account.unlock()`: quên đặt lại `failedAttempts = 0` | Ban đầu **không có test nào fail** → đã bổ sung `Login.login_AfterUnlock_FailedAttemptsResetToZero`, chèn lại lỗi lần 2 → test mới fail đúng như kỳ vọng | ✅ |