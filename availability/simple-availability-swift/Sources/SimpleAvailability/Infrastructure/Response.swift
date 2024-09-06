import Foundation

struct Response: Encodable {
    let status: Int
    let body: any Encodable

    enum CodingKeys: String, CodingKey {
        case status
        case payload
    }

    func encode(to encoder: any Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)

        try container.encode(status, forKey: .status)
        try container.encode(body, forKey: .payload)
    }
}

extension Response {
    var json: String {
        let encoder = JSONEncoder()
        encoder.outputFormatting = [.prettyPrinted, .sortedKeys, .withoutEscapingSlashes]
        encoder.keyEncodingStrategy = .convertToSnakeCase

        let data = try? encoder.encode(self)

        return String(data: data ?? Data(), encoding: .utf8) ?? ""
    }
}
