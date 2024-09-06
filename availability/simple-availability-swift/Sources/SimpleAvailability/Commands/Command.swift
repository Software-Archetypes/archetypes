import Foundation
import Sugar

protocol Command: Decodable {
    static var type: String { get }
}

extension Command {
    static var type: String {
        String(describing: self).formatted(.snakeCase).uppercased()
    }
}
