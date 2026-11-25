# 0010. 局部生命周期原生对象与多实例原生视图宿主规范

* 状态: Accepted
* 日期: 2026-11-25

## 背景与问题上下文

工程原有的 **范式 A（1:1 原生委托透传）** 通过 `Core*` 静态门面 + 全局单例 `*Delegate` 解决了应用级全局能力（日志、埋点、崩溃监控、支付、推送、全局缓存等）。但在以下场景中，单一全局委托会出现状态串扰与资源回收缺失问题：

1. **多实例局部逻辑对象**：同一页面或不同页面需要同时持有多个相互隔离的原生引擎/控制器实例（例如多路播放器控制器、RTC/IM 房间会话、临时蓝牙/上传任务），实例随页面或组件销毁而必须释放；
2. **多实例局部原生 UI 视图**：需要在 Compose Multiplatform 列表、Pager 或弹层中动态挂载与回收原生视图（如原生视频播放视图、WebView、信息流原生广告容器），且各商业化 Kit 模块仍需保持 **100% 纯 `commonMain` 契约**。

## 决策方案

采用 **「无状态全局工厂 + 局部作用域句柄」** 与 **「`:core:ui` 统一原生视图宿主」** 双层模型：

### 1. 纯逻辑局部对象：`ScopedNativeFactory` + `ScopedNativeHandle`
- **全局装配无状态工厂**：在 Kit 的 `*Delegate` 中暴露或返回 `ScopedNativeFactory<Config, T : ScopedNativeHandle>`，而非直接保存唯一有状态实例；
- **局部创建独立句柄**：业务 ViewModel 或 Compose 状态通过 `factory.create(config)` 获取实现 `ScopedNativeHandle : AutoCloseable` 的独立句柄；
- **作用域绑定释放**：ViewModel 中通过 `addCloseable(handle)` 随页面销毁自动释放；Compose 中使用 `:core:ui` 提供的 `rememberScopedNativeHandle(key) { factory.create(...) }`，在 `DisposableEffect` 离开组合树时幂等调用 `close()`。

### 2. 原生 UI 视图局部对象：`:core:ui` `NativeViewRegistry` + `NativeViewHost`
- **桥接壳下沉集中**：全仓仅在 `:core:ui`（`androidMain` / `iosMain`）维护一次 `AndroidView` / `UIKitView` 生命周期胶水代码，其他 9 大范式 A Kit 严禁新增 `androidMain` / `iosMain`；
- **双端冷启动注册工厂**：Android 宿主（`NativeKitBootstrap.kt`）与 iOS 宿主（`NativeKitBootstrap.swift`）通过 `NativeViewRegistry.register(...)` / `NativeViewRegistry.registerView(...)` 按 `viewType` 注册对应原生视图构造器；
- **跨端声明式挂载与回收**：共享 UI 层通过 `NativeViewHost(request = NativeViewRequest(viewType, instanceKey, params))` 按 `(viewType, instanceKey)` 隔离创建独立 `View`/`UIView`，参数变更触发 `onUpdate`，离开组合树时由 `onRelease` 幂等清理资源。

### 使用示例

```kotlin
// 1. 逻辑句柄作用域绑定（Compose）
val session = rememberScopedNativeHandle(key = roomId) {
    roomFactory.create(RoomConfig(roomId = roomId))
}

// 2. 原生 UI 视图多实例挂载（commonMain）
NativeViewHost(
    request =
        NativeViewRequest(
            viewType = "native_video_player",
            instanceKey = videoItem.id,
            params = persistentMapOf("url" to videoItem.url),
        ),
    modifier = Modifier.fillMaxWidth().height(220.dp),
)
```

```swift
// iOS 宿主注册（NativeKitBootstrap.swift）
NativeViewRegistry.shared.registerView(
    viewType: "native_video_player",
    create: { request in
        let playerView = NativePlayerView()
        playerView.update(url: request.params["url"] ?? "")
        return playerView
    },
    onUpdate: { view, request in
        (view as? NativePlayerView)?.update(url: request.params["url"] ?? "")
    },
    onRelease: { view in
        (view as? NativePlayerView)?.releasePlayer()
    }
)
```

## 权衡与约束

### 正面影响
* 彻底解决多实例并发隔离与页面级资源泄漏问题，同时零破坏现有范式 A（9 大纯 `commonMain` Kit）与 `verifyArchitecture` 架构红线；
* `rememberScopedNativeHandle`、`NativeViewRequest` 与 `NativeViewRegistry` 在 `commonTest` 中可直接基于 Fake 契约验证；
* `:core:ui` 通过 `:shared` 的 Swift `Shared.framework` 自动导出给 `iosApp`，Swift 侧直接以 `NativeViewRegistry.shared.registerView(...)` 零样板接入。

### 边界约束
* 通过 `NativeViewRequest.params` 传递的参数统一使用 `ImmutableMap<String, String>`（满足 Detekt `UnstableCollections`），复杂结构建议序列化或先通过局部 `ScopedNativeHandle` 管理后再用 ID 绑定视图；
* 各端宿主 `register` / `registerView` 中捕获的实例引用须注意弱引用或依赖 `onRelease` 主动释放，避免原生 View 与控制器发生循环引用。
