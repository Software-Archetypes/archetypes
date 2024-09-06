import Testing
@testable import SimpleAvailability

@Suite
struct CommandTests {
    @Test func LoremIpsum_type() {
        #expect(LoremIpsum.type == "LOREM_IPSUM")
    }

    @Test func Dolor_type() {
        #expect(Dolor.type == "DOLOR_SIT_AMET")
    }

    @Test func Activate_type() {
        #expect(LoremIpsum.type == "LOREM_IPSUM")
    }

    @Test func Lock_type() {
        #expect(Lock.type == "LOCK")
    }

    @Test func LockIndefinitely_type() {
        #expect(LockIndefinitely.type == "LOCK_INDEFINITELY")
    }

    @Test func Register_type() {
        #expect(Register.type == "REGISTER")
    }

    @Test func Unlock_type() {
        #expect(Unlock.type == "UNLOCK")
    }

    @Test func Withdraw_type() {
        #expect(Withdraw.type == "WITHDRAW")
    }
}

private struct LoremIpsum: Command {}

private struct Dolor: Command {
    static var type: String {
        "DOLOR_SIT_AMET"
    }
}
