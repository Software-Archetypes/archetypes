import Foundation
import XCTest
@testable import SimpleAvailability

extension AssetId {
    static var fixture: AssetId {
        .init(value: "asset-id:\(UUID.random)")
    }
}

extension AssetAvailability {
    static var fixture: AssetAvailability {
        .init(assetId: .fixture, clock: .init { Date.now })
    }

    func thatIsActive() -> Self {
        _ = activate()
        return self
    }

    func thatWasLockedBySomeOwner() -> Self {
        _ = lockFor(ownerId: .Fixture.some, time: Duration.Fixture.some)
        return self
    }

    func thatWasLockedByOwnerWithForSomeValidDuration(ownerId: OwnerId) -> Self {
        _ = lockFor(ownerId: ownerId, time: Duration.Fixture.some)
        return self
    }

    func thenUnlocked(ownerId: OwnerId) -> Self {
        _ = unlockFor(ownerId: ownerId, at: .now)
        return self
    }
}

extension OwnerId {
    enum Fixture {
        static var some: OwnerId {
            .init(value: "owner-id:\(UUID.random)")
        }
    }
}

extension Duration {
    enum Fixture {
        static var some: Duration {
            .minutes((1...15).randomElement()!)
        }
    }
}
