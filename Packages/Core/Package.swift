// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "Core",
    platforms: [
        .iOS(.v17),
        .macOS(.v14)
    ],
    products: [
        .library(name: "CoreModel", targets: ["CoreModel"]),
        .library(name: "CoreUI", targets: ["CoreUI"]),
        .library(name: "CoreNavigation", targets: ["CoreNavigation"]),
        .library(name: "CoreNetwork", targets: ["CoreNetwork"]),
        .library(name: "CoreStorage", targets: ["CoreStorage"]),
        .library(name: "CoreDI", targets: ["CoreDI"]),
        .library(name: "CoreAnalytics", targets: ["CoreAnalytics"]),
        .library(name: "CoreAds", targets: ["CoreAds"]),
        .library(name: "CorePay", targets: ["CorePay"]),
        .library(name: "CorePush", targets: ["CorePush"]),
        .library(name: "CoreLog", targets: ["CoreLog"]),
        .library(name: "CoreAbTesting", targets: ["CoreAbTesting"]),
        .library(name: "CoreCrashReport", targets: ["CoreCrashReport"]),
        .library(name: "CoreTesting", targets: ["CoreTesting"]),
    ],
    dependencies: [],
    targets: [
        // 1. CoreModel
        .target(
            name: "CoreModel",
            dependencies: []
        ),

        // 2. CoreDI (依赖注入中心)
        .target(
            name: "CoreDI",
            dependencies: []
        ),

        // 3. CoreLog (结构化日志)
        .target(
            name: "CoreLog",
            dependencies: []
        ),

        // 4. CoreUI (基础设计系统)
        .target(
            name: "CoreUI",
            dependencies: ["CoreModel"]
        ),

        // 5. CoreNavigation (类型安全路由)
        .target(
            name: "CoreNavigation",
            dependencies: ["CoreModel", "CoreLog"]
        ),

        // 6. CoreNetwork (async-await 网络层)
        .target(
            name: "CoreNetwork",
            dependencies: ["CoreModel", "CoreLog"]
        ),

        // 7. CoreStorage (本地持久化)
        .target(
            name: "CoreStorage",
            dependencies: ["CoreModel", "CoreLog"]
        ),

        // 8. CoreAnalytics (埋点门面)
        .target(
            name: "CoreAnalytics",
            dependencies: ["CoreLog"]
        ),

        // 9. CoreAds (广告门面)
        .target(
            name: "CoreAds",
            dependencies: ["CoreLog"]
        ),

        // 10. CorePay (StoreKit 2 支付门面)
        .target(
            name: "CorePay",
            dependencies: ["CoreLog"]
        ),

        // 11. CorePush (通知调度门面)
        .target(
            name: "CorePush",
            dependencies: ["CoreLog"]
        ),

        // 12. CoreAbTesting (A/B 测试门面)
        .target(
            name: "CoreAbTesting",
            dependencies: ["CoreLog"]
        ),

        // 13. CoreCrashReport (崩溃上报门面)
        .target(
            name: "CoreCrashReport",
            dependencies: ["CoreLog"]
        ),

        // 14. CoreTesting (测试辅助与 Fake 实现)
        .target(
            name: "CoreTesting",
            dependencies: ["CoreModel", "CoreNetwork", "CoreStorage"]
        ),

        // 测试 Target
        .testTarget(
            name: "CoreTests",
            dependencies: [
                "CoreModel",
                "CoreDI",
                "CoreNavigation",
                "CoreNetwork",
                "CoreStorage",
                "CoreAnalytics",
                "CoreLog",
                "CoreTesting",
            ]
        ),
    ]
)
