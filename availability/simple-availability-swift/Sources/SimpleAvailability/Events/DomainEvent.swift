import Foundation

protocol DomainEvent {
    static var type: String { get }
}

extension DomainEvent {
    static var type: String {
        String(describing: self).formatted(.snakeCase).uppercased()
    }
}
