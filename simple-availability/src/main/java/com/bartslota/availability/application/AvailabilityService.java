package com.bartslota.availability.application;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bartslota.availability.common.Result;
import com.bartslota.availability.domain.AssetAvailability;
import com.bartslota.availability.domain.AssetAvailabilityRepository;
import com.bartslota.availability.domain.AssetId;
import com.bartslota.availability.domain.OwnerId;
import com.bartslota.availability.events.AssetActivated;
import com.bartslota.availability.events.AssetActivationRejected;
import com.bartslota.availability.events.AssetLockRejected;
import com.bartslota.availability.events.AssetLocked;
import com.bartslota.availability.events.AssetRegistered;
import com.bartslota.availability.events.AssetRegistrationRejected;
import com.bartslota.availability.events.AssetUnlocked;
import com.bartslota.availability.events.AssetUnlockingRejected;
import com.bartslota.availability.events.AssetWithdrawalRejected;
import com.bartslota.availability.events.AssetWithdrawn;
import com.bartslota.availability.events.DomainEvent;
import com.bartslota.availability.events.DomainEventsPublisher;

@Transactional
@Service
public class AvailabilityService {

    private final AssetAvailabilityRepository repository;
    private final DomainEventsPublisher domainEventsPublisher;

    AvailabilityService(AssetAvailabilityRepository repository, DomainEventsPublisher domainEventsPublisher) {
        this.repository = repository;
        this.domainEventsPublisher = domainEventsPublisher;
    }

    public Result<AssetRegistrationRejected, AssetRegistered> registerAssetWith(AssetId assetId) {
        Optional<AssetAvailability> existingAsset = repository.findBy(assetId);
        if (existingAsset.isEmpty()) {
            repository.save(AssetAvailability.of(assetId));
            AssetRegistered event = AssetRegistered.from(assetId);
            domainEventsPublisher.publish(event);
            return Result.success(event);
        } else {
            return Result.failure(AssetRegistrationRejected.dueToAlreadyExistingAssetWith(assetId));
        }
    }

    public Result<AssetActivationRejected, AssetActivated> activate(AssetId assetId) {
        return repository
                .findBy(assetId)
                .map(asset -> handle(asset, asset.activate()))
                .orElse(Result.failure(AssetActivationRejected.dueToMissingAssetWith(assetId)));
    }

    public Result<AssetWithdrawalRejected, AssetWithdrawn> withdraw(AssetId assetId) {
        return repository
                .findBy(assetId)
                .map(asset -> handle(asset, asset.withdraw()))
                .orElse(Result.failure(AssetWithdrawalRejected.dueToMissingAssetWith(assetId)));
    }

    public Result<AssetLockRejected, AssetLocked> lock(AssetId assetId, OwnerId ownerId, Duration time) {
        return repository
                .findBy(assetId)
                .map(asset -> handle(asset, asset.lockFor(ownerId, time)))
                .orElse(Result.failure(AssetLockRejected.dueToMissingAssetWith(assetId, ownerId)));
    }

    public Result<AssetLockRejected, AssetLocked> lockIndefinitely(AssetId assetId, OwnerId ownerId) {
        return repository
                .findBy(assetId)
                .map(asset -> handle(asset, asset.lockIndefinitelyFor(ownerId)))
                .orElse(Result.failure(AssetLockRejected.dueToMissingAssetWith(assetId, ownerId)));
    }

    public Result<AssetUnlockingRejected, AssetUnlocked> unlock(AssetId assetId, OwnerId ownerId, LocalDateTime at) {
        return repository
                .findBy(assetId)
                .map(asset -> handle(asset, asset.unlockFor(ownerId, at)))
                .orElse(Result.failure(AssetUnlockingRejected.dueToMissingAssetWith(assetId, ownerId)));
    }

    public void unlockIfOverdue(AssetAvailability assetAvailability) {
        assetAvailability.unlockIfOverdue()
                         .ifPresent(event -> {
                             repository.save(assetAvailability);
                             domainEventsPublisher.publish(event);
                         });
    }

    private <T extends DomainEvent, U extends DomainEvent> Result<T, U> handle(AssetAvailability asset, Result<T, U> executionResult) {
        if (executionResult.success()) {
            repository.save(asset);
            domainEventsPublisher.publish(executionResult.getSuccess());
        }
        return executionResult;
    }
}
