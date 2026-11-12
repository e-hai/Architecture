// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "iosApp",
    platforms: [
        .iOS(.v17)
    ],
    products: [
        .library(name: "iosAppLib", targets: ["iosAppLib"])
    ],
    dependencies: [
        // [KIT:log:START]
        .package(url: "https://github.com/e-hai/LogKit-iOS.git", from: "1.0.0"),
        // [KIT:log:END]
        // [KIT:crashreport:START]
        .package(url: "https://github.com/e-hai/CrashReportKit-iOS.git", from: "1.0.0"),
        // [KIT:crashreport:END]
        // [KIT:analytics:START]
        .package(url: "https://github.com/e-hai/AnalyticsKit-iOS.git", from: "1.0.0"),
        // [KIT:analytics:END]
        // [KIT:abtesting:START]
        .package(url: "https://github.com/e-hai/AbTestingKit-iOS.git", from: "1.0.0"),
        // [KIT:abtesting:END]
        // [KIT:mmp:START]
        .package(url: "https://github.com/e-hai/MmpKit-iOS.git", from: "1.0.0"),
        // [KIT:mmp:END]
        // [KIT:ads:START]
        .package(url: "https://github.com/e-hai/AdsKit-iOS.git", from: "1.0.2"),
        // [KIT:ads:END]
        // [KIT:pay:START]
        .package(url: "https://github.com/e-hai/PayKit-iOS.git", from: "1.1.1"),
        // [KIT:pay:END]
        // [KIT:push:START]
        .package(url: "https://github.com/e-hai/PushKit-iOS.git", from: "1.0.0"),
        // [KIT:push:END]
        // [KIT:video:START]
        .package(url: "https://github.com/e-hai/VideoKit-iOS.git", from: "1.0.0"),
        // [KIT:video:END]
    ],
    targets: [
        .target(
            name: "iosAppLib",
            dependencies: [
                // [KIT:log:START]
                .product(name: "LogKit", package: "LogKit-iOS"),
                // [KIT:log:END]
                // [KIT:crashreport:START]
                .product(name: "CrashReportKit", package: "CrashReportKit-iOS"),
                // [KIT:crashreport:END]
                // [KIT:analytics:START]
                .product(name: "Analytics", package: "AnalyticsKit-iOS"),
                // [KIT:analytics:END]
                // [KIT:abtesting:START]
                .product(name: "AbTesting", package: "AbTestingKit-iOS"),
                // [KIT:abtesting:END]
                // [KIT:mmp:START]
                .product(name: "MmpCore", package: "MmpKit-iOS"),
                // [KIT:mmp:END]
                // [KIT:ads:START]
                .product(name: "AdsKit", package: "AdsKit-iOS"),
                // [KIT:ads:END]
                // [KIT:pay:START]
                .product(name: "PayKit", package: "PayKit-iOS"),
                // [KIT:pay:END]
                // [KIT:push:START]
                .product(name: "PushKit", package: "PushKit-iOS"),
                // [KIT:push:END]
                // [KIT:video:START]
                .product(name: "VideoKit", package: "VideoKit-iOS"),
                // [KIT:video:END]
            ],
            path: "Sources",
            exclude: [
                "Info.plist",
                "like.pag",
            ]
        ),
        .testTarget(
            name: "iosAppTests",
            dependencies: [
                "iosAppLib",
            ],
            path: "Tests"
        )
    ]
)
