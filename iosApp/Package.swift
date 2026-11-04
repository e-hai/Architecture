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
        .package(url: "https://github.com/e-hai/LogKit-iOS.git", from: "1.0.0"),
        .package(url: "https://github.com/e-hai/CrashReportKit-iOS.git", from: "1.0.0"),
        .package(url: "https://github.com/e-hai/AnalyticsKit-iOS.git", from: "1.0.0"),
        .package(url: "https://github.com/e-hai/AbTestingKit-iOS.git", from: "1.0.0"),
        .package(url: "https://github.com/e-hai/MmpKit-iOS.git", from: "1.0.0"),
        .package(url: "https://github.com/e-hai/AdsKit-iOS.git", from: "1.0.0"),
        .package(url: "https://github.com/e-hai/PayKit-iOS.git", from: "1.0.0"),
        .package(url: "https://github.com/e-hai/PushKit-iOS.git", from: "1.0.0"),
    ],
    targets: [
        .target(
            name: "iosAppLib",
            dependencies: [
                .product(name: "LogKit", package: "LogKit-iOS"),
                .product(name: "CrashReportKit", package: "CrashReportKit-iOS"),
                .product(name: "AnalyticsKit", package: "AnalyticsKit-iOS"),
                .product(name: "AbTestingKit", package: "AbTestingKit-iOS"),
                .product(name: "MmpCore", package: "MmpKit-iOS"),
                .product(name: "AdsCore", package: "AdsKit-iOS"),
                .product(name: "PayKit", package: "PayKit-iOS"),
                .product(name: "PushKit", package: "PushKit-iOS"),
            ],
            path: "Sources"
        )
    ]
)
