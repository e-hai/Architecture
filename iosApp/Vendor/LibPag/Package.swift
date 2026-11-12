// swift-tools-version: 5.9
import PackageDescription

/**
 * 本地 Swift Package 包装器：通过官方 Release BinaryTarget 分发 libpag.xcframework，
 * 避免将 8MB 二进制文件提交至 Git 仓库，确保 XcodeGen / Xcode SPM 开箱自动拉取并嵌入。
 */
let package = Package(
    name: "LibPag",
    platforms: [
        .iOS(.v13),
    ],
    products: [
        .library(
            name: "libpag",
            targets: ["libpag"],
        ),
    ],
    targets: [
        .binaryTarget(
            name: "libpag",
            url: "https://github.com/Tencent/libpag/releases/download/v4.5.102/libpag_4.5.102_ios_arm64_x86_64.zip",
            checksum: "c8615ae4891440e3c24c3622b7150d3fd4a041d0108edf33429a3193d0339d18",
        ),
    ],
)
