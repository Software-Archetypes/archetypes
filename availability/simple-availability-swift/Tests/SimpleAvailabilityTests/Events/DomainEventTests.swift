import Testing
import Foundation
@testable import SimpleAvailability

@Suite
struct DomainEventTests {
    @Test func LoremIpsum_type() {
        #expect(LoremIpsumed.type == "LOREM_IPSUM")
    }

    @Test func Dolor_type() {
        #expect(Dolored.type == "DOLOR_SIT_AMET")
    }

    @Test func AssetActivated_type() {
        #expect(AssetActivated.type == "ASSET_ACTIVATED")
    }

    @Test func AssetActivationRejected_type() {
        #expect(AssetActivationRejected.type == "ASSET_ACTIVATION_REJECTED")
    }

    @Test func AssetLocked_type() {
        #expect(AssetLocked.type == "ASSET_LOCKED")
    }

    @Test func AssetLockExpired_type() {
        #expect(AssetLockExpired.type == "ASSET_LOCK_EXPIRED")
    }

    @Test func AssetLockRejected_type() {
        #expect(AssetLockRejected.type == "ASSET_LOCK_REJECTED")
    }

    @Test func AssetRegistered_type() {
        #expect(AssetRegistered.type == "ASSET_REGISTERED")
    }

    @Test func AssetRegistrationRejected_type() {
        #expect(AssetRegistrationRejected.type == "ASSET_REGISTRATION_REJECTED")
    }

    @Test func AssetUnlocked_type() {
        #expect(AssetUnlocked.type == "ASSET_UNLOCKED")
    }

    @Test func AssetUnlockingRejected_type() {
        #expect(AssetUnlockingRejected.type == "ASSET_UNLOCKING_REJECTED")
    }

    @Test func AssetWithdrawn_type() {
        #expect(AssetWithdrawn.type == "ASSET_WITHDRAWN")
    }

    @Test func AssetWithdrawalRejected_type() {
        #expect(AssetWithdrawalRejected.type == "ASSET_WITHDRAWAL_REJECTED")
    }
}

private struct LoremIpsumed: DomainEvent {
    static var type: String {
        "LOREM_IPSUM"
    }

    let id: UUID
    let occurredAt: Date
}

private struct Dolored: DomainEvent {
    static var type: String {
        "DOLOR_SIT_AMET"
    }

    let id: UUID
    let occurredAt: Date
}
