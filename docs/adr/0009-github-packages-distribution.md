# 0009. SDK 统一分发下线 JitPack 全面拥抱 GitHub Packages

* 状态: Accepted
* 日期: 2026-11-11

## 背景与问题上下文

此前项目中的自研原生与跨平台 Kit 依赖大多通过 JitPack 动态构建分发（`https://jitpack.io`）。
在实际工程实践中，JitPack 暴露出诸多生产环境隐患：
1. **构建不可靠与网络抖动**：JitPack 经常发生构建超时、下载中断或 HTTP 500/502 错误，导致 CI/CD 流程脆弱；
2. **版本不可变性与快照机制混乱**：JitPack 动态拉取构建，难以做严格的企业级制品权限控制与安全审计；
3. **分发格式割裂**：Android 原生 AAR 与 KMP 跨平台 `.klib` 在 JitPack 上的兼容性与 Gradle 变体支持不足。

## 备选方案

1. **方案 1：继续使用 JitPack 并添加代理/重试脚本**
   - **缺点**：治标不治本，无法解决偶发性构建失败与稳定性问题。
2. **方案 2：自建私有 Nexus / Artifactory 制品库**
   - **缺点**：增加运维基础设施成本与服务器维护负担。
3. **方案 3：全面迁移至 GitHub Packages (GitHub Maven Registry)**
   - **优点**：与 GitHub 组织及源码仓库无缝集成；发布流程由 GitHub Actions 标准化构建，产物稳定持久；开箱即用支持 Android AAR 与 KMP Gradle 模块发布；全球 CDN 加速。

## 最终决策

选择 **方案 3**。
全面下线 JitPack：
1. 在 `settings.gradle.kts` 中彻底移除 `maven { url = uri("https://jitpack.io") }`；
2. 动态循环配置 10 大 Kit 对应的 GitHub Packages Maven 仓库（`https://maven.pkg.github.com/e-hai/<repoName>`）；
3. 凭证统一读取 `~/.gradle/gradle.properties` 中的 `gpr.user` / `gpr.key` 或 CI 环境变量 `GITHUB_ACTOR` / `GITHUB_TOKEN`；
4. 版本依赖统一规范化，去除 `v` 前缀。

## 影响

### 正面影响
* 依赖解析具备确定性，构建速度显著提升，彻底规避 JitPack 偶发编译失败；
* 制品与源码版本严格绑定，可追溯、防篡改；
* 原生与 KMP 库的 Gradle Module Metadata 得到原生级完整支持。

### 负面影响与注意事项
* 访问 GitHub Packages 必须携带带有 `read:packages` 权限的 GitHub Token，本地开发者需在全局配置 `~/.gradle/gradle.properties`；
* 极少数发布失败的库在紧急修复期间需临时配合 `mavenLocal()` 兜底验证。
