// swift-tools-version: 6.0

import PackageDescription

let package = Package(
    name: "SimpleAvailability",
    platforms: [
        .macOS(.v15),
        .iOS(.v16)
    ],
    products: [
        .library(
            name: "SimpleAvailability",
            targets: [
                "SimpleAvailability"
            ]
        )
    ],
    dependencies: [
        .package(url: "https://github.com/korpotron/sugar.git", exact: "0.0.10")
    ],
    targets: [
        .target(
            name: "SimpleAvailability",
            dependencies: [
                .product(name: "Sugar", package: "sugar")
            ]
        ),
        .testTarget(
            name: "SimpleAvailabilityTests",
            dependencies: [
                "SimpleAvailability"
            ]
        )
    ]
)
