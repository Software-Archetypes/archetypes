import Foundation
@testable import SimpleAvailability

final class InMemoryDomainEventsPublisher: DomainEventsPublisher {
    struct Wrapper {
        let id: UUID
        let occurredAt: Date
        let event: any DomainEvent
    }

    private(set) var events: [Wrapper] = []

    func publish(event: any DomainEvent) {
        let wrapper = Wrapper(id: .random, occurredAt: .now, event: event)
        events.append(wrapper)
    }

    func cleanup() {
        events = []
    }

    func events<T: DomainEvent>(of type: T.Type) -> [T] {
        events.map(\.event).compactMap { $0 as? T }
    }

    func events(where predicate: (any DomainEvent) throws -> Bool) rethrows -> [any DomainEvent] {
        try events.map(\.event).filter(predicate)
    }

    func contains(where predicate: (any DomainEvent) throws -> Bool) rethrows -> Bool {
        try events.map(\.event).contains(where: predicate)
    }

    func contains<T: DomainEvent>(of type: T.Type, where predicate: (T) throws -> Bool) rethrows -> Bool {
        try events(of: type).contains(where: predicate)
    }
}
