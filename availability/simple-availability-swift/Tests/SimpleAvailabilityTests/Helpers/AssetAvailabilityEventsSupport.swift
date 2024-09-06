//import Foundation
//
//protocol AssetAvailabilityEventsSupport {
//    
//}
//
//
//trait  {
//
//    DomainEventsPublisher publisher = new InMemoryDomainEventPublisher()
//
//    boolean assetRegisteredEventWasPublishedFor(AssetId assetId) {
//        publisher.thereWasAnEventFulfilling { DomainEvent e -> e instanceof AssetRegistered && AssetId.of(e.getAssetId()) == assetId }
//    }
//
//    boolean assetWithdrawnEventWasPublishedFor(AssetId id) {
//        publisher.thereWasAnEventFulfilling { DomainEvent e -> e instanceof AssetWithdrawn && AssetId.of(e.getAssetId()) == id }
//    }
//
//    boolean assetLockedEventWasPublishedFor(AssetId assetId, OwnerId ownerId) {
//        publisher.thereWasAnEventFulfilling { DomainEvent e -> e instanceof AssetLocked && AssetId.of(e.getAssetId()) == assetId && OwnerId.of(e.getOwnerId()) == ownerId }
//    }
//
//    boolean assetUnlockedEventWasPublishedFor(AssetId assetId, OwnerId ownerId) {
//        publisher.thereWasAnEventFulfilling { DomainEvent e -> e instanceof AssetUnlocked && AssetId.of(e.getAssetId()) == assetId && OwnerId.of(e.getOwnerId()) == ownerId }
//    }
//}
