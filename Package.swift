// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "Architecture",
    platforms: [
        .iOS(.v17),
        .macOS(.v14)
    ],
    products: [
        .library(name: "AppLib", targets: ["AppLib"])
    ],
    dependencies: [
        .package(path: "Packages/Core")
    ],
    targets: [
        .target(
            name: "AppLib",
            dependencies: [
                .product(name: "CoreModel", package: "Core"),
                .product(name: "CoreUI", package: "Core"),
                .product(name: "CoreNavigation", package: "Core"),
                .product(name: "CoreNetwork", package: "Core"),
                .product(name: "CoreStorage", package: "Core"),
                .product(name: "CoreDI", package: "Core"),
                .product(name: "CoreAnalytics", package: "Core"),
                .product(name: "CoreAds", package: "Core"),
                .product(name: "CorePay", package: "Core"),
                .product(name: "CorePush", package: "Core"),
                .product(name: "CoreLog", package: "Core"),
                .product(name: "CoreAbTesting", package: "Core"),
                .product(name: "CoreCrashReport", package: "Core"),
            ],
            path: "App/Sources"
        ),
        .testTarget(
            name: "AppTests",
            dependencies: [
                "AppLib",
                .product(name: "CoreTesting", package: "Core")
            ],
            path: "App/Tests"
        )
    ]
)
