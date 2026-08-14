# ADR-0017: 使用 VideoKit 作为音视频播放与剪辑基础设施

## 状态

Accepted

---

## 背景

短视频业务（`feature/shortvideo` 分支）需要工业级的音视频播放、预加载、缓存与剪辑能力。手写 Media3 ExoPlayer 封装容易出现预加载不一致、播放器实例泄露、内存卡顿及重复造轮子的问题。已有自研 [VideoKit](https://github.com/e-hai/VideoKit) 提供了 Feed 竖滑、VOD 点播、Clip 剪辑调参引擎、1GB 磁盘 LRU 缓存与双向预加载队列，同时原生支持 Jetpack Compose。

## 决策

- 使用 **VideoKit `v0.0.1`**（JitPack 坐标 `com.github.e-hai.VideoKit:video`，可选 `video-ffmpeg` 软解扩展）。
- 新增基础设施模块 **`:core:video`**，接入方式与其它 Kit 瘦身门面范式保持严格一致：
  - `api(libs.videokit.core)`：透出 VideoKit API，业务模块与 Application 直接调用门面与 Composable（如 `FeedCoverPlayerView`、`VodPlayer`、`ClipEngine` 等）。
  - `VideoInitializer`：集中配置视频缓存大小与预加载策略，在 `MyApplication.onCreate()` 中初始化。
  - **不**包多余 Helper / Koin，避免多层抽象与冗余开销。
- 扩充图片加载库为 `coil-compose`，作为视频封面与头像加载标准库。

## 影响

- 减少了播放器底层开发与维护成本，开箱即用获得秒开、预加载和磁盘缓存能力。
- 与项目既有的 API/Impl 模块化、Koin 依赖注入及 Navigation 3 架构无缝契合。

## 参考

- [VideoKit GitHub 仓库](https://github.com/e-hai/VideoKit)
- ADR-0011（AnalyticsKit 瘦身门面范式）
- ADR-0012（MMP / Ads / Pay 接入范式）
