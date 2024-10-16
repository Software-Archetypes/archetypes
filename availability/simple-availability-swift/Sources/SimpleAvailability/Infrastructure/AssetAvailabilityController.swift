import Foundation
import Sugar

struct AssetAvailabilityController {
    let service: AvailabilityService
    let clock: Supplier<Date>

    func handle(_ request: Request) -> Response {
        do {
            let command = try command(from: request)
            return handle(command, Principal(name: "XXX"))
        } catch {
            return Response(status: 500, body: ServerError(message: error.localizedDescription))
        }
    }

    func handle(_ command: Command, _ principal: Principal) -> Response {
        switch command {
        case let command as Register:
            handle(command: command)
        case let command as Withdraw:
            handle(command: command)
        case let command as Activate:
            handle(command: command)
        case let command as Lock:
            handle(command: command, principal: principal)
        case let command as LockIndefinitely:
            handle(command: command, principal: principal)
        case let command as Unlock:
            handle(command: command, principal: principal)
        default:
            Response(status: 500, body: ServerError(message: "Given command is not supported."))
        }
    }

    private func handle(command: Register) -> Response {
        service
            .registerAssetWith(assetId: AssetId(value: command.assetId))
            .fold()
    }

    private func handle(command: Withdraw) -> Response {
        service
            .withdraw(assetId: AssetId(value: command.assetId))
            .fold()
    }

    private func handle(command: Activate) -> Response {
        service
            .activate(assetId: AssetId(value: command.assetId))
            .fold()
    }

    private func handle(command: Lock, principal: Principal) -> Response {
        service
            .lock(assetId: AssetId(value: command.assetId), ownerId: .of(principal), time: command.duration)
            .fold()
    }

    private func handle(command: LockIndefinitely, principal: Principal) -> Response {
        service
            .lockIndefinitely(assetId: AssetId(value: command.assetId), ownerId: .of(principal))
            .fold()
    }

    private func handle(command: Unlock, principal: Principal) -> Response {
        service
            .unlock(assetId: AssetId(value: command.assetId), ownerId: .of(principal), at: clock())
            .fold()
    }
}

private extension Result where Success: Encodable, Failure: Encodable {
    func fold() -> Response {
        fold { value in
            Response(status: 202, body: value)
        } failure: { value in
            Response(status: 422, body: value)
        }
    }
}

func command(from request: Request) throws -> any Command {
    let commands: [Command.Type] = [
        Register.self,
        Withdraw.self,
        Activate.self,
        Lock.self,
        LockIndefinitely.self,
        Unlock.self
    ]

    let type = commands.first { type in
        type.type == request.type
    }!

    let decoder = JSONDecoder()
    let object = try decoder.decode(type, from: request.data)

    return object
}

struct ServerError: Encodable {
    let message: String
}
