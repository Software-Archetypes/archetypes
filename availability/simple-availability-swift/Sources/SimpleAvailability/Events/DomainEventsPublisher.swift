import Foundation

protocol DomainEventsPublisher {
    func publish(event: DomainEvent)
}
