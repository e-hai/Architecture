# iOS Firebase (`GoogleService-Info.plist`) 测试 / 正式环境物理隔离说明

为防止测试包与正式包混用或同时打包两个 Firebase 配置文件，工程在 [`iosApp/Sources/`](../../Sources/) 之外按目录物理隔离两套环境凭证：

| 构建环境 | Xcode Scheme / Configuration | 本地存放路径（已纳入 `.gitignore`） | 运行时行为 |
| :--- | :--- | :--- | :--- |
| **测试环境 (Dev)** | `iosApp-Dev` (`Debug`) | `iosApp/Config/Firebase/Dev/GoogleService-Info.plist` | 构建期自动拷贝至 `.app` 根目录，并校检 `BUNDLE_ID == Dev Bundle ID` |
| **正式环境 (Prod)** | `iosApp-Prod` (`Release`) | `iosApp/Config/Firebase/Prod/GoogleService-Info.plist` | 构建期自动拷贝至 `.app` 根目录，并校检 `BUNDLE_ID == Prod Bundle ID` |

## 注意事项
1. **免重跑 XcodeGen**：本目录下的 `.plist` 由 [`scripts/hooks/copy_ios_firebase_plist.sh`](../../../scripts/hooks/copy_ios_firebase_plist.sh) 在构建期按当前 `CONFIGURATION` 隔离复制，增删或更新 `.plist` 后**无需**再次执行 `xcodegen generate`。
2. **缺失即零阻塞降级**：若当前项目暂未接入 Firebase 或 CI 未注入该文件，构建脚本会自动清理 `.app` 内可能存在的旧残留并跳过复制，[`NativeKitBootstrap.swift`](../../Sources/Bridge/NativeKitBootstrap.swift) 与相关 Delegate 会在运行时自动安全降级。
3. **防串环境硬拦截**：
   - 一旦检测到对应目录下存在 `GoogleService-Info.plist`，脚本会自动核对其中的 `BUNDLE_ID` 与当前 Xcode 构建的 `PRODUCT_BUNDLE_IDENTIFIER` 是否一致，若将 Dev 与 Prod 文件放反会立即报错阻断构建；
   - **严禁**再将 `GoogleService-Info.plist` 直接放入 [`iosApp/Sources/`](../../Sources/) 下，否则会触发构建阶段报错拦截（避免两套配置被一并打入产物体）。
