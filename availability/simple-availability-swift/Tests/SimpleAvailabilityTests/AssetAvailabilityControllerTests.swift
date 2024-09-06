import Foundation
import Testing
import Sugar
@testable import SimpleAvailability

@Suite struct AssetAvailabilityControllerTests {
    func prepare() -> (AssetAvailabilityController, InMemoryAssetAvailabilityRepository, Supplier<Date>) {
        let clock = Supplier {
            try! Date("2001-02-03T04:05:06Z", strategy: .iso8601)
        }
        let publisher = InMemoryDomainEventsPublisher()
        let repository = InMemoryAssetAvailabilityRepository()
        let service = AvailabilityService(repository: repository, publisher: publisher, clock: clock)
        let controller = AssetAvailabilityController(service: service, clock: clock)

        return (controller, repository, clock)
    }

    @Test func should_return_500_for_unknown_command() throws {
        let (controller, _, _) = prepare()

        struct Unknown: Command {}

        let command = Unknown()
        let response = controller.handle(command, Principal(name: "Marek"))

        #expect(response.status == 500)
        #expect(response.json == """
        {
          "payload" : {
            "message" : "Given command is not supported."
          },
          "status" : 500
        }
        """)
    }

    @Test func should_accept_the_registration_of_new_asset() throws {
        let (controller, _, _) = prepare()

        let command = Register(assetId: "12345")
        let response = controller.handle(command, Principal(name: "Marek"))

        #expect(response.status == 202)
        #expect(response.json == """
        {
          "payload" : {
            "asset_id" : "12345"
          },
          "status" : 202
        }
        """)
    }

    @Test func should_reject_the_registration_of_already_existing_asset() throws {
        let (controller, repository, clock) = prepare()
        repository.save(assetAvailability: AssetAvailability(assetId: AssetId(value: "12345"), clock: clock))

        let command = Register(assetId: "12345")
        let response = controller.handle(command, Principal(name: "Marek"))

        #expect(response.status == 422)
        #expect(response.json == """
        {
          "payload" : {
            "asset_id" : "12345",
            "reason" : "ASSET_ALREADY_EXISTS"
          },
          "status" : 422
        }
        """)
    }

    @Test func should_accept_the_withdrawal_of_existing_asset() throws {
        let (controller, repository, clock) = prepare()
        repository.save(assetAvailability: AssetAvailability(assetId: AssetId(value: "42"), clock: clock))

        let command = Withdraw(assetId: "42")
        let response = controller.handle(command, Principal(name: "Marek"))

        #expect(response.status == 202)
        #expect(response.json == """
        {
          "payload" : {
            "asset_id" : "42"
          },
          "status" : 202
        }
        """)
    }

    @Test func should_reject_the_withdrawal_of_locked_asset() throws {
        let (controller, repository, _) = prepare()
        let asset = AssetAvailability.fixture
            .thatIsActive()
            .thatWasLockedBySomeOwner()
        repository.save(assetAvailability: asset)

        let command = Withdraw(assetId: "42")
        let response = controller.handle(command, Principal(name: "Marek"))

        #expect(response.status == 422)
        #expect(response.json == """
        {
          "payload" : {
            "asset_id" : "42",
            "reason" : "ASSET_IS_MISSING"
          },
          "status" : 422
        }
        """)
    }

    @Test func should_accept_the_activation_of_registered_asset() throws {
        let (controller, repository, clock) = prepare()
        let asset = AssetAvailability(assetId: AssetId(value: "42"), clock: clock)
        repository.save(assetAvailability: asset)

        let command = Activate(assetId: "42")
        let response = controller.handle(command, Principal(name: "Marek"))

        #expect(response.status == 202)
        #expect(response.json == """
        {
          "payload" : {
            "asset_id" : "42"
          },
          "status" : 202
        }
        """)
    }

    @Test func should_reject_the_activation_of_not_existing_asset() throws {
        let (controller, _, _) = prepare()

        let command = Activate(assetId: "666")
        let response = controller.handle(command, Principal(name: "Marek"))

        #expect(response.status == 422)
        #expect(response.json == """
        {
          "payload" : {
            "asset_id" : "666",
            "reason" : "ASSET_IS_MISSING"
          },
          "status" : 422
        }
        """)
    }

    @Test func should_accept_the_locking_of_an_activated_asset() throws {
        let (controller, repository, clock) = prepare()
        let asset = AssetAvailability(assetId: AssetId(value: "42"), clock: clock)
            .thatIsActive()
        repository.save(assetAvailability: asset)
        let duration: Duration = .minutes(35)

        let command = Lock(assetId: "42", duration: duration)
        let response = controller.handle(command, Principal(name: "Marek"))

        #expect(response.status == 202)
        #expect(response.json == """
        {
          "payload" : {
            "asset_id" : "42",
            "owner_id" : "Marek",
            "valid_until" : 2868006
          },
          "status" : 202
        }
        """)
    }

    @Test func should_reject_the_locking_of_locked_asset() throws {
        let (controller, repository, _) = prepare()
        let asset = AssetAvailability.fixture
            .thatIsActive()
            .thatWasLockedBySomeOwner()
        repository.save(assetAvailability: asset)
        let duration: Duration = .minutes(35)

        let command = Lock(assetId: "42", duration: duration)
        let response = controller.handle(command, Principal(name: "Marek"))

        #expect(response.status == 422)
        #expect(response.json == """
        {
          "payload" : {
            "asset_id" : "42",
            "owner_id" : "Marek",
            "reason" : "ASSET_IS_MISSING"
          },
          "status" : 422
        }
        """)
    }

    @Test func should_accept_the_indefinite_locking_of_already_locked_asset() throws {
        let (controller, repository, clock) = prepare()
        let asset = AssetAvailability(assetId: AssetId(value: "42"), clock: clock)
            .thatIsActive()
            .thatWasLockedByOwnerWithForSomeValidDuration(ownerId: OwnerId(value: "Adam"))
        repository.save(assetAvailability: asset)

        let command = LockIndefinitely(assetId: "42")
        let response = controller.handle(command, Principal(name: "Adam"))

        #expect(response.status == 202)
        #expect(response.json == """
        {
          "payload" : {
            "asset_id" : "42",
            "owner_id" : "Adam",
            "valid_until" : 34401906
          },
          "status" : 202
        }
        """)
    }

    @Test func should_reject_the_indefinite_locking_of_asset_locked_by_someone_else() throws {
        let (controller, repository, clock) = prepare()
        let asset = AssetAvailability(assetId: AssetId(value: "42"), clock: clock)
            .thatIsActive()
            .thatWasLockedByOwnerWithForSomeValidDuration(ownerId: OwnerId(value: "Bob"))
        repository.save(assetAvailability: asset)

        let command = LockIndefinitely(assetId: "42")
        let response = controller.handle(command, Principal(name: "Adam"))

        #expect(response.status == 422)
        #expect(response.json == """
        {
          "payload" : {
            "asset_id" : "42",
            "owner_id" : "Adam",
            "reason" : "NO_LOCK_DEFINED_FOR_OWNER_REASON"
          },
          "status" : 422
        }
        """)
    }

    @Test func should_accept_the_unlocking_of_already_locked_asset() throws {
        let (controller, repository, clock) = prepare()
        let asset = AssetAvailability(assetId: AssetId(value: "42"), clock: clock)
            .thatIsActive()
            .thatWasLockedByOwnerWithForSomeValidDuration(ownerId: OwnerId(value: "Bob"))
        repository.save(assetAvailability: asset)

        let command = Unlock(assetId: "42")
        let response = controller.handle(command, Principal(name: "Bob"))

        #expect(response.status == 202)
        #expect(response.json == """
        {
          "payload" : {
            "asset_id" : "42",
            "owner_id" : "Bob",
            "unlocked_at" : 2865906
          },
          "status" : 202
        }
        """)
    }

    @Test func should_reject_the_unlocking_of_asset_locked_by_someone_else() throws {
        let (controller, repository, clock) = prepare()
        let asset = AssetAvailability(assetId: AssetId(value: "42"), clock: clock)
            .thatIsActive()
            .thatWasLockedByOwnerWithForSomeValidDuration(ownerId: OwnerId(value: "Bob"))
        repository.save(assetAvailability: asset)

        let command = Unlock(assetId: "42")
        let response = controller.handle(command, Principal(name: "Alice"))

        #expect(response.status == 422)
        #expect(response.json == """
        {
          "payload" : {
            "asset_id" : "42",
            "owner_id" : "Alice",
            "reason" : "NO_LOCK_ON_THE_ASSET_REASON"
          },
          "status" : 422
        }
        """)
    }

}
